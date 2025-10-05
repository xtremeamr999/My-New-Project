package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.events.ScreenChangeEvent;
import lombok.Getter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

// Utility class for current screen info
public class ScreenInfo implements BUListener {
    @Getter
    private static ScreenInfo currentScreenInfo;
    @Getter
    private final Screen screen;
    @Getter
    private static final List<ScreenInfo> previousScreenInfos = new ArrayList<>();

    public enum BazaarMenuType {
        BUY_ORDER("How many do you want?"),
        INSTA_BUY("➜ Insta"),
        ORDER_SCREEN("Bazaar Orders"),
        SELL_SETUP("At what price are you selling?"),
        CONFIRM_SELL_OFFER("Confirm Sell Offer"),
        CONFIRM_BUY_OFFER("Confirm Buy Order"),
        ORDER_PRICE("How much do you want to pay?"),
        FLIP_GUI("Order options"),
        BAZAAR_MAIN_PAGE("Bazaar ➜ "),
        BAZAAR_SETTINGS_PAGE("Settings"),
        ITEM_GROUP_PAGE(info -> {
            String name = info.getContainerName();
            if (name == null || !name.contains("➜")) return false;
            return !GUIUtils.getChestItem(34).getItem().equals(Items.FURNACE);
        }),

        INDIVIDUAL_ITEM(info -> {
            String name = info.getContainerName();
            if (name == null || !name.contains("➜")) return false;
            return GUIUtils.getChestItem(33).getItem().equals(Items.PAPER);
        });

        @Getter
        private final String titleString;
        private final Predicate<ScreenInfo> matcher;

        // Constructor for simple title substring match
        BazaarMenuType(String titleString) {
            this.titleString = titleString;
            this.matcher = info -> {
                String n = info.getContainerName();
                return n != null && n.contains(titleString);
            };
        }

        // Constructor for complex predicate
        BazaarMenuType(Predicate<ScreenInfo> matcher) {
            this.titleString = null;
            this.matcher = matcher;
        }

        public boolean matches(ScreenInfo info) {
            return matcher.test(info);
        }
    }

    public ScreenInfo(Screen screen) {
        this.screen = screen;
        subscribe();
    }

    @EventHandler
    private void onScreenChange(ScreenChangeEvent e){
        if(e.getNewScreen() == null) return;

        EVENT_BUS.unsubscribe(this);

        currentScreenInfo = new ScreenInfo(e.getNewScreen());
        if(e.getOldScreen() == null){
            previousScreenInfos.clear();
        } else {
            previousScreenInfos.add(this);
        }
    }

    public static boolean previousScreenHas(Predicate<ScreenInfo> filter){
        return previousScreenInfos.stream().anyMatch(filter);
    }

    @Override
    public void subscribe() {
        EVENT_BUS.subscribe(this);
    }

    /*
     * returns true if the user is in any of the specified types of bazaar management
     */
    public boolean inMenu(BazaarMenuType... types) {
        for (BazaarMenuType bazaarMenuType : types) {
            if (bazaarMenuType.matches(this)) {
                return true;
            }
        }
        return false;
    }

    public boolean inBazaar(){
        return inMenu(
                BazaarMenuType.BUY_ORDER,
                BazaarMenuType.FLIP_GUI,
                BazaarMenuType.INSTA_BUY,
                BazaarMenuType.BAZAAR_MAIN_PAGE,
                BazaarMenuType.ORDER_SCREEN,
                BazaarMenuType.SELL_SETUP,
                BazaarMenuType.CONFIRM_SELL_OFFER,
                BazaarMenuType.CONFIRM_BUY_OFFER,
                BazaarMenuType.ORDER_PRICE,
                BazaarMenuType.ITEM_GROUP_PAGE,
                BazaarMenuType.INDIVIDUAL_ITEM
        );
    }

    // only for specific items
    public boolean inAnyItemScreen(){
        if(inMenu(BazaarMenuType.BAZAAR_MAIN_PAGE)) return false;
        return inMenu(BazaarMenuType.ITEM_GROUP_PAGE, BazaarMenuType.BUY_ORDER,
                BazaarMenuType.INSTA_BUY, BazaarMenuType.INDIVIDUAL_ITEM);
    }

    public String getContainerName(){
        if(screen == null || screen.getTitle() == null)
            return null;
        return Util.removeFormatting(screen.getTitle().getString());
    }
}