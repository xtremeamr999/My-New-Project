package com.github.mkram17.bazaarutils.features.customorder.management;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.features.customorder.CustomOrder;
import com.github.mkram17.bazaarutils.utils.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.function.IntConsumer;

public class PickSlotMenu extends Screen {
    private static final int COLUMNS = 9;
    private static final int SLOT_SIZE = 18;     // outer grid size (vanilla spacing)
    private static final int SLOT_INNER = 16;
    private static final int BAZAAR_BUY_MENU_ROWS = 4;// inner square we draw/highlight
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    private static final int TOTAL_SLOTS = BAZAAR_BUY_MENU_ROWS * COLUMNS;

    private final ItemStack[] slotItems = new ItemStack[TOTAL_SLOTS];

    private int panelWidth;
    private int panelHeight;
    private int left;
    private int top;

    private final int orderAmount;
    private final IntConsumer onPick;

    public PickSlotMenu(int orderAmount, IntConsumer onPick) {
        super(Text.literal("Pick a slot"));
        this.orderAmount = Math.max(1, orderAmount);
        this.onPick = onPick;
        initializeSlots();
        recalcPanel();
    }

    private void initializeSlots(){
        Arrays.fill(slotItems, ItemStack.EMPTY);

        setSlotItem(10, Items.DIAMOND.getDefaultStack());
        setSlotItem(12, Items.CHEST.getDefaultStack());
        setSlotItem(14, Items.CHEST.getDefaultStack());
        setSlotItem(16, Items.OAK_SIGN.getDefaultStack());
        setSlotItem(31, Items.ARROW.getDefaultStack());

        for(CustomOrder order : BUConfig.get().customOrders){
            setSlotItem(order.getSlotNumber(), order.getItem().getDefaultStack());
        }
    }

    public void setSlotItem(int index, ItemStack stack) {
        if (index >= 0 && index < TOTAL_SLOTS){
            this.slotItems[index] = stack == null ? ItemStack.EMPTY : stack;
        }
    }

    private void recalcPanel() {
        // Simple panel sized to the grid with margins
        int margin = 8;
        this.panelWidth = margin + COLUMNS * SLOT_SIZE + margin;
        this.panelHeight = margin + BAZAAR_BUY_MENU_ROWS * SLOT_SIZE + margin + 14; // + title
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
        super.render(ctx, mouseX, mouseY, delta);

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
        for (int r = 0; r < BAZAAR_BUY_MENU_ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                int x = gridX + c * SLOT_SIZE;
                int y = gridY + r * SLOT_SIZE;

                ctx.drawGuiTexture(RenderLayer::getGuiTextured, SLOT_TEXTURE, x, y, 18, 18);

                ItemStack stack = slotItems[r * COLUMNS + c];
                if(!stack.isEmpty()){
                    ctx.drawItem(stack, x + 1, y + 1);
                }
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
        if (c >= COLUMNS || r >= BAZAAR_BUY_MENU_ROWS) return -1;

        // Ensure inside inner 16x16 area (like vanilla slot visuals)
        int innerX = relX % SLOT_SIZE;
        int innerY = relY % SLOT_SIZE;
        if (innerX >= SLOT_INNER || innerY >= SLOT_INNER) return -1;

        return r * COLUMNS + c;
    }
}
