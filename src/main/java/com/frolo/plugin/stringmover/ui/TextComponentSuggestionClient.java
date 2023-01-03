package com.frolo.plugin.stringmover.ui;

import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/**
 * Matches entire text instead of separate words
 */
public class TextComponentSuggestionClient implements SuggestionClient<JTextComponent> {

    private Function<String, List<Suggestion>> suggestionProvider;

    public TextComponentSuggestionClient(Function<String, List<Suggestion>> suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }

    @Override
    public Point getPopupLocation(JTextComponent invoker) {
        return new Point(0, invoker.getPreferredSize().height);
    }

    @Override
    public void setSelectedText(JTextComponent invoker, String selectedValue) {
        invoker.setText(selectedValue);
    }

    @Override
    public List<Suggestion> getSuggestions(JTextComponent invoker) {
        return suggestionProvider.apply(invoker.getText().trim());
    }
}