package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.github.mkram17.bazaarutils.misc.autoregistration.RegisterWidget;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.ui.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public class BazaarSettingsButton {
    private static final Identifier BASE = Identifier.tryParse(BazaarUtils.MODID, "widget/settings_widget_base");
    private static final Identifier HOVER = Identifier.tryParse(BazaarUtils.MODID, "widget/settings_widget_hover");
    public static final ButtonTextures SLOT_BUTTON_TEXTURES = new ButtonTextures(
            BASE,
            HOVER);

    @RegisterWidget
    public static List<ItemSlotButtonWidget> getWidget() {
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        if (!(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen) || !screenInfo.inBazaar())
            return Collections.emptyList();

        String screenTitle = MinecraftClient.getInstance().currentScreen.getTitle().getString();

        ItemSlotButtonWidget.ScreenWidgetDimensions dimensions = ItemSlotButtonWidget.getSafeScreenDimensions(screen, screenTitle);

        int buttonSize = 18;
        int spacing = 4;
        int buttonX = dimensions.x() - buttonSize - spacing;
        int currentButtonY = dimensions.y() + spacing;


        ItemSlotButtonWidget button = new ItemSlotButtonWidget(
                buttonX,
                currentButtonY,
                buttonSize, buttonSize,
                SLOT_BUTTON_TEXTURES,
                (btn) -> ConfigUtil.openGUI(),
                null,
                Text.literal("Bazaar Utils Settings")
        );
        return Collections.singletonList(button);
    }

}
