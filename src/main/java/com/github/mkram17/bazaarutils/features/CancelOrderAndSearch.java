package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderInfoContainer;
import com.github.mkram17.bazaarutils.misc.ui.CustomItemButton;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import dev.isxander.yacl3.api.Option;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.item.Items;
import net.minecraft.text.ClickEvent;

//TODO do this --high priority
public class CancelOrderAndSearch extends CustomItemButton implements BUListener {
    @Getter @Setter
    private boolean enabled;
    private OrderInfoContainer orderInfo;

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
        if(!isInCorrectScreen() || !shouldUseSlot(event)) return;
        GUIUtils.closeHandledScreen();
        PlayerActionUtil.runCommand("bz " + orderInfo.getName());
    }

    @Override
    public void subscribe() {
        BazaarUtils.EVENT_BUS.subscribe(this);
    }
}
