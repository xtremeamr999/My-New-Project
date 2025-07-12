package com.github.mkram17.bazaarutils.misc.orderinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.item.ItemStack;

//has info for an orders position in orders screen, etc
public record OrderItemInfo(Integer slotIndex, ItemStack itemStack) {
}
