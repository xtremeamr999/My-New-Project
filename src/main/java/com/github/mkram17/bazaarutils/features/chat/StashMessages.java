package com.github.mkram17.bazaarutils.features.chat;

import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.github.mkram17.bazaarutils.events.listener.BUListener;
import com.github.mkram17.bazaarutils.features.util.BUToggleableFeature;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.ArrayList;
import java.util.Collections;

@ConfigObject
public class StashMessages extends BUListener implements BUToggleableFeature {
    public boolean shouldRemoveMessages(){
        return removeMessages;
    }
    @Setter @ConfigEntry(id = "removeMessages")
    private boolean removeMessages;
    @Setter @Getter @ConfigEntry(id = "stashsPreviouslyClaimed")
    private boolean stashPreviouslyClaimed = false;
    private transient ArrayList<String> pastMessages = new ArrayList<>(Collections.singleton(""));
    private static final String[] removeList = {" ", "materials stashed away", "type of material stashed", "to pick them up", "   "};

    public StashMessages(boolean removeMessages){
        this.removeMessages = removeMessages;
    }

    private void registerStashClaimDetector(){
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if(message.getString().contains("You picked up") && message.getString().contains("from your material stash") && !stashPreviouslyClaimed) {
                stashPreviouslyClaimed = true;
                ConfigUtil.scheduleConfigSave();
                Util.tickExecuteLater(2, () -> PlayerActionUtil.notifyAll("TIP - To claim stash more easily and quickly, use the Stash Helper keybind, which closes the bazaar and claims your stash! To disable stash messages, enable the \"Disable Stash Messages\" option in the Bazaar Utils config."));
            }
        });
    }
    private void registerStashRemover() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!shouldRemoveMessages() || message.getString().contains("Mana")) return true;

            String currentMessageString = message.getString();
            int currentMessageRoleIndex = indexOfMessage(currentMessageString);
            if (pastMessages == null) {
                pastMessages = new ArrayList<>();
            }

            boolean removeCurrentMessage;

            if (currentMessageRoleIndex == -1) {
                pastMessages.clear();
                removeCurrentMessage = false;
            } else {
                if (currentMessageRoleIndex == pastMessages.size()) {
                    pastMessages.add(currentMessageString);
                    removeCurrentMessage = true;

                    if (pastMessages.size() == removeList.length)
                        pastMessages.clear();
                } else {
                    pastMessages.clear();
                    removeCurrentMessage = false;

                    if (currentMessageRoleIndex == 0) {
                        pastMessages.add(currentMessageString);
                        removeCurrentMessage = true;
                    }
                }
            }
            return !removeCurrentMessage;
        });
    }
    private static int indexOfMessage(String messageContent) {
        if(messageContent.equals(" "))
            return 0;
        if(messageContent.equals("  "))
            return 4;
        if(messageContent.contains("types of materials stashed"))
            return 2;
        for (int i = removeList.length - 2; i >= 1; i--) {
            if (messageContent.contains(removeList[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void registerFabricEvents() {
        super.subscribeToMeteorEventBus = false;
        registerStashRemover();
        registerStashClaimDetector();
    }
}
