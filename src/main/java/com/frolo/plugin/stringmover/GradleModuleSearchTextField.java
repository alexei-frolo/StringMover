package com.frolo.plugin.stringmover;


import com.intellij.ui.JBColor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;

import javax.annotation.Nullable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class GradleModuleSearchTextField extends JTextField implements KeyListener,
        DocumentListener {
    private final GradleModuleFinder gradleModuleFinder = GradleHelper.INSTANCE;
    @Nullable
    private GradleModule currentGuess = null;
    private Color incompleteColor;
    private boolean areGuessing;
    private boolean caseSensitive;

    public GradleModuleSearchTextField() {
        this(5, false);
    }

    public GradleModuleSearchTextField(int columns) {
        this(columns, false);
    }

    public GradleModuleSearchTextField(int columns, boolean caseSensitive) {
        super.setColumns(columns);
        this.incompleteColor = JBColor.GRAY.brighter();
        this.currentGuess = null;
        this.areGuessing = false;
        this.caseSensitive = caseSensitive;
        this.addKeyListener(this);
        this.getDocument().addDocumentListener(this);
    }

    public void setIncompleteColor(Color incompleteColor) {
        this.incompleteColor = incompleteColor;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    private String getCurrentGuess() {
        if (this.currentGuess != null) {
            return currentGuess.getPath();
        }
        return this.getText();
    }

    private void findCurrentGuess() {
        String entered = this.getText();
        if (!this.caseSensitive) {
            entered = entered.toLowerCase();
        }
        currentGuess = gradleModuleFinder.findGradleModule(entered);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        this.areGuessing = false;
        this.currentGuess = null;
    }

    @Override
    public void paintComponent(Graphics g) {
        String guess = getCurrentGuess();
        String drawGuess = guess;

        super.paintComponent(g);
        String entered = this.getText();
        Rectangle2D enteredBounds = g.getFontMetrics().getStringBounds(entered, g);

        if (!(this.caseSensitive)) {
            entered = entered.toLowerCase();
            guess = guess.toLowerCase();
        }

        if (!(guess.startsWith(entered))) {
            this.areGuessing = false;
        }

        if (entered != null && !(entered.equals(""))
                && this.areGuessing) {
            String subGuess = drawGuess.substring(entered.length(), drawGuess.length());
            Rectangle2D subGuessBounds = g.getFontMetrics().getStringBounds(drawGuess, g);

            int centeredY = ((getHeight() / 2) + (int)(subGuessBounds.getHeight() / 2));

            g.setColor(this.incompleteColor);
            g.drawString(subGuess + "   press ENTER to send or \u2192 to fill", (int)(enteredBounds.getWidth()) + 2, centeredY - 2);
        }
    }

    public void keyTyped(KeyEvent e) { }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (this.areGuessing) {
                this.setText(this.getCurrentGuess());
                this.areGuessing = false;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (this.areGuessing) {
                this.setText(this.getCurrentGuess());
                this.areGuessing = false;
                e.consume();
            }
        }
    }

    public void keyReleased(KeyEvent e) { }

    public void insertUpdate(DocumentEvent e) {
        String temp = this.getText();
        if (temp.length() == 1) {
            this.areGuessing = true;
        }
        if (this.areGuessing) {
            this.findCurrentGuess();
        }
    }

    public void removeUpdate(DocumentEvent e) {
        String temp = this.getText();
        if (!(this.areGuessing)) {
            this.areGuessing = true;
        }
        if (temp.length() == 0) {
            this.areGuessing = false;
        } else if (this.areGuessing) {
            this.findCurrentGuess();
        }
    }

    public void changedUpdate(DocumentEvent e) { }
}
