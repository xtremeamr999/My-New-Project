package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.ScreenChangeEvent;
import lombok.Getter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//Utility class for current screen info
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
        ITEM_GROUP_PAGE("➜");

        @Getter
        private final String titleString;

        BazaarMenuType(String titleString) {
            this.titleString = titleString;
        }
    }

    public ScreenInfo(Screen screen) {
        this.screen = screen;
        subscribe();
    }

    @EventHandler
    private void onScreenChange(ScreenChangeEvent e){
        if(e.getNewScreen() == null)
            return;

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
    * returns true if the user is in any of the specified types of bazaar menus
    */
    public boolean inMenu(BazaarMenuType... types) {
        for (BazaarMenuType bazaarMenuType : types) {
            if (getContainerName().contains(bazaarMenuType.getTitleString())) {
                return true;
            }
        }
        return false;
    }

    public boolean inBazaar(){
        return inMenu(BazaarMenuType.BUY_ORDER, BazaarMenuType.FLIP_GUI,
                BazaarMenuType.INSTA_BUY, BazaarMenuType.BAZAAR_MAIN_PAGE,
                BazaarMenuType.ORDER_SCREEN, BazaarMenuType.SELL_SETUP,
                BazaarMenuType.CONFIRM_SELL_OFFER, BazaarMenuType.CONFIRM_BUY_OFFER,
                BazaarMenuType.ORDER_PRICE, BazaarMenuType.ITEM_GROUP_PAGE);
    }

    //only for specific items
    public boolean inAnyItemScreen(){
        if(inMenu(BazaarMenuType.BAZAAR_MAIN_PAGE)) return false;
        return inMenu(BazaarMenuType.ITEM_GROUP_PAGE, BazaarMenuType.BUY_ORDER, BazaarMenuType.INSTA_BUY);
    }

    public String getContainerName(){
        if(screen == null || screen.getTitle() == null)
            return null;
        return Util.removeFormatting(screen.getTitle().getString());
    }
}
