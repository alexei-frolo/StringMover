package com.frolo.plugin.stringmover.ui;

import javax.swing.*;
import java.awt.*;

public interface SuggestionClient<C extends JComponent> {

    Point getPopupLocation(C invoker);

    void setSelection(C invoker, Suggestion selectedValue);

    java.util.List<Suggestion> getSuggestions(C invoker);

}