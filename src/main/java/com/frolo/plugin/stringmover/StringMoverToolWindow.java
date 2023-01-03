package com.frolo.plugin.stringmover;

import com.frolo.plugin.stringmover.ui.Suggestion;
import com.frolo.plugin.stringmover.ui.SuggestionClient;
import com.frolo.plugin.stringmover.ui.SuggestionDropDownDecorator;
import com.frolo.plugin.stringmover.ui.TextComponentSuggestionClient;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.*;
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
    private JRadioButton keep_radio_button;
    private JRadioButton remove_radio_button;
    private JRadioButton ignore_radio_button;
    private JRadioButton replace_radio_button;

    StringMoverToolWindow(Project project, ToolWindow window, StringMover stringMover) {
        this.project = project;
        this.gradleHelper = new GradleHelper(project);
        this.stringMover = stringMover;
        setupComponents();
    }

    private void setupComponents() {
        Function<String, List<Suggestion>> suggestionProvider = s -> {
            if (s == null) s = "";
            List<GradleModule> modules = gradleHelper.getGradleModuleSuggestions(s);
            List<Suggestion> suggestions = new ArrayList<>(modules.size());
            for (GradleModule module : modules) {
                suggestions.add(Suggestion.of(module.getModuleName(), module));
            }
            return suggestions;
        };
        SuggestionClient<JTextComponent> suggestionClient =
            new TextComponentSuggestionClient(suggestionProvider);
        SuggestionDropDownDecorator.decorate(src_module, suggestionClient);
        SuggestionDropDownDecorator.decorate(dst_module, suggestionClient);

        ButtonGroup sourceRetentionStrategyGroup = new ButtonGroup();
        sourceRetentionStrategyGroup.add(keep_radio_button);
        sourceRetentionStrategyGroup.add(remove_radio_button);
        keep_radio_button.setSelected(true);

        ButtonGroup conflictStrategyGroup = new ButtonGroup();
        conflictStrategyGroup.add(ignore_radio_button);
        conflictStrategyGroup.add(replace_radio_button);
        ignore_radio_button.setSelected(true);

        move_button.addActionListener(e -> execMoveStrings());
    }

    @Nullable
    public JPanel getContent() {
        return content;
    }

    private void createUIComponents() {
    }

    private StringMover.Params readStringMoverParams() throws Exception {
        GradleModule srcModule = (GradleModule) TextComponentSuggestionClient.getSelection(src_module);
        if (srcModule == null) {
            throw new NullPointerException("Src module not found");
        }
        GradleModule dstModule = (GradleModule) TextComponentSuggestionClient.getSelection(dst_module);
        if (dstModule == null) {
            throw new NullPointerException("Dst module not found");
        }
        Set<String> stringKeys = new HashSet<>();
        String stringKeysRaw = string_keys.getText();
        if (stringKeysRaw != null && !stringKeysRaw.isEmpty()) {
            String[] keys = stringKeysRaw.split(",");
            stringKeys.addAll(Arrays.asList(keys));
        }
        final StringMover.SourceRetentionStrategy sourceRetentionStrategy;
        if (keep_radio_button.isSelected()) {
            sourceRetentionStrategy = StringMover.SourceRetentionStrategy.KEEP;
        } else if (remove_radio_button.isSelected()) {
            sourceRetentionStrategy = StringMover.SourceRetentionStrategy.REMOVE;
        } else {
            throw new IllegalStateException("Failed to resolve source retention strategy");
        }
        final StringMover.ConflictStrategy conflictStrategy;
        if (ignore_radio_button.isSelected()) {
            conflictStrategy = StringMover.ConflictStrategy.IGNORE;
        } else if (replace_radio_button.isSelected()) {
            conflictStrategy = StringMover.ConflictStrategy.REPLACE;
        } else {
            throw new IllegalStateException("Failed to resolve conflict strategy");
        }
        return new StringMover.Params(srcModule, dstModule, stringKeys, sourceRetentionStrategy, conflictStrategy);
    }

    private void execMoveStrings() {
        final StringMover.Params params;
        final ErrorDispatcher errorDispatcher = this::showError;
        try {
            params = readStringMoverParams();
            stringMover.moveStrings(params);
        } catch (Exception e) {
            errorDispatcher.dispatchError(new RuntimeException(e));
        }
    }

    private void showError(@NotNull Throwable error) {
        System.out.println("Error occurred: \n" + error);
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(error.toString(), MessageType.ERROR, null)
            .setFadeoutTime(7500)
            .createBalloon()
            .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }
}
