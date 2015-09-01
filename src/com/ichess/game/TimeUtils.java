package com.ichess.game;

import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by Ran on 31/08/2015.
 */
public class TimeUtils {

    private final static Logger LOGGER = Logger.getLogger(TimeUtils.class.getName());
    public static final long MINUTES_IN_HOUR = (60L);
    public static final long MINUTES_IN_DAY = (MINUTES_IN_HOUR * 24L);
    public static final long MINUTES_IN_WEEK = (MINUTES_IN_DAY * 7L);
    public static final long MS_IN_SECOND = (1000L);
    public static final long MS_IN_MINUTE = (MS_IN_SECOND * 60L);
    public static final long MS_IN_HOUR = (MS_IN_MINUTE * 60L);
    public static final long MS_IN_DAY = (MS_IN_HOUR * 24L);
    public static final long SECONDS_IN_DAY = (60L * 60L * 24L);
    public static final long SECONDS_IN_YEAR = (SECONDS_IN_DAY * 365L);
    public static final long SECONDS_IN_HOUR = (60L * 60L);
    public static final long SECONDS_IN_MINUTE = (60L);

    public static Date now() { return new Date(); };
    public static long nowInMs() { return now().getTime(); };

}
