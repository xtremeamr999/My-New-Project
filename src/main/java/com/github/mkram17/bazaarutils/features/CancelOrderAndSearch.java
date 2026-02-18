package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import com.github.mkram17.bazaarutils.ui.CustomItemButton;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

@ConfigObject
public class CancelOrderAndSearch extends CustomItemButton {
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
            id = "slot",
            translation = "bazaarutils.config.buttons.button.slot.value"
    )
    @Comment(
            value = "The container slot where the button will be registered at",
            translation = "bazaarutils.config.buttons.button.slot.description"
    )
    @ConfigOption.Range(min = 0, max = 35)
    public int slotNumber;

    @ConfigEntry(
            id = "itemId",
            translation = "bazaarutils.config.buttons.button.itemId.value"
    )
    @Comment(
            value = "The item id which will be used as a reference to construct the button (make sure it is valid)",
            translation = "bazaarutils.config.buttons.button.itemId.description"
    )
    public String itemId;

    public CancelOrderAndSearch(boolean enabled, int slotNumber, String itemId) {
        this.enabled = enabled;
        this.slotNumber = slotNumber;
        this.itemId = itemId;

//      TODO: Consider either deprecating or making of CustomItemButton either an interface or a record,
//       for no matter whether we @ConfigEntry or @ConfigObject it,
//       ResourcefulConfig will not reflect on a ConfigObjects' super class for fields to add/consider. **The subclass must own the field**.
//       I will personally come to this once I'm refactoring the InputHelper pr, for this button is just a subset of such.
//
//      Faulty and not final at all, just placed this here to prevent NPE's while development
        super.slotNumber = slotNumber;
        super.replacementItem = Items.BLUE_TERRACOTTA.getDefaultStack();
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
