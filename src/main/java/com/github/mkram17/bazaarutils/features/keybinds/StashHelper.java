package com.github.mkram17.bazaarutils.features.keybinds;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.features.util.BUKeybinding;
import net.minecraft.util.Identifier;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class StashHelper extends BUKeybinding {
    @Getter
    private static int ticksBetweenPresses;
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(BazaarUtils.MODID));
    private static final KeyBinding keyBinding = new KeyBinding(
       "Pick Up Stash",
       InputUtil.Type.KEYSYM,
       GLFW.GLFW_KEY_V,
       CATEGORY
    );

    @Override @RunOnInit
    public void initializeKeybinding() {
        registerKeybinding(keyBinding);
    }

    @RunOnInit
    public static void registerOnPressed(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ticksBetweenPresses++;
            if(!keyBinding.isPressed()) {
                return;
            }
            if(ticksBetweenPresses > 10) {
                ticksBetweenPresses = 0;
                GUIUtils.closeHandledScreen();
                PlayerActionUtil.runCommand("pickupstash");
            }
        });
    }

    public static String getUsage(){
        return keyBinding.getBoundKeyTranslationKey();
    }
}
