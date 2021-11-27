package io.kirschstein.reversi.model;

/**
 * A player in the Reversi game, either human or computer.
 */
public enum Player {

    /**
     * The human.
     */
    HUMAN,

    /**
     * The computer.
     */
    MACHINE;

    /**
     * Determine this player's opponent. The opponent of the human is the
     * computer and vice versa.
     *
     * @return The opponent of this player.
     */
    public Player opponent() {
        return this == HUMAN ? MACHINE : HUMAN;
    }

    /**
     * Convert this player into a human-readable string representation.
     *
     * Its format is 'X' for the human and 'O' for the computer.
     *
     * @return A human-readable string representation of this node.
     */
    @Override
    public String toString() {
        return this == HUMAN ? "X" : "O";
    }
}
