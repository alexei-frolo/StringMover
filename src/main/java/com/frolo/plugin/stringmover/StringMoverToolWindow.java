package com.frolo.plugin.stringmover;

import com.frolo.plugin.stringmover.ui.SuggestionClient;
import com.frolo.plugin.stringmover.ui.SuggestionDropDownDecorator;
import com.frolo.plugin.stringmover.ui.TextComponentSuggestionClient;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class StringMoverToolWindow {
    private final Project project;
    private final GradleModuleFinder gradleHelper;
    private final StringMover stringMover;

    private JPanel content;
    private JTextField string_keys;
    private JButton move_button;
    private JTextField src_module;
    private JTextField dst_module;

    StringMoverToolWindow(Project project, ToolWindow window, StringMover stringMover) {
        this.project = project;
        this.gradleHelper = new GradleHelper(project);
        this.stringMover = stringMover;
        setupComponents();
    }

    private void setupComponents() {
        Function<String, List<String>> suggestionProvider = s -> {
            if (s == null) s = "";
            List<GradleModule> modules = gradleHelper.getGradleModuleSuggestions(s);
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
        move_button.addActionListener(e -> execMoveStrings());
    }

    @Nullable
    public JPanel getContent() {
        return content;
    }

    private void createUIComponents() {
    }

    private StringMover.Params readStringMoverParams() throws Exception {
        GradleModule srcModule = gradleHelper.findGradleModule(src_module.getText());
        if (srcModule == null) {
            throw new NullPointerException("Src module not found");
        }
        GradleModule dstModule = gradleHelper.findGradleModule(dst_module.getText());
        if (dstModule == null) {
            throw new NullPointerException("Dst module not found");
        }
        Set<String> stringKeys = new HashSet<>();
        return new StringMover.Params(srcModule, dstModule, stringKeys);
    }

    private void execMoveStrings() {
        final StringMover.Params params;
        try {
            params = readStringMoverParams();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        stringMover.moveStrings(params);
    }
}
