package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.UserOrdersChangeEvent;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;


//TODO figure out how to handle rounding with price
//TODO use last viewed item in bazaar to help with finding accurate price instead of just chat message
@Slf4j
public class BazaarOrder extends OrderInfo {

    private static final double DEFAULT_TOLERANCE = 0.9;
    private static final double TOTAL_PRICE_ROUNDING_THRESHOLD = 10000;


    @Getter @Setter
    private Statuses fillStatus; //only used to determine if order is set or filled, not outdated, competitive, or matched
    @Getter
    private final Integer volume;
    @Getter @Setter
    private int amountClaimed = 0;
    @Getter @Setter
    private int amountFilled = 0;
    @Getter @Setter
    private ItemInfo itemInfo;
    @Getter @Setter
    private double tolerance; //When finding item price, it can round to the nearest coin sometimes, so tolerance is needed for price calculations

    public BazaarOrder(String name, Integer volume, Double pricePerItem, priceTypes priceType) {
        super(name, pricePerItem, priceType);
        this.volume = volume;
        this.fillStatus = Statuses.SET;
        this.tolerance = calculateTolerance();
    }

    public BazaarOrder(String name, Integer volume, Double pricePerItem, priceTypes priceType, ItemInfo itemInfo) {
        this(name, volume, pricePerItem, priceType);
        this.itemInfo = itemInfo;
    }

    private double calculateTolerance() {
        //default tolerance
        if (pricePerItem == null || volume == null) {
            return DEFAULT_TOLERANCE;
        }
        //doesnt round prices when total is over 10k
        if (pricePerItem * volume < TOTAL_PRICE_ROUNDING_THRESHOLD) {
            return 0;
        } else {
            double priceMaximumInaccuracy = DEFAULT_TOLERANCE / volume; //0.9 coins is the most that it can be off per unit and not show in places where it rounds
            return (Math.round(priceMaximumInaccuracy * 10)) / 10.0;
        }
    }


    public int getIndex() {
        return BUConfig.get().userOrders.indexOf(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(name: ").append(name).append("[").append(getIndex()).append("]")
                .append(", price:").append(pricePerItem)
                .append(", volume: ").append(volume);
        if (amountClaimed != 0) {
            sb.append(", amount claimed: ").append(amountClaimed);
        }
        sb.append(", type: ").append(getPriceType().getString());
        if (fillStatus == Statuses.FILLED) {
            sb.append(", status: ").append(fillStatus);
        }
        sb.append(")");
        return sb.toString();
    }

    public void flipItem(double newPrice) {
        flipPrices(newPrice);
        this.amountFilled = 0;
        this.fillStatus = Statuses.SET;
    }

    //TODO some error with maximum rounding or finding the price. either finding price can round down by .1 accidentally or maximum rounding calculation is wrong
    private boolean isSimilarPrice(double price) {
        return Util.genericIsSimilarValue(pricePerItem, price, tolerance);
    }

    //run by ex: getVariables((item) -> item.getPrice()) orItemData.getVariables(ItemData::getPrice);
    public static <T> List<T> getVariables(Function<BazaarOrder, T> variable) {
        return BUConfig.get().userOrders.stream()
                .map(variable)
                .toList();
    }

    public boolean isSimilarTo(BazaarOrder other, boolean isStrict) {
        String otherOrderName = other.getName();
        Double otherOrderPrice = other.getPricePerItem();
        Integer otherOrderVolume = other.getVolume();
        int otherOrderAmountUnclaimed = other.getAmountFilled() - other.getAmountClaimed();
        PriceInfo.priceTypes priceType = other.getPriceType();

        if (isStrict) {
            return isStrictlySimilarTo(otherOrderName, otherOrderPrice, otherOrderVolume, priceType);
        }
        return isLooselySimilarTo(otherOrderName, otherOrderPrice, otherOrderVolume, otherOrderAmountUnclaimed, priceType);
    }

    private boolean isStrictlySimilarTo(String otherOrderName, Double otherOrderPrice, Integer otherOrderVolume, PriceInfo.priceTypes priceType) {
        return (areAnyNull(this.pricePerItem, otherOrderPrice) || isSimilarPrice(otherOrderPrice)) &&
                (areAnyNull(this.volume, otherOrderVolume) || this.volume.equals(otherOrderVolume)) &&
                (areAnyNull(this.name, otherOrderName) || this.name.equalsIgnoreCase(otherOrderName)) &&
                (areAnyNull(this.priceType, priceType) || this.priceType == priceType);
    }

    private boolean isLooselySimilarTo(String otherOrderName, Double otherOrderPrice, Integer otherOrderVolume, int otherOrderAmountUnclaimed, PriceInfo.priceTypes priceType) {
        return (areAnyNull(this.pricePerItem, otherOrderPrice) || this.isSimilarPrice(otherOrderPrice)) &&
                (areAnyNull(this.volume, otherOrderVolume) || Util.genericIsSimilarValue(this.getVolume(), otherOrderVolume, 0.05 * otherOrderVolume) || this.getVolume().equals(otherOrderAmountUnclaimed)) && // sometimes the only volume that can be found is the amount that is unclaimed, like in FlipHelper
                (areAnyNull(this.name, otherOrderName) || this.getName().equalsIgnoreCase(otherOrderName)) &&
                (areAnyNull(this.priceType, priceType) || this.getPriceType() == priceType);
    }

    private boolean areAnyNull(Object... objects) {
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    public Optional<BazaarOrder> findOrderInList(List<BazaarOrder> list) {
        List<BazaarOrder> itemList = findAllMatchesInList(list);
        if (itemList.size() > 1) {
            return Optional.of(findBestMatch(itemList));
        }
        if (itemList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(itemList.getFirst());
    }

    public List<BazaarOrder> findAllMatchesInList(List<BazaarOrder> list) {
        List<BazaarOrder> itemList = new ArrayList<>();
        for (BazaarOrder item : list) {
            if (this.isSimilarTo(item, true)) {
                itemList.add(item);
            }
        }
        if (itemList.isEmpty()) {
            for (BazaarOrder item : list) {
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
    private BazaarOrder findBestMatch(List<BazaarOrder> list) {
        return list.stream()
                .min(getVolumeThenPriceComparator())
                .orElse(list.getFirst());
    }

    private Comparator<BazaarOrder> getVolumeThenPriceComparator() {
        Comparator<BazaarOrder> volumeComparator = Comparator.comparingDouble(order -> {
            if (areAnyNull(this.getVolume(), order.getVolume())) {
                return Double.MAX_VALUE;
            }
            return Math.abs(order.getVolume() - this.getVolume());
        });

        Comparator<BazaarOrder> priceComparator = Comparator.comparingDouble(order -> {
            if (areAnyNull(this.pricePerItem, order.getPricePerItem())) {
                return Double.MAX_VALUE;
            }
            return Math.abs(order.getPricePerItem() - this.pricePerItem);
        });

        return volumeComparator.thenComparing(priceComparator);
    }

    public double getFlipPrice() {
        updateMarketPrice(productID);
        if (getMarketOppositePrice() == 0) {
            return 0;
        }
        if (getPriceType() == com.github.mkram17.bazaarutils.misc.orderinfo.PriceInfo.priceTypes.INSTABUY) {
            return (getMarketOppositePrice() + .1);
        } else {
            return (getMarketOppositePrice() - .1);
        }
    }

    public void setFilled() {
        amountFilled = volume;
        fillStatus = Statuses.FILLED;
    }

    public void removeFromWatchedItems() {
        if (!BUConfig.get().userOrders.remove(this)) {
            PlayerActionUtil.notifyAll("Error removing " + name + " from watched items. Item couldn't be found.");
        }
        EVENT_BUS.post(new UserOrdersChangeEvent(UserOrdersChangeEvent.ChangeTypes.REMOVE, this));
        Util.scheduleConfigSave();
    }
}