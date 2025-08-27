package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BazaarDataUpdateEvent;
import com.github.mkram17.bazaarutils.events.OutbidOrderEvent;
import com.github.mkram17.bazaarutils.events.UserOrdersChangeEvent;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import meteordevelopment.orbit.EventHandler;

import java.util.Optional;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;


//TODO figure out how to handle rounding with price
//TODO use last viewed item in bazaar to help with finding accurate price instead of just chat message
//An eventful OrderInfoContainer, also tracks info only needed for actual user orders
@Slf4j
public class BazaarOrder extends OrderInfoContainer {

    @Getter @Setter
    private Statuses fillStatus; //only used to determine if order is set or filled, not outdated, competitive, or matched
    @Getter @Setter
    private int amountClaimed = 0;
    @Getter
    private int amountFilled = 0;


    public BazaarOrder(String name, Integer volume, Double pricePerItem, PriceType priceType) {
        super(name, volume, pricePerItem, priceType);
        this.fillStatus = Statuses.SET;
        startTracking();
    }

    public BazaarOrder(String name, Integer volume, Double pricePerItem, PriceType priceType, ItemInfo itemInfo) {
        super(name, volume, pricePerItem, priceType, itemInfo);
        this.fillStatus = Statuses.SET;
        startTracking();
    }

    private void startTracking(){
        handleOutbidStatusChange();
        subscribe();
        scheduleHealthCheck();
        updateMarketPrice();
    }

    @EventHandler
    private void onDataUpdate(BazaarDataUpdateEvent e){
        updateMarketPrice();
        handleOutbidStatusChange();
    }

    @EventHandler
    private void onUserOrderChange(UserOrdersChangeEvent e) {
        if(e.getChangeType() == UserOrdersChangeEvent.ChangeTypes.REMOVE || e.getOrder() != this)
            return;
        updateMarketPrice();
        handleOutbidStatusChange();
    }

    private void handleOutbidStatusChange(){
        Optional<Statuses> outbidOptional = findOutbidStatus();
        if(outbidOptional.isEmpty()) return;

        Statuses newStatus = outbidOptional.get();
        if(outbidStatus != newStatus){
            outbidStatus = newStatus;
            EVENT_BUS.post(new OutbidOrderEvent(this, newStatus == Statuses.OUTBID));
        }
    }


    public int getIndex() {
        return BUConfig.get().userOrders.indexOf(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(name).append("[").append(getIndex()).append("]");
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

    public double getFlipPrice() {
        updateMarketPrice();
        Double marketPrice = getMarketPrice(priceType);
        Double marketOppositePrice = getMarketPrice(priceType.getOpposite());
        if (marketPrice == 0) {
            return 0;
        }
        if (getPriceType() == PriceType.INSTABUY) {
            return (marketOppositePrice + .1);
        } else {
            return (marketOppositePrice - .1);
        }
    }

    public void setAmountFilled(int amountFilled) {
        this.amountFilled = amountFilled;
        if (this.amountFilled >= volume) {
            setFilled();
        } else {
            this.fillStatus = Statuses.SET;
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