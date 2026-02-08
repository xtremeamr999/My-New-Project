package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.utils.bazaar.data.BazaarDataManager;
import com.github.mkram17.bazaarutils.events.ScreenChangeEvent;
import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderType;
import dev.isxander.yacl3.api.Option;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.*;
import net.minecraft.util.Formatting;
import com.github.mkram17.bazaarutils.features.customorder.CustomOrder;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaxBuyOrder extends CustomOrder {
    private static final Pattern PURSE_PATTERN = Pattern.compile("(Purse|Piggy): (?<purse>[0-9,.]+)");
    private static double purse;

    public MaxBuyOrder(boolean enabled) {
        super(enabled);
    }

    @Override
    public void subscribe() {
        super.subscribe();
    }

    @EventHandler
    public void onScreenChange(ScreenChangeEvent event) {
        if (event.getNewScreen() == null || event.getOldScreen() == null) {
            return;
        }

        try {
            if (!inCorrectScreen(event)) {
                return;
            }

            ItemStack itemStack = getItemStack(event.getOldScreen());

            if (itemStack == null) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            updatePurse(client);

            String name = itemStack.getCustomName().getString();
            Optional<String> productIdOptional = BazaarDataManager.findProductIdOptional(name);

            if (productIdOptional.isEmpty()) {
                return;
            }

            OptionalDouble costOpt = BazaarDataManager.findItemPriceOptional(productIdOptional.get(), OrderType.BUY);

            if (costOpt.isEmpty()) {
                return;
            }

            double cost = costOpt.getAsDouble() + .1;//.1 is for lowest competitive price

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
        if (!(previousScreen instanceof GenericContainerScreen containerScreen)) {
            return null;
        }

        ItemStack itemStack = containerScreen.getScreenHandler().getInventory().getStack(13);

        if (itemStack.isEmpty()) {
            Util.notifyError("Could not find item in previous container.", new Throwable());
            return null;
        }

        return itemStack;
    }

    private static void updatePurse(MinecraftClient client) {
        ClientWorld world = client.world;

        if (world == null) {
            return;
        }

        Scoreboard scoreboard = world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);

        if (objective == null) {
            return;
        }

        ObjectArrayList<String> stringLines = new ObjectArrayList<>();

        for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
            if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
                Team team = scoreboard.getScoreHolderTeam(scoreHolder.getNameForScoreboard());
                if (team != null) {
                    String line = team.getPrefix().getString() + team.getSuffix().getString();
                    if (!line.trim().isEmpty()) {
                        stringLines.add(Formatting.strip(line));
                    }
                }
            }
        }

        purse = getPurse(stringLines);
    }

    private static double getPurse(List<String> scoreboardLines) {
        for (String line : scoreboardLines) {
            if (line.contains("Purse:") || line.contains("Piggy:")) {
                Matcher matcher = PURSE_PATTERN.matcher(line);

                if (matcher.find()) {
                    try {
                        return Double.parseDouble(matcher.group("purse").replace(",", ""));
                    } catch (NumberFormatException e) {
                        Util.notifyError("Failed to parse purse from scoreboard", e);
                    }
                }
            }
        }
        
        return -1d;
    }
}