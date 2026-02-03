package com.github.mkram17.bazaarutils.utils.minecraft.gui.container;

import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.utils.minecraft.SlotLookup;
import com.github.mkram17.bazaarutils.utils.minecraft.gui.ScreenManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ContainerQuery {
    private final IntRange slotRange;
    private final Predicate<ItemStack> filter;

    private ContainerQuery(IntRange slotRange, Predicate<ItemStack> filter) {
        this.slotRange = slotRange;
        this.filter = filter;
    }

    public static ContainerQuery at(int slotNumber) {
        return new ContainerQuery(
                IntRange.exactly(slotNumber),
                item -> true
        );
    }

    public static ContainerQuery range(int minInclusive, int maxInclusive) {
        return new ContainerQuery(
                IntRange.between(minInclusive, maxInclusive),
                item -> true
        );
    }

    public ContainerQuery itemType(Item... wanted) {
        return new ContainerQuery(slotRange, filter.and(itemStack -> {
            Item item = itemStack.getItem();

            for (Item type : wanted) {
                if (item == type) {
                    return true;
                }
            }

            return false;
        }));
    }

    public ContainerQuery withCustomName(String... allowed) {
        return new ContainerQuery(slotRange, filter.and(item -> {
            Text data = item.get(DataComponentTypes.CUSTOM_NAME);

            if (data != null) {
                for (String name : allowed) {
                    if (data.getString().contains(name)) {
                        return true;
                    }
                }
            }

            return false;
        }));
    }

    public ContainerQuery withLore(String lore) {
        return new ContainerQuery(slotRange, filter.and(item -> {
            LoreComponent data = item.get(DataComponentTypes.LORE);

            return data != null && !Util.findComponentsSpanningMatch(data.lines(), lore).isEmpty();
        }));
    }

    public Optional<ItemStack> first(Inventory inventory) {
        int invSize = inventory.size();

        int min = Math.max(0, slotRange.getMin().orElse(0));
        int max = Math.min(invSize - 1, slotRange.getMax().orElse(invSize - 1));

        for (int i = min; i <= max; i++) {
            ItemStack stack = SlotLookup.getInventoryItem(inventory, i);

            if (!stack.isEmpty() && filter.test(stack)) {
                return Optional.of(stack);
            }
        }

        return Optional.empty();
    }

    public Optional<ItemStack> first() {
        Optional<Inventory> inventory = ScreenManager.getScreenContainer();

        if (inventory.isEmpty()) {
            return Optional.empty();
        }

        return first(inventory.get());
    }

    public List<ItemStack> all(Inventory inventory) {
        int invSize = inventory.size();

        int min = Math.max(0, slotRange.getMin().orElse(0));
        int max = Math.min(invSize - 1, slotRange.getMax().orElse(invSize - 1));

        List<ItemStack> out = new ArrayList<>();

        for (int i = min; i <= max; i++) {
            ItemStack stack = SlotLookup.getInventoryItem(inventory, i);

            if (!stack.isEmpty() && filter.test(stack)) {
                out.add(stack);
            }
        }

        return out;
    }

    public List<ItemStack> all() {
        Optional<Inventory> inventory = ScreenManager.getScreenContainer();

        if (inventory.isEmpty()) {
            return new ArrayList<>();
        }

        return all(inventory.get());
    }
}
