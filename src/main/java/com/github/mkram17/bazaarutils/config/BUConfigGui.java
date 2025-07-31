package com.github.mkram17.bazaarutils.config;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.events.handlers.ChatHandler;
import com.github.mkram17.bazaarutils.features.CustomOrder;
import com.github.mkram17.bazaarutils.features.restrictsell.InstaSellRestrictions;
import com.github.mkram17.bazaarutils.misc.BUCompatibilityHelper;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.URISyntaxException;

public class BUConfigGui {
    public static Screen create(Screen parent, BUConfig config) {
        return YetAnotherConfigLib.create(BUConfig.HANDLER, (defaults, cfg, builder) -> {
            builder.title(Text.literal(BazaarUtils.MOD_NAME));

            buildGeneralCategory(builder, config);
            buildCustomOrdersCategory(builder);

            if (config.developerMode) {
                buildDeveloperCategory(builder, config.developer);
            }

            return builder;
        }).generateScreen(parent);
    }

    private static void buildGeneralCategory(YetAnotherConfigLib.Builder builder, BUConfig config) {
        ConfigCategory.Builder generalBuilder = ConfigCategory.createBuilder()
                .name(Text.literal("General"));

        generalBuilder.option(config.flipHelper.createOption());
        generalBuilder.options(config.outdatedOrderHandler.createOptions());
        generalBuilder.option(ChatHandler.createOrderFilledSoundOption());
        generalBuilder.option(config.stashMessages.createOption());
        generalBuilder.option(config.priceCharts.createOption());
        generalBuilder.option(config.orderStatusHighlight.createOption());
        generalBuilder.option(createDisableErrorNotifsOption(config));

        if (!BUCompatibilityHelper.isAmecsReborn()) {
            generalBuilder.option(createAmecsDownloadButton());
        }

        generalBuilder.group(buildRestrictSellGroup(config.instaSellRestrictions));
        generalBuilder.group(config.orderLimit.buildOrderLimitGroup());

        builder.category(generalBuilder.build());
    }

    private static OptionGroup buildRestrictSellGroup(InstaSellRestrictions instaSellRestrictions) {
        OptionGroup.Builder restrictSellGroupBuilder = OptionGroup.createBuilder()
                .name(Text.literal("Sell rules"))
                .description(OptionDescription.of(Text.literal("Blocks insta selling based on rules. You can add a new rule with /bu rule add {based on volume or price} {amount over which will be restricted} or you can remove it with /bu rule remove {rule number}")));

        if (instaSellRestrictions.getControls().isEmpty()) {
            instaSellRestrictions.addRule(InstaSellRestrictions.restrictBy.PRICE, 1000000);
        }
        instaSellRestrictions.buildOptions(restrictSellGroupBuilder);

        return restrictSellGroupBuilder.build();
    }

    private static void buildCustomOrdersCategory(YetAnotherConfigLib.Builder builder) {
        OptionGroup.Builder customOrdersGroupBuilder = OptionGroup.createBuilder()
                .name(Text.literal("Custom Buy Amounts"))
                .description(OptionDescription.of(Text.literal("Add buttons for custom buy order/insta buy amounts. To add more do /bu customorder add {order amount} {slot number} (top left slot is slot #1, to the right is #2, etc etc.")));

        CustomOrder.buildOptions(customOrdersGroupBuilder);
        builder.category(CustomOrder.createOrdersCategory().group(customOrdersGroupBuilder.build()).build());
    }

    private static void buildDeveloperCategory(YetAnotherConfigLib.Builder builder, BUConfig.Developer developer) {
        ConfigCategory.Builder developerBuilder = ConfigCategory.createBuilder()
                .name(Text.literal("Developer"));

        developerBuilder.option(Option.<Boolean>createBuilder()
                .name(Text.literal("All Messages"))
                .binding(developer.allMessages,
                        () -> developer.allMessages,
                        newVal -> developer.allMessages = newVal)
                .controller(BUConfigGui::createBooleanController)
                .build());

        developerBuilder.group(
                OptionGroup.createBuilder()
                        .name(Text.literal("Message Options"))
                        .description(OptionDescription.of(Text.literal("DEVELOPER ONLY")))
                        .options(developer.createOptions())
                        .build());

        builder.category(developerBuilder.build());
    }

    private static Option<Boolean> createDisableErrorNotifsOption(BUConfig config) {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Disable Error Notifications"))
                .description(OptionDescription.of(Text.literal("Not recommended to enable this unless you are experiencing error spam. This will disable all error notifications, but not the errors themselves.")))
                .binding(
                        config.disableErrorNotifications,
                        () -> config.disableErrorNotifications,
                        newVal -> config.disableErrorNotifications = newVal
                )
                .controller(BUConfigGui::createBooleanController)
                .build();
    }

    private static ButtonOption createAmecsDownloadButton() {
        return ButtonOption.createBuilder()
                .name(Text.of("Download Amecs Reborn"))
                .description(OptionDescription.of(Text.of("Amecs Reborn is needed for the Stash Helper feature. Download here.")))
                .text(Text.of("(for Stash Helper)"))
                .action((yaclScreen, buttonOption) -> {
                    MinecraftClient.getInstance().setScreen(new ConfirmLinkScreen((confirmed) -> {
                        if (confirmed) {
                            try {
                                net.minecraft.util.Util.getOperatingSystem().open(new URI("https://modrinth.com/mod/amecs-reborn"));
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        MinecraftClient.getInstance().setScreen(null);
                    }, "https://modrinth.com/mod/amecs-reborn", true));
                })
                .build();
    }

    public static BooleanControllerBuilder createBooleanController(Option<Boolean> opt) {
        return BooleanControllerBuilder.create(opt).onOffFormatter().coloured(true);
    }
}

