package swantech;

/**
 * Created by simon on 31/03/15.
 */

/**
 * Used by ChessEngine amongst others.
 */
public enum PlayColour {
    WHITE, BLACK;

    /**
     * Simple check, eg <br>
     * PlayColour c = PlayColour.WHITE <br>
     * if (c.isBlack()) is FALSE
     * @return True if colour is BLACK
     */
    public boolean isBlack () { return (this == PlayColour.BLACK);}
}