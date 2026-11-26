"""Mutation-style tests for KB-Ai's privacy and architecture guard."""
from pathlib import Path
import tempfile
import unittest

from verify_architecture import check_architecture


ANDROID_NS = "http://schemas.android.com/apk/res/android"


class KbAiArchitectureGuardTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp_dir = tempfile.TemporaryDirectory()
        self.root = Path(self.temp_dir.name)

        manifest = self.root / "app/src/main/AndroidManifest.xml"
        manifest.parent.mkdir(parents=True)
        manifest.write_text(
            f'''<manifest xmlns:android="{ANDROID_NS}">
  <application>
    <service android:name=".KeyboardInputMethodService"
      android:exported="true"
      android:permission="android.permission.BIND_INPUT_METHOD" />
  </application>
</manifest>''',
            encoding="utf-8",
        )

        core = self.root / "native/crates/kb-core/src/lib.rs"
        core.parent.mkdir(parents=True)
        core.write_text("engine.set_full_text_tracking(false);", encoding="utf-8")

        jni = self.root / "native/crates/kb-ai-native/src/lib.rs"
        jni.parent.mkdir(parents=True)
        jni.write_text(
            "char::from_u32(key_code as u32);\n"
            "let mut sessions = lock_sessions();\nsessions\n.get_mut(&handle);\n"
            "lock_sessions().remove(&handle);",
            encoding="utf-8",
        )

        kotlin = self.root / "app/src/main/java/com/snowgem/kbai"
        kotlin.mkdir(parents=True)
        (kotlin / "NativeBridge.kt").write_text(
            "nativeCreateSession nativeProcessKey nativeDestroySession", encoding="utf-8"
        )
        (kotlin / "KeyboardInputMethodService.kt").write_text(
            "createNativeSession() destroyNativeSession()", encoding="utf-8"
        )

        workflows = self.root / ".github/workflows"
        workflows.mkdir(parents=True)
        (workflows / "android.yml").write_text(
            "cargo fmt --all cargo test --workspace cargo clippy --workspace "
            "python3 -m unittest discover -s scripts python3 scripts/verify_architecture.py "
            ":app:lintDebug :app:testDebugUnitTest :app:assembleDebug",
            encoding="utf-8",
        )

    def tearDown(self) -> None:
        self.temp_dir.cleanup()

    def test_accepts_valid_privacy_and_native_boundary_fixture(self) -> None:
        self.assertEqual(check_architecture(self.root), [])

    def test_rejects_new_manifest_permissions(self) -> None:
        manifest = self.root / "app/src/main/AndroidManifest.xml"
        content = manifest.read_text(encoding="utf-8").replace(
            "<application>",
            '<uses-permission android:name="android.permission.INTERNET" />\n  <application>',
        )
        manifest.write_text(content, encoding="utf-8")
        self.assertTrue(any("permission-free" in item for item in check_architecture(self.root)))

    def test_rejects_removal_of_native_full_text_privacy_setting(self) -> None:
        (self.root / "native/crates/kb-core/src/lib.rs").write_text(
            "Engine::new();", encoding="utf-8"
        )
        self.assertTrue(any("full-text tracking" in item for item in check_architecture(self.root)))

    def test_rejects_removal_of_ime_binding_permission(self) -> None:
        manifest = self.root / "app/src/main/AndroidManifest.xml"
        content = manifest.read_text(encoding="utf-8").replace(
            'android:permission="android.permission.BIND_INPUT_METHOD"', ""
        )
        manifest.write_text(content, encoding="utf-8")
        self.assertTrue(any("BIND_INPUT_METHOD" in item for item in check_architecture(self.root)))

    def test_rejects_missing_unicode_validation(self) -> None:
        jni = self.root / "native/crates/kb-ai-native/src/lib.rs"
        jni.write_text(
            jni.read_text(encoding="utf-8").replace("char::from_u32", "key_code as char"),
            encoding="utf-8",
        )
        self.assertTrue(any("char::from_u32" in item for item in check_architecture(self.root)))


if __name__ == "__main__":
    unittest.main()
