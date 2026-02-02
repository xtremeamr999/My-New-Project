package com.github.mkram17.bazaarutils.features.gui.buttons.inputhelper.customorder;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.github.mkram17.bazaarutils.events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.utils.SoundUtil;
import com.github.mkram17.bazaarutils.utils.bazaar.gui.BazaarScreens;
import com.github.mkram17.bazaarutils.events.listener.BUListener;
import com.github.mkram17.bazaarutils.utils.minecraft.ItemButton;
import com.github.mkram17.bazaarutils.utils.minecraft.gui.ScreenManager;
import com.github.mkram17.bazaarutils.utils.minecraft.gui.container.ContainerManager;
import com.github.mkram17.bazaarutils.utils.minecraft.gui.sign.SignManager;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//TODO low priority -- add number formating with commas (NumberFormat class?) for the tooltips to make large numbers easier to read
//TODO find new name for this
@NoArgsConstructor @ConfigObject
public class CustomOrder extends BUListener implements ItemButton  {
    public static final Map<Integer, Item> COLORMAP = new HashMap<>(Map.of(0, Items.PURPLE_STAINED_GLASS_PANE, 1, Items.BLUE_STAINED_GLASS_PANE, 2, Items.ORANGE_STAINED_GLASS_PANE, 3, Items.BROWN_STAINED_GLASS, 4, Items.RED_STAINED_GLASS_PANE));

    @Getter @Setter @ConfigEntry(id = "enabled")
    private boolean enabled;
    @Getter @Setter @ConfigEntry(id = "orderAmount")
    private int orderAmount;
    @Getter @ConfigEntry(id = "item")
    private Item item;

    @Getter
    @ConfigEntry(
            id = "slot",
            translation = "bazaarutils.config.buttons.button.slot.value"
    )
    @Comment(
            value = "The container slot where the button will be registered at",
            translation = "bazaarutils.config.buttons.button.slot.description"
    )
    @ConfigOption.Range(min = 0, max = 35)
    public int slotNumber;

    @Getter
    private transient ItemStack replacementItem;

    public CustomOrder(boolean enabled, int orderAmount, int slotNumber) {
        this.enabled = enabled;
        this.orderAmount = orderAmount;
        this.slotNumber = slotNumber;
        this.item = getNextColoredPane();
        super.subscribe();
    }

    protected CustomOrder(boolean enabled) {
        this.enabled = enabled;
        this.orderAmount = 71680;
        this.slotNumber = 17;
        this.item = Items.PURPLE_STAINED_GLASS_PANE;
    }

    public static Item getNextColoredPane(){
        int size = BUConfig.get().feature.customOrders.size();
        return CustomOrder.COLORMAP.get(size % 5);
    }

    @EventHandler
    public void replaceItemEvent(ReplaceItemEvent event) {
        if (!ScreenManager.getInstance().isCurrent(BazaarScreens.BUY_ORDER_AMOUNT, BazaarScreens.INSTANT_BUY) || !isEnabled()) {
            return;
        }

        if (!shouldReplaceItem(event)) {
            return;
        }

        ItemStack itemStack = new ItemStack(getItem(), 1);
        itemStack.set(BazaarUtils.CUSTOM_SIZE_COMPONENT, String.valueOf(getOrderAmount()));

        itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Buy " + getOrderAmount()).formatted(Formatting.DARK_PURPLE));
        event.setReplacement(itemStack);
    }

    @EventHandler
    public void onSlotClicked(SlotClickEvent event) {
        if (!ScreenManager.getInstance().isCurrent(BazaarScreens.BUY_ORDER_AMOUNT, BazaarScreens.INSTANT_BUY) || !isEnabled()) {
            return;
        }

        if (event.slot.getIndex() != slotNumber) {
            return;
        }

        SoundUtil.playSound(BUTTON_SOUND, BUTTON_VOLUME);

        openSignAndInputText();
    }

    public void openSignAndInputText() {
        int signSlotId = 16;

        ContainerManager.clickSlot(signSlotId, 0);

        SignManager.runOnNextSignOpen(event -> SignManager.setSignText(Integer.toString(getOrderAmount()), true));
    }

    public void removeFromConfig(){
        if (BUConfig.get().feature.customOrders.contains(this)) {
            BUConfig.get().feature.customOrders.remove(this);
            ConfigUtil.scheduleConfigSave();
            EVENT_BUS.unsubscribe(this);
        }
    }
}