package io.kirschstein.reversi;

import io.kirschstein.reversi.gui.ReversiFrame;

import javax.swing.SwingUtilities;

public class Main {

    /**
     * The program entry point.
     *
     * @param args Command line arguments. They will not be processed.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ReversiFrame::new);
    }
}
