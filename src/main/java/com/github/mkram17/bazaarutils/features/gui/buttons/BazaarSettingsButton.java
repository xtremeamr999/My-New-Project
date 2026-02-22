package com.github.mkram17.bazaarutils.features.gui.buttons;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.features.gui.ButtonsConfig;
import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.github.mkram17.bazaarutils.utils.annotations.autoregistration.RegisterWidget;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.ui.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

@ConfigObject
public class BazaarSettingsButton {
    @ConfigEntry(
            id = "enabled",
            translation = "bazaarutils.config.buttons.button.enabled.value"
    )
    @Comment(
            value = "Whether the button will be registered or not",
            translation = "bazaarutils.config.buttons.button.enabled.description"
    )
    public boolean enabled;

    @ConfigEntry(
            id = "size",
            translation = "bazaarutils.config.buttons.button.size.value"
    )
    public int size = 18;

    @ConfigEntry(
            id = "spacing",
            translation = "bazaarutils.config.buttons.button.spacing.value"
    )
    public int spacing = 4;

    private static final Identifier DEFAULT = Identifier.tryParse(BazaarUtils.MOD_ID, "widget/settings_widget_base");
    private static final Identifier HOVER = Identifier.tryParse(BazaarUtils.MOD_ID, "widget/settings_widget_hover");

    public static final ButtonTextures SLOT_BUTTON_TEXTURES = new ButtonTextures(
            DEFAULT,
            HOVER
    );

    public BazaarSettingsButton(boolean enabled) {
        this.enabled = enabled;
    };

    @RegisterWidget
    public static List<ItemSlotButtonWidget> getWidget() {
        if (!ButtonsConfig.MOD_SETTINGS.enabled) {
            return Collections.emptyList();
        }

        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();

        if (!(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen) || !screenInfo.inBazaar()) {
            return Collections.emptyList();
        }

        String screenTitle = MinecraftClient.getInstance().currentScreen.getTitle().getString();

        ItemSlotButtonWidget.ScreenWidgetDimensions dimensions = ItemSlotButtonWidget.getSafeScreenDimensions(screen, screenTitle);
        ItemSlotButtonWidget button = ButtonsConfig.MOD_SETTINGS.getItemSlotButtonWidget(dimensions);

        return Collections.singletonList(button);
    }

    private ItemSlotButtonWidget getItemSlotButtonWidget(ItemSlotButtonWidget.ScreenWidgetDimensions dimensions) {
        int buttonX = dimensions.x() - size - spacing;
        int currentButtonY = dimensions.y() + spacing;

        return new ItemSlotButtonWidget(
                buttonX,
                currentButtonY,
                size, size,
                SLOT_BUTTON_TEXTURES,
                (btn) -> ConfigUtil.openGUI(),
                null,
                Text.literal("Bazaar Utils Settings")
        );
    }
}
