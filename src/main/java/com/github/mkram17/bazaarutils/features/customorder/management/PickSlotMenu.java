package com.github.mkram17.bazaarutils.features.customorder.management;

import com.github.mkram17.bazaarutils.utils.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.IntConsumer;

public class PickSlotMenu extends Screen {
    private static final int COLUMNS = 9;
    private static final int SLOT_SIZE = 18;     // outer grid size (vanilla spacing)
    private static final int SLOT_INNER = 16;    // inner square we draw/highlight

    private int rows = 4;                        // default to 4 rows (36 slots)
    private int panelWidth;
    private int panelHeight;
    private int left;
    private int top;

    private int orderAmount = 1;
    private IntConsumer onPick;

    public PickSlotMenu(int orderAmount, int rows, IntConsumer onPick) {
        super(Text.literal("Pick a slot"));
        this.orderAmount = Math.max(1, orderAmount);
        this.rows = Math.max(1, Math.min(6, rows)); // clamp 1..6
        this.onPick = onPick;
        recalcPanel();
    }

    private void recalcPanel() {
        // Simple panel sized to the grid with margins
        int margin = 8;
        this.panelWidth = margin + COLUMNS * SLOT_SIZE + margin;
        this.panelHeight = margin + rows * SLOT_SIZE + margin + 14; // + title
        if (this.width > 0 && this.height > 0) {
            this.left = (this.width - panelWidth) / 2;
            this.top = (this.height - panelHeight) / 2;
        }
    }

    @Override
    protected void init() {
        super.init();
        recalcPanel();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        recalcPanel();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        // Panel background
        int bg = 0xC0101010; // semi-transparent dark
        int border = 0xFF3F3F3F;
        ctx.fill(left, top, left + panelWidth, top + panelHeight, bg);
        ctx.drawBorder(left, top, panelWidth, panelHeight, border);

        // Title
        String titleStr = "Pick slot for " + orderAmount;
        int titleX = left + (panelWidth - textRenderer.getWidth(titleStr)) / 2;
        int titleY = top + 6;
        ctx.drawText(textRenderer, titleStr, titleX, titleY, 0xFFE0E0E0, true);

        int gridX = left + 8;
        int gridY = top + 8 + 14; // leave space for title

        // Draw grid
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                int x = gridX + c * SLOT_SIZE;
                int y = gridY + r * SLOT_SIZE;

                // inner slot box
                int innerX = x;
                int innerY = y;
                int innerX2 = x + SLOT_INNER;
                int innerY2 = y + SLOT_INNER;

                // slot background and border
                ctx.fill(innerX, innerY, innerX2, innerY2, 0xFF202020);
                ctx.drawBorder(innerX, innerY, SLOT_INNER, SLOT_INNER, 0xFF505050);

                // draw 1-based number centered
                int slotIndex = r * COLUMNS + c;
                String num = Integer.toString(slotIndex + 1);
                int tx = innerX + (SLOT_INNER - textRenderer.getWidth(num)) / 2;
                int ty = innerY + (SLOT_INNER - textRenderer.fontHeight) / 2;
                ctx.drawText(textRenderer, num, tx, ty, 0xFFB0B0B0, false);
            }
        }

        // Hover highlight
        int hovered = getSlotAt(mouseX, mouseY);
        if (hovered >= 0) {
            int r = hovered / COLUMNS;
            int c = hovered % COLUMNS;

            int x = gridX + c * SLOT_SIZE;
            int y = gridY + r * SLOT_SIZE;

            ctx.fill(x, y, x + SLOT_INNER, y + SLOT_INNER, 0x66FFFFFF);
            ctx.drawTooltip(textRenderer, Text.literal("Slot #" + (hovered + 1)), mouseX, mouseY);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int slot = getSlotAt((int) mouseX, (int) mouseY);
        if (slot >= 0) {
            if (onPick != null) {
                try {
                    onPick.accept(slot); // 0-based
                } catch (Exception e) {
                    Util.notifyError("Error handling slot selection", e);
                }
            }
            // Close back to previous screen (don’t send container packets)
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }
        // Clicking outside closes
        if (button == 0) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getSlotAt(int mouseX, int mouseY) {
        int gridX = left + 8;
        int gridY = top + 8 + 14;

        int relX = mouseX - gridX;
        int relY = mouseY - gridY;
        if (relX < 0 || relY < 0) return -1;

        int c = relX / SLOT_SIZE;
        int r = relY / SLOT_SIZE;
        if (c < 0 || c >= COLUMNS || r < 0 || r >= rows) return -1;

        // Ensure inside inner 16x16 area (like vanilla slot visuals)
        int innerX = relX % SLOT_SIZE;
        int innerY = relY % SLOT_SIZE;
        if (innerX >= SLOT_INNER || innerY >= SLOT_INNER) return -1;

        return r * COLUMNS + c;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
