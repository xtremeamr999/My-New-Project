package com.github.mkram17.bazaarutils.misc.orderinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.item.ItemStack;

//has info for an orders position in orders screen, etc
@AllArgsConstructor @NoArgsConstructor
public class OrderItemInfo {
    @Getter @Setter
    private Integer slotIndex;
    @Getter @Setter
    private ItemStack itemStack;
}
