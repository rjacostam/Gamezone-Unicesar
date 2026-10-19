package com.gamezone.format;

import java.util.Locale;

/**
 * The single place where the store decides how an amount and a percentage are
 * written for the user.
 *
 * <p>This class was created by the correction of violation V-02 of the SOLID
 * audit, which found the money and percentage formats copied in ten places
 * across three packages, with no owner. The class holds no state and depends on
 * nothing inside the project, so any layer can use it, including
 * {@code com.gamezone.model}, which must not depend on {@code com.gamezone.ui}.
 *
 * <p><strong>Two locales are offered on purpose.</strong> Ten of the call sites
 * pinned the format with {@code Locale.US}, and four of them, the whole of
 * {@code ReceiptGenerator} plus the descriptions of {@code VideoGame} and
 * {@code Console}, used the locale of the computer instead. That difference is
 * registered as out of scope finding OOS-02, because forcing {@code Locale.US}
 * on those places would change what the user sees. Because of that, this class
 * offers both variants and every call site keeps the one it was already using:
 * the correction moves the decision here without moving it.
 *
 * <p>The methods return the same characters the inline formats returned, so a
 * call site can be changed without its output changing.
 *
 * @see #money(double)
 * @see #moneyInComputerLocale(double)
 */
public final class AmountFormat {

    /** The format of an amount, pinned to {@code Locale.US}. */
    private static final String MONEY_US = "$%,.2f";

    /** The same format of an amount, using the locale of the computer. */
    private static final String MONEY_COMPUTER = "$%,.2f";

    /** The format of a percentage, pinned to {@code Locale.US}. */
    private static final String PERCENT_US = "%.2f%%";

    /** The same format of a percentage, using the locale of the computer. */
    private static final String PERCENT_COMPUTER = "%.2f%%";

    /**
     * Not instantiable: the class only offers static answers and holds no state.
     */
    private AmountFormat() {
    }

    /**
     * Writes an amount the way the store writes it everywhere else: two decimals,
     * thousands separated with commas and the dollar sign first, regardless of the
     * locale of the computer.
     *
     * @param amount amount to write
     * @return the amount ready to be shown to the user
     */
    public static String money(double amount) {
        return String.format(Locale.US, MONEY_US, amount);
    }

    /**
     * Writes a discount, which is the amount already negative, with the minus sign
     * in front of the dollar sign.
     *
     * @param amount amount taken away from the list price
     * @return the discount ready to be shown to the user
     */
    public static String negativeMoney(double amount) {
        return String.format(Locale.US, "-" + MONEY_US, amount);
    }

    /**
     * Writes a percentage with two decimals and the percent sign, pinned to
     * {@code Locale.US} so every promotion is listed the same way.
     *
     * @param value percentage to write
     * @return the percentage ready to be shown to the user
     */
    public static String percent(double value) {
        return String.format(Locale.US, PERCENT_US, value);
    }

    /**
     * Writes an amount using the locale of the computer, which is what the
     * descriptions of a video game and of a console and the sale receipt already
     * did. It is kept because removing it would change what the user reads; see
     * out of scope finding OOS-02.
     *
     * @param amount amount to write
     * @return the amount, written with the separators of the locale of the
     *         computer
     */
    public static String moneyInComputerLocale(double amount) {
        return String.format(MONEY_COMPUTER, amount);
    }

    /**
     * Writes a percentage using the locale of the computer. Offered so that the
     * computer locale variant exists for every kind of value, even though no
     * call site uses it yet.
     *
     * @param value percentage to write
     * @return the percentage, written with the separators of the locale of the
     *         computer
     */
    public static String percentInComputerLocale(double value) {
        return String.format(PERCENT_COMPUTER, value);
    }
}
