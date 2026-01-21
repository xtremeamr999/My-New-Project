package com.github.mkram17.bazaarutils.ui;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

public class NumTextBox extends TextBoxComponent {
    public NumTextBox(Sizing horizontalSizing) {
        super(horizontalSizing);
        // Use a text predicate to enforce that only digits can be entered locally.
        this.setTextPredicate(text -> text.matches("\\d*"));
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (!Character.isDigit(keyCode) && keyCode != GLFW.GLFW_KEY_BACKSPACE) {
            return false;
        } else {
            return super.keyPressed(input);
        }
    }
}
