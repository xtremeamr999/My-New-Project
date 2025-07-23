package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;

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
    private Double pricePerItem;
    @Setter @Getter
    private priceTypes priceType;
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
        //TODO initialize market price updater here whenever object is created
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
