//==============================================================================
//            Copyright (c) 2009-2014 ichess.co.il
//
//This document contains confidential information which is protected by
//copyright and is proprietary to ichess.co.il. No part
//of this document may be used, copied, disclosed, or conveyed to another
//party without prior written consent of ichess.co.il.
//==============================================================================

package com.ichess.game;



import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * @author Ran Berenfeld
 * @version 1.0
 */
public class Notation {

    private final static Logger LOGGER = Logger.getLogger(Notation.class.getName());

    // map of numbers to rows, English
    private static final List<String> sColNamesEng = Arrays.asList("", "a", "b", "c", "d", "e", "f", "g", "h");
    // map of numbers to rows
    private static final String[] sRowNames = {"", "1", "2", "3", "4", "5", "6", "7", "8"};

    // mapping of English characters to pieces
    private static HashMap<String, Integer> sCharEngToPiece = new HashMap<String, Integer>();
    // mapping of pieces to English characters
    private static HashMap<Integer, String> sPieceToCharEng = new HashMap<Integer, String>();

    static {
        sCharEngToPiece.put("Q", Common.PIECE_TYPE_QUEEN);
        sCharEngToPiece.put("K", Common.PIECE_TYPE_KING);
        sCharEngToPiece.put("R", Common.PIECE_TYPE_ROOK);
        sCharEngToPiece.put("N", Common.PIECE_TYPE_KNIGHT);
        sCharEngToPiece.put("B", Common.PIECE_TYPE_BISHOP);
        sCharEngToPiece.put("P", Common.PIECE_TYPE_PAWN);
        sCharEngToPiece.put("G", Common.PIECE_TYPE_GRASSHOPER);
        sCharEngToPiece.put("A", Common.PIECE_TYPE_ARCHBISHOP);
        sCharEngToPiece.put("C", Common.PIECE_TYPE_CHANCELLOR);
        sCharEngToPiece.put("", Common.PIECE_TYPE_PAWN);
    }

    static {
        sPieceToCharEng.put(Common.PIECE_TYPE_QUEEN, "Q");
        sPieceToCharEng.put(Common.PIECE_TYPE_KING, "K");
        sPieceToCharEng.put(Common.PIECE_TYPE_ROOK, "R");
        sPieceToCharEng.put(Common.PIECE_TYPE_KNIGHT, "N");
        sPieceToCharEng.put(Common.PIECE_TYPE_BISHOP, "B");
        sPieceToCharEng.put(Common.PIECE_TYPE_PAWN, "P");
        sPieceToCharEng.put(Common.PIECE_TYPE_GRASSHOPER, "G");
        sPieceToCharEng.put(Common.PIECE_TYPE_ARCHBISHOP, "A");
        sPieceToCharEng.put(Common.PIECE_TYPE_CHANCELLOR, "C");
        sPieceToCharEng.put(Common.PIECE_TYPE_DROP_ANY, "X");
    }

    // mapping of pieces to unicode figures (white)
    private static HashMap<Integer, String> sPieceToUnicodeFiguresWhite = new HashMap<Integer, String>();
    // mapping of pieces to unicode figures (black)
    private static HashMap<Integer, String> sPieceToUnicodeFiguresBlack = new HashMap<Integer, String>();

    static {
        sPieceToUnicodeFiguresWhite.put(Common.PIECE_TYPE_KING, "\u2654");
        sPieceToUnicodeFiguresWhite.put(Common.PIECE_TYPE_QUEEN, "\u2655");
        sPieceToUnicodeFiguresWhite.put(Common.PIECE_TYPE_ROOK, "\u2656");
        sPieceToUnicodeFiguresWhite.put(Common.PIECE_TYPE_BISHOP, "\u2657");
        sPieceToUnicodeFiguresWhite.put(Common.PIECE_TYPE_KNIGHT, "\u2658");
        sPieceToUnicodeFiguresWhite.put(Common.PIECE_TYPE_PAWN, "\u2659");
        sPieceToUnicodeFiguresWhite.put(Common.PIECE_TYPE_GRASSHOPER, "\u2645");
        sPieceToUnicodeFiguresWhite.put(Common.PIECE_TYPE_ARCHBISHOP, "\u2647");
        sPieceToUnicodeFiguresWhite.put(Common.PIECE_TYPE_CHANCELLOR, "\u2646");
    }

    static {
        sPieceToUnicodeFiguresBlack.put(Common.PIECE_TYPE_KING, "\u265A");
        sPieceToUnicodeFiguresBlack.put(Common.PIECE_TYPE_QUEEN, "\u265B");
        sPieceToUnicodeFiguresBlack.put(Common.PIECE_TYPE_ROOK, "\u265C");
        sPieceToUnicodeFiguresBlack.put(Common.PIECE_TYPE_BISHOP, "\u265D");
        sPieceToUnicodeFiguresBlack.put(Common.PIECE_TYPE_KNIGHT, "\u265E");
        sPieceToUnicodeFiguresBlack.put(Common.PIECE_TYPE_PAWN, "\u265F");
        sPieceToUnicodeFiguresBlack.put(Common.PIECE_TYPE_GRASSHOPER, "\u2648");
        sPieceToUnicodeFiguresBlack.put(Common.PIECE_TYPE_ARCHBISHOP, "\u2650");
        sPieceToUnicodeFiguresBlack.put(Common.PIECE_TYPE_CHANCELLOR, "\u2649");
    }

    /**
     * try to find the piece type from the given string. look in all supported languages. english first
     *
     * @param str
     * @return the found piece type. PIECE_TYPE_ILLEGAL if not found
     */
    static int getPieceType(String str) {
        Integer pieceType = sCharEngToPiece.get(str);
        return pieceType == null ? Common.PIECE_TYPE_ILLEGAL : pieceType;
    }

    /**
     * tries to get column number, first in english then in all languages
     *
     * @param columnLetter
     * @return the column number from the given column letter
     */
    static int getColumn(String columnLetter) {
        return sColNamesEng.indexOf(columnLetter);
    }

    static int getColumn(char columnLetter) {
        return getColumn(String.valueOf(columnLetter));
    }

    /**
     * return the piece character of the given piece type
     *
     * @param pieceType
     * @return The piece character of the given piece type
     */
    public static String getPieceCharacter(int pieceType) {
        return sPieceToCharEng.get(pieceType);
    }

    static Move getMove(Game game, String str) {
        Utils.AssertNotNull(game);
        Utils.AssertNotNull(str);

        int fromX, fromY, toX, toY;

        LOGGER.fine("getting move '" + str + "'");

        // check if move is in long numeric format
        if (str.length() >= 4) {
            fromX = str.charAt(1) - 48;
            fromY = getColumn(str.charAt(0));
            toX = str.charAt(3) - 48;
            toY = getColumn(str.charAt(2));

            LOGGER.fine("Found move numeric " + fromX + "," + fromY + "-" + toX + "," + toY);
            if ((Utils.isBetween(fromX, 1, 8)) && (Utils.isBetween(fromY, 1, 8)) && (Utils.isBetween(toX, 1, 8))
                    && (Utils.isBetween(toY, 1, 8))) {
                // move with numeric format. if length = 4, add a space
                int additionalPieceType = Common.PIECE_TYPE_ILLEGAL;
                if (str.length() == 5) {
                    String additionalPieceTypeStr = String.valueOf(str.charAt(4)).toUpperCase();
                    additionalPieceType = Notation.getPieceType(additionalPieceTypeStr);
                    LOGGER.fine("gkind " + Common.GAME_KIND_TEXT[game.getGameKind()] + " grules " + Common.GAME_RULES_TEXT[game.getGameRules()] +
                            " additional info string is '" + additionalPieceTypeStr + "' additional info piece is " + additionalPieceType);
                }

                // handle crazy house drop move
                if (game.isCrazyHouseOrBugHouse() && (fromX == toX) && (fromY == toY))
                {
                    // handle drop move
                    int sourcePieceType = additionalPieceType;
                    if (! game.isCrazyOrBugHouse())
                    {
                        LOGGER.warning("a drop move in non crazyhouse/bughouse game");
                        return null;
                    }
                    String destString = str.substring(str.indexOf('@') + 1, str.length());
                    if (destString.length() < 2) {
                        LOGGER.warning("could not parse move " + str);
                        return null;
                    }

                    LOGGER.fine("a drop move '" + str + "' of " + sourcePieceType + " to " + toX + "," + toY);
                    // first look for a valid drop move (if it's there its all ok)
                    Move validDropMove = game.getValidMove(toX, toY, toX, toY, sourcePieceType);
                    if (validDropMove != null)
                    {
                        // we got the drop move
                        return validDropMove;
                    }
                    Move move = game.getValidMove(toX, toY, toX, toY, Common.PIECE_TYPE_DROP_ANY);
                    if (move == null)
                    {
                        // drop move not found.
                        LOGGER.warning("can't find drop move " + str);
                        return null;
                    }
                    // drop move found. now check if piece can be dropped
                    if ((sourcePieceType == Common.PIECE_TYPE_PAWN) && ( (toX == 1) || (toX == 8)))
                    {
                        LOGGER.warning("can't find drop move " + str + " : pawn can't be dropped on 1st or 8th line");
                        return null;
                    }
                    List<Piece> droppable = game.getDroppablePieces(game.getCurrentColor());
                    Piece droppablePiece = Game.findPieceToDrop(droppable, sourcePieceType);
                    if ( droppablePiece != null )
                    {
                        game.getCurrentMoveInfo().addValidMove(droppablePiece, toX, toY, true);
                    }
                    // ok. if all is ok, we now have a valid drop move
                    validDropMove = game.getValidMove(toX, toY, toX, toY, sourcePieceType);
                    if (validDropMove == null)
                    {
                        LOGGER.warning("can't find drop move " + str);
                    }
                    return validDropMove;
                }
                return game.getValidMove(fromX, fromY, toX, toY, additionalPieceType);
            }
        }

        int color = game.getCurrentColor();

        // PGN notation notation
        int sourcePieceType;

        int sourceColumn = 0;
        int sourceRow = 0;
        int destColumn = 0;
        int destRow = 0;

        int promotionPiece = Common.PIECE_TYPE_ILLEGAL;

        // remove unwanted tokens in moves (like #,+,!,?,ep)
        str = str.replaceAll("[\\?!\\+#\\.\\$]+", "");
        str = str.replaceAll("ep", "");

        // handle promotion =
        int eqLoc = str.indexOf('=');
        if (eqLoc != -1) {
            if (str.length() != eqLoc + 2) {
                LOGGER.warning("bad move string " + str);
                return null;
            }
            String promotionPieceString = String.valueOf(str.charAt(eqLoc + 1));
            promotionPiece = Notation.getPieceType(promotionPieceString);
            if (promotionPiece == Common.PIECE_TYPE_ILLEGAL) {
                LOGGER.warning("illegal promotion piece");
                return null;
            }

            str = str.substring(0, str.length() - 2);
        }
        String moveUpper = str.toUpperCase();

        // special case 0-0, 0-0-0
        if ("O-O".equals(moveUpper)) {
            // small castle
            if (color == Common.COLOR_WHITE) {
                fromX = 1;
                fromY = (Integer) game.getAttribute(Game.KING_LOCATION);
                toX = 1;
                toY = 7;
                if (!game.isFischer()) {
                    return game.getValidMove(fromX, fromY, toX, toY, promotionPiece);
                } else {
                    // in fischer 960 use another option - point the king on the rook
                    return game.getValidMove(fromX, fromY, toX, (Integer) game.getAttribute(Game.RIGHT_ROOK_LOCATION), promotionPiece);
                }
            } else {
                fromX = 8;
                fromY = (Integer) game.getAttribute(Game.KING_LOCATION);
                toX = 8;
                toY = 7;
                if (!game.isFischer()) {
                    return game.getValidMove(fromX, fromY, toX, toY, promotionPiece);
                } else {
                    // in fischer 960 use another option - to point the king on the rook
                    return game.getValidMove(fromX, fromY, toX, (Integer) game.getAttribute(Game.RIGHT_ROOK_LOCATION), promotionPiece);
                }
            }
        }
        if ("O-O-O".equals(moveUpper)) {
            LOGGER.fine("checking castling move " + moveUpper);
            // long castle
            if (color == Common.COLOR_WHITE) {
                fromX = 1;
                fromY = (Integer) game.getAttribute(Game.KING_LOCATION);
                toX = 1;
                toY = 3;
                if (!game.isFischer()) {
                    return game.getValidMove(fromX, fromY, toX, toY, promotionPiece);
                } else {
                    // in fischer 960 use another option - to point the king on the rook
                    return game.getValidMove(fromX, fromY, toX, (Integer) game.getAttribute(Game.LEFT_ROOK_LOCATION), promotionPiece);
                }
            } else {
                fromX = 8;
                fromY = (Integer) game.getAttribute(Game.KING_LOCATION);
                toX = 8;
                toY = 3;
                if (!game.isFischer()) {
                    return game.getValidMove(fromX, fromY, toX, toY, promotionPiece);
                } else {
                    // in fischer 960 use another option - to point the king on the rook
                    return game.getValidMove(fromX, fromY, toX, (Integer) game.getAttribute(Game.LEFT_ROOK_LOCATION), promotionPiece);
                }
            }
        }

        if (str.length() < 2) {
            LOGGER.warning("illegal move '" + str + "'");
            return null;
        }
        String pieceChar = String.valueOf(str.charAt(0));

        sourcePieceType = Notation.getPieceType(String.valueOf(pieceChar));
        if (sourcePieceType == Common.PIECE_TYPE_ILLEGAL) {
            sourcePieceType = Common.PIECE_TYPE_PAWN;
        }

        if (sourcePieceType == Common.PIECE_TYPE_PAWN) {
            // first letter is source column (unless drop move)
            if (!str.contains("@")) {
                sourceColumn = getColumn(pieceChar.toLowerCase());
                if (!((sourceColumn >= 1) && (sourceColumn <= 8))) {
                    LOGGER.warning("bad source column");
                    return null;
                }
            }
        }
        MoveInfo currentInfo = game.getCurrentMoveInfo();

        if (!str.contains("x")) {
            if (!str.contains("@")) {

                // a move. not a capture nor a drop
                if (sourcePieceType == Common.PIECE_TYPE_PAWN) {
                    // a pawn advance move . second char is destination row
                    destColumn = sourceColumn;
                    destRow = str.charAt(1) - 48;
                    if (!((destRow >= 1) && (destRow <= 8))) {
                        LOGGER.warning("bad dest row");
                        return null;
                    }

                    LOGGER.fine("parsing pawn algebric move str '" + str +
                            "' from src row " + +sourceRow + " col " + sourceColumn + " to dest row " + destRow + " col " + destColumn);

                    // find a pawn on the source column that can move to destination row
                    if (color == Common.COLOR_WHITE) {
                        // find the pawn on dest row - 1
                        sourceRow = destRow - 1;
                        if (sourceRow == 0) {
                            LOGGER.warning("illegal pawn move " + str);
                            return null;
                        }
                        Piece whitePawn = game.getPieceAt(sourceRow, sourceColumn);

                        if (whitePawn == null) {
                            // if dest row is 4, source column can also be 2
                            if (destRow == 4) {
                                sourceRow = destRow - 2;
                                whitePawn = game.getPieceAt(sourceRow, sourceColumn);
                            }
                            if (whitePawn == null) {
                                LOGGER.warning("bad piece moving");
                                return null;
                            }
                        }
                        // verify its a white pawn
                        if (!whitePawn.isWhite()) {
                            LOGGER.warning("bad piece moving");
                            return null;
                        }
                        if (!whitePawn.isPawn()) {
                            LOGGER.warning("bad piece moving");
                            return null;
                        }
                        // move the white pawn
                        fromX = sourceRow;
                        fromY = sourceColumn;
                        toX = destRow;
                        toY = destColumn;
                        return game.getValidMove(fromX, fromY, toX, toY, promotionPiece);
                    } else {
                        // the same for pawn advance, black
                        // find the pawn on dest row + 1
                        sourceRow = destRow + 1;
                        if (sourceRow == 9) {
                            LOGGER.warning("illegal pawn move " + str);
                            return null;
                        }
                        Piece blackPawn = game.getPieceAt(sourceRow, sourceColumn);

                        if (blackPawn == null) {
                            // if dest row is 5, source column can also be 7
                            if (destRow == 5) {
                                sourceRow = destRow + 2;
                                blackPawn = game.getPieceAt(sourceRow, sourceColumn);
                            }
                            if (blackPawn == null) {
                                LOGGER.warning("bad piece moving");
                                return null;
                            }
                        }
                        // verify its a black pawn
                        if (!blackPawn.isPawn()) {
                            LOGGER.warning("bad piece moving");
                            return null;
                        }
                        if (!blackPawn.isBlack()) {
                            LOGGER.warning("bad piece moving");
                            return null;
                        }

                        // move the white pawn
                        fromX = sourceRow;
                        fromY = sourceColumn;
                        toX = destRow;
                        toY = destColumn;

                        LOGGER.fine("parsing pawn algebric move str '" + str +
                                "' from src row " + +sourceRow + " col " + sourceColumn + " to dest row " + destRow + " col " + destColumn);
                        return game.getValidMove(fromX, fromY, toX, toY, promotionPiece);
                    }
                } else {

                    // a piece is moving. 2 letters in the end is the destination
                    destRow = str.charAt(str.length() - 1) - 48;
                    destColumn = getColumn(str.charAt(str.length() - 2));

                    // if there are more letters they are source helpers
                    for (int index = 1; index < str.length() - 2; index++) {
                        char sourceHelp = str.charAt(index);
                        if (Character.isDigit(sourceHelp)) {
                            sourceRow = sourceHelp - 48;
                        }
                        if ((Character.isLetter(sourceHelp)) && (Character.isLowerCase(sourceHelp))) {
                            sourceColumn = getColumn(sourceHelp);
                        }
                    }
                    // find a candidate piece that can move
                    Piece srcPiece = null;
                    List<Piece> srcPieces = game.findPieces(sourcePieceType, color);

                    for (Piece piece : srcPieces) {
                        if ((sourceRow != 0) && (piece.getX() != sourceRow)) {
                            continue;
                        }
                        if ((sourceColumn != 0) && (piece.getY() != sourceColumn)) {
                            continue;
                        }
                        if (!(currentInfo.isMoveValid(piece.getX(), piece.getY(), destRow, destColumn))) {
                            continue;
                        }
                        srcPiece = piece;
                        break;
                    }

                    if (srcPiece == null) {
                        LOGGER.warning("could not parse move " + str);
                        return null;
                    }

                    LOGGER.fine("parsing regular algebric move of " + Notation.getPieceCharacter(srcPiece.getType()) + " str '" + str +
                            "' from " + srcPiece.getX() + "," + srcPiece.getY() + " to " + destRow + "," + destColumn);

                    fromX = srcPiece.getX();
                    fromY = srcPiece.getY();
                    toX = destRow;
                    toY = destColumn;
                    return game.getValidMove(fromX, fromY, toX, toY, promotionPiece);
                }
            } else {
                // a drop move destination must be a square
                if (! game.isCrazyOrBugHouse())
                {
                    LOGGER.warning("a drop move in non crazyhouse/bughouse game");
                    return null;
                }
                String destString = str.substring(str.indexOf('@') + 1, str.length());
                if (destString.length() < 2) {
                    LOGGER.warning("could not parse move " + str);
                    return null;
                }
                for (int i = 0; i < destString.length(); i++) {
                    char destInfo = destString.charAt(i);
                    if (Character.isDigit(destInfo)) {
                        destRow = destInfo - 48;
                    }
                    if ((Character.isLetter(destInfo)) && (Character.isLowerCase(destInfo))) {
                        destColumn = destInfo - 96;
                    }
                }
                LOGGER.fine("a drop move '" + str + "' of " + sourcePieceType + " to " + destRow + "," + destColumn);
                toX = destRow;
                toY = destColumn;
                // first look for a valid drop move (if it's there its all ok)
                Move validDropMove = game.getValidMove(toX, toY, toX, toY, sourcePieceType);
                if (validDropMove != null)
                {
                    // we got the drop move
                    return validDropMove;
                }
                Move move = game.getValidMove(toX, toY, toX, toY, Common.PIECE_TYPE_DROP_ANY);
                if (move == null)
                {
                    // drop move not found.
                    LOGGER.warning("can't find drop move " + str);
                    return null;
                }
                // drop move found. now check if piece can be dropped
                if ((sourcePieceType == Common.PIECE_TYPE_PAWN) && ( (toX == 1) || (toX == 8)))
                {
                    LOGGER.warning("can't find drop move " + str + " : pawn can't be dropped on 1st or 8th line");
                    return null;
                }
                List<Piece> droppable = game.getDroppablePieces(game.getCurrentColor());
                Piece droppablePiece = Game.findPieceToDrop(droppable, sourcePieceType);
                if ( droppablePiece != null )
                {
                    game.getCurrentMoveInfo().addValidMove(droppablePiece, toX, toY, true);
                }
                // ok. if all is ok, we now have a valid drop move
                validDropMove = game.getValidMove(toX, toY, toX, toY, sourcePieceType);
                if (validDropMove == null)
                {
                    LOGGER.warning("can't find drop move " + str);
                }
                return validDropMove;
            }
        } else {
            // a capture move. destination can be a piece or a pawn
            // and anyway must contain a valid square
            String destString = str.substring(str.indexOf('x') + 1, str.length());
            String sourceString = str.substring(0, str.indexOf('x'));

            if (destString.length() < 2) {
                LOGGER.warning("could not parse move " + str);
                return null;
            }
            // pieceChar = String.valueOf(destString.charAt(0));

            // try to get row/column from source/dest string
            for (int i = 0; i < sourceString.length(); i++) {
                char sourceInfo = sourceString.charAt(i);
                if (Character.isDigit(sourceInfo)) {
                    sourceRow = sourceInfo - 48;
                }
                if ((Character.isLetter(sourceInfo)) && (Character.isLowerCase(sourceInfo))) {
                    sourceColumn = getColumn(sourceInfo);
                }
            }
            for (int i = 0; i < destString.length(); i++) {
                char destInfo = destString.charAt(i);
                if (Character.isDigit(destInfo)) {
                    destRow = destInfo - 48;
                }
                if ((Character.isLetter(destInfo)) && (Character.isLowerCase(destInfo))) {
                    destColumn = getColumn(destInfo);
                }
            }

            Piece srcPiece = null;
            List<Piece> srcPieces = game.findPieces(sourcePieceType, color);
            for (Piece piece : srcPieces) {
                if ((sourceRow != 0) && (piece.getX() != sourceRow)) {
                    continue;
                }
                if ((sourceColumn != 0) && (piece.getY() != sourceColumn)) {
                    continue;
                }
                if (!currentInfo.isMoveValid(piece.getX(), piece.getY(), destRow, destColumn)) {
                    continue;
                }
                srcPiece = piece;
                break;
            }
            if (srcPiece == null) {
                LOGGER.warning("can't find moving piece type " + getPieceCharacter(sourcePieceType) + " at " + sourceRow + "," + sourceColumn + " for move '" + str + "'");
                return null;
            }

            fromX = srcPiece.getX();
            fromY = srcPiece.getY();
            toX = destRow;
            toY = destColumn;
            return game.getValidMove(fromX, fromY, toX, toY, promotionPiece);
        }
    }

    /*
     * given a game and the PGN move string, returns a new move with the given
     * string
     */
    static void getNames(Move move) {
        Utils.AssertNotNull(move);
        Game game = move.getGame();
        Utils.AssertNotNull(game);

        int fromX = move.getFromX();
        int fromY = move.getFromY();
        int toX = move.getToX();
        int toY = move.getToY();
        boolean FischerCastle = false;

        String name_num = "";

        if (move.isDropMove()) {
            name_num += (char) (toY + 96);
            name_num += (char) (toX + 48);
        } else {
            name_num += (char) (fromY + 96);
            name_num += (char) (fromX + 48);
        }

        name_num += (char) (toY + 96);
        name_num += (char) (toX + 48);

        if (move.getAdditionalPieceTypeInfo() != Common.PIECE_TYPE_ILLEGAL) {
            String promotionPieceName = Notation.getPieceCharacter(move.getAdditionalPieceTypeInfo()).toLowerCase();
            LOGGER.fine("move promotion piece is " + move.getAdditionalPieceTypeInfo() + " piece name " + promotionPieceName);
            name_num += promotionPieceName;
        } else {
            name_num += " ";
        }

        move.setNameNum(name_num);

        MoveInfo info = game.getCurrentMoveInfo();
        Utils.AssertNotNull(info);

        if (Utils.isEmptyString(move.getNameAlg())) {

            String name_alg = "";
            String name_fig = "";

            Piece piece = move.getMovedPiece();
            Utils.AssertNotNull(piece);


            if (move.isDropMove()) {
                if (piece.getType() != Common.PIECE_TYPE_DROP_ANY) {
                    // special names for drop move
                    name_alg += getPieceCharacter(piece.getType());
                    if (piece.isWhite()) {
                        name_fig += sPieceToUnicodeFiguresWhite.get(piece.getType());
                    } else {
                        name_fig += sPieceToUnicodeFiguresBlack.get(piece.getType());
                    }

                    name_alg += "@";
                    name_fig += "@";
                    name_alg += sColNamesEng.get(toY);
                    name_alg += sRowNames[toX];
                    name_fig += sColNamesEng.get(toY);
                    name_fig += sRowNames[toX];
                }
            } else {
                Piece destPiece = move.getCapturedPiece();
                String captureEng = "";
                if (destPiece != null) {
                    captureEng = "x";
                    if (destPiece.isRook() && piece.isKing() && destPiece.isColor(piece.getColor())) {
                        FischerCastle = true;
                    }
                }

                if (piece.isPawn()) {

                    name_alg += sColNamesEng.get(fromY);
                    if (piece.isWhite()) {
                        name_fig += sPieceToUnicodeFiguresWhite.get(piece.getType());
                    } else {
                        name_fig += sPieceToUnicodeFiguresBlack.get(piece.getType());
                    }

                    name_fig += sColNamesEng.get(fromY);

                    // pawn move, not capture
                    if (destPiece == null) {
                        name_alg += sRowNames[toX];
                        name_fig += sRowNames[toX];
                    } else {
                        // pawn capture
                        name_alg += captureEng;
                        name_fig += captureEng;
                        name_alg += sColNamesEng.get(toY);
                        name_fig += sColNamesEng.get(toY);
                        name_alg += sRowNames[toX];
                        name_fig += sRowNames[toX];
                    }
                    // handle promotion
                    if ((toX == 8) || (toX == 1)) {
                        LOGGER.fine("move promotion piece is " + move.getAdditionalPieceTypeInfo());
                        name_num += Notation.getPieceCharacter(move.getAdditionalPieceTypeInfo());
                        name_alg += '=' + Notation.getPieceCharacter(move.getAdditionalPieceTypeInfo());
                        if (piece.isWhite()) {
                            name_fig += '=' + sPieceToUnicodeFiguresWhite.get(move.getAdditionalPieceTypeInfo());
                        } else {
                            name_fig += '=' + sPieceToUnicodeFiguresBlack.get(move.getAdditionalPieceTypeInfo());
                        }
                    }
                } else {
                    // piece move
                    name_alg += getPieceCharacter(piece.getType());
                    if (piece.isWhite()) {
                        name_fig += sPieceToUnicodeFiguresWhite.get(piece.getType());
                    } else {
                        name_fig += sPieceToUnicodeFiguresBlack.get(piece.getType());
                    }

                    // verify if helpers needed
                    String h1 = "";
                    String h2 = "";

                    LOGGER.fine("name_alg is '" + name_alg + "'");

                    List<Piece> others = game.findPieces(piece.getType(), piece.getColor());

                    LOGGER.fine("others size " + others.size());

                    for (Piece other : others) {
                        if ((other.getX() != piece.getX()) || (other.getY() != piece.getY())) {
                            LOGGER.fine("found helper piece " + other.getType() + " color " + other.getColor());
                            if (info.isMoveValid(other.getX(), other.getY(), toX, toY)) {
                                if (other.getY() != fromY) {
                                    // add column helper
                                    if (Utils.isEmptyString(h1)) {
                                        h1 += sColNamesEng.get(fromY);
                                    }
                                } else {
                                    // add row helper
                                    if (Utils.isEmptyString(h2)) {
                                        h2 += sRowNames[fromX];
                                    }
                                }
                            }
                        }
                    }

                    name_alg += (h1 + h2);
                    name_fig += (h1 + h2);

                    name_alg += captureEng;
                    name_fig += captureEng;
                    name_alg += sColNamesEng.get(toY);
                    name_fig += sColNamesEng.get(toY);
                    name_alg += sRowNames[toX];
                    name_fig += sRowNames[toX];
                    LOGGER.fine("name_alg is '" + name_alg + "'");

                    // check for castle
                    if (piece.isKing()) {
                        int dist = move.getFromY() - move.getToY();
                        if (dist > 1 || (FischerCastle && dist == 1)) {
                            name_alg = "O-O-O";
                            name_fig = "O-O-O";
                        }
                        if (dist < -1 || (FischerCastle && dist == -1)) {
                            name_alg = "O-O";
                            name_fig = "O-O";
                        }
                    }
                }
            }

            LOGGER.fine("move names are '" + name_alg + "' , '" + name_fig);
            move.setNameAlg(name_alg);
            move.setNameFig(name_fig);
        }

        // suffix is null and this is the actual move played, then we can
        // compute the suffix
        if (Utils.isEmptyString(move.getNameAlgSuffix())) {
            MoveInfo nextInfo = game.getMoveInfo(move.getMoveNumber() + 1);
            if (nextInfo != null) {
                if (move.getNameNum().equals(game.getMove(move.getMoveNumber()).getNameNum())) {
                    if (nextInfo.isCheckMate()) {
                        move.setNameAlgSuffix("#");
                        LOGGER.fine("renaming name of move to " + move.getNameAlg());
                    } else if (nextInfo.isFloatCheck()) {
                        move.setNameAlgSuffix("++");
                        LOGGER.fine("renaming name of move to " + move.getNameAlg());
                    } else if (nextInfo.isCheck()) {
                        move.setNameAlgSuffix("+");
                        LOGGER.fine("renaming name of move to " + move.getNameAlg());
                    }
                }
            }
        }
    }

    /*
     * calculates the algebraic and numeric names of a move
     */
    static String getSquareEng(int x, int y) {
        return sColNamesEng.get(y) + sRowNames[x];
    }

    static boolean playMoveList(Game game, String movelist) {
        return playMoveList(game, movelist, 0);
    }

    static boolean playMoveList(Game game, String movelist, int toMove) {


        Utils.AssertNotNull(game);

        LOGGER.fine("playing move list " + movelist);

        if (movelist == null) {

            return false;
        }

        // play the moves on an empty game
        // remove all kinds of spaces, convert with " "
        movelist = movelist.replaceAll("[\\s+]", " ");

        movelist = movelist.replace(".", ". ");
        movelist = movelist.replace("{", " { ");
        movelist = movelist.replace("}", " } ");

        StringTokenizer st = new StringTokenizer(movelist, " ");
        while (st.hasMoreTokens()) {

            if ((game.getCurrentMove() >= toMove) && (toMove != 0)) {
                // no need to play move moves
                break;
            }
            String tok = st.nextToken();

            tok = tok.trim();

            if ("{".equals(tok)) {
                String comment = "";
                tok = st.nextToken();
                do {
                    if (!st.hasMoreTokens()) {
                        LOGGER.warning("end of pgn in the middle of comment");
                        return false;
                    }
                    comment += tok + " ";
                    tok = st.nextToken();
                } while (!("}".equals(tok)));

                // add comment for current move
                Move move = game.getLastMove();
                if (move != null) {
                    move.setComment(comment);
                }

                continue;
            }

            if (game.isEnded()) {
                LOGGER.info("game ended. ignoring the rest of the move list");
                break;
            }

            // remove unwanted tokens in moves (like #,+,!,?,ep)
            tok = tok.replaceAll("[\\?!\\+#\\.\\$]+", "");
            tok = tok.replaceAll("ep", "");

            if (Utils.isEmptyString(tok)) {
                continue;
            }

            // filter out tokens that does not begin with a letter
            if (!Character.isLetter(tok.charAt(0))) {
                continue;
            }

            if (!game.playMove(tok)) {
                LOGGER.warning("invalid move token '" + tok + "'");

                return false;
            }
        }

        return true;
    }

    public static Game playMoveList(String movelist) {
        Game game = new Game();
        if (!playMoveList(game, movelist)) {
            return null;
        }
        return game;
    }
}
