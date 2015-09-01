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
 * @author Ran Berenfeld
 * @version 1.0
 */
public class King extends Piece {

    public King(int color) {
        super(Common.PIECE_TYPE_KING, color);
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

        // check that the square is square away from me
        if ((Math.abs(deltaX) > 1) || (Math.abs(deltaY) > 1)) {
            // king can only move 1 square at a time
            return false;
        }

        // check that the destination is empty or contain non-king enemy piece
        Piece destPiece = position.getPieceAt(x, y);
        return null == destPiece || destPiece.getColor() != _color;

    }

    @Override
    void doCalcReachability(Game pos) {
        for (int x = _x - 1; x <= _x + 1; x += 1) {
            for (int y = _y - 1; y <= _y + 1; y += 1) {
                if ((x < 1) || (y < 1) || (x > 8) || (y > 8)) {
                    continue;
                }
                Piece piece = pos.getPieceAt(x, y);
                if (piece == null) {
                    setReachable(x, y, true);
                    continue;
                }
                if (piece.getColor() != _color) {
                    setReachable(x, y, true);
                }
            }
        }
    }

    @Override
    public String toString() {
        return _color == Common.COLOR_WHITE ? "K" : "k";
    }
}
