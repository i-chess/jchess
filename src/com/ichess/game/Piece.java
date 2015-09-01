//==============================================================================
//            Copyright (c) 2009-2014 ichess.co.il
//
//This document contains confidential information which is protected by
//copyright and is proprietary to ichess.co.il. No part
//of this document may be used, copied, disclosed, or conveyed to another
//party without prior written consent of ichess.co.il.
//==============================================================================

package com.ichess.game;



import java.awt.*;
import java.util.Arrays;

/**
 * @author Ran Berenfeld
 * @version 1.0
 */
public abstract class Piece {

    protected boolean _checkPin = false;
    protected int _color = Common.COLOR_ILLEGAL;
    protected boolean _moved = false;
    protected int _pieceType = Common.PIECE_TYPE_ILLEGAL;
    protected boolean _pinned;
    protected Piece _pinningPiece;
    protected boolean[] _reachable = new boolean[64];
    protected int _x = 0, _y = 0;
    protected boolean _promoted;

    public Piece(int pieceType, int color) {
        _pieceType = pieceType;
        _color = color;
    }

    public static Piece create(int type, int color) {
        Utils.AssertNotNull(type, "illegal null piece type");
        Piece newPiece = null;
        switch (type) {
            case Common.PIECE_TYPE_KING:
                newPiece = new King(color);
                break;
            case Common.PIECE_TYPE_QUEEN:
                newPiece = new Queen(color);
                break;
            case Common.PIECE_TYPE_ROOK:
                newPiece = new Rook(color);
                break;
            case Common.PIECE_TYPE_BISHOP:
                newPiece = new Bishop(color);
                break;
            case Common.PIECE_TYPE_KNIGHT:
                newPiece = new Knight(color);
                break;
            case Common.PIECE_TYPE_PAWN:
                newPiece = new Pawn(color);
                break;
            case Common.PIECE_TYPE_GRASSHOPER:
                newPiece = new Grasshoper(color);
                break;
            case Common.PIECE_TYPE_ARCHBISHOP:
                newPiece = new Archbishop(color);
                break;
            case Common.PIECE_TYPE_CHANCELLOR:
                newPiece = new Chancellor(color);
                break;
            case Common.PIECE_TYPE_DROP_ANY:
                newPiece = new AnyPiece(color);
                break;
        }
        return newPiece;
    }

    public boolean isPromoted() {
        return _promoted;
    }

    public void setPromoted() { _promoted = true; };
    public void clearPromoted() { _promoted = false; };

    public boolean canBeDroppedAt(int x, int y) {
        return true;
    }

    abstract boolean canMoveTo(int x, int y, Game position);

    boolean canMoveTo(Point loc, Game position) {
        Utils.AssertNotNull(loc);
        Utils.AssertNotNull(position);
        return canMoveTo(loc.x, loc.y, position);
    }

    public void clearReachability() {
        Arrays.fill(_reachable, false);
    }

    abstract void doCalcReachability(Game pos);

    public int getColor() {
        return _color;
    }

    public Piece getPinningPiece() {
        return _pinningPiece;
    }

    public void setPinningPiece(Piece pinningPiece) {
        this._pinningPiece = pinningPiece;
    }

    public int getType() {
        return _pieceType;
    }

    public int getTypeWhenDropping() {
        if (_promoted)
        {
            return Common.PIECE_TYPE_PAWN;
        }
        return _pieceType;
    }

    public int getX() {
        return _x;
    }

    public void setX(int x) {
        this._x = x;
    }

    public int getY() {
        return _y;
    }

    public void setY(int y) {
        this._y = y;
    }

    @Override
    public int hashCode() {
        return (_pieceType * Common.PIECE_TYPE_NUM) + (_color * 2);
    }

    public boolean isBishop() {
        return _pieceType == Common.PIECE_TYPE_BISHOP;
    }

    public boolean isBlack() {
        return _color == Common.COLOR_BLACK;
    }

    public boolean isCheckPin() {
        return _checkPin;
    }

    public void setCheckPin(boolean checkPin) {
        this._checkPin = checkPin;
    }

    public boolean isColor(int color) {
        return _color == color;
    }

    public boolean isKing() {
        return _pieceType == Common.PIECE_TYPE_KING;
    }

    public boolean isKnight() {
        return _pieceType == Common.PIECE_TYPE_KNIGHT;
    }

    public boolean isMoved() {
        return _moved;
    }

    public void setMoved(boolean moved) {
        this._moved = moved;
    }

    public boolean isPawn() {
        return _pieceType == Common.PIECE_TYPE_PAWN;
    }

    public boolean isPinned() {
        return _pinned;
    }

    public void setPinned(boolean pinned) {
        this._pinned = pinned;
    }

    public boolean isQueen() {
        return _pieceType == Common.PIECE_TYPE_QUEEN;
    }

    public boolean isGrasshoper() {
        return _pieceType == Common.PIECE_TYPE_GRASSHOPER;
    }

    public boolean isArchbisop() {
        return _pieceType == Common.PIECE_TYPE_ARCHBISHOP;
    }

    public boolean isChancellor() {
        return _pieceType == Common.PIECE_TYPE_CHANCELLOR;
    }

    public boolean isReachable(int x, int y) {
        return _reachable[((x - 1) << 3) + (y - 1)];
    }

    public boolean isRook() {
        return _pieceType == Common.PIECE_TYPE_ROOK;
    }

    public boolean isWhite() {
        return _color == Common.COLOR_WHITE;
    }

    public void setReachable(int x, int y, boolean val) {
        _reachable[((x - 1) << 3) + (y - 1)] = val;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
        {
            return false;
        }
        Piece otherPiece = (Piece) other;
        if (_pieceType != otherPiece._pieceType)
        {
            return false;
        }
        if (_color != otherPiece._color)
        {
            return false;
        }
        if (_promoted != otherPiece._promoted)
        {
            return false;
        }
        return true;

    }
}
