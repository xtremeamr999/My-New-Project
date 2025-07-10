package com.github.mkram17.bazaarutils.misc.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class TextDisplayWidget extends ClickableWidget {
    private final Text text;

    public TextDisplayWidget(int x, int y, int width, int height, Text text) {
        super(x, y, width, height, text);
        this.text = text;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(text);
        int textX = this.getX() + (this.width - textWidth) / 2;
        int textY = this.getY() + (this.height - textRenderer.fontHeight) / 2;
        context.drawText(textRenderer, text, textX, textY, 0xFFFFFF, false);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}