package com.github.mkram17.bazaarutils.utils.bazaar.market.price;

import com.github.mkram17.bazaarutils.utils.bazaar.data.BazaarDataManager;
import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.Order;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderType;
import lombok.Getter;
import lombok.Setter;

/**
 * Container for market price metadata of a single product. For actual user orders, prefer
 * {@link OrderInfo} or {@link Order}.
 */
public class PriceInfo {
    @Setter @Getter
    protected OrderType orderType;

    @Setter @Getter
    protected PricingPosition pricingPosition;

    @Setter @Getter
    protected Double pricePerItem;

    @Setter
    private Double marketBuyPrice;

    @Setter
    private Double marketSellPrice;

    public PriceInfo(Double pricePerItem, OrderType orderType) {
        this.orderType = orderType;

        if (pricePerItem != null) {
            //TODO figure out best rounding. Eg to the tenth, hundredth or thousandth
            this.pricePerItem = (double) Math.round(pricePerItem * 10) / 10;
        }
        if (orderType == null) {
            //if the orderType is null, it's value doesn't matter, but the rest of the code needs a value to run as expected, so we give a default value
            //TODO revisit whether this still needs to have default value
            this.orderType = OrderType.SELL;
        }
    }

    public void flipPrices(double newPrice) {
        this.orderType = this.orderType.getOpposite();
        this.pricePerItem = newPrice;
    }

    protected void updateMarketPrice(String productId) {
        var buyPriceOpt = BazaarDataManager.findItemPriceOptional(productId, OrderType.BUY);
        var sellPriceOpt = BazaarDataManager.findItemPriceOptional(productId, OrderType.SELL);

        Util.logMessage("buyPriceOpt (marketBuyPrice): " + buyPriceOpt.getAsDouble());
        Util.logMessage("sellPriceOpt (marketSellPrice): " + sellPriceOpt.getAsDouble());

        buyPriceOpt.ifPresent(price -> marketBuyPrice = Util.truncateNum(price));
        sellPriceOpt.ifPresent(price -> marketSellPrice = Util.truncateNum(price));
    }

    public Double getPriceForPosition(PricingPosition pricingPosition, OrderType orderType) {
        return switch (orderType) {
            case SELL -> switch (pricingPosition) {
                case COMPETITIVE -> marketSellPrice - 0.1;
                case MATCHED -> marketSellPrice;
                case OUTBID -> marketSellPrice + 0.1;
            };
            case BUY -> switch (pricingPosition) {
                case COMPETITIVE -> marketBuyPrice + 0.1;
                case MATCHED -> marketBuyPrice;
                case OUTBID -> marketBuyPrice - 0.1;
            };
        };
    }
}
