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
public class Grasshoper extends Piece {

    public Grasshoper(int color) {
        super(Common.PIECE_TYPE_GRASSHOPER, color);
    }

    @Override
    boolean canMoveTo(int x, int y, Game position) {
        // check that not moving to the same square
        int deltaX = x - _x;
        int deltaY = y - _y;

        if ((Math.abs(deltaX) + Math.abs(deltaY)) <= 1) {
            // can't move to the same place or 1 length ROOK
            return false;
        }

        if ((Math.abs(deltaX) * Math.abs(deltaY)) == 1) { // moving 1 square Bishop is not possible
            return false;
        }


        if ((Math.abs(deltaX) == 0) || (Math.abs(deltaY) == 0)) {
            // ok. move like a rook
        } else if (Math.abs(deltaX) == Math.abs(deltaY)) {
            // ok. move like a bishop
        } else {
            return false;
        }

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

        // check that there are no pieces in the way (up to one before last)
        int dirX = deltaX > 0 ? 1 : deltaX < 0 ? -1 : 0;
        int dirY = deltaY > 0 ? 1 : deltaY < 0 ? -1 : 0;
        int posX = _x + dirX;
        int posY = _y + dirY;

        x -= dirX;
        y -= dirY; // moving to one before last

        if (position.getPieceAt(x, y) == null) { // can only hoop over a piece
            return false;
        }

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

    @Override
    void doCalcReachability(Game pos) {
        for (int dirX = -1; dirX < 2; dirX += 1) {
            for (int dirY = -1; dirY < 2; dirY += 1) {
                if ((dirX == 0) && (dirY == 0)) { // dont stay on foot
                    continue;
                }
                int x = _x + dirX; // can't move 1 length (must jump over something)
                int y = _y + dirY;
                while ((x > 0) && (x < 9) && (y > 0) && (y < 9) && (x + dirX > 0) && (x + dirX < 9) && (y + dirY > 0) && (y + dirY < 9)) {
                    Piece piece = pos.getPieceAt(x, y);
                    if (piece != null) { // got to jump over a piece (anycolor)
                        piece = pos.getPieceAt(x + dirX, y + dirY);
                        if (piece != null && piece.getColor() == _color) { // capture own piece
                            break;
                        }
                        setReachable(x + dirX, y + dirY, true);
                        break;
                    }
                    x += dirX;
                    y += dirY;
                }
            }
        }
    }

    @Override
    public String toString() {
        return _color == Common.COLOR_WHITE ? "G" : "g";
    }

}
