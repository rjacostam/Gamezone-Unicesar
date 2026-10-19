package com.gamezone.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Reads and checks the values typed by the user in the console.
 * <p>
 * Every submenu reads its data through one shared instance of this class, so the rules for
 * required fields, numbers and dates are written once. The messages shown when a value is
 * wrong are in Spanish, like the rest of the menu.
 */
public class ConsoleInput {

    private final Scanner scanner;

    /**
     * Creates the reader over the standard input.
     */
    public ConsoleInput() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Reads a required text. An empty line is asked again.
     *
     * @param prompt text shown before reading
     * @return the trimmed text, or an empty text when there is no more input
     */
    public String readText(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value == null) {
                return "";
            }
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Este campo es obligatorio.");
        }
    }

    /**
     * Reads an optional text. An empty line is accepted.
     *
     * @param prompt text shown before reading
     * @return the trimmed text, empty when the user only pressed Enter
     */
    public String readOptionalText(String prompt) {
        String value = readLine(prompt);
        return value == null ? "" : value;
    }

    /**
     * Reads a whole number, asking again until the value is valid.
     *
     * @param prompt text shown before reading
     * @return the number, or {@code 0} when there is no more input
     */
    public int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value == null) {
                return 0;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número entero.");
            }
        }
    }

    /**
     * Reads a decimal number. A comma is accepted as decimal separator.
     *
     * @param prompt text shown before reading
     * @return the number, or {@code 0} when there is no more input
     */
    public double readDouble(String prompt) {
        while (true) {
            String value = readText(prompt);
            if (value.isEmpty()) {
                return 0;
            }
            try {
                return Double.parseDouble(value.replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número válido.");
            }
        }
    }

    /**
     * Reads a yes or no answer.
     *
     * @param prompt text shown before reading
     * @return true when the user answered "s", "si" or "sí"
     */
    public boolean readYesNo(String prompt) {
        String value = readOptionalText(prompt);
        return value.equalsIgnoreCase("s") || value.equalsIgnoreCase("si") || value.equalsIgnoreCase("sí");
    }

    /**
     * Reads a date in the format {@code AAAA-MM-DD}, asking again until it is valid.
     *
     * @param prompt text shown before reading
     * @return the date, or {@code null} when there is no more input
     */
    public LocalDate readDate(String prompt) {
        while (true) {
            String value = readText(prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                System.out.println("La fecha debe tener el formato AAAA-MM-DD.");
            }
        }
    }

    /**
     * Shows the prompt and reads one line.
     *
     * @param prompt text shown before reading
     * @return the trimmed line, or {@code null} when there is no more input
     */
    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }
}
