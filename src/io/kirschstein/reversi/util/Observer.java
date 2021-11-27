package io.kirschstein.reversi.util;

/**
 * An abstract observer in the Observer Pattern.
 */
public interface Observer {
    /**
     * Perform appropriate updates based on notification by an observable.
     *
     * @param o The observable responsible for the update.
     */
    void update(Observable o);
}
