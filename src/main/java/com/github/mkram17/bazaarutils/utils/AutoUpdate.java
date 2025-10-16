package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.moulberry.mixinconstraints.annotations.IfDevEnvironment;
import moe.nea.libautoupdate.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AutoUpdate {
    private static final UpdateContext updateContext = new UpdateContext(
            UpdateSource.githubUpdateSource("mkram17","Bazaar-Utils"),
            UpdateTarget.replaceJar(Main.class),
            CurrentVersion.ofTag("v" + BazaarUtils.currentVersion),
            "bazaarutils"
    );

    public static void checkForUpdates() {
        updateContext.cleanup();
        updateContext.checkUpdate("pre").thenCompose(update -> {
            if (!update.isUpdateAvailable()) return CompletableFuture.completedFuture(null);

            if (BUConfig.get().autoUpdateEnabled) {
                PlayerActionUtil.notifyAll("Successfully updated. Restart for changes to take effect.");
                return update.launchUpdate();
            } else {
                PlayerActionUtil.notifyAll("A new version of Bazaar Utils is available! To update, download it from the Modrinth/GitHub, or enable auto update.");
                return CompletableFuture.completedFuture(null);
            }
        });
    }
}
