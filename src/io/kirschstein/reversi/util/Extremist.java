package io.kirschstein.reversi.util;

import java.util.List;

/**
 * Utility class for finding certain extrema of lists in a custom fashion.
 */
public final class Extremist {

    /**
     * Suppress default constructor to ensure non-instantiability.
     */
    private Extremist() {
        // empty
    }

    /**
     * Determine the first minimum of a list, i.e. if there are multiple minima,
     * return the first in terms of list order.
     *
     * @param list The concerned list.
     * @param <T>  The type of the list elements.
     * @return The first minimum of {@code list}.
     */
    public static <T extends Comparable<T>> T firstMin(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("An empty list has no minimum.");
        }
        T min = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            T item = list.get(i);
            if (item.compareTo(min) < 0) {
                min = item;
            }
        }
        return min;
    }

    /**
     * Determine the first maximum of a list, i.e. if there are multiple maxima,
     * return the first in terms of list order.
     *
     * @param list The concerned list.
     * @param <T>  The type of the list elements.
     * @return The first maximum of {@code list}.
     */
    public static <T extends Comparable<T>> T firstMax(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("An empty list has no maximum.");
        }
        T max = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            T item = list.get(i);
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }
}
