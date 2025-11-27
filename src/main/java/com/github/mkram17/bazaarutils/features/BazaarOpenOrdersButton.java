package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.misc.autoregistration.RegisterWidget;
import com.github.mkram17.bazaarutils.ui.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

//brings you to the orders page as long as you have a cookie
public class BazaarOpenOrdersButton {

    @Getter @Setter
    private boolean enabled;
    private static final Identifier BASE = Identifier.tryParse(BazaarUtils.MODID, "widget/generic_widget_base");
    private static final Identifier HOVER = Identifier.tryParse(BazaarUtils.MODID, "widget/generic_widget_hover");
    public static final ButtonTextures SLOT_BUTTON_TEXTURES = new ButtonTextures(
            BASE,
            HOVER);

    public BazaarOpenOrdersButton(boolean enabled) {
        this.enabled = enabled;
    }

    @RegisterWidget
    public static List<ItemSlotButtonWidget> getWidget() {
        if(!BUConfig.get().bazaarOpenOrdersButton.isEnabled())
            return Collections.emptyList();

        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        if (!(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen) || !screenInfo.inBazaar())
            return Collections.emptyList();


        ItemSlotButtonWidget.ScreenWidgetDimensions dimensions = ItemSlotButtonWidget.getSafeScreenDimensions(screen, screenInfo.getScreenName());

        ItemSlotButtonWidget button = getItemSlotButtonWidget(dimensions);
        return Collections.singletonList(button);
    }

    private static ItemSlotButtonWidget getItemSlotButtonWidget(ItemSlotButtonWidget.ScreenWidgetDimensions dimensions) {
        int buttonSize = 18;
        int spacing = 4;
        int buttonOffset = 18; // to avoid overlap with other buttons since this is the second button down
        int buttonX = dimensions.x() - buttonSize - spacing;
        int currentButtonY = dimensions.y() + spacing + (buttonOffset + spacing) * 1;

        return new ItemSlotButtonWidget(
                buttonX,
                currentButtonY,
                buttonSize, buttonSize,
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
