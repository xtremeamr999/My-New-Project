package com.github.mkram17.bazaarutils.data;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.ResourceManager;
import com.github.mkram17.bazaarutils.utils.Util;
import com.google.gson.JsonObject;
import net.hypixel.api.reply.skyblock.SkyBlockBazaarReply;

import java.util.concurrent.TimeUnit;

//TODO more efficient timing of api requests
public class BazaarData implements BUListener {
    public static final BazaarData INSTANCE = new BazaarData();

    private static SkyBlockBazaarReply bazaarReply = null;
    private static int bazaarDataPeriod = 1;
    private static int exceptionCount = 0;
    private static int bazaarCalls = 0;
    private static boolean skipNextCall = false;
    private static final long bazaarDataDelay = 10L;

    @Override
    public void subscribe(){
        scheduleBazaar();
    }

    public static void scheduleBazaar() {
        BazaarUtils.BUExecutorService.scheduleAtFixedRate(() -> {
            if (!(bazaarCalls % bazaarDataPeriod == 0))
                return;
            if (skipNextCall) {
                skipNextCall = false;
                return;
            }

            APIUtils.API.getSkyBlockBazaar().whenComplete((reply, throwable) -> {
                bazaarCalls++;
                if (bazaarCalls % 10 == 0 || bazaarCalls < 5)
                    skipNextCall = true;

                if (throwable != null) {
                    skipNextCall = true;
                    exceptionCount++;
                    Util.notifyError("Exception thrown trying to get bazaar data", throwable);
                    System.out.println("[Bazaar Utils] Error info: period-" + bazaarDataPeriod + ", exceptionCount-" + exceptionCount);
                    System.out.println("[Bazaar Utils] Status: " + APIUtils.API.getStatus(APIUtils.uuid));
                    if (exceptionCount % 5 == 0) {
                        bazaarDataPeriod++;
                    }
                } else {
                    if (reply == null) {
                        Util.notifyError("Bazaar data is null", new Throwable());
                        return;
                    }
                    bazaarReply = reply;
                    if (!BUConfig.get().watchedOrders.isEmpty()) {
                        BUConfig.get().outdatedOrderHandler.postOutdatedOrderEvents();
                    }
                }
            });
        }, bazaarDataDelay, 1, TimeUnit.SECONDS);
    }

    public static int getOrderCount(String productId, OrderPriceInfo.priceTypes priceType, double price) {
        if (bazaarReply == null) {
            Util.notifyError("Bazaar data is null", new Throwable());
            return -1;
        }
        try {
            SkyBlockBazaarReply.Product product = bazaarReply.getProduct(productId);
            if (product == null) {
                Util.logError("Could not find item using product ID: " + productId, null);
                return -1;
            }

            java.util.List<SkyBlockBazaarReply.Product.Summary> summaryList;
            if (priceType == OrderPriceInfo.priceTypes.INSTABUY) {
                summaryList = product.getBuySummary();
            } else if (priceType == OrderPriceInfo.priceTypes.INSTASELL) {
                summaryList = product.getSellSummary();
            } else {
                return -1; // invalid price type
            }

            int numOrders =0;

            for( SkyBlockBazaarReply.Product.Summary summary : summaryList) {
                if (summary.getPricePerUnit() == price) {
                    return (int) summary.getOrders();
                }
            }

            return numOrders;
        } catch (Exception e) {
            Util.notifyError("There was an error fetching order count (probably caused by incorrect product ID [" + productId + "])", e);
            return -1;
        }
    }

    public static Double findItemPrice(String productId, OrderPriceInfo.priceTypes priceType) {
        if (bazaarReply == null) {
            Util.notifyError("Bazaar data is null", new Throwable());
            return -1.0;
        }
        try {
            SkyBlockBazaarReply.Product product = bazaarReply.getProduct(productId);
            if (product == null) {
                Util.logError("Could not find item using product ID: " + productId, null);
                return -1.0;
            }

            var sell_order_summary = product.getBuySummary();
            var buy_order_summary = product.getSellSummary();

            if (priceType == OrderPriceInfo.priceTypes.INSTABUY) {
                if (sell_order_summary.isEmpty()) {
                    PlayerActionUtil.notifyAll("Buy summary is empty for product ID: " + productId, Util.notificationTypes.BAZAARDATA);
                    return 0.0;
                }
                double sellOrderPrice = sell_order_summary.getFirst().getPricePerUnit();
                return sellOrderPrice;
            } else if (priceType == OrderPriceInfo.priceTypes.INSTASELL) {
                if (buy_order_summary.isEmpty()) {
                    PlayerActionUtil.notifyAll("Sell summary is empty for product ID: " + productId + ", returning 0 for INSTABUY.", Util.notificationTypes.BAZAARDATA);
                    return 0.0;
                }
                double buyOrderPrice = buy_order_summary.getFirst().getPricePerUnit();
                return buyOrderPrice;
            }
        } catch (Exception e) {
            Util.notifyError("There was an error fetching product data (probably caused by incorrect product ID [" + productId + "])", e);
            return -1.0;
        }
        // Should not be reached if priceType is INSTASELL or INSTABUY
        return null;
    }

    //returns null if it cant find anything, gets product id from natural name
    public static String findProductId(String name) {
        JsonObject resources;
        JsonObject bazaarConversions;

        try {
            resources = ResourceManager.getResourceJson();
            bazaarConversions = resources.getAsJsonObject("bazaarConversions");

            for (String key : bazaarConversions.keySet()) {
                if (bazaarConversions.get(key).getAsString().equalsIgnoreCase(name)) {
                    return key;
                }
            }
        } catch (Exception e) {
            Util.notifyError("Exception caught while finding product ID from name: " + name + ". If this keeps happening, please report to the developer to fix. You can disable error notifications in settings", e);
        }

//        Util.logError("Couldn't find product id from name: " + name, null);
        return null;
    }

}