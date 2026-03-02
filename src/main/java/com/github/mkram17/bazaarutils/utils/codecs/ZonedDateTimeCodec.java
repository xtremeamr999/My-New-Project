package com.github.mkram17.bazaarutils.utils.codecs;

import com.google.gson.*;
import com.mojang.serialization.Codec;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeCodec {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    public static final Codec<ZonedDateTime> CODEC = Codec.STRING.xmap(
            string -> ZonedDateTime.parse(string, FORMATTER),
            time -> FORMATTER.format(time)
    );
}