# Mock Task JSONL

Test data file:

```text
src/test/resources/mock-tasks/aerospace_adcs_tasks.jsonl
```

Each line is one `TaskSpec`-shaped JSON object:

```json
{
  "taskId": "string",
  "objective": "string",
  "target": "string",
  "facts": ["string"],
  "concepts": ["string"],
  "goals": ["string"],
  "criteria": ["string"]
}
```

The mock tasks cover two safe, non-operational domains:

- Aerospace manufacturing test scenarios.
- Satellite ADCS / orbit-control-adjacent test scenarios.

These examples are for software loop testing only. They intentionally avoid real
production release decisions, flight acceptance thresholds, control gains,
spacecraft commands, maneuver timing, delta-v values, and other operational
engineering parameters.

The current `MockModelClient` expects convergence-oriented tasks to include:

```text
target_keyword=DONE
```

and criteria requiring acceptance evidence. That keeps deterministic mock tests
aligned with the current two-round behavior.
