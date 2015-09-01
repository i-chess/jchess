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
public class

        Knight extends Piece {

    public Knight(int color) {
        super(Common.PIECE_TYPE_KNIGHT, color);
    }

    @Override
    boolean canMoveTo(int x, int y, Game position) {
        // check that not moving to the same square
        int deltaX = x - _x;
        int deltaY = y - _y;

        if ((deltaX == 0) || (deltaY == 0) || (Math.abs(deltaX) > 2) || (Math.abs(deltaY) > 2)) {
            // can't move
            return false;
        }

        if (Math.abs(deltaX) == Math.abs(deltaY)) {
            // can't move
            return false;
        }

        // check that the destination is empty or contain an enemy piece
        Piece destPiece = position.getPieceAt(x, y);
        return null == destPiece || destPiece.getColor() != _color;

    }

    @Override
    void doCalcReachability(Game pos) {
        for (int dirX = -2; dirX < 3; dirX += 1) {
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
        return _color == Common.COLOR_WHITE ? "N" : "n";
    }

}
