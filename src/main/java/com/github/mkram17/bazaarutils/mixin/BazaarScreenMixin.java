// src/main/java/com/github/mkram17/bazaarutils/mixin/client/BazaarScreenMixin.java
package com.github.mkram17.bazaarutils.mixin;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.features.Bookmark;
import com.github.mkram17.bazaarutils.misc.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.utils.Util;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class BazaarScreenMixin<T extends ScreenHandler> extends Screen {
    @Unique
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");

    @Unique
    private static final ButtonTextures SLOT_BUTTON_TEXTURES = new ButtonTextures(
            SLOT_TEXTURE,
            SLOT_TEXTURE,
            SLOT_TEXTURE
    );

    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;

    protected BazaarScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void bazaarutils$addConfiguredButtons(CallbackInfo ci) {
        String screenTitle = this.title.getString();
        boolean isTargetScreen = screenTitle.startsWith("Bazaar");

        if (isTargetScreen) {
            int currentX = this.x;
            int currentY = this.y;
            int currentBgWidth = this.backgroundWidth;

            if (currentBgWidth <= 0) {
                Util.notifyAll("BackgroundWidth is not yet initialized correctly in init TAIL for " + screenTitle, Util.notificationTypes.GUI);
                return;
            }

            int buttonSize = 18;
            int spacing = 4;
            int buttonX = currentX + currentBgWidth + spacing;
            int currentButtonY = currentY + spacing;

            List<Bookmark> bookmarks = BUConfig.get().bookmarks;

            int buttonsAdded = 0;
            for (int i = 0; i < bookmarks.size(); i++) {
                ItemStack configuredItem = bookmarks.get(i).getBookmarkedItem();

                final int buttonIndex = i;
                final ItemStack itemForButton = (configuredItem == null) ? Items.BARRIER.getDefaultStack() : configuredItem;
                final Bookmark bookmark = bookmarks.get(buttonIndex);

                ItemSlotButtonWidget button = new ItemSlotButtonWidget(
                        buttonX,
                        currentButtonY,
                        buttonSize, buttonSize,
                        Bookmark.SLOT_BUTTON_TEXTURES,
                        (btn) -> {
                            if (Screen.hasShiftDown()) {
                                Util.notifyAll("Removed " + bookmark.getName() + " bookmark from shift-click. Open Bazaar again to display changes.");
                                bookmark.onWidgetShiftClick();
                            } else {
                                bookmark.onWidgetLeftClick();
                            }

                        },
                        itemForButton,
                        Text.of(bookmark.getName())
                );

                this.addDrawableChild(button);
                buttonsAdded++;

                currentButtonY += buttonSize + spacing;
            }

//            if (buttonsAdded > 0) {
//                Util.notifyAll("Added " + buttonsAdded + " configured button(s) to screen: " + screenTitle, Util.notificationTypes.GUI);
//            }
        }
    }

}

