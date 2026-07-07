package com.ontogenval.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record OntologyFrame(String name, List<OntologyStatement> statements) {
    public OntologyFrame {
        name = name == null ? "" : name.trim();
        statements = List.copyOf(statements == null ? List.of() : statements);
    }

    public OntologyFrame plus(OntologyStatement statement) {
        List<OntologyStatement> next = new ArrayList<>(statements);
        next.add(statement);
        return new OntologyFrame(name, next);
    }

    public String render() {
        return statements.stream()
                .map(item -> "%s: %s %s %s".formatted(
                        item.kind(),
                        item.subject(),
                        item.predicate(),
                        item.object()
                ))
                .collect(Collectors.joining("\n"));
    }
}
