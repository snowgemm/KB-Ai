pub const fn native_status() -> &'static str {
    "KB-Ai Rust native bridge is connected."
}

#[cfg(test)]
mod tests {
    use super::native_status;

    #[test]
    fn returns_a_deterministic_phase_zero_status() {
        assert_eq!(
            native_status(),
            "KB-Ai Rust native bridge is connected."
        );
    }
}
