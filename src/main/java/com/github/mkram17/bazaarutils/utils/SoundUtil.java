package com.github.mkram17.bazaarutils.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.concurrent.CompletableFuture;

public class SoundUtil {
    public static void playSound(SoundEvent sound, float volume) {
        MinecraftClient client = MinecraftClient.getInstance();
        var player = client.player;

        if (client.world == null || player == null || client.getSoundManager() == null) {
            Util.logError("Failed to play sound due to null value", new Throwable());
//            Util.tickExecuteLater(40, () -> playSound(sound, volume));
            return;
        }


        PositionedSoundInstance soundInstance = PositionedSoundInstance.ambient(sound, 1f, volume);

        client.getSoundManager().play(soundInstance);
    }
    public static void playSound(RegistryEntry<SoundEvent> soundEntry, float volume) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getSoundManager() == null || client.world == null) {
            Util.logError("Failed to play sound due to null value", new Throwable());
            Util.tickExecuteLater(20, () -> playSound(soundEntry, volume));
            return;
        }

        PositionedSoundInstance soundInstance = PositionedSoundInstance.ambient(soundEntry.value(), 1f, volume);

        client.getSoundManager().play(soundInstance);
    }

    public static void notifyMultipleTimes(int notifyNum){
        CompletableFuture.runAsync(() ->{
            for(int i = 0; i < notifyNum; i++) {
                Util.tickExecuteLater(1, () -> SoundUtil.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, .5f));
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        }
}
