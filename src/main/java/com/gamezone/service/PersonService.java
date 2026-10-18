package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonRepository;

import java.io.IOException;
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
 * Every rejected operation throws an {@link IllegalArgumentException} whose
 * message is written in Spanish, because the console menu shows it directly to
 * the user. The validations live in this layer and not in the domain classes on
 * purpose: deciding whether a customer is valid requires information the
 * customer object does not have, such as the identifications already in use.
 * <p>
 * The service has no knowledge of the sales module. The purchase history of a
 * customer is derived information and is answered by {@code SaleService}.
 *
 * @author Jose Cervantes
 * @since 1.0.0
 */
public class PersonService {

    /** Pattern used to check that an e-mail address has a valid format. */
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /** Repository used to persist and recover the people. */
    private final PersonRepository repository;

    /** Customers currently registered in the store. */
    private final List<Customer> customers = new ArrayList<>();

    /** Sellers currently registered in the store. */
    private final List<Seller> sellers = new ArrayList<>();

    /**
     * Builds the service and loads the people previously stored.
     *
     * @param repository repository in charge of the persistence
     * @throws IOException if the customers or the sellers file cannot be read
     */
    public PersonService(PersonRepository repository) throws IOException {
        this.repository = repository;
        this.customers.addAll(repository.loadCustomers());
        this.sellers.addAll(repository.loadSellers());
    }

    /**
     * Registers a new customer, validates its information, adds it to the store
     * and persists the change.
     * <p>
     * Customers are the only people that are registered from the console menu.
     * Sellers come from the preload file and are not registered here.
     *
     * @param id    identification of the customer
     * @param name  full name of the customer
     * @param phone contact phone of the customer, digits only
     * @param email e-mail address of the customer
     * @return the customer that was registered
     * @throws IllegalArgumentException if any value is empty, if the phone
     *                                  contains a character that is not a
     *                                  digit, if the e-mail address has an
     *                                  invalid format, or if the identification
     *                                  is already in use
     * @throws IOException              if the customers file cannot be written
     */
    public Customer registerCustomer(String id, String name, String phone, String email)
            throws IOException {
        String validId = requireText(id, "La identificación del cliente no puede estar vacía.");
        String validName = requireText(name, "El nombre del cliente no puede estar vacío.");
        String validPhone = requirePhone(phone);
        String validEmail = requireEmail(email);

        if (findPersonById(validId) != null) {
            throw new IllegalArgumentException(
                    "Ya existe una persona registrada con la identificación " + validId + ".");
        }

        Customer customer = new Customer(validId, validName, validPhone, validEmail);
        customers.add(customer);
        repository.saveCustomers(customers);
        return customer;
    }

    /**
     * Returns every customer currently registered.
     *
     * @return a copy of the list of registered customers
     */
    public List<Customer> listCustomers() {
        return new ArrayList<>(customers);
    }

    /**
     * Returns every seller currently registered.
     *
     * @return a copy of the list of registered sellers
     */
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

    /**
     * Searches a person of any role by its identification. It is used to verify
     * that a new identification is not already in use.
     *
     * @param id identification to search
     * @return the person found, or null when the identification is free
     */
    private Person findPersonById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String searched = id.trim();
        for (Customer customer : customers) {
            if (customer.getId().equals(searched)) {
                return customer;
            }
        }
        return findSellerById(searched);
    }

    /**
     * Checks that a required text value is neither null nor blank.
     *
     * @param value   value to check
     * @param message message used when the value is not valid
     * @return the trimmed value
     * @throws IllegalArgumentException if the value is null or blank
     */
    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    /**
     * Checks that a contact phone is written with digits only.
     * <p>
     * Every character is compared against the ASCII range {@code '0'} to
     * {@code '9'} instead of using {@link Character#isDigit(char)}, because that
     * method also accepts digits of other writing systems, which are not valid
     * in a phone number of the store.
     *
     * @param phone value to check
     * @return the trimmed value
     * @throws IllegalArgumentException if the value is null, blank or contains
     *                                  a character that is not a digit
     */
    private String requirePhone(String phone) {
        String value = requireText(phone, "El teléfono del cliente no puede estar vacío.");
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character < '0' || character > '9') {
                throw new IllegalArgumentException(
                        "El teléfono del cliente solo puede contener números: " + value);
            }
        }
        return value;
    }

    /**
     * Checks that an e-mail address has a valid format.
     *
     * @param email value to check
     * @return the trimmed value
     * @throws IllegalArgumentException if the value is null, blank or does not
     *                                  follow the format of an e-mail address
     */
    private String requireEmail(String email) {
        String value = requireText(email, "El correo electrónico del cliente no puede estar vacío.");
        if (!value.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException(
                    "El correo electrónico " + value + " no tiene un formato válido.");
        }
        return value;
    }
}