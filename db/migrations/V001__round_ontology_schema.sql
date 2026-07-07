create extension if not exists pgcrypto;

create table task (
    id varchar(128) primary key,
    objective text not null,
    target text not null,
    created_at timestamptz not null default now()
);

create table task_statement (
    id bigserial primary key,
    task_id varchar(128) not null references task(id) on delete cascade,
    kind varchar(32) not null,
    subject text not null,
    predicate text not null,
    object text not null,
    ordinal int not null,
    created_at timestamptz not null default now(),
    constraint task_statement_kind_check check (
        kind in (
            'FACT',
            'CONCEPT',
            'INTENTION',
            'GOAL',
            'CRITERION',
            'CANDIDATE',
            'VALIDATION',
            'FEEDBACK',
            'DECISION',
            'EVIDENCE'
        )
    ),
    constraint task_statement_ordinal_unique unique (task_id, ordinal)
);

create table loop_run (
    id uuid primary key default gen_random_uuid(),
    task_id varchar(128) not null references task(id) on delete cascade,
    status varchar(32) not null default 'RUNNING',
    converged boolean not null default false,
    max_rounds int not null,
    started_at timestamptz not null default now(),
    finished_at timestamptz,
    constraint loop_run_status_check check (status in ('RUNNING', 'CONVERGED', 'FAILED')),
    constraint loop_run_max_rounds_check check (max_rounds >= 1)
);

create table round_trace (
    id uuid primary key default gen_random_uuid(),
    run_id uuid not null references loop_run(id) on delete cascade,
    round_index int not null,
    converged boolean not null default false,
    created_at timestamptz not null default now(),
    constraint round_trace_round_index_check check (round_index >= 1),
    constraint round_trace_run_round_unique unique (run_id, round_index)
);

create table ontology_frame (
    id uuid primary key default gen_random_uuid(),
    round_trace_id uuid not null references round_trace(id) on delete cascade,
    frame_role varchar(16) not null,
    name varchar(128) not null,
    created_at timestamptz not null default now(),
    constraint ontology_frame_role_check check (frame_role in ('INPUT', 'PROCESS', 'OUTPUT')),
    constraint ontology_frame_round_role_unique unique (round_trace_id, frame_role)
);

create table ontology_statement (
    id bigserial primary key,
    frame_id uuid not null references ontology_frame(id) on delete cascade,
    kind varchar(32) not null,
    subject text not null,
    predicate text not null,
    object text not null,
    ordinal int not null,
    created_at timestamptz not null default now(),
    constraint ontology_statement_kind_check check (
        kind in (
            'FACT',
            'CONCEPT',
            'INTENTION',
            'GOAL',
            'CRITERION',
            'CANDIDATE',
            'VALIDATION',
            'FEEDBACK',
            'DECISION',
            'EVIDENCE'
        )
    ),
    constraint ontology_statement_frame_ordinal_unique unique (frame_id, ordinal)
);

create table candidate (
    id uuid primary key default gen_random_uuid(),
    round_trace_id uuid not null unique references round_trace(id) on delete cascade,
    content text not null,
    created_at timestamptz not null default now()
);

create table validation_result (
    id uuid primary key default gen_random_uuid(),
    round_trace_id uuid not null unique references round_trace(id) on delete cascade,
    valid boolean not null,
    score double precision not null,
    gap double precision not null,
    feedback text not null,
    created_at timestamptz not null default now(),
    constraint validation_result_score_check check (score >= 0.0 and score <= 1.0),
    constraint validation_result_gap_check check (gap >= 0.0 and gap <= 1.0)
);

create table validation_issue (
    id bigserial primary key,
    validation_result_id uuid not null references validation_result(id) on delete cascade,
    issue text not null,
    ordinal int not null,
    created_at timestamptz not null default now(),
    constraint validation_issue_ordinal_unique unique (validation_result_id, ordinal)
);

create index task_statement_task_kind_idx on task_statement (task_id, kind);
create index loop_run_task_started_idx on loop_run (task_id, started_at desc);
create index round_trace_run_round_idx on round_trace (run_id, round_index);
create index ontology_frame_round_role_idx on ontology_frame (round_trace_id, frame_role);
create index ontology_statement_frame_kind_idx on ontology_statement (frame_id, kind);
create index ontology_statement_predicate_idx on ontology_statement (predicate);
create index validation_result_round_idx on validation_result (round_trace_id);
