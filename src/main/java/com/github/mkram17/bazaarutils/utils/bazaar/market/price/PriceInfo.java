package com.github.mkram17.bazaarutils.utils.bazaar.market.price;

import com.github.mkram17.bazaarutils.utils.bazaar.data.BazaarDataManager;
import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.Order;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * Container for market price metadata of a single product. For actual user orders, prefer
 * {@link OrderInfo} or {@link Order}.
 */
public class PriceInfo {
    @Setter @Getter
    protected PriceType priceType;

    @Setter @Getter
    protected PricingPosition pricingPosition;

    @Setter @Getter
    protected Double pricePerItem;

    @Setter
    private Double marketInstaSellPrice;

    @Setter
    private Double marketInstaBuyPrice;

    public PriceInfo(Double pricePerItem, PriceType priceType) {
        this.priceType = priceType;

        if (pricePerItem != null) {
            //TODO figure out best rounding. Eg to the tenth, hundredth or thousandth
            this.pricePerItem = (double) Math.round(pricePerItem * 10) / 10;
        }
        if (priceType == null) {
            //if the priceType is null, it's value doesn't matter, but the rest of the code needs a value to run as expected, so we give a default value
            //TODO revisit whether this still needs to have default value
            this.priceType = PriceType.INSTABUY;
        }
    }

    public void flipPrices(double newPrice) {
        this.priceType = this.priceType.getOpposite();
        this.pricePerItem = newPrice;
    }

    protected void updateMarketPrice(String productId) {
        var instaSellPriceOpt = BazaarDataManager.findItemPriceOptional(productId, PriceType.INSTASELL);
        var instaBuyPriceOpt = BazaarDataManager.findItemPriceOptional(productId, PriceType.INSTABUY);

        instaSellPriceOpt.ifPresent(price -> marketInstaSellPrice = Util.truncateNum(price));
        instaBuyPriceOpt.ifPresent(price -> marketInstaBuyPrice = Util.truncateNum(price));
    }

    public Double getPriceForPosition(PricingPosition pricingPosition, PriceType priceType) {
        return switch (priceType) {
            case INSTABUY -> switch (pricingPosition) {
                case COMPETITIVE -> marketInstaBuyPrice - 0.1;
                case MATCHED -> marketInstaBuyPrice;
                case OUTBID -> marketInstaBuyPrice + 0.1;
            };
            case INSTASELL -> switch (pricingPosition) {
                case COMPETITIVE -> marketInstaSellPrice + 0.1;
                case MATCHED -> marketInstaSellPrice;
                case OUTBID -> marketInstaSellPrice - 0.1;
            };
        };
    }
}
