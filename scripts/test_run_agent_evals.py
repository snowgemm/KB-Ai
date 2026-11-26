"""Unit and mutation tests for the Agent Evaluation runner."""
from pathlib import Path
import tempfile
import unittest

from run_agent_evals import evaluate_suite


class AgentEvalRunnerTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        (self.root / "src").mkdir()
        (self.root / "src/policy.kt").write_text("fun privateTabsAreNotSaved() { }\n", encoding="utf-8")
        (self.root / "app").mkdir()
        (self.root / "app/AndroidManifest.xml").write_text(
            '<manifest xmlns:android="http://schemas.android.com/apk/res/android"><application /></manifest>',
            encoding="utf-8",
        )
        (self.root / ".github/workflows").mkdir(parents=True)
        (self.root / ".github/workflows/ci.yml").write_text(
            "permissions:\n  contents: read\n"
            "steps:\n  - uses: actions/checkout@0123456789abcdef0123456789abcdef01234567\n",
            encoding="utf-8",
        )

    def tearDown(self) -> None:
        self.temp.cleanup()

    def suite_with(self, checks: list[dict]) -> dict:
        return {
            "schema_version": 1,
            "project": "Fixture",
            "cases": [{"id": "FIXTURE-001", "title": "Fixture", "severity": "critical", "checks": checks}],
        }

    def test_valid_suite_passes_and_scores_one_hundred_percent(self) -> None:
        report = evaluate_suite(self.root, self.suite_with([
            {"id": "file", "type": "file_exists", "path": "src/policy.kt"},
            {"id": "method", "type": "test_method", "path": "src/policy.kt", "name": "privateTabsAreNotSaved"},
            {"id": "permissions", "type": "manifest_no_permissions", "path": "app/AndroidManifest.xml"},
            {"id": "workflow", "type": "workflow_read_only", "path": ".github/workflows/ci.yml"},
            {"id": "pins", "type": "workflow_actions_pinned_sha", "path": ".github/workflows/ci.yml"},
        ]))
        self.assertEqual(report["status"], "passed")
        self.assertEqual(report["score"], 100.0)
        self.assertEqual(report["passed_cases"], 1)

    def test_missing_acceptance_marker_fails_case(self) -> None:
        report = evaluate_suite(self.root, self.suite_with([
            {"id": "missing", "type": "file_contains", "path": "src/policy.kt", "text": "must-not-exist"}
        ]))
        self.assertEqual(report["status"], "failed")
        self.assertFalse(report["cases"][0]["checks"][0]["passed"])

    def test_rejects_path_traversal(self) -> None:
        report = evaluate_suite(self.root, self.suite_with([
            {"id": "escape", "type": "file_exists", "path": "../outside"}
        ]))
        self.assertEqual(report["status"], "failed")
        self.assertIn("ValueError", report["cases"][0]["checks"][0]["evidence"])

    def test_detects_api_scoped_manifest_permissions(self) -> None:
        manifest = self.root / "app/AndroidManifest.xml"
        manifest.write_text(
            manifest.read_text(encoding="utf-8").replace(
                "<application>",
                '<uses-permission-sdk-23 android:name="android.permission.INTERNET" /><application>',
            ),
            encoding="utf-8",
        )
        report = evaluate_suite(self.root, self.suite_with([
            {"id": "permissions", "type": "manifest_no_permissions", "path": "app/AndroidManifest.xml"}
        ]))
        self.assertEqual(report["status"], "failed")

    def test_unpinned_action_fails(self) -> None:
        workflow = self.root / ".github/workflows/ci.yml"
        workflow.write_text(
            "permissions:\n  contents: read\nsteps:\n  - uses: actions/checkout@v4\n",
            encoding="utf-8",
        )
        report = evaluate_suite(self.root, self.suite_with([
            {"id": "pins", "type": "workflow_actions_pinned_sha", "path": ".github/workflows/ci.yml"}
        ]))
        self.assertEqual(report["status"], "failed")


if __name__ == "__main__":
    unittest.main()
