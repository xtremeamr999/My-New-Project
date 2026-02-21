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
            id = "uselessBazaarNotificationsRemover",
            translation = "bazaarutils.config.chat.uselessBazaarNotificationsRemover.value"
    )
    @Comment(
            value = "Enables to remove the transient messages sent to the chat by the Bazaar when interacting with it.",
            translation = "bazaarutils.config.chat.uselessBazaarNotificationsRemover.description"
    )
    public static final UselessBazaarNotificationsRemover uselessBazaarNotificationsRemover = new UselessBazaarNotificationsRemover(false);

    @ConfigEntry(
            id = "STASH_MESSAGES_REMOVER",
            translation = "bazaarutils.config.chat.STASH_MESSAGES_REMOVER.value"
    )
    @Comment(
            value = "Enables to remove the messages sent to the chat remembering to pick up your stash.",
            translation = "bazaarutils.config.chat.STASH_MESSAGES_REMOVER.description"
    )
    public static final StashMessagesRemover stashMessagesRemover = new StashMessagesRemover(false);
}