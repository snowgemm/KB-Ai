#!/usr/bin/env python3
"""Validate KB-Ai's privacy, IME, and Kotlin/Rust boundary invariants."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ANDROID_NS = "{http://schemas.android.com/apk/res/android}"


def check_architecture(root: Path) -> list[str]:
    failures: list[str] = []
    manifest_path = root / "app/src/main/AndroidManifest.xml"
    try:
        manifest = ET.parse(manifest_path).getroot()
    except (ET.ParseError, OSError) as error:
        return [f"Android manifest cannot be parsed: {error}"]

    permissions = [
        node.attrib.get(ANDROID_NS + "name", "<unnamed>")
        for node in manifest
        if node.tag.rsplit("}", 1)[-1].startswith("uses-permission")
    ]
    if permissions:
        failures.append(f"core keyboard must remain permission-free; found {permissions}")

    service = next(
        (
            node for node in manifest.findall("./application/service")
            if node.attrib.get(ANDROID_NS + "name", "").endswith("KeyboardInputMethodService")
        ),
        None,
    )
    if service is None:
        failures.append("KeyboardInputMethodService registration is missing")
    else:
        if service.attrib.get(ANDROID_NS + "exported") != "true":
            failures.append("input method service must be exported for Android IME discovery")
        if service.attrib.get(ANDROID_NS + "permission") != "android.permission.BIND_INPUT_METHOD":
            failures.append("input method service must require BIND_INPUT_METHOD")

    core_path = root / "native/crates/kb-core/src/lib.rs"
    core = core_path.read_text(encoding="utf-8") if core_path.is_file() else ""
    if "engine.set_full_text_tracking(false);" not in core:
        failures.append("Bamboo Core full-text tracking must remain disabled")

    jni_path = root / "native/crates/kb-ai-native/src/lib.rs"
    jni = jni_path.read_text(encoding="utf-8") if jni_path.is_file() else ""
    for contract in (
        "char::from_u32",
        "let mut sessions = lock_sessions();",
        ".get_mut(&handle)",
        "lock_sessions().remove(&handle)",
    ):
        if contract not in jni:
            failures.append(f"JNI safety contract is missing: {contract}")

    bridge_path = root / "app/src/main/java/com/snowgem/kbai/NativeBridge.kt"
    bridge = bridge_path.read_text(encoding="utf-8") if bridge_path.is_file() else ""
    for method in ("nativeCreateSession", "nativeProcessKey", "nativeDestroySession"):
        if method not in bridge:
            failures.append(f"Kotlin JNI declaration is missing: {method}")

    service_path = root / "app/src/main/java/com/snowgem/kbai/KeyboardInputMethodService.kt"
    service_source = service_path.read_text(encoding="utf-8") if service_path.is_file() else ""
    for lifecycle_call in ("createNativeSession()", "destroyNativeSession()"):
        if lifecycle_call not in service_source:
            failures.append(f"IME native-session lifecycle is missing: {lifecycle_call}")

    workflow_path = root / ".github/workflows/android.yml"
    workflow = workflow_path.read_text(encoding="utf-8") if workflow_path.is_file() else ""
    for gate in (
        "cargo fmt --all",
        "cargo test --workspace",
        "cargo clippy --workspace",
        "python3 -m unittest discover -s scripts",
        "python3 scripts/verify_architecture.py",
        ":app:lintDebug",
        ":app:testDebugUnitTest",
        ":app:assembleDebug",
    ):
        if gate not in workflow:
            failures.append(f"Android CI is missing required quality gate: {gate}")

    return failures


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    failures = check_architecture(root)
    if failures:
        for failure in failures:
            print(f"ARCHITECTURE VIOLATION: {failure}", file=sys.stderr)
        return 1
    print("KB-Ai architecture guard passed: manifest privacy, IME protection, native boundaries, and CI gates.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
