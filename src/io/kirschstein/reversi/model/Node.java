package io.kirschstein.reversi.model;

import io.kirschstein.reversi.util.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A node in the game tree constructed for determining the best machine move.
 * See {@link ArrayBoard#generateGameTree(ArrayBoard, Position, int)}.
 */
final class Node implements Comparable<Node> {

    /**
     * The game move assigned to this node.
     */
    private final Position move;

    /**
     * The score assigned to this node.
     */
    private final double score;

    /**
     * The children of this node.
     */
    private final List<Node> children;

    /**
     * Construct a node from a given move and score.
     *
     * @param move  The move to assign to the node.
     * @param score The score to assign to the node.
     */
    Node(Position move, double score) {
        this(move, score, new ArrayList<>());
    }

    /**
     * Construct a node from a given move, score and list of children.
     *
     * @param move     The move to assign to the node.
     * @param score    The score to assign to the node.
     * @param children The children of the node.
     */
    Node(Position move, double score, List<Node> children) {
        this.move = move;
        this.score = score;
        this.children = children;
    }

    /**
     * Get the move assigned to this node.
     *
     * @return The move.
     */
    Position getMove() {
        return move;
    }

    /**
     * Get the score assigned to this node.
     *
     * @return The score.
     */
    double getScore() {
        return score;
    }

    /**
     * Get the read-only children of this node.
     *
     * @return The children.
     */
    List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Compare this node to another node by their score values. Note that this
     * specific natural ordering is inconsistent with equals.
     *
     * @param o The other node.
     * @return A negative integer, zero, or a positive integer as this node is
     *         less than, equal to, or greater than the specified node.
     */
    @Override
    public int compareTo(Node o) {
        return Double.compare(score, o.score);
    }

    /**
     * Convert this node into a human-readable string representation.
     *
     * Its format is a multiline tree output similar to a file explorer's
     * visualization of the file system.
     *
     * @return A human-readable string representation of this node.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        stringify(sb, "", true);
        return sb.toString();
    }

    /**
     * Append a human-readable string representation of this node to a given
     * string builder while respecting a given prefix and whether this node is
     * the last child of its parent.
     *
     * @param sb        A string builder.
     * @param prefix    The prefix to be prepended.
     * @param lastChild Whether this node is the last child.
     */
    private void stringify(StringBuilder sb, String prefix, boolean lastChild) {
        sb.append(prefix).append(lastChild ? "└── " : "├── ").append(move)
                .append(": ").append(score).append(System.lineSeparator());

        int lastIndex = children.size() - 1;
        if (!children.isEmpty()) {
            String childPrefix = prefix + (lastChild ? "    " : "│   ");
            for (int i = 0; i < lastIndex; i++) {
                children.get(i).stringify(sb, childPrefix, false);
            }
            children.get(lastIndex).stringify(sb, childPrefix, true);
        }
    }
}
