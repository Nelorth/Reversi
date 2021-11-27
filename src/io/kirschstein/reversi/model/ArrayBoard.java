package io.kirschstein.reversi.model;

import io.kirschstein.reversi.util.Extremist;
import io.kirschstein.reversi.util.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of a Reversi game board. See {@link Board}.
 */
public class ArrayBoard implements Board {

    /**
     * The slots of the board. If a slot is occupied by a tile, then it contains
     * the {@link Player} to whom the tile belongs. Otherwise it is null.
     */
    private Player[][] slots;

    /**
     * The inital player in the game this board belongs to.
     */
    private final Player firstPlayer;

    /**
     * The player who had the last turn. If none had a turn yet, this is null.
     */
    private Player lastPlayer;

    /**
     * The difficulty level of the current game. Must be positive.
     */
    private int level;

    /**
     * Construct a new Reversi array board.
     *
     * @param firstPlayer The initial player.
     * @param level       The difficulty level.
     */
    public ArrayBoard(Player firstPlayer, int level) {
        if (firstPlayer == null) {
            throw new IllegalArgumentException("First player cannot be null.");
        } else if (level < 1) {
            throw new IllegalArgumentException("Level must be positive!");
        }

        slots = new Player[SIZE][SIZE];
        initializeSlots(firstPlayer);
        this.firstPlayer = firstPlayer;
        this.level = level;
    }

    /**
     * Initialize the board with the first player obtaining the lower left and
     * upper right tile of the 2x2 center square and the opponent occupying the
     * other two.
     *
     * @param firstPlayer The initial player.
     */
    private void initializeSlots(Player firstPlayer) {
        int hi = SIZE / 2;
        int lo = hi - 1;
        slots[lo][lo] = firstPlayer.opponent();
        slots[lo][hi] = firstPlayer;
        slots[hi][lo] = firstPlayer;
        slots[hi][hi] = firstPlayer.opponent();
    }

    /**
     * Validate whether a given position lies on the board.
     *
     * @param row The row index of the position.
     * @param col The column index of the position.
     * @return {@code true} iff {@code row} and {@code col} specify a valid
     *         position on the board.
     */
    public static boolean validatePosition(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getFirstPlayer() {
        return firstPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player next() {
        if (lastPlayer == null) {
            return firstPlayer;
        } else {
            if (isMovePossibleFor(lastPlayer.opponent())) {
                return lastPlayer.opponent();
            } else if (isMovePossibleFor(lastPlayer)) {
                return lastPlayer;
            } else {
                return null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board move(int row, int col) {
        if (gameOver()) {
            throw new IllegalMoveException("The game is already over.");
        } else if (next() != Player.HUMAN) {
            throw new IllegalMoveException("This is not your turn.");
        } else if (!validatePosition(row, col)) {
            throw new IllegalArgumentException("Invalid board position.");
        }

        return move(row, col, Player.HUMAN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board machineMove() {
        if (gameOver()) {
            throw new IllegalMoveException("The game is already over.");
        } else if (next() != Player.MACHINE) {
            throw new IllegalMoveException("This is not your turn.");
        }

        Node tree = generateGameTree(this, null, level);
        Position bestMove = Extremist.firstMax(tree.getChildren()).getMove();
        return move(bestMove.getRow(), bestMove.getCol(), Player.MACHINE);
    }

    /**
     * Execute a general move on the board.
     *
     * @param row    The slot's row index where a tile of the {@code player}
     *               should be placed on.
     * @param col    The slot's column index where a tile of the {@code player}
     *               should be placed on.
     * @param player The player performing the move.
     * @return A new board with the move executed. If the move is not valid,
     *         e.g., the defined slot was occupied and not at least one tile of
     *         the machine was reversed, then {@code null} will be returned.
     */
    private ArrayBoard move(int row, int col, Player player) {
        List<Position> reversableTiles = reversableTiles(row, col, player);
        if (reversableTiles.isEmpty()) {
            return null;
        } else {
            ArrayBoard boardClone = clone();
            boardClone.slots[row][col] = player;
            for (Position pos : reversableTiles) {
                boardClone.slots[pos.getRow()][pos.getCol()] = player;
            }
            boardClone.lastPlayer = player;
            return boardClone;
        }
    }

    /**
     * Generate a Minimax game tree from a given board state and the move that
     * led to this state up to a certain depth. Every node is scored with the
     * score of the board it represents plus (if it has children) the minimum of
     * the scores of the children if the human is next or the maximum of the
     * scores of the children if the machine is next.
     *
     * @param board The board state from which to generate the tree.
     * @param move  The last move on the {@code board}.
     * @param level The desired depth of the tree.
     * @return A Minimax game tree of depth {@code level}.
     */
    private Node generateGameTree(ArrayBoard board, Position move, int level) {
        if (level == 0 || board.gameOver()) {
            return new Node(move, Score.score(board));
        } else {
            Player nextPlayer = board.next();

            List<Node> children = new ArrayList<>();
            for (Position validMove : board.validMoves(nextPlayer)) {
                int row = validMove.getRow();
                int col = validMove.getCol();
                ArrayBoard result = board.move(row, col, nextPlayer);
                Node child = generateGameTree(result, validMove, level - 1);
                children.add(child);
            }

            /*
             * Calculate the score for this node. Note that, at this point,
             * the node MUST have children as the game is not over yet and the
             * final depth is not yet reached.
             */
            double score = Score.score(board);
            if (nextPlayer == Player.HUMAN) {
                score += Extremist.firstMin(children).getScore();
            } else {
                score += Extremist.firstMax(children).getScore();
            }

            return new Node(move, score, children);
        }
    }

    /**
     * Determine the positions of tiles that would be reversed as the result of
     * a certain move by a certain player.
     *
     * @param row    The row index of the move.
     * @param col    The column index of the move.
     * @param player The player making the move.
     * @return A list of all tiles reversed by the move. Consequently, the list
     *         is empty iff the move is not possible.
     */
    private List<Position> reversableTiles(int row, int col, Player player) {
        List<Position> reversableTiles = new ArrayList<>();
        if (slots[row][col] == null) {
            for (Direction direction : Direction.values()) {
                List<Position> directionTiles = new ArrayList<>();
                Player opponent = player.opponent();
                int i = direction.followVertically(row);
                int j = direction.followHorizontally(col);

                // follow direction as long as possible
                while (validatePosition(i, j) && slots[i][j] == opponent) {
                    directionTiles.add(new Position(i, j));
                    i = direction.followVertically(i);
                    j = direction.followHorizontally(j);
                }

                if (validatePosition(i, j) && slots[i][j] == player) {
                    reversableTiles.addAll(directionTiles);
                }
            }
        }
        return reversableTiles;
    }

    /**
     * Validate whether the specified move is possible for a certain player.
     *
     * @param row    The row index of the desired move position.
     * @param col    The column index of the desired move position.
     * @param player The concerned player.
     * @return {@code true} iff the specified move is valid on the board.
     */
    boolean validateMove(int row, int col, Player player) {
        return !reversableTiles(row, col, player).isEmpty();
    }

    /**
     * Determine the valid moves a certain player can make on a given board.
     *
     * @param player The concerned player.
     * @return All possible move positions for the {@code player}.
     */
    private List<Position> validMoves(Player player) {
        List<Position> validMoves = new ArrayList<>();
        for (int row = 0; row < slots.length; row++) {
            for (int col = 0; col < slots.length; col++) {
                if (validateMove(row, col, player)) {
                    validMoves.add(new Position(row, col));
                }
            }
        }
        return validMoves;
    }

    /**
     * Check whether a certain player is able to make a move at all.
     *
     * @param player The concerned player.
     * @return {@code true} iff {@code player} has at least one valid move
     *         possibility on the current board.
     */
    private boolean isMovePossibleFor(Player player) {
        return !validMoves(player).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be positive!");
        }
        this.level = level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean gameOver() {
        return next() == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        if (!gameOver()) {
            throw new IllegalStateException("The game is not over yet.");
        }

        int humanTiles = getNumberOfHumanTiles();
        int machineTiles = getNumberOfMachineTiles();

        if (humanTiles > machineTiles) {
            return Player.HUMAN;
        } else if (machineTiles > humanTiles) {
            return Player.MACHINE;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfHumanTiles() {
        return getNumberOfPlayerTiles(Player.HUMAN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfMachineTiles() {
        return getNumberOfPlayerTiles(Player.MACHINE);
    }

    /**
     * Count the number of tiles occupied by both players.
     *
     * @return The total number of occupied tiles.
     */
    int countOccupiedTiles() {
        return getNumberOfMachineTiles() + getNumberOfHumanTiles();
    }

    /**
     * Get the number of tiles belonging to a certain player currently placed on
     * the grid.
     *
     * @param player The player whose tiles shall be counted.
     * @return The number of machine tiles.
     */
    private int getNumberOfPlayerTiles(Player player) {
        int playerTilesNum = 0;
        for (Player[] slotRow : slots) {
            for (Player slot : slotRow) {
                if (slot == player) {
                    playerTilesNum++;
                }
            }
        }
        return playerTilesNum;
    }

    /**
     * Count the number of unoccupied slots ajdacent to a specified position.
     *
     * @param row The row index of the position.
     * @param col The col index of the position.
     * @return The number of free neighbors of the slot.
     */
    int countFreeNeighbors(int row, int col) {
        int freeNeighbors = 0;
        for (Direction direction : Direction.values()) {
            int adjRow = direction.followVertically(row);
            int adjCol = direction.followHorizontally(col);
            if (ArrayBoard.validatePosition(adjRow, adjCol)
                    && slots[adjRow][adjCol] == null) {
                freeNeighbors++;
            }
        }
        return freeNeighbors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getSlot(int row, int col) {
        if (!validatePosition(row, col)) {
            throw new IllegalArgumentException("Invalid board position.");
        }
        return slots[row][col];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayBoard clone() {
        ArrayBoard boardClone;
        try {
            boardClone = (ArrayBoard) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Clone not supported. This cannot happen!");
        }
        boardClone.slots = slots.clone();
        for (int i = 0; i < boardClone.slots.length; i++) {
            boardClone.slots[i] = slots[i].clone(); // sufficient for enum refs
        }
        return boardClone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Player[] slotRow : slots) {
            for (Player slot : slotRow) {
                if (slot != null) {
                    sb.append(slot);
                } else {
                    sb.append('.');
                }
                sb.append(' ');
            }
            sb.replace(sb.length() - 1, sb.length(), "\n");
        }
        return sb.toString();
    }
}
