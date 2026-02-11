package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import com.github.mkram17.bazaarutils.ui.CustomItemButton;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

//TODO do this --high priority
public class CancelOrderAndSearch extends CustomItemButton {
    @Getter @Setter
    private boolean enabled;
    private OrderInfo orderInfo;

    public CancelOrderAndSearch(){
        super.slotNumber = 25; // below cancel button
        super.replacementItem = Items.BLUE_TERRACOTTA.getDefaultStack();
    }

    private Boolean inCorrectScreen; // access boolean using isInCorrectScreen()

    private boolean isInCorrectScreen(){
        if(inCorrectScreen == null){
            var screen = ScreenInfo.getCurrentScreenInfo();
            return screen.inMenu(ScreenInfo.BazaarMenuType.CANCEL_ORDER);
        } else {
            return inCorrectScreen;
        }
    }

    @EventHandler
    private void replaceItem(ReplaceItemEvent event) {
        if(!isInCorrectScreen() || !shouldReplaceItem(event)) return;
        event.setReplacement(super.replacementItem);
    }

    @EventHandler
    private void onClick(SlotClickEvent event) {
        if(!isInCorrectScreen() || !wasButtonSlotClicked(event)) return;
        GUIUtils.closeHandledScreen();
        PlayerActionUtil.runCommand("bz " + orderInfo.getName());
    }
}
