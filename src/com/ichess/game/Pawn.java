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
public class Pawn extends Piece {

    private boolean _promoted = false;
    private int _wasPromotedTo = Common.PIECE_TYPE_ILLEGAL;

    public Pawn(int color) {
        super(Common.PIECE_TYPE_PAWN, color);
    }

    @Override
    public boolean canBeDroppedAt(int x, int y) {
        return (x > 1) && (x < 8);
    }

    @Override
    boolean canMoveTo(int x, int y, Game game) {
        // check that not moving to the same square
        int deltaX = x - _x;
        int deltaY = y - _y;

        if ((deltaX == 0) && (deltaY == 0)) {
            // can't move to the same place
            return false;
        }

        Piece dest = game.getPieceAt(x, y);

        if (_color == Common.COLOR_WHITE) {
            // check advance 1
            if ((x == _x + 1) && (y == _y)) {
                return dest == null;
            }
            // check advance 2
            if ((x == _x + 2) && (y == _y) && (_x == 2)) {
                Piece dest2 = game.getPieceAt(_x + 1, y);
                return !((dest2 != null) || (dest != null));
            }
            // check capture
            if ((x == _x + 1) && ((y == _y + 1) || (y == _y - 1))) {
                if (dest == null) {
                    // check ep
                    Pawn epPawn = game.getEpPawn();
                    if (epPawn != null) {
                        if (y == epPawn.getY()) {
                            if (x == epPawn.getX() + 1) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
                return dest.getColor() != _color;
            }
        } else {
            // check advance 1
            if ((x == _x - 1) && (y == _y)) {
                return dest == null;
            }
            // check advance 2
            if ((x == _x - 2) && (y == _y) && (_x == 7)) {
                Piece dest2 = game.getPieceAt(_x - 1, y);
                return !((dest2 != null) || (dest != null));
            }
            // check capture
            if ((x == _x - 1) && ((y == _y + 1) || (y == _y - 1))) {
                if (dest == null) {
                    // check ep
                    Pawn epPawn = game.getEpPawn();
                    if (epPawn != null) {
                        if (y == epPawn.getY()) {
                            if (x == epPawn.getX() - 1) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
                return dest.getColor() != _color;
            }
        }

        return false;
    }

    @Override
    void doCalcReachability(Game game) {
        Piece piece;

        int deltaX;
        int baseXFor2;
        if (_color == Common.COLOR_WHITE) {
            deltaX = 1;
            baseXFor2 = 2;
        } else {
            deltaX = -1;
            baseXFor2 = 7;
        }

        piece = game.getPieceAt(_x + deltaX, _y);
        if (piece == null) {
            setReachable(_x + deltaX, _y, true);
            if (_x == baseXFor2) {
                piece = game.getPieceAt(_x + deltaX + deltaX, _y);
                if (piece == null) {
                    setReachable(_x + deltaX + deltaX, _y, true);
                }
            }
        }
        if (_y > 1) {
            piece = game.getPieceAt(_x + deltaX, _y - 1);
            if (piece != null) {
                if (piece.getColor() != _color) {
                    setReachable(_x + deltaX, _y - 1, true);
                }
            } else {
                // check for ep case
                Pawn epPawn = game.getEpPawn();
                if (epPawn != null) {
                    if (Math.abs(_y - epPawn.getY()) == 1) {
                        if (isWhite() && _x == 5) {
                            setReachable(epPawn.getX() + 1, epPawn.getY(), true);
                        } else if (isBlack() && _x == 4) {
                            setReachable(epPawn.getX() - 1, epPawn.getY(), true);
                        }
                    }
                }
            }
        }

        if (_y < 8) {
            piece = game.getPieceAt(_x + deltaX, _y + 1);
            if (piece != null) {
                if (piece.getColor() != _color) {
                    setReachable(_x + deltaX, _y + 1, true);
                }
            } else {
                // check for ep case
                Pawn epPawn = game.getEpPawn();
                if (epPawn != null) {
                    if (Math.abs(_y - epPawn.getY()) == 1) {
                        if (isWhite() && _x == 5) {
                            setReachable(epPawn.getX() + 1, epPawn.getY(), true);
                        } else if (isBlack() && _x == 4) {
                            setReachable(epPawn.getX() - 1, epPawn.getY(), true);
                        }
                    }
                }
            }
        }

    }

    public int getWasPromotedTo()
    {
        return _wasPromotedTo;
    }

    public void setWasPromotedTo(int wasPromotedTo)
    {
        _wasPromotedTo = wasPromotedTo;
    }

    @Override
    public String toString() {
        return _color == Common.COLOR_WHITE ? "P" : "p";
    }
}
