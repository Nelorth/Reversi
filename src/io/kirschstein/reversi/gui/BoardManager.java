package io.kirschstein.reversi.gui;

import io.kirschstein.reversi.model.ArrayBoard;
import io.kirschstein.reversi.model.Board;
import io.kirschstein.reversi.model.Player;
import io.kirschstein.reversi.util.Observable;
import io.kirschstein.reversi.util.Observer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class manages instances of Reversi boards offering undo functionality
 * while at all times representing the latest board.
 */
class BoardManager implements Observable {
    /**
     * Stack of all games in a single game from most recent to oldest.
     *
     * Important Invariant: The board stack always contains at least one board
     * to refer to, i.e. it is never empty!
     */
    private Deque<Board> boardStack;

    /**
     * Collection of game observers.
     */
    private List<Observer> observers;

    /**
     * Construct a new board manager and already the first board as well.
     *
     * @param firstPlayer The initial player of the first game.
     * @param level       The difficulty level of the first game.
     */
    BoardManager(Player firstPlayer, int level) {
        boardStack = new ArrayDeque<>();
        boardStack.push(new ArrayBoard(firstPlayer, level));
        observers = new ArrayList<>();
    }

    /**
     * Start a new game by emptying the stack and adding a fresh board.
     *
     * @param firstPlayer The initial player of the new game.
     * @param level       The difficulty level of the new game.
     */
    void newGame(Player firstPlayer, int level) {
        boardStack.clear();
        boardStack.push(new ArrayBoard(firstPlayer, level));
        notifyObservers();
    }

    /**
     * Undo the last human move by reverting to the state before the last human
     * move. You have to check beforehand if undo is possible in this state
     * using {@link BoardManager#undoPossible()}.
     */
    void undo() {
        if (!undoPossible()) {
            throw new IllegalStateException("Undo impossible in this state!");
        }
        boardStack.pop();
        while (currentBoard().next() != Player.HUMAN) {
            boardStack.pop();
        }
        notifyObservers();
    }

    /**
     * Check whether undo functionality is available in the current state.
     *
     * @return {@code true} iff undoing the last human move is possible.
     */
    boolean undoPossible() {
        // stream of all boards except the latest
        Stream<Board> stream = boardStack.stream().skip(1);
        return stream.anyMatch(board -> board.next() == Player.HUMAN);
    }

    /**
     * Retrieve the current board, i.e. the top of the board stack.
     *
     * @return The most recent board.
     */
    private Board currentBoard() {
        if (boardStack.isEmpty()) {
            throw new IllegalStateException("Board stack shall not be empty.");
        }
        return boardStack.peek();
    }

    /**
     * Get the starting player of the current game.
     *
     * @return The originally first player of the current board.
     */
    Player getFirstPlayer() {
        return currentBoard().getFirstPlayer();
    }

    /**
     * Get the player who owns the next turn in the current game.
     *
     * @return The next player on the current board.
     */
    Player next() {
        return currentBoard().next();
    }

    /**
     * Execute a human move on the current board at a specified position.
     *
     * @param row The row index of the desired slot.
     * @param col The column index of the desired slot.
     * @return {@code true} iff the move is allowed.
     */
    boolean move(int row, int col) {
        Board moveResult = currentBoard().move(row, col);
        if (moveResult != null) {
            boardStack.push(moveResult);
            notifyObservers();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Execute a machine move on the current board.
     */
    void machineMove() {
        boardStack.push(currentBoard().machineMove());
        notifyObservers();
    }

    /**
     * Set the level of the current game.
     *
     * @param level The difficulty level to set.
     */
    void setLevel(int level) {
        if (boardStack.isEmpty()) {
            throw new IllegalStateException("Board stack shall not be empty.");
        }
        boardStack.forEach(b -> b.setLevel(level));
    }

    /**
     * Determine whether the current game is over in this state.
     *
     * @return {@code true} iff the current game is over.
     */
    boolean gameOver() {
        return currentBoard().gameOver();
    }

    /**
     * Get the winner of the current game. This makes sense only if the game is
     * actually over! Check this via {@link BoardManager#gameOver()}.
     *
     * @return The winner of the current game.
     */
    Player getWinner() {
        return currentBoard().getWinner();
    }

    /**
     * Retrieve the total number of tiles occupied by the user.
     *
     * @return The number of human tiles on the current board.
     */
    int getNumberOfHumanTiles() {
        return currentBoard().getNumberOfHumanTiles();
    }

    /**
     * Retrieve the total number of tiles occupied by the bot.
     *
     * @return The number of machine tiles on the current board.
     */
    int getNumberOfMachineTiles() {
        return currentBoard().getNumberOfMachineTiles();
    }

    /**
     * Get the content of the specified slot on the current board.
     *
     * @param row The row index of the desired slot.
     * @param col The oolumn index of the desired slot.
     * @return The occupant of the slot.
     */
    Player getSlot(int row, int col) {
        return currentBoard().getSlot(row, col);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attachObserver(Observer o) {
        observers.add(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detachObserver(Observer o) {
        observers.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyObservers() {
        observers.forEach(o -> o.update(this));
    }
}
