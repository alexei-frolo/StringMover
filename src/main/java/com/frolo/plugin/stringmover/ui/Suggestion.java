package com.frolo.plugin.stringmover.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Suggestion {
    @NotNull
    final String text;
    @Nullable
    final Object model;

    @NotNull
    public static Suggestion of(@NotNull String text, @Nullable Object model) {
        return new Suggestion(text, model);
    }

    Suggestion(@NotNull String text) {
        this(text, text);
    }

    Suggestion(@NotNull String text, @Nullable Object model) {
        this.text = text;
        this.model = model;
    }

    @Override
    public String toString() {
        return text;
    }
}
