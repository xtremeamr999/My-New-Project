package com.github.mkram17.bazaarutils.features.restrictsell;

import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.features.restrictsell.controls.DoubleSellRestrictionControl;
import com.github.mkram17.bazaarutils.features.restrictsell.controls.SellRestrictionControl;
import com.github.mkram17.bazaarutils.features.restrictsell.controls.StringSellRestrictionControl;
import com.github.mkram17.bazaarutils.features.util.ConfigurableFeature;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.InstaSellUtil;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

import java.util.ArrayList;
import java.util.List;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//TODO maybe color chest if it is locked
@ConfigObject
public class InstaSellRestrictions implements BUListener, ConfigurableFeature {
    @Getter @Setter @ConfigEntry(id = "enabled")
    private boolean enabled;
    private static final int SAFETY_CLICKS_REQUIRED = 3; // Number of clicks it stops blocking insta-sell
    @Getter @Setter @ConfigEntry(id = "controls")
    private ArrayList<SellRestrictionControl> controls;
    @Getter
    private int safetyClicks = 0;
    private boolean isInstaSellRestricted;

    private static final int INSTA_SELL_SLOT_INDEX = 47;

    public InstaSellRestrictions(boolean enabled, ArrayList<SellRestrictionControl> controls) {
        this.enabled = enabled;
        this.controls = controls;
    }
    private void registerScreenEvent() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            safetyClicks = 0;
            isInstaSellRestricted = true;
        });
    }
    @EventHandler
    private void onChestLoaded(ChestLoadedEvent e) {
        if (!enabled || !ScreenInfo.getCurrentScreenInfo().inMenu(ScreenInfo.BazaarMenuType.BAZAAR_MAIN_PAGE))
            return;

        List<OrderInfo> items = InstaSellUtil.getInstaSellOrders(e.getItemStacks());
        isInstaSellRestricted = shouldRestrictInstaSell(items);
    }

    @EventHandler
    private void onClick(SlotClickEvent clickEvent){
        if(!enabled || clickEvent.slot.getIndex() != INSTA_SELL_SLOT_INDEX)
            return;
        if (isInstaSellRestricted && safetyClicks < SAFETY_CLICKS_REQUIRED) {
            safetyClicks++;
            PlayerActionUtil.notifyAll(getMessage());
            clickEvent.cancel();
        }
    }

    private boolean shouldRestrictInstaSell(List<OrderInfo> items){
        return items.stream().anyMatch(this::isItemRestricted);
    }

    public void addRule(SellRestrictionControl control){
        controls.add(control);
    }

    private boolean isItemRestricted(OrderInfo item) {
        for(SellRestrictionControl control : controls) {
            if(!control.isEnabled())
                continue;
            if (control.shouldRestrict(item)) {
                return true;
            }
        }
        return false;
    }

    public String getMessage(){
        StringBuilder message = new StringBuilder("Sell protected by rules:");
        for(SellRestrictionControl control : controls) {
            if(!control.isEnabled())
                continue;
            if(control instanceof DoubleSellRestrictionControl doubleControl) {
                if (control.getRule() == RestrictInstaSellBy.PRICE)
                    message.append(" PRICE: ");
                else if (control.getRule() == RestrictInstaSellBy.VOLUME)
                    message.append(" VOLUME: ");
                message.append(doubleControl.getAmount());
            } else {
                StringSellRestrictionControl stringControl = (StringSellRestrictionControl) control;
                message.append(" NAME: ");
                message.append(stringControl.getName());
            }
        }
        message.append(" (Safety Clicks Left: ").append(3 - safetyClicks).append(")");
        return message.toString();
    }

    @Override
    public void subscribe() {
        registerScreenEvent();
        EVENT_BUS.subscribe(this);
    }
}
