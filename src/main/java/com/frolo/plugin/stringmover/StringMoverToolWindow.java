package com.frolo.plugin.stringmover;

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
                suggestions.add(module.getDirPath());
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
        String stringKeysRaw = string_keys.getText();
        if (stringKeysRaw != null && !stringKeysRaw.isEmpty()) {
            String[] keys = stringKeysRaw.split(",");
            stringKeys.addAll(Arrays.asList(keys));
        }
        return new StringMover.Params(srcModule, dstModule, stringKeys);
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
