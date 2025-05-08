package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.Utils.GUIUtils;
import com.github.mkram17.bazaarutils.Utils.Util;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

//TODO make using amecs optional
public class StashHelper extends AmecsKeyBinding {
    private transient int ticksBetweenPresses;

    public StashHelper() {
        super("Pick Up Stash", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "Bazaar Utils", new KeyModifiers(true, false, false, false));
        ticksBetweenPresses = 0;
    }

    @Override
    public void onPressed(){
        if(ticksBetweenPresses>10) {
            GUIUtils.closeHandledScreen();
            Util.sendCommand("pickupstash");

            ticksBetweenPresses = 0;
        }
    }

    public void registerTickCounter() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ticksBetweenPresses++;
        });
    }
//    public Option<Boolean> createOption() {
//        return Option.<Boolean>createBuilder()
//                .name(Text.literal("Stash Helper"))
//                .description(OptionDescription.of(Text.literal("Alt + V to close bazaar (if it's open) and then pick up stash")))
//                .binding(true,
//                        this::isEnabled,
//                        this::setEnabled)
//                .controller(BUConfig::createBooleanController)
//                .build();
//    }
}
