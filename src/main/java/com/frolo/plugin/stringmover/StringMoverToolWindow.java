package com.frolo.plugin.stringmover;

import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StringMoverToolWindow {
    private JPanel content;
    private JButton move_button;
    private GradleModuleSearchTextField gradleModuleSearchTextField1;
    private GradleModuleSearchTextField gradleModuleSearchTextField2;

    StringMoverToolWindow(ToolWindow window) {
//        setupSpinner(src_module_spinner);
//        setupSpinner(dst_module_spinner);
    }

    private void setupSpinner(JSpinner spinner) {
        SpinnerListModel model = new SpinnerListModel();
        model.setList(GradleHelper.getAllModules());
        spinner.setModel(model);
    }

    @Nullable
    public JPanel getContent() {
        return content;
    }

    private void createUIComponents() {
        gradleModuleSearchTextField1 = new GradleModuleSearchTextField();
        gradleModuleSearchTextField2 = new GradleModuleSearchTextField();
    }
}
