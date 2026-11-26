#!/usr/bin/env python3
"""Deterministic acceptance evaluations for AI-authored repository changes.

The runner evaluates the checked-out artifact. It does not invoke a language
model, inspect hidden reasoning, or execute commands supplied by evaluation data.
"""
from __future__ import annotations

import argparse
from datetime import datetime, timezone
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from typing import Any

WEIGHTS = {"critical": 3, "high": 2, "normal": 1}
SUPPORTED_CHECKS = {
    "file_exists", "file_contains", "file_not_contains", "test_method",
    "tree_count_text", "manifest_permission_allowlist", "manifest_no_permissions",
    "workflow_read_only", "workflow_actions_pinned_sha", "workflow_contains_all",
}
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"


def safe_repo_path(root: Path, relative: str) -> Path:
    if not isinstance(relative, str) or not relative or "\\" in relative:
        raise ValueError("path must be a non-empty repository-relative path")
    candidate = Path(relative)
    if candidate.is_absolute() or ".." in candidate.parts:
        raise ValueError("path must not be absolute or traverse parent directories")
    resolved = (root / candidate).resolve()
    try:
        resolved.relative_to(root.resolve())
    except ValueError as error:
        raise ValueError("path resolves outside the repository") from error
    return resolved


def read_repo_file(root: Path, relative: str) -> str:
    path = safe_repo_path(root, relative)
    if not path.is_file():
        raise FileNotFoundError(f"required file is missing: {relative}")
    return path.read_text(encoding="utf-8")


def manifest_permissions(path: Path) -> list[str]:
    manifest = ET.parse(path).getroot()
    return [
        node.attrib.get(ANDROID_NS + "name", "<unnamed>")
        for node in manifest
        if node.tag.rsplit("}", 1)[-1].startswith("uses-permission")
    ]


def check_one(root: Path, check: dict[str, Any]) -> tuple[bool, str]:
    kind = check.get("type")
    if kind not in SUPPORTED_CHECKS:
        return False, f"unsupported check type: {kind}"

    if kind == "file_exists":
        path = safe_repo_path(root, check.get("path"))
        passed = path.is_file()
        return passed, "file exists" if passed else "file is missing"

    if kind in {"file_contains", "file_not_contains"}:
        content = read_repo_file(root, check.get("path"))
        values = check.get("texts", [check.get("text")])
        if not isinstance(values, list) or not values or not all(isinstance(v, str) and v for v in values):
            raise ValueError("texts must be a non-empty list of non-empty strings")
        found = [value for value in values if value in content]
        if kind == "file_contains":
            missing = [value for value in values if value not in content]
            return not missing, "all markers found" if not missing else f"missing markers: {missing}"
        return not found, "forbidden markers absent" if not found else f"forbidden markers found: {found}"

    if kind == "test_method":
        content = read_repo_file(root, check.get("path"))
        name = check.get("name")
        if not isinstance(name, str) or not name:
            raise ValueError("test_method requires a non-empty name")
        pattern = re.compile(r"\b(?:fun|fn)\s+" + re.escape(name) + r"\s*\(")
        passed = bool(pattern.search(content))
        return passed, f"test method {'found' if passed else 'not found'}: {name}"

    if kind == "tree_count_text":
        directory = safe_repo_path(root, check.get("path"))
        if not directory.is_dir():
            return False, f"source directory is missing: {check.get('path')}"
        needle = check.get("text")
        glob = check.get("glob", "*.kt")
        expected = check.get("equals")
        if not isinstance(needle, str) or not needle or not isinstance(glob, str) or not isinstance(expected, int):
            raise ValueError("tree_count_text requires text, glob, and integer equals")
        count = 0
        for file_path in directory.rglob(glob):
            if file_path.is_file():
                try:
                    file_path.resolve().relative_to(root.resolve())
                except ValueError:
                    continue
                count += file_path.read_text(encoding="utf-8").count(needle)
        return count == expected, f"found {count}; expected {expected}"

    if kind in {"manifest_permission_allowlist", "manifest_no_permissions"}:
        path = safe_repo_path(root, check.get("path"))
        if not path.is_file():
            return False, f"manifest is missing: {check.get('path')}"
        found = manifest_permissions(path)
        if kind == "manifest_no_permissions":
            return not found, "no permission declarations found" if not found else f"permissions found: {found}"
        allowed = check.get("allowed")
        if not isinstance(allowed, list) or not all(isinstance(item, str) for item in allowed):
            raise ValueError("manifest_permission_allowlist requires an allowed list")
        unexpected = sorted(set(found) - set(allowed))
        return not unexpected, f"allowlist satisfied ({len(found)} declarations)" if not unexpected else f"unreviewed permissions: {unexpected}"

    if kind == "workflow_read_only":
        content = read_repo_file(root, check.get("path"))
        lines = content.splitlines()
        start = next((index for index, line in enumerate(lines) if line == "permissions:"), None)
        if start is None:
            return False, "top-level permissions block is missing"
        entries: list[tuple[str, str]] = []
        for line in lines[start + 1:]:
            if line.strip() and not line[0].isspace():
                break
            match = re.match(r"^\s+([A-Za-z0-9_-]+):\s*([A-Za-z0-9_-]+)\s*$", line)
            if match:
                entries.append((match.group(1), match.group(2)))
        values = [value for _, value in entries]
        passed = bool(entries) and ("contents", "read") in entries and all(value in {"read", "none"} for value in values)
        return passed, f"permissions={entries}"

    if kind == "workflow_actions_pinned_sha":
        content = read_repo_file(root, check.get("path"))
        uses_values = []
        for line in content.splitlines():
            match = re.match(r"^\s*(?:-\s*)?uses:\s*([^\s#]+)", line)
            if match:
                uses_values.append(match.group(1))
        unpinned = [
            value for value in uses_values
            if not value.startswith("./") and not re.fullmatch(r".+@[0-9a-fA-F]{40}", value)
        ]
        return bool(uses_values) and not unpinned, f"checked {len(uses_values)} action references; unpinned={unpinned}"

    if kind == "workflow_contains_all":
        content = read_repo_file(root, check.get("path"))
        values = check.get("texts")
        if not isinstance(values, list) or not values or not all(isinstance(v, str) for v in values):
            raise ValueError("workflow_contains_all requires a non-empty texts list")
        missing = [value for value in values if value not in content]
        return not missing, "all required CI gates found" if not missing else f"missing CI gates: {missing}"

    return False, "unhandled check"


def current_commit(root: Path) -> str | None:
    try:
        result = subprocess.run(
            ["git", "rev-parse", "HEAD"], cwd=root, check=True,
            capture_output=True, text=True, timeout=5,
        )
        return result.stdout.strip()
    except (OSError, subprocess.SubprocessError):
        return None


def evaluate_suite(root: Path, suite: dict[str, Any]) -> dict[str, Any]:
    if suite.get("schema_version") != 1:
        raise ValueError("unsupported suite schema_version")
    project, cases = suite.get("project"), suite.get("cases")
    if not isinstance(project, str) or not project or not isinstance(cases, list) or not cases:
        raise ValueError("suite requires project and a non-empty cases list")

    seen: set[str] = set()
    results = []
    total_weight = passed_weight = 0
    for case in cases:
        if not isinstance(case, dict):
            raise ValueError("each evaluation case must be an object")
        case_id, severity, checks = case.get("id"), case.get("severity"), case.get("checks")
        if not isinstance(case_id, str) or not case_id or case_id in seen or severity not in WEIGHTS or not isinstance(checks, list) or not checks:
            raise ValueError(f"invalid or duplicate evaluation case: {case_id}")
        seen.add(case_id)
        checks_out = []
        for check in checks:
            if not isinstance(check, dict):
                checks_out.append({"id": "<invalid>", "type": "<invalid>", "passed": False, "evidence": "check must be an object"})
                continue
            try:
                passed, evidence = check_one(root, check)
            except (OSError, ValueError, ET.ParseError) as error:
                passed, evidence = False, f"{type(error).__name__}: {error}"
            checks_out.append({
                "id": check.get("id", "<unnamed>"),
                "type": check.get("type", "<missing>"),
                "passed": passed,
                "evidence": evidence,
            })
        case_passed = all(item["passed"] for item in checks_out)
        weight = WEIGHTS[severity]
        total_weight += weight
        passed_weight += weight if case_passed else 0
        results.append({
            "id": case_id, "title": case.get("title", case_id),
            "category": case.get("category", "general"), "severity": severity,
            "weight": weight, "passed": case_passed, "checks": checks_out,
        })

    passed_cases = sum(1 for item in results if item["passed"])
    return {
        "schema_version": 1,
        "project": project,
        "commit_sha": os.environ.get("GITHUB_SHA") or current_commit(root),
        "evaluated_at": datetime.now(timezone.utc).isoformat(),
        "status": "passed" if passed_cases == len(results) else "failed",
        "score": round(passed_weight * 100 / total_weight, 1) if total_weight else 0,
        "passed_cases": passed_cases,
        "failed_cases": len(results) - passed_cases,
        "total_cases": len(results),
        "cases": results,
        "scope_note": "Repository-artifact acceptance evaluation; it does not score an LLM's hidden reasoning or model quality.",
    }


def render_summary(report: dict[str, Any]) -> str:
    lines = [
        f"## Agent Evaluation — {report['project']}",
        "",
        f"**Status:** {report['status'].upper()}  ",
        f"**Score:** {report['score']}%  ",
        f"**Cases:** {report['passed_cases']}/{report['total_cases']} passed",
        "",
        "| Result | Case | Category | Risk | Checks |",
        "|---|---|---|---|---:|",
    ]
    for case in report["cases"]:
        status = "PASS" if case["passed"] else "FAIL"
        count = sum(1 for check in case["checks"] if check["passed"])
        lines.append(f"| {status} | {case['id']} — {case['title']} | {case['category']} | {case['severity']} | {count}/{len(case['checks'])} |")
    lines.extend(["", report["scope_note"], ""])
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=".", help="repository root")
    parser.add_argument("--suite", default="evals/agent-evals.json", help="evaluation suite JSON")
    parser.add_argument("--output", default="build/agent-evals/report.json", help="JSON report path")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    output = Path(args.output)
    if not output.is_absolute():
        output = root / output

    try:
        suite_path = safe_repo_path(root, args.suite)
        suite = json.loads(suite_path.read_text(encoding="utf-8"))
        report = evaluate_suite(root, suite)
    except (OSError, ValueError, json.JSONDecodeError) as error:
        report = {
            "schema_version": 1, "project": "unknown",
            "commit_sha": os.environ.get("GITHUB_SHA") or current_commit(root),
            "evaluated_at": datetime.now(timezone.utc).isoformat(),
            "status": "failed", "score": 0, "passed_cases": 0,
            "failed_cases": 1, "total_cases": 1, "cases": [],
            "error": f"{type(error).__name__}: {error}",
            "scope_note": "Evaluation suite could not be loaded; no acceptance claim is made.",
        }

    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    summary = render_summary(report)
    print(summary)
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        with open(summary_path, "a", encoding="utf-8") as stream:
            stream.write(summary)
    return 0 if report["status"] == "passed" else 1


if __name__ == "__main__":
    raise SystemExit(main())
