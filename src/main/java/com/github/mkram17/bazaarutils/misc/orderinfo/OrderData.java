package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.features.OutdatedOrderHandler;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


//TODO figure out how to handle rounding with price
//TODO use last viewed item in bazaar to help with finding accurate price instead of just chat message
@Slf4j
public class OrderData implements BUListener {
    public String getProductID() {
        return productID;
    }

    //name of the item in game
    @Getter
    private final String name;
    //hypixel's code for the product
    private final String productID;

    public int getIndex(){return BUConfig.get().watchedOrders.indexOf(this);}

    public String getGeneralInfo(){
        String str = "(name: " + name + "[" + getIndex() + "]" + ", price:" + priceInfo.getPrice() + ", volume: " + volume;
        if(amountClaimed != 0)
            str += ", amount claimed: " + amountClaimed;
        str += ", type: " + priceInfo.getPriceType().getString() + " order";
        if(fillStatus == statuses.FILLED)
            str += ", status: " + fillStatus;
        str +=  ")";
        return str;
    }

    public enum statuses{SET,FILLED, OUTDATED, COMPETITIVE, MATCHED}

    //only used to determine if order is set or filled, not outdated, competitive, or matched
    @Setter @Getter
    private statuses fillStatus;
    @Getter
    private final Integer volume;

    @Setter
    @Getter
    private int amountClaimed = 0;
    @Setter
    @Getter
    private int amountFilled = 0;
    @Getter @Setter
    private double tolerance;
    @Getter
    private OrderPriceInfo priceInfo;
    @Getter
    private final OrderItemInfo itemInfo = new OrderItemInfo();

    //When finding item price, it can round to the nearest coin sometimes, so tolerance is needed for price calculations
    public OrderData(String name, Integer volume, OrderPriceInfo priceInfo) {
        this.name = name;
        this.volume = volume;
        this.productID = BazaarData.findProductId(name);
        this.fillStatus = statuses.SET;
        this.priceInfo = priceInfo;
        this.tolerance = calculateTolerance();

        if(productID == null && name != null){
            Util.notifyError("Product ID for " + name + " is null. This may cause issues", null);
            return;
        }
    }

    private double calculateTolerance(){
        //default tolerance
        if(priceInfo.getPrice() == null || volume == null)
            return 0.9;
        //doesnt round prices when total is over 10k
        if(priceInfo.getPrice()*volume < 10000)
            return 0;
        else{
            double priceMaximumInaccuracy = .9 / volume; //0.9 coins is the most that it can be off per unit and not show in places where it rounds
            return (Math.round(priceMaximumInaccuracy * 10)) / 10.0;
        }
    }

    public void flipItem(double newPrice){
        priceInfo.flipPrices(newPrice);
        this.amountFilled = 0;
        this.fillStatus = statuses.SET;
    }

    //TODO some error with maximum rounding or finding the price. either finding price can round down by .1 accidentally or maximum rounding calculation is wrong
    public boolean isSimilarPrice(double price) {
        return Math.abs(priceInfo.getPrice() - price) <= tolerance;
    }

    //run by ex: getVariables((item) -> item.getPrice()) orItemData.getVariables(ItemData::getPrice);
    public static <T> ArrayList<T> getVariables(Function<OrderData, T> variable){
        ArrayList<T> variables = new ArrayList<>();
        for(OrderData item : BUConfig.get().watchedOrders){
            variables.add(variable.apply(item));
        }
        return variables;
    }

    private static ArrayList<OrderData> findLooseVolumeMatches(String name, Double price, Integer volume, OrderPriceInfo.priceTypes priceType){
        ArrayList<OrderData> itemList = new ArrayList<>();
        for(OrderData item : BUConfig.get().watchedOrders){
            if((price == null || item.isSimilarPrice(price)) &&
                    (volume == null || Math.abs(item.getVolume() - volume) <= (0.05 * volume) || Math.abs(item.getVolume()-item.getAmountClaimed() - volume) <= (0.05 * volume)) &&
                    (name == null || name.equalsIgnoreCase(item.getName())) &&
                    (priceType == null || priceType == item.getPriceInfo().getPriceType())){
                itemList.add(item);
            }
        }
        return itemList;
    }

    public boolean equals(OrderData order, boolean isStrict) {
        String name = order.getName();
        Double price = order.getPriceInfo().getPrice();
        Integer volume = order.getVolume();
        OrderPriceInfo.priceTypes priceType = order.getPriceInfo().getPriceType();

        if (isStrict) {
            return (cantCompare(this.getPriceInfo().getPrice(), price) || this.isSimilarPrice(price)) &&
                    (cantCompare(this.getVolume(), volume) || this.getVolume().equals(volume)) &&
                    (cantCompare(this.getName(), name) || this.getName().equalsIgnoreCase(name)) &&
                    (cantCompare(this.getPriceInfo().getPriceType(), priceType) || this.getPriceInfo().getPriceType() == priceType);
        }
        return (cantCompare(this.getPriceInfo().getPrice(), price) || this.isSimilarPrice(price)) &&
                (cantCompare(this.getVolume(), volume) || Math.abs(this.getVolume() - volume) <= (0.05 * volume)) &&
                (cantCompare(this.getName(), name) || this.getName().equalsIgnoreCase(name)) &&
                (cantCompare(this.getPriceInfo().getPriceType(), priceType) || this.getPriceInfo().getPriceType() == priceType);
    }

    private boolean cantCompare(Object... objects) {
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    public OrderData findItemInList(List<OrderData> list) {
        ArrayList<OrderData> itemList = new ArrayList<>();
        for(OrderData item : list){
            if(this.equals(item, true)){
                itemList.add(item);
            }
        }
        if (itemList.isEmpty()) {
            for(OrderData item : list){
                if(this.equals(item, false)){
                    itemList.add(item);
                }
            }
        }
        if (itemList.size() > 1) {
            OrderData bestMatch = itemList.getFirst();
            for (OrderData duplicate : itemList) {
                PlayerActionUtil.notifyAll("Duplicate item: " + duplicate.getGeneralInfo(), Util.notificationTypes.ITEMDATA);
                if (this.getVolume() == null) {
                    continue;
                }
                // Find the duplicate with the closest volume to the matching item
                if (Math.abs(duplicate.getVolume() - this.getVolume()) < Math.abs(bestMatch.getVolume() - this.getVolume())) {
                    bestMatch = duplicate;
                }
            }
            return bestMatch;
        }
        if (itemList.isEmpty()) {
            return null;
        }
        return itemList.getFirst();
    }

    public void updateMarketPrice(){
        priceInfo.updateMarketPrice(productID);
    }

    @Override
    public void subscribe() {
        scheduleHealthCheck();
    }

    private void scheduleHealthCheck(){
        BazaarUtils.BUExecutorService.scheduleAtFixedRate(() ->{
                if(!fixProductID()){
                   Util.notifyError("Could not fix product ID for " + name + ". This may cause the mod to work improperly.", null);
                }
        }, 1, 30, TimeUnit.SECONDS);
    }

    //returns true if productID is safe/fixed after run, and false if it is not
    private boolean fixProductID(){
        if(isProductIDHealthy()) {
            return true;
        }
        String newProductID = BazaarData.findProductId(name);
        if(newProductID == null){
            Util.logError("While refinding product id, could not find product ID for " + name, null);
            return false;
        } else {
            Util.logMessage("Successfully fixed product ID for " + name + ": " + newProductID);
            return true;
        }
    }

    private boolean isProductIDHealthy(){
        return !(productID == null || productID.isEmpty() || BazaarData.findItemPrice(productID, priceInfo.getPriceType()) == null);
    }

    public statuses getOutdatedStatus(){
        updateMarketPrice();
        if(fillStatus == statuses.FILLED)
            return statuses.FILLED;
        if(priceInfo.getPrice().equals(priceInfo.getMarketPrice()) && tolerance == 0 && BazaarData.getOrderCount(productID, priceInfo.getPriceType(), priceInfo.getPrice()) > 1)
            return statuses.MATCHED;

        if (priceInfo.getPriceType() == OrderPriceInfo.priceTypes.INSTABUY) {
            if(priceInfo.getPrice()- tolerance > priceInfo.getMarketPrice())
                return statuses.OUTDATED;
        } else if(priceInfo.getPriceType() == OrderPriceInfo.priceTypes.INSTASELL){
            if(priceInfo.getPrice()+ tolerance < priceInfo.getMarketPrice())
                return statuses.OUTDATED;
        }

        return statuses.COMPETITIVE;
    }

    public double getFlipPrice(){
        updateMarketPrice();
        if(priceInfo.getMarketOppositePrice() == 0)
            return 0;
        if (priceInfo.getPriceType() == OrderPriceInfo.priceTypes.INSTABUY) {
            return (priceInfo.getMarketOppositePrice() + .1);
        } else {
            return (priceInfo.getMarketOppositePrice() - .1);
        }
    }

    public void setFilled(){
        amountFilled = volume;
        fillStatus = statuses.FILLED;
    }

    public void removeFromWatchedItems(){
        if(!BUConfig.get().watchedOrders.remove(this))
            PlayerActionUtil.notifyAll("Error removing " + name + " from watched items. Item couldn't be found.");
        BUConfig.HANDLER.save();
        OutdatedOrderHandler.updateOrdersOutdatedStatuses();
    }
}
