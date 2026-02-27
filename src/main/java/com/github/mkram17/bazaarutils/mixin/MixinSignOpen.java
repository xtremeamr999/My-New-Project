package com.github.mkram17.bazaarutils.mixin;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.events.SignOpenEvent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO maybe try to do this with AbstractSignEditScreen init() and see if it works to be more efficient
//used for SignOpenEvent
@Mixin(SignEditScreen.class)
public class MixinSignOpen {
    @Inject(method = "init()V", at = @At("TAIL"))
    private void onScreenInit(CallbackInfo ci) {
        SignEditScreen screen = (SignEditScreen) (Object) this;

        BazaarUtils.EVENT_BUS.post(new SignOpenEvent(screen));
    }
}