package io.kirschstein.reversi.gui;

import io.kirschstein.reversi.model.Board;
import io.kirschstein.reversi.model.Player;
import io.kirschstein.reversi.util.Observable;
import io.kirschstein.reversi.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A Reversi game window.
 */
public class ReversiFrame extends JFrame implements Observer {

    /**
     * Class version for ensuring serialization compatibility.
     */
    private static final long serialVersionUID = 7842230275537206160L;

    /**
     * Unified title for program windows.
     */
    private static final String WINDOW_TITLE = "Reversi";

    /**
     * Range of supported difficulty levels.
     */
    private static final Integer[] SUPPORTED_LEVELS = {1, 2, 3, 4, 5, 6, 7};

    /**
     * Initially set default difficulty level.
     */
    private static final int DEFAULT_LEVEL = 3;

    /**
     * Font size for player scores.
     */
    private static final float SCORE_FONT_SIZE = 16f;

    /**
     * Color assigned to the user.
     */
    private static final Color HUMAN_COLOR = Color.BLUE;

    /**
     * Color assigned to the bot.
     */
    private static final Color MACHINE_COLOR = Color.RED;

    /**
     * Border of a slot cell on the grid.
     */
    private static final Border SLOT_BORDER = BorderFactory
            .createLineBorder(Color.BLACK);

    /**
     * Border of a currently selected slot cell.
     */
    private static final Border SLOT_BORDER_HOVER = BorderFactory
            .createLineBorder(ReversiFrame.colorOf(Player.HUMAN), 3);

    /**
     * Amount of space for grid captions.
     */
    private static final int SPACING = 15;

    /**
     * Amount of side margin for the control bar.
     */
    private static final int SIDE_MARGIN = 10;

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Agent for game model interaction.
     */
    private final BoardManager boardManager;

    /**
     * Thread for complex game move calculations.
     */
    private Thread moveThread;

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Panels representing all slots on the Reversi grid.
     */
    private SlotPanel[][] slotPanels;

    /**
     * Label for the user score.
     */
    private JLabel humanScore;

    /**
     * Label for the bot score.
     */
    private JLabel machineScore;

    /**
     * Difficulty level selection widget.
     */
    private JComboBox<Integer> levelChooser;

    /**
     * Button for undoing the last user move.
     */
    private JButton undoButton;

    /**
     * Construct a new Reversi game window.
     */
    public ReversiFrame() {
        boardManager = new BoardManager(Player.HUMAN, DEFAULT_LEVEL);
        boardManager.attachObserver(this);
        moveThread = new Thread(); // trivial thread avoids null reference
        initComponents();
        setVisible(true);
    }

    /**
     * Map a given player to his assigned color.
     *
     * @param player The concerned player.
     * @return The color assigned to {@code player}.
     */
    static Color colorOf(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Null does not have a color.");
        }
        switch (player) {
        case HUMAN:
            return HUMAN_COLOR;
        case MACHINE:
            return MACHINE_COLOR;
        default:
            throw new InternalError("Unknown player");
        }
    }

    /**
     * Initialize all GUI components.
     */
    private void initComponents() {
        initFrame();
        initGameGrid();
        initLegend();
        initControlBar();
        pack();
    }

    /**
     * Initialize the basic frame properties.
     */
    private void initFrame() {
        setTitle(WINDOW_TITLE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null); // center window
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                clearMoveThread();
            }
        });
    }

    /**
     * Initialize the game grid panel.
     */
    private void initGameGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(Board.SIZE, Board.SIZE));
        slotPanels = new SlotPanel[Board.SIZE][Board.SIZE];
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Player occupant = boardManager.getSlot(row, col);
                slotPanels[row][col] = new SlotPanel(occupant);
                slotPanels[row][col].setBorder(SLOT_BORDER);
                setSlotListener(row, col, slotPanels[row][col]);
                gridPanel.add(slotPanels[row][col]);
            }
        }
        getContentPane().add(gridPanel, BorderLayout.CENTER);
    }

    /**
     * Generate a mouse listener for a certain slot which reacts to click events
     * by launching move processing in a separate thread.
     *
     * @param row   The row index of the slot.
     * @param col   The column index of the slot.
     * @param panel The panel to set the listener on.
     */
    private void setSlotListener(int row, int col, JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!boardManager.gameOver() && !moveThread.isAlive()) {
                    panel.setBorder(SLOT_BORDER);
                    moveThread = new Thread(() -> processHumanMove(row, col));
                    moveThread.start();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!boardManager.gameOver() && !moveThread.isAlive()) {
                    panel.setBorder(SLOT_BORDER_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(SLOT_BORDER);
            }
        });
    }

    /**
     * Initialize the captions surrounding the game grid.
     */
    private void initLegend() {
        // row numbers on the left
        JPanel rowCaptionBar = createCaptionBar(true);
        getContentPane().add(rowCaptionBar, BorderLayout.WEST);

        // column numbers on the top
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel colCaptionBar = createCaptionBar(false);
        topPanel.add(colCaptionBar, BorderLayout.CENTER);
        topPanel.add(Box.createHorizontalStrut(SPACING), BorderLayout.WEST);
        topPanel.add(Box.createHorizontalStrut(SPACING), BorderLayout.EAST);
        getContentPane().add(topPanel, BorderLayout.NORTH);

        // symmetrizing margin on the right
        Component marginRight = Box.createHorizontalStrut(SPACING);
        getContentPane().add(marginRight, BorderLayout.EAST);
    }

    /**
     * Create a caption bar for labelling slot rows or columns.
     *
     * @param vertical Whether to orient the labels vertically or horizontally.
     * @return The caption bar with slot labels.
     */
    private JPanel createCaptionBar(boolean vertical) {
        JPanel captionBar = new JPanel();
        if (vertical) {
            captionBar.setLayout(new GridLayout(Board.SIZE, 1));
        } else {
            captionBar.setLayout(new GridLayout(1, Board.SIZE));
        }
        for (int slot = 1; slot <= Board.SIZE; slot++) {
            JLabel slotLabel = new JLabel(String.valueOf(slot));
            slotLabel.setPreferredSize(new Dimension(SPACING, SPACING));
            slotLabel.setHorizontalAlignment(SwingConstants.CENTER);
            captionBar.add(slotLabel);
        }
        return captionBar;
    }

    /**
     * Initialize the control bar on the bottom of the window.
     */
    private void initControlBar() {
        JPanel controlBar = new JPanel(new BorderLayout());
        controlBar.setBorder(new EmptyBorder(0, SIDE_MARGIN, 0, SIDE_MARGIN));
        initScoreDisplay(controlBar);
        initControlWidgets(controlBar);
        getContentPane().add(controlBar, BorderLayout.SOUTH);
    }

    /**
     * Initialize the player score labels.
     *
     * @param controlBar The control bar to add the labels to.
     */
    private void initScoreDisplay(JPanel controlBar) {
        humanScore = createScoreLabel(Player.HUMAN);
        controlBar.add(humanScore, BorderLayout.WEST);

        machineScore = createScoreLabel(Player.MACHINE);
        controlBar.add(machineScore, BorderLayout.EAST);

        updatePlayerScores();
    }

    /**
     * Create an empty score label for a given player.
     *
     * @param player The player associated with the score label.
     * @return The ready-to-use score label.
     */
    private JLabel createScoreLabel(Player player) {
        JLabel scoreLabel = new JLabel();
        scoreLabel.setForeground(colorOf(player));
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(SCORE_FONT_SIZE));
        return scoreLabel;
    }

    /**
     * Initialize the control widgets.
     *
     * @param controlBar The control bar to add the widgets to.
     */
    private void initControlWidgets(JPanel controlBar) {
        JPanel widgetPanel = new JPanel();
        widgetPanel.add(createLevelChooser());
        widgetPanel.add(createNewButton());
        widgetPanel.add(createSwitchButton());
        widgetPanel.add(createUndoButton());
        widgetPanel.add(createQuitButton());
        controlBar.add(widgetPanel, BorderLayout.CENTER);
    }

    /**
     * Initialize the difficulty level selection widget.
     *
     * @return The initialized level chooser combo box.
     */
    private JComboBox<Integer> createLevelChooser() {
        levelChooser = new JComboBox<>(SUPPORTED_LEVELS);
        levelChooser.setSelectedItem(DEFAULT_LEVEL);
        levelChooser.addActionListener(a -> boardManager.setLevel(getLevel()));
        return levelChooser;
    }

    /**
     * Initialize the button for starting a new game with the same initial
     * player as before.
     *
     * @return The initialized new button.
     */
    private JButton createNewButton() {
        JButton newButton = new JButton("New");
        newButton.setMnemonic('n');
        newButton.addActionListener(a -> {
            restartGame(boardManager.getFirstPlayer(), getLevel());
        });
        return newButton;
    }

    /**
     * Initialize the button for starting a new game and switching the initial
     * player.
     *
     * @return The initialized switch button.
     */
    private JButton createSwitchButton() {
        JButton switchButton = new JButton("Switch");
        switchButton.setMnemonic('s');
        switchButton.addActionListener(a -> {
            Player opponent = boardManager.getFirstPlayer().opponent();
            restartGame(opponent, getLevel());
        });
        return switchButton;
    }

    /**
     * Initialize the button for undoing the last human move.
     *
     * @return The initialized undo button.
     */
    private JButton createUndoButton() {
        undoButton = new JButton("Undo");
        undoButton.setMnemonic('u');
        undoButton.addActionListener(a -> {
            clearMoveThread();
            boardManager.undo();
        });
        undoButton.setEnabled(false); // invariant: only active if undo possible
        return undoButton;
    }

    /**
     * Initialize the button for terminating the program.
     *
     * @return The initialized quit button.
     */
    private JButton createQuitButton() {
        JButton quitButton = new JButton("Quit");
        quitButton.setMnemonic('q');
        quitButton.addActionListener(a -> {
            for (Frame frame : Frame.getFrames()) {
                frame.dispose();
            }
        });
        return quitButton;
    }

    /**
     * Retrieve the currently set level from the level chooser.
     *
     * @return The difficulty currently set by the user.
     */
    private int getLevel() {
        return levelChooser.getItemAt(levelChooser.getSelectedIndex());
    }

    /**
     * Immediately abort the board calculation in the move thread if active.
     */
    @SuppressWarnings("deprecation")
    private void clearMoveThread() {
        if (moveThread.isAlive()) {
            moveThread.stop();
        }
    }

    /**
     * Start a new Reversi game with a certain initial player.
     *
     * @param firstPlayer The player to have the first turn.
     * @param level       The difficulty level for the new game.
     */
    private void restartGame(Player firstPlayer, int level) {
        clearMoveThread();
        boardManager.newGame(firstPlayer, level);
        if (firstPlayer == Player.MACHINE) {
            moveThread = new Thread(this::processMachineMoves);
            moveThread.start();
        }
    }

    /**
     * Attempt a human move in the indicated position and afterwards as many
     * machine moves as necessary.
     *
     * @param row The row number of the desired move.
     * @param col The column number of the desired move.
     */
    private void processHumanMove(int row, int col) {
        if (boardManager.move(row, col)) {
            processMachineMoves();
            checkGameOver();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Automatically execute as many computer moves as necessary.
     */
    private void processMachineMoves() {
        if (boardManager.next() == Player.HUMAN) {
            showMessageDialog("The bot has to miss a turn.");
        } else if (boardManager.next() == Player.MACHINE) {
            boardManager.machineMove();
            while (boardManager.next() == Player.MACHINE) {
                showMessageDialog("You have to miss a turn.");
                boardManager.machineMove();
            }
        }
    }

    /**
     * Check whether the game is over. If so, proclaim the winner.
     */
    private void checkGameOver() {
        if (boardManager.gameOver()) {
            Player winner = boardManager.getWinner();
            if (winner == Player.HUMAN) {
                showMessageDialog("You have won!");
            } else if (winner == Player.MACHINE) {
                showMessageDialog("The bot has won.");
            } else {
                showMessageDialog("Tie game!");
            }
        }
    }

    /**
     * Update the GUI in reaction to a notification from the board manager.
     *
     * @param o The observable responsible for the update. (not needed here)
     */
    @Override
    public void update(Observable o) {
        SwingUtilities.invokeLater(() -> {
            updateGridTiles();
            updatePlayerScores();
            updateUndoAvailability();
        });
    }

    /**
     * Update all tiles on the game grid. Note that this is necessary as the
     * board interface does not provide functionality for querying only the
     * tiles that have actually changed due to a model update.
     */
    private void updateGridTiles() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Player slotOccupant = boardManager.getSlot(row, col);
                slotPanels[row][col].setOccupant(slotOccupant);
            }
        }
    }

    /**
     * Update the player score values.
     */
    private void updatePlayerScores() {
        int humanTiles = boardManager.getNumberOfHumanTiles();
        humanScore.setText(String.valueOf(humanTiles));

        int machineTiles = boardManager.getNumberOfMachineTiles();
        machineScore.setText(String.valueOf(machineTiles));
    }

    /**
     * Update the availability status of the undo button.
     */
    private void updateUndoAvailability() {
        undoButton.setEnabled(boardManager.undoPossible());
    }

    /**
     * Display a dialog on the screen with the given message.
     *
     * @param message The message to be displayed.
     */
    private void showMessageDialog(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                message, WINDOW_TITLE, JOptionPane.INFORMATION_MESSAGE));
    }
}
