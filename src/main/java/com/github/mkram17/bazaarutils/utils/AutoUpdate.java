package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.BUConfigGui;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Getter;
import lombok.Setter;
import moe.nea.libautoupdate.*;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class AutoUpdate {
    @Getter @Setter
    public boolean enabled = true;

    private static final UpdateContext updateContext = new UpdateContext(
            UpdateSource.githubUpdateSource("mkram17","Bazaar-Utils"),
            UpdateTarget.replaceJar(Main.class),
            CurrentVersion.ofTag("v" + BazaarUtils.currentVersion),
            "bazaarutils"
    );

    public static void checkForUpdates() {
        updateContext.cleanup();
        String updateReleaseType = BazaarUtils.releaseType.equals("stable") ? "full" : "pre";
        updateContext.checkUpdate(updateReleaseType).thenCompose(update -> {
            if (!update.isUpdateAvailable()) return CompletableFuture.completedFuture(null);

            if (BUConfig.get().autoUpdate.enabled) {
                PlayerActionUtil.notifyAll("Successfully updated. Restart for changes to take effect.");
                return update.launchUpdate();
            } else {
                PlayerActionUtil.notifyAll("A new version of Bazaar Utils is available! To update, download it from the Modrinth/GitHub, or enable auto update.");
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    public Option<Boolean> createOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Auto Update"))
                .description(OptionDescription.of(Text.literal("If enabled, automatically downloads and installs the latest version when available. Only updates to the release type you are on, e.g. beta -> newer beta version.")))
                .binding(false,
                        this::isEnabled,
                        this::setEnabled)
                .controller(BUConfigGui::createBooleanController)
                .build();
    }
}
