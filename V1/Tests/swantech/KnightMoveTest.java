package swantech;

import ictk.boardgame.chess.ChessBoard;
import ictk.boardgame.chess.ChessMove;
import ictk.boardgame.chess.Square;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 01/04/15.
 * Test the Knight moves, 8 in all
 */

@RunWith(Parameterized.class)
public class KnightMoveTest {
@Parameters
public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
            // All possible knight moves from starting pos
            { 'w', 'b', '1', 'a', '3'},
            { 'w', 'b', '1', 'c', '3'},
            { 'w', 'g', '1', 'f', '3'},
            { 'w', 'g', '1', 'h', '3'},

            { 'b', 'b', '8', 'a', '6'},
            { 'b', 'b', '8', 'c', '6'},
            { 'b', 'g', '8', 'f', '6'},
            { 'b', 'g', '8', 'h', '6'}
    });
}

    private PlayColour playerColour;
    private PlayColour engineColour;
    private char fromFile;
    private char fromRank;
    private char toFile;
    private char toRank;

public KnightMoveTest(char gameColour, char a, char b, char c, char d) {
    if (gameColour == 'w'){
        playerColour = PlayColour.WHITE;
        engineColour = PlayColour.BLACK;
    } else {
        playerColour = playerColour.BLACK;
        engineColour = PlayColour.WHITE;
    }

    fromFile = a;
    fromRank = b;
    toFile = c;
    toRank = d;
}

    ChessEngine chessEngine;
    ChessBoard board;


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void knightMove() throws  Exception
    {
        chessEngine = new ChessEngine(engineColour);
        board = chessEngine.getChessBoard();
        Square fromSquare = new Square (fromFile, fromRank);
        Square toSquare = new Square(toFile, toRank);

        ChessEngineErrors e = chessEngine.makeMyMove(fromSquare, toSquare);
        String mesg = "Knight move : " + Character.toString(fromFile) + Character.toString(fromRank) +
                            " to : " + Character.toString(toFile) + Character.toString(toRank) + " FAILED";
        assertEquals(mesg, ChessEngineErrors.OK, e);
    }




}