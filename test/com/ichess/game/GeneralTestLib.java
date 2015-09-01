//==============================================================================
//            Copyright (c) 2009-2014 ichess.co.il
//
//This document contains confidential information which is protected by
//copyright and is proprietary to ichess.co.il. No part
//of this document may be used, copied, disclosed, or conveyed to another
//party without prior written consent of ichess.co.il.
//==============================================================================

package com.ichess.game;

import com.ichess.game.*;
import com.ichess.game.TimeUtils;
import com.ichess.game.Utils;
import com.ichess.jvoodoo.Invocation;
import com.ichess.jvoodoo.ReturnPredicat;
import com.ichess.jvoodoo.Scenarios;
import com.ichess.jvoodoo.Voodoo;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import java.io.FileInputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneralTestLib {

    private final static Logger LOGGER = Logger.getLogger(FEN.class.getName());

    private long fakeNowInMilliseconds;
    
    @Before
    public void setUp() throws java.lang.Exception {
        Voodoo.castVoodooOn("com.ichess.game.TimeUtils");
        ReturnPredicat.Predicat predicat = new ReturnPredicat.Predicat() {
            @Override
            public Object returnValue(Object... parameters) {
                LOGGER.fine("return time " + fakeNowInMilliseconds);
                return new Date(fakeNowInMilliseconds);
            }
        };
        Scenarios.always(new Invocation("com.ichess.game.TimeUtils", "now", new ReturnPredicat(predicat)));
        predicat = new ReturnPredicat.Predicat() {
            @Override
            public Object returnValue(Object... parameters) {
                LOGGER.fine("return time " + fakeNowInMilliseconds);
                return fakeNowInMilliseconds;
            }
        };
        Scenarios.always(new Invocation("com.ichess.game.TimeUtils", "nowInMs", new ReturnPredicat(predicat)));
        fakeNowInMilliseconds = 1;
    }

    @Test
    public void test_PlayAFewMovesAndCheckFEN() {
        LOGGER.info("test_PlayAFewMovesAndCheckFEN started");
        Game game = new Game();
        assertNotNull("could not create game", game);
        assertTrue("could not play move list", game.playMoveList("e4 e5 Nf3 Nc6"));
        assertEquals("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R", FEN.getFENPosition(game));
    }

    @Test
    public void test_BugHouseGame() {
        // every test is just a public function that begins with test...()
        // ok lets start
        Game game1 = new Game(Common.GAME_KIND_BUG_HOUSE);
        Game game2 = new Game(Common.GAME_KIND_BUG_HOUSE);
        // ok. we will create 1 bughouse game (this is 2 game objects), link them and play some moves
        // here - a very simple assertion
        assertEquals(game1.getGameKind(), Common.GAME_KIND_BUG_HOUSE);

        game1.setOtherGame(game2);
        game2.setOtherGame(game1);

        // this is assertion with verbose
        assertTrue("no captures pieces yet", game1.noCapturedPieces());
        assertTrue("no captures pieces yet", game2.noCapturedPieces());

        // lets play some moves
        game1.playMoveList("e4 d5 exd5");
        // and check
        assertTrue(game1.getCurrentMove() == 3);

        // go ahead - try now (since we have a captured pieces in game1) to drop it in game 2 !
        // game 1 has a black captured pawn...
        boolean movePlayed = game2.playMove("e4");
        assertEquals(true, movePlayed);
        //need to have black to move to drop black pieces


        boolean move2Played = game2.playMove("P@f3");
        assertEquals(true, move2Played);

        //    Assert.assertEquals("e4 P@f3",game2.getMoveListNum());
        Piece piece = game2.getPieceAt(3, game2.getColumn(Common.BOARD_COLUMN.F));
        Piece blackPawn = Piece.create(Common.PIECE_TYPE_PAWN, Common.COLOR_BLACK);
        assertEquals(blackPawn, piece);

        boolean move4Played = game1.playMoveList("Qxd5 Nc3");
        assertTrue(move4Played);
    }

    /*
    public class Clock
    {
        long overrideTime = 0; // lets say 0 is illegal time value
        long getTime()
        {
            return overrideTime != 0 ? overrideTime : System.currentTimeMillis();
        }
        void setTime(long overrideTime)
        {
            this.overrideTime = overrideTime;
        }
    }



    static long overrideTime;
    static
    {
        Voodoo.castVoodooOn("Clock");
        Scenarios.always(new Invocation("Clock", "getTime", new ReturnPredicat.Predicat()
            {
                @Override
                public Object returnValue(Object... parameters) {
                    return overrideTime;
                }
            }
        ));
    }
    public void test_TimeControlsForBlitzGame() {

        Game game = new Game();
        game.setTimeLimitForGame(5);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));

        overrideTime += 5 * 60 * 1000;
        assertTrue(game.isOutOfTime(Common.COLOR_WHITE));
    }
    */

    @Test
    public void test_TimeControlsBasic() {
        Game game = new Game();
        game.setTimeLimitForGame(5);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        fakeNowInMilliseconds += (10 * TimeUtils.MS_IN_MINUTE);
        // no timeout - no moves played
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 5 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 5 * TimeUtils.MS_IN_MINUTE);

        game.playMove("e4");
        fakeNowInMilliseconds += TimeUtils.MS_IN_MINUTE;
        // no timeout after 1 minute
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 5 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE);

        // no timeout after 4 minutes - 1 second
        fakeNowInMilliseconds += ((4 * TimeUtils.MS_IN_MINUTE) - TimeUtils.MS_IN_SECOND);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 5 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 1 * TimeUtils.MS_IN_SECOND);

        // timeout after 2 more seconds
        fakeNowInMilliseconds += (2 * TimeUtils.MS_IN_SECOND);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertTrue(game.isOutOfTime(Common.COLOR_BLACK));
    }

    @Test
    public void test_TimeControlsIncrement() {
        Game game = new Game();
        game.setTimeLimitForGame(3);
        game.setTimeIncrementPerMove(3);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE);

        game.playMoveList("e4 e5 d4 d5 f4 f5");
        // 3 moves played - white clock should be 3 minutes + 9 seconds
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE + 9 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE + 9 * TimeUtils.MS_IN_SECOND);


        fakeNowInMilliseconds += (3 * TimeUtils.MS_IN_MINUTE);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 9 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE + 9 * TimeUtils.MS_IN_SECOND);

        fakeNowInMilliseconds += (8 * TimeUtils.MS_IN_SECOND);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 1 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE + 9 * TimeUtils.MS_IN_SECOND);

        fakeNowInMilliseconds += (2 * TimeUtils.MS_IN_SECOND);
        assertTrue(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
    }

    @Test
    public void test_DifferentTimeControlsForEachColor() {
        Game game = new Game();
        game.setTimeLimitForGame(Common.COLOR_WHITE, 1);
        game.setTimeIncrementPerMove(Common.COLOR_WHITE, 2);
        game.setTimeLimitForGame(Common.COLOR_BLACK, 3);
        game.setTimeIncrementPerMove(Common.COLOR_BLACK, 4);

        game.playMoveList("e4 e5");
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), (1 * TimeUtils.MS_IN_MINUTE) + (2 * TimeUtils.MS_IN_SECOND));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), (3 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));

        game.playMoveList("d4 d5");
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), (1 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), (3 * TimeUtils.MS_IN_MINUTE) + (8 * TimeUtils.MS_IN_SECOND));

        fakeNowInMilliseconds += (2 * TimeUtils.MS_IN_MINUTE);
        assertTrue(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
    }

    @Test
    public void test_TimeControlsDoesNotChangeOnTakeback() {
        Game game = new Game();
        game.setTimeLimitForGame(2);
        game.setTimeIncrementPerMove(4);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));

        game.playMoveList("e4");

        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), (2 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), (2 * TimeUtils.MS_IN_MINUTE));

        fakeNowInMilliseconds += TimeUtils.MS_IN_MINUTE;

        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), (2 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), (1 * TimeUtils.MS_IN_MINUTE));

        game.playMoveList("e5");

        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), (2 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), (1 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));

        fakeNowInMilliseconds += TimeUtils.MS_IN_MINUTE;

        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), (1 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), (1 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));

        // after takeback the time gets back to when the clock started, and the clock holds
        game.takeback();
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), (2 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), (1 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));

        // another takeback does not change the time
        game.takeback();
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), (2 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), (1 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));

        // and the clock now runs again as white
        fakeNowInMilliseconds += TimeUtils.MS_IN_MINUTE;
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), (1 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), (1 * TimeUtils.MS_IN_MINUTE) + (4 * TimeUtils.MS_IN_SECOND));
    }

    @Test
    public void test_TimeControlsStartClockBeforeFirstMove() {
        Game game = new Game();
        game.setTimeLimitForGame(5);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 5 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 5 * TimeUtils.MS_IN_MINUTE);

        assertTrue(game.resumeClock());
        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);

        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 4 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 5 * TimeUtils.MS_IN_MINUTE);

        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);
        assertTrue(game.playMove("e4"));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 5 * TimeUtils.MS_IN_MINUTE);
    }

    @Test
    public void test_TimeControlsPauseAndResume() {
        LOGGER.info("test_TimeControlsPauseAndResume started");
        Game game = new Game();
        game.setTimeLimitForGame(4);
        game.setTimeIncrementPerMove(2);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 4 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE);

        assertTrue(game.playMoveList("e4 e5"));
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 4 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);

        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);

        game.pauseClock();
        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);

        game.resumeClock();
        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 2 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);

        game.playMove("Nf3");
        fakeNowInMilliseconds += (3 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 2 * TimeUtils.MS_IN_MINUTE + 4 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 1 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);

        game.pauseClock();
        fakeNowInMilliseconds += (3 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 2 * TimeUtils.MS_IN_MINUTE + 4 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 1 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);

        game.resumeClock();
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 2 * TimeUtils.MS_IN_MINUTE + 4 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 1 * TimeUtils.MS_IN_MINUTE + 2 * TimeUtils.MS_IN_SECOND);

        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 2 * TimeUtils.MS_IN_MINUTE + 4 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 2 * TimeUtils.MS_IN_SECOND);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));

        fakeNowInMilliseconds += (3 * TimeUtils.MS_IN_SECOND);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertTrue(game.isOutOfTime(Common.COLOR_BLACK));
    }

    @Test
    public void test_TimeControlsResetClock() {
        Game game = new Game();
        game.setTimeLimitForGame(3);
        game.setTimeIncrementPerMove(6);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE);

        assertTrue(game.playMoveList("e4 e5"));
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);

        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 2 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);

        game.resetClock(Common.COLOR_WHITE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);

        game.resumeClock();
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);

        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 2 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);

        fakeNowInMilliseconds += (2 * TimeUtils.MS_IN_MINUTE) +  7 * TimeUtils.MS_IN_SECOND;
        assertTrue(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
    }

    @Test
    public void test_TimeControlsSetClock() {
        Game game = new Game();
        game.setTimeLimitForGame(3);
        game.setTimeIncrementPerMove(6);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE);

        assertTrue(game.playMoveList("e4 e5"));
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 3 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);

        game.setTimeLeftMilliseconds(Common.COLOR_WHITE, 2 * TimeUtils.MS_IN_MINUTE);
        game.setTimeLeftMilliseconds(Common.COLOR_BLACK, 4 * TimeUtils.MS_IN_MINUTE);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 2 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE);

        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 1 * TimeUtils.MS_IN_MINUTE);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE);

        assertTrue(game.playMoveList("f4 f5"));
        fakeNowInMilliseconds += (1 * TimeUtils.MS_IN_MINUTE);
        assertFalse(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_WHITE), 6 * TimeUtils.MS_IN_SECOND);
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);

        fakeNowInMilliseconds += (7 * TimeUtils.MS_IN_SECOND);
        assertTrue(game.isOutOfTime(Common.COLOR_WHITE));
        assertFalse(game.isOutOfTime(Common.COLOR_BLACK));
        assertEquals(game.getTimeLeftMs(Common.COLOR_BLACK), 4 * TimeUtils.MS_IN_MINUTE + 6 * TimeUtils.MS_IN_SECOND);
    }

    @Test
    public void test_Draw_3rdrepetition() {
        Game game = new Game();
        game.setAutoDrawOn3rdRepetition(true);
        assertTrue(game.playMoveList("Nf3 Nf6 Ng1 Ng8 Nf3 Nf6 Ng1"));
        assertFalse(game.isEnded());

        assertTrue(game.playMoveList("Ng8"));
        assertTrue(game.isEnded());
        assertTrue(game.getWinner() == Common.COLOR_ILLEGAL);

        game = new Game();
        game.setAutoDrawOn3rdRepetition(true);
        assertTrue(game.playMoveList("e4"));
        assertTrue(game.playMoveList("Nf6 Nf3 Ng8 Ng1 Nf6 Nf3 Ng8"));
        assertFalse(game.isEnded());

        assertTrue(game.playMoveList("Ng1"));
        assertTrue(game.isEnded());
        assertTrue(game.getWinner() == Common.COLOR_ILLEGAL);
    }

    @Test
    public void test_Draw_3rdrepetition_CrazyHouse() {
        Game game = new Game(Common.GAME_KIND_CRAZY_HOUSE);
        game.setAutoDrawOn3rdRepetition(true);
        assertTrue(game.playMoveList("Nf3 Nf6 Ng1 Ng8 Nf3 Nf6 Ng1"));
        assertFalse(game.isEnded());

        assertTrue(game.playMoveList("Ng8"));
        assertTrue(game.isEnded());
        assertTrue(game.getWinner() == Common.COLOR_ILLEGAL);

        game = new Game(Common.GAME_KIND_CRAZY_HOUSE);
        game.setAutoDrawOn3rdRepetition(true);
        assertTrue(game.playMoveList("e4"));
        assertTrue(game.playMoveList("Nf6 Nf3 Ng8 Ng1 Nf6 Nf3 Ng8"));
        assertFalse(game.isEnded());

        assertTrue(game.playMoveList("Ng1"));
        assertTrue(game.isEnded());
        assertTrue(game.getWinner() == Common.COLOR_ILLEGAL);
    }

    @Test
    public void test_NoDraw_NoMaterial_CrazyHouse() {
        Game game = FEN.loadGame("7k/8/7K/8/8/8/8/8[-] w - - 0 1", true, Common.GAME_KIND_CRAZY_HOUSE);
        assertNotNull(game);
        game.setAutoDrawOnNoMaterial(true);
        assertTrue(game.playMove("Kg6"));
        assertFalse(game.isEnded());

        game = FEN.loadGame("7k/8/7K/8/8/8/8/8[r] w - - 0 1", true, Common.GAME_KIND_CRAZY_HOUSE);
        assertNotNull(game);
        game.setAutoDrawOnNoMaterial(true);
        assertTrue(game.playMove("Kg6"));
        assertFalse(game.isEnded());

        assertTrue(game.playMove("R@f6+"));
        assertFalse(game.isEnded());

        assertTrue(game.playMove("Kxf6"));
        assertFalse(game.isEnded());
        assertEquals(game.getWinner(), 0);
    }

    @Test
    public void test_Draw_Stalemate_CrazyHouse() {
        Game game = FEN.loadGame("8/8/8/8/8/6k1/5q2/7K[-] w - - 0 1", true, Common.GAME_KIND_CRAZY_HOUSE);
        assertNotNull(game);
        assertTrue(game.isEnded());

        game = FEN.loadGame("8/8/8/8/8/6k1/5q2/7K[N] w - - 0 1", true, Common.GAME_KIND_CRAZY_HOUSE);
        assertNotNull(game);
        assertFalse(game.isEnded());

        assertTrue(game.playMove("N@h3"));
        assertTrue(game.playMove("Kxh3"));
        assertTrue(game.isEnded());
        assertEquals(game.getWinner(), Common.COLOR_ILLEGAL);

    }

    @Test
    public void test_Bug_callsetAutoDrawOnNoMaterial_Exception()
    {
        Game game = new Game();
        game.setAutoDrawOn3rdRepetition(true);
        game.setAutoDrawOn50MovesRule(true);
        game.setAutoDrawOnNoMaterial(true);
        game.playMove("e4");
        game.takeback();
    }

    private void _testPGN(String pgn, String variant)
    {
        if (!Utils.isEmptyString(variant))
        {
            pgn = "[Variant \"" + variant + "\"]\n" + pgn;
        }
        LOGGER.fine("final PGN string '" + pgn + "'");
        Game game = PGN.loadGame(pgn);
        assertNotNull("failed to parse PGN '" + pgn + "'", game);

        LOGGER.fine("game is loaded from PGN");

        String gamePGN = PGN.getPGNString(game);
        LOGGER.fine("Got PGN  : '" + gamePGN + "'");
        // test PGNs are the same.
        // remove [] tags
        pgn = pgn.replaceAll("\\[.*\\]\n", "");
        gamePGN = gamePGN.replaceAll("\\[.*\\]\n", "");
        // remove comments
        pgn = pgn.replaceAll("\\{.*\\}", "");
        gamePGN = gamePGN.replaceAll("\\{.*\\}", "");
        // unify spaces
        pgn = pgn.replaceAll("\\s+", " ");
        gamePGN = gamePGN.replaceAll("\\s+", " ");
        // remove non ascii (UTF8 controls) control characters
        pgn = pgn.replaceAll("\\P{Cc}", "");
        gamePGN = gamePGN.replaceAll("\\P{Cc}", "");

        assertEquals(pgn, gamePGN);

        String fen = FEN.getFENString(game);
        LOGGER.fine("FEN String '" + fen + "'");
        Game fromFEN = FEN.loadGame(fen, true, game.getGameKind());
        assertNotNull("failed to parse FEN '" + fen + "'", fromFEN);
        if (game.isCrazyHouseOrBugHouse())
        {
            assertEquals(fromFEN.getCapturedPieces(Common.COLOR_WHITE), game.getCapturedPieces(Common.COLOR_WHITE));
            assertEquals(fromFEN.getCapturedPieces(Common.COLOR_BLACK), game.getCapturedPieces(Common.COLOR_BLACK));
        }
        else
        {
            assertTrue( fromFEN.getCapturedPieces(Common.COLOR_WHITE).isEmpty());
            assertTrue( fromFEN.getCapturedPieces(Common.COLOR_BLACK).isEmpty());
        }
        assertEquals(FEN.getFENString(fromFEN), fen);

        game.takebackAllMoves();
        assertTrue(game.getCapturedPiecesWhite().isEmpty());
        assertTrue(game.getCapturedPiecesBlack().isEmpty());
    }

    private void assertEqualGames(Game game1, Game game2)
    {
        assertEquals(game1.getGameKind(), game2.getGameKind());
        for (int x=1;x<=8;x++)
        {
            for (int y=1;y<=8;y++)
            {
                Piece piece1 = game1.getPieceAt(x,y);
                Piece piece2 = game2.getPieceAt(x,y);
                if (piece1 == null)
                {
                    assertNull(piece2);
                    continue;
                }
                assertEquals(piece1.getType(), piece2.getType());
                assertEquals(piece1.getColor(), piece2.getColor());
                if (game1.isCrazyOrBugHouse())
                {
                    assertEquals(piece1.isPromoted(), piece2.isPromoted());
                }
            }
        }
        // assertEquals(game1.getEpPawn(), game2.getEpPawn());
        assertEquals(game1.getMoveNumber(), game2.getMoveNumber());
        assertEquals(game1.getCurrentColor(), game2.getCurrentColor());
    }

    private void _testPGN(String pgn)
    {
        Game game = PGN.loadGame(pgn);
        assertNotNull("failed to parse PGN '" + pgn + "'", game);

        boolean bpgn = pgn.contains("1A.");

        if (! bpgn)
        {
            // verify moves count
            Pattern moveNumberPattern = Pattern.compile("\\s+(\\d+)\\.");
            Matcher m = moveNumberPattern.matcher(pgn);
            int moveNumber = 0;
            while (m.find())
            {
                moveNumber = Utils.parseInt(m.group(1));
            }
            // verify game number of moves
            if (game.getCurrentColor() == Common.COLOR_WHITE)
            {
                assertEquals(game.getMoveNumber(), moveNumber + 1);
                assertEquals(game.getCurrentMove(), (moveNumber * 2));
            }
            else
            {
                assertEquals(game.getMoveNumber(), moveNumber);
                assertEquals(game.getCurrentMove(), (moveNumber * 2) - 1);
            }
        }

        String gamePGN = PGN.getPGNString(game);
        LOGGER.fine("Got PGN  : '" + gamePGN + "'");
        // test PGNs are the same.
        // remove [] tags
        pgn = pgn.replaceAll("\\[.*\\]\n", "");
        gamePGN = gamePGN.replaceAll("\\[.*\\]\n", "");
        // remove comments
        pgn = pgn.replaceAll("\\{.*\\}", "");
        gamePGN = gamePGN.replaceAll("\\{.*\\}", "");
        // unify spaces
        pgn = pgn.replaceAll("\\s+", " ");
        gamePGN = gamePGN.replaceAll("\\s+", " ");
        // remove non ascii (UTF8 controls) control characters
        pgn = pgn.replaceAll("\\P{Cc}", "");
        gamePGN = gamePGN.replaceAll("\\P{Cc}", "");

        assertEquals(pgn, gamePGN);

        String fen = FEN.getFENString(game);
        LOGGER.fine("FEN String '" + fen + "'");
        Game fromFEN = FEN.loadGame(fen, true, game.getGameKind());
        assertEqualGames(fromFEN, game);
        if (game.isCrazyHouse())
        {
            // XFEN encoder captured, not dropped, and lose the type when dropping...
            if (FEN.ENCODE_CAPTURED_AS_XFEN)
            {
                // XFEN does not encode "was promoted"
                for (Piece piece : game.getCapturedPieces(Common.COLOR_WHITE))
                {
                    piece.clearPromoted();
                }
                for (Piece piece : game.getCapturedPieces(Common.COLOR_BLACK))
                {
                    piece.clearPromoted();
                }
                assertEquals(game.getCapturedPieces(Common.COLOR_WHITE), fromFEN.getCapturedPieces(Common.COLOR_WHITE));
                assertEquals(game.getCapturedPieces(Common.COLOR_BLACK), fromFEN.getCapturedPieces(Common.COLOR_BLACK));
            }
            else
            {
                assertEquals(game.getActualDroppablePieceTypes(Common.COLOR_WHITE), fromFEN.getActualDroppablePieceTypes(Common.COLOR_WHITE));
                assertEquals(game.getActualDroppablePieceTypes(Common.COLOR_BLACK), fromFEN.getActualDroppablePieceTypes(Common.COLOR_BLACK));

                List<Integer> droppableTypes1 = new ArrayList<Integer>();
                List<Integer> droppableTypes2 = new ArrayList<Integer>();
                for (Piece piece : fromFEN.getDroppablePieces(Common.COLOR_WHITE))
                {
                    droppableTypes1.add(piece.getTypeWhenDropping());
                }
                for (Piece piece : game.getDroppablePieces(Common.COLOR_WHITE))
                {
                    droppableTypes2.add(piece.getTypeWhenDropping());
                }
                assertEquals(droppableTypes1, droppableTypes2);
                droppableTypes1.clear();
                droppableTypes2.clear();
                for (Piece piece : fromFEN.getDroppablePieces(Common.COLOR_BLACK))
                {
                    droppableTypes1.add(piece.getTypeWhenDropping());
                }
                for (Piece piece : game.getDroppablePieces(Common.COLOR_BLACK))
                {
                    droppableTypes2.add(piece.getTypeWhenDropping());
                }
                assertEquals(droppableTypes1, droppableTypes2);
            }
        }
        else if (game.isBugHouse())
        {
            Game otherGame = game.getOtherGame();
            if (otherGame != null)
            {
                String otherFEN = FEN.getFENString(otherGame);
                LOGGER.fine("other FEN String '" + otherFEN + "'");
                Game fromOtherFEN = FEN.loadGame(otherFEN, true, otherGame.getGameKind());
                assertEqualGames(fromOtherFEN, otherGame);
            }
        }
        else
        {
            assertTrue( fromFEN.getCapturedPieces(Common.COLOR_WHITE).isEmpty());
            assertTrue( fromFEN.getCapturedPieces(Common.COLOR_BLACK).isEmpty());
            assertEquals(FEN.getFENString(fromFEN), fen);
        }

        LOGGER.fine("taking back all moves");

        Game otherGame = game.getOtherGame();
        if ((! game.isBugHouse()) || (otherGame == null))
        {
            game.takebackAllMoves();
            assertTrue(game.getCapturedPiecesWhite().isEmpty());
            assertTrue(game.getCapturedPiecesBlack().isEmpty());
            if (game.isCrazyHouse())
            {
                assertTrue(game.getDroppablePieces(Common.COLOR_WHITE).isEmpty());
                assertTrue(game.getDroppablePieces(Common.COLOR_BLACK).isEmpty());
            }
        }
        else
        {
            // take back all moves in both games - carefully
            while (game.canTakeback() || otherGame.canTakeback())
            {
                if (game.canTakeback())
                {
                    game.takeback();
                }
                if (otherGame.canTakeback())
                {
                    otherGame.takeback();
                }
            }

            LOGGER.fine("done taking back bughouse moves");
            assertTrue(game.getCapturedPiecesWhite().isEmpty());
            assertTrue(game.getCapturedPiecesBlack().isEmpty());
            assertTrue(game.getDroppablePieces(Common.COLOR_WHITE).isEmpty());
            assertTrue(game.getDroppablePieces(Common.COLOR_BLACK).isEmpty());
            assertTrue(otherGame.getCapturedPiecesWhite().isEmpty());
            assertTrue(otherGame.getCapturedPiecesBlack().isEmpty());
            assertTrue(otherGame.getDroppablePieces(Common.COLOR_WHITE).isEmpty());
            assertTrue(otherGame.getDroppablePieces(Common.COLOR_BLACK).isEmpty());
        }
    }

    public void _loadPGNFile(String filename, String variant)
    {
        int pgnsToTest = 100;
        int pgnsTested = 0;
        Scanner scanner = null;
        try
        {
            scanner = new Scanner(new FileInputStream(filename), "UTF-8");
        }
        catch (Exception ex) {
            Utils.exception(ex);
            assertTrue(false);
        }
        String line;
        String pgn = "";
        Game game;

        int index = 0;

        while (scanner.hasNextLine()) {
            line = scanner.nextLine();

            LOGGER.fine("parsing line '" + line + "'");
            if (line.startsWith("[Event "))
            {
                pgn = pgn.trim();
                if (! Utils.isEmptyString(pgn))
                {
                    index ++ ;

                    if (! Utils.isEmptyString(variant))
                    {
                        pgn = "[Variant \"" + variant + "\"]\n" + pgn;
                    }
                    LOGGER.info("parsing PGN " + pgnsTested + " : \n" + pgn);
                    _testPGN(pgn);

                    pgnsTested ++;
                    if (pgnsTested == pgnsToTest)
                    {
                        return;
                    }
                    pgn = "";
                    LOGGER.info("parsed PGNs " + pgnsTested + " / " + pgnsToTest);
                }
            }
            pgn = pgn + line + "\n";
        }
    }

    @Test
    public void testCrazyhousePGNsFile()
    {
        LOGGER.info("testCrazyhousePGNsFile started");
        _loadPGNFile("test/pgn_files/crazyhouse.pgn", "crazyhouse");
    }

    @Test
    public void testCrazyhousePGNsFileWithXFENEncoding()
    {
        LOGGER.info("testCrazyhousePGNsFileWithXFENEncoding started");
        FEN.ENCODE_CAPTURED_AS_XFEN = true;
        _loadPGNFile("test/pgn_files/crazyhouse.pgn", "crazyhouse");
        FEN.ENCODE_CAPTURED_AS_XFEN = false;
    }

    @Test
    public void testCrazyhousePGNsFileAsBugHouseStandAlonePGNs()
    {
        LOGGER.info("testCrazyhousePGNsFileAsBugHouseStandAlonePGNs started");
        _loadPGNFile("test/pgn_files/crazyhouse.pgn", "bughouse");
    }

    @Test
    public void testFishcer960PGNsFile()
    {
        LOGGER.info("testFishcer960PGNsFile started");
        _loadPGNFile("test/pgn_files/fischerandom.pgn", "fischerandom");
    }

    @Test
    public void testRegularPGNsFile()
    {
        LOGGER.info("testRegularPGNsFile started");
        _loadPGNFile("test/pgn_files/regular.pgn", "");
    }

    @Test
    public void testBugHousePGNsFile()
    {
        LOGGER.info("testBugHousePGNsFile started");
        _loadPGNFile("test/pgn_files/bughouse.pgn", "bughouse");
    }

    @Test
    public void testSinglePGN()
    {
        String pgn =
                "[Event \"Leipzig olm prel\"]\n" +
                        "[Site \"Leipzig\"]\n" +
                        "[Date \"1960.??.??\"]\n" +
                        "[Round \"2\"]\n" +
                        "[White \"Fuchs, Reinhart\"]\n" +
                        "[Black \"Veizaj\"]\n" +
                        "[Result \"1-0\"]\n" +
                        "[WhiteElo \"\"]\n" +
                        "[BlackElo \"\"]\n" +
                        "[ECO \"A42\"]\n" +
                        "\n" +
                        "1.d4 g6 2.c4 Bg7 3.Nc3 d6 4.e4 Nc6 5.Be3 Nf6 6.Nf3 Bg4 7.Be2 e5 8.d5 Bxf3\n" +
                        "9.Bxf3 Nd4 10.Bxd4 exd4 11.Qxd4 O-O 12.Qd2 Nd7 13.O-O Qh4 14.Rae1 a6 15.Bd1 b5\n" +
                        "16.cxb5 axb5 17.b4 Rfb8 18.Bb3 Ne5 19.f4 Nc4 20.Qd3 Qf6 21.Nxb5 Rxb5 22.Qxc4 Qd4+\n" +
                        "23.Qxd4 Bxd4+ 24.Kh1 f6 25.Rc1 Rxb4 26.Rxc7 Re8 27.Re1 Bf2 28.Re2 Rbxe4 29.Rxe4 Rxe4\n" +
                        "30.g3 Re2 31.Rc2 Re1+ 32.Kg2 Bc5 33.Kf3 Re3+ 34.Kg2 Kf7 35.Bc4 Ra3 36.Re2 Re3\n" +
                        "37.Rxe3 Bxe3 38.Kf3 Bc5 39.a4 Bg1 40.Bd3 Bc5 41.a5 h6 42.a6 g5 43.Kg4 Be3\n" +
                        "44.Kf5 gxf4 45.gxf4 h5 46.h4  1-0‬";
        _testPGN(pgn);
    }

    @Test
    public void testSinglePGN2()
    {
        String pgn =
                "[Variant \"crazyhouse\"]\n" +
                        "[Event \"fics rated crazyhouse match\"]\n" +
                        "[Site \"fics, Oklahoma City, OK USA\"]\n" +
                        "[Date \"2001.01.02\"]\n" +
                        "[White \"pminear\"]\n" +
                        "[WhiteElo \"2508\"]\n" +
                        "[Black \"GusMcClain\"]\n" +
                        "[BlackElo \"2213\"]\n" +
                        "[TimeControl \"60+0\"]\n" +
                        "[Result \"0-1\"]\n" +
                        "\n" +
                        "1. e4 Nf6 2. Nc3 d5 3. exd5 Nxd5 4. d4 e6 5. Nf3 Bb4 6. Bd2 \n" +
                        "Nc6 7. Bd3 Qe7 8. O-O f5 9. Nxd5 exd5 10. Bxb4 Nxb4 11. Bb5+ B@d7 \n" +
                        "12. Bxd7+ Bxd7 13. Ne5 N@h4 14. Nxd7 Qxd7 15. N@e5 P@f3 16. B@h5+ B@g6 17. Bxf3 \n" +
                        "Nxf3+ 18. Qxf3 N@h4 19. Qg3 Qe7 20. P@d7+ Kf8 21. B@g5 B@f6 22. Bxf6 gxf6 \n" +
                        "23. B@h6+ B@g7 24. B@c5 Bxh6 25. Bxe7+ Kxe7 26. Qxh4 B@g5 27. Nxg6+ hxg6 28. B@c5+ \n" +
                        "B@d6 29. Rfe1+ N@e4 30. Bxd6+ cxd6 31. d8=Q+ Raxd8 32. P@e5 dxe5 33. dxe5 fxe5 \n" +
                        "34. B@c5+ B@d6 35. Bxb4 Bxb4 36. Rxe4 dxe4 37. Qxg5+ Bxg5 38. Q@g7+ Q@f7 39. P@f6+ \n" +
                        "Bxf6 40. Qxf7+ Kxf7 41. N@e3 R@d1+ 42. B@f1 Rxf1# \n" +
                        "{pminear forfeits on time} 0-1‬";
        _testPGN(pgn);
    }

    @Test
    public void testSinglePGN3()
    {
        String pgn =
                "[Variant \"crazyhouse\"]\n" +
                        "[Event \"fics rated crazyhouse match\"]\n" +
                        "[Site \"fics, Oklahoma City, OK USA\"]\n" +
                        "[Date \"2000.01.01\"]\n" +
                        "[White \"jtp\"]\n" +
                        "[WhiteElo \"2272\"]\n" +
                        "[Black \"Firefly\"]\n" +
                        "[BlackElo \"2450\"]\n" +
                        "[TimeControl \"180+0\"]\n" +
                        "[Result \"0-1\"]\n" +
                        "\n" +
                        "1. e4 Nf6 2. Nc3 d5 3. exd5 Nxd5 4. Nf3 Nxc3 5. bxc3 Nc6 6. Bb5 \n" +
                        "Bd7 7. d4 e6 8. Rb1 N@e4 9. Qd3 P@d5 10. O-O a6 11. Bxc6 Bxc6 \n" +
                        "12. Ne5 B@b5 13. Qf3 Qe7 14. Re1 f6 15. P@f7+ Kd8 16. N@g8 Qd6 17. Bf4 \n" +
                        "fxe5 18. dxe5 Qd7 19. Rxb5 axb5 20. B@e8 N@d2 21. Bxd2 Nxd2 22. Qe2 Ne4 \n" +
                        "23. Bxd7 Bxd7 24. Q@e8+ Bxe8 25. fxe8=Q+ Kxe8 26. Qxb5+ B@c6 27. Qe2 Rxg8 28. P@b5 \n" +
                        "Bd7 29. P@a6 bxa6 30. bxa6 P@h3 31. P@b7 R@h1+ 32. Kxh1 hxg2+ 33. Kxg2 N@h4+ \n" +
                        "34. Kh3 Q@g2+ 35. Kxh4 B@g5+ 36. Kh5 g6# \n" +
                        "{jtp checkmated} 0-1‬";
        _testPGN(pgn);
    }

    @Test
    public void testSinglePGN4()
    {
        String pgn =
                "[Variant \"bughouse\"]\n" +
                        "[Event \"fics rated bughouse match\"]\n" +
                        "[Site \"fics, Oklahoma City, OK USA\"]\n" +
                        "[Date \"2001.11.10\"]\n" +
                        "[WhiteA \"LINDEGREN\"][WhiteAElo \"2416\"]\n" +
                        "[BlackA \"WhoAmI\"][BlackAElo \"2262\"]\n" +
                        "[WhiteB \"VABORIS\"][WhiteBElo \"2405\"]\n" +
                        "[BlackB \"LinusO\"][BlackBElo \"2310\"]\n" +
                        "[TimeControl \"180+0\"]\n" +
                        "[Result \"0-1\"]\n" +
                        "\n" +
                        "1A. e4 {179} 1a. e6 {180} 1B. e4 {178} 1b. e6 {180} 2A. d4 {178} 2a. Nc6 {180}\n" +
                        "2B. d4 {177} 3A. e5 {177} 2b. Nc6 {180} 3a. Nf6 {180} 3B. Nf3 {176}\n" +
                        "4A. exf6 {176} 3b. d6 {180} 4a. Qxf6 {180} 4B. Nc3 {176} 5A. Nc3 {176}\n" +
                        "4b. Nf6 {179} 5a. Qxd4 {179} 5B. Bd3 {175} 6A. Be3 {175} 6a. Qb4 {177}\n" +
                        "5b. Be7 {177} 6B. e5 {174} 7A. Rb1 {173} 6b. dxe5 {176} 7B. dxe5 {173}\n" +
                        "7a. P@d4 {174} 7b. Nd5 {174} 8A. Bxd4 {172} 8a. Qxd4 {173} 8B. Nxd5 {170}\n" +
                        "8b. exd5 {174} 9A. Nf3 {169} 9B. P@h6 {167} 9a. Qxd1+ {169} 10A. Rxd1 {169}\n" +
                        "9b. P@e4 {170} 10a. Nb4 {166} 10B. hxg7 {166} 10b. Rg8 {169} 11B. Q@h8 {165}\n" +
                        "11A. N@d4 {163} 11b. Rxh8 {162} 12B. gxh8=Q+ {163} 11a. P@c5 {160}\n" +
                        "12b. Q@f8 {160} 13B. P@g7 {163} 13b. exf3 {157} 14B. gxf8=Q+ {161}\n" +
                        "14b. Bxf8 {156} 12A. Ne4 {153} 15B. Bh6 {159} 15b. Qe7 {153} 12a. Q@g6 {154}\n" +
                        "13A. P@f5 {151} 13a. cxd4 {153} 14A. fxg6 {146} 14a. Nxc2+ {151} 15A. Kd2 {145}\n" +
                        "16B. P@g7 {145} 15a. Bb4+ {146} 16A. Kxc2 {144} 16a. hxg6 {144}\n" +
                        "17A. N@f6+ {142} 17a. Kd8 {142} 16b. N@e6 {139} 17B. N@f6+ {142} 17b. Kd8 {137}\n" +
                        "18B. gxf8=Q+ {141} 18b. Nxf8 {137} 18A. P@h7 {133} 19B. Bxf8 {140}\n" +
                        "18a. Be7 {138} 19b. Qxe5+ {133} 20B. P@e4 {137} 19A. P@c3 {127} 19a. dxc3 {136}\n" +
                        "20b. N@g6 {122} 21B. P@e7+ {131} 21b. Ncxe7 {120}{C:LinusO(2310)[60] kibitzes: \n" +
                        "high vs high then} 20A. bxc3 {90} 20a. N@a3+ {135} 21A. Kd2 {89}\n" +
                        "21a. B@c2 {133} 22A. Re1 {74} 22a. R@d1+ {131}";
        _testPGN(pgn);
    }

    /*
        http://www.bughouse-db.org/cgi-bin/searchbug.cgi?gID=3084897
     */
    @Test
    public void test_SinglePGN5()
    {
        String pgn = "[Variant \"bughouse\"]\n" +
            "[Event \"fics rated bughouse match\"]\n" +
            "[Site \"fics, Oklahoma City, OK USA\"]\n" +
            "[Date \"2001.11.10\"]\n" +
            "[WhiteA \"JKiller\"][WhiteAElo \"2589\"]\n" +
            "[BlackA \"marcusm\"][BlackAElo \"2234\"]\n" +
            "[WhiteB \"Darkfire\"][WhiteBElo \"2242\"]\n" +
            "[BlackB \"Gnejs\"][BlackBElo \"2588\"]\n" +
            "[TimeControl \"180+0\"]\n" +
            "[Result \"0-1\"]\n" +
            "\n" +
            "1A. e4 {179} 1B. e4 {179} 1a. Nf6 {179} 1b. Nf6 {179} 2A. Nc3 {178}\n" +
            "2a. d5 {179} 2B. Nc3 {177} 3A. exd5 {177} 3a. Nxd5 {179} 4A. Nf3 {177}\n" +
            "2b. P@d4 {177} 4a. e6 {179} 5A. d4 {176} 5a. Nc6 {178} 3B. P@g5 {174}\n" +
            "6A. Bd3 {175} 3b. dxc3 {177} 4B. gxf6 {174} 6a. Ndb4 {177} 4b. cxd2+ {176}\n" +
            "7A. O-O {174} 5B. Bxd2 {172} 5b. exf6 {176} 6B. Nf3 {172} 7a. Nxd3 {174}\n" +
            "8A. Qxd3 {174} 8a. P@g4 {173} 6b. Nc6 {175} 7B. Bb5 {170} 9A. Bg5 {171}\n" +
            "7b. N@e5 {172} 9a. gxf3 {169} 10A. Bxd8 {170} 8B. Bxc6 {167} 10a. fxg2 {169}\n" +
            "8b. Nxf3+ {171} 11A. Bxc7 {167} 9B. Qxf3 {164} 9b. dxc6 {170} 11a. N@h3+ {164}\n" +
            "10B. B@c4 {161} 12A. Qxh3 {165} 10b. P@e6 {169} 12a. gxf1=Q+ {161}\n" +
            "13A. Rxf1 {165} 13a. N@g5 {160} 11B. N@h5 {153} 14A. Qg4 {162} 11b. N@e5 {168}\n" +
            "14a. h5 {157} 12B. Qe2 {151} 15A. Qg3 {155} 15a. h4 {155} 16A. Qg4 {154}\n" +
            "16a. e5 {152} 17A. P@f5 {151}{C:VABORIS(2467)[134] kibitzes: winners?}\n" +
            "17a. Nxd4 {133} 18A. B@d5 {147}{C:Ebenfelt(FM)(CA)(2158)[134] kibitzes: winners\n" +
            " !!!} 18a. N@f3+ {130} 19A. Kh1 {146} 19a. h3 {128} 20A. P@g2 {139}\n" +
            "12b. Nxc4 {115} 13B. Qxc4 {149} 13b. P@d5 {114} 20a. hxg2+ {123}\n" +
            "21A. Qxg2 {139} 14B. exd5 {147} 14b. cxd5 {113} 21a. P@h3 {121} 15B. Qe2 {145}\n" +
            "22A. B@a4+ {134} 22a. N@c6 {119} 15b. Q@e4 {106} 23A. Qxh3 {126}\n" +
            "23a. Rxh3 {117} 24A. Bxe5 {88} 24a. Bxf5 {115} 25A. Baxc6+ {77} 25a. bxc6 {113}\n" +
            "26A. N@c7+ {76} 26a. Kd7 {112} 27A. Nxa8 {74} 27a. Nxe5 {109} 16B. B@b5+ {70}\n" +
            "16b. P@c6 {105}{C:DragonSlayr(2500)[138] whispers: hmm�DragonSlayr(2500)[138] w\n" +
            "hispers: i think darkfire/marcus just missed a win} 28A. Bg2 {46}\n" +
            "17B. Qxe4 {45} 17b. dxe4 {104} 28a. Rxh2+ {102} 18B. Bxc6+ {43}{C:Firefly(2518)\n" +
            "[138] whispers: well they got it back} 29A. Kg1 {45} 18b. bxc6 {103}\n" +
            "29a. Rxg2+ {101} 30A. Kxg2 {45} 19B. B@h6 {40} 30a. Q@h3+ {98} 19b. N@h4 {101}\n" +
            "{C:DragonSlayr(2500)[138] whispers: yes}";
        _testPGN(pgn);
    }

    @Test
    public void test_SinglePGN6()
    {
        String pgn = "[Event \"Budapest\"]\n" +
                "[Site \"Budapest\"]\n" +
                "[Date \"1926.07.02\"]\n" +
                "[Round \"9\"]\n" +
                "[White \"Steiner, Endre\"]\n" +
                "[Black \"Havasi, Kornel\"]\n" +
                "[Result \"1-0\"]\n" +
                "[WhiteElo \"\"]\n" +
                "[BlackElo \"\"]\n" +
                "[ECO \"A42\"]\n" +
                "\n" +
                "1.c4 g6 2.d4 Bg7 3.e4 d6 4.Nc3 c5 5.dxc5 Bxc3+ 6.bxc3 dxc5 7.Bd3 Nc6 8.f4 Qa5\n" +
                "9.Ne2 Be6 10.f5 O-O-O 11.fxe6 Ne5 12.exf7 Nf6 13.O-O Nxd3 14.Bh6 Ne5 15.Qb3 Nxf7  1-0?";
        _testPGN(pgn);
    }

    @Test
    public void testFENGameKindGuessing()
    {
        final Map<String, Integer> fens = new HashMap<String, Integer>();
        fens.put(FEN.FEN_INITIAL_POS, Common.GAME_KIND_REGULAR);
        fens.put(FEN.FEN_GRASSHOPER_POS, Common.GAME_KIND_GRASSHOPER);
        fens.put(FEN.FEN_MINICAPA_POS, Common.GAME_KIND_MINICAPA);
        fens.put("8/k7/8/8/7K/8/8/8", Common.GAME_KIND_REGULAR);
        fens.put("8/r7/8/8/7R/8/8/8", Common.GAME_KIND_FREEPLAY);
        fens.put("8/r7/8/8/K7/8/8/8", Common.GAME_KIND_FREEPLAY);
        fens.put("8/k7/8/8/7R/8/8/8", Common.GAME_KIND_FREEPLAY);
        fens.put("8/k7/8/8/6KG/8/8/8", Common.GAME_KIND_GRASSHOPER);
        fens.put("8/k6a/8/8/6K1/8/8/8", Common.GAME_KIND_MINICAPA);
        fens.put("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w aAhH - 0 1", Common.GAME_KIND_FISCHER);

        for (String fen : fens.keySet())
        {
            int gkind = fens.get(fen);
            LOGGER.info("testing FEN' " + fen + "' expected gkind " + Common.GAME_KIND_TEXT[gkind]);
            Game game = FEN.loadGame(fen, true, 0);
            assertNotNull(game);
            assertEquals(game.getGameKind(), gkind);
        }
    }

    @Test
    public void testCrazyHouseFENStrings()
    {
        Game game;
        game = FEN.loadGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR[-] w KQkq - 0 1", true, Common.GAME_KIND_CRAZY_HOUSE);
        assertNotNull(game);
        assertTrue(game.getCapturedPiecesWhite().isEmpty());
        assertTrue(game.getCapturedPiecesBlack().isEmpty());

        game = FEN.loadGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR/ w KQkq - 0 1", true, Common.GAME_KIND_CRAZY_HOUSE);
        assertNotNull(game);
        assertTrue(game.getCapturedPiecesWhite().isEmpty());
        assertTrue(game.getCapturedPiecesBlack().isEmpty());

        game = FEN.loadGame("r1b2n1Q~/pppp1kr1/4pNpp/1N2n3/1b6/4PPQP/PPP1BB2/R4K2[Qppr] w - - 0 37", true, Common.GAME_KIND_CRAZY_HOUSE);
        assertNotNull(game);
        assertTrue(game.getCapturedPiecesBlack().contains(Piece.create(Common.PIECE_TYPE_QUEEN, Common.COLOR_BLACK)));
        assertEquals(game.getCapturedPiecesBlack().size(), 1);
        assertEquals(Collections.frequency(game.getCapturedPiecesWhite(), Piece.create(Common.PIECE_TYPE_PAWN, Common.COLOR_WHITE)), 2);
        assertTrue(game.getCapturedPiecesWhite().contains(Piece.create(Common.PIECE_TYPE_ROOK, Common.COLOR_WHITE)));
        assertEquals(game.getCapturedPiecesWhite().size(), 3);

        FEN.ENCODE_CAPTURED_AS_XFEN = true;
        game = FEN.loadGame("r1b2n1Q~/pppp1kr1/4pNpp/1N2n3/1b6/4PPQP/PPP1BB2/R4K2/Qppr w - - 0 37", true, Common.GAME_KIND_CRAZY_HOUSE);
        FEN.ENCODE_CAPTURED_AS_XFEN = false;
        assertNotNull(game);
        assertTrue(game.getCapturedPiecesWhite().contains(Piece.create(Common.PIECE_TYPE_QUEEN, Common.COLOR_WHITE)));
        assertEquals(game.getCapturedPiecesBlack().size(), 3);
        assertEquals(Collections.frequency(game.getCapturedPiecesBlack(), Piece.create(Common.PIECE_TYPE_PAWN, Common.COLOR_BLACK)), 2);
        assertTrue(game.getCapturedPiecesBlack().contains(Piece.create(Common.PIECE_TYPE_ROOK, Common.COLOR_BLACK)));
        assertEquals(game.getCapturedPiecesWhite().size(), 1);
        String fen = FEN.getFENString(game);
        assertEquals("r1b2n1Q~/pppp1kr1/4pNpp/1N2n3/1b6/4PPQP/PPP1BB2/R4K2[PPRq] w - - 0 37", fen);

        FEN.ENCODE_CAPTURED_AS_XFEN = true;
        game = FEN.loadGame("1rq1k1nr/p1pp1ppp/b1p1p3/4N3/1b1PPB2/1PN5/P1PQ1PPP/R3K2R/Bn w KQk - 0 10", true, Common.GAME_KIND_CRAZY_HOUSE);
        FEN.ENCODE_CAPTURED_AS_XFEN = false;
        assertNotNull(game);
        assertTrue(game.playMove("N@b5"));

        game = FEN.loadGame("1rq1k1nr/p1pp1ppp/b1p1p3/4N3/1b1PPB2/1PN5/P1PQ1PPP/R3K2R[Bn] b KQk - 0 10", true, Common.GAME_KIND_CRAZY_HOUSE);
        assertNotNull(game);
        assertTrue(game.playMove("N@b5"));

        game = new Game(Common.GAME_KIND_CRAZY_HOUSE);
        assertTrue(game.playMoveList("e4 d5 exd5 Nc6 dxc6 e6 cxb7 Bxb7 N@c3 P@d3"));
        assertEquals("r2qkbnr/pbp2ppp/4p3/8/8/2Np4/PPPP1PPP/RNBQKBNR[PP] w KQkq - 0 6", FEN.getFENString(game));
        assertEquals("e2e4  d7d5  e4d5  b8c6  d5c6  e7e6  c6b7  c8b7  c3c3n d3d3p", game.getMoveListNum());
        assertEquals("e4 d5 exd5 Nc6 dxc6 e6 cxb7 Bxb7 N@c3 P@d3", game.getMoveListAlg());

        game = new Game(Common.GAME_KIND_CRAZY_HOUSE);
        assertTrue(game.playMoveList("Nf3 Nc6 e3 e6 Bb5 Nf6 d4 Bb4 c3"));
        assertEquals("r1bqk2r/pppp1ppp/2n1pn2/1B6/1b1P4/2P1PN2/PP3PPP/RNBQK2R[-] b KQkq - 0 5", FEN.getFENString(game));
        assertFalse(game.isEnded());
    }

}
