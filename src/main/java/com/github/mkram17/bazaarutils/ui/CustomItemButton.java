package com.github.mkram17.bazaarutils.ui;

import com.github.mkram17.bazaarutils.events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.events.listener.BUListener;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class CustomItemButton extends BUListener {
    //TODO make flip helper and custom order use this instead of their own settings variables when possible
    @Getter @ConfigEntry(id = "slotNumber")
    protected int slotNumber;
    @Getter @Setter
    protected transient ItemStack replacementItem;
    protected static final RegistryEntry<SoundEvent> BUTTON_SOUND = SoundEvents.UI_BUTTON_CLICK;
    protected static final float BUTTON_VOLUME = .2f;

    protected boolean shouldReplaceItem(ReplaceItemEvent event) {
        return event.getSlotId() == slotNumber;
    }

    protected boolean wasButtonSlotClicked(SlotClickEvent event) {
        return (event.slotId == slotNumber);
    }
}
