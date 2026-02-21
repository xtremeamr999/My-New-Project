package com.github.mkram17.bazaarutils.utils.codecs;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.lang.reflect.Type;

public class CodecGsonAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    private final Codec<T> codec;

    public CodecGsonAdapter(Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return codec.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return codec.parse(JsonOps.INSTANCE, json).getOrThrow(e -> { throw new JsonParseException(e); });
    }
}