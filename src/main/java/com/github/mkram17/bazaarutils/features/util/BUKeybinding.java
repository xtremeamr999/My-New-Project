package com.github.mkram17.bazaarutils.features.util;

import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;

public abstract class BUKeybinding {
    protected static void registerKeybinding(KeyBinding keyBinding){
        KeyBindingHelper.registerKeyBinding(keyBinding);
    }

    //TODO can I have @RunOnInit here instead of on each method of the subclasses?
    abstract public void initializeKeybinding();
}
