package com.github.mkram17.bazaarutils.features.keybinds;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.features.util.BUKeybinding;
import net.minecraft.util.Identifier;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;

public class StashHelper extends BUKeybinding {
    @Getter
    private static int ticksBetweenPresses;
    public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(BazaarUtils.MODID));

    public StashHelper(KeyBinding keyBinding) {
        super(keyBinding);
    }

    @Override
    protected void registerOnPressed(){
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
}
