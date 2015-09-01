//==============================================================================
//            Copyright (c) 2009-2014 ichess.co.il
//
//This document contains confidential information which is protected by
//copyright and is proprietary to ichess.co.il. No part
//of this document may be used, copied, disclosed, or conveyed to another
//party without prior written consent of ichess.co.il.
//==============================================================================

package com.ichess.game;

import java.util.*;
import java.util.logging.Logger;

/**
 * Represents a Chess game. <br>
 * The game is by default initialized to the starting position, however you can load a custom position using
 * from FEN string using {@link FEN#loadGame FEN.loadGame} or load both position and moves from a PGN String
 * by using {@link PGN#loadGame PGN.loadGame}. <br>
 * By default a game does not have time control or limits. You can set time limit using
 * {@link #setTimeLimitForGame setTimeLimitForGame} and {@link #setTimeIncrementPerMove setTimeIncrementPerMove}.
 * You can check if time runs out using {@link #isOutOfTime isOutOfTime}. <br>
 *
 * @author Ran Berenfeld
 * @version 1.0
 */
public class Game {

    private final static Logger LOGGER = Logger.getLogger(Game.class.getName());

    public static final String CHECK_50_MOVES_DRAW = "check-50-moves-draw";
    public static final String CHECK_NO_MATERIAL_DRAW = "check-no-material-draw";
    public static final String CHECK_REPEATITION_DRAW = "check-repeatition-draw";
    // use this attribute to set a unique ID for the game
    public static final String GAME_ID_ATTRIBUTE = "game-id";
    // FEN initial position of the game
    public static final String INITIAL_POSITION_FEN = "initial-position-fen";
    static final String KING_LOCATION = "king-location";
    static final String RIGHT_ROOK_LOCATION = "right-rook-location";
    static final String LEFT_ROOK_LOCATION = "left-rook-location";
    // from FEN/PGN)
    // and increases after black move
    private Piece[][] _board = new Piece[8][8];
    private int _clockDuration[] = new int[Common.COLOR_NUM];
    // white and black clocks
    private long _clockStarted[] = new long[Common.COLOR_NUM];
    private int _currentColor = Common.COLOR_WHITE;
    private int _currentMove = 0; // actual move count starting from 1
    /*
     * end game support
     */
    private boolean _ended = false;
    private String _endString;
    private Pawn _epPawn; // pawn that just made 2 square jump
    private boolean _isPaused = false;
    private King _king[] = new King[Common.COLOR_NUM];
    private ArrayList<MoveInfo> _moveInfos = new ArrayList<MoveInfo>(40);
    private List<Piece> _whiteCaptured = new ArrayList<Piece>();
    private List<Piece> _blackCaptured = new ArrayList<Piece>();
    private Game _otherGame; // other game for bug house
    // by comma
    private MoveList _movelist = new MoveList();
    private int _moveNumber = 1; // display move number. starts with 1 (unless
    private Hashtable<String, Object> _properties = new Hashtable<String, Object>();
    private boolean _rated = false;
    // draw support
    private boolean _reqDraw[] = new boolean[Common.COLOR_NUM];
    // clock pause support
    private boolean _reqPause[] = new boolean[Common.COLOR_NUM];
    // takeback support
    private boolean _reqTakeback[] = new boolean[Common.COLOR_NUM];
    private int _startingColor = Common.COLOR_WHITE;
    private int[] _timeLimitForGame = new int[Common.COLOR_NUM];
    private int[] _timeIncrementForMove = new int[Common.COLOR_NUM];
    private int[] _timeLimitForMove = new int[Common.COLOR_NUM];
    /*
     * timed game support
     */
    private int _timeLeftMilliseconds[] = new int[Common.COLOR_NUM];
    private String _whiteName, _blackName;
    private int _winner = 0;
    private int _grules = Common.GAME_RULES_REGULAR;
    private int _gkind = Common.GAME_KIND_REGULAR;

    /**
     * Create a standard game of chess.
     */
    public Game() {
        this(null, null, Common.GAME_KIND_REGULAR);
    }

    /**
     * Create a game of chess with the given game kind
     * {@link com.ichess.game.Common#GAME_KIND_TEXT} For textual list of game kinds
     *
     * @param gkind - game kind
     */
    public Game(int gkind) {
        this(null, null, gkind);
    }

    /**
     * Create a game with the given white and black players
     *
     * @param whiteName - white player name
     * @param blackName - black player name
     */
    public Game(String whiteName, String blackName) {
        this(whiteName, blackName, Common.GAME_KIND_REGULAR);
    }

    /**
     * Create a game of chess with the given game kind
     * {@link com.ichess.game.Common#GAME_KIND_TEXT} For textual list of game kinds
     * Create a game with the given white and black players
     *
     * @param whiteName - white player name
     * @param blackName - black player name
     * @param gkind     - game kind
     */
    public Game(String whiteName, String blackName, int gkind) {
        _gkind = gkind;
        // set default game rules base on game kind
        switch (_gkind) {
            case Common.GAME_KIND_FISCHER:
                _grules = Common.GAME_RULES_FISCHER;
                break;
            case Common.GAME_KIND_SUICIDE:
                _grules = Common.GAME_RULES_SUICIDE;
                break;
            case Common.GAME_KIND_FREEPLAY:
                _grules = Common.GAME_RULES_FREEPLAY;
                break;
            case Common.GAME_KIND_CRAZY_HOUSE:
            case Common.GAME_KIND_BUG_HOUSE:
                _grules = Common.GAME_RULES_CRAZY_HOUSE;
                break;
            default:
                _grules = Common.GAME_RULES_REGULAR;
                break;
        }

        _whiteName = whiteName;
        _blackName = blackName;

        setAttribute(PGN.STR_DATE, PGN.formatDate(TimeUtils.now()));
        setAttribute(PGN.STR_RESULT, PGN.STR_RESULT_ONGOING);

        _timeLeftMilliseconds[Common.COLOR_WHITE] = 0;
        _timeLeftMilliseconds[Common.COLOR_BLACK] = 0;
        resetClock(Common.COLOR_WHITE);
        resetClock(Common.COLOR_BLACK);
        _reqTakeback[Common.COLOR_WHITE] = false;
        _reqTakeback[Common.COLOR_BLACK] = false;
        _reqDraw[Common.COLOR_WHITE] = false;
        _reqDraw[Common.COLOR_BLACK] = false;
        _reqPause[Common.COLOR_WHITE] = false;
        _reqPause[Common.COLOR_BLACK] = false;
        _ended = false;
        _winner = 0; // means in progress

        setAttribute(CHECK_50_MOVES_DRAW, false);
        setAttribute(CHECK_REPEATITION_DRAW, false);
        setAttribute(CHECK_NO_MATERIAL_DRAW, false);

        initialPosition();
    }

    public int getGameRules() {
        return _grules;
    }

    public void setGameRules(int grules) {
        if (isStarted()) {
            LOGGER.warning("can't change game rules of a started game");
        }
        _grules = grules;
    }

    public Game getOtherGame() {
        return _otherGame;
    }

    /**
     * Set the paired game in bug house mode only
     *
     * @param otherGame - the paired game in bug house mode only
     */
    public void setOtherGame(Game otherGame) {
        if (!isBugHouse()) {
            LOGGER.warning("Can't set other game - not bug house");
            return;
        }
        if (isStarted()) {
            LOGGER.warning("Can't set other game - already started");
            return;
        }
        Utils.AssertNotNull(otherGame);
        _otherGame = otherGame;
    }

    public boolean hasCapturedPieces()
    {
        return ( _whiteCaptured.size() != 0 ) || ( _blackCaptured.size() != 0 );
    }

    public List<Integer> getActualDroppablePieceTypes(int color)
    {
        List<Integer> result = new ArrayList<Integer>();
        if (! isCrazyOrBugHouse())
        {
            return result;
        }
        List<Piece> droppable = getDroppablePieces(color);
        for (Piece piece : droppable)
        {
            int dropType = piece.getTypeWhenDropping();
            if (! result.contains(dropType))
            {
                result.add(dropType);
            }
        }
        return result;
    }

    public List<Piece> getDroppablePieces(int color)
    {
        if (isCrazyHouse())
        {
            return getCapturedPieces(Common.OtherColor(color));
        }
        if (isBugHouse())
        {
            if (_otherGame != null)
            {
                return _otherGame.getCapturedPieces(color);
            }
            if (color == Common.COLOR_WHITE)
            {
                return new ArrayList<Piece>(
                        Arrays.asList(
                                Piece.create(Common.PIECE_TYPE_PAWN, Common.COLOR_WHITE),
                                Piece.create(Common.PIECE_TYPE_KNIGHT, Common.COLOR_WHITE),
                                Piece.create(Common.PIECE_TYPE_BISHOP, Common.COLOR_WHITE),
                                Piece.create(Common.PIECE_TYPE_ROOK, Common.COLOR_WHITE),
                                Piece.create(Common.PIECE_TYPE_QUEEN, Common.COLOR_WHITE)
                        )
                );

            }
            return new ArrayList<Piece>(
                    Arrays.asList(
                            Piece.create(Common.PIECE_TYPE_PAWN, Common.COLOR_BLACK),
                            Piece.create(Common.PIECE_TYPE_KNIGHT, Common.COLOR_BLACK),
                            Piece.create(Common.PIECE_TYPE_BISHOP, Common.COLOR_BLACK),
                            Piece.create(Common.PIECE_TYPE_ROOK, Common.COLOR_BLACK),
                            Piece.create(Common.PIECE_TYPE_QUEEN, Common.COLOR_BLACK)
                    )
            );
        }
        // not bughouse/crazyhouse. no droppable pieces
        return new ArrayList<Piece>();
    }
    
    /**
     * Return a list of the given color captured pieces
     *
     * @param color - a given color
     * @return a list of the given color captured pieces
     */
    public List<Piece> getCapturedPieces(int color) {
        return color == Common.COLOR_WHITE ? _whiteCaptured : _blackCaptured;
    }

    public boolean noCapturedPieces() {
        return getCapturedPiecesWhite().isEmpty() && getCapturedPiecesBlack().isEmpty();
    }

    public List<Piece> getCapturedPiecesWhite() {
        return getCapturedPieces(Common.COLOR_WHITE);
    }

    public List<Piece> getCapturedPiecesBlack() {
        return getCapturedPieces(Common.COLOR_BLACK);
    }

    public void setCapturedPieces(int color, List<Piece> pieces)
    {
        if (color == Common.COLOR_WHITE) {
            _whiteCaptured = pieces;
        } else {
            _blackCaptured = pieces;
        }
    }

    public void setDroppablePieces(int color, List<Piece> pieces)
    {
        if (isCrazyHouse()) {
            for (Piece piece : pieces)
            {
                Utils.Assert(piece.getColor() == Common.OtherColor(color));
            }
            if (color == Common.COLOR_WHITE) {
                _blackCaptured = pieces;
            } else {
                _whiteCaptured = pieces;
            }
        }
        else if (isBugHouse())
        {
            Game otherGame = getOtherGame();
            if (otherGame != null) {
                if (color == Common.COLOR_WHITE) {
                    otherGame._whiteCaptured = pieces;
                } else {
                    otherGame._blackCaptured = pieces;
                }
            }
        }
    }

    public int getGameKind() {
        return _gkind;
    }

    public void setGameKind(int gkind) {
        if (isStarted()) {
            LOGGER.warning("can't change game kind of a started game");
        }
        _gkind = gkind;
    }

    public boolean isFischer() {
        return _grules == Common.GAME_RULES_FISCHER;
    }

    public boolean isSuicide() {
        return _grules == Common.GAME_RULES_SUICIDE;
    }

    public boolean isFreePlay() {
        return _grules == Common.GAME_RULES_FREEPLAY;
    }

    public boolean isSuicideOrFreePlay() {
        return (_grules == Common.GAME_RULES_SUICIDE) || (_grules == Common.GAME_RULES_FREEPLAY);
    }

    public boolean isCrazyHouseOrBugHouse() {
        return _grules == Common.GAME_RULES_CRAZY_HOUSE;
    }

    public boolean isCrazyHouse() {
        return _gkind == Common.GAME_KIND_CRAZY_HOUSE;
    }

    public boolean isBugHouse() {
        return _gkind == Common.GAME_KIND_BUG_HOUSE;
    }

    public boolean isCrazyOrBugHouse() {
        return isCrazyHouse() || isBugHouse();
    }

    public boolean isGrassHopper() {
        return _gkind == Common.GAME_KIND_GRASSHOPER;
    }

    public boolean isMiniCapa() {
        return _gkind == Common.GAME_KIND_MINICAPA;
    }

    public boolean hasTypePiece(Vector<Piece> pieces, int type) {
        for (Piece piece : pieces) {
            if (piece.getType() == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ends this game because the given side aborted.
     *
     * @param color - the color that aborted this game.
     */
    public void abort(int color) {
        // TODO when do we store aborted games ?
        if (color == Common.COLOR_WHITE) {
            _endString = PGN.STR_WHITE_ABORT;
        } else {
            _endString = PGN.STR_BLACK_ABORT;
        }
        endGame(Common.COLOR_ILLEGAL);
    }

    @SuppressWarnings({"unchecked"})
    private boolean analyse() {


        if (_moveInfos.size() == (_currentMove + 1)) {
            return true;
        }
        MoveInfo newMoveInfo = new MoveInfo(this);
        int otherColor = Common.OtherColor(_currentColor);

        // check exactly 1 king for each size
        List<Piece> currentColorKings = findPieces(Common.PIECE_TYPE_KING, _currentColor);
        List<Piece> otherColorKings = findPieces(Common.PIECE_TYPE_KING, otherColor);

        if (!isSuicideOrFreePlay()) {
            if (currentColorKings.size() == 0) {
                LOGGER.fine( "no moving king - analyze failed");

                return false;
            }

            if (currentColorKings.size() > 1) {
                LOGGER.fine( "more then 1 moving king - analyze failed");

                return false;
            }

            if (otherColorKings.size() == 0) {
                LOGGER.fine( "no other king - analyze failed");

                return false;
            }

            if (otherColorKings.size() > 1) {
                LOGGER.fine( "more then 1 other king - analyze failed");

                return false;
            }
        }

        // check that there are no pawns on lines 1 or 8
        List<Piece> currentColorPawns = findPieces(Common.PIECE_TYPE_PAWN, _currentColor);
        List<Piece> otherColorPawns = findPieces(Common.PIECE_TYPE_PAWN, otherColor);

        for (Piece pawn : currentColorPawns) {
            if ((pawn.getX() == 1) || (pawn.getX() == 8)) {
                LOGGER.fine( "moving pawn on row 1 or 8 - analyze failed");

                return false;
            }
        }

        for (Piece pawn : otherColorPawns) {
            if ((pawn.getX() == 1) || (pawn.getX() == 8)) {
                LOGGER.fine( "other pawn on row 1 or 8 - analyze failed");

                return false;
            }
        }

        int kingX = 0;
        int kingY = 0;

        Piece movingKing = null;
        Piece otherKing = null;

        if (!isSuicideOrFreePlay()) {
            _king[_currentColor] = (King) Utils.getFirstInList(currentColorKings);
            _king[otherColor] = (King) Utils.getFirstInList(otherColorKings);

            movingKing = _king[_currentColor];
            otherKing = _king[otherColor];

            kingX = movingKing.getX();
            kingY = movingKing.getY();
        }

        // iterate, find all the moving and other pieces
        Vector<Piece>[] pieces = new Vector[Common.COLOR_NUM];
        pieces[Common.COLOR_WHITE] = new Vector<Piece>();
        pieces[Common.COLOR_BLACK] = new Vector<Piece>();

        Vector<Piece> otherPieces = pieces[otherColor];
        Vector<Piece> movingPieces = pieces[_currentColor];
        // calculate readability for all pieces
        for (int x = 8; x != 0; x--) {
            for (int y = 8; y != 0; y--) {
                Piece piece = getPieceAt(x, y);
                if (piece == null) {
                    continue;
                }
                piece.setPinned(false);
                piece.clearReachability();
                piece.doCalcReachability(this);
                pieces[piece.getColor()].add(piece);
            }
        }
        if (!isSuicideOrFreePlay()) {
            if (_currentMove == 0) {

                // this is the very first move. so also check that the other color
                // is not checked.
                // if it is - fail the analyse
                for (Piece movingPiece : movingPieces) {
                    movingPiece.setCheckPin(true);
                    LOGGER.fine(_grules + " piece " + movingPiece.getType() + ":" + movingPiece.getColor() + "," + movingPiece.getPinningPiece()
                            + " " + movingPiece.getX() + "," + movingPiece.getY());
                    if (movingPiece.canMoveTo(otherKing.getX(), otherKing.getY(), this)) {
                        LOGGER.fine( "other side in check - analyse failed");

                        return false;
                    }
                }
            }
        }

        // check if moving side is in check
        newMoveInfo.setCheck(false);
        newMoveInfo.setDoubleCheck(false);
        Piece checkingPiece = null;
        Piece oldPiece = null;
        boolean floatCheck = false;

        if (!isSuicideOrFreePlay()) {
            int checkCount = 0;
            for (Piece otherPiece : otherPieces) {
                otherPiece.setCheckPin(true);
                if (otherPiece.canMoveTo(kingX, kingY, this)) {
                    if (newMoveInfo.isCheck()) {
                        newMoveInfo.setDoubleCheck(true);
                    } else {
                        newMoveInfo.setCheck(true);
                    }
                    oldPiece = checkingPiece;
                    checkingPiece = otherPiece;
                    checkCount++;
                    if (checkCount == 2) {
                        floatCheck = true;
                        if (checkingPiece.isGrasshoper()) { // double check grasshopper
                            checkingPiece = oldPiece;
                            oldPiece = otherPiece; // grasshopper
                        }
                    }
                }
                otherPiece.setCheckPin(false);
            }
        }

        if (newMoveInfo.isCheck()) {

            newMoveInfo.setCheckMate(true);
            // moving side is in check. find all valid moved to get out of check
            // first test is to move the king
            for (int x = (kingX - 1); x <= (kingX + 1); x++) {
                for (int y = (kingY - 1); y <= (kingY + 1); y++) {
                    if ((x < 1) || (x > 8) || (y < 1) || (y > 8)) {
                        continue;
                    }
                    Piece piece = getPieceAt(x, y);
                    if (piece != null) {
                        if (piece.getColor() == _currentColor) {
                            // the king can't move there
                            continue;
                        }
                    }
                    // check if other pieces can move here by removing temporary
                    // from the board and moving the king there
                    _board[x - 1][y - 1] = movingKing;
                    _board[kingX - 1][kingY - 1] = null;
                    boolean otherGuard = false;
                    for (Piece otherPiece : otherPieces) {
                        if (otherPiece.canMoveTo(x, y, this)) {
                            otherGuard = true;
                            break;
                        }
                    }
                    _board[x - 1][y - 1] = piece;
                    _board[kingX - 1][kingY - 1] = movingKing;

                    if (otherGuard) {
                        // still in check mate
                        continue;
                    }

                    // found a valid move for the king
                    newMoveInfo.addValidMove(movingKing, x, y);
                    newMoveInfo.setCheckMate(false);
                }
            }

            int checkingX = checkingPiece.getX();
            int checkingY = checkingPiece.getY();

            // if not float check, see if someone can capture the checking
            // piece
            if (!floatCheck) {

                for (Piece movingPiece : movingPieces) {
                    if (movingPiece.isKing()) {
                        continue;
                    }
                    // can capture if not (pinned and pinning piece is not
                    // checking piece)
                    if (movingPiece.isReachable(checkingX, checkingY)
                            && (!(movingPiece.isPinned() && movingPiece.getPinningPiece() != checkingPiece))) {
                        newMoveInfo.addValidMove(movingPiece, checkingX, checkingY);
                        newMoveInfo.setCheckMate(false);
                    }
                }

                // if checking piece is a pawn that just made 2 jump move, maybe
                // it can be captured with ep
                if (checkingPiece.isPawn()) {
                    if (getEpPawn() != null) {
                        int epCaptureX = getEpPawn().getX();
                        if (_currentColor == Common.COLOR_WHITE) {
                            epCaptureX++;
                        } else {
                            epCaptureX--;
                        }
                        for (Piece movingPiece : movingPieces) {
                            if (!movingPiece.isPawn()) {
                                continue;
                            }
                            // can capture if not (pinned and pinning piece is
                            // not checking piece)
                            if (movingPiece.isReachable(epCaptureX, checkingY) && (!(movingPiece.isPinned()))) {

                                newMoveInfo.addValidMove(movingPiece, epCaptureX, checkingY);
                                newMoveInfo.setCheckMate(false);
                            }
                        }
                    }
                }
            } else if (oldPiece.isGrasshoper()) { // double check on grasshopper

                for (Piece movingPiece : movingPieces) {
                    if (movingPiece.isKing()) {
                        continue;
                    }
                    // can capture if not (pinned and pinning piece is not
                    // checking piece)
                    if (movingPiece.isReachable(checkingX, checkingY)
                            && (!(movingPiece.isPinned() && movingPiece.getPinningPiece() != checkingPiece))) {
                        // verify that the grass doens't check anymore
                        int X = movingPiece.getX();
                        int Y = movingPiece.getY();
                        _board[X - 1][Y - 1] = null;
                        if (!oldPiece.canMoveTo(kingX, kingY, this)) { // doesn't check
                            newMoveInfo.addValidMove(movingPiece, checkingX, checkingY);
                            newMoveInfo.setCheckMate(false);
                        }
                        _board[X - 1][Y - 1] = movingPiece;
                    }
                }

                // if checking piece is a pawn that just made 2 jump move, maybe
                // it can be captured with ep
                if (checkingPiece.isPawn()) {
                    if (getEpPawn() != null) {
                        int epCaptureX = getEpPawn().getX();
                        if (_currentColor == Common.COLOR_WHITE) {
                            epCaptureX++;
                        } else {
                            epCaptureX--;
                        }
                        for (Piece movingPiece : movingPieces) {
                            if (!movingPiece.isPawn()) {
                                continue;
                            }
                            // can capture if not (pinned and pinning piece is
                            // not checking piece)
                            if (movingPiece.isReachable(epCaptureX, checkingY) && (!(movingPiece.isPinned()))) {

                                newMoveInfo.addValidMove(movingPiece, epCaptureX, checkingY);
                                newMoveInfo.setCheckMate(false);
                            }
                        }
                    }
                }
            }

            // if not float check, and checking piece is not knight, maybe someone
            // can block the check
            boolean knightCheck = false;

            if (checkingPiece.isKnight() || (checkingX != kingX && checkingY != kingY && Math.abs(checkingX - kingX) != Math.abs(checkingY - kingY))) {
                knightCheck = true;
            }
            if ((!knightCheck) && (!floatCheck)) {
                int dirX = kingX > checkingX ? 1 : kingX < checkingX ? -1 : 0;
                int dirY = kingY > checkingY ? 1 : kingY < checkingY ? -1 : 0;

                int x = checkingX + dirX;
                int y = checkingY + dirY;
                while ((x != kingX) || (y != kingY)) {
                    for (Piece movingPiece : movingPieces) {
                        if (movingPiece.isKing()) {
                            continue;
                        }
                        if (movingPiece.isReachable(x, y) && (!movingPiece.isPinned())) {
                            LOGGER.fine("in the way." + x + "," + y + ":" + kingX + "," + kingY + "~" + dirX + "," + dirY);
                            if (checkingPiece.isGrasshoper()) {
                                if ((x == kingX - dirX) && (y == kingY - dirY)) {
                                    continue; // capture piece in front of the king doesn't help
                                }
                            }
                            LOGGER.fine("adding blocking move by " + movingPiece.getType() + " to " + x + "," + y);
                            newMoveInfo.addValidMove(movingPiece, x, y);
                            newMoveInfo.setCheckMate(false);
                        } // maybe the checking piece is a grasshopper - then some piece might "go away" from the king
                    }

                    // in crazy house and bug house, any piece can be dropped in the middle
                    // in crazy house, check if we have droppable piece.
                    // in bug house, don't check
                    // in both cases, add drop any move
                    if (isCrazyOrBugHouse())
                    {
                        boolean addDropAnyMove = true;
                        if (isCrazyHouse())
                        {
                            List<Piece> droppable = getDroppablePieces(_currentColor);
                            addDropAnyMove = ! droppable.isEmpty();
                        }
                        if (addDropAnyMove)
                        {
                            Piece droppedPiece = Piece.create(Common.PIECE_TYPE_DROP_ANY, _currentColor);
                            LOGGER.fine("adding drop move by " + droppedPiece.getType() + " color " + Common.getColor(droppedPiece.getColor()) + " to " + x + "," + y);
                            newMoveInfo.addValidMove(droppedPiece, x, y, true);
                            newMoveInfo.setCheckMate(false);
                        }
                    }
                    x += dirX;
                    y += dirY;
                }

                if (checkingPiece.isGrasshoper()) {
                    Piece saver = _board[kingX - dirX - 1][kingY - dirY - 1];
                    if (saver != null && saver.isColor(_currentColor)) {
                        for (int a = 1; a < 9; a++) { // if he can move anywhere - it won't be check
                            for (int b = 1; b < 9; b++) {
                                if (saver.isReachable(a, b)) {
                                    LOGGER.fine("adding saver " + checkingX + "," + checkingY);
                                    newMoveInfo.addValidMove(saver, a, b);
                                    newMoveInfo.setCheckMate(false);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // not in check. verify that not in stalemate, by finding other
            // pieces that are not tied and can move
            newMoveInfo.setStaleMate(true);

            for (Piece movingPiece : movingPieces) {
                if (movingPiece.isKing()) {
                    // check standard king moves

                    // in suicide there can be multiple kings
                    kingX = movingPiece.getX();
                    kingY = movingPiece.getY();

                    for (int x = (kingX - 1); x <= (kingX + 1); x++) {
                        for (int y = (kingY - 1); y <= (kingY + 1); y++) {
                            if ((x < 1) || (x > 8) || (y < 1) || (y > 8)) {
                                continue;
                            }
                            // verify that the king is not moving into check by
                            // moving the king
                            Piece destPiece = getPieceAt(x, y);
                            if (destPiece != null) {
                                if (_currentColor == destPiece.getColor()) {
                                    // 	king can't move there
                                    continue;
                                }
                            }

                            boolean kingCanMove = true;

                            if (!isSuicideOrFreePlay()) {
                                // move the king and see if other piece can now
                                // attack it
                                _board[x - 1][y - 1] = movingPiece;
                                _board[kingX - 1][kingY - 1] = null;

                                for (Piece otherPiece : otherPieces) {
                                    if (otherPiece.canMoveTo(x, y, this)) {
                                        kingCanMove = false;
                                        break;
                                    }
                                }
                                _board[x - 1][y - 1] = destPiece;
                                _board[kingX - 1][kingY - 1] = movingKing;
                            }

                            if (kingCanMove) {
                                newMoveInfo.setStaleMate(false);
                                newMoveInfo.addValidMove(movingPiece, x, y);
                            }
                        }
                    }

                    // check special king move - castling
                    if (!isSuicideOrFreePlay()) {
                        if (!movingPiece.isMoved()) { // king didn't move

                            // check long castle
                            int RookInit = (Integer) getAttribute(LEFT_ROOK_LOCATION);
                            int KingInit = (Integer) getAttribute(KING_LOCATION);
                            int KingDest = 3; // preparation for Capablanca chess
                            int RookDest = 4;
                            do {
                                Piece rook = getPieceAt(kingX, RookInit);
                                if (rook == null) {
                                    break;
                                }
                                if (!rook.isRook()) {
                                    break;
                                }
                                if (!rook.isColor(_currentColor)) {
                                    break;
                                }
                                if (rook.isMoved()) {
                                    break;
                                }
                                boolean canCastle = true;
                                // rook didnt move
                                /* need to check 2 more things:
                                * 1. No square between the king's initial and final squares (including the initial and final
								* squares) may be under attack by an enemy piece.
								* 2. All the squares between the king's initial and final squares (including the final square), 
								* and all of the squares between the rook's initial and final squares (including the final square), 
								* must be vacant except for the king and castling rook. (An equivalent way of stating this is: 
								* the smallest back rank interval containing the king, the castling rook, and their destination 
								* squares, contains no pieces other than the king and castling rook.)
								*/
                                int leftSquare = Math.min(KingDest, RookInit);
                                int rightSquare = Math.max(RookDest, KingInit);
                                for (int col = leftSquare; col <= rightSquare; col++) {
                                    Piece pieceBetween = getPieceAt(kingX, col);
                                    if (pieceBetween != null) { // condition 2
                                        if (!(col == RookInit || col == KingInit)) { // not the initial rook or king
                                            canCastle = false;
                                            break;
                                        }
                                    }
                                    if ((col >= KingDest && col <= KingInit) && (!isSuicideOrFreePlay())) { // condition 1
                                        for (Piece otherPiece : otherPieces) {
                                            if (otherPiece.canMoveTo(kingX, col, this)) {
                                                canCastle = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (canCastle) {
                                    if (KingDest != KingInit && KingDest != RookInit) {
                                        newMoveInfo.addValidMove(movingPiece, kingX, KingDest);
                                    }
                                    if (isFischer()) {
                                        newMoveInfo.addValidMove(movingPiece, kingX, RookInit); // allow castling by king + rook press
                                    }
                                    LOGGER.fine("gkind " + Common.GAME_KIND_TEXT[getGameKind()] + " allowing O-O-O of " + Common.getColor(this._currentColor) +
                                            " king from " + kingX + "," + kingY + " to " + KingDest + " or " + RookInit);
                                }
                            } while (false);
                            // check short castle
                            do {
                                RookInit = (Integer) getAttribute(RIGHT_ROOK_LOCATION);
                                Piece rook = getPieceAt(kingX, RookInit);
                                if (rook == null) {
                                    break;
                                }
                                if (!rook.isRook()) {
                                    break;
                                }
                                if (!rook.isColor(_currentColor)) {
                                    break;
                                }
                                if (rook.isMoved()) {
                                    break;
                                }
                                boolean canCastle = true;

                                KingDest = 7;
                                RookDest = 6;
                                int leftSquare = Math.min(RookDest, KingInit);
                                int rightSquare = Math.max(KingDest, RookInit);
                                for (int col = leftSquare; col <= rightSquare; col++) {
                                    Piece pieceBetween = getPieceAt(kingX, col);
                                    if (pieceBetween != null) { // condition 2
                                        if (!(col == RookInit || col == KingInit)) { // not the initial rook or king
                                            canCastle = false;
                                            break;
                                        }
                                    }
                                    if ((col <= KingDest && col >= KingInit) && (!isSuicideOrFreePlay())) { // condition 1
                                        for (Piece otherPiece : otherPieces) {
                                            if (otherPiece.canMoveTo(kingX, col, this)) {
                                                canCastle = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (canCastle) {
                                    if (KingDest != KingInit && KingDest != RookInit) {
                                        newMoveInfo.addValidMove(movingPiece, kingX, KingDest);
                                    }
                                    if (isFischer()) {
                                        newMoveInfo.addValidMove(movingPiece, kingX, RookInit); // allow castling by king + rook press
                                    }
                                    LOGGER.fine("grules " + Common.GAME_RULES_TEXT[getGameRules()] + " allowing O-O of " + Common.getColor(this._currentColor) +
                                            " king from " + kingX + "," + kingY + " to " + KingDest + " or " + RookInit);
                                }
                            } while (false);
                        }
                    }
                } else {
                    // piece is not a king. see if it can move somewhere
                    LOGGER.fine("PIECE:" + movingPiece.getX() + ", " + movingPiece.getY() + " @ " + movingPiece.getColor() + ":" + movingPiece.getType());
                    for (int x = 8; x != 0; x--) {
                        for (int y = 8; y != 0; y--) {
                            boolean badMoveGrassHopperIntoCheck = false;

                            // need to check that you don't move into check !@!
                            if ((hasTypePiece(otherPieces, Common.PIECE_TYPE_GRASSHOPER)) &&
                                    (!isSuicideOrFreePlay())) {
                                int mx = movingPiece.getX();
                                int my = movingPiece.getY();
                                Piece temp = _board[x - 1][y - 1];
                                _board[x - 1][y - 1] = movingPiece;
                                _board[mx - 1][my - 1] = null;

                                King king = ((King) Utils.getFirstInList(currentColorKings));
                                kingX = ((King) Utils.getFirstInList(currentColorKings)).getX();
                                kingY = ((King) Utils.getFirstInList(currentColorKings)).getY();

                                for (Piece otherPiece : otherPieces) {
                                    if (otherPiece.canMoveTo(kingX, kingY, this)) {
                                        if (otherPiece.getX() != x || otherPiece.getY() != y) { // not captured
                                            badMoveGrassHopperIntoCheck = true;
                                            break; // can't move there
                                        }
                                    }
                                }
                                _board[x - 1][y - 1] = temp;
                                _board[mx - 1][my - 1] = movingPiece;
                                if (badMoveGrassHopperIntoCheck) {
                                    continue;
                                }
                            }

                            if (movingPiece.isPinned()) {
                                // the piece can only move within the pinning
                                Piece PinningPiece = movingPiece.getPinningPiece();
                                Utils.AssertNotNull(PinningPiece);
                                if (movingPiece.isReachable(x, y)) {
                                    // the moving point should fall exactly
                                    // inside the path from
                                    // the pinning piece to the king
                                    if (Utils.isInPath(x, y, PinningPiece.getX(), PinningPiece.getY(), kingX, kingY)) {

                                        // valid move within the pin
                                        newMoveInfo.setStaleMate(false);
                                        newMoveInfo.addValidMove(movingPiece, x, y);
                                    }
                                }
                            } else {
                                if (movingPiece.isReachable(x, y)) {
                                    newMoveInfo.setStaleMate(false);
                                    newMoveInfo.addValidMove(movingPiece, x, y);
                                }
                            }
                        }
                    }
                }
            }

            // in crazy house and bug house, any piece can be dropped in the middle
            // in crazy house, check if we have droppable piece.
            // in bug house, don't check
            // in both cases, add drop any move
            if (isCrazyOrBugHouse())
            {
                boolean addDropAnyMove = true;
                if (isCrazyHouse())
                {
                    List<Piece> droppable = getDroppablePieces(_currentColor);
                    addDropAnyMove = ! droppable.isEmpty();
                }
                if (addDropAnyMove)
                {
                    // there is a piece to drop. its not a stale mate
                    newMoveInfo.setStaleMate(false);
                    Piece droppedPiece = Piece.create(Common.PIECE_TYPE_DROP_ANY, _currentColor);
                    LOGGER.fine("move " + _currentMove + " color " + _currentColor + " adding drop moves in all squares");
                    // piece can be dropped anywhere
                    for ( int x = 8 ;x != 0 ; x-- )
                    {
                        for ( int y = 8 ;y != 0 ; y-- )
                        {
                            if (getPieceAt(x,y) == null)
                            {
                                newMoveInfo.addValidMove(droppedPiece, x, y, true);
                            }
                        }
                    }
                }
            }
        }

        // in suicide if there is a valid capture move then remove all non capture moves
        if (isSuicide()) {
            boolean hasCaptureMove = false;
            for (Move nextMove : newMoveInfo.getValidNextMoves()) {
                if (nextMove.getCapturedPiece() != null) {
                    hasCaptureMove = true;
                    break;
                }
            }

            if (hasCaptureMove) {
                List<Move> newValidNextMoves = new ArrayList<Move>();

                for (Move nextMove : newMoveInfo.getValidNextMoves()) {
                    LOGGER.fine("checking move " + nextMove.getFromX() + "," + nextMove.getFromY() + " - " +
                            nextMove.getToX() + "," + nextMove.getToY());

                    Piece capturedPiece = nextMove.getCapturedPiece();
                    if (capturedPiece != null) {
                        LOGGER.fine("move " + nextMove.getFromX() + "," + nextMove.getFromY() + " - " +
                                nextMove.getToX() + "," + nextMove.getToY() + " has captured piece");

                        newValidNextMoves.add(nextMove);
                    }
                }
                newMoveInfo.getValidNextMoves().clear();
                newMoveInfo.getValidNextMoves().addAll(newValidNextMoves);
            }
        }

        if (newMoveInfo.isCheckMate()) {
            if (_currentColor == Common.COLOR_WHITE) {
                _endString = PGN.STR_WHITE_MATE;
            } else {
                _endString = PGN.STR_BLACK_MATE;
            }
            endGame(Common.OtherColor(_currentColor));
        }
        if (newMoveInfo.isStaleMate()) {
            _endString = PGN.STR_STALEMATE;
            if (isSuicide()) {
                // in suicide game if you enter stale mate you win
                endGame(_currentColor);
                if (_currentColor == Common.COLOR_WHITE) {
                    _endString = PGN.STR_WHITE_WIN;
                } else {
                    _endString = PGN.STR_BLACK_WIN;
                }
            } else if (isFreePlay()) {
                // in free play game if you enter stale mate you win
                endGame(Common.OtherColor(_currentColor));
            } else {
                // in normal game stale mate is a draw
                endGame(Common.COLOR_ILLEGAL);
            }
        }

        if (movingPieces.isEmpty()) {
            boolean white = _currentColor == Common.COLOR_WHITE;
            if (isFreePlay()) {
                white = !white;
            }
            if (white) {
                _endString = PGN.STR_WHITE_WIN;
            } else {
                _endString = PGN.STR_BLACK_WIN;
            }
            if (isFreePlay()) {
                endGame(otherColor);
            } else {
                endGame(_currentColor);
            }
        }

        if ((Boolean) getAttribute(CHECK_NO_MATERIAL_DRAW)) {
            // calculate having enough material

            for (int color = Common.COLOR_WHITE; color <= Common.COLOR_BLACK; color++) {
                // in bug house and crazy house there is always material
                if (isCrazyOrBugHouse())
                {
                    newMoveInfo.getHasEnoughMaterial()[color] = true;
                    continue;
                }

                newMoveInfo.getHasEnoughMaterial()[color] = false;
                int numberOfBishops = 0, numberOfKnights = 0;
                for (Piece movingPiece : pieces[color]) {
                    switch (movingPiece.getType()) {
                        case Common.PIECE_TYPE_PAWN:
                        case Common.PIECE_TYPE_ROOK:
                        case Common.PIECE_TYPE_QUEEN:
                        case Common.PIECE_TYPE_ARCHBISHOP:
                        case Common.PIECE_TYPE_CHANCELLOR:
                            newMoveInfo.getHasEnoughMaterial()[color] = true;
                            break;
                        case Common.PIECE_TYPE_KING:
                            break;
                        case Common.PIECE_TYPE_BISHOP:
                        case Common.PIECE_TYPE_GRASSHOPER: // this seems ok to count it as bishop
                            numberOfBishops++;
                            break;
                        case Common.PIECE_TYPE_KNIGHT:
                            numberOfKnights++;
                            break;
                    }
                }

                // also add droppable pieces in crazyhouse / bughouse
                final List<Piece> capturedPieces = new ArrayList<Piece>();
                for (Piece movingPiece : capturedPieces) {
                    switch (movingPiece.getType()) {
                        case Common.PIECE_TYPE_PAWN:
                        case Common.PIECE_TYPE_ROOK:
                        case Common.PIECE_TYPE_QUEEN:
                        case Common.PIECE_TYPE_ARCHBISHOP:
                        case Common.PIECE_TYPE_CHANCELLOR:
                            newMoveInfo.getHasEnoughMaterial()[color] = true;
                            break;
                        case Common.PIECE_TYPE_KING:
                            break;
                        case Common.PIECE_TYPE_BISHOP:
                        case Common.PIECE_TYPE_GRASSHOPER: // this seems ok to count it as bishop
                            numberOfBishops++;
                            break;
                        case Common.PIECE_TYPE_KNIGHT:
                            numberOfKnights++;
                            break;
                    }
                }

                // 2 pieces of any kind are enough
                if ((numberOfBishops + numberOfKnights) > 1) {
                    newMoveInfo.getHasEnoughMaterial()[color] = true;
                }
            }

            if ((!newMoveInfo.getHasEnoughMaterial()[Common.COLOR_WHITE])
                    && (!newMoveInfo.getHasEnoughMaterial()[Common.COLOR_BLACK])) {
                _endString = PGN.STR_NO_MATERIAL;
                endGame(Common.COLOR_ILLEGAL);
            }
        }

        if ((Boolean) getAttribute(CHECK_50_MOVES_DRAW)) {
            // calculate 50 moves count

            Move lastMove = getLastMove();
            if (lastMove == null) {
                newMoveInfo.setDraw50MovesCount(0);
            } else {
                if ((lastMove.getMovedPiece().isPawn()) || (lastMove.getCapturedPiece() != null)) {
                    // capture or pawn move. zero the count
                    newMoveInfo.setDraw50MovesCount(0);
                } else {
                    MoveInfo lastInfo = getCurrentMoveInfo();
                    if (lastInfo != null) {
                        newMoveInfo.setDraw50MovesCount(lastInfo.getDraw50MovesCount() + 1);
                    }
                }
            }

            if (newMoveInfo.getDraw50MovesCount() >= 100) {
                _endString = PGN.STR_50_MOVES;
                endGame(Common.COLOR_ILLEGAL);
            }
        }

        newMoveInfo.setFenPos(FEN.getFENPosition(this));
        if ((Boolean) getAttribute(CHECK_REPEATITION_DRAW)) {
            // calculate repeatition, by counting how many past position matches
            // this one.
            // if 3 or more, declare draw
            int matches = 1;
            for (int moveIndex = 0; moveIndex < _currentMove; moveIndex++) {
                MoveInfo lastMoveInfo = getMoveInfo(moveIndex);
                if (lastMoveInfo == null) {
                    continue;
                }
                if (newMoveInfo.getFenPosition().equals(lastMoveInfo.getFenPosition())) {
                    matches++;
                }
            }
            if (matches == 3) {
                _endString = PGN.STR_REPETITION;
                endGame(Common.COLOR_ILLEGAL);
            }
        }

        _moveInfos.add(newMoveInfo);

        for (Move move : newMoveInfo.getValidNextMoves()) {
            Notation.getNames(move);
        }

        // System.err.println("analyse took " +
        // String.valueOf(TimeUtils.nowInMs() - current) + " ms.");


        return true;
    }

    /**
     * Take back all moves and clear all pieces from the board.
     */
    public void clearBoard() {
        takebackAllMoves();
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                setPieceAt(x, y, null);
            }
        }
        _moveInfos.clear();
    }

    /**
     * Prints the current game state to the console if onScreen is true, with white/black captured pieces, and the position on the board
     *
     * @param onScreen - if display it in the log or not
     * @return returns the string it prints to the log
     */
    public String printGameState(boolean onScreen) {
        StringBuilder builder = new StringBuilder("\n");

        builder.append("\t\t\tBlack Player:").append(getBlackName()).append("\t\tClock:").append(getTimeLeftMs(Common.COLOR_BLACK)).append("\n");
        builder.append("\t\t\tBlack captured:\n");
        builder.append("\t\t\t").append(getCapturedPiecesBlack()).append("\n\n");


        for (int x = 8; x >= 1; x--) {
            builder.append(x).append("\t|\t");
            for (int y = 1; y <= 8; y++) {
                Piece piece = getPieceAt(x, y);

                builder.append(piece == null ? "" : piece).append("\t");
            }
            builder.append("\n");
        }

        builder.append(" \t \t");
        for (int i = 1; i <= 8; i++) {
            builder.append("-\t");
        }
        builder.append("\n");
        builder.append(" \t \t");
        for (int i = 0; i < Common.BOARD_COLUMN.values().length; i++) {
            builder.append(Common.BOARD_COLUMN.values()[i].getColumnName()).append("\t");
        }
        builder.append("\n\n");
        builder.append("\t\t\tWhite captured:\n");
        builder.append("\t\t\t").append(getCapturedPiecesWhite()).append("\n");

        builder.append("\t\t\tWhite Player:").append(getBlackName()).append("\t\tClock:").append(getTimeLeftMs(Common.COLOR_WHITE)).append("\n");

        builder.append("\n\t\t\tSide to move:").append(Common.getColor(_currentColor));
        if (onScreen) {
            LOGGER.info(builder.toString());
        }
        return builder.toString();
    }


    /**
     * @return the number of pieces currently on the board
     */
    public int getNumberOfPiecesOnTheBoard() {
        int numberOfPieces = 0;
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                if (getPieceAt(x, y) != null) {
                    numberOfPieces++;
                }
            }
        }
        return numberOfPieces;
    }

    /**
     * Clear the given side color request
     *
     * @param color - the given color
     */
    public void clearReqPause(int color) {
        _reqPause[color] = false;
    }

    public void delAttribute(String key) {
        _properties.remove(key);
    }

    /**
     * Ends this game because the given side aborted, and forfeit the game as a
     * win to the other color.
     *
     * @param color - the color that aborted this game.
     */
    public void disconnect(int color) {
        _endString = Common.getColor(color) + " " + "disconnected";
        endGame(Common.OtherColor(color));
    }

    /**
     * Ends the game as a mutual agreed draw.
     */
    public void drawMutual() {
        _endString = "Draw agreed.";
        _winner = Common.COLOR_ILLEGAL;
        endGame(Common.COLOR_ILLEGAL);
    }

    private void endGame(int winner) {
        resetClock(Common.COLOR_WHITE);
        resetClock(Common.COLOR_BLACK);
        _ended = true;
        _winner = winner;
        switch (_winner) {
            case Common.COLOR_WHITE:
                setAttribute(PGN.STR_RESULT, PGN.STR_RESULT_WHITE);
                break;
            case Common.COLOR_BLACK:
                setAttribute(PGN.STR_RESULT, PGN.STR_RESULT_BLACK);
                break;
            case Common.COLOR_ILLEGAL:
                setAttribute(PGN.STR_RESULT, PGN.STR_RESULT_DRAW);
                break;
        }
        LOGGER.fine("Game " + getName() + " : ended. " + _endString);
    }

    Piece findPiece(int type, int color) {
        for (int x = 8; x != 0; x--) {
            for (int y = 8; y != 0; y--) {
                Piece piece = getPieceAt(x, y);
                if (piece != null) {
                    if ((piece.getType() == type) && (piece.getColor() == color)) {
                        return piece;
                    }
                }
            }
        }
        return null;
    }

    Piece findCapturedPiece(int color, int type) {
        List<Piece> pieces = getCapturedPieces(color);
        for (Piece piece : pieces) {
            if (piece.getType() == type) {
                return piece;
            }
        }
        return null;
    }

    public static Piece findPieceToDrop(List<Piece> pieces, int droppedPieceType)
    {
        // first look in regular piece type
        for (Piece piece : pieces )
        {
            if ((piece.getType() == droppedPieceType) && (piece.getTypeWhenDropping() == droppedPieceType))
            {
                return piece;
            }
        }
        // now look in promoted pawns
        for (Piece piece : pieces )
        {
            if (piece.getTypeWhenDropping() == droppedPieceType)
            {
                return piece;
            }
        }
        return null;
    }

    /**
     * find all pieces of the given type and color
     * if color = 0, get pieces from all colors
     * if type = 0, get pieces from all types
     */
    List<Piece> findPieces(int type, int color) {
        List<Piece> pieces = new ArrayList<Piece>();
        for (int x = 8; x != 0; x--) {
            for (int y = 8; y != 0; y--) {
                Piece piece = getPieceAt(x, y);
                if (piece != null) {
                    if (((piece.getType() == type) || (type == 0)) &&
                            ((piece.getColor() == color) || (color == 0))) {
                        pieces.add(piece);
                    }
                }
            }
        }
        return pieces;
    }

    public Object getAttribute(String key) {
        return _properties.get(key);
    }

    public Set<String> getAttributes() {
        return _properties.keySet();
    }

    public String getBlackName() {
        return _blackName;
    }

    public void setBlackName(String blackName) {
        _blackName = blackName;
        setAttribute(PGN.STR_BLACK, _blackName);
    }

    /**
     * Returns true if the given color can castle to the given side.
     *
     * @param color
     * @param castle
     * @return true if the given color can castle to the given side.
     */
    public boolean getCastlingAvailability(int color, int castle) {

        if (_king[color] == null) {
            return false;
        }
        if (_king[color].isMoved()) {
            return false;
        }

        List<Piece> rooks = findPieces(Common.PIECE_TYPE_ROOK, color);
        for (Piece rook : rooks) {
            if ((castle == Common.CASTLE_QUEEN) && (rook.getY() == (Integer) getAttribute(LEFT_ROOK_LOCATION)) && (!rook.isMoved())) {
                return true;
            }
            if ((castle == Common.CASTLE_KING) && (rook.getY() == (Integer) getAttribute(RIGHT_ROOK_LOCATION)) && (!rook.isMoved())) {
                return true;
            }
        }
        return false;
    }

    public int getClockDuration(int color) {
        return _clockDuration[color];
    }

    public ArrayList<String> getComments() {
        ArrayList<String> result = new ArrayList<String>();
        for (Move move : getMoves()) {
            result.add(move.getComment());
        }
        return result;
    }

    /**
     * Returns the color that is moving next.
     *
     * @return The color that is moving next.
     */
    public int getCurrentColor() {
        return _currentColor;
    }

    /**
     * Returns the current half-move number of the game. (0 is the first move of
     * white, 1 is the first move of black, etc).
     *
     * @return The current half-move number of the game.
     */
    public int getCurrentMove() {
        return _currentMove;
    }

    /**
     * Returns the current move info (information about the game after the last
     * move). returns null if no such info is available.
     *
     * @return The current move info (information about the game after the last
     * move).
     */
    public MoveInfo getCurrentMoveInfo() {
        if (_moveInfos.isEmpty()) {
            // maybe game was not started
            analyse();
            if (_moveInfos.isEmpty()) {
                return null;
            }
        }
        return _moveInfos.get(_moveInfos.size() - 1);
    }

    public String getEndString() {
        return _endString;
    }

    public void setEndString(String endString) {
        _endString = endString;
    }

    public Pawn getEpPawn() {
        return _epPawn;
    }

    void setEpPawn(Pawn epPawn) {
        this._epPawn = epPawn;
    }

    /**
     * Returns the time increment per move in seconds, for the given color. 0 means no increment.
     *
     * @param color - the given color
     * @return The time increment per move in seconds.
     */
    public int getTimeIncrementForMove(int color) {
        return _timeIncrementForMove[color];
    }

    /**
     * Returns the last move played, if available.
     *
     * @return The last move played, if available. otherwise null.
     */
    public Move getLastMove() {
        if (_moveInfos.size() < 2) {
            return null;
        }
        return getPreviousMoveInfo().getMove();
    }

    /**
     * Returns the move given its half-move number (0 is the first move of
     * white, 1 is the first move of black, etc).
     *
     * @param moveNumber
     * @return The move given its half-move number
     */
    public Move getMove(int moveNumber) {
        if (moveNumber >= _currentMove) {
            return null;
        }
        return getMoves().get(moveNumber);
    }

    /**
     * Returns the move info given its half-move number (0 is the first move of
     * white, 1 is the first move of black, etc).
     *
     * @param moveNumber
     * @return The move info given its half-move number
     */
    public MoveInfo getMoveInfo(int moveNumber) {
        if (moveNumber < _moveInfos.size()) {
            return _moveInfos.get(moveNumber);
        }
        return null;
    }

    /**
     * Returns the game move list, in algebric notation.
     *
     * @return The game move list, in algebric notation.
     */
    public String getMoveListAlg() {
        return _movelist.getListAlg();
    }

    public String getMoveListAlgFromMove(int fromMove) {
        return _movelist.getListAlg(fromMove);
    }

    /**
     * Returns the game move list, in figure notation.
     *
     * @return The game move list, in figure notation.
     */
    public String getMoveListFig() {
        return _movelist.getListFig();
    }

    public String getMoveListFigFromMove(int fromMove) {
        return _movelist.getListFig(fromMove);
    }

    /**
     * Returns the game move list, in website language figure notation.
     *
     * @return The game move list, in website language figure notation.
     */
    public String getMoveListFigLang() {
        return _movelist.getListFigLang();
    }

    public String getMoveListFigLangFromMove(int fromMove) {
        return _movelist.getListFigLang(fromMove);
    }

    /**
     * Returns the game move list, in numeric notation.
     *
     * @return The game move list, in numeric notation.
     */
    public String getMoveListNum() {
        return _movelist.getListNum();
    }

    /**
     * returns partial move list - from the given fromMove to the end of the movelist
     *
     * @param fromMove
     * @return partial move list - from the given fromMove to the end of the movelist
     */
    public String getMoveListNumFromMove(int fromMove) {
        return _movelist.getListNumFromMove(fromMove);
    }

    /**
     * returns partial move list - from the beginning to the given toMove
     *
     * @param toMove
     * @return partial move list - from the beginning to the given toMove
     */
    public String getMoveListNumToMove(int toMove) {
        return _movelist.getListNumToMove(toMove);
    }

    /**
     * Returns the current PGN style move number. e.g 1 if this game is in the
     * first move of white or black, 2 if this game is in the second move of
     * white or black, etc. This can be override when loading FEN.
     *
     * @return The current PGN style move number.
     */
    public int getMoveNumber() {
        return _moveNumber;
    }

    /**
     * Returns all the moves of this game.
     *
     * @return All the moves of this game.
     */
    public ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<Move>();
        for (MoveInfo info : _moveInfos) {
            if (info.getMove() != null) {
                result.add(info.getMove());
            }
        }
        return result;
    }

    /**
     * Retrus the name of the moving player.
     *
     * @return The name of the moving player.
     */
    public String getMovingPlayerName() {
        return _currentColor == Common.COLOR_WHITE ? _whiteName : _blackName;
    }

    /**
     * Returns the name of the game as [white] - [black]
     *
     * @return The name of the game as [white] - [black]
     */
    public String getName() {
        return _whiteName + " - " + _blackName;
    }

    /**
     * Returns the piece at square (x,y) - x is the row number 1..8, y is the
     * column number 1..8
     *
     * @param x row number 1..8
     * @param y column number 1..8
     * @return The piece at square (x,y)
     */
    public Piece getPieceAt(int x, int y) {
        if (x < 1 || y < 1 || x > 8 || y > 8) {
            LOGGER.fine("BAD INPUT");
            return null;
        }
        return _board[x - 1][y - 1];
    }

    public int getColumn(Common.BOARD_COLUMN col) {
        return col.ordinal() + 1;
    }

    /**
     * Returns the previous move played, if available.
     *
     * @return The previous move played, if available. otherwise null.
     */
    public Move getPreviousMove() {
        if (_moveInfos.size() < 3) {
            return null;
        }
        return _moveInfos.get(_moveInfos.size() - 3).getMove();
    }

    /**
     * Returns the previous move info (information about the game before the
     * last move was player). returns null if no such info is available.
     *
     * @return The previous move info (information about the game before the
     * last move was player)
     */
    public MoveInfo getPreviousMoveInfo() {
        if (_moveInfos.size() < 2) {
            return null;
        }
        return _moveInfos.get(_moveInfos.size() - 2);
    }

    public boolean getReqTakeback(int color) {
        return _reqTakeback[color];
    }

    /**
     * Returns the starting color. this is usually white, but can be black if
     * game was loaded from FEN or PGN.
     *
     * @return The starting color.
     */
    public int getStartingColor() {
        return _startingColor;
    }

    /**
     * Returns the time left, in millisecond, for the given color.
     *
     * @param color
     * @return The time left, in millisecond, for the given color.
     */
    public int getTimeLeftMs(int color) {
        if (color == _currentColor) {
            if (_clockStarted[color] == 0) {
                return (_timeLeftMilliseconds[color] - _clockDuration[color]);
            } else {
                return Math.max(0, _timeLeftMilliseconds[color] - ((int) (TimeUtils.nowInMs() - _clockStarted[color])) - _clockDuration[color]);
            }
        } else {
            return (_timeLeftMilliseconds[color]);
        }
    }

    /**
     * Returns the time limit for the entire game in minutes, for the given color.
     * 0 means there is no time limit.
     *
     * @return The time limit for the entire game in minutes.
     */
    public int getTimeLimitForGame(int color) {
        return _timeLimitForGame[color];
    }

    /**
     * Returns the time limit for each move, for the given color, in seconds.
     * return 0 if there is no limit.
     *
     * @param color - the given color
     * @return The time limit for each move, in seconds
     */
    public int getTimeLimitForMove(int color) {
        return _timeLimitForMove[color];
    }

    public String getUser(int color) {
        if (Common.isWhite(color)) {
            return _whiteName;
        } else {
            return _blackName;
        }
    }

    public Move getValidMove(int fromX, int fromY, int toX, int toY) {
        return getValidMove(fromX, fromY, toX, toY, Common.PIECE_TYPE_ILLEGAL);
    }

    public Move getValidMove(int fromX, int fromY, int toX, int toY, int promotionPiece) {
        MoveInfo moveInfo = getCurrentMoveInfo();
        Move move = null;
        if (moveInfo != null) {
            move = moveInfo.getValidMove(fromX, fromY, toX, toY, promotionPiece);
            if (move != null) {
                move.setAdditionalPieceType(promotionPiece);
            }
        }
        return move;
    }

    /**
     * Returns a list of valid next moves. The moves can be correctly played
     * using playMove.
     *
     * @return A list of valid next moves.
     */
    public List<Move> getValidNextMoves() {
        MoveInfo currentInfo = getCurrentMoveInfo();
        if (currentInfo == null) {
            return new ArrayList<Move>();
        }
        return currentInfo.getValidNextMoves();
    }

    /*
    public void copyPositionFrom(Game other)
    {
        IChessUtils.AssertNotNull(other);
        clearBoard();
        for (int x=8; x!=0 ; x--)
        {
            for (int y=8; y!=0 ; y--)
            {
                Piece otherPiece = other.getPieceAt(x,y);
                if (otherPiece != null)
                {
                    setPieceAt(x, y, Piece.create(otherPiece.getType(), otherPiece.getColor()));
                }
            }
        }
        if (isFischer())
        {
            setAttribute(Game.KING_LOCATION, other.getAttribute(Game.KING_LOCATION));
            setAttribute(Game.LEFT_ROOK_LOCATION, other.getAttribute(Game.LEFT_ROOK_LOCATION));
            setAttribute(Game.RIGHT_ROOK_LOCATION, other.getAttribute(Game.RIGHT_ROOK_LOCATION));
        }
    }
    */

    /**
     * Returns a list of valid next moves that can be played if the game is
     * taken back to the given half move number.
     *
     * @param moveNumber
     * @return A list of valid next moves that can be played if the game is
     * taken back to the given half move number.
     */
    public List<Move> getValidNextMovesFromMove(int moveNumber) {
        MoveInfo moveInfo = getMoveInfo(moveNumber);
        if (moveInfo == null) {
            return null;
        }
        return moveInfo.getValidNextMoves();
    }

    public String getWhiteName() {
        return _whiteName;
    }

    public void setWhiteName(String whiteName) {
        _whiteName = whiteName;
        setAttribute(PGN.STR_WHITE, _whiteName);
    }

    public int getWinner() {
        return _winner;
    }

    /**
     * Take back all moves and set the initial position.
     */
    public void initialPosition() {
        takebackAllMoves();

        String initPosition;
        Game initialPositionGame;
        switch (_gkind) {
            case Common.GAME_KIND_FISCHER:
                initPosition = FEN.create960FEN();
                break;
            case Common.GAME_KIND_MINICAPA:
                initPosition = FEN.FEN_MINICAPA_POS;
                break;
            case Common.GAME_KIND_GRASSHOPER:
                initPosition = FEN.FEN_GRASSHOPER_POS;
                break;
            case Common.GAME_KIND_CRAZY_HOUSE:
            case Common.GAME_KIND_BUG_HOUSE:
                initPosition = FEN.FEN_CRAZYHOUSE_POS;
                break;
            default:
                initPosition = FEN.FEN_INITIAL_POS;
                break;
        }
        FEN.loadPositionToGame(this, initPosition);
        // copyPositionFrom(initialPositionGame);
        setAttribute(Game.INITIAL_POSITION_FEN, initPosition);
        setAttribute(Game.KING_LOCATION, initPosition.indexOf('k') + 1);
        setAttribute(Game.LEFT_ROOK_LOCATION, initPosition.indexOf('r') + 1);
        setAttribute(Game.RIGHT_ROOK_LOCATION, initPosition.lastIndexOf('r') + 1);
        analyse();
    }

    /**
     * Returns true iff the game reached a check position.
     *
     * @return true iff the game reached a check position.
     */
    public boolean isCheck() {
        return getCurrentMoveInfo().isCheck();
    }

    /**
     * Returns true iff the game reached a checkmate position.
     *
     * @return true iff the game reached a checkmate position.
     */
    public boolean isCheckMate() {
        return getCurrentMoveInfo().isCheckMate();
    }

    public boolean isEnded() {
        return _ended;
    }

    /**
     * Checks if the given player is out of time If it's not the current color
     * turn, just check if it ran out of time. If it's the current color turn,
     * the game also checks if the player already exceeded its time left
     * (relative to now) minus 5 seconds bonus time to the player, to allow for
     * a LAG in networks.
     *
     * @param color Color to check
     * @return true if the player is out of time. otherwise false.
     */
    public boolean isOutOfTime(int color) {
        if (!isTimed()) {
            return false;
        }
        if (isPaused()) {
            return false;
        }
        if (color == getCurrentColor()) {
            if (_clockStarted[color] == 0) {
                // clock not started
                return false;
            }

            // check the current player - if the remaining time -
            // check the time left, and also check the time left with respect to
            // thinking
            // in the current move
            if (_timeLeftMilliseconds[color] < 0) {
                LOGGER.info("Game : " + getName() + " : " + Common.getColor(color) + " is out of time.");
                return true;
            }
            if (((TimeUtils.nowInMs() - _clockStarted[color]) + _clockDuration[color]) >= _timeLeftMilliseconds[color]) {
                LOGGER.info("Game : " + getName() + " : " + Common.getColor(color) + " is out of time.");
                return true;
            }
            return false;
        } else {
            // check the non moving color
            return (_timeLeftMilliseconds[Common.OtherColor(color)] <= 0);
        }
    }

    public boolean isPaused() {
        return _isPaused;
    }

    public boolean isRated() {
        return _rated;
    }

    public void setRated(boolean rated) {
        _rated = rated;
    }

    /**
     * Returns true iff the game reached a stalemate position.
     *
     * @return true iff the game reached a stalemate position.
     */
    public boolean isStaleMate() {
        return getCurrentMoveInfo().isStaleMate();
    }

    /**
     * Returns true if the game started (some moves played)
     *
     * @return True if the game started (some moves played)
     */
    public boolean isStarted() {
        return _currentMove > 0;
    }

    /**
     * Returns true if the game has any time limit. this means return true if there
     * is a time limit for game or increment per move (or both), for any color
     *
     * @return true if the game has time limit. otherwise false.
     */
    public boolean isTimed() {
        return (
                (_timeLimitForGame[Common.COLOR_WHITE] > 0) ||
                        (_timeIncrementForMove[Common.COLOR_WHITE] > 0) ||
                        (_timeLimitForMove[Common.COLOR_WHITE] > 0) ||
                        (_timeLimitForGame[Common.COLOR_BLACK] > 0) ||
                        (_timeIncrementForMove[Common.COLOR_BLACK] > 0) ||
                        (_timeLimitForMove[Common.COLOR_BLACK] > 0)
        );
    }

    public boolean isWhiteOrBlack(String name) {
        Utils.AssertNotNull(name);
        return (name.equals(_whiteName)) || (name.equals(_blackName));
    }

    private void movePiece(int fromX, int fromY, int toX, int toY) {
        Piece fromPiece = _board[fromX - 1][fromY - 1];
        Utils.AssertNotNull(fromPiece);
        setPieceAt(fromX, fromY, null);
        setPieceAt(toX, toY, fromPiece);
    }

    /**
     * Return true if the given color offered draw
     *
     * @param color the given color
     * @return true if the given color offered draw
     */
    public boolean drawOfferedBy(int color) {
        return _reqDraw[color];
    }

    public boolean offerPause(int color) {
        return _reqPause[color];
    }

    public void pauseGame() {
        pauseClock();
        _isPaused = true;
    }

    private boolean playMove(Move move) {

        Utils.AssertNotNull(move);

        if (!analyse()) {
            LOGGER.warning("analyse failed.");

            return false;
        }

        if (_ended) {
            LOGGER.info("can't play move. game ended.");

            return false;
        }

        MoveInfo currentInfo = getCurrentMoveInfo();

        int fromX = move.getFromX();
        int fromY = move.getFromY();
        int toX = move.getToX();
        int toY = move.getToY();

        Move testMove = currentInfo.getValidMove(fromX, fromY, toX, toY, move.getAdditionalPieceTypeInfo());
        if ((testMove == null) || (testMove != move)) {
            LOGGER.warning("move not in valid moves list");

            return false;
        }

        if (isPaused()) {
            LOGGER.warning("can't play move now. game is paused");

            return false;
        }

        move.setMoveNumber(_currentMove);

        if (move.isDropMove()) {
            // drop move
            List<Piece> droppable = getDroppablePieces(_currentColor);
            Piece droppedPiece = findPieceToDrop(droppable, move.getAdditionalPieceTypeInfo());
            if (droppedPiece == null) {
                LOGGER.warning("can't find dropped piece " + move.getAdditionalPieceTypeInfo());
                return false;
            }
            LOGGER.fine("move " + _currentMove + " color " + Common.getColor(_currentColor) + " drop move " +
                    droppedPiece.getType() + " at " + toX + "," + toY);
            if (! droppable.remove(droppedPiece))
            {
                LOGGER.warning("could not remove droppable piece");
            }
            int droppedPieceType = droppedPiece.getType();
            int droppedPieceColor = droppedPiece.getColor();
            int promotedTo = Common.PIECE_TYPE_ILLEGAL;
            if (isCrazyHouse())
            {
                droppedPieceColor = Common.OtherColor(droppedPieceColor);
            }
            if (droppedPiece.isPromoted())
            {
                promotedTo = droppedPieceType;
                droppedPieceType = Common.PIECE_TYPE_PAWN;
            }
            Piece actuallyDropped = Piece.create(droppedPieceType, droppedPieceColor);
            if (promotedTo != Common.PIECE_TYPE_ILLEGAL)
            {
                ((Pawn)actuallyDropped).setWasPromotedTo(promotedTo);
            }
            setPieceAt(toX, toY, actuallyDropped);
            move.setMovedPiece(actuallyDropped);
        } else {
            // normal move

            Utils.Assert((fromX >= 1) && (fromX <= 8), "bad x coordinates");
            Utils.Assert((fromY >= 1) && (fromY <= 8), "bad y coordinates");
            Utils.Assert((toX >= 1) && (toX <= 8), "bad x coordinates");
            Utils.Assert((toY >= 1) && (toY <= 8), "bad y coordinates");

            // make the move on the next position
            Piece movingPiece = getPieceAt(fromX, fromY);
            if (movingPiece == null) {
                LOGGER.warning("moving piece is null");

                return false;
            }

            if (movingPiece.getColor() != _currentColor) {
                LOGGER.warning("moving piece have incorrect color");
                return false;
            }

            move.setMovedPiece(movingPiece);

            Piece capturedPiece = getPieceAt(toX, toY);
            boolean FischerCastle = false;
            if (capturedPiece != null) {
                // don't allow to capture a king
                if (capturedPiece.isKing() && !isSuicideOrFreePlay()) {
                    // can't capture a king
                    LOGGER.warning("can't capture a king");

                    return false;
                }
                // don't allow to capture your own pieces
                if (capturedPiece.getColor() == _currentColor) {
                    if (isFischer() && capturedPiece.isRook() && movingPiece.isKing()) {
                        FischerCastle = true;
                        capturedPiece = null;
                    } else {
                        LOGGER.warning("can't capture same color");

                        return false;
                    }
                }
            } else if (isFischer() && movingPiece.isKing() && (Math.abs(fromY - toY) >= 2)) {
                FischerCastle = true;
            }

            int rookY, rookToY;
            int dest = 7;
            if (FischerCastle) { // eating our own rook
                setPieceAt(fromX, fromY, null); // remove the king
            } else {
                movePiece(fromX, fromY, toX, toY); // normal castle - move the king
            }

            // special case : castle
            Piece rook = null;
            if ((movingPiece.isKing()) && ((Math.abs(fromY - toY) >= 2) || FischerCastle)) {
                // castling
                if (fromY > toY) {
                    rookY = (Integer) getAttribute(LEFT_ROOK_LOCATION);
                    rookToY = 4;
                    dest = 3;
                } else {
                    rookY = (Integer) getAttribute(RIGHT_ROOK_LOCATION);
                    rookToY = 6;
                }
                rook = getPieceAt(fromX, rookY);
                Utils.AssertNotNull(rook);
                Utils.Assert(rook.isRook());
                Utils.Assert(rook.isColor(_currentColor));
                movePiece(rook.getX(), rookY, rook.getX(), rookToY); // move the rook
                if (FischerCastle) {
                    setPieceAt(toX, dest, movingPiece); // put the king in his place
                }
            }
            if (rook != null) {
                currentInfo.getMovedPiece()[1] = rook;
                rook.setMoved(true);
            }

            // special case : promotion
            int promotionSquare[] = new int[Common.COLOR_NUM];
            promotionSquare[Common.COLOR_WHITE] = 8;
            promotionSquare[Common.COLOR_BLACK] = 1;
            if ((movingPiece.isPawn()) && (toX == promotionSquare[_currentColor])) {
                LOGGER.fine("promotion move. promotion piece is " + move.getAdditionalPieceTypeInfo());
                Piece promotedPiece = Piece.create(move.getAdditionalPieceTypeInfo(), _currentColor);
                promotedPiece.setPromoted();
                setPieceAt(fromX, fromY, null);
                setPieceAt(toX, toY, promotedPiece);
            }

            // special move : ep capture by pawn
            if (movingPiece.isPawn()) {
                if ((toY != fromY) && (capturedPiece == null)) {
                    if (_epPawn != null) {
                        capturedPiece = _epPawn;
                        setPieceAt(_epPawn.getX(), _epPawn.getY(), null);
                        move.setEpCapture(true);
                    }
                }
            }

            // special move : mark ep pawn for next move
            if ((movingPiece.isPawn() && (Math.abs(fromX - toX) == 2))) {
                _epPawn = (Pawn) movingPiece;
            } else {
                _epPawn = null;
            }
            if (!movingPiece.isMoved()) {
                currentInfo.getMovedPiece()[0] = movingPiece;
                movingPiece.setMoved(true);
            }

            move.setCapturedPiece(capturedPiece);
            // add captured piece to list
            if (capturedPiece != null)
            {
                getCapturedPieces(Common.OtherColor(_currentColor)).add(capturedPiece);
                capturedPiece.setX(0);
                capturedPiece.setY(0);
            }
        }

        currentInfo.setMove(move);
        move.setMoveInfo(currentInfo);

        _reqTakeback[Common.COLOR_WHITE] = false;
        _reqTakeback[Common.COLOR_BLACK] = false;
        _reqDraw[Common.COLOR_WHITE] = false;
        _reqDraw[Common.COLOR_BLACK] = false;
        _reqPause[Common.COLOR_WHITE] = false;
        _reqPause[Common.COLOR_BLACK] = false;
        move.setTimePlayed(TimeUtils.nowInMs());

        // update time (unless its first or second move)
        if (isTimed()) {
            pauseClock();
            int clockDuration = getClockDuration(_currentColor);
            move.setMoveTime(clockDuration);
            _timeLeftMilliseconds[_currentColor] -= clockDuration;
            resetClock(_currentColor);
            _timeLeftMilliseconds[_currentColor] += (_timeIncrementForMove[_currentColor] * TimeUtils.MS_IN_SECOND);

            LOGGER.fine(" Game " + getName() + " : Time Left W " + (_timeLeftMilliseconds[Common.COLOR_WHITE] / 1000) + " B "
                    + (_timeLeftMilliseconds[Common.COLOR_BLACK] / 1000));
        }

        _currentMove++;
        _currentColor = Common.OtherColor(_currentColor);

        if (isTimed()) {
            resumeClock();
        }
        if (_currentColor == Common.COLOR_WHITE) {
            _moveNumber++;
        }
        analyse();

        Notation.getNames(move);

        _movelist.addMove(move);

        LOGGER.fine(" Game " + getName() + " : " + ((_currentMove + 1) / 2) + "." + (((_currentMove % 2) == 0) ? ".. " : " ")
                + move.getNameAlg() + " . Time left : [ " + _timeLeftMilliseconds[Common.COLOR_WHITE] + " ] , [ "
                + _timeLeftMilliseconds[Common.COLOR_BLACK] + " ]");


        return true;

    }

    /**
     * Play the given move on this game. The move string can have either
     * algebric or numeric english notation.
     *
     * @param moveStr move string in either algebric or numeric english notation.
     * @return true if the move was successfully played
     */
    public boolean playMove(String moveStr) {

        Utils.AssertNotNull(moveStr);

        analyse();

        Move move = Notation.getMove(this, moveStr);
        if (move == null) {
            LOGGER.warning("game " + getName() + " could not parse move '" + moveStr + "' from player " + getMovingPlayerName());
            return false;
        }
        return playMove(move);
    }

    public boolean playMoveList(String movelist) {
        return Notation.playMoveList(this, movelist);
    }

    public boolean playMoveList(String movelist, int toMove) {
        return Notation.playMoveList(this, movelist, toMove);
    }

    /**
     * Stop and reset the count down clock of the given color.
     *
     * @param color the given color
     */
    public void resetClock(int color) {
        _clockStarted[color] = 0;
        _clockDuration[color] = 0;
    }

    /**
     * Enable/disable auto draw ending after 50 moves where no piece was captured and a pawn was not moved
     *
     * @param enabled auto draw ending after 50 moves where no piece was captured and a pawn was not moved enabled
     */
    public void setAutoDrawOn50MovesRule(boolean enabled) {
        setAttribute(CHECK_50_MOVES_DRAW, enabled);
    }

    /**
     * Enable/disable auto draw ending after repeating the same position 3 times
     *
     * @param enabled auto draw ending after repeating the same position 3 times enabled
     */
    public void setAutoDrawOn3rdRepetition(boolean enabled) {
        setAttribute(CHECK_REPEATITION_DRAW, enabled);
    }

    /**
     * Enable/disable auto draw ending after each side has at most a king and a minor piece
     *
     * @param enabled auto draw ending after each side has at most a king and a minor piece enabled
     */
    public void setAutoDrawOnNoMaterial(boolean enabled) {
        setAttribute(CHECK_NO_MATERIAL_DRAW, enabled);
    }

    /**
     * Return true if auto draw ending after 50 moves where no piece was captured and a pawn was not moved is enabled
     *
     * @return true if auto draw ending after 50 moves where no piece was captured and a pawn was not moved is enabled
     */
    public boolean isAutoDrawOn50MovesRule() {
        return Boolean.valueOf((String) getAttribute(CHECK_50_MOVES_DRAW));
    }

    /**
     * Return true if  auto draw ending after repeating the same position 3 times is enabled
     *
     * @return true if  auto draw ending after repeating the same position 3 times is enabled
     */
    public boolean isAutoDrawOn3rdRepetition() {
        return Boolean.valueOf((String) getAttribute(CHECK_REPEATITION_DRAW));
    }

    /**
     * Return true if auto draw ending after each side has at most a king and a minor piece is enabled
     *
     * @return true if auto draw ending after each side has at most a king and a minor piece is enabled
     */
    public boolean isAutoDrawOnNoMaterial() {
        return Boolean.valueOf((String) getAttribute(CHECK_NO_MATERIAL_DRAW));
    }

    /**
     * Ends this game because the given side resigned, and forfeit the game as a
     * win to the other color.
     *
     * @param color the color that resigned this game.
     */
    public void resign(int color) {
        _endString = Common.getColor(color) + " " + "resigned";
        endGame(Common.OtherColor(color));
    }

    /**
     * Signal the game that the other game ended
     */
    public void otherGameEnded() {
        switch (_otherGame.getWinner())
        {
            case Common.COLOR_WHITE: endGame(Common.COLOR_BLACK); break;
            case Common.COLOR_BLACK: endGame(Common.COLOR_WHITE); break;
            case Common.COLOR_ILLEGAL: endGame(Common.COLOR_ILLEGAL); break;
            default:
                LOGGER.warning("bughouse other game did not end");
                return;
        }
        _endString = "BugHouse : Other game ended";
    }
    
    public void resumeGame() {
        resumeClock();
        _isPaused = false;
    }

    public void setAttribute(String key, Object value) {
        _properties.put(key, value);
    }

    /**
     * Sets the castling availability when reading FEN
     *
     * @param color
     * @param castle
     * @param available
     * @return true if operation succeeded
     */
    boolean setCastlingAvailability(int color, int castle, boolean available) {
        if (isStarted()) {
            LOGGER.warning("can't set castling. game allready started");
            return false;
        }

        List<Piece> rooks = findPieces(Common.PIECE_TYPE_ROOK, color);
        for (Piece rook : rooks) {
            if ((castle == Common.CASTLE_QUEEN) && (rook.getY() == (Integer) getAttribute(LEFT_ROOK_LOCATION))) {
                rook.setMoved(!available);
            }
            if ((castle == Common.CASTLE_KING) && (rook.getY() == (Integer) getAttribute(RIGHT_ROOK_LOCATION))) {
                rook.setMoved(!available);
            }
        }

        return true;
    }

    /**
     * Sets time increment, per move, for the given color, in seconds.
     * This should be set together with setTimeLimitForGame.
     *
     * @param color              a given color
     * @param incrementInSeconds increment for each move, in seconds.
     * @return true on success, otherwise false.
     */
    public boolean setTimeIncrementPerMove(int color, int incrementInSeconds) {
        if (incrementInSeconds < 0) {
            LOGGER.warning("negative incrementInSeconds " + incrementInSeconds);
            return false;
        }

        _timeIncrementForMove[color] = incrementInSeconds;
        _timeLimitForMove[color] = 0;

        updateTimeControlPGNTag();

        if (isStarted()) {
            LOGGER.warning("Can't update clock - game started");
            return false;
        }

        _timeLeftMilliseconds[color] = _timeLimitForGame[color] * (int) TimeUtils.MS_IN_MINUTE;

        return true;
    }

    /**
     * Sets time increment, per move, in seconds.
     * This should be set together with setTimeLimitForGame.
     *
     * @param incrementInSeconds increment for each move, in seconds.
     * @return true on success, otherwise false.
     */
    public boolean setTimeIncrementPerMove(int incrementInSeconds) {
        return
                setTimeIncrementPerMove(Common.COLOR_WHITE, incrementInSeconds) &&
                        setTimeIncrementPerMove(Common.COLOR_BLACK, incrementInSeconds);

    }

    boolean setMoveNumber(int move) {
        if (isStarted()) {
            LOGGER.warning("can't set move number. game allready started");
            return false;
        }

        _moveNumber = move;
        return true;
    }

    /*
     * Returns a list of all pieces on the board that matches the given type and
     * color.
     */
    boolean setPieceAt(int x, int y, Piece piece) {
        _board[x - 1][y - 1] = piece;
        if (piece != null) {
            piece.setX(x);
            piece.setY(y);
            if (piece.isRook()) {
            }
            if (piece.isKing()) {
                _king[piece.getColor()] = (King) piece;
            }
        }

        return true;
    }

    /**
     * Signals that the given color offers draw. The draw request will be cleared if a move
     * is played (or taken back).
     * If both sides offered draw, the game will be ended (same as calling {@link #drawMutual()})
     *
     * @param color the given color
     */
    public void offerDraw(int color) {
        _reqDraw[color] = true;
        if ((_reqDraw[Common.COLOR_WHITE]) && (_reqDraw[Common.COLOR_BLACK])) {
            drawMutual();
        }
    }

    public boolean getReqPause(int color) {
        return _reqPause[color];
    }

    public void setReqPause(int color) {
        _reqPause[color] = true;
    }

    public void setReqTakeback(int color) {
        _reqTakeback[color] = true;
    }

    boolean setStartingColor(int color) {
        if (isStarted()) {
            LOGGER.warning("can't set color. game allready started");
            return false;
        }
        Utils.Assert(Common.isBlackOrWhite(color), "bad color " + color);
        _startingColor = color;
        _currentColor = color;
        return true;
    }

    /**
     * Override the time left, in millisecond, for the given color.
     * This does not affect/stop/resume the clock in any way
     *
     * @param color    - the given color
     * @param timeleft - time left on clock, in milliseconds. must be > 0
     * @return true on success, otherwise false.
     */
    public boolean setTimeLeftMilliseconds(int color, long timeleft) {
        if (timeleft <= 0) {
            LOGGER.warning("Can't set non positive time left " + timeleft);
            return false;
        }
        _timeLeftMilliseconds[color] = (int) timeleft;
        // restart the clock if its started
        if (_clockStarted[color] != 0) {
            _clockStarted[color] = TimeUtils.nowInMs();
        }
        return true;
    }

    /**
     * Sets time limit for the entire game in minutes, for the given color.
     * The game must not be started. Set to 0 to disable time limit for game.
     *
     * @param color         a given color
     * @param timeInMinutes time limit for the entire game, in minutes
     * @return true on success, otherwise false.
     */
    public boolean setTimeLimitForGame(int color, int timeInMinutes) {

        if (timeInMinutes < 0) {
            LOGGER.warning("negative timeInMinutes");
            return false;
        }

        _timeLimitForGame[color] = timeInMinutes;
        _timeLimitForMove[color] = 0;
        updateTimeControlPGNTag();
        if (isStarted()) {
            // can't update actual clock
            return true;
        }

        _timeLeftMilliseconds[color] = _timeLimitForGame[color] * (int) TimeUtils.MS_IN_MINUTE;

        return true;
    }

    /**
     * Sets time limit for the entire game in minutes.
     * The game must not be started. Set to 0 to disable time limit for game.
     *
     * @param timeInMinutes time limit for the entire game, in minutes
     * @return true on success, otherwise false.
     */
    public boolean setTimeLimitForGame(int timeInMinutes) {
        return
                setTimeLimitForGame(Common.COLOR_WHITE, timeInMinutes) &&
                        setTimeLimitForGame(Common.COLOR_BLACK, timeInMinutes);
    }

    /**
     * Sets a time limit for each individual move, for a given color, in seconds
     *
     * @param color         a given color
     * @param timeInSeconds time limit for each move, in seconds
     * @return true on success, otherwise false.
     */
    public boolean setTimeLimitForMove(int color, int timeInSeconds) {

        if (timeInSeconds < 0) {
            LOGGER.warning("negative timeInSeconds");
            return false;
        }

        _timeLimitForGame[color] = 0;
        _timeIncrementForMove[color] = 0;
        _timeLimitForMove[color] = timeInSeconds;

        updateTimeControlPGNTag();
        return true;
    }

    /**
     * Sets a time limit for each individual move, in seconds
     *
     * @param timeInSeconds time limit for each move, in seconds
     * @return true on success, otherwise false.
     */
    public boolean setTimeLimitForMove(int timeInSeconds) {
        return
                setTimeLimitForMove(Common.COLOR_WHITE, timeInSeconds) &&
                        setTimeLimitForMove(Common.COLOR_BLACK, timeInSeconds);
    }

    /**
     * Start or resumes the count down clock of the player that is about to move now
     *
     * @return true on success
     */
    public boolean resumeClock() {
        if (_clockStarted[_currentColor] != 0) {
            LOGGER.warning("Clock already started");
            return false;
        }
        _clockStarted[_currentColor] = TimeUtils.nowInMs();
        return true;
    }

    public long getClockStartedTime(int color) {
        return _clockStarted[color];
    }

    /**
     * pauses the count down clock of the player that is about to move now
     *
     * @return true upon success
     */
    public boolean pauseClock() {
        if (_clockStarted[_currentColor] == 0) {
            LOGGER.fine("clock of " + Common.getColor(_currentColor) + " already stopped");
            return true;
        }
        _clockDuration[_currentColor] += (TimeUtils.nowInMs() - _clockStarted[_currentColor]);
        LOGGER.fine("think started " + _clockStarted[_currentColor] + " now " + TimeUtils.nowInMs() +
                " duration " + _clockDuration[_currentColor]);
        _clockStarted[_currentColor] = 0;
        return true;
    }

    /**
     * Add mili seconds to the given color clock
     *
     * @param color      - the given color
     * @param additionMS - how much mili seconds to add to the clock
     */
    public void addClockTime(int color, int additionMS) {
        _clockStarted[color] += additionMS;
    }

    public boolean canTakeback()
    {
        if (! isBugHouse())
        {
            return true;
        }
        if (_otherGame == null)
        {
            return true;
        }
        Move lastMove = getLastMove();
        if (lastMove == null)
        {
            return false;
        }

        if (! lastMove.isCapture())
        {
            return true;
        }
        Piece capturedPiece = lastMove.getCapturedPiece();
        return _otherGame.getDroppablePieces(Common.OtherColor(lastMove.getColor())).contains(capturedPiece);
    }

    /**
     * Take back the last move played, if present.
     */
    public void takeback() {


        if (_currentMove == 0) {
            LOGGER.info("no takeback - game at first move");
            return;
        }

        _ended = false;
        _winner = 0;
        MoveInfo lastMoveInfo = getPreviousMoveInfo();

        Move lastMove = lastMoveInfo.getMove();
        lastMove.setMoveInfo(null);

        Piece movedPiece = lastMove.getMovedPiece();
        Piece capturedPiece = lastMove.getCapturedPiece();

        int fromX = lastMove.getFromX();
        int fromY = lastMove.getFromY();
        int toX = lastMove.getToX();
        int toY = lastMove.getToY();

        if (lastMove.isDropMove()) {
            // takeback drop move. just remove the piece and return to the droppable list
            setPieceAt(toX, toY, null);

            List<Piece> droppable = getDroppablePieces(Common.OtherColor(_currentColor));
            Piece droppedPiece = lastMove.getMovedPiece();
            Piece capturedBeforeDropped = droppedPiece;
            // in crazy house piece reversed color when it was dropped. so reverse back
            if (isCrazyHouse())
            {
                capturedBeforeDropped = Piece.create(droppedPiece.getType(), Common.OtherColor(droppedPiece.getColor()));
            }
            if (droppedPiece.isPawn() && (((Pawn)droppedPiece).getWasPromotedTo() != Common.PIECE_TYPE_ILLEGAL))
            {
                capturedBeforeDropped = Piece.create(((Pawn) droppedPiece).getWasPromotedTo(), capturedBeforeDropped.getColor());
                capturedBeforeDropped.setPromoted();
            }
            droppable.add(capturedBeforeDropped);
            lastMove.setMovedPiece(capturedBeforeDropped);
        } else {
            boolean fischerCastle = isFischer() && capturedPiece != null && movedPiece.isKing() &&
                    capturedPiece.isColor(movedPiece.getColor()) && capturedPiece.isRook();

            if (!fischerCastle) { // fischer KxR - the rook might go elsewhere
                setPieceAt(toX, toY, capturedPiece);
                setPieceAt(fromX, fromY, movedPiece);
            }

            // special case - takeback ep capture
            if (lastMove.isEpCapture()) {
                setPieceAt(toX, toY, null);
                if (_currentColor == Common.COLOR_WHITE) {
                    setPieceAt(toX + 1, toY, capturedPiece);
                } else if (_currentColor == Common.COLOR_BLACK) {
                    setPieceAt(toX - 1, toY, capturedPiece);
                }
            }

            // special case - castling
            if (movedPiece.isKing() && ((Math.abs(fromY - toY) >= 2) || fischerCastle)) {
                // castling
                Piece rook;
                int rookY, rookToY, kingTo = 3;
                if (fromY > toY) { // long
                    rookY = 4;
                    rookToY = (Integer) getAttribute(LEFT_ROOK_LOCATION);
                } else {
                    rookY = 6;
                    kingTo = 7;
                    rookToY = (Integer) getAttribute(RIGHT_ROOK_LOCATION);
                }
                rook = getPieceAt(fromX, rookY);
                if (fischerCastle) {
                    setPieceAt(toX, kingTo, null); // clean kings square
                    setPieceAt(toX, rookY, null); // clean rooks square
                    setPieceAt(fromX, fromY, movedPiece); // put king in place
                    setPieceAt(fromX, rookToY, rook); // put king in place
                }

                if (!fischerCastle) {
                    movePiece(fromX, rookY, fromX, rookToY); // place the rook
                }
            }

            if (lastMoveInfo.getMovedPiece()[0] != null) {
                lastMoveInfo.getMovedPiece()[0].setMoved(false);
            }
            if (lastMoveInfo.getMovedPiece()[1] != null) {
                lastMoveInfo.getMovedPiece()[1].setMoved(false);
            }

            if (capturedPiece != null)
            {
                if (! getCapturedPieces(_currentColor).remove( capturedPiece )) {
                    LOGGER.warning("could not find " + Common.getColor(_currentColor) + " captured piece " + capturedPiece);
                }
            }
        }

        _reqTakeback[Common.COLOR_WHITE] = false;
        _reqTakeback[Common.COLOR_BLACK] = false;
        _reqDraw[Common.COLOR_WHITE] = false;
        _reqDraw[Common.COLOR_BLACK] = false;
        _reqPause[Common.COLOR_WHITE] = false;
        _reqPause[Common.COLOR_BLACK] = false;

        _moveInfos.remove(_moveInfos.size() - 1);
        _currentColor = Common.OtherColor(_currentColor);
        _currentMove--;

        // re-set game EP pawn
        Move move = getLastMove();
        setEpPawn(null);
        if (move != null) {
            movedPiece = move.getMovedPiece();
            if (movedPiece.isPawn()) {
                if (Math.abs(move.getToX() - move.getFromX()) == 2) {
                    setEpPawn((Pawn) movedPiece);
                }
            }
        }

        LOGGER.fine(" Game " + getName() + " : taking back move " + lastMove.getNameAlg());

        LOGGER.fine("\n" + toString());

        // must be called last
        _movelist.takeback();

        if (_currentColor == Common.COLOR_BLACK) {
            _moveNumber--;
        }

        if (isTimed()) {
            // reset clocks and start again with the current color
            resetClock(_currentColor);
            resumeClock();
        }
    }

    /**
     * Take back all moves.
     */
    public void takebackAllMoves() {
        while (_currentMove > 0) {
            takeback();
        }
    }

    public boolean takebackToMove(int moveNumber) {
        while (_currentMove > moveNumber) {
            takeback();
        }
        return true;
    }

    /**
     * Ends this game, as the current color ran out of time.
     */
    public void timeOut() {
        timeOut(_currentColor);
    }

    /**
     * Ends this game, as the given color ran out of time.
     *
     * @param color the color that ran out of time.
     */
    public void timeOut(int color) {
        int otherColor = Common.OtherColor(color);
        int winner;
        if (getCurrentMoveInfo().getHasEnoughMaterial()[otherColor]) {
            winner = otherColor;
            _endString = Common.getColor(color) + " " + "is out of time";
            LOGGER.info("Game " + getName() + " ended : " + Common.getColor(color) + " out of time.");
        } else {
            winner = Common.COLOR_ILLEGAL;
            _endString = Common.getColor(color) + " " + "is out of time, but " +
                    Common.getColor(otherColor) + " " + "has no material";
            LOGGER.info("Game " + getName() + " ended : " + Common.getColor(color) + " out of time. but no material for "
                    + Common.getColor(otherColor));
        }
        endGame(winner);
    }

    @Override
    public String toString() {
        String result = "  a b c d e f g h\n";
        int sx, ex, xd;
        if (_currentColor == Common.COLOR_WHITE) {
            sx = 1;
            ex = 9;
            xd = 1;
        } else {
            sx = 8;
            ex = 0;
            xd = -1;
        }
        for (int x = sx; x != ex; x += xd) {
            result += x + " ";
            for (int y = 1; y <= 8; y++) {
                Piece piece = getPieceAt(x, y);
                if (piece == null) {
                    result += ". ";
                } else {
                    result += piece + " ";
                }
            }
            result += "\n";
        }
        return result;
    }

    private void updateTimeControlPGNTag() {
        if ((_timeLimitForGame[Common.COLOR_WHITE] > 0) || (_timeIncrementForMove[Common.COLOR_WHITE] > 0)) {
            if (_timeIncrementForMove[Common.COLOR_WHITE] == 0) {
                setAttribute(PGN.STR_TIME_CONTROL, String.valueOf(_timeLimitForGame[Common.COLOR_WHITE] * TimeUtils.SECONDS_IN_MINUTE));
            } else {
                setAttribute(PGN.STR_TIME_CONTROL, String.valueOf(_timeLimitForGame[Common.COLOR_WHITE] * TimeUtils.SECONDS_IN_MINUTE)
                        + "+" + String.valueOf(_timeIncrementForMove[Common.COLOR_WHITE]));
            }
        } else if (_timeLimitForMove[Common.COLOR_WHITE] > 0) {
            setAttribute(PGN.STR_TIME_CONTROL, "*" + String.valueOf(_timeLimitForMove[Common.COLOR_WHITE]));
        }
    }
}
