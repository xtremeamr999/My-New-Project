package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.utils.Util;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class PriceCharts implements ItemTooltipCallback, BUListener {
    private String productID;

    @Override
    public void getTooltip(ItemStack stack, Item.TooltipContext tooltip, TooltipType tooltipType, List<Text> lines) {
        if (stack == null || stack.isEmpty() || !BazaarUtils.gui.inBazaar() || stack.getItem().getName().getString().contains("GLASS_PANE")) {
            return;
        }

        String itemName = stack.getName().getString();
        productID = BazaarData.findProductId(itemName);
        if(productID == null) {
            if(stack.getComponentChanges() != null && stack.getComponentChanges().get(DataComponentTypes.CUSTOM_DATA) != null) {
                productID = stack.getComponentChanges().get(DataComponentTypes.CUSTOM_DATA).toString();
                productID = productID.substring(productID.indexOf("\"")+1, productID.lastIndexOf("\""));
                if(productID.contains(itemName.toUpperCase()) || (itemName.contains(" ") && productID.contains(itemName.substring(0, itemName.indexOf(" ")).toUpperCase())) || (itemName.contains("-") && productID.contains(itemName.substring(0, itemName.indexOf("-")).toUpperCase()))) {
                    Util.notifyAll("New product id detected: " + productID + "name: " + itemName, Util.notificationTypes.BAZAARDATA);
                }
            }
            return;
        }
        MutableText text = Text.literal("CTRL+SHIFT click for price charts & other info")
                .formatted(Formatting.GOLD)
                .formatted(Formatting.BOLD);

        MutableText poweredBy = Text.literal("Powered by skyblock.finance")
                .formatted(Formatting.GRAY);


        lines.add(text);
        lines.add(poweredBy);
    }

    @EventHandler
    private void onClick(SlotClickEvent e){
        if(productID == null || e.isCancelled() || !BazaarUtils.gui.inBazaar() || !(Screen.hasShiftDown() && Screen.hasControlDown()))
            return;

        String link = "https://skyblock.finance/items/"+productID;
        MinecraftClient.getInstance().setScreen(new ConfirmLinkScreen((confirmed) -> {
            if (confirmed) {
                try {
                    net.minecraft.util.Util.getOperatingSystem().open(new URI(link));
                } catch (URISyntaxException exception) {
                    throw new RuntimeException(exception);
                }
            }
            MinecraftClient.getInstance().setScreen(null);
        }, link, true));
        e.cancel();
    }

    @Override
    public void subscribe() {
        ItemTooltipCallback.EVENT.register(this);
        BazaarUtils.eventBus.subscribe(this);
    }
}