package com.gamezone.model;

/**
 * Cliente que compra en la tienda.
 */
public class Customer extends Person {
    private String email;

    /**
     * Crea un cliente nuevo.
     * @param name nombre completo
     * @param nationalId identification
     * @param phone phone
     * @param email email address
     */
    public Customer(String name, String nationalId, String phone, String email) {
        super(name, nationalId, phone);
        this.email = email;
    }

    /** @return email */
    public String getEmail() { return email; }
    /** @param email email address */
    public void setEmail(String email) { this.email = email; }

    /**
     * Role label for customers.
     * @return role label
     */
    @Override
    public String getRoleLabel() {
        return "Customer: " + getName() + " <" + email + ">";
    }
}
