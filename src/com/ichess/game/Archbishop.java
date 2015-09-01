//==============================================================================
//            Copyright (c) 2009-2014 ichess.co.il
//
//This document contains confidential information which is protected by
//copyright and is proprietary to ichess.co.il. No part
//of this document may be used, copied, disclosed, or conveyed to another
//party without prior written consent of ichess.co.il.
//==============================================================================

package com.ichess.game;

/**
 * @author Maor Ganz
 * @version 1.0
 */
public class Archbishop extends Piece {

    public Archbishop(int color) {
        super(Common.PIECE_TYPE_ARCHBISHOP, color);
    }

    @Override
    boolean canMoveTo(int x, int y, Game position) {
        // check that not moving to the same square
        int deltaX = x - _x;
        int deltaY = y - _y;

        if ((deltaX == 0) && (deltaY == 0)) {
            // can't move to the same place
            return false;
        }

        boolean bishop = false;
        if (Math.abs(deltaX) == Math.abs(deltaY)) {
            bishop = true;
        }

        // check that the destination is empty or contain an enemy piece
        Piece destPiece = position.getPieceAt(x, y);
        boolean checkPin = false;
        if (destPiece != null) {
            if (destPiece.getColor() == _color) {
                // can't move there
                return false;
            }
            if (destPiece.isKing() && (_checkPin)) {
                // moving to enemy king. check pinning
                checkPin = true;
            }
        }

        // check that there are no pieces in the diagonal way
        if (bishop) {
            int dirX = deltaX > 0 ? 1 : deltaX < 0 ? -1 : 0;
            int dirY = deltaY > 0 ? 1 : deltaY < 0 ? -1 : 0;
            int posX = _x + dirX;
            int posY = _y + dirY;
            Piece maybePinned = null;
            while ((posX != x) || (posY != y)) {
                Piece pieceInTheWay = position.getPieceAt(posX, posY);
                if (null != pieceInTheWay) {
                    if ((pieceInTheWay.getColor() != _color) && checkPin && (maybePinned == null)) {
                        maybePinned = pieceInTheWay;
                    } else {
                        return false;
                    }
                }
                posX += dirX;
                posY += dirY;
            }

            if (maybePinned != null) {
                // it's pinned
                maybePinned.setPinned(true);
                maybePinned.setPinningPiece(this);
                return false;
            }

            return true;
        }

        // else a knight
        if ((deltaX == 0) || (deltaY == 0) || (Math.abs(deltaX) > 2) || (Math.abs(deltaY) > 2)) {
            // can't move
            return false;
        }

        // already checked that the destination is empty or contain an enemy piece
        return true;
    }

    @Override
    void doCalcReachability(Game pos) {
        for (int dirX = -1; (dirX - 3) != 0; dirX += 2) { // bishop
            for (int dirY = -1; (dirY - 3) != 0; dirY += 2) {
                int x = _x + dirX;
                int y = _y + dirY;
                while ((x > 0) && (x < 9) && (y > 0) && (y < 9)) {
                    Piece piece = pos.getPieceAt(x, y);
                    if (piece == null) {
                        setReachable(x, y, true);
                    } else {
                        if (piece.getColor() != _color) {
                            setReachable(x, y, true);
                        }
                        break;
                    }
                    x += dirX;
                    y += dirY;
                }
            }
        }

        for (int dirX = -2; dirX < 3; dirX += 1) { // knight
            for (int dirY = -2; dirY < 3; dirY += 1) {
                if ((dirX == 0) || (dirY == 0)) {
                    continue;
                }
                if (Math.abs(dirX) == Math.abs(dirY)) {
                    continue;
                }

                int x = _x + dirX;
                int y = _y + dirY;
                if ((x <= 0) || (x > 8) || (y <= 0) || (y > 8)) {
                    continue;
                }
                Piece piece = pos.getPieceAt(x, y);
                if (piece == null) {
                    setReachable(x, y, true);
                } else if (piece.getColor() != _color) {
                    setReachable(x, y, true);
                }
            }
        }
    }

    @Override
    public String toString() {
        return _color == Common.COLOR_WHITE ? "A" : "a";
    }

}
