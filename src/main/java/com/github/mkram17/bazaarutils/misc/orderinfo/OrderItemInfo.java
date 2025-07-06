package com.github.mkram17.bazaarutils.misc.orderinfo;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;

//has info for an orders position in orders screen, etc
public class OrderItemInfo {
    @Getter @Setter
    private int slotIndex;
    @Getter @Setter
    private ItemStack itemStack;

    public OrderItemInfo(int slotIndex, ItemStack itemStack) {
        this.slotIndex = slotIndex;
        this.itemStack = itemStack;
    }
}
