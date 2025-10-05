package com.github.mkram17.bazaarutils.features.customorder;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomOrdersMenu extends BaseOwoScreen<FlowLayout> {
    private static final float BACKGROUND_BLUR_QUALITY = 10f;
    private static final float BACKGROUND_BLUR_SIZE = 10f;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.blur(BACKGROUND_BLUR_QUALITY, BACKGROUND_BLUR_SIZE))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        rootComponent.child(getParentComponent());
    }

    private static ParentComponent getParentComponent(){
        var customOrders = BUConfig.get().customOrders;
        if(customOrders.size() > 15)
            return Containers.verticalScroll(Sizing.content(), Sizing.fill(80), generateOrderButtonsContainer())
                    .padding(Insets.of(8))
                    .surface(Surface.DARK_PANEL)
                    .verticalAlignment(VerticalAlignment.CENTER)
                    .horizontalAlignment(HorizontalAlignment.CENTER);
        return generateOrderButtonsContainer()
                .padding(Insets.of(8))
                .surface(Surface.DARK_PANEL)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);
    }

    private static FlowLayout generateOrderButtonsContainer() {
        var customOrders = BUConfig.get().customOrders;
        var verticalFlow = Containers.verticalFlow(Sizing.content(), Sizing.content());
        for(CustomOrder order : customOrders) {
            verticalFlow.child(generateOrderButton(order));
        }
        return verticalFlow;
    }

    private static FlowLayout generateOrderButton(CustomOrder order) {
        var customOrders = BUConfig.get().customOrders;
        var horizontalFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        horizontalFlow.child(
                Components.button(
                        Text.literal(order.getOrderAmount() + "x, Slot #" + (order.getSlotNumber()+1)),
                        button -> PlayerActionUtil.notifyAll("Custom Order Button Click: " + customOrders.indexOf(order))
                ).margins(Insets.of(3,3,3,1))
        ).child(
                Components.button(
                        Text.literal("\uD83D\uDDD1"),
                        button -> {
                            customOrders.remove(order);
                            MinecraftClient.getInstance().setScreen(new CustomOrdersMenu());
                        }).margins(Insets.of(3,3,1,3))
                );
        return horizontalFlow;
    }
}