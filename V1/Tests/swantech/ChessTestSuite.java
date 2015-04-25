package swantech;

/**
 * Created by simon on 05/04/15.
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ChessEngineTestWhite.class,
        ChessEngineTestBlack.class,
        KnightMoveTest.class,
        StockFishTest.class

    })

public class ChessTestSuite {
    // the class remains empty,
    // used only as a holder for the above annotations
}