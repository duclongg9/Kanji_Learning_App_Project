package com.example.kanjilearning.data;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot describing whether the remote MySQL schema matches the expected layout
 * defined in {@code kanji_app_v1.sql}.
 */
public final class MySqlSchemaInspectionResult {

    private final List<String> missingTables;
    private final List<String> missingViews;
    private final List<String> missingProcedures;

    public MySqlSchemaInspectionResult(
            @NonNull List<String> missingTables,
            @NonNull List<String> missingViews,
            @NonNull List<String> missingProcedures
    ) {
        this.missingTables = Collections.unmodifiableList(new ArrayList<>(missingTables));
        this.missingViews = Collections.unmodifiableList(new ArrayList<>(missingViews));
        this.missingProcedures = Collections.unmodifiableList(new ArrayList<>(missingProcedures));
    }

    public boolean isHealthy() {
        return missingTables.isEmpty() && missingViews.isEmpty() && missingProcedures.isEmpty();
    }

    @NonNull
    public List<String> getMissingTables() {
        return missingTables;
    }

    @NonNull
    public List<String> getMissingViews() {
        return missingViews;
    }

    @NonNull
    public List<String> getMissingProcedures() {
        return missingProcedures;
    }

    /**
     * Human readable summary that lists the objects that still need to be created.
     */
    @NonNull
    public String describeProblems() {
        if (isHealthy()) {
            return "Schema is up to date";
        }
        final StringBuilder builder = new StringBuilder();
        if (!missingTables.isEmpty()) {
            builder.append("Missing tables: ")
                    .append(String.join(", ", missingTables));
        }
        if (!missingViews.isEmpty()) {
            if (builder.length() > 0) builder.append("; ");
            builder.append("Missing views: ")
                    .append(String.join(", ", missingViews));
        }
        if (!missingProcedures.isEmpty()) {
            if (builder.length() > 0) builder.append("; ");
            builder.append("Missing procedures: ")
                    .append(String.join(", ", missingProcedures));
        }
        return builder.toString();
    }
}
