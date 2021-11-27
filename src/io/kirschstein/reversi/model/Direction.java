package io.kirschstein.reversi.model;

/**
 * The possible tile reversion directions on a Reversi board.
 */
enum Direction {

    /**
     * Moving north on the board, i.e. one row up.
     */
    NORTH(-1, 0),

    /**
     * Moving south on the board, i.e. one row down.
     */
    SOUTH(1, 0),

    /**
     * Moving west on the board, i.e. one column left.
     */
    WEST(0, -1),

    /**
     * Moving east on the board, i.e. one column right.
     */
    EAST(0, 1),

    /**
     * Moving northwest on the board, i.e. one row up and one column left.
     */
    NORTHWEST(-1, -1),

    /**
     * Moving northeast on the board, i.e. one row up and one column right.
     */
    NORTHEAST(-1, 1),

    /**
     * Moving southwest on the board, i.e. one row down and one column left.
     */
    SOUTHWEST(1, -1),

    /**
     * Moving southeast on the board, i.e. one row down and one column right.
     */
    SOUTHEAST(1, 1);

    /**
     * The change in row. 0 = no change, -1 = up, 1 = down.
     */
    private int rowOffset;

    /**
     * The change in column. 0 = no change, -1 = left, 1 = right.
     */
    private int colOffset;

    /**
     * Construct a new Direction.
     *
     * @param colOffset The change in row.
     * @param rowOffset The change in column.
     */
    Direction(int colOffset, int rowOffset) {
        this.rowOffset = rowOffset;
        this.colOffset = colOffset;
    }

    /**
     * Follow this direction vertically, i.e. apply its row offset.
     *
     * @param row The row index from which to follow this direction.
     * @return The index of the resulting row after following the direction.
     */
    int followVertically(int row) {
        return row + rowOffset;
    }

    /**
     * Follow this direction horizontally, i.e. apply its column offset.
     *
     * @param col The column index from which to follow this direction.
     * @return The index of the resulting column after following the direction.
     */
    int followHorizontally(int col) {
        return col + colOffset;
    }
}
