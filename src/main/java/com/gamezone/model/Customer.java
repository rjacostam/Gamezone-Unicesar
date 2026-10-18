package com.gamezone.model;

import java.util.Locale;

/**
 * Concrete class that represents a customer of the GameZone Unicesar store.
 * <p>
 * A customer is the person who comes to the store to buy products. On top of
 * the information inherited from {@link Person}, a customer owns an e-mail
 * address used to receive communications about promotions and new arrivals.
 * <p>
 * The purchase history is deliberately not stored as an attribute of this
 * class. It is derived information, obtained from the registered sales through
 * {@code SaleService.findSalesByCustomer}, so the same data is never kept in two
 * places and this class does not depend on {@code Sale}.
 * <p>
 * The e-mail address is the only attribute of the hierarchy that this class
 * normalises by itself, through {@link #setEmail(String)}, because two customers
 * who write the same address with a different capitalisation must end up with
 * the same stored value. Every other validation belongs to
 * {@code PersonService}.
 *
 * @author Jose Cervantes
 * @since 1.0.0
 */
public class Customer extends Person {

    /** E-mail address of the customer. */
    private String email;

    /**
     * Builds a customer with the information shared by every person plus the
     * e-mail address that belongs to this role.
     * <p>
     * The e-mail address is passed to {@link #setEmail(String)}, so it is
     * normalised in the same way no matter how the customer is built.
     *
     * @param id    identification of the customer, stored as given
     * @param name  full name of the customer, stored as given
     * @param phone contact phone of the customer, stored as given
     * @param email e-mail address of the customer, trimmed and lower cased
     */
    public Customer(String id, String name, String phone, String email) {
        super(id, name, phone);
        setEmail(email);
    }

    /**
     * Returns the role played by this person inside the store.
     *
     * @return {@code Cliente}
     */
    @Override
    public String getRole() {
        return "Cliente";
    }

    /**
     * Returns the description line of the customer, prefixed with its role and
     * adding the e-mail address to the information shared by every person.
     *
     * @return the description line of the customer
     */
    @Override
    public String getSummary() {
        return "[" + getRole() + "] " + getId() + " | " + getName()
                + " | " + getPhone() + " | " + email;
    }

    /**
     * Returns the e-mail address of the customer, already normalised by
     * {@link #setEmail(String)}.
     *
     * @return e-mail address of the customer, trimmed and in lower case
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the e-mail address of the customer, normalising it.
     * <p>
     * The value is stored without surrounding spaces and in lower case, so that
     * two customers who wrote the same address with a different capitalisation
     * end up with the same stored value. {@link Locale#ROOT} is used on purpose,
     * because the default locale of the machine would change the result for
     * some letters.
     *
     * @param email e-mail address of the customer, trimmed and lower cased
     */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}