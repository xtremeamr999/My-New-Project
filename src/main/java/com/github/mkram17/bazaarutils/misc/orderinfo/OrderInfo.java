package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.events.BazaarDataUpdateEvent;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;

import java.util.concurrent.TimeUnit;

//Can be used when you need to store info about an item with automatic price updates and health checks. Actual orders should use OrderData instead.
public class OrderInfo extends PriceInfo implements BUListener {

    @Getter
    protected final String name; //name of the item in game
    @Getter
    protected final String productID; //Hypixel's code for the product

    public OrderInfo(String name, Double pricePerItem, priceTypes priceType) {
        super(pricePerItem, priceType);
        this.name = name;
        this.productID = BazaarData.findProductId(name);
        validateProduct();
        subscribe();
        startActions();
        updateMarketPrice();
    }

    public void updateMarketPrice(){
        updateMarketPrice(productID);
    }

    private void validateProduct(){
        if (productID == null && name != null) {
            Util.notifyError("Product ID for " + name + " is null. This may cause issues", new Throwable());
        }
    }

    private void scheduleHealthCheck() {
        long START_DELAY_SECONDS = 60;
        long CHECK_INTERVAL_SECONDS = 30;
        BazaarUtils.BUExecutorService.scheduleAtFixedRate(() -> {
            if(!fixProductID()){
                Util.notifyError("Could not fix product ID for " + name + ". This may cause the mod to work improperly.", new Throwable());
            }
        }, START_DELAY_SECONDS, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }


    public void startActions(){
        scheduleHealthCheck();
    }

    //returns true if productID is safe/fixed after run, and false if it is not
    private boolean fixProductID() {
        if (isProductIDHealthy()) {
            return true;
        }
        String newProductID = BazaarData.findProductId(name);
        if (newProductID == null) {
            Util.logError("While refinding product id, could not find product ID for " + name, null);
            return false;
        } else {
            Util.logMessage("Successfully fixed product ID for " + name + ": " + newProductID);
            return true;
        }
    }

    private boolean isProductIDHealthy() {
        return !(productID == null || productID.isEmpty() || BazaarData.findItemPrice(productID, getPriceType()) == null);
    }

    @EventHandler
    private void onDataUpdate(BazaarDataUpdateEvent e){
        updateMarketPrice();
    }

    @Override
    public void subscribe() {
        BazaarUtils.EVENT_BUS.subscribe(this);
    }
}
