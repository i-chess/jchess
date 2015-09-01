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

/**
 * @author Ran Berenfeld
 * @version 1.0
 */
public class MoveList {

    private int _currentMove = 0;
    private String _listAlg;
    private String _listFig;
    private String _listFigLang;

    private String _listNum;
    private List<String> _movesAlg = new ArrayList<String>();
    private List<String> _movesFig = new ArrayList<String>();
    private List<String> _movesFigLang = new ArrayList<String>();
    private List<String> _movesNum = new ArrayList<String>();

    public MoveList() {
        _listAlg = "";
        _listFig = "";
        _listFigLang = "";
        _listNum = "";
    }

    public void addMove(Move move) {
        Utils.AssertNotNull(move);

        Game game = move.getGame();
        Utils.AssertNotNull(game);

        if (_currentMove != 0) {
            _listNum += " ";
            _listAlg += " ";
            _listFig += " ";
            _listFigLang += " ";
        }

        _listNum += move.getNameNum();
        _listAlg += move.getNameAlg();
        _listFig += move.getNameFig();
        _listFigLang += move.getNameFigLang();

        _movesNum.add(move.getNameNum());
        _movesAlg.add(move.getNameAlg());
        _movesFig.add(move.getNameFig());
        _movesFigLang.add(move.getNameFigLang());

        _currentMove++;
    }

    public String getListAlg() {
        return _listAlg;
    }

    public String getListAlg(int fromMove) {
        Utils.Assert(fromMove <= _currentMove);
        String result = "";
        for (int index = fromMove; index < _currentMove; index++) {
            result += _movesAlg.get(index);
            if (index != (_currentMove - 1)) {
                result += " ";
            }
        }
        return result;
    }

    public String getListFig() {
        return _listFig;
    }

    public String getListFig(int fromMove) {
        Utils.Assert(fromMove <= _currentMove);
        String result = "";
        for (int index = fromMove; index < _currentMove; index++) {
            result += _movesFig.get(index);
            if (index != (_currentMove - 1)) {
                result += " ";
            }
        }
        return result;
    }

    public String getListFigLang() {
        return _listFigLang;
    }

    public String getListFigLang(int fromMove) {
        Utils.Assert(fromMove <= _currentMove);
        String result = "";
        for (int index = fromMove; index < _currentMove; index++) {
            result += _movesFigLang.get(index);
            if (index != (_currentMove - 1)) {
                result += " ";
            }
        }
        return result;
    }

    public String getListNum() {
        return _listNum;
    }

    public String getListNumFromMove(int fromMove) {
        Utils.Assert(fromMove <= _currentMove);
        String result = "";
        for (int index = fromMove; index < _currentMove; index++) {
            result += _movesNum.get(index);
            if (index != (_currentMove - 1)) {
                result += " ";
            }
        }
        return result;
    }

    public String getListNumToMove(int toMove) {
        Utils.Assert(toMove <= _currentMove);
        String result = "";
        for (int index = 0; index < toMove; index++) {
            result += _movesNum.get(index);
            if (index != (toMove - 1)) {
                result += " ";
            }
        }
        return result;
    }

    public void takeback() {

        if (_currentMove == 1) {
            _listAlg = "";
            _listFig = "";
            _listFigLang = "";
            _listNum = "";
        } else {
            _listNum = _listNum.substring(0, _listNum.length() - 6);

            int lastSpace = _listAlg.lastIndexOf(" ");
            _listAlg = _listAlg.substring(0, lastSpace);

            lastSpace = _listFig.lastIndexOf(" ");
            _listFig = _listFig.substring(0, lastSpace);

            lastSpace = _listFigLang.lastIndexOf(" ");
            _listFigLang = _listFigLang.substring(0, lastSpace);
        }

        _currentMove--;

        _movesAlg.remove(_currentMove);
        _movesFig.remove(_currentMove);
        _movesFigLang.remove(_currentMove);
        _movesNum.remove(_currentMove);
    }

    @Override
    public String toString() {
        return _listAlg;
    }

}
