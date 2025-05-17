package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUSerializedListener;
import com.github.mkram17.bazaarutils.utils.Util;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class StashMessages implements BUSerializedListener {
    public boolean shouldRemoveMessages(){
        return removeMessages;
    }
    @Setter
    private boolean removeMessages;
    @Setter @Getter
    private boolean stashPreviouslyClaimed = false;
    private ArrayList<String> previousMessages = new ArrayList<>();
    private static final String[] removeList = {" ", "materials stashed away", "type of material stashed", "to pick them up", "  "};

    public StashMessages(boolean removeMessages){
        this.removeMessages = removeMessages;
    }
    public void registerEvents(){
        registerStashRemover();
        registerStashClaimDetector();
    }
    private void registerStashClaimDetector(){
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if(message.getString().contains("You picked up") && message.getString().contains("from your material stash") && !stashPreviouslyClaimed) {
                stashPreviouslyClaimed = true;
                BUConfig.HANDLER.save();
                Util.tickExecuteLater(2, () ->{
                    Util.notifyAll("To help claiming your stash, use " + BazaarUtils.stashHelper.getUsage() + " to close the bazaar and claim stash! To disable stash messages, enable the \"Disable Stash Messages\" option in the Bazaar Utils config.");
                    });
            }
        });
    }
    private void registerStashRemover() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!BUConfig.get().stashMessages.shouldRemoveMessages()) return true;

            String currentMessageString = message.getString();
            boolean shouldRemove = currentMessageString.equalsIgnoreCase(removeList[0]);
            int indexOfMessage = indexOfMessage(currentMessageString);

            for(int index = indexOfMessage; index>=1; index--){
                if(index > previousMessages.size()) {
                    previousMessages.clear();
                    break;
                }
                if(previousMessages.get(index-1).contains(removeList[index-1])) {
                    shouldRemove = true;
                } else {
                    shouldRemove = false;
                }
            }

            previousMessages.add(currentMessageString);
            if(!shouldRemove)
                previousMessages.clear();
            return !shouldRemove;
        });

    }
    private static int indexOfMessage(String message){
        for (int k = 1; k < removeList.length-1; k++) {
            String flag = removeList[k];
            if (message.contains(flag))
                return k;
        }
        if(message.contains(removeList[removeList.length-1])) return removeList.length-1;
        return -1;
    }

    public Option<Boolean> createOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Disable Stash Messages"))
                .description(OptionDescription.of(Text.literal("When this option is ON, messages reminding you to pick up your stash will no longer appear in chat.")))
                .binding(false,
                        this::shouldRemoveMessages,
                        this::setRemoveMessages)
                .controller(BUConfig::createBooleanController)
                .build();
    }
}
