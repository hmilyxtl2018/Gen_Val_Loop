# Database Schema

The database follows the current round-level ontology design. It stores task
definitions, loop runs, round traces, input/process/output ontology frames,
candidates, and validation results.

Migration:

```text
db/migrations/V001__round_ontology_schema.sql
```

## Core Shape

```text
task
  -> task_statement
  -> loop_run
       -> round_trace
            -> ontology_frame(INPUT|PROCESS|OUTPUT)
                 -> ontology_statement
            -> candidate
            -> validation_result
                 -> validation_issue
```

## Design Rules

- `task_statement` stores the initial `TaskSpec` facts, concepts, goals, and
  criteria as ontology statements.
- `ontology_frame` stores one of the three round roles: `INPUT`, `PROCESS`, or
  `OUTPUT`.
- `ontology_statement` is the primary trace unit:

```text
kind + subject + predicate + object
```

- `candidate` and `validation_result` are kept as first-class tables because
  they are the main GenAgent and ValAgent artifacts.
- `validation_issue` is normalized so issue order can be preserved.

## Statement Kinds

The SQL check constraints match `StatementKind`:

```text
FACT
CONCEPT
INTENTION
GOAL
CRITERION
CANDIDATE
VALIDATION
FEEDBACK
DECISION
EVIDENCE
```

## What Is Intentionally Not Included

The first schema does not include users, auth, model token accounting, vector
indexes, prompt snapshots, or long-term memory. Those should be added only after
the round trace persistence is stable.
