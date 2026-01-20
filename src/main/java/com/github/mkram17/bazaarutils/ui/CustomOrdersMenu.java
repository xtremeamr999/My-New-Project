package com.github.mkram17.bazaarutils.ui;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.features.customorder.CustomOrder;
import com.github.mkram17.bazaarutils.features.customorder.OrderToAdd;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class CustomOrdersMenu extends BaseOwoScreen<FlowLayout> {
    private static final float BACKGROUND_BLUR_QUALITY = 10f;
    private static final float BACKGROUND_BLUR_SIZE = 10f;

    public OrderToAdd orderToAdd = new OrderToAdd();

    //TODO make this work depending on screen size so it doesnt only work for me
    private static final int MAXIMUM_ORDERS_BEFORE_SCROLL = 8;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.blur(BACKGROUND_BLUR_QUALITY, BACKGROUND_BLUR_SIZE))
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        var grid = Containers.grid(Sizing.fill(100), Sizing.fill(100), 1, 3);
        grid.horizontalAlignment(HorizontalAlignment.CENTER);
        grid.padding(Insets.of(15));
        // Top-left: New Order (absolute)
        grid.child(
                generateNewOrderParent()
                        .verticalAlignment(VerticalAlignment.CENTER),
                0,
                0
        );

        // Center: Orders (wrap in full-size container that centers its child)
        FlowLayout centered = Containers.verticalFlow(Sizing.content(), Sizing.content());
        centered.horizontalAlignment(HorizontalAlignment.CENTER);
        centered.child(generateOrderButtonsParent());

        grid.child(centered, 0, 1);

        rootComponent.child(grid);
    }

    private ParentComponent generateOrderButtonsParent() {
        var customOrders = BUConfig.get().customOrders;
        ParentComponent parent;
        if (customOrders.size() > MAXIMUM_ORDERS_BEFORE_SCROLL) {
            parent = Containers.verticalScroll(Sizing.content(), Sizing.fill(80), generateOrderButtonsContainer());
        } else {
            parent = generateOrderButtonsContainer();
        }
        return parent
                .padding(Insets.of(8))
                .surface(Surface.DARK_PANEL);
    }

    private FlowLayout generateOrderButtonsContainer() {
        var customOrders = BUConfig.get().customOrders;
        var verticalFlow = Containers.verticalFlow(Sizing.content(), Sizing.content());

        for(CustomOrder order : customOrders) {
            verticalFlow.child(generateOrderButton(order));
        }
        return verticalFlow;
    }
    private ParentComponent generateNewOrderParent() {
        var horizontalFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        horizontalFlow.child(Components.label(Text.literal("New Order").formatted(Formatting.BOLD)).margins(Insets.of(10)));
        horizontalFlow.child(Components.label(Text.literal("Amount:")));
        horizontalFlow.child(addOrderAmountTextBox());
        horizontalFlow.child(chooseSlotButton());
        horizontalFlow.child(addOrderButton());

        return horizontalFlow
                .padding(Insets.of(8))
                .surface(Surface.DARK_PANEL);
    }

    private Component addOrderAmountTextBox() {
        TextBoxComponent orderAmountBox = new NumTextBox(Sizing.fixed(50));
        TextBoxComponent.OnChanged updateOrderAmount = orderAmount -> orderToAdd.setOrderAmount(orderAmount.isEmpty() ? null : Integer.parseInt(orderAmount));
        orderAmountBox.onChanged().subscribe(updateOrderAmount);
        return orderAmountBox;
    }
    private Component chooseSlotButton() {
        return Components.button(
                        Text.literal("Choose Slot"),
                        button -> {
                            if(orderToAdd.getOrderAmount() == null){
                                PlayerActionUtil.notifyAll("Please enter a valid order amount first.");
                                return;
                            }
                            var pickSlotMenu = new PickSlotMenu(this, orderToAdd.getOrderAmount(), (pickedSlot) -> orderToAdd.setSlotNumber(pickedSlot));
                            MinecraftClient.getInstance().setScreen(pickSlotMenu);
                        });
    }

    private Component addOrderButton() {
        return Components.button(
                Text.literal("Add"),
                button -> {
                    if(orderToAdd.getOrderAmount() == null || orderToAdd.getSlotNumber() == null){
                        PlayerActionUtil.notifyAll("Please enter a valid order amount and slot number.");
                        return;
                    }
                    CustomOrder newOrder = new CustomOrder(orderToAdd.isEnabled(), orderToAdd.getOrderAmount(), orderToAdd.getSlotNumber());
                    BUConfig.get().customOrders.add(newOrder);
                    MinecraftClient.getInstance().setScreen(new CustomOrdersMenu());
                    BUConfig.scheduleConfigSave();
                })
                    .margins(Insets.of(3));
    }

    private FlowLayout generateOrderButton(CustomOrder order) {
        var horizontalFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        String garbageCanEmoji = "\uD83D\uDDD1";

        horizontalFlow.child(
                Components.label(
                        Text.literal(order.getOrderAmount() + "x, Slot #" + (order.getSlotNumber()+1))
                ).margins(Insets.of(3,3,3,1))
        ).child(
                Components.button(
                        Text.literal(garbageCanEmoji),
                        button -> {
                            order.removeFromConfig();
                            MinecraftClient.getInstance().setScreen(new CustomOrdersMenu());
                        }).margins(Insets.of(3,3,1,3))
                );
        return horizontalFlow;
    }
}