package io.kirschstein.reversi.model;

/**
 * Interface for a Reversi game, also known as Othello.
 *
 * A human plays against the machine.
 */
public interface Board extends Cloneable {

    /**
     * The number of rows and columns of the game grid. Originally 8. Here, even
     * and at least 4.
     */
    int SIZE = 8;

    /**
     * Get the player who should start or already has started the game.
     *
     * @return The player who makes the initial move.
     */
    Player getFirstPlayer();

    /**
     * Get the player who owns the next game turn.
     *
     * @return The player who is allowed to make the next turn. {@code null} if
     *         none of the players is able to take a turn.
     */
    Player next();

    /**
     * Execute a human move. This method does not change the state of this
     * instance, which is treated here as immutable. Instead, a new board/game
     * is returned, which is a copy of {@code this} with the move executed.
     *
     * @param row The slot's row index where a tile of the human player should
     *            be placed on.
     * @param col The slot's column index where a tile of the human player
     *            should be placed on.
     * @return A new board with the move executed. If the move is not valid,
     *         e.g., the defined slot was occupied and not at least one tile of
     *         the machine was reversed, then {@code null} will be returned.
     * @throws IllegalMoveException     If the game is already over, or it is
     *                                  not the human's turn.
     * @throws IllegalArgumentException If the provided parameters are invalid,
     *                                  e.g. the defined slot is not on the
     *                                  grid.
     */
    Board move(int row, int col);

    /**
     * Execute a machine move. This method does not change the state of this
     * instance, which is treated here as immutable. Instead, a new board/game
     * is returned, which is a copy of {@code this} with the move executed.
     *
     * @return A new board with the move executed.
     * @throws IllegalMoveException If the game is already over, or it is not
     *                              the machine's turn.
     */
    Board machineMove();

    /**
     * Set the skill level of the machine.
     *
     * @param level The skill as a number, must be at least 1.
     */
    void setLevel(int level);

    /**
     * Check if the game is over. Either one player has won or there is a tie,
     * i.e. no player can perform a move any more.
     *
     * @return {@code true} if and only if the game is over.
     */
    boolean gameOver();

    /**
     * Check if the game state is won. Should only be called if {@link
     * #gameOver()} returns {@code true}.
     *
     * @return The winner or {@code null} in case of a tie.
     */
    Player getWinner();

    /**
     * Get the number of human tiles currently placed on the grid.
     *
     * @return The number of human tiles.
     */
    int getNumberOfHumanTiles();

    /**
     * Get the number of machine tiles currently placed on the grid.
     *
     * @return The number of machine tiles.
     */
    int getNumberOfMachineTiles();

    /**
     * Get the content of the slot at the specified coordinates. Either it
     * contains a tile of one of the two players already or it is empty.
     *
     * @param row The row of the slot in the game grid.
     * @param col The column of the slot in the game grid.
     * @return The slot's content. {@code null} iff empty.
     */
    Player getSlot(int row, int col);

    /**
     * Create and return a deep copy of this board.
     *
     * @return A clone.
     */
    Board clone();

    /**
     * Get the string representation of this board as row x column matrix. Each
     * slot is represented by one of the three chars '.', 'X', or 'O'. '.' means
     * that the slot currently contains no tile. 'X' means that it contains a
     * tile of the human player. 'O' means that it contains a machine tile. In
     * contrast to the rows, the columns are whitespace separated.
     *
     * @return The string representation of the current Reversi game.
     */
    @Override
    String toString();
}
