package com.github.mkram17.bazaarutils.config.features.chat;


import com.github.mkram17.bazaarutils.features.chat.StashMessagesRemover;
import com.github.mkram17.bazaarutils.features.chat.UselessBazaarNotificationsRemover;
import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;

@Category(value = "chat_config")
@ConfigInfo(
        title = "Chat Config",
        titleTranslation = "bazaarutils.config.chat.category.value",
        description = "Configurations for the chat features of the mod",
        descriptionTranslation = "bazaarutils.config.chat.category.description",
        icon = "keyboard"
)
public final class ChatConfig {
    @ConfigEntry(
            id = "useless_bazaar_notifications_remover",
            translation = "bazaarutils.config.chat.useless_bazaar_notifications_remover.value"
    )
    @Comment(
            value = "Enables to remove the transient messages sent to the chat by the Bazaar when interacting with it.",
            translation = "bazaarutils.config.chat.useless_bazaar_notifications_remover.description"
    )
    public static final UselessBazaarNotificationsRemover USELESS_BAZAAR_NOTIFICATIONS_REMOVER = new UselessBazaarNotificationsRemover(false);

    @ConfigEntry(
            id = "stash_messages_remover",
            translation = "bazaarutils.config.chat.stash_messages_remover.value"
    )
    @Comment(
            value = "Enables to remove the messages sent to the chat remembering to pick up your stash.",
            translation = "bazaarutils.config.chat.stash_messages_remover.description"
    )
    public static final StashMessagesRemover STASH_MESSAGES_REMOVER = new StashMessagesRemover(false);
}