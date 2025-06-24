package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.events.ScreenChangeEvent;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.Util;
import dev.isxander.yacl3.api.Option;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
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
        super(enabled);
    }

    @Override
    public void subscribe() {
        super.subscribe();
    }

    @EventHandler
    public void onScreenChange(ScreenChangeEvent event){
        try {
            if(!inCorrectScreen(event)) {
                return;
            }
            ItemStack itemStack = getItemStack(event.getOldScreen());
            if(itemStack == null)
                return;

            MinecraftClient client = MinecraftClient.getInstance();
            updateScoreboard(client);

            String name = itemStack.getCustomName().getString();
            String productID = BazaarData.findProductId(name);

            if(productID == null)
                return;


            double cost = BazaarData.findItemPrice(productID, OrderPriceInfo.priceTypes.INSTASELL) + .1;//.1 is for lowest competitive price

            int amountCanBuy = (int) (Math.floor(purse / cost));
            super.setOrderAmount(Math.min(amountCanBuy, 71680));

        } catch (Exception e) {
            Util.notifyError("Could not parse coins from scoreboard text", e);
        }
    }

    private static boolean inCorrectScreen(ScreenChangeEvent event){
        return (event.getNewScreen().getTitle().getString().contains("How many do you want?") || event.getNewScreen().getTitle().getString().contains("➜ Insta"))
                && event.getNewScreen() instanceof GenericContainerScreen;
    }

    private static ItemStack getItemStack(Screen previousScreen) {

        if(!(previousScreen instanceof GenericContainerScreen containerScreen))
            return null;

        ItemStack itemStack = containerScreen.getScreenHandler().getInventory().getStack(13);
        if (itemStack.isEmpty()) {
            Util.notifyError("Could not find item in previous container.", null);
            return null;
        }
        return itemStack;
    }

//    private static int calculateMaximumCanBuy(){
//        if(GUIUtils.inBuyOrderScreen()){
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

