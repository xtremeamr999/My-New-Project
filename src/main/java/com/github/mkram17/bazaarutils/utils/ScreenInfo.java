package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.ScreenChangeEvent;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
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


    public static void initializeScreenInfo(Screen screen){
        currentScreenInfo = new ScreenInfo(screen);
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

    public boolean inBuyOrderScreen(){
        if(getContainerName() == null) return false;
        return getContainerName().contains("How many do you want?");
    }
    public boolean inInstaBuy(){
        if(getContainerName() == null) return false;
        return getContainerName().contains("➜ Insta");
    }
    public boolean inOrderScreen(){
        if(getContainerName() == null) return false;
        return getContainerName().contains("Bazaar Orders");
    }
    public boolean inSellSetup(){
        if(getContainerName() == null) return false;
        return getContainerName().contains("At what price are you selling?");
    }
    public boolean inConfirmSellOffer(){
        if(getContainerName() == null) return false;
        return getContainerName().contains("Confirm Sell Offer");
    }

    public boolean inFlipGui() {
        if (getContainerName() == null) {
            return false;
        }
        return getContainerName().contains("Order options");
    }

    public boolean inBazaarMainPage(){
        if (getContainerName() == null) {
            return false;
        }
        return getContainerName().contains("Bazaar ➜ ") && !inBazaarSettingsPage();
    }
    public boolean inBazaarSettingsPage(){
        if (getContainerName() == null) {
            return false;
        }
        return getContainerName().contains("Bazaar") && getContainerName().contains("Settings");
    }
    public boolean inItemGroupPage(){
        if (getContainerName() == null) {
            return false;
        }
        return getContainerName().contains("➜") && previousScreenHas(ScreenInfo::inBazaarMainPage);
    }

    public boolean inBazaar(){
        if(getContainerName() == null)
            return false;
        return inBuyOrderScreen() || inFlipGui() || inInstaBuy() || inBazaarMainPage() || inOrderScreen() || inSellSetup() || inConfirmSellOffer() || inItemGroupPage();
    }

    //only for specific items
    public boolean inAnyItemScreen(){
        if(getContainerName() == null || getContainerName().contains("Bazaar")) return false;
        return inItemGroupPage()
                || inBuyOrderScreen()
                || inInstaBuy();
    }

    public String getContainerName(){
        if(screen == null || screen.getTitle() == null)
            return null;
        return Util.removeFormatting(screen.getTitle().getString());
    }
}
