package com.github.mkram17.bazaarutils.utils.minecraft.gui;

import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.utils.minecraft.gui.container.ContainerManager;
import com.github.mkram17.bazaarutils.utils.minecraft.gui.container.ContainerQuery;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.predicate.NumberRange;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ScreenType extends Predicate<Screen> {
    String asString();
    String shortName();

    final class Builder {
        private final List<ScreenPredicate> chain;
        private final String name;

        private static List<ScreenPredicate> concat(List<ScreenPredicate> list, ScreenPredicate next) {
            List<ScreenPredicate> copy = new ArrayList<>(list.size() + 1);
            copy.addAll(list);
            copy.add(next);
            return List.copyOf(copy);
        }

        private Builder(List<ScreenPredicate> chain, String name) {
            this.chain = List.copyOf(chain);
            this.name  = name;
        }

        public Builder() {
            this(List.of(), null);
        }

        public Builder name(String label) {
            return new Builder(chain, label);
        }

        public Builder genericContainer() {
            return new Builder(concat(chain, new ScreenPredicate("GenericContainer",
                    screen -> screen instanceof GenericContainerScreen)), name);
        }

        public Builder containerTitle(String fragment) {
            return new Builder(concat(chain, new ScreenPredicate("Title[" + fragment + "]", screen -> {
                Text text = screen.getTitle();
                return text != null && Util.removeFormatting(text.getString()).contains(fragment);
            })), name);
        }

        public Builder containerItem(NumberRange.IntRange slotRange, Item... wanted) {
            String desc = "Item[slots=" + slotRange.getMin().orElse(0) + ".." +
                    slotRange.getMax().orElse(54) + ", types=" +
                    java.util.Arrays.toString(wanted) + "]";

            return new Builder(concat(chain, new ScreenPredicate(desc, screen -> ContainerQuery
                    .range(
                            slotRange.getMin().orElse(0),
                            slotRange.getMax().orElse(ContainerManager.getLowerChestInventory().size() - 1)
                    )
                    .itemType(wanted)
                    .first()
                    .isPresent())), name);
        }

        public Builder containerItem(int slot, Item... wanted) {
            return containerItem(NumberRange.IntRange.exactly(slot), wanted);
        }

        public Builder containerQuery(ContainerQuery query) {
            return new Builder(concat(chain, new ScreenPredicate(
                    "Query[" + query.describe() + "]",
                    screen -> query.first().isPresent())), name);
        }

        public Builder containerQuery(Function<Inventory, ContainerQuery> builder) {
            return containerQuery("fn", builder);
        }

        public Builder containerQuery(String label, Function<Inventory, ContainerQuery> builder) {
            return new Builder(concat(chain, new ScreenPredicate(
                    "Query[" + label + "]",
                    screen -> builder.apply(ContainerManager.getLowerChestInventory()).first().isPresent())), name);
        }

        public Builder custom(String label, Predicate<Screen> test) {
            return new Builder(concat(chain, new ScreenPredicate(label, test)), name);
        }

        public Builder custom(Predicate<Screen> test) {
            return custom("Custom", test);
        }

        public ScreenType build() {
            String chainDesc = chain.isEmpty()
                    ? "always false"
                    : chain.stream()
                    .map(ScreenPredicate::name)
                    .reduce((a, b) -> a + " && " + b)
                    .orElse("");

            String label = name != null
                    ? name + " (" + chainDesc + ")"
                    : chainDesc;

            return new ScreenType() {
                @Override
                public String asString() {
                    return label;
                }

                public String shortName() {
                    return name != null ? name : label;
                }

                @Override
                public boolean test(Screen screen) {
                    return chain.stream().allMatch(predicate -> predicate.test(screen));
                }

                @Override
                public String toString() {
                    return asString();
                }
            };
        }

        private record ScreenPredicate(String name, Predicate<Screen> predicate) implements Predicate<Screen> {
            @Override
            public boolean test(Screen screen) {
                return predicate.test(screen);
            }
        }
    }
}