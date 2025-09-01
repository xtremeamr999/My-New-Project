package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;


//Simply a container for information about price of an item. For actual orders, use OrderInfoContainer or BazaarOrder instead
public class PriceInfoContainer {
    @Getter
    public enum PriceType {INSTASELL,INSTABUY;
        private PriceType opposite;

        static {
            INSTASELL.opposite = INSTABUY;
            INSTABUY.opposite = INSTASELL;
        }

        public String getString(){
            return switch (this) {
                case INSTASELL -> "Buy";
                case INSTABUY -> "Sell";
                default -> "Undefined";
            };
        }
    }

    @Setter @Getter
    protected Double pricePerItem;
    @Setter @Getter
    protected PriceType priceType;
    @Getter
    private Double instaSellPrice;
    //the price of the opposite type of order
    @Getter @Setter
    private Double instaBuyPrice;

    public PriceInfoContainer(Double pricePerItem, PriceType priceType) {
        this.priceType = priceType;
        if(pricePerItem != null){
            //TODO figure out best rounding. Eg to the tenth, hundredth or thousandth
            this.pricePerItem = (double) Math.round(pricePerItem * 100) / 100;
        }
        if(priceType == null){
            //if the priceType is null, it's value doesn't matter, but the rest of the code needs a value to run as expected, so we give a default value of buy order
            this.priceType = PriceType.INSTASELL;
        }
    }

    public Double getMarketPrice(PriceType priceType){
        return switch (priceType) {
            case INSTASELL -> instaSellPrice;
            case INSTABUY -> instaBuyPrice;
        };
    }

    protected void updateMarketPrice(String productId) {
        var instaSellPriceOpt = BazaarData.findItemPriceOptional(productId, PriceType.INSTASELL);
        var instaBuyPriceOpt = BazaarData.findItemPriceOptional(productId, PriceType.INSTABUY);

        instaSellPriceOpt.ifPresent(price -> instaSellPrice = Util.truncateNum(price));
        instaBuyPriceOpt.ifPresent(price -> instaBuyPrice = Util.truncateNum(price));
    }

    public void flipPrices(double newPrice){
        this.priceType = this.priceType.getOpposite();
        this.pricePerItem = newPrice;
    }
}
