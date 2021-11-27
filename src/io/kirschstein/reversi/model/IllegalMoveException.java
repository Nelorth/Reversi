package io.kirschstein.reversi.model;

/**
 * A runtime exception indicating an illegal game move.
 */
class IllegalMoveException extends RuntimeException {

    /**
     * Class version for ensuring serialization compatibility.
     */
    private static final long serialVersionUID = -2160498196863852771L;

    /**
     * Construct a new {@code IllegalMoveException} without a message.
     */
    IllegalMoveException() {
        super();
    }

    /**
     * Construct a new {@code IllegalMoveException} with a given message.
     *
     * @param message A detailed exception message.
     */
    IllegalMoveException(String message) {
        super(message);
    }
}
