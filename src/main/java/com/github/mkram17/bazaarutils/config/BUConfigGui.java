package com.github.mkram17.bazaarutils.config;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.events.handlers.ChatHandler;
import com.github.mkram17.bazaarutils.features.customorder.CustomOrder;
import com.github.mkram17.bazaarutils.features.restrictsell.InstaSellRestrictions;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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

        config.outbidOrderHandler.createOption(generalBuilder);
        generalBuilder.option(ChatHandler.createOrderFilledSoundOption());
        config.stashMessages.createOption(generalBuilder);
        config.uselessNotificationRemover.createOption(generalBuilder);
        config.priceCharts.createOption(generalBuilder);
        config.orderStatusHighlight.createOption(generalBuilder);
        generalBuilder.option(createDisableErrorNotifsOption(config));
        config.orderLimit.createOption(generalBuilder);
        config.instaSellRestrictions.createOption(generalBuilder);

        builder.category(generalBuilder.build());
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

    public static BooleanControllerBuilder createBooleanController(Option<Boolean> opt) {
        return BooleanControllerBuilder.create(opt).onOffFormatter().coloured(true);
    }
}
