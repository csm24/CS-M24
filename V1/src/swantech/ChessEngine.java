package swantech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.*;

import ictk.boardgame.*;
import ictk.boardgame.chess.*;
import ictk.boardgame.chess.io.FEN;


// TODO put enums in this class?


import static ictk.boardgame.chess.io.FEN.*;

/**
 * Created by simon on 31/03/15.
 * <p/>
 * \brief ChessEngine provides an interface to a chess engine such as StockFish.
 * <p/>
 * <p/>
 * The ChessEngine is run as a detached process via a thread, i.e. it runs effectively as a daemon,
 * an executable program running in the background.
 * Commands are sent and received as if through standard input/output, and using the UCI chess interface
 * (Universal Chess Interface, see http://en.wikipedia.org/wiki/Universal_Chess_Interface)
 * <p/>
 * Other chess engines could be plugged in.
 * <p/>
 * Makes extensive use of ICTK library, released under the GNU GPL license for public use,
 * Copyright (c) 1997-2014 J. Varsoke <ictk.jvarsoke [at] neverbox.com>
 * Thanks and acknowledgements to Author: J. Varsoke. I can be reached at jvarsoke at users sourceforge net.
 * see at http://ictk.sourceforge.net/#downloads, http://ictk.sourceforge.net/docs/current/ ,
 * and samples at Github: https://github.com/jvarsoke/ictk/tree/master/src/samples#simplepgndemo
 */


public class ChessEngine {

    ChessGame game = null;
    ChessBoard board = null;
    History history = null;
    Move move = null,
            e4 = null;
    //    MoveNotation  san     = new SAN();
//    PGNWriter     writer  = null;
    ChessGameInfo gi = null;
    ChessPlayer player = null;
    PlayColour engineColour;
    Stockfish stockfish = null;  // The StockFish Java interface

    private final static Logger LG = Logger.getLogger(ChessEngine.class.getName());


    /**
     * Default constructor. Sets up a new board with standard starting position
     * Starts the Stockfish engine.
     *
     * @param newEngineColour from playColour enum, WHITE or BLACK, the colour to be played by the chess engine
     */
    public ChessEngine(PlayColour newEngineColour)  throws Exception
    {
        try {
            engineColour = newEngineColour;
            game = new ChessGame();

            // Set default players. Can be over-ridden once instance created
            gi = new ChessGameInfo();
            setChessplayer(PlayColour.BLACK, "Boris", "Spasky");
            setChessplayer(PlayColour.WHITE, "Bobby", "Fischer");
            game.setGameInfo(gi);

            //setup ready for the moves
            history = game.getHistory();
            board = (ChessBoard) game.getBoard();

            board.setBlackMove(engineColour == PlayColour.WHITE); // start as White or Black

            // keep for debugging - System.out.println("Working Directory = " + System.getProperty("user.dir"));
            stockfish = new Stockfish("./engine/stockfish");  // create new stockfish interface
            if (!stockfish.startEngine()) {
                LG.log(Level.SEVERE, "Cannot start StockFish chess engine");
                throw new Exception("Cannot start StockFish chess engine");
            }

            // We are not doing anything with this yet, just make sure we get a response
            List<String> responses = askStockfish("uci", 100);
            if (responses.size() < 2) {
                // something wrong, should be 22 lines of text here
                throw new Exception("ChessEngine constructor, askStockfish wrong response (too short)");
            }

        } catch (Exception e) {
            LG.log(Level.SEVERE, "ChessEngine constructor FAILED");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Used to set the White and Black player names
     * @param Colour, PlayColour.White or .Black
     * @param firstName  eg Boris
     * @param lastName eg Spasky
     * @return error code, ChessEngineErrors.OK if all is well
     */
    public ChessEngineErrors setChessplayer (PlayColour c, String firstName, String lastName)
    {
        // gi is global private ChessGFameInfo
        player = new ChessPlayer();
        player.setFirstName(firstName);
        player.setLastName(lastName);
        if (c.isBlack())
            gi.setBlack(player);
        else
            gi.setWhite(player);
        return ChessEngineErrors.OK;
    }

    /**
     * Given a board square that has a piece on it, returns a list of squares that this piece is permitted to move to,
     * based on the current board state.
     * Excludes squares that would be legal but would result in check.
     * Stateless - this method can be called any number of times with same or different squares
     * <p/>
     * Note Square is defined in ICTK jar class Square
     *
     * @param startSquare - rank and file class , must contain a piece
     * @return List of zero or more squares. Returns NULL if startSquare is empty (so don't).
     * List is EMPTY (length == 0) if there are NO legal moves (including only moves that result in check)
     */
    public ArrayList<Square> getLegalMoves(Square startSquare) {

        // Sanity check that a legal square has been specified. Should never fail in production !
        assert ((startSquare.getFile() > 0) && (startSquare.getFile() < 9) &&
                (startSquare.getRank() > 0) && (startSquare.getRank() < 9));

        Square thisSquare = board.getSquare(startSquare.getFile(), startSquare.getRank());
        ChessPiece thisPiece = (ChessPiece) thisSquare.getPiece();
        if (thisPiece == null) return null; // Null piece as the square is empty

        boolean wasBlackMove = board.isBlackMove();  // save current move state
        board.setBlackMove(thisPiece.isBlack()); // set the right player
        ArrayList<Square> legalMoves = (ArrayList<Square>) thisPiece.getLegalDests();
        board.setBlackMove(wasBlackMove);

        return legalMoves;
    }


    /**
     * Returns the ChessPiece at the nominated Square
     * Example:
     * Square s = new Square('a', '1');
     * Piece p = getPieceAt(s);
     * if (p.isQueen().......)
     *
     * @param sq, Square object
     * @return Piece, or null if its an empty square
     */
    public ChessPiece getPieceAt(Square sq) {
        Square thisSquare = board.getSquare(sq.getFile(), sq.getRank());
        ChessPiece thisPiece = (ChessPiece) thisSquare.getPiece();
        return thisPiece;
    }

    /**
     * Returns the colour of the current player
     *
     * @return PlayColour.BLACK or .WHITE
     */
    public PlayColour whoseMove() {
        return board.isBlackMove() ? PlayColour.BLACK : PlayColour.WHITE;
    }


    /*
    * Utility, it turns out ITCK only works properly when Move is created using start and end rank and file numbers,
    * so this private method does that.
    * There is a constructor ChessMove (Board, StartSquare, EndSquare) but it DOES NOT WORK
     */
    private Move makeAMove(Square startSquare, Square endSquare) throws Exception {
        try {
            Move m = new ChessMove((ChessBoard) board, startSquare.getFile(), startSquare.getRank(), endSquare.getFile(), endSquare.getRank());
            if (m.isLegal()) {
                history.add(m);  // This executes the move, and records it in history
            } else {
                LG.log(Level.SEVERE, "makeMyMove Illegal move : " + m.toString());
                throw new IllegalMoveException("ChessEngine:makeAMove ChessMove was illegal : " + m.toString());
            }

            //System.out.println(m);
            return m;
        } catch (Exception e) {
            LG.log(Level.SEVERE, "makeAMove failed to create new move");
            throw e;  // needs to be caught up the stack as well.
        }
    }

    /**
     * Used by the 'Other' player, not this ChessEngine, to make their move
     *
     * @param startSquare. There are several constructors for Square, eg new Square (byte File, byte Row)
     * @param endSquare
     * @return ChessEngineErrors, OK if all good.
     */
    public ChessEngineErrors makeMyMove(Square startSquare, Square endSquare) {
        ChessEngineErrors e = this.makeMyMove(startSquare, endSquare, false);
        return e;
    }

    /**
     * Private method that can be used by the engine as well as 'other' player.
     * Otherwise same as makeMyMove (qv)
     *
     * @param startSquare
     * @param endSquare
     * @param EnginePlaying TRUE if its this. (the ChessEngine),
     * @return
     */
    private ChessEngineErrors makeMyMove(Square startSquare, Square endSquare, boolean EnginePlaying) {
        // Validate matching idea of whose move it is.
        ChessPiece p = getPieceAt(startSquare);
        if (p == null) {
            LG.log(Level.SEVERE, "makeMyMove no piece on start square");
            return ChessEngineErrors.ILLEGAL_MOVE;
        }
        startSquare.setPiece(p);
        try {
            PlayColour c = board.isBlackMove() ? PlayColour.BLACK : PlayColour.WHITE;
            if ((!EnginePlaying && (c == engineColour)) || (EnginePlaying && (c != engineColour)))
            {
                // WRONG colour, whether player or engine move
                String who = EnginePlaying ? "Engine" : "Player";
                LG.log(Level.SEVERE, "makeMyMove wrong player colour in move by : " + who);
                return ChessEngineErrors.WRONG_COLOUR;
            }

            Move m = makeAMove(startSquare, endSquare); // Th eITYCK method that does the work

        } catch (Exception e) {
            LG.log(Level.SEVERE, "ChessEngine makeMyMove FAILED");
            e.printStackTrace();
            return ChessEngineErrors.ILLEGAL_MOVE;
        }

        return ChessEngineErrors.OK;
    }

    /**
     * Returns the FEN description of the game
     * FEN is Forsyth–Edwards Notation, see http://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
     * eg from test suite: rnbqkbnr/p1pppppp/1p6/8/1P6/N7/P1PPPPPP/R1BQKBNR b KQkq b3 0 1
     * upperCase isd White, lowercase is Black, numbers represent empty square counts on a rank
     * eg 1p6 above is rank 6, blank square, black pawn, 6 blanks.
     * The " b " means Black move,  KQkq means black and white can both castle on either side, b3 is en-passant (?)
     * and "0 1" is number of halfmoves and fullmoves. See Wikipedia for full detail
     *
     * @return string FEN
     */
    public String getGameFEN() {
        FEN f = new FEN();
        String fen = f.boardToString((ChessBoard) game.getBoard());
        return fen;
    }

    /**
     * Calls getGamefen but truncates to basic board position text only,
     * used to set up chess engine
     *
     * @return FEN board position only eg  rnbqkbnr/p1pppppp/1p6/8/1P6/N7/P1PPPPPP/R1BQKBNR b
     */
    private String getGameShortFEN() {
        String fen = getGameFEN();
        List<String> words = Arrays.asList(fen.split(" "));
        fen = words.get(0) + " " + words.get(1);

        return fen;

    }

    /**
     * @return current ChessBoard
     */
    public ChessBoard getChessBoard() {
        return board;
    }

    /**
     * This should be the ONLY method to interact with stockfish, as it does error checking on the response
     * The command may return multiple lines, as a single string separated by '\n'.
     * Split into a list<String>
     *
     * @param command, the command to be sent, such as "BestMove"
     * @return List<String> of responses, generally one or more
     * @throws Exception if StockFish responde 'Unknown command'
     */
    private List<String> askStockfish(String command, int milliseconds) throws Exception {
        stockfish.sendCommand(command);
        String response = stockfish.getOutput(milliseconds);
        // IF DEBUGGING note that a 'good' response is just "" (does not help much!)
        List<String> responseLines = Arrays.asList(response.split("\n"));

        if ((responseLines.size() > 0) && (responseLines.get(0).length() >= 7)) {
            String line0 = responseLines.get(0);
            List<String> Line0words = Arrays.asList(responseLines.get(0).split(" "));
            if (Line0words.get(0).toLowerCase().equals("unknown")) {
                LG.log(Level.SEVERE, "ChessEngine:askStockFish Chess engine has returned: " + line0);
                throw new IllegalMoveException("ChessEngine:askStockFish Chess engine has returned: " + line0);
            }
        }
        return responseLines;
    }

    /**
     * THE BIG ONE, ask the engine to make a move!
     *
     * @return the ChessMove made, i.e. the engine-controlled piece with before and after .
     * NOTE that methods on ChessMove provide all the necessary ionformation for continuing
     * play, such as isCheck, isCheckMate()
     */
    public ChessMove engineMove() {
        ChessMove move;

        try {
            if (this.whoseMove() != engineColour) {
                LG.log(Level.SEVERE, "ChessEngine engineMove wrong play colour");
                throw new IllegalMoveException("Wrong play colour");
            }

            // Set up the game positions in the engine
            List<String> responses = askStockfish("position fen " + getGameShortFEN(), 100);
            if (responses.size() > 1) {
                 new Exception("ChessEngine:chessMove error response too long at : " + responses.size() + " (Should be 1 blank line)");
            }

            // The big one, calculate a move
            responses = askStockfish("go", 250);  // TODO reduce 2000 ??
            // Should get several lines, last one is important and says eg:
            // bestmove g1f3 ponder d7d5 -
            // check words(0) == bestmove, and if ok, use words(1) for the move
            String lastLine = responses.get(responses.size() - 1);
            List<String> lastLineWords = Arrays.asList(lastLine.split(" "));  // and split up into distinct words,

            if (!lastLineWords.get(0).equals("bestmove")) {
                throw new Exception("ChessEngine:chessMove error no BestMove response, : " + lastLine);
            }

            // OK if we got this far, element 2nd (1) is board position eg g1f3
            String m = lastLineWords.get(1);
            Square fromSquare = new Square(m.charAt(0), m.charAt(1));
            Square toSquare = new Square(m.charAt(2), m.charAt(3));

            move = (ChessMove) makeAMove(fromSquare, toSquare);
            // System.out.println(move.dump());

        } catch (Exception e) {
            LG.log(Level.SEVERE, "engineMove FAILED");
            e.printStackTrace();
            return null;
        }
        return move;
    }


} // class ChessEngine
