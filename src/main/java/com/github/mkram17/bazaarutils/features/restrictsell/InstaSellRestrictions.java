package com.github.mkram17.bazaarutils.features.restrictsell;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import com.github.mkram17.bazaarutils.utils.instasell.InstaSellItem;
import com.github.mkram17.bazaarutils.utils.instasell.InstaSellUtil;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//TODO maybe color chest if it is locked
public class InstaSellRestrictions implements BUListener {
    public enum restrictBy{PRICE, VOLUME, NAME}
    @Getter @Setter
    private boolean enabled;
    private final int safetyClicksRequired;
    @Getter @Setter
    private ArrayList<SellRestrictionControl> controls;
    @Getter
    private int safetyClicks = 0;

    public void addSafetyClick(){
        safetyClicks++;
    }
    public void resetSafetyClicks(){
        safetyClicks = 0;
    }

    public InstaSellRestrictions(boolean enabled, int safetyClicksRequired, ArrayList<SellRestrictionControl> controls) {
        this.enabled = enabled;
        this.safetyClicksRequired = safetyClicksRequired;
        this.controls = controls;
    }
    private void registerScreenEvent() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            safetyClicks = 0;
        });
    }
    @EventHandler
    private void onChestLoaded(ChestLoadedEvent e) {
        if (!enabled || !ScreenInfo.getCurrentScreenInfo().inBazaar())
            return;

        List<InstaSellItem> items = InstaSellUtil.getInstaSellOrders(e.getItemStacks());
    }

    private boolean shouldRestrictInstaSell(List<InstaSellItem> items){
        List<String> names = items.stream().map(InstaSellItem::name).toList();
        List<Double> prices = items.stream().map(instaSellItem -> instaSellItem.priceInfo().getPricePerItem()).toList();
        return isRestrictedByNames(names) || isRestrictedByPrices(prices);
    }

    private boolean isRestrictedByNames(List<String> names){
        List<String> nameRestrctions = controls.stream()
                .filter(sellRestrictionControl -> sellRestrictionControl.getRule() == restrictBy.NAME)
                .map(SellRestrictionControl::getName)
                .toList();
        return names.stream()
                .anyMatch(nameRestrctions::contains);
    }

    private boolean isRestrictedByPrices(List<Double> prices){
        List<Double> priceRestrictions = controls.stream()
                .filter(sellRestrictionControl -> sellRestrictionControl.getRule() == restrictBy.PRICE)
                .map(SellRestrictionControl::getAmount)
                .toList();
        return priceRestrictions.stream()
                .anyMatch(prices::contains);
    }

    public void addRule(restrictBy newRule, double limit){
        controls.add(new SellRestrictionControl(newRule, limit));
    }
    public void addRule(restrictBy newRule, String name){
        controls.add(new SellRestrictionControl(newRule, name));
    }

    public String getMessage(){
        StringBuilder message = new StringBuilder("Sell protected by rules:");
        for(SellRestrictionControl control : controls) {
            if(!control.isEnabled())
                continue;
            if (control.getRule() == restrictBy.PRICE)
                message.append(" PRICE: ");
            else if(control.getRule() == restrictBy.VOLUME)
                message.append(" VOLUME: ");
            else {
                message.append(" NAME: ");
                message.append(control.getName());
                continue;
            }
            message.append(control.getAmount());
        }
        message.append(" (Safety Clicks Left: ").append(3 - safetyClicks).append(")");
        return message.toString();
    }

    public Option<Boolean> createRuleOption(SellRestrictionControl control) {
        // Determine display text based on rule type
        Text nameText;
        Text descriptionText;

        if (control.getRule() == restrictBy.NAME) {
            String itemName = control.getName(); // Assuming getName() exists for NAME rules
            nameText = Text.literal("Item: " + itemName);
            descriptionText = Text.literal("Block insta-sell for item: " + itemName);
        } else {
            double amount = control.getAmount();
            String typeText = control.getRule() == restrictBy.VOLUME ? "Volume < " : "Price < ";
            nameText = Text.literal(typeText + amount);
            String desc = control.getRule() == restrictBy.PRICE ?
                    "Block insta-sell if price exceeds " + amount :
                    "Block insta-sell if volume exceeds " + amount;
            descriptionText = Text.literal(desc);
        }

        return Option.<Boolean>createBuilder()
                .name(nameText)
                .description(OptionDescription.of(descriptionText))
                .binding(
                        false,
                        control::isEnabled,
                        control::setEnabled
                )
                .controller(BUConfigGui::createBooleanController)
                .build();
    }

    public void buildOptions(OptionGroup.Builder builder){
        for(SellRestrictionControl control : getControls()){
            builder.option(createRuleOption(control));
        }
    }
    @Override
    public void subscribe() {
        registerScreenEvent();
        EVENT_BUS.subscribe(this);
    }
}
