package com.github.mkram17.bazaarutils.features.gui.buttons;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.events.listener.BUListener;
import com.github.mkram17.bazaarutils.misc.BUCompatibilityHelper;
import com.github.mkram17.bazaarutils.misc.autoregistration.RegisterWidget;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.ui.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.utils.*;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import com.github.mkram17.bazaarutils.ui.CustomItemButton;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderType;
import com.github.mkram17.bazaarutils.utils.bazaar.market.price.PricingPosition;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ConfigObject
public class CancelOrderAndSearch extends BUListener implements CustomItemButton {
    @ConfigEntry(
            id = "enabled",
            translation = "bazaarutils.config.buttons.button.enabled.value"
    )
    @Comment(
            value = "Whether the button will be registered or not",
            translation = "bazaarutils.config.buttons.button.enabled.description"
    )
    public boolean enabled;

    @Getter
    @ConfigEntry(
            id = "slot_number",
            translation = "bazaarutils.config.buttons.button.slot_number.value"
    )
    @Comment(
            value = "The container slot where the button will be registered at",
            translation = "bazaarutils.config.buttons.button.slot_number.description"
    )
    @ConfigOption.Range(min = 0, max = 35)
    public int slotNumber;

    @ConfigEntry(
            id = "item_id",
            translation = "bazaarutils.config.buttons.button.item_id.value"
    )
    @Comment(
            value = "The item id which will be used as a reference to construct the button (make sure it is valid)",
            translation = "bazaarutils.config.buttons.button.item_id.description"
    )
    public String itemId;

    @Getter
    private transient ItemStack replacementItem;

    public CancelOrderAndSearch(boolean enabled, int slotNumber, String itemId) {
        this.enabled = enabled;
        this.slotNumber = slotNumber;
        this.itemId = itemId;
        this.replacementItem = Items.BLUE_TERRACOTTA.getDefaultStack();
    }

    private transient OrderInfo orderInfo;

    private Boolean inCorrectScreen; // access boolean using isInCorrectScreen()

//        !! see comment at constructor
//    private boolean isInCorrectScreen() {
//        if(inCorrectScreen == null){
//            var screen = ScreenInfo.getCurrentScreenInfo();
//            return screen.inMenu(ScreenInfo.BazaarMenuType.CANCEL_ORDER);
//        } else {
//            return inCorrectScreen;
//        }
//
//        return inCorrectScreen;
//    }
//
//    @EventHandler
//    private void replaceItem(ReplaceItemEvent event) {
//        if(!isInCorrectScreen() || !shouldReplaceItem(event)) return;
//        event.setReplacement(super.replacementItem);
//    }
//
//    @EventHandler
//    private void onClick(SlotClickEvent event) {
//        if(!isInCorrectScreen() || !wasButtonSlotClicked(event)) return;
//        GUIUtils.closeHandledScreen();
//        PlayerActionUtil.runCommand("bz " + orderInfo.getName());
//    }

}
