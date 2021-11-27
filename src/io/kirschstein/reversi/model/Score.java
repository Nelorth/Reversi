package io.kirschstein.reversi.model;

/**
 * Utility class responsible for score calculations on a Reversi array board.
 */
final class Score {

    /**
     * Heuristic significance values for each cell of the game.
     */
    private static final int[][] SIGNIFICANCE_MATRIX = {
        {9999,   5, 500, 200, 200, 500,   5, 9999},
        {   5,   1,  50, 150, 150,  50,   1,    5},
        { 500,  50, 250, 100, 100, 250,  50,  500},
        { 200, 150, 100,  50,  50, 100, 150,  200},
        { 200, 150, 100,  50,  50, 100, 150,  200},
        { 500,  50, 250, 100, 100, 250,  50,  500},
        {   5,   1,  50, 150, 150,  50,   1,    5},
        {9999,   5, 500, 200, 200, 500,   5, 9999}
    };

    /**
     * Suppress default constructor to ensure non-instantiability.
     */
    private Score() {
        // empty
    }

    /**
     * Calculate the score for a given board. This score rates the game state
     * represented by the board from the perspective of the machine.
     *
     * @param board The board to be scored.
     * @return The score of the {@code board}.
     */
    static double score(ArrayBoard board) {
        if (Board.SIZE != SIGNIFICANCE_MATRIX.length) {
            throw new InternalError("Score calculation only for 8x8 boards.");
        }
        return significance(board) + mobility(board) + potential(board);
    }

    /**
     * Calculate the significance score for a given board which rates the
     * occupied fields of the players by heuristic weightings.
     *
     * @param board The board to be significance-scored.
     * @return The significance score of the {@code board}.
     */
    private static double significance(ArrayBoard board) {
        int humanSignificance = 0;
        int machineSignificance = 0;
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Player tileOccupant = board.getSlot(row, col);
                if (tileOccupant == Player.HUMAN) {
                    humanSignificance += SIGNIFICANCE_MATRIX[row][col];
                } else if (tileOccupant == Player.MACHINE) {
                    machineSignificance += SIGNIFICANCE_MATRIX[row][col];
                }
            }
        }
        return machineSignificance - 1.5 * humanSignificance;
    }

    /**
     * Calculate the mobility score for a given board which considers the number
     * of possible moves each player could make in this state.
     *
     * @param board The board to be mobility-scored.
     * @return The mobility score of the {@code board}.
     */
    private static double mobility(ArrayBoard board) {
        int humanMobility = 0;
        int machineMobility = 0;
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.validateMove(row, col, Player.HUMAN)) {
                    humanMobility++;
                }
                if (board.validateMove(row, col, Player.MACHINE)) {
                    machineMobility++;
                }
            }
        }
        int s = Board.SIZE * Board.SIZE;
        int n = board.countOccupiedTiles();
        return s / (1.0 * n) * (3.0 * machineMobility - 4.0 * humanMobility);
    }

    /**
     * Calculate the potential score for a given board which takes into account
     * free tiles directly adjacent to tiles occupied by the opponent.
     *
     * @param board The board to be potential-scored.
     * @return The potential score of the {@code board}.
     */
    private static double potential(ArrayBoard board) {
        int humanPotential = 0;
        int machinePotential = 0;
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Player tileOccupant = board.getSlot(row, col);
                if (tileOccupant == Player.HUMAN) {
                    machinePotential += board.countFreeNeighbors(row, col);
                } else if (tileOccupant == Player.MACHINE) {
                    humanPotential += board.countFreeNeighbors(row, col);
                }
            }
        }
        int s = Board.SIZE * Board.SIZE;
        int n = board.countOccupiedTiles();
        return s / (2.0 * n) * (2.5 * machinePotential - 3.0 * humanPotential);
    }
}
