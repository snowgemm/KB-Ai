# KB-Ai Agent Evaluation Pack

This pack evaluates a checked-out KB-Ai repository against keyboard-specific acceptance scenarios.

## Run

```bash
python3 -m unittest discover -s scripts -p 'test_*.py'
python3 scripts/verify_architecture.py
python3 scripts/run_agent_evals.py
```

The runner writes `build/agent-evals/report.json`, produces a weighted score, and adds a compact GitHub Actions step summary. CI uploads the JSON report as an artifact.

## Coverage

Scenarios cover Telex/VNI correctness and rollback, sensitive editor handling, typed-data minimization, JNI validation/session lifecycle, and CI security. Functional Rust and Android tests still run separately and must pass; this runner does not replace them.

## Scope boundary

This is an artifact-based acceptance evaluator. It does **not** invoke a language model, inspect hidden reasoning, or claim to measure model intelligence. Model-behavior evaluation requires a real agent adapter and held-out task inputs with independent graders. A passing static assertion is not proof that an agent reasoned correctly.
