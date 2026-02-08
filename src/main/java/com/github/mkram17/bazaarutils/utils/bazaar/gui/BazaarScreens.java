package com.github.mkram17.bazaarutils.utils.bazaar.gui;

import com.github.mkram17.bazaarutils.utils.minecraft.gui.ScreenManager;
import com.github.mkram17.bazaarutils.utils.minecraft.gui.ScreenType;

import java.util.*;

// Text references are somewhat short as often they're split between multiple components if they're large and current Util.findComponentWith don't handle that case
public class BazaarScreens {
    private static boolean initialized = false;

    private BazaarScreens() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        for (ScreenType screen : ALL) {
            ScreenManager.register(screen);
        }
    }

//    Screens

    public static final ScreenType MAIN_PAGE = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("Bazaar ➜ ")
            .build();

    public static final ScreenType SETTINGS_PAGE = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("Settings")
            .build();

    public static final ScreenType ORDERS_PAGE = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("Bazaar Orders")
            .build();

//    Browsing stuff

    public static final ScreenType ITEM_PAGE = new ScreenType.Builder()
            .genericContainer()
            .containerTitle(" ➜ ")
            .containerQuery(BazaarSlots.ITEM_PAGE.VIEW_GRAPHS::query)
            .build();

    public static final ScreenType ITEMS_GROUP_PAGE = new ScreenType.Builder()
            .genericContainer()
            .containerTitle(" ➜ ")
            .containerQuery(BazaarSlots.ITEMS_GROUP_PAGE.SWITCH_VIEW_MODE::query)
            .build();

//    Buying stuff

    public static final ScreenType BUY_ORDER_AMOUNT = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("How many do you want?")
            .build();

    public static final ScreenType BUY_ORDER_PRICE = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("How much do you want to pay?")
            .build();

    public static final ScreenType BUY_ORDER_CONFIRMATION = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("Confirm Buy Order")
            .build();

    public static final ScreenType PENDING_BUY_ORDER_OPTIONS = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("Order options")
            .containerQuery(BazaarSlots.ORDER_OPTIONS.FLIP_UNFILLED_BUY_ORDER::query)
            .containerQuery(BazaarSlots.ORDER_OPTIONS.CANCEL_UNFILLED_BUY_ORDER::query)
            .build();

    public static final ScreenType COMPLETED_BUY_ORDER_OPTIONS = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("Order options")
            .containerQuery(BazaarSlots.ORDER_OPTIONS.FLIP_FILLED_BUY_ORDER::query)
            .containerQuery(BazaarSlots.ORDER_OPTIONS.CANCEL_FILLED_BUY_ORDER::query)
            .build();

    public static final ScreenType INSTANT_BUY = new ScreenType.Builder()
            .genericContainer()
//            .containerTitle("➜ Instant Buy")
            .containerTitle("➜ Inst")
            .containerQuery(BazaarSlots.INSTANT_BUY.INPUT_CUSTOM_AMOUNT::query)
            .build();

//    Selling stuff

    public static final ScreenType SELL_ORDER_AMOUNT = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("How many are you selling?")
            .build();

    public static final ScreenType SELL_ORDER_PRICE = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("At what price are you selling?")
            .build();

    public static final ScreenType SELL_ORDER_CONFIRMATION = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("Confirm Sell Offer")
            .build();

    public static final ScreenType SELL_ORDER_OPTIONS = new ScreenType.Builder()
            .genericContainer()
            .containerTitle("Order options")
            .containerQuery(BazaarSlots.ORDER_OPTIONS.CANCEL_SELL_ORDER::query)
            .build();

    public static final ScreenType INSTANT_SELL = new ScreenType.Builder()
            .genericContainer()
//            .containerTitle("➜ Instant Sell")
            .containerTitle("➜ Inst")
            .containerQuery(BazaarSlots.INSTANT_SELL.SELL_INVENTORY::query)
            .build();

    public static final Set<ScreenType> ALL = Set.of(
            MAIN_PAGE,
            SETTINGS_PAGE,
            ORDERS_PAGE,

            ITEM_PAGE,
            ITEMS_GROUP_PAGE,

            BUY_ORDER_AMOUNT,
            BUY_ORDER_PRICE,
            BUY_ORDER_CONFIRMATION,
            PENDING_BUY_ORDER_OPTIONS,
            COMPLETED_BUY_ORDER_OPTIONS,
            INSTANT_BUY,

            SELL_ORDER_AMOUNT,
            SELL_ORDER_PRICE,
            SELL_ORDER_CONFIRMATION,
            SELL_ORDER_OPTIONS,
            INSTANT_SELL
    );
}
