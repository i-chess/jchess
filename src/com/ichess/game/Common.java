//==============================================================================
//            Copyright (c) 2009-2014 ichess.co.il
//
//This document contains confidential information which is protected by
//copyright and is proprietary to ichess.co.il. No part
//of this document may be used, copied, disclosed, or conveyed to another
//party without prior written consent of ichess.co.il.
//==============================================================================

package com.ichess.game;


import java.util.logging.Logger;

public class Common {

    private final static Logger LOGGER = Logger.getLogger(Common.class.getName());

    public static final int GAME_KIND_REGULAR = 1;
    public static final int GAME_KIND_FISCHER = 2;
    public static final int GAME_KIND_SUICIDE = 3;
    public static final int GAME_KIND_GRASSHOPER = 4;
    public static final int GAME_KIND_MINICAPA = 5;
    public static final int GAME_KIND_FREEPLAY = 6;
    public static final int GAME_KIND_CRAZY_HOUSE = 7;
    public static final int GAME_KIND_BUG_HOUSE = 8;
    public static final int GAME_KIND_NUM = 9;
    public static final int GAME_RULES_REGULAR = 1;
    public static final int GAME_RULES_FISCHER = 2;
    public static final int GAME_RULES_SUICIDE = 3;
    public static final int GAME_RULES_FREEPLAY = 4;
    public static final int GAME_RULES_CRAZY_HOUSE = 5;
    // game rules
    public static transient final String[] GAME_RULES_TEXT = {"any", "regular", "fischer 960", "suicide", "free", "crazy house"};
    public static final int PIECE_TYPE_START = 0;
    public static final int PIECE_TYPE_PAWN = 1;
    public static final int PIECE_TYPE_KNIGHT = 2;
    public static final int PIECE_TYPE_BISHOP = 3;
    public static final int PIECE_TYPE_ROOK = 4;
    public static final int PIECE_TYPE_QUEEN = 5;
    public static final int PIECE_TYPE_KING = 6;
    public static final int PIECE_TYPE_ILLEGAL = 7;
    public static final int PIECE_TYPE_GRASSHOPER = 8;
    public static final int PIECE_TYPE_ARCHBISHOP = 9;
    public static final int PIECE_TYPE_CHANCELLOR = 10;
    public static final int PIECE_TYPE_DROP_ANY = 11;
    public static final int PIECE_TYPE_NUM = 12;
    public static final int COLOR_START = 0;
    public static final int COLOR_WHITE = 1;
    public static final int COLOR_BLACK = 2;
    public static final int COLOR_ILLEGAL = 3;
    public static final int COLOR_NUM = 4;
    public static final int CASTLE_START = 0;
    public static final int CASTLE_KING = 1;
    public static final int CASTLE_QUEEN = 2;
    public static final int CASTLE_NUM = 3;
    /**
     * A textual description of all game kinds
     */
    public static final String[] GAME_KIND_TEXT = new String[]{"Any", "Regular", "Fishcer 960", "Losing", "Grasshoppers", "Mini Capablance", "Free", "CrazyHouse", "Bug House"};
    public static final String STD_INIT_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static final String DEFAULT_BOARD = "gray";
    public static final String[] BOARDS = new String[] { "Gray", "Earth", "Marble", "Stone" };
    public static final String DEFAULT_PIECES = "merida";
    public static final String[] PIECES = new String[] { "Merida", "Alpha", "Uscf" };
    // max move number value
    public static final int MAX_MOVENUM = 10000;

    public static String getColor(int color) {
        switch (color) {
            case COLOR_WHITE:
                return "White";
            case COLOR_BLACK:
                return "Black";
            case COLOR_ILLEGAL:
                return "Random";
            default:
                return "Unknown";
        }
    }

    public static String getColorChar(int color) {
        switch (color) {
            case COLOR_WHITE:
                return "w";
            case COLOR_BLACK:
                return "b";
            case COLOR_ILLEGAL:
                return "r";
            default:
                return "u";
        }
    }

    public static boolean isBlack(int color) {
        return color == COLOR_BLACK;
    }

    public static boolean isBlackOrWhite(int color) {
        return isWhite(color) || isBlack(color);
    }

    public static boolean isWhite(int color) {
        return color == COLOR_WHITE;
    }

    public static int OtherColor(int col) {
        switch (col) {
            case COLOR_WHITE:
                return COLOR_BLACK;
            case COLOR_BLACK:
                return COLOR_WHITE;
        }
        LOGGER.warning("illegal color " + col);
        return col;
    }

    public enum BOARD_COLUMN {
        A("a"), B("b"), C("c"), D("d"), E("e"), F("f"), G("g"), H("h");

        String letter;

        BOARD_COLUMN(String letter) {
            this.letter = letter;
        }

        public String getColumnName() {
            return letter;
        }
    }
}
