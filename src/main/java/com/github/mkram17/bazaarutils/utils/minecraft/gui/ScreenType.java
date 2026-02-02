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

    final class Builder {
        private final List<ScreenPredicate> chain;

        private static List<ScreenPredicate> concat(List<ScreenPredicate> list, ScreenPredicate next) {
            List<ScreenPredicate> copy = new ArrayList<>(list.size() + 1);

            copy.addAll(list);
            copy.add(next);

            return List.copyOf(copy);
        }

        private Builder(List<ScreenPredicate> chain) {
            this.chain = List.copyOf(chain);
        }

        public Builder() {
            this(List.of());
        }

        public Builder genericContainer() {
            return new Builder(concat(chain, new ScreenPredicate("GenericContainer", screen -> screen instanceof GenericContainerScreen)));
        }

        public Builder containerTitle(String fragment) {
            return new Builder(concat(chain, new ScreenPredicate("TitleContains[" + fragment + "]", screen -> {
                Text text = screen.getTitle();

                return text != null && (Util.removeFormatting(text.getString())).contains(fragment);
            })));
        }

        public Builder containerItem(NumberRange.IntRange slotRange, Item... wanted) {
            String desc = "ItemInSlots[" + slotRange.getMin().orElse(0) + '-' +
                    slotRange.getMax().orElse(54) + "] " +
                    java.util.Arrays.toString(wanted);

            return new Builder(concat(chain, new ScreenPredicate(desc, screen -> ContainerQuery
                    .range(
                            slotRange.getMin().orElse(0),
                            slotRange
                                    .getMax()
                                    .orElse(ContainerManager.getLowerChestInventory().size() - 1)
                    )
                    .itemType(wanted)
                    .first()
                    .isPresent())
            ));
        }

        public Builder containerItem(int slot, Item... wanted) {
            return containerItem(NumberRange.IntRange.exactly(slot), wanted);
        }

        public Builder containerQuery(ContainerQuery query) {
            return new Builder(concat(chain, new ScreenPredicate("ContainerQuery", screen -> query.first().isPresent())));
        }

        public Builder containerQuery(Function<Inventory, ContainerQuery> builder) {
            return new Builder(concat(chain, new ScreenPredicate("ContainerQueryFn", screen -> builder.apply(ContainerManager.getLowerChestInventory()).first().isPresent())));
        }

        public Builder custom(String name, Predicate<Screen> test) {
            return new Builder(concat(chain, new ScreenPredicate(name, test)));
        }

        public Builder custom(Predicate<Screen> test) {
            return custom("Custom", test);
        }

        public ScreenType build() {
            return new ScreenType() {
                @Override
                public String asString() {
                    return chain.isEmpty()
                            ? "ScreenType{always false}"
                            : "ScreenType{" +
                            chain.stream()
                                    .map(ScreenPredicate::name)
                                    .reduce((a, b) -> a + " && " + b)
                                    .orElse("") +
                            '}';
                }

                @Override
                public boolean test(Screen screen) {
                    return chain.stream().allMatch(predicate -> predicate.test(screen));
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
