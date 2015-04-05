package swantech;

/**
 * Created by simon on 31/03/15.
 */

/**
 * Enumeration of the errors the ChessEngine class will return
 */
public enum ChessEngineErrors {
    OK,                 ///< Success
    ILLEGAL_MOVE,       ///< General illegal move error
    ILLEGAL_SQUARE,     ///< Co-ordinates  are outside the range (rank) a-h or (file) 1-8
    MOVE_RANGE_ERROR,   ///< Move is off the board
    WOULD_CHECK,        ///< Move would put player in check
    WRONG_COLOUR,        ///< An incorrect play colour has been specified

}
