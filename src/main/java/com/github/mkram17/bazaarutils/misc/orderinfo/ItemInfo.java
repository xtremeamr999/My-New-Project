package com.github.mkram17.bazaarutils.misc.orderinfo;

import net.minecraft.item.ItemStack;

//has info for an orders position in orders screen, etc
public record ItemInfo(Integer slotIndex, ItemStack itemStack) {
}
