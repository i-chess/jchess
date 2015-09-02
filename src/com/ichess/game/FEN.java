//==============================================================================
//            Copyright (c) 2009-2014 ichess.co.il
//
//This document contains confidential information which is protected by
//copyright and is proprietary to ichess.co.il. No part
//of this document may be used, copied, disclosed, or conveyed to another
//party without prior written consent of ichess.co.il.
//==============================================================================

package com.ichess.game;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Used to import and export games from the FEN notation. The FEN specification
 * is used to record a status of a game, so it can be continued at a later time.
 *
 * @author Ran Berenfeld
 * @version 1.0
 * @see <a href="http://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation">FEN notation specification.</a>
 */
public class FEN {

    private final static Logger LOGGER = Logger.getLogger(FEN.class.getName());

    public static final String FEN_COLOR_BLACK = "b";
    public static final String FEN_COLOR_WHITE = "w";

    public static final String FEN_EMPTY_POS = "8/8/8/8/8/8/8/8";

    public static final String FEN_EMPTY_POS_BLACK = "8/8/8/8/8/8/8/8 b";
    public static final String FEN_EMPTY_POS_WHITE = "8/8/8/8/8/8/8/8 w";
    /**
     * FEN value of initial position.
     */
    public static final String FEN_INITIAL_POS = Common.STD_INIT_FEN;
    public static final String FEN_GRASSHOPER_POS = "rnbqkbnr/gggggggg/pppppppp/8/8/PPPPPPPP/GGGGGGGG/RNBQKBNR w KQkq - 0 1";
    public static final String FEN_MINICAPA_POS = "rabqkbcr/pppppppp/8/8/8/8/PPPPPPPP/RABQKBCR w KQkq - 0 1";
    public static final String FEN_CRAZYHOUSE_POS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR[-] w KQkq - 0 1";

    // if true will encode last row captured pieces in crazyhouse (XFEN)
    // by default encode in Droppable pieces in [] like winboard format for crazyhouse / bughouse
    public static boolean ENCODE_CAPTURED_AS_XFEN = false;

    /**
     * Returns the FEN position of the given Game. The FEN position is the first
     * token of the FEN string, containing only the pieces position.
     *
     * @param game the game whose position is returned
     * @return The FEN position of the given Game. null on error.
     */
    public static String getFENPosition(Game game) {
        if (game == null) {
            LOGGER.warning("game == null");
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (int row = 8; row > 0; row--) {
            int emptyCount = 0;
            for (int col = 1; col < 9; col++) {
                Piece piece = game.getPieceAt(row, col);
                if (piece == null) {
                    emptyCount++;
                    continue;
                }
                if (emptyCount > 0) {
                    result.append(emptyCount);
                    emptyCount = 0;
                }
                String pieceStr = Notation.getPieceCharacter(piece.getType());
                if (piece.isBlack()) {
                    pieceStr = pieceStr.toLowerCase();
                }
                result.append(pieceStr);
                if ((game.isCrazyOrBugHouse()) && (piece.isPromoted()))
                {
                    result.append('~');
                }
            }
            if (emptyCount > 0) {
                result.append(emptyCount);
            }
            if (row != 1) {
                result.append('/');
            }
        }
        if (game.isCrazyOrBugHouse())
        {
            if (ENCODE_CAPTURED_AS_XFEN)
            {
                // add crazyhouse captured pieces
                if (game.isCrazyHouse())
                {
                    if (game.hasCapturedPieces()) {
                        result.append('/');
                        for (Piece piece : game.getCapturedPieces(Common.COLOR_WHITE)) {
                            String pieceChar = Notation.getPieceCharacter(piece.getType());
                            result.append(pieceChar.toUpperCase());
                        }
                        for (Piece piece : game.getCapturedPieces(Common.COLOR_BLACK)) {
                            String pieceChar = Notation.getPieceCharacter(piece.getType());
                            result.append(pieceChar.toLowerCase());
                        }
                    }
                }
            }
            else {
                // add crazyhouse / bughouse holding
                result.append('[');
                boolean hasDroppablePieces = false;
                for (Piece piece : game.getDroppablePieces(Common.COLOR_WHITE)) {
                    String pieceChar = Notation.getPieceCharacter(piece.getTypeWhenDropping());
                    result.append(pieceChar);
                    hasDroppablePieces = true;
                }
                for (Piece piece : game.getDroppablePieces(Common.COLOR_BLACK)) {
                    String pieceChar = Notation.getPieceCharacter(piece.getTypeWhenDropping());
                    result.append(pieceChar.toLowerCase());
                    hasDroppablePieces = true;
                }
                if (!hasDroppablePieces) {
                    result.append("-");
                }
                result.append("]");
            }
        }
        return result.toString();
    }

    /**
     * Returns the FEN representation of the given Game.
     *
     * @param game the game whose FEN representation is returned
     * @return The FEN representation of the given Game. null on error.
     */
    public static String getFENString(Game game) {
        if (game == null) {
            LOGGER.warning("game == null");
            return null;
        }

        String _position = getFENPosition(game);

        StringBuilder fen = new StringBuilder();

        fen.append(_position);
        fen.append(" ");

        switch (game.getCurrentColor()) {
            case Common.COLOR_WHITE:
                fen.append(FEN_COLOR_WHITE);
                break;
            case Common.COLOR_BLACK:
                fen.append(FEN_COLOR_BLACK);
                break;
            default:
                LOGGER.warning("illegal game color");
                return null;
        }

        // append castling availability
        fen.append(" ");

        boolean[][] _castling = new boolean[Common.COLOR_NUM][Common.CASTLE_NUM];

        for (int color = Common.COLOR_START; color < Common.COLOR_NUM; color++) {
            for (int cstl = Common.CASTLE_START; cstl < Common.CASTLE_NUM; cstl++) {
                if (Common.isBlackOrWhite(color)) {
                    _castling[color][cstl] = game.getCastlingAvailability(color, cstl);
                }
            }
        }

        boolean canCastle = false;
        char right = 'k';
        char left = 'q';
        if (game.isFischer()) {
            right = (char) ((int) 'a' + (Integer) game.getAttribute(Game.RIGHT_ROOK_LOCATION) - 1);
            left = (char) ((int) 'a' + (Integer) game.getAttribute(Game.LEFT_ROOK_LOCATION) - 1);
        }
        if (_castling[Common.COLOR_WHITE][Common.CASTLE_KING]) {
            fen.append(Character.toUpperCase(right));
            canCastle = true;
        }
        if (_castling[Common.COLOR_WHITE][Common.CASTLE_QUEEN]) {
            fen.append(Character.toUpperCase(left));
            canCastle = true;
        }
        if (_castling[Common.COLOR_BLACK][Common.CASTLE_KING]) {
            fen.append(right);
            canCastle = true;
        }
        if (_castling[Common.COLOR_BLACK][Common.CASTLE_QUEEN]) {
            fen.append(left);
            canCastle = true;
        }
        if (!canCastle) {
            fen.append("-");
        }

        // append ep move

        fen.append(" ");

        Pawn epPawn = game.getEpPawn();
        if (epPawn != null) {
            boolean ep_capture_pawn_found = false;
            // also check if opponent pawn can make an en passent capture
            int epY = epPawn.getY();
            int epX = epPawn.getX();
            if (epY > 1) {
                Piece capturePawn = game.getPieceAt(epX, epY - 1);
                if (capturePawn != null) {
                    if (capturePawn.isPawn()) {
                        if (capturePawn.isColor(game.getCurrentColor())) {
                            // found
                            ep_capture_pawn_found = true;
                        }
                    }
                }
            }
            if (epY < 8) {
                Piece capturePawn = game.getPieceAt(epX, epY + 1);
                if (capturePawn != null) {
                    if (capturePawn.isPawn()) {
                        if (capturePawn.isColor(game.getCurrentColor())) {
                            // found
                            ep_capture_pawn_found = true;
                        }
                    }
                }
            }
            if (ep_capture_pawn_found) {
                if (epPawn.isWhite()) {
                    fen.append(Notation.getSquareEng(epPawn.getX() - 1, epPawn.getY()));
                } else {
                    fen.append(Notation.getSquareEng(epPawn.getX() + 1, epPawn.getY()));
                }
            } else {
                fen.append("-");
            }
        } else {
            fen.append("-");
        }

        // append half moves

        fen.append(" ");

        // half moves
        MoveInfo currentInfo = game.getCurrentMoveInfo();
        if (currentInfo != null) {
            fen.append(currentInfo.getDraw50MovesCount());
        } else {
            fen.append("0");
        }

        // append full moves

        fen.append(" ");

        fen.append(game.getMoveNumber());

        return fen.toString();
    }

    /**
     * Loads the given FEN string into the given game. All the game moves are
     * taken back. If startGame is false, then the FEN may be an invalid game
     * position.
     *
     * @param fen       - The FEN string to load.
     * @param startGame - If true, FEN position is validated as a valid chess
     *                  position.
     * @return true on success. otherwise false.
     */
    public static Game loadGame(String fen, boolean startGame, int gkind) {
        Utils.AssertNotNull(fen);
        Game game = new Game(gkind);

        // try to guess game kind and game rules if needed
        int grules = 0;
        if (gkind != 0) {
            // gkind is given. so we can deduct game rules
            switch (gkind) {
                case Common.GAME_KIND_FISCHER:
                    grules = Common.GAME_RULES_FISCHER;
                    break;
                case Common.GAME_KIND_SUICIDE:
                    grules = Common.GAME_RULES_SUICIDE;
                    break;
                case Common.GAME_KIND_CRAZY_HOUSE:
                case Common.GAME_KIND_BUG_HOUSE:
                    grules = Common.GAME_RULES_CRAZY_HOUSE;
                    break;
                case Common.GAME_KIND_FREEPLAY:
                    grules = Common.GAME_RULES_FREEPLAY;
                    break;
                case Common.GAME_KIND_REGULAR:
                case Common.GAME_KIND_GRASSHOPER:
                case Common.GAME_KIND_MINICAPA:
                    grules = Common.GAME_RULES_REGULAR;
                    break;
            }
        }

        LOGGER.fine("loading fen '" + fen + "' start " + startGame + " gkind " + Common.GAME_KIND_TEXT[gkind] + " grules " +
                Common.GAME_RULES_TEXT[grules]);

        game.takebackAllMoves();
        game.clearBoard();

        fen = fen.trim();
        String[] tokens = fen.split("\\s+");
        ArrayList<String> toks = new ArrayList<String>();
        for (String tok : tokens) {
            if (!Utils.isEmptyString(tok)) {
                toks.add(tok);
            }
        }
        if (toks.size() < 1) {
            LOGGER.warning("illegal FEN '" + fen + "'");
            return null;
        }

        String position = toks.get(0);

        String currentColor = FEN.FEN_COLOR_WHITE;
        if (toks.size() > 1) {
            currentColor = toks.get(1);
        }

        String castling = "KQkq";
        if (toks.size() > 2) {
            castling = toks.get(2);
        }

        String epMoveStr = "-";
        if (toks.size() > 3) {
            epMoveStr = toks.get(3);
        }

        int draw50MovesRuleCount = 0;
        if (toks.size() > 4) {
            draw50MovesRuleCount = Utils.parseInt(toks.get(4));
        }

        String moveNumberStr = "1";
        if (toks.size() > 5) {
            moveNumberStr = toks.get(5);
        }

        if (!loadPositionToGame(game, position)) {
            LOGGER.warning("Failed to load positiong from fen '" + fen + "'");
            return null;
        }

        // reload gkind, grules (maybe deducted from position)
        gkind = game.getGameKind();
        grules = game.getGameRules();

        List<Piece> whiteKings = game.findPieces(Common.PIECE_TYPE_KING, Common.COLOR_WHITE);
        List<Piece> blackKings = game.findPieces(Common.PIECE_TYPE_KING, Common.COLOR_BLACK);
        // now try to guess game kind and rules if needed
        if (gkind == 0) {
            if ((whiteKings.size() != 1) || (blackKings.size() != 1)) {
                // no king / too many kings. must be freeplay
                gkind = Common.GAME_KIND_FREEPLAY;
                grules = Common.GAME_RULES_FREEPLAY;
            } else if (!game.findPieces(Common.PIECE_TYPE_GRASSHOPER, 0).isEmpty()) {
                gkind = Common.GAME_KIND_GRASSHOPER;
                grules = Common.GAME_RULES_REGULAR;
            } else if (!game.findPieces(Common.PIECE_TYPE_ARCHBISHOP, 0).isEmpty() ||
                    !game.findPieces(Common.PIECE_TYPE_CHANCELLOR, 0).isEmpty()) {
                gkind = Common.GAME_KIND_MINICAPA;
                grules = Common.GAME_RULES_REGULAR;
            } else {
                gkind = Common.GAME_KIND_REGULAR;
                grules = Common.GAME_RULES_REGULAR;
            }
        } else {
            if ((whiteKings.size() != 1) || (blackKings.size() != 1)) {
                if (gkind == 0) {
                    gkind = Common.GAME_KIND_FISCHER;
                    grules = Common.GAME_RULES_FREEPLAY;
                    game.setGameRules(grules);
                }
                if ((gkind != Common.GAME_RULES_FREEPLAY) && (gkind != Common.GAME_RULES_SUICIDE)) {
                    LOGGER.warning("game kind mismatch fen '" + fen + "' gkind " + Common.GAME_KIND_TEXT[gkind]);
                    return null;
                }
            }
        }


        Piece whiteKing = (Piece) Utils.getFirstInList(whiteKings);
        Piece blackKing = (Piece) Utils.getFirstInList(blackKings);

        if (FEN_COLOR_WHITE.equals(currentColor)) {
            game.setStartingColor(Common.COLOR_WHITE);
        } else if (FEN_COLOR_BLACK.equals(currentColor)) {
            game.setStartingColor(Common.COLOR_BLACK);
        } else {
            LOGGER.warning("illegal FEN '" + fen + "' bad color " + currentColor);
            return null;
        }

        boolean canCastle[][] = new boolean[Common.COLOR_NUM][Common.CASTLE_NUM];

        // handle fischer style castling letters
        int kingLoc = 5;
        int LeftRook = 1;
        int RightRook = 8;
        if (!(castling.equals("-") || castling.contains("k") || castling.contains("K") || castling.contains("Q") || castling.contains("q"))) {
            if (blackKing != null) {
                kingLoc = blackKing.getY();
            }
            LeftRook = 0; // if not defined in castling
            RightRook = 0;
            for (byte pieceB : castling.getBytes()) {
                char pieceCh = (char) pieceB;
                int p = pieceCh - 'a' + 1;
                if (p < 0) { // white can castle
                    p = pieceCh - 'A' + 1;
                    kingLoc = whiteKing.getY();
                }
                if (p > kingLoc) {
                    RightRook = p;
                } else {
                    LeftRook = p;
                }
            }
        }
        LOGGER.fine(LeftRook + "," + RightRook);
        if (LeftRook == 0) {
            LeftRook = kingLoc;
        }
        if (RightRook == 0) {
            RightRook = kingLoc;
        }
        if (LeftRook > 8 || RightRook > 8) {
            LOGGER.warning("Error in Fischer 960 FEN casteling");
            castling = "-";
            LeftRook = kingLoc;
            RightRook = kingLoc;
        }
        game.setAttribute(Game.KING_LOCATION, kingLoc);
        game.setAttribute(Game.LEFT_ROOK_LOCATION, LeftRook);
        game.setAttribute(Game.RIGHT_ROOK_LOCATION, RightRook);
        LOGGER.fine("Game rooks location right " + RightRook + " king " + kingLoc + " left " + LeftRook);

        Piece maybeWhiteKing = game.getPieceAt(1, kingLoc);
        if (maybeWhiteKing != null) {
            if (maybeWhiteKing.isKing() && maybeWhiteKing.isWhite()) {
                // white king in place. check rooks
                Piece rook1 = game.getPieceAt(1, RightRook);
                if (rook1 != null) {
                    if (rook1.isRook() && rook1.isWhite()) {
                        canCastle[Common.COLOR_WHITE][Common.CASTLE_KING] = true;
                    }
                }
                Piece rook2 = game.getPieceAt(1, LeftRook);
                if (rook2 != null) {
                    if (rook2.isRook() && rook2.isWhite()) {
                        canCastle[Common.COLOR_WHITE][Common.CASTLE_QUEEN] = true;
                    }
                }
            }
        }
        Piece maybeBlackKing = game.getPieceAt(8, kingLoc);
        if (maybeBlackKing != null) {
            if (maybeBlackKing.isKing() && maybeBlackKing.isBlack()) {
                // white king in place. check rooks
                Piece rook1 = game.getPieceAt(8, RightRook);
                if (rook1 != null) {
                    if (rook1.isRook() && rook1.isBlack()) {
                        canCastle[Common.COLOR_BLACK][Common.CASTLE_KING] = true;
                    }
                }
                Piece rook2 = game.getPieceAt(8, LeftRook);
                if (rook2 != null) {
                    if (rook2.isRook() && rook2.isBlack()) {
                        canCastle[Common.COLOR_BLACK][Common.CASTLE_QUEEN] = true;
                    }
                }
            }
        }
        game.setCastlingAvailability(Common.COLOR_WHITE, Common.CASTLE_KING, false);
        game.setCastlingAvailability(Common.COLOR_WHITE, Common.CASTLE_QUEEN, false);
        game.setCastlingAvailability(Common.COLOR_BLACK, Common.CASTLE_KING, false);
        game.setCastlingAvailability(Common.COLOR_BLACK, Common.CASTLE_QUEEN, false);
        LOGGER.fine("castling bytes " + castling + " can castle "
                + canCastle[Common.COLOR_WHITE][Common.CASTLE_KING]
                + canCastle[Common.COLOR_WHITE][Common.CASTLE_QUEEN]
                + canCastle[Common.COLOR_BLACK][Common.CASTLE_KING]
                + canCastle[Common.COLOR_BLACK][Common.CASTLE_QUEEN]);
        for (byte castleB : castling.getBytes()) {

            char castleCh = (char) castleB;
            switch (castleCh) {
                case 'K':
                    if (canCastle[Common.COLOR_WHITE][Common.CASTLE_KING]) {
                        game.setCastlingAvailability(Common.COLOR_WHITE, Common.CASTLE_KING, true);
                    }
                    break;
                case 'Q':
                    if (canCastle[Common.COLOR_WHITE][Common.CASTLE_QUEEN]) {
                        game.setCastlingAvailability(Common.COLOR_WHITE, Common.CASTLE_QUEEN, true);
                    }
                    break;
                case 'k':
                    if (canCastle[Common.COLOR_BLACK][Common.CASTLE_KING]) {
                        game.setCastlingAvailability(Common.COLOR_BLACK, Common.CASTLE_KING, true);
                    }
                    break;
                case 'q':
                    if (canCastle[Common.COLOR_BLACK][Common.CASTLE_QUEEN]) {
                        game.setCastlingAvailability(Common.COLOR_BLACK, Common.CASTLE_QUEEN, true);
                    }
                    break;
                case '-':
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                    LOGGER.fine("fischer castling detected. grules " + Common.GAME_RULES_TEXT[grules]);
                    if ((grules == 0) || (grules == Common.GAME_RULES_REGULAR)) {
                        gkind = Common.GAME_KIND_FISCHER;
                        grules = Common.GAME_RULES_FISCHER;
                        game.setGameRules(grules);
                    }
                    if (grules != Common.GAME_RULES_FISCHER) {
                        LOGGER.warning("game kind mismatch fen '" + fen + "' gkind " + Common.GAME_KIND_TEXT[gkind]);
                        return null;
                    }
                    if (((int) castleCh) - 'a' + 1 > kingLoc) {
                        if (canCastle[Common.COLOR_BLACK][Common.CASTLE_KING]) {
                            game.setCastlingAvailability(Common.COLOR_BLACK, Common.CASTLE_KING, true);
                        }
                    } else {
                        if (canCastle[Common.COLOR_BLACK][Common.CASTLE_QUEEN]) {
                            game.setCastlingAvailability(Common.COLOR_BLACK, Common.CASTLE_QUEEN, true);
                        }
                    }
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                    if ((grules == 0) || (grules == Common.GAME_RULES_REGULAR)) {
                        gkind = Common.GAME_KIND_FISCHER;
                        grules = Common.GAME_RULES_FISCHER;
                        game.setGameRules(grules);
                    }
                    if (grules != Common.GAME_RULES_FISCHER) {
                        LOGGER.warning("game kind mismatch fen '" + fen + "' gkind " + Common.GAME_KIND_TEXT[gkind]);
                        return null;
                    }
                    if (((int) castleCh) - 'A' + 1 > kingLoc) {
                        if (canCastle[Common.COLOR_WHITE][Common.CASTLE_KING]) {
                            game.setCastlingAvailability(Common.COLOR_WHITE, Common.CASTLE_KING, true);
                        }
                    } else {
                        if (canCastle[Common.COLOR_WHITE][Common.CASTLE_QUEEN]) {
                            game.setCastlingAvailability(Common.COLOR_WHITE, Common.CASTLE_QUEEN, true);
                        }
                    }
                    break;
                default:
                    LOGGER.warning("illegal FEN '" + fen + "' bad castling string " + castling);
                    return null;
            }
        }

        if (gkind == 0) {
            // set game kind according to the resulted game rules
            switch (grules) {
                case Common.GAME_RULES_FISCHER:
                    game.setGameKind(Common.GAME_KIND_FISCHER);
                    break;
                case Common.GAME_RULES_SUICIDE:
                    game.setGameKind(Common.GAME_KIND_SUICIDE);
                    break;
                case Common.GAME_RULES_FREEPLAY:
                    game.setGameKind(Common.GAME_KIND_FREEPLAY);
                    break;
                case Common.GAME_RULES_CRAZY_HOUSE:
                    game.setGameKind(Common.GAME_KIND_CRAZY_HOUSE);
                    break;
                default:
                    game.setGameKind(Common.GAME_KIND_REGULAR);
                    break;
            }
        }

        LOGGER.fine("loaded fen '" + fen + "' gkind " + Common.GAME_KIND_TEXT[gkind] + " grules " + Common.GAME_RULES_TEXT[grules]);
        game.setGameKind(gkind);
        game.setGameRules(grules);

		/*
         * if FEN contains EP move, add it to the game
		 */
        if (!("-".equals(epMoveStr))) {
            do {
                if (epMoveStr.length() != 2) {
                    LOGGER.warning("bad ep pawn move : " + epMoveStr);
                    break;
                }
                int epX = epMoveStr.charAt(1) - 48;
                int epY = Notation.getColumn(epMoveStr.charAt(0));
                if ((epY < 1) || (epY > 8) || (epX < 1) || (epX > 8)) {
                    LOGGER.warning("bad ep pawn move : " + epMoveStr);
                    break;
                }

                int captureX = 0;
                Piece epPawn = null;
                if ((game.getCurrentColor() == Common.COLOR_WHITE) && (epX == 6)) {
                    // black EP pawn
                    captureX = epX - 1;
                    epPawn = game.getPieceAt(captureX, epY);
                    if ((game.getPieceAt(epX, epY) != null) || (game.getPieceAt(epX + 1, epY) != null)) {
                        LOGGER.warning("bad ep pawn move : " + epMoveStr);
                        break;
                    }
                } else if ((game.getCurrentColor() == Common.COLOR_BLACK) && (epX == 3)) {
                    // white EP pawn
                    captureX = epX + 1;
                    epPawn = game.getPieceAt(captureX, epY);
                    if ((game.getPieceAt(epX, epY) != null) || (game.getPieceAt(epX - 1, epY) != null)) {
                        LOGGER.warning("bad ep pawn move : " + epMoveStr);
                        break;
                    }
                } else {
                    LOGGER.info("bad ep pawn move : " + epMoveStr);
                    break;
                }
                if (epPawn == null) {
                    LOGGER.info("bad ep pawn move : " + epMoveStr);
                    break;
                }
                if ((!epPawn.isPawn()) || (epPawn.getColor() == game.getCurrentColor())) {
                    LOGGER.info("bad ep pawn move : " + epMoveStr);
                    break;
                }
                // look for capturing pawns
                Piece capturingPawn = null;
                if (epY > 1) {
                    capturingPawn = game.getPieceAt(captureX, epY - 1);
                    if (capturingPawn != null) {
                        if ((capturingPawn.isPawn()) && (capturingPawn.isColor(game.getCurrentColor()))) {
                            // found a capturing pawn !
                            game.setEpPawn((Pawn) epPawn);
                        }
                    }
                }
                if (epY < 8) {
                    capturingPawn = game.getPieceAt(captureX, epY + 1);
                    if (capturingPawn != null) {
                        if ((capturingPawn.isPawn()) && (capturingPawn.isColor(game.getCurrentColor()))) {
                            // found a capturing pawn !
                            game.setEpPawn((Pawn) epPawn);
                        }
                    }
                }
            } while (false);
        }

        int _moveNumber = Utils.parseInt(moveNumberStr);

        if (_moveNumber < 1) {
            _moveNumber = 1;
        }

        game.setMoveNumber(_moveNumber);
        game.setAttribute(Game.INITIAL_POSITION_FEN, fen);

        MoveInfo moveInfo = null;
        if (startGame) {
            moveInfo = game.getCurrentMoveInfo();
            if (null == moveInfo) {
                LOGGER.warning("illegal FEN '" + fen + "' gkind " + Common.GAME_KIND_TEXT[gkind] + " game failed first analyse");
                return null;
            }
            moveInfo.setDraw50MovesCount(draw50MovesRuleCount);
        }
        return game;
    }

    /**
     * Loads the given FEN string into a new game instance.
     *
     * @param fen a FEN string.
     * @return a new game instance.
     */
    public static Game loadGame(String fen) {
        return loadGame(fen, Common.GAME_KIND_REGULAR);
    }

    /**
     * Loads the given FEN string into a new game instance. If startGame is
     * false, then the FEN may be an invalid game position.
     *
     * @param fen       a FEN string.
     * @param startGame - If true, FEN position is validated as a valid chess
     *                  position.
     * @return a new game instance. null if fails.
     */
    public static Game loadGame(String fen, boolean startGame) {
        return loadGame(fen, startGame, 0);
    }

    /**
     * Loads the given FEN string into a new game instance.
     *
     * @param fen   - a FEN string
     * @param gkind - a game kind
     * @return - a new game instance. null if fails.
     */
    public static Game loadGame(String fen, int gkind) {
        return loadGame(fen, true, gkind);
    }

    /**
     * Load only the position of a given FEN string (or even only the position part of it)
     * into a game
     *
     * @param game        - a given game
     * @param position - a given FEN position
     * @return - true iff the position is valid and was loaded to the game
     */
    public static boolean loadPositionToGame(Game game, String position) {
        Utils.AssertNotNull(game);
        String[] tokens = position.split("\\s+");
        position = tokens[0];

        // can be both position or a FEN. so we just spilit
        LOGGER.fine("loading position '" + position + "'");
        // set the pieces
        int gkind = game.getGameKind();
        int grules = game.getGameRules();
        String rows[] = position.split("[/\\[\\]]");
        if (rows.length < 8) {
            LOGGER.info("illegal FEN " + position);
            return false;
        }
        int currentRow = 8;
        for (String row : rows) {
            if (currentRow == 0) {
            {
                if ((gkind == Common.GAME_KIND_CRAZY_HOUSE) || (gkind == Common.GAME_KIND_BUG_HOUSE) || (gkind == 0))
                {
                    if (gkind == 0)
                    {
                        gkind = Common.GAME_KIND_CRAZY_HOUSE;
                        grules = Common.GAME_RULES_CRAZY_HOUSE;
                        game.setGameKind(gkind);
                        game.setGameRules(grules);
                    }
                    // this is a list of droppable pieces for crazy house or bug house
                    // XFEN mode is encoding captured pieces as "line 9"
                    boolean xfen = (position.indexOf('[') == -1);
                    List<Piece> capturedOrDroppableWhite = new ArrayList<Piece>();
                    List<Piece> capturedOrDroppableBlack = new ArrayList<Piece>();
                    Piece newPiece;
                    for (byte pieceB : row.getBytes()) {
                        char pieceCh = (char) pieceB;
                        if (pieceCh == '-')
                        {
                            break;
                        }
                        int color = Common.COLOR_WHITE;
                        if (Character.isLowerCase(pieceCh)) {
                            color = Common.COLOR_BLACK;
                        }
                        if (game.isCrazyHouse() && (!xfen))
                        {
                            color = Common.OtherColor(color);
                        }
                        pieceCh = Character.toUpperCase(pieceCh);
                        Integer type = Notation.getPieceType(String.valueOf(pieceCh));
                        if ((type == null) || (type == Common.PIECE_TYPE_ILLEGAL)) {
                            LOGGER.warning("illegal FEN '" + position + "' bad piece '" + pieceCh + "'");
                            return false;
                        }
                        newPiece = Piece.create(type, color);
                        if (newPiece.isWhite())
                        {
                            capturedOrDroppableWhite.add(newPiece);
                        }
                        else
                        {
                            capturedOrDroppableBlack.add(newPiece);
                        }
                    }
                    if (xfen)
                    {
                        game.setCapturedPieces(Common.COLOR_WHITE, capturedOrDroppableWhite);
                        game.setCapturedPieces(Common.COLOR_BLACK, capturedOrDroppableBlack);
                    }
                    else {
                        if (game.isCrazyHouse())
                        {
                            game.setDroppablePieces(Common.COLOR_BLACK, capturedOrDroppableWhite);
                            game.setDroppablePieces(Common.COLOR_WHITE, capturedOrDroppableBlack);
                        }
                        else
                        {
                            game.setDroppablePieces(Common.COLOR_WHITE, capturedOrDroppableWhite);
                            game.setDroppablePieces(Common.COLOR_BLACK, capturedOrDroppableBlack);
                        }
                    }
                    LOGGER.fine("FEN set captured piece white " + capturedOrDroppableWhite + " black " + capturedOrDroppableBlack);
                    break;
                }
                LOGGER.warning("bad extra row in FEN gkind " + Common.GAME_KIND_TEXT[gkind]);
                return false;
            }
            }
            int column = 1;
            Piece newPiece = null;
            for (byte pieceB : row.getBytes()) {
                char pieceCh = (char) pieceB;

                if (pieceCh == '~')
                {
                    // last piece is a promoted pawn
                    newPiece.setPromoted();
                    continue;
                }

                if (Character.isDigit(pieceCh)) {
                    column += (pieceCh - 48);
                    continue;
                }

                if (column > 8) {
                    LOGGER.warning("illegal FEN '" + position + "' bad row '" + row + "'");
                    return false;
                }

                int color = Common.COLOR_WHITE;
                if (Character.isLowerCase(pieceCh)) {
                    color = Common.COLOR_BLACK;
                }
                pieceCh = Character.toUpperCase(pieceCh);

                int type = Notation.getPieceType(String.valueOf(pieceCh));
                if (type == Common.PIECE_TYPE_ILLEGAL) {
                    LOGGER.warning("illegal FEN '" + position + "' bad piece '" + pieceCh + "'");
                    return false;
                }
                newPiece = Piece.create(type, color);
                game.setPieceAt(currentRow, column, newPiece);
                column++;
            }
            if (column != 9) {
                LOGGER.warning("illegal FEN '" + position + "' bad row '" + row + "'");
                return false;
            }
            currentRow--;
        }
        if (currentRow != 0) {
            LOGGER.warning("illegal FEN '" + position + "' bad number of rows");
            return false;
        }
        return true;
    }

    /**
     * Create a random Fischer 960 init position
     *
     * @return a random Fischer 960 init position
     */
    public static String create960FEN() {
        List<String> pos = new ArrayList<String>();
        pos.addAll(Arrays.asList("R", "K", "R"));
        int i = Utils.randomInt(4);
        LOGGER.fine(pos + " I:" + i);
        pos.add(i, "Q");
        i = Utils.randomInt(5);
        pos.add(i, "N");
        i = Utils.randomInt(6);
        pos.add(i, "N");
        i = Utils.randomInt(7);
        pos.add(i, "B"); // the right bishop
        int l = 0;
        if (((i + 1) / 2) > 0) {
            l = Utils.randomInt((i + 1) / 2);
        }
        pos.add(2 * l + (i % 2), "B"); // left bishop
        String POS = "";
        for (i = 0; i < 8; i++) {
            POS += pos.get(i);
        }
        List<String> letters = Arrays.asList(new String[]{"A", "B", "C", "D", "E", "F", "G", "H"});
        String castle = letters.get(pos.indexOf("R")) + letters.get(pos.lastIndexOf("R"));
        castle += castle.toLowerCase();
        return POS.toLowerCase() + "/pppppppp/8/8/8/8/PPPPPPPP/" + POS + " w " + castle + " - 0 1";
    }
}
