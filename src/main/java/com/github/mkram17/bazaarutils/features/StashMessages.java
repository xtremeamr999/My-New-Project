package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfig;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Setter;
import net.minecraft.text.Text;

//actual handling done in ChatHandler
public class StashMessages {
    public boolean shouldRemoveMessages(){
        return removeMessages;
    }
    @Setter
    private boolean removeMessages;
    public StashMessages(boolean removeMessages){
        this.removeMessages = removeMessages;
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
