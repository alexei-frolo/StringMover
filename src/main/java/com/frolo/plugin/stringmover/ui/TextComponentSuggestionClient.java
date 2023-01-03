package com.frolo.plugin.stringmover.ui;

import org.jetbrains.annotations.Nullable;

import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/**
 * Matches entire text instead of separate words
 */
public class TextComponentSuggestionClient implements SuggestionClient<JTextComponent> {
    private static final String SELECTED_SUGGESTION_PROPERTY = "selected_suggestion";

    @Nullable
    public static Object getSelection(JTextComponent component) {
        Object property = component.getClientProperty(SELECTED_SUGGESTION_PROPERTY);
        if (property instanceof Suggestion) {
            return ((Suggestion) property).model;
        }
        return null;
    }

    private Function<String, List<Suggestion>> suggestionProvider;

    public TextComponentSuggestionClient(Function<String, List<Suggestion>> suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }

    @Override
    public Point getPopupLocation(JTextComponent invoker) {
        return new Point(0, invoker.getPreferredSize().height);
    }

    @Override
    public void setSelection(JTextComponent invoker, Suggestion selectedValue) {
        invoker.putClientProperty(SELECTED_SUGGESTION_PROPERTY, selectedValue);
        invoker.setText(selectedValue.text);
    }

    @Override
    public List<Suggestion> getSuggestions(JTextComponent invoker) {
        return suggestionProvider.apply(invoker.getText().trim());
    }
}