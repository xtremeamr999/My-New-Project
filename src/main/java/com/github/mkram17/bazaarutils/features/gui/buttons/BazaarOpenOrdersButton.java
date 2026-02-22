package com.github.mkram17.bazaarutils.features.gui.buttons;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.features.gui.ButtonsConfig;
import com.github.mkram17.bazaarutils.utils.annotations.autoregistration.RegisterWidget;
import com.github.mkram17.bazaarutils.ui.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

@ConfigObject
public class BazaarOpenOrdersButton {
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

    @ConfigEntry(
            id = "offset",
            translation = "bazaarutils.config.buttons.button.offset.value"
    )
    public int offset = 18;

    private static final Identifier DEFAULT = Identifier.tryParse(BazaarUtils.MOD_ID, "widget/generic_widget_base");
    private static final Identifier HOVERED = Identifier.tryParse(BazaarUtils.MOD_ID, "widget/generic_widget_hover");

    public static final ButtonTextures SLOT_BUTTON_TEXTURES = new ButtonTextures(
            DEFAULT,
            HOVERED
    );

    public BazaarOpenOrdersButton(boolean enabled) {
        this.enabled = enabled;
    };

    @RegisterWidget
    public static List<ItemSlotButtonWidget> getWidget() {
        if (!ButtonsConfig.OPEN_ORDERS.enabled) {
            return Collections.emptyList();
        }

        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();

        if (!(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen) || !screenInfo.inBazaar()) {
            return Collections.emptyList();
        }

        ItemSlotButtonWidget.ScreenWidgetDimensions dimensions = ItemSlotButtonWidget.getSafeScreenDimensions(screen, screenInfo.getScreenName());
        ItemSlotButtonWidget button = ButtonsConfig.OPEN_ORDERS.getItemSlotButtonWidget(dimensions);

        return Collections.singletonList(button);
    }

    private ItemSlotButtonWidget getItemSlotButtonWidget(ItemSlotButtonWidget.ScreenWidgetDimensions dimensions) {
        int buttonX = dimensions.x() - size - spacing;
        int currentButtonY = dimensions.y() + spacing + (offset + spacing);

        return new ItemSlotButtonWidget(
                buttonX,
                currentButtonY,
                size, size,
                SLOT_BUTTON_TEXTURES,
                (btn) -> {
//                    GUIUtils.closeHandledScreen();
                    PlayerActionUtil.runCommand("managebazaarorders");
                },
                Items.BOOK.getDefaultStack(),
                Text.literal("Go to Orders (Requires Cookie)")
        );
    }
}
