package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.utils.Util;
import dev.isxander.yacl3.api.Option;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//thanks to skyblocker for purse and scoreboard code
public class MaxBuyOrder extends CustomOrder {
    /**
     * @implNote The parent text will always be empty, the actual text content is inside the text's siblings.
     */
    public static final ObjectArrayList<Text> TEXT_SCOREBOARD = new ObjectArrayList<>();
    public static final ObjectArrayList<String> STRING_SCOREBOARD = new ObjectArrayList<>();
    private static final Pattern PURSE = Pattern.compile("(Purse|Piggy): (?<purse>[0-9,.]+)( \\((?<change>[+\\-][0-9,.]+)\\))?");
    private static double purse;

    public MaxBuyOrder(boolean enabled) {
        super(enabled, 71680, 17, Items.PURPLE_STAINED_GLASS_PANE);
//        registerScreenOpen();
    }

    private void registerScreenOpen(){
            ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
                try {
                    if(!(BazaarUtils.gui.inBuyOrderScreen() || BazaarUtils.gui.inInstaBuy())) {
                        return;
                    }
                    updateScoreboard(client);

                } catch (Exception e) {
                    Util.notifyError("Could not parse coins from scoreboard text.", null);
                }
            });
    }
    @Override
    public void subscribe() {
        super.subscribe();
        registerScreenOpen();
    }

//    private static int calculateMaximumCanBuy(){
//        if(BazaarUtils.gui.inBuyOrderScreen()){
//        }
//    }

    private static void updateScoreboard(MinecraftClient client) {
        try {
            TEXT_SCOREBOARD.clear();
            STRING_SCOREBOARD.clear();

            ClientPlayerEntity player = client.player;
            if (player == null) return;

            Scoreboard scoreboard = player.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
            ObjectArrayList<Text> textLines = new ObjectArrayList<>();
            ObjectArrayList<String> stringLines = new ObjectArrayList<>();

            for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
                //Limit to just objectives displayed in the scoreboard (specifically sidebar objective)
                if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
                    Team team = scoreboard.getScoreHolderTeam(scoreHolder.getNameForScoreboard());

                    if (team != null) {
                        Text textLine = Text.empty().append(team.getPrefix().copy()).append(team.getSuffix().copy());
                        String strLine = team.getPrefix().getString() + team.getSuffix().getString();

                        if (!strLine.trim().isEmpty()) {
                            String formatted = Formatting.strip(strLine);

                            textLines.add(textLine);
                            stringLines.add(formatted);
                        }
                    }
                }
            }

            if (objective != null) {
                stringLines.add(objective.getDisplayName().getString());
                textLines.add(Text.empty().append(objective.getDisplayName().copy()));

                Collections.reverse(stringLines);
                Collections.reverse(textLines);
            }

            TEXT_SCOREBOARD.addAll(textLines);
            STRING_SCOREBOARD.addAll(stringLines);
            updatePurse();
        } catch (NullPointerException e) {
            Util.notifyError("Could not update scoreboard.", e);
        }
    }

    public static void updatePurse() {
        STRING_SCOREBOARD.stream().filter(s -> s.contains("Piggy:") || s.contains("Purse:")).findFirst().ifPresent(purseString -> {
            Matcher matcher = PURSE.matcher(purseString);
            if (matcher.find()) {
                double newPurse = Double.parseDouble(matcher.group("purse").replaceAll(",", ""));
                double changeSinceLast = newPurse - purse;
                if (changeSinceLast == 0) return;
                purse = newPurse;
            }
        });
    }

    @Override
    public Option<Boolean> createOption() {
        return super.createOption(
                "Buy Max Button",
                "Buy order button for the maximum amount of an item you can buy with the coins in your purse.",
                this::isEnabled,
                this::setEnabled
        );
    }
}

