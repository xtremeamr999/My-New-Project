package com.github.mkram17.bazaarutils.config.feature;

import com.github.mkram17.bazaarutils.features.keybinds.StashHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindConfig {
    //Keybinds (must be static). They get registered on object creation.
    public static StashHelper stashHelper = new StashHelper(new KeyBinding(
            "Pick Up Stash",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            StashHelper.CATEGORY
    ));
}
