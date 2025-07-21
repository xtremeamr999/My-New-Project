package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


//TODO figure out how to handle rounding with price
//TODO use last viewed item in bazaar to help with finding accurate price instead of just chat message
@Slf4j
public class OrderData implements BUListener {

    private static final double DEFAULT_TOLERANCE = 0.9;
    private static final double TOTAL_PRICE_ROUNDING_THRESHOLD = 10000;

    public enum statuses {SET, FILLED, OUTDATED, COMPETITIVE, MATCHED}

    @Getter
    private final String name; //name of the item in game
    @Getter
    private final String productID; //hypixel's code for the product
    @Getter
    @Setter
    private statuses fillStatus; //only used to determine if order is set or filled, not outdated, competitive, or matched
    @Getter
    private final Integer volume;
    @Getter
    @Setter
    private int amountClaimed = 0;
    @Getter
    @Setter
    private int amountFilled = 0;
    @Getter
    @Setter
    private double tolerance;
    @Getter @Setter
    private OrderPriceInfo priceInfo;
    @Getter
    @Setter
    private OrderItemInfo itemInfo;

    //When finding item price, it can round to the nearest coin sometimes, so tolerance is needed for price calculations
    public OrderData(String name, Integer volume, OrderPriceInfo priceInfo) {
        this.name = name;
        this.volume = volume;
        this.productID = BazaarData.findProductId(name);
        this.fillStatus = statuses.SET;
        this.priceInfo = priceInfo;
        this.tolerance = calculateTolerance();

        if (productID == null && name != null) {
            Util.notifyError("Product ID for " + name + " is null. This may cause issues", new Throwable());
        }
    }

    public OrderData(String name, Integer volume, OrderPriceInfo priceInfo, OrderItemInfo itemInfo) {
        this(name, volume, priceInfo);
        this.itemInfo = itemInfo;
    }

    private double calculateTolerance() {
        //default tolerance
        if (priceInfo.getPricePerItem() == null || volume == null) {
            return DEFAULT_TOLERANCE;
        }
        //doesnt round prices when total is over 10k
        if (priceInfo.getPricePerItem() * volume < TOTAL_PRICE_ROUNDING_THRESHOLD) {
            return 0;
        } else {
            double priceMaximumInaccuracy = DEFAULT_TOLERANCE / volume; //0.9 coins is the most that it can be off per unit and not show in places where it rounds
            return (Math.round(priceMaximumInaccuracy * 10)) / 10.0;
        }
    }

    public int getIndex() {
        return BUConfig.get().watchedOrders.indexOf(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(name: ").append(name).append("[").append(getIndex()).append("]")
            .append(", price:").append(priceInfo.getPricePerItem())
            .append(", volume: ").append(volume);
        if (amountClaimed != 0) {
            sb.append(", amount claimed: ").append(amountClaimed);
        }
        sb.append(", type: ").append(priceInfo.getPriceType().getString());
        if (fillStatus == statuses.FILLED) {
            sb.append(", status: ").append(fillStatus);
        }
        sb.append(")");
        return sb.toString();
    }

    public void flipItem(double newPrice) {
        priceInfo.flipPrices(newPrice);
        this.amountFilled = 0;
        this.fillStatus = statuses.SET;
    }

    //TODO some error with maximum rounding or finding the price. either finding price can round down by .1 accidentally or maximum rounding calculation is wrong
    private boolean isSimilarPrice(double price) {
        return Util.genericIsSimilarValue(priceInfo.getPricePerItem(), price, tolerance);
    }



    //run by ex: getVariables((item) -> item.getPrice()) orItemData.getVariables(ItemData::getPrice);
    public static <T> List<T> getVariables(Function<OrderData, T> variable) {
        return BUConfig.get().watchedOrders.stream()
            .map(variable)
            .toList();
    }

    public boolean isSimilarTo(OrderData other, boolean isStrict) {
        String otherOrderName = other.getName();
        Double otherOrderPrice = other.getPriceInfo().getPricePerItem();
        Integer otherOrderVolume = other.getVolume();
        int otherOrderAmountUnclaimed = other.getAmountFilled() - other.getAmountClaimed();
        OrderPriceInfo.priceTypes priceType = other.getPriceInfo().getPriceType();

        if (isStrict) {
            return isStrictlySimilarTo(otherOrderName, otherOrderPrice, otherOrderVolume, priceType);
        }
        return isLooselySimilarTo(otherOrderName, otherOrderPrice, otherOrderVolume, otherOrderAmountUnclaimed, priceType);
    }

    private boolean isStrictlySimilarTo(String otherOrderName, Double otherOrderPrice, Integer otherOrderVolume, OrderPriceInfo.priceTypes priceType) {
        return (areAnyNull(this.getPriceInfo().getPricePerItem(), otherOrderPrice) || this.isSimilarPrice(otherOrderPrice)) &&
            (areAnyNull(this.getVolume(), otherOrderVolume) || this.getVolume().equals(otherOrderVolume)) &&
            (areAnyNull(this.getName(), otherOrderName) || this.getName().equalsIgnoreCase(otherOrderName)) &&
            (areAnyNull(this.getPriceInfo().getPriceType(), priceType) || this.getPriceInfo().getPriceType() == priceType);
    }

    private boolean isLooselySimilarTo(String otherOrderName, Double otherOrderPrice, Integer otherOrderVolume, int otherOrderAmountUnclaimed, OrderPriceInfo.priceTypes priceType) {
        return (areAnyNull(this.getPriceInfo().getPricePerItem(), otherOrderPrice) || this.isSimilarPrice(otherOrderPrice)) &&
            (areAnyNull(this.getVolume(), otherOrderVolume) || Util.genericIsSimilarValue(this.getVolume(), otherOrderVolume, 0.05 * otherOrderVolume) || this.getVolume().equals(otherOrderAmountUnclaimed)) && // sometimes the only volume that can be found is the amount that is unclaimed, like in FlipHelper
            (areAnyNull(this.getName(), otherOrderName) || this.getName().equalsIgnoreCase(otherOrderName)) &&
            (areAnyNull(this.getPriceInfo().getPriceType(), priceType) || this.getPriceInfo().getPriceType() == priceType);
    }

    private boolean areAnyNull(Object... objects) {
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    public Optional<OrderData> findOrderInList(List<OrderData> list) {
        List<OrderData> itemList = findAllMatchesInList(list);
        if (itemList.size() > 1) {
            return Optional.of(findBestMatch(itemList));
        }
        if (itemList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(itemList.getFirst());
    }

    public List<OrderData> findAllMatchesInList(List<OrderData> list) {
        List<OrderData> itemList = new ArrayList<>();
        for (OrderData item : list) {
            if (this.isSimilarTo(item, true)) {
                itemList.add(item);
            }
        }
        if (itemList.isEmpty()) {
            for (OrderData item : list) {
                if (this.isSimilarTo(item, false)) {
                    itemList.add(item);
                }
            }
        }
        return itemList;
    }

    /* Used for when there are duplicate matches found and the best should be chosen to use.
    Typically, volume is the variable that is different, but it can also be price
    */
    private OrderData findBestMatch(List<OrderData> list) {
        return list.stream()
            .min(getVolumeThenPriceComparator())
            .orElse(list.getFirst());
    }

    private Comparator<OrderData> getVolumeThenPriceComparator() {
        Comparator<OrderData> volumeComparator = Comparator.comparingDouble(order -> {
            if (areAnyNull(this.getVolume(), order.getVolume())) {
                return Double.MAX_VALUE;
            }
            return Math.abs(order.getVolume() - this.getVolume());
        });

        Comparator<OrderData> priceComparator = Comparator.comparingDouble(order -> {
            if (areAnyNull(this.getPriceInfo().getPricePerItem(), order.getPriceInfo().getPricePerItem())) {
                return Double.MAX_VALUE;
            }
            return Math.abs(order.getPriceInfo().getPricePerItem() - this.getPriceInfo().getPricePerItem());
        });

        return volumeComparator.thenComparing(priceComparator);
    }

    public void updateMarketPrice() {
        priceInfo.updateMarketPrice(productID);
    }

    @Override
    public void subscribe() {
        scheduleHealthCheck();
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
        return !(productID == null || productID.isEmpty() || BazaarData.findItemPrice(productID, priceInfo.getPriceType()) == null);
    }

    public statuses getOutdatedStatus() {
        updateMarketPrice();
        if (fillStatus == statuses.FILLED) {
            return statuses.FILLED;
        }
        if (priceInfo.getPricePerItem().equals(priceInfo.getMarketPrice()) && tolerance == 0 && BazaarData.getOrderCount(productID, priceInfo.getPriceType(), priceInfo.getPricePerItem()) > 1) {
            return statuses.MATCHED;
        }

        if (priceInfo.getPriceType() == OrderPriceInfo.priceTypes.INSTABUY) {
            if (priceInfo.getPricePerItem() - tolerance > priceInfo.getMarketPrice()) {
                return statuses.OUTDATED;
            }
        } else if (priceInfo.getPriceType() == OrderPriceInfo.priceTypes.INSTASELL) {
            if (priceInfo.getPricePerItem() + tolerance < priceInfo.getMarketPrice()) {
                return statuses.OUTDATED;
            }
        }

        return statuses.COMPETITIVE;
    }

    public double getFlipPrice() {
        updateMarketPrice();
        if (priceInfo.getMarketOppositePrice() == 0) {
            return 0;
        }
        if (priceInfo.getPriceType() == OrderPriceInfo.priceTypes.INSTABUY) {
            return (priceInfo.getMarketOppositePrice() + .1);
        } else {
            return (priceInfo.getMarketOppositePrice() - .1);
        }
    }

    public void setFilled() {
        amountFilled = volume;
        fillStatus = statuses.FILLED;
    }

    public void removeFromWatchedItems() {
        if (!BUConfig.get().watchedOrders.remove(this)) {
            PlayerActionUtil.notifyAll("Error removing " + name + " from watched items. Item couldn't be found.");
        }
        Util.scheduleConfigSave();
    }
}
