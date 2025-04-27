package com.github.mkram17.bazaarutils.data;

import com.github.mkram17.bazaarutils.misc.ItemData;
import com.github.mkram17.bazaarutils.Utils.Util;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.google.gson.*;
import net.hypixel.api.reply.skyblock.SkyBlockBazaarReply;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BazaarData {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String PRODUCT_NAME_RESOURCE = "bazaar-resources.json";
//    private static final String dataFile = "bazaar_json.json";
    static ScheduledExecutorService bzExecutor = Executors.newScheduledThreadPool(5);
    private static SkyBlockBazaarReply bazaarReply = null;


    public static<T> String getAsPrettyJsonObject(String object){
        return gson.toJson(object);
    }
    public static JsonObject getAsJsonObjectFromString(String str){
        return JsonParser.parseString(str).getAsJsonObject();
    }

    //called in init
    public static void scheduleBazaar(){
        bzExecutor.scheduleAtFixedRate(() -> {
                APIUtils.API.getSkyBlockBazaar().whenComplete((reply, throwable) -> {
                    if (throwable != null) {
                        Util.notifyAll("Exception thrown trying to get bazaar data", Util.notificationTypes.ERROR);
                        throwable.printStackTrace();
                    } else {
                        if(reply == null){
                            Util.notifyAll("Bazaar data is null", Util.notificationTypes.ERROR);
                            return;
                        }
                        bazaarReply = reply;
//                        writeJsonToFile(jsonString);

                        if (!BUConfig.get().watchedItems.isEmpty()) {
                            ItemData.update();
                        }
                    }
                });
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static JsonObject loadResourceJson(String resourcePath) {
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        Identifier resourceId = Identifier.of("bazaar-utils", resourcePath);

        try {
            Optional<Resource> optionalResource = resourceManager.getResource(resourceId);
            if (optionalResource.isPresent()) {
                Resource resource = optionalResource.get();
                try (InputStream inputStream = resource.getInputStream();
                     InputStreamReader reader = new InputStreamReader(inputStream)) {
                    return JsonParser.parseReader(reader).getAsJsonObject();
                }
            } else {
                Util.notifyAll("Could not find resource: " + resourcePath, Util.notificationTypes.ERROR);
                return new JsonObject();
            }
        } catch (IOException e) {
            Util.notifyAll("Error reading resource: " + resourcePath, Util.notificationTypes.ERROR);
            e.printStackTrace();
            return new JsonObject();
        }
    }

//    public static File getDataFile() {
//        return FabricLoader.getInstance().getConfigDir().resolve("bazaarutils_data.json").toFile();
//    }

//    private static void writeJsonToFile(String jsonString) {
//        try (FileWriter writer = new FileWriter(getDataFile())) {
//            writer.write(jsonString);
//        } catch (IOException e) {
//            Util.notifyAll("Error writing JSON data to file: " + e.getMessage(), Util.notificationTypes.BAZAARDATA);
//            e.printStackTrace();
//        }
//    }

    //(product id, what you are looking for in quick status-- either buyPrice or sellPrice)
    public static Double findItemPrice(String productId, ItemData.priceTypes priceType) {
        double sellPrice = -1;
        double buyPrice = -1;
        if (bazaarReply == null){
            Util.notifyAll("Bazaar data is null", Util.notificationTypes.ERROR);
            return -1.0;
        }
        try {
            var product = bazaarReply.getProduct(productId);
            var buy_summary = product.getBuySummary();
            var sell_summary = product.getSellSummary();
            sellPrice = sell_summary.get(0).getPricePerUnit();
            buyPrice = buy_summary.get(0).getPricePerUnit();
            Util.notifyAll("Price found: " + sellPrice, Util.notificationTypes.BAZAARDATA);
            Util.notifyAll("Price found: " +buyPrice, Util.notificationTypes.BAZAARDATA);
            if(priceType == ItemData.priceTypes.INSTASELL)
                return sellPrice;
            else if(priceType == ItemData.priceTypes.INSTABUY)
                return buyPrice;
        } catch (Exception e) {
            Util.notifyAll("There was an error fetching Json objects (probably caused by incorrect product ID [" + productId + "])", Util.notificationTypes.ERROR);
            Util.notifyAll(e.getMessage(), Util.notificationTypes.ERROR);
            Util.notifyAll(e.getCause(), Util.notificationTypes.ERROR);
            e.printStackTrace();
        }

        if (priceType == ItemData.priceTypes.INSTASELL) {
            Util.notifyAll("Price found: " + sellPrice, Util.notificationTypes.BAZAARDATA);
            return sellPrice;
        } else if (priceType == ItemData.priceTypes.INSTABUY) {
            Util.notifyAll("Price found: " +buyPrice, Util.notificationTypes.BAZAARDATA);
            return buyPrice;
        }
        return null;
    }

    //returns null if it cant find anything, gets product id from natural name
    public static String findProductId(String name) {
        JsonObject resources;
        JsonObject bazaarConversions;

        try {
            resources = loadResourceJson(PRODUCT_NAME_RESOURCE);
            bazaarConversions = resources.getAsJsonObject("bazaarConversions");

            for (String key : bazaarConversions.keySet()) {
                if (bazaarConversions.get(key).getAsString().equalsIgnoreCase(name)) {
                    return key;
                }
            }
        } catch (Exception e) {
            Util.notifyAll("Error finding product ID: " + e.getMessage(), Util.notificationTypes.ERROR);
            e.printStackTrace();
        }

        Util.notifyAll("Couldn't find product id", Util.notificationTypes.BAZAARDATA);
        return null;
    }

}
