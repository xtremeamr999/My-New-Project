package com.github.mkram17.bazaarutils.utils;

import java.sql.Time;
import java.time.*;

import javax.swing.Timer;

import com.github.mkram17.bazaarutils.events.BUListener;
import lombok.Getter;

public class TimeUtil implements BUListener {
    @Getter
    private static ZonedDateTime modInitTime;
    public static final ZonedDateTime LAST_BAZAAR_LIMIT_RESET_TIME = ZonedDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT, ZoneOffset.UTC);
    @Getter
    private static ZonedDateTime nextBazaarLimitReset;
    @Getter
    private static long timeToBazaarLimitReset;
    public static final TimeUtil INSTANCE = new TimeUtil();

    public static void init() {
        updateTime();
        startTimer();
    }

    private static void updateTime() {
        modInitTime = ZonedDateTime.now(ZoneOffset.UTC);
        nextBazaarLimitReset = modInitTime.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC);
        timeToBazaarLimitReset = Duration.between(modInitTime, nextBazaarLimitReset).toSeconds();
    }

    private static void startTimer() {
        Timer timer = new Timer(10 * 1000, e -> {
            updateTime();
        });

        timer.setRepeats(true);
        timer.start();
    }

    @Override
    public void subscribe() {
        init();
    }
}
