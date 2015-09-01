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
public class AnyPiece extends Piece {

    public AnyPiece(int color) {
        super(Common.PIECE_TYPE_DROP_ANY, color);
    }

    @Override
    boolean canMoveTo(int x, int y, Game position) {
        assert(false);
        return false;
    }

    @Override
    void doCalcReachability(Game pos) {
        assert(false);
    }

    @Override
    public String toString() {
        return _color == Common.COLOR_WHITE ? "X" : "x";
    }

}
