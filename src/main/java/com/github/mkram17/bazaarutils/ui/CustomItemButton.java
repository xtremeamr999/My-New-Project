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

public interface CustomItemButton {
    RegistryEntry<SoundEvent> BUTTON_SOUND = SoundEvents.UI_BUTTON_CLICK;
    float BUTTON_VOLUME = 0.2f;

    int getSlotNumber();

    ItemStack getReplacementItem();

    default boolean shouldReplaceItem(ReplaceItemEvent event) {
        return event.getSlotId() == getSlotNumber();
    }

    default boolean wasButtonSlotClicked(SlotClickEvent event) {
        return event.getSlotId() == getSlotNumber();
    }
}
