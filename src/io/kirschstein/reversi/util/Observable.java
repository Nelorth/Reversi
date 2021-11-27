package io.kirschstein.reversi.util;

/**
 * An abstract observation subject in the Observer Pattern.
 */
public interface Observable {
    /**
     * Register an observer.
     *
     * @param o The new observer to add.
     */
    void attachObserver(Observer o);

    /**
     * Unregister an observer.
     *
     * @param o The observer to remove.
     */
    void detachObserver(Observer o);

    /**
     * Indicate a change in the internal state by updating all observers.
     */
    void notifyObservers();
}
