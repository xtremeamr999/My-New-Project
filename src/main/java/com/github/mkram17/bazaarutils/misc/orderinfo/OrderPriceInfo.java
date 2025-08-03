package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

public class OrderPriceInfo {
    @Getter
    public enum priceTypes{INSTASELL,INSTABUY;
        private priceTypes opposite;

        static {
            INSTASELL.opposite = INSTABUY;
            INSTABUY.opposite = INSTASELL;
        }

        public String getString(){
            return switch (this) {
                case INSTASELL -> "Buy";
                case INSTABUY -> "Sell";
            };
        }
    }

    @Setter @Getter
    protected Double pricePerItem;
    @Setter @Getter
    protected priceTypes priceType;
    @Getter
    private Double marketPrice;
    //the price of the opposite type of order
    @Getter @Setter
    private Double marketOppositePrice;

    public OrderPriceInfo(priceTypes priceType) {
        this.priceType = priceType;
    }

    public OrderPriceInfo(double pricePerItem, priceTypes priceType) {
        this.priceType = priceType;
        this.pricePerItem = (double) Math.round(pricePerItem * 100) / 100;
    }

    private void schedulePriceUpdates() {
        long START_DELAY_SECONDS = 0;
        long CHECK_INTERVAL_SECONDS = 1;
        BazaarUtils.BUExecutorService.scheduleAtFixedRate(() -> {

        }, START_DELAY_SECONDS, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void updateMarketPrice(String productId) {
        marketPrice = Util.getPrettyNumber(BazaarData.findItemPrice(productId, priceType));
        marketOppositePrice = Util.getPrettyNumber(BazaarData.findItemPrice(productId, priceType.getOpposite()));
    }

    public void flipPrices(double newPrice){
        this.priceType = this.priceType.getOpposite();
        this.pricePerItem = newPrice;
    }

    public static String getPrettyString(double theDouble){
        return String.format("$%,.1f", theDouble);
    }

    public String getMarketPriceString() {
        return priceType.getString() + " " + getPrettyString(marketPrice);
    }
    public String getOppositeMarketPriceString() {
        return priceType.getOpposite().getString() + " " +  getPrettyString(marketOppositePrice);
    }
}
