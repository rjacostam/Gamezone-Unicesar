package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Customer;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonRepository;
import com.gamezone.validation.PersonValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Service in charge of the business rules of the people module.
 * <p>
 * This class is the only one of the module allowed to invoke
 * {@link PersonRepository}, which guarantees that the user interface never
 * touches the persistence layer directly.
 * <p>
 * The data is loaded once when the service is built and it is persisted again
 * after every change, so the information survives between executions.
 * <p>
 * Every rejected operation throws one of the typed exceptions of the project:
 * {@link InvalidDataException} when a value is empty or malformed, and
 * {@link BusinessRuleException} when the identification is already in use. Both build
 * their message in Spanish, because the console menu shows it directly to the
 * user. The rules themselves live in {@link PersonValidator}.
 * <ul>
 *   <li>the identification, the name, the phone and the e-mail cannot be empty;</li>
 *   <li>the phone can only contain digits;</li>
 *   <li>the e-mail has to follow the format of an e-mail address;</li>
 *   <li>the identification cannot be in use by another person of any role.</li>
 * </ul>
 * All of them live in this layer and not in the domain classes on purpose:
 * deciding whether a customer is valid requires information the customer object
 * does not have, such as the identifications already in use.
 * <p>
 * The service has no knowledge of the sales module. The purchase history of a
 * customer is derived information and is answered by {@code SaleService}.
 *
 * @author Jose Cervantes
 * @since 1.0.0
 */
public class PersonService implements PersonFinder {

        /** Repository used to persist and recover the people. */
    private final PersonRepository repository;

    /** Customers currently registered in the store. */
    private final List<Customer> customers = new ArrayList<>();

    /** Sellers currently registered in the store. */
    private final List<Seller> sellers = new ArrayList<>();

    /**
 * Builds the service and loads the people previously stored.
 * <p>
 * The exception is declared because the data is read from disk here, before the
 * menu starts. Whoever builds the service, normally {@code Main}, has to handle
 * it.
 *
 * @param repository repository in charge of the persistence
 * @throws PersistenceException if the customers or the sellers file cannot be read
 */
    public PersonService(PersonRepository repository) {
        this.repository = repository;
        this.customers.addAll(repository.loadCustomers());
        this.sellers.addAll(repository.loadSellers());
    }

    /**
     * Registers a new customer, validates its information, persists it and adds
     * it to the store.
     * <p>
     * The customer is written to the file **before** it joins the list held in
     * memory. If the write fails the exception leaves the service and the customer
     * is not registered anywhere, instead of staying in memory while the file
     * knows nothing about it.
     * <p>
     * Customers are the only people that are registered from the console menu.
     * Sellers come from the preload file and are not registered here.
     *
     * @param id    identification of the customer
     * @param name  full name of the customer
     * @param phone contact phone of the customer, digits only
     * @param email e-mail address of the customer
     * @return the customer that was registered
     * @throws InvalidDataException  if any value is empty, if the phone
     *                              contains a character that is not a digit, or
     *                              if the e-mail address has an invalid format
     * @throws BusinessRuleException if the identification is already in use
     * @throws PersistenceException  if the customers file cannot be written
     */
    public Customer registerCustomer(String id, String name, String phone, String email) {
        PersonValidator.validatePersonData(id, name, email, phone);
        String validId = id.trim();
        PersonValidator.validateUniquePerson(people(), validId);

        Customer customer = new Customer(validId, name.trim(), phone.trim(), email.trim());
        repository.saveCustomers(withCustomer(customer));
        customers.add(customer);
        return customer;
    }

    /**
     * Returns every customer, the ones already registered and the one that is
     * about to be registered, so the file receives the whole list at once.
     *
     * @param customer customer that is being registered
     * @return list with the new customer already in it
     */
    private List<Customer> withCustomer(Customer customer) {
        List<Customer> pending = new ArrayList<>(customers);
        pending.add(customer);
        return pending;
    }

    /**
     * Returns every person of the store, customers and sellers, which is the
     * population the identification check works on.
     *
     * @return list of the people held in memory
     */
    private List<Person> people() {
        List<Person> all = new ArrayList<>(customers);
        all.addAll(sellers);
        return all;
    }

    /**
     * Returns every customer currently registered.
     *
     * @return a copy of the list of registered customers
     */
    @Override
    public List<Customer> listCustomers() {
        return new ArrayList<>(customers);
    }

    /**
     * Returns every seller currently registered.
     *
     * @return a copy of the list of registered sellers
     */
    @Override
    public List<Seller> listSellers() {
        return new ArrayList<>(sellers);
    }

    /**
     * Searches a customer by its identification.
     *
     * @param id identification to search
     * @return the customer found, or null when no customer uses that
     *         identification
     */
    @Override
    public Customer findCustomerById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String searched = id.trim();
        for (Customer customer : customers) {
            if (customer.getId().equals(searched)) {
                return customer;
            }
        }
        return null;
    }

    /**
     * Searches a seller by its identification or by its employee code, because
     * both values identify a seller in the daily work of the store.
     * <p>
     * The comparison ignores the capitalisation and the surrounding spaces of
     * the searched value, so the menu accepts what the user types without
     * forcing an exact capitalisation.
     *
     * @param idOrEmployeeCode identification or employee code to search
     * @return the seller found, or null when no seller matches
     */
    @Override
    public Seller findSellerById(String idOrEmployeeCode) {
        if (idOrEmployeeCode == null || idOrEmployeeCode.isBlank()) {
            return null;
        }
        String searched = idOrEmployeeCode.trim();
        for (Seller seller : sellers) {
            if (seller.getId().trim().equalsIgnoreCase(searched)
                    || seller.getEmployeeCode().trim().equalsIgnoreCase(searched)) {
                return seller;
            }
        }
        return null;
    }

}
