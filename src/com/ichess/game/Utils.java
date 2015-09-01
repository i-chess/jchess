package com.ichess.game;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ran on 31/08/2015.
 */
public class Utils {

    private final static Logger LOGGER = Logger.getLogger(Utils.class.getName());
    private final static Random random = new Random();

    public static void AssertNotNull(Object object, String message) {
        if (object == null) {
            LOGGER.warning(message);
        }
        assert object != null : message;
    }

    public static void AssertNotNull(Object object) {
        AssertNotNull(object, "");
    }

    public static void Assert(boolean cond, String message) {
        if (! cond) {
            LOGGER.warning(message);
        }
        assert cond : message;
    }

    public static void Assert(boolean cond) {
        AssertNotNull(cond, "");
    }


    public static int randomInt(int n) {
        return random.nextInt(n);
    }

    public static boolean isBetween(int x, int a, int b) {
        return (x >= a) && (x <= b);
    }

    public static boolean isInPath(int x1, int y1, int x2, int y2, int x3, int y3) {

        if ((x1 == x2) && (y1 == y2)) {
            return true;
        }
        if ((x1 == x3) && (y1 == y3)) {
            return true;
        }

        // if x2=x3, then check if y1 is in the middle
        if (x2 == x3) {
            return ((x1 == x2) && (((y2 > y1) && (y1 > y3)) || ((y2 < y1) && (y1 < y3))));
        }

        // if y2=y3, then check if x1 is in the middle
        if (y2 == y3) {
            return ((y1 == y2) && (((x2 > x1) && (x1 > x3)) || ((x2 < x1) && (x1 < x3))));
        }

        // both are different, then it must be diagonal check
        int deltaX = x3 - x2;
        int deltaY = y3 - y2;

        if (Math.abs(deltaX) != Math.abs(deltaY)) {
            return false;
        }

        deltaX = deltaX / Math.abs(deltaX);
        deltaY = deltaY / Math.abs(deltaY);

        int curX = x2;
        int curY = y2;

        while ((curX != x3) && (curY != y3)) {
            if ((x1 == curX) && (y1 == curY)) {
                return true;
            }
            curX += deltaX;
            curY += deltaY;
        }
        return false;
    }
    
    public static boolean isEmptyString(String str) {
        return (str == null) || (str.equals(""));
    }

    public static void exception(Exception ex) {
        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
    }

    public static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch ( NumberFormatException ex) {
            exception(ex);
            return 0;
        }
    }

    public static<T> T getFirstInList(List<T> list) {
        if ((list == null) || (list.size() == 0)) {
            return null;
        }
        return list.get(0);
    }


    public static final String LRE = "\u202A";

    public static final String RLE = "\u202B";

    public static final String PDF = "\u202C";

    public static final String LRO = "\u202D";

    public static final String RLO = "\u202E";

    public static String encodeInRLE(String str) {
        return RLE + str + PDF;
    }

    public static String encodeInLRE(String str) {
        return LRE + str + PDF;
    }

    public static String encodeInRLO(String str) {
        return RLO + str + PDF;
    }

    public static String encodeInLRO(String str) {
        return LRO + str + PDF;
    }
}
