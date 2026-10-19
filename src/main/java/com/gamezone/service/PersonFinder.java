package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;

import java.util.List;

/**
 * Contract of the people module that a sale needs.
 *
 * <p>Part of the correction of violation V-11: {@code SaleService} asked the
 * whole {@code PersonService} for three operations, so it depended on the list
 * of sellers and on the registration of a person, which a sale never does. This
 * contract has only the three it uses.
 */
public interface PersonFinder {

    /**
     * Returns every customer registered, in registration order.
     *
     * @return the customers currently registered
     */
    List<Customer> listCustomers();

    /**
     * Searches a seller by identifier or by employee code.
     *
     * @param idOrEmployeeCode identifier or employee code to look for
     * @return the seller found
     */
    Seller findSellerById(String idOrEmployeeCode);

    /**
     * Searches a customer by identifier.
     *
     * @param id identifier to look for
     * @return the customer found, or {@code null} when no customer uses it
     */
    Customer findCustomerById(String id);
}