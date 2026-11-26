use bamboo_core::{Config, Engine, InputMethod, Mode};

/// Per-editor text composition state for the Android keyboard.
///
/// Committed text is deliberately not retained by the engine. The Android
/// editor remains the source of truth for text already inserted into an app.
pub struct InputSession {
    engine: Engine,
}

impl InputSession {
    /// Creates a session supporting Telex and VNI simultaneously.
    ///
    /// Extended phonetic onsets are disabled so ordinary English words
    /// beginning with f, j, or w are less likely to be interpreted as Vietnamese.
    pub fn new() -> Self {
        let config = Config::builder().extended_onsets(false).build();
        let mut engine = Engine::with_config(InputMethod::telex_vni(), config);

        // Do not retain committed words or raw keystrokes in this process.
        engine.set_full_text_tracking(false);

        Self { engine }
    }

    /// Applies one Unicode scalar value and returns the borrowed current preedit.
    ///
    /// The returned slice is backed by the engine's reusable composition buffer.
    /// Callers must finish using it before mutating or resetting this session.
    pub fn process_key(&mut self, key: char, vietnamese: bool) -> &str {
        let mode = if vietnamese {
            Mode::Vietnamese
        } else {
            Mode::English
        };
        self.engine.process_key(key, mode);
        self.engine.output_str()
    }

    /// Returns whether the last key forced the previous preedit to commit.
    pub fn was_forced_commit(&self) -> bool {
        self.engine.just_committed()
    }

    /// Undoes the last physical keystroke in the active composition.
    ///
    /// Returns None when there is no active composition, allowing the Android
    /// editor to handle ordinary Backspace outside the current word.
    pub fn undo_last_key(&mut self) -> Option<&str> {
        if self.engine.output_str().is_empty() {
            return None;
        }

        self.engine.undo_last_key();
        Some(self.engine.output_str())
    }

    /// Finishes the active composition without retaining its text in Rust.
    pub fn commit(&mut self) {
        self.engine.commit();
    }

    /// Clears active and internal engine state before reusing the session.
    pub fn reset(&mut self) {
        self.engine.reset();
    }

    /// Returns the active preedit, never previously committed editor text.
    pub fn preedit(&self) -> &str {
        self.engine.output_str()
    }
}

impl Default for InputSession {
    fn default() -> Self {
        Self::new()
    }
}

pub const fn native_status() -> &'static str {
    "KB-Ai Rust native bridge is connected."
}

#[cfg(test)]
mod tests {
    use super::{InputSession, native_status};
    use bamboo_core::{Config, Engine, InputMethod, Mode, OutputOptions};

    fn type_keys(session: &mut InputSession, keys: &str, vietnamese: bool) -> String {
        let mut output = String::new();
        for key in keys.chars() {
            output = session.process_key(key, vietnamese).to_owned();
        }
        output
    }

    #[test]
    fn returns_a_deterministic_phase_zero_status() {
        assert_eq!(native_status(), "KB-Ai Rust native bridge is connected.");
    }

    #[test]
    fn telex_composes_vietnamese_text() {
        let mut session = InputSession::new();
        assert_eq!(type_keys(&mut session, "tieengs", true), "tiếng");
        session.commit();
        assert_eq!(type_keys(&mut session, "vieetj", true), "việt");
    }

    #[test]
    fn vni_composes_vietnamese_text() {
        let mut session = InputSession::new();
        assert_eq!(type_keys(&mut session, "tie6ng1", true), "tiếng");
        session.commit();
        assert_eq!(type_keys(&mut session, "vie6t5", true), "việt");
    }

    #[test]
    fn repeated_tone_key_can_restore_literal_input() {
        let mut session = InputSession::new();
        assert_eq!(type_keys(&mut session, "ass", true), "as");
    }

    #[test]
    fn english_mode_does_not_apply_vietnamese_transformations() {
        let mut session = InputSession::new();
        assert_eq!(type_keys(&mut session, "just", false), "just");
    }

    #[test]
    fn backspace_undoes_the_last_input_key() {
        let mut session = InputSession::new();
        assert_eq!(type_keys(&mut session, "tieengs", true), "tiếng");
        assert_eq!(session.undo_last_key(), Some("tiêng"));
        assert_eq!(session.undo_last_key(), Some("tiên"));
    }

    #[test]
    fn backspace_outside_composition_is_left_to_the_editor() {
        let mut session = InputSession::new();
        assert_eq!(session.undo_last_key(), None);
    }

    #[test]
    fn commit_clears_the_active_preedit() {
        let mut session = InputSession::new();
        assert_eq!(type_keys(&mut session, "tieengs", true), "tiếng");
        session.commit();
        assert!(session.preedit().is_empty());
        assert_eq!(type_keys(&mut session, "nam", true), "nam");
    }

    #[test]
    fn reset_clears_the_active_preedit() {
        let mut session = InputSession::new();
        assert_eq!(type_keys(&mut session, "tieengs", true), "tiếng");
        session.reset();
        assert!(session.preedit().is_empty());
    }

    #[test]
    fn committed_text_is_not_retained_by_the_engine() {
        let config = Config::builder().extended_onsets(false).build();
        let mut engine = Engine::with_config(InputMethod::telex_vni(), config);
        engine.set_full_text_tracking(false);

        for key in "tieengs".chars() {
            engine.process_key(key, Mode::Vietnamese);
        }
        assert_eq!(engine.output_str(), "tiếng");

        engine.commit();
        assert!(
            engine
                .get_processed_str_cow(OutputOptions::FULL_TEXT)
                .is_empty(),
            "committed words must not remain in the engine's full-text buffers"
        );
    }
}
