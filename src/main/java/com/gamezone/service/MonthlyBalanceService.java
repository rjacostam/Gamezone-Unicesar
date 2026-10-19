package com.gamezone.service;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.validation.ReturnValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Builds the financial report of a month: what the store took in, what it gave
 * back, and the difference.
 *
 * <p>This class is the correction of violation V-03 of the consolidated SOLID
 * audit. The three methods are the ones that used to live in
 * {@code ReturnService}, copied without any change, so the figures, the
 * grouping by month and the messages are exactly the ones the user saw before.
 *
 * <p>The reason the report has its own service is that it is not knowledge of a
 * return: it reads the sales of the store and the returns of the module, so
 * {@code ReturnService} had two reasons to change, the rules of a return and the
 * accounting of the store. Here the class that builds the report depends only on
 * the two public operations it needs, {@code SaleService.listAllSales} and
 * {@code ReturnService.viewAllReturns}, and never reaches a repository.
 *
 * <p>{@code ReturnService} still answers the same three methods for now, so the
 * menu can be moved to this service before the old ones are deleted.
 */
public class MonthlyBalanceService {

    private final SaleFinder saleService;
    private final ReturnService returnService;

    /**
     * Builds the service with the two collaborators whose public operations it
     * reads.
     *
     * @param saleService   service of the sales, used to list them
     * @param returnService service of the returns, used to list them
     */
    public MonthlyBalanceService(SaleFinder saleService, ReturnService returnService) {
        this.saleService = saleService;
        this.returnService = returnService;
    }

    /**
     * Sums the total of every sale of a month, that is the final amount of the
     * sale with its discount and its warranties already applied.
     * <p>
     * Each sale is grouped by its own date, so a sale made in September and
     * returned in October counts as income in September only.
     *
     * @param month number of the month, from 1 to 12
     * @param year  number of the year
     * @return total of the sales of that month
     * @throws InvalidDataException if the month is not between 1 and 12
     */
    public double calculateMonthlySales(int month, int year) {
        validateMonth(month);
        double total = 0;
        for (Sale sale : saleService.listAllSales()) {
            if (isInMonth(sale.getDate(), month, year)) {
                total += sale.getTotal();
            }
        }
        return total;
    }

    /**
     * Sums the refunded amount of every return of a month, that is the money the
     * store gave back.
     * <p>
     * Each return is grouped by its own date, so the refund of a sale made in a
     * previous month counts in the month the customer handed the product back.
     *
     * @param month number of the month, from 1 to 12
     * @param year  number of the year
     * @return total of the refunds of that month
     * @throws InvalidDataException if the month is not between 1 and 12
     */
    public double calculateMonthlyReturns(int month, int year) {
        validateMonth(month);
        double total = 0;
        for (Return registeredReturn : returnService.viewAllReturns()) {
            if (isInMonth(registeredReturn.getDate(), month, year)) {
                total += registeredReturn.getRefundAmount();
            }
        }
        return total;
    }

    /**
     * Builds the balance of a month: the money the store took in that month minus
     * the money it gave back in that month.
     * <p>
     * The result is {@link #calculateMonthlySales(int, int)} minus
     * {@link #calculateMonthlyReturns(int, int)}, so the net figure and the two
     * figures the report shows next to it can never disagree: all three come from
     * the same two methods.
     *
     * @param month number of the month, from 1 to 12
     * @param year  number of the year
     * @return the income of the month minus the refunds of the month
     * @throws InvalidDataException if the month is not between 1 and 12
     */
    public double generateMonthlyBalance(int month, int year) {
        return calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year);
    }

    /**
     * Checks that a month number is one of the twelve months of a year. The rule
     * and its message are written only once and shared by the three reports of the
     * month.
     *
     * @param month number of the month
     * @throws InvalidDataException if the month is not between 1 and 12
     */
    private void validateMonth(int month) {
        ReturnValidator.validateMonth(month);
    }

    /**
     * Tells whether a date falls on a given month and year.
     *
     * @param date  date to check
     * @param month number of the month
     * @param year  number of the year
     * @return true when the date is on that month of that year
     */
    private boolean isInMonth(LocalDate date, int month, int year) {
        return date != null && date.getMonthValue() == month && date.getYear() == year;
    }
}