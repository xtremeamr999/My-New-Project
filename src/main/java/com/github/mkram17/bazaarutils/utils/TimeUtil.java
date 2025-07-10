package com.github.mkram17.bazaarutils.utils;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.swing.Timer;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.features.OrderLimit;

public class TimeUtil {
    protected static ZonedDateTime modInitTime;
    protected static ZonedDateTime nextReset;
    protected static long timeToLimitReset;

    public static void init() {
        updateTime();
        startTimer();

    }

    private static void updateTime() {
        ZonedDateTime lastTime = ZonedDateTime.parse(BUConfig.get().getLastDay());
        modInitTime = ZonedDateTime.now(ZoneOffset.UTC);
        nextReset = modInitTime.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC);
        if (lastTime == null || Duration
                .between(lastTime, modInitTime.toLocalDate().atStartOfDay().atZone(ZoneOffset.UTC)).isPositive()) {
            OrderLimit.resetLimit();
        }
        timeToLimitReset = Duration.between(modInitTime, nextReset).toSeconds();
        BUConfig.get().setLastDay(modInitTime.toLocalDate().atStartOfDay().atZone(ZoneOffset.UTC).toString());
    }

    private static void startTimer() {
        Timer timer = new Timer((int) (1000 * timeToLimitReset), e -> {
            OrderLimit.resetLimit();
            updateTime();
            Timer delay = new Timer(1000, ev -> {
                startTimer();
            });
            delay.setRepeats(false);
            delay.start();
        });

        timer.setRepeats(false);
        timer.start();

    }

}
