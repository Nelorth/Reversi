package io.kirschstein.reversi.util;

/**
 * An abstract position on some grid, encapsulating a row and a column index.
 */
public final class Position {

    /**
     * The row index.
     */
    private final int row;

    /**
     * The column index.
     */
    private final int col;

    /**
     * Construct a new grid position from given row and column indices.
     *
     * @param row The row index.
     * @param col The column index.
     */
    public Position(int row, int col) {
        if (row < 0) {
            throw new IllegalArgumentException("Row index cannot be negative!");
        } else if (col < 0) {
            throw new IllegalArgumentException("Col index cannot be negative!");
        }

        this.row = row;
        this.col = col;
    }

    /**
     * Get the row of this grid position.
     *
     * @return The row index.
     */
    public int getRow() {
        return row;
    }

    /**
     * Get the column of this grid position.
     *
     * @return The column index.
     */
    public int getCol() {
        return col;
    }

    /**
     * Convert this position into a human-readable string representation.
     *
     * Its format is "[row][col]", where row is the row index and col the column
     * index of this grid position.
     *
     * @return A human-readable string representation of this position.
     */
    @Override
    public String toString() {
        return "[" + row + "][" + col + "]";
    }
}
