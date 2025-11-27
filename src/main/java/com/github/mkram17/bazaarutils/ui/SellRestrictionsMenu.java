package com.github.mkram17.bazaarutils.ui;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.features.customorder.CustomOrder;
import com.github.mkram17.bazaarutils.features.restrictsell.RestrictSell;
import com.github.mkram17.bazaarutils.features.restrictsell.RestrictSellControl;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class SellRestrictionsMenu extends BaseOwoScreen<FlowLayout> {
    private static final float BACKGROUND_BLUR_QUALITY = 10f;
    private static final float BACKGROUND_BLUR_SIZE = 10f;

    //TODO make this work depending on screen size so it doesnt only work for me
    private static final int MAXIMUM_ORDERS_BEFORE_SCROLL = 8;
    private static RestrictSell.restrictBy restrictionTypeToAdd;

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
        var sellRestrictions = BUConfig.get().restrictSell.getControls();
        var verticalFlow = Containers.verticalFlow(Sizing.content(), Sizing.content());

        for(RestrictSellControl control : sellRestrictions) {
            verticalFlow.child(generateOrderButton(control));
        }
        return verticalFlow;
    }
    private ParentComponent generateNewOrderParent() {
        var horizontalFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        horizontalFlow.child(Components.label(Text.literal("New Sell Restriction").formatted(Formatting.BOLD)).margins(Insets.of(10)));
        horizontalFlow.child(addRestrictionTypeDropdown());
        horizontalFlow.child(addOrderButton());

        return horizontalFlow
                .padding(Insets.of(8))
                .surface(Surface.DARK_PANEL);
    }

    private Component addRestrictionTypeDropdown() {
        return Components.dropdown(Sizing.content())
                .closeWhenNotHovered(false)
                .button(Text.literal("Choose sell Restriction Type"),
                        button -> restrictionTypeToAdd = RestrictSell.restrictBy.NAME)
                .nested(Text.literal("Submenu"), Sizing.content(), submenu -> {
                    submenu.checkbox(Text.literal("Restrict By Name"),
                                    restrictionTypeToAdd == RestrictSell.restrictBy.NAME,
                                    button -> restrictionTypeToAdd = RestrictSell.restrictBy.NAME);
                    submenu.checkbox(Text.literal("Restrict By Volume"),
                                    restrictionTypeToAdd == RestrictSell.restrictBy.VOLUME,
                                    button -> restrictionTypeToAdd = RestrictSell.restrictBy.VOLUME);
                    submenu.checkbox(Text.literal("Restrict By Price"),
                                    restrictionTypeToAdd == RestrictSell.restrictBy.PRICE,
                                    button -> restrictionTypeToAdd = RestrictSell.restrictBy.PRICE);
                })
                .padding(Insets.of(5));
    }

    private Component addOrderButton() {
        return Components.button(
                        Text.literal("Add"),
                        button -> {
                                PlayerActionUtil.notifyAll("Please enter a restriction type and amount/name.");

                            RestrictSellControl newControl = new RestrictSellControl(RestrictSell.restrictBy.NAME, "Example Item");
                            BUConfig.get().restrictSell.addRule(newControl);
                            MinecraftClient.getInstance().setScreen(new SellRestrictionsMenu());
                            BUConfig.scheduleConfigSave();
                        })
                .margins(Insets.of(3));
    }

    private FlowLayout generateOrderButton(RestrictSellControl control) {
        var horizontalFlow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        String garbageCanEmoji = "\uD83D\uDDD1";
        boolean isRestrictedByName = control.getRule() == RestrictSell.restrictBy.NAME;

        horizontalFlow.child(
                Components.label(
                        Text.literal("Restrict Insta Selling: " + control.getRestrictionAsString())
                ).margins(Insets.of(3,3,3,1))
        ).child(
                Components.button(
                        Text.literal(garbageCanEmoji),
                        button -> {
                            BUConfig.get().restrictSell.getControls().remove(control);
                            MinecraftClient.getInstance().setScreen(new CustomOrdersMenu());
                        }).margins(Insets.of(3,3,1,3))
        );
        return horizontalFlow;
    }
}
