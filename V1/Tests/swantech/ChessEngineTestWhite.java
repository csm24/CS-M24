package swantech;

import ictk.boardgame.chess.ChessBoard;
import ictk.boardgame.chess.ChessMove;
import ictk.boardgame.chess.ChessPiece;
import ictk.boardgame.chess.Square;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by simon on 01/04/15.
 */
public class ChessEngineTestWhite {

    ChessEngine chessEngine;
    ChessBoard board;

    // Just shorthand to avoid the typecast on every use.
    private Square intSquare(int f, int r)
    {
        Square s = new Square((byte)f, (byte)r);
        return s;
    }

    @Before
    public void setUp() throws Exception {
        chessEngine = new ChessEngine(PlayColour.BLACK);
        board = chessEngine.getChessBoard();
    }

    @After
    public void tearDown() throws Exception {
    }
    

    @Test
    public void testWhitePawnsMoves() throws  Exception {
        // Check the pawns can all move to two squares
        ArrayList<Square> squares;

        for (int i = 1; i <= 8; i++) {
            // White pawn, can move (from Bx) to Cx or Dx, x=1..8
            squares = chessEngine.getLegalMoves(intSquare(i, 2));
            assertEquals("White Pawn has two legal moves",    2, squares.size());
            assertEquals("White Pawn stays in same file",     i, squares.get(0).getFile());
            assertEquals("White Pawn can move 1 square",      3, squares.get(0).getRank());
            assertEquals("White Pawn stays in same file",     i, squares.get(1).getFile());
            assertEquals("White Pawn can move 2 squares",     4, squares.get(1).getRank());
        }
    }


    @Test
    public void testBlackPawnsMoves() throws  Exception {
        // Expecting WHITE move so should ALL fail (null)
        ArrayList<Square> squares;

        for (int i = 1; i <= 8; i++) {
            // Black pawn, can move (from Bx) to Cx or Dx, x=1..8
            squares = chessEngine.getLegalMoves(intSquare(i, 7));
            assertNull("Black Pawn has NO legal moves",    squares);
        }
    }
    @Test
    public void testStartMoves() throws  Exception {
        // Check the rank 1 & 8 pieces EXCEPT knights, which have their own test.
        // None of these pieces can move!
        ArrayList<Square> squares;

        for (int i = 1; i <= 8; i++) {
            if ((i != 2) && (i != 7)) {
                // not a Knight
                squares = chessEngine.getLegalMoves(intSquare(i, 1));
                assertEquals ("Back row (White) cannot move (except Knights) " + i,  0, squares.size());
                squares = chessEngine.getLegalMoves(intSquare(i, 8));
                assertNull ("Black Back (Black) row cannot move AT ALL ", squares);
            }
        }
    }


    @Test
    public void testWhiteKnightMoves() throws  Exception {
        // Check the knights
        ArrayList<Square> squares;

        squares = chessEngine.getLegalMoves(intSquare(2, 1));
        assertEquals("White Knight b1 has two legal moves",    2, squares.size());
        assertEquals("White Knight b1 -> a3", 3, squares.get(0).getFile());
        assertEquals("White Knight b1 -> a3", 3, squares.get(0).getRank());
        assertEquals("White Knight b1 -> c3", 1, squares.get(1).getFile());
        assertEquals("White Knight b1 -> c3", 3, squares.get(1).getRank());

        squares = chessEngine.getLegalMoves(intSquare(7, 1));
        assertEquals("White Knight b7 has two legal moves",    2, squares.size());
        assertEquals("White Knight b7 -> f3", 8, squares.get(0).getFile());
        assertEquals("White Knight b7 -> f3", 3, squares.get(0).getRank());
        assertEquals("White Knight b7 -> h3", 6, squares.get(1).getFile());
        assertEquals("White Knight b7 -> h3", 3, squares.get(1).getRank());
    }

    @Test
    public void testBlackKnightMoves() throws  Exception {
        // Check the knights
        ArrayList<Square> squares;

        squares = chessEngine.getLegalMoves(intSquare(2, 8));
        assertNull("Black Knight b1 has NO legal moves", squares);

        squares = chessEngine.getLegalMoves(intSquare(7, 8));
        assertNull("Black Knight b1 has NO legal moves", squares);


    }


    @Test
    public void testGetColour() throws Exception {
        assertEquals("Check it is WHITE to play", chessEngine.whoseMove(), PlayColour.WHITE);
    }



    @Test public void testGetFEN () throws Exception {
        String fen = chessEngine.getGameFEN();
        assertEquals("Starting FEN should be rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0", fen);
    }


    @Test
    public  void testKnightFail() throws  Exception {
        System.out.println("This test should FAIL and cause TWO SEVERE logs and a stacktrace in ChessEngine");
        ChessEngineErrors e = chessEngine.makeMyMove(new Square('b', '1'), new Square('a', '4'));
        assertEquals("Knight b1 to a4", ChessEngineErrors.ILLEGAL_MOVE, e);
    }

    @Test
    public  void testSomeMovesAndFEN() throws  Exception {
        // Make three moves...
        ChessEngineErrors e = chessEngine.makeMyMove(new Square('b', '1'), new Square('a', '3'));
        assertEquals("Knight b1->a3", e, ChessEngineErrors.OK);

        ChessMove m = chessEngine.engineMove(); // usually the engine does pawn D7 -> D5
        assertTrue("1st ChessEngine move" , m.getDestination().toString().equals("d5"));
        // MAY NOT be an error if this fails, the chess engine may make another move,
        // if different engine or different parameters

        e = chessEngine.makeMyMove(new Square ('b', '2'), new Square('b', '4'));
        assertEquals("Pawn b2 -> b4", e, ChessEngineErrors.OK);
        assertTrue("Pawn b2 -> b4", chessEngine.getPieceAt(new Square('b', '4')).isPawn());
        // Check the status of the knight...
        ChessPiece k = chessEngine.getPieceAt(new Square('a', '3'));
        assertEquals("Knight b1 to a3", ChessEngineErrors.OK, e);
        assertEquals("Expect a Knight at a3", k.toString(), "Knight");
        String fen = chessEngine.getGameFEN();
        // NB this depends on the engine and could easily change
        assertEquals("Check board after three moves", fen, "rnbqkbnr/ppp1pppp/8/3p4/1P6/N7/P1PPPPPP/R1BQKBNR b KQkq b3 0 1");
    }

}