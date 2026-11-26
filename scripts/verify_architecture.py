#!/usr/bin/env python3
"""Fail CI when KB-Ai's privacy and Android/native boundaries drift."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"


def fail(message: str) -> None:
    print(f"ARCHITECTURE VIOLATION: {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> None:
    manifest_path = ROOT / "app/src/main/AndroidManifest.xml"
    manifest = ET.parse(manifest_path).getroot()
    permissions = [
        node.attrib.get(ANDROID_NS + "name", "<unnamed>")
        for node in manifest.findall("uses-permission")
    ]
    if permissions:
        fail(f"core keyboard must remain permission-free; found {permissions}")

    service = next(
        (node for node in manifest.findall("./application/service")
         if node.attrib.get(ANDROID_NS + "name", "").endswith("KeyboardInputMethodService")),
        None,
    )
    if service is None:
        fail("KeyboardInputMethodService registration is missing")
    if service.attrib.get(ANDROID_NS + "exported") != "true":
        fail("input method service must be exported for Android IME discovery")
    if service.attrib.get(ANDROID_NS + "permission") != "android.permission.BIND_INPUT_METHOD":
        fail("input method service must require BIND_INPUT_METHOD")

    core = (ROOT / "native/crates/kb-core/src/lib.rs").read_text(encoding="utf-8")
    if "engine.set_full_text_tracking(false);" not in core:
        fail("Bamboo Core full-text tracking must remain disabled")

    jni = (ROOT / "native/crates/kb-ai-native/src/lib.rs").read_text(encoding="utf-8")
    for contract in ("char::from_u32", "sessions.get_mut(&handle)", "lock_sessions().remove(&handle)"):
        if contract not in jni:
            fail(f"JNI safety contract is missing: {contract}")

    bridge = (ROOT / "app/src/main/java/com/snowgem/kbai/NativeBridge.kt").read_text(encoding="utf-8")
    service_source = (ROOT / "app/src/main/java/com/snowgem/kbai/KeyboardInputMethodService.kt").read_text(encoding="utf-8")
    for method in ("nativeCreateSession", "nativeProcessKey", "nativeDestroySession"):
        if method not in bridge:
            fail(f"Kotlin JNI declaration is missing: {method}")
    for lifecycle_call in ("createNativeSession()", "destroyNativeSession()"):
        if lifecycle_call not in service_source:
            fail(f"IME native-session lifecycle is missing: {lifecycle_call}")

    workflow = (ROOT / ".github/workflows/android.yml").read_text(encoding="utf-8")
    for gate in (
        "cargo fmt --all",
        "cargo test --workspace",
        "cargo clippy --workspace",
        ":app:lintDebug",
        ":app:testDebugUnitTest",
        ":app:assembleDebug",
    ):
        if gate not in workflow:
            fail(f"Android CI is missing required quality gate: {gate}")

    print("KB-Ai architecture guard passed: manifest privacy, IME protection, native boundaries, and CI gates.")


if __name__ == "__main__":
    main()
