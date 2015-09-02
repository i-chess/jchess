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
import java.util.List;
import java.util.logging.Logger;

/**
 * This class contains additional information about the state of the game when
 * the move was played. A move info is obtained from a Game using getMoveInfo,
 * and is valid as long as the move was not taken back. This move info can also
 * be obtained from Move.getMoveInfo()
 *
 * @author Ran Berenfeld
 * @version 1.0
 */
public class MoveInfo {

    private final static Logger LOGGER = Logger.getLogger(MoveInfo.class.getName());

    private Game _game;

    private boolean check = false;
    private boolean doubleCheck = false;
    private boolean checkMate = false;
    private int draw50MovesCount; // for 50 moves, count moves with no pawn or
    private String fenPos;
    private boolean hasEnoughMaterial[] = {true, true, true, true};
    private Move move;
    private Piece movedPiece[] = {null, null};
    private boolean staleMate = false;
    private List<Move> validNextMoves = new ArrayList<Move>();

    public MoveInfo(Game game) {
        _game = game;
    }

    void addValidMove(Piece piece, int toX, int toY) {
        addValidMove(piece, toX, toY, false);
    }

    /*
     * add valid move of a piece to a target square
     */
    void addValidMove(Piece piece, int toX, int toY, boolean drop) {
        Utils.AssertNotNull(piece);
        boolean promotion = piece.isPawn() && ((toX == 1) || (toX == 8));
        if (promotion) {
            if (_game.isSuicide()) {
                addValidMove(piece, toX, toY, Common.PIECE_TYPE_KING);
            }

            if (_game.isGrassHopper()) {
                addValidMove(piece, toX, toY, Common.PIECE_TYPE_GRASSHOPER);
            }

            if (_game.isMiniCapa()) {
                addValidMove(piece, toX, toY, Common.PIECE_TYPE_ARCHBISHOP);
                addValidMove(piece, toX, toY, Common.PIECE_TYPE_CHANCELLOR);
            }

            addValidMove(piece, toX, toY, Common.PIECE_TYPE_QUEEN);
            addValidMove(piece, toX, toY, Common.PIECE_TYPE_ROOK);
            addValidMove(piece, toX, toY, Common.PIECE_TYPE_KNIGHT);
            addValidMove(piece, toX, toY, Common.PIECE_TYPE_BISHOP);
            return;
        }
        if (drop) {
            addValidMove(piece, toX, toY, Common.PIECE_TYPE_ILLEGAL, true);
            return;
        }
        addValidMove(piece, toX, toY, Common.PIECE_TYPE_ILLEGAL, false);
    }

    void addValidMove(Piece piece, int toX, int toY, int promotionPiece) {
        addValidMove(piece, toX, toY, promotionPiece, false);
    }

    /*
     * add a valid move
     */
    void addValidMove(Piece piece, int toX, int toY, int promotionPiece, boolean drop) {
        Utils.AssertNotNull(piece);

        if (drop) {
            Move move = new Move(_game, toX, toY, toX, toY, piece.getTypeWhenDropping());
            move.setMovedPiece(piece);
            validNextMoves.add(move);
            move.setMoveNumber(_game.getCurrentMove() + 1);
            LOGGER.fine("adding move " + _game.getCurrentMove() + " valid drop to " + toX + "," + toY + " piece " + Notation.getPieceCharacter(piece.getTypeWhenDropping()));
            return;
        }

        Move move = new Move(_game, piece.getX(), piece.getY(), toX, toY, promotionPiece);
        move.setMovedPiece(piece);
        Piece captured = _game.getPieceAt(toX, toY);

        // check if EP capture move
        if (piece.isPawn()) {
            if ((piece.getY() != toY) && (captured == null)) {
                if (piece.isWhite()) {
                    captured = _game.getPieceAt(toX - 1, toY);
                } else {
                    captured = _game.getPieceAt(toX + 1, toY);
                }
            }
        }
        move.setMoveNumber(_game.getCurrentMove() + 1);
        move.setCapturedPiece(captured);

        LOGGER.fine("adding valid move " + _game.getCurrentMove() + " from " + piece.getX() + "," + piece.getY() + " to " + toX + "," + toY +
                " piece " + Notation.getPieceCharacter(piece.getType()));

        validNextMoves.add(move);
    }

    /**
     * Return the number of half-moves played from the last capture or pawn
     * advance, after this move was played.
     *
     * @return The number of half-moves played from the last capture or pawn
     * advance.
     */
    int getDraw50MovesCount() {
        return draw50MovesCount;
    }

	/*
     * Check if a move is valid
	 */

    void setDraw50MovesCount(int draw50MovesCount) {
        this.draw50MovesCount = draw50MovesCount;
    }

    /**
     * Return a FEN representation of the Game right after this move was played.
     *
     * @return A FEN representation of the Game right after this move was
     * played.
     */
    public String getFenPosition() {
        return fenPos;
    }

    boolean[] getHasEnoughMaterial() {
        return hasEnoughMaterial;
    }

    void setHasEnoughMaterial(boolean[] hasEnoughMaterial) {
        this.hasEnoughMaterial = hasEnoughMaterial;
    }

    Move getMove() {
        return move;
    }

    void setMove(Move move) {
        this.move = move;
    }

    Piece[] getMovedPiece() {
        return movedPiece;
    }

    void setMovedPiece(Piece[] movedPiece) {
        this.movedPiece = movedPiece;
    }

    Move getValidMove(int fromX, int fromY, int toX, int toY, int additionalPieceType) {
        for (Move move : validNextMoves) {
            if ((move.getFromX() == fromX) && (move.getFromY() == fromY) && (move.getToX() == toX) && (move.getToY() == toY)
                    && (move.getAdditionalPieceTypeInfo() == additionalPieceType)) {
                LOGGER.fine("found valid move " + move.getNameNum());
                return move;
            }
        }
        return null;
    }

    /**
     * Return all the valid moves that can be played from this move.
     *
     * @return All the valid moves that can be played from this move.
     */
    List<Move> getValidNextMoves() {
        return validNextMoves;
    }

    void setValidNextMoves(List<Move> validNextMoves) {
        this.validNextMoves = validNextMoves;
    }

    /**
     * Returns true if the game is in check after this move. otherwise false.
     *
     * @return true if the game is in check after this move. otherwise false.
     */
    boolean isCheck() {
        return check;
    }

    void setCheck(boolean check) {
        this.check = check;
    }

    /**
     * Returns true if the game is in float check after this move. otherwise
     * false.
     *
     * @return true if the game is in float check after this move. otherwise
     * false.
     */
    boolean isFloatCheck() {
        return doubleCheck;
    }

    /**
     * Returns true if the game is in checkmate after this move. otherwise
     * false.
     *
     * @return true if the game is in checkmate after this move. otherwise
     * false.
     */
    boolean isCheckMate() {
        return checkMate;
    }

    void setCheckMate(boolean checkMate) {
        this.checkMate = checkMate;
    }

    boolean isMoveValid(int fromX, int fromY, int toX, int toY) {
        for (Move move : validNextMoves) {
            if ((move.getFromX() == fromX) && (move.getFromY() == fromY) && (move.getToX() == toX) && (move.getToY() == toY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the game is in stalemate after this move. otherwise
     * false.
     *
     * @return true if the game is in stalemate after this move. otherwise
     * false.
     */
    boolean isStaleMate() {
        return staleMate;
    }

    void setStaleMate(boolean staleMate) {
        this.staleMate = staleMate;
    }

    void setDoubleCheck(boolean doubleCheck) {
        this.doubleCheck = doubleCheck;
    }

    void setFenPos(String fenPos) {
        this.fenPos = fenPos;
    }
}
