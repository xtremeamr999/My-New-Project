package com.github.mkram17.bazaarutils.ui;

import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class BetterDropdownComponent extends DropdownComponent {
    private final boolean singleChoiceDropdown;
    protected BetterDropdownComponent(Sizing horizontalSizing, boolean singleChoiceDropdown) {
        super(horizontalSizing);
        this.singleChoiceDropdown = singleChoiceDropdown;
    }

    @Override
    public DropdownComponent checkbox(Text text, boolean state, Consumer<Boolean> onClick) {
        Consumer<Boolean> onClickAndResetOthers = onClick.andThen(enabled -> {
            if(enabled && singleChoiceDropdown) {
                for(var entry : entries.children()){
                    if(entry instanceof OptionBox optionBox) {
                        optionBox.setState(false);
                    }
                }
            }
        });

        this.entries.child(new OptionBox(this, text, state, onClickAndResetOthers).margins(Insets.of(2)));
        return this;
    }

    protected static class OptionBox extends Checkbox {
        public OptionBox(DropdownComponent parentDropdown, Text text, boolean state, Consumer<Boolean> onClick) {
            super(parentDropdown, text, state, onClick);
        }

        public void setState(boolean state) {
            this.state = state;
        }
    }
}
