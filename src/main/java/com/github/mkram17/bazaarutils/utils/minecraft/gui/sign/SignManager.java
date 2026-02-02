package com.github.mkram17.bazaarutils.utils.minecraft.gui.sign;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.events.SignOpenEvent;
import com.github.mkram17.bazaarutils.misc.NotificationType;
import com.github.mkram17.bazaarutils.mixin.AccessorSignEditScreen;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.utils.minecraft.gui.ScreenManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;

import java.util.function.Consumer;

public class SignManager {
    public static void runOnNextSignOpen(Consumer<SignOpenEvent> action) {
        BazaarUtils.EVENT_BUS.subscribe(new Object() {
            @EventHandler
            private void onSignOpen(SignOpenEvent event) {
                try {
                    action.accept(event);
                } finally {
                    BazaarUtils.EVENT_BUS.unsubscribe(this);
                }
            }
        });
    }

    public static void closeSign() {
        try {
            PlayerActionUtil.notifyAll("Closing sign", NotificationType.GUI);

            MinecraftClient client = MinecraftClient.getInstance();
            ScreenManager screenManager = ScreenManager.getInstance();

            Screen screen = screenManager.getCurrent();

            if (client.currentScreen instanceof SignEditScreen signEditScreen && signEditScreen != screen) {
                ScreenManager.setCurrentScreen(signEditScreen);
                screen = signEditScreen;
            }

            if (screen != null) {
                client.execute(screen::close);
            } else {
                Util.notifyError("Error closing sign: client and manager was null or not in a sign", new Throwable());
            }
        } catch (Exception e) {
            Util.notifyError("Unknown error while closing sign", e);
        }
    }

    public static void setSignText(String text, boolean closeAfter) {
        setSignTextInternal(text, closeAfter, 5);
    }

    private static void setSignTextInternal(String text, boolean closeAfter, int attemptsLeft) {
        if (attemptsLeft <= 0) {
            Util.notifyError("Failed to set Sign text: max amount of attempts reached.", new Throwable());

            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null) {
            Util.notifyError("Failed to set sign text: MinecraftClient is null.", new Throwable());

            return;
        }

        client.execute(() -> {
            Screen screen = null;

            ScreenManager screenManager = ScreenManager.getInstance();

            screen = screenManager.getCurrent();

            if (client.currentScreen instanceof SignEditScreen signEditScreen && signEditScreen != screen) {


                ScreenManager.setCurrentScreen(signEditScreen);
                screen = signEditScreen;
            }

            if (screen instanceof SignEditScreen) {
                try {
                    AccessorSignEditScreen signScreen = (AccessorSignEditScreen) screen;

                    String[] lines = text.split("\n", 4);
                    int originalRow = signScreen.getCurrentRow();

                    for (int i = 0; i < 4; i++) {
                        String line = i < lines.length ? lines[i] : "";

                        signScreen.setCurrentRow(i);
                        signScreen.callSetCurrentRowMessage(line);
                    }

                    signScreen.setCurrentRow(originalRow);

                    if (closeAfter) {
                        closeSign();
                    }
                } catch (Exception exception) {
                    Util.notifyError("Error executing sign text update", exception);

                    exception.printStackTrace();
                }
            } else {
                // Screen not open yet, schedule a retry
                Util.tickExecuteLater(4, () -> setSignTextInternal(text, closeAfter, attemptsLeft - 1));
            }
        });
    }

}
