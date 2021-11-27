package io.kirschstein.reversi.gui;

import io.kirschstein.reversi.model.Player;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A single slot on a Reversi grid that paints itself.
 */
public class SlotPanel extends JPanel {

    /**
     * Class version for ensuring serialization compatibility.
     */
    private static final long serialVersionUID = 6313495752362958215L;

    /**
     * The initial size of a slot.
     */
    private static final int SLOT_SIZE = 48;

    /**
     * The padding between border and content inside the slot.
     */
    private static final int SLOT_PADDING = 4;

    /**
     * The player occupying the slot.
     */
    private Player occupant;

    /**
     * Construct a new slot panel with a given occupant.
     *
     * @param occupant The player holding the slot.
     */
    SlotPanel(Player occupant) {
        this.occupant = occupant;
        setBackground(Color.GREEN);
        setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
    }

    /**
     * Set the player currently occupying this slot and render if the occupant
     * has changed.
     *
     * @param occupant The player holding the slot.
     */
    void setOccupant(Player occupant) {
        if (occupant != this.occupant) {
            this.occupant = occupant;
            repaint();
        }
    }

    /**
     * Paint the content of this slot to the given canvas.
     *
     * @param g The grapics context.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // smooth curves ;)

        if (occupant != null) {
            paintTile(g2);
        }
    }

    /**
     * Paint a circular tile in the center of the slot with the color associated
     * with the currently occupying player.
     *
     * @param g2 The graphics context.
     */
    private void paintTile(Graphics2D g2) {
        int slotWidth = getWidth();
        int slotHeight = getHeight();
        int diameter = Math.min(slotWidth, slotHeight) - 2 * SLOT_PADDING;
        int offsetX = Math.max(slotWidth - slotHeight, 0) / 2;
        int offsetY = Math.max(slotHeight - slotWidth, 0) / 2;
        int topLeftX = SLOT_PADDING + offsetX;
        int topLeftY = SLOT_PADDING + offsetY;

        g2.setColor(ReversiFrame.colorOf(occupant));
        g2.fillOval(topLeftX, topLeftY, diameter, diameter);
    }
}
