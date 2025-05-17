package com.github.mkram17.bazaarutils;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUSerializedListener;
import com.github.mkram17.bazaarutils.events.BUTransientListener;
import com.github.mkram17.bazaarutils.features.StashHelper;
import com.github.mkram17.bazaarutils.misc.ModCompatibilityHelper;
import com.github.mkram17.bazaarutils.utils.Commands;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.mojang.serialization.Codec;
import de.siphalor.amecs.api.AmecsKeyBinding;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BazaarUtils implements ClientModInitializer {
    public static IEventBus eventBus = new EventBus();
    public static GUIUtils gui = new GUIUtils();
    public static StashHelper stashHelper;
    public static ArrayList<AmecsKeyBinding> keybinds = new ArrayList<>();
    public static final String MODID = "bazaarutils";
    public static final String VERSION = new Properties().getProperty("mod_version");

    @Override
    public void onInitializeClient() {
        registerEvents();
        BUConfig.HANDLER.load();
        serializedSubscribeEvents();
        registerCommands();
        registerKeybinds();

        ModCompatibilityHelper.initializePatches();
    }

    private void registerEvents() {
        eventBus.registerLambdaFactory("com.github.mkram17.bazaarutils", (lookupInMethod, klass) ->
                (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        transientSubscribeEvents();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            Commands.register(dispatcher);
        });
    }
    private void transientSubscribeEvents(){
        BUTransientListener.addTransientEvents();
        List<BUTransientListener> serializedListeners = BUTransientListener.getTransientEvents();

        for(BUTransientListener listener : serializedListeners) {
            listener.subscribe();
        }
    }
    //must be run after config load
    private void serializedSubscribeEvents(){
        List<BUSerializedListener> serializedListeners = BUConfig.get().getSerializedEvents();

        for(BUSerializedListener listener : serializedListeners) {
            listener.registerEvents();
        }
    }

    private void registerKeybinds(){
        stashHelper = new StashHelper();
        stashHelper.registerTickCounter();
        keybinds.add(stashHelper);

        for(AmecsKeyBinding keybind : keybinds) {
            KeyBindingHelper.registerKeyBinding(keybind);
        }
    }

    public static final ComponentType<String> CUSTOM_SIZE_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(BazaarUtils.MODID, "custom_size"),
            ComponentType.<String>builder().codec(Codec.STRING).build()
    );
}
