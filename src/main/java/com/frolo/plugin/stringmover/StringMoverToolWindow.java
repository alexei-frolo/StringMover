package com.frolo.plugin.stringmover;

import com.frolo.plugin.stringmover.ui.SuggestionClient;
import com.frolo.plugin.stringmover.ui.SuggestionDropDownDecorator;
import com.frolo.plugin.stringmover.ui.TextComponentSuggestionClient;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StringMoverToolWindow {
    private final Project project;
    private JPanel content;
    private JButton move_button;
    private JTextField src_module;
    private JTextField dst_module;

    StringMoverToolWindow(Project project, ToolWindow window) {
        this.project = project;
        setupComponents();
    }

    private void setupComponents() {
        final GradleModuleFinder finder = new GradleHelper(project);
        Function<String, List<String>> suggestionProvider = s -> {
            if (s == null) s = "";
            List<GradleModule> modules = finder.getGradleModuleSuggestions(s);
            List<String> suggestions = new ArrayList<>(modules.size());
            for (GradleModule module : modules) {
                suggestions.add(module.getPath());
            }
            return suggestions;
        };
        SuggestionClient<JTextComponent> suggestionClient =
            new TextComponentSuggestionClient(suggestionProvider);
        SuggestionDropDownDecorator.decorate(src_module, suggestionClient);
        SuggestionDropDownDecorator.decorate(dst_module, suggestionClient);
    }

    @Nullable
    public JPanel getContent() {
        return content;
    }

    private void createUIComponents() {
    }
}
