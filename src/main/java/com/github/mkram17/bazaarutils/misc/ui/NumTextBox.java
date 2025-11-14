package com.github.mkram17.bazaarutils.misc.ui;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;

public class NumTextBox extends TextBoxComponent {
    public NumTextBox(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(!Character.isDigit(keyCode) && keyCode != 259) { // 259 = backspace
            return false;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(!Character.isDigit(chr)) {
            return false;
        } else {
            return super.charTyped(chr, modifiers);
        }
    }
}

