package com.github.mkram17.bazaarutils.config;

import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.features.*;
import com.github.mkram17.bazaarutils.features.restrictsell.RestrictSell;
import com.github.mkram17.bazaarutils.features.restrictsell.RestrictSellControl;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import com.github.mkram17.bazaarutils.misc.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.misc.ItemStackCodecGsonAdapter;
import com.github.mkram17.bazaarutils.utils.Util;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
<<<<<<< HEAD
=======
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
>>>>>>> 6e6df62 (initial implementation of limit tracker)
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class BUConfig {
    public static RuntimeTypeAdapterFactory<CustomOrder> customOrderAdapterFactory = RuntimeTypeAdapterFactory.of(CustomOrder.class)
            .registerSubtype(MaxBuyOrder.class)
            .registerSubtype(CustomOrder.class);


    public static final ConfigClassHandler<BUConfig> HANDLER = ConfigClassHandler.createBuilder(BUConfig.class)
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("bazaarutils.json"))
                    .appendGsonBuilder(gsonBuilder -> gsonBuilder
                            .registerTypeAdapter(ItemStack.class, new ItemStackCodecGsonAdapter())
                            .registerTypeAdapterFactory(customOrderAdapterFactory))
                    .build())
            .build();

    public static BUConfig get() {
        return HANDLER.instance();
    }


    @SerialEntry
    public String MODVERSION = "";
    @SerialEntry
    public boolean firstLoad = true;
    @SerialEntry
    public FlipHelper flipHelper = new FlipHelper(true, 17, Items.CHERRY_SIGN);
    @SerialEntry
    public ArrayList<OrderData> watchedOrders = new ArrayList<>();
    @SerialEntry
    public double bzTax = 1.125;
    @SerialEntry
    public ArrayList<CustomOrder> customOrders = new ArrayList<>(List.of(new MaxBuyOrder(true)));
    @SerialEntry
    public boolean developerMode = false;
    @SerialEntry
    public OutdatedOrderHandler outdatedOrderHandler = new OutdatedOrderHandler(false, true);
    //TODO make restrict sell able to take empty array list (might need to think about config gui group + options)
    @SerialEntry
    public RestrictSell restrictSell = new RestrictSell(true, 3, new ArrayList<>(List.of(new RestrictSellControl(RestrictSell.restrictBy.PRICE, 1000000))));
    @SerialEntry
    public Developer developer = new Developer();
    @SerialEntry
    public StashMessages stashMessages = new StashMessages(false);
    @SerialEntry
    public ArrayList<Bookmark> bookmarks = new ArrayList<>();
    @SerialEntry
    public PriceCharts priceCharts = new PriceCharts();
    @SerialEntry
    public OrderStatusHighlight orderStatusHighlight = new OrderStatusHighlight(true);
    @SerialEntry @Getter @Setter
    public boolean disableErrorNotifications = false;
    @SerialEntry @Getter @Setter
    public boolean orderFilledSound = true;
    @SerialEntry @Getter @Setter
    public long dailyLimit;
    @SerialEntry @Getter @Setter
    public String lastDay = "1970-01-01T00:00:00Z";


    public static void openGUI() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.send(() -> client.setScreen(BUConfigGui.create(null, get())));
    }

    public Screen createGUI(Screen parent) {
        return BUConfigGui.create(parent, this);
    }

     public List<BUListener> getSerializedEvents() {
         List<BUListener> events = new ArrayList<>();

         for (Field field : this.getClass().getDeclaredFields()) {
             field.setAccessible(true);
             try {
                 Object value = field.get(this);

                 if (value instanceof BUListener) {
                     events.add((BUListener) value);
                 }
                 else if (value instanceof Collection) {
                     for (Object item : (Collection<?>) value) {
                         if (item instanceof BUListener) {
                             events.add((BUListener) item);
                         }
                     }
                 }
             } catch (IllegalAccessException e) {
                 Util.notifyError("Error accessing field: " + field.getName() + " - " + e.getMessage(), e);
             }
         }
         return events;


     }

     public static List<ItemSlotButtonWidget> getWidgets(){
         List<ItemSlotButtonWidget> widgets = new ArrayList<>();

         widgets.addAll(Bookmark.getWidgets());
         widgets.addAll(BazaarSettingsButton.getWidget());
         return widgets;
     }
        widgets.addAll(Bookmark.getWidgets());
        widgets.addAll(BazaarSettingsButton.getWidget());
        widgets.addAll(OrderLimit.getWidget());
        return widgets;
    }
    private static ButtonOption createAmecsDownloadButton() {
        return ButtonOption.createBuilder()
                .name(Text.of("Download Amecs Reborn"))
                .description(OptionDescription.of(Text.of("Amecs Reborn is needed for the Stash Helper feature. Download here.")))
                .text(Text.of("(for Stash Helper)")) // optional
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

    public static class Developer {
        public boolean allMessages = false;
        public boolean errorMessages = false;
        public boolean guiMessages = false;
        public boolean featureMessages = false;
        public boolean bazaarDataMessages = false;
        public boolean commandMessages = false;
        public boolean itemDataMessages = false;

        public Collection<? extends Option<?>> createOptions() {
            ArrayList<Option<?>> optionList = new ArrayList<>();
            optionList.add(Option.<Boolean>createBuilder()
                    .name(Text.literal("Error Messages"))
                    .binding(errorMessages,
                            () -> errorMessages,
                            newVal -> errorMessages = newVal)
                    .controller(BUConfigGui::createBooleanController)
                    .build());
            optionList.add(Option.<Boolean>createBuilder()
                    .name(Text.literal("GUI Messages"))
                    .binding(guiMessages,
                            () -> guiMessages,
                            newVal -> guiMessages = newVal)
                    .controller(BUConfigGui::createBooleanController)
                    .build());
            optionList.add(Option.<Boolean>createBuilder()
                    .name(Text.literal("Feature Messages"))
                    .binding(featureMessages,
                            () -> featureMessages,
                            newVal -> featureMessages = newVal)
                    .controller(BUConfigGui::createBooleanController)
                    .build());
            optionList.add(Option.<Boolean>createBuilder()
                    .name(Text.literal("Bazaar Data Messages"))
                    .binding(bazaarDataMessages,
                            () -> bazaarDataMessages,
                            newVal -> bazaarDataMessages = newVal)
                    .controller(BUConfigGui::createBooleanController)
                    .build());
            optionList.add(Option.<Boolean>createBuilder()
                    .name(Text.literal("Command Messages"))
                    .binding(commandMessages,
                            () -> commandMessages,
                            newVal -> commandMessages = newVal)
                    .controller(BUConfigGui::createBooleanController)
                    .build());
            optionList.add(Option.<Boolean>createBuilder()
                    .name(Text.literal("Item Data Messages"))
                    .binding(itemDataMessages,
                            () -> itemDataMessages,
                            newVal -> itemDataMessages = newVal)
                    .controller(BUConfigGui::createBooleanController)
                    .build());
            return optionList;
        }

        public boolean isDeveloperVariableEnabled(Util.notificationTypes type) {
            return switch (type) {
                case GUI -> guiMessages;
                case FEATURE -> featureMessages;
                case BAZAARDATA -> bazaarDataMessages;
                case COMMAND -> commandMessages;
                case ORDERDATA -> itemDataMessages;
            };
        }
    }
 }
