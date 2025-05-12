package com.github.mkram17.bazaarutils.data;

import com.github.mkram17.bazaarutils.utils.Util;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.hypixel.api.reply.AbstractReply;

import java.util.UUID;
import java.util.function.BiConsumer;

public class APIUtils {

    public static String getApiKey() {
        String apiKey = System.getenv("HYPIXEL_API_KEY");
        if (apiKey != null) {
            return apiKey;
        }
        Util.notifyAll("No API key found!", Util.notificationTypes.BAZAARDATA);
        return System.getProperty("apiKey", "");
    }

    public static final HypixelAPI API;
    public static final UUID uuid;

    static {
        uuid = UUID.fromString(getApiKey());
        API = new HypixelAPI(new ApacheHttpClient(uuid));
    }

    public static <T extends AbstractReply> BiConsumer<T, Throwable> getTestConsumer() {
        return (result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                System.exit(0);
                return;
            }

//            System.out.println(result);

            System.exit(0);
        };
    }
}
