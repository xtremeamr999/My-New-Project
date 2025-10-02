package com.github.mkram17.bazaarutils.features.customorder;

import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class CustomOrdersMenu extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.blur(.8f, 1f))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.child(
                Components.button(
                        Text.literal("A Custom Order Button"),
                        button -> PlayerActionUtil.notifyAll("Custom Order Button Click")
                )
        );
    }
}