package swantech;

import ictk.boardgame.chess.ChessBoard;
import ictk.boardgame.chess.ChessPiece;
import ictk.boardgame.chess.Square;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 01/04/15.
 */
public class StockFishTest {

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
    public void testUCICommand() throws  Exception {
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



}