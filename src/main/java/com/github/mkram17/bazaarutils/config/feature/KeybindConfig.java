package com.github.mkram17.bazaarutils.config.feature;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.features.keybinds.StashHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeybindConfig {

    public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(BazaarUtils.MODID));

    //Keybinds get registered on object creation.
    public StashHelper stashHelper = new StashHelper(new KeyBinding(
            "Pick Up Stash",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    ));
}
