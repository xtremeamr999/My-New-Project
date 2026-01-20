package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;


/**
 * Container for market price metadata of a single product. For actual user orders, prefer
 * {@link OrderInfoContainer} or {@link BazaarOrder}.
 */
public class PriceInfoContainer {
    @Getter
    public enum PriceType {INSTASELL,INSTABUY;
        private PriceType opposite;

        static {
            INSTASELL.opposite = INSTABUY;
            INSTABUY.opposite = INSTASELL;
        }

        /**
         * @return Human-readable string for the price type as shown to players.
         */
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

    //insta sell price = buy order price, insta buy price = sell order price
    @Setter
    private Double marketInstaSellPrice;
    @Setter
    private Double marketInstaBuyPrice;

    public PriceInfoContainer(Double pricePerItem, PriceType priceType) {
        this.priceType = priceType;
        if(pricePerItem != null){
            //TODO figure out best rounding. Eg to the tenth, hundredth or thousandth
            this.pricePerItem = (double) Math.round(pricePerItem * 10) / 10;
        }
        if(priceType == null){
            //if the priceType is null, it's value doesn't matter, but the rest of the code needs a value to run as expected, so we give a default value
            //TODO revisit whether this still needs to have default value
            this.priceType = PriceType.INSTASELL;
        }
    }

    /**
     * @return current market price for the given {@link PriceType} or {@code -1.0} for error.
     */
    public Double getMarketPrice(PriceType priceType){
        return switch (priceType) {
            case INSTASELL -> marketInstaSellPrice;
            case INSTABUY -> marketInstaBuyPrice;
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
