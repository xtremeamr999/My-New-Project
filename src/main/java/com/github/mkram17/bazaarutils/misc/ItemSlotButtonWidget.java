package com.github.mkram17.bazaarutils.misc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemSlotButtonWidget extends TexturedButtonWidget {
    private final ItemStack itemStack;

    public ItemSlotButtonWidget(int x, int y, int width, int height, ButtonTextures textures, PressAction onPress, ItemStack itemStack, Text tooltip) {
        super(x, y, width, height, textures, onPress, Text.empty());
        this.itemStack = itemStack;
        this.setTooltip(Tooltip.of(tooltip));
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        if (this.itemStack != null && !this.itemStack.isEmpty()) {
            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

            int itemX = this.getX() + (this.width - 16) / 2;
            int itemY = this.getY() + (this.height - 16) / 2;

            context.drawItem(this.itemStack, itemX, itemY);
        }
    }
}