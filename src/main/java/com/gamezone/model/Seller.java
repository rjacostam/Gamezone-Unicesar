package com.gamezone.model;

/**
 * Concrete class that represents a seller of the GameZone Unicesar store.
 * <p>
 * A seller is the employee who attends the customers and registers the sales.
 * On top of the information inherited from {@link Person}, a seller owns the
 * employee code that identifies him inside the company and the shift he is
 * assigned to.
 * <p>
 * Sellers belong to the preload data of the system: they are already hired and
 * working in the store, so they are not registered through the console menu but
 * loaded from {@code data/sellers.csv} when the application starts.
 * <p>
 * Because of that, this class has no registration method in
 * {@code PersonService}. The only way to add a seller is to include it in the
 * preload file, and both values that identify a seller, the identification and
 * the employee code, are accepted by
 * {@code PersonService.findSellerById(String)}.
 *
 * @author Jose Cervantes
 * @since 1.0.0
 */
public class Seller extends Person {

    /** Employee code that identifies the seller inside the company. */
    private String employeeCode;

    /** Shift assigned to the seller. */
    private String shift;

    /**
     * Builds a seller with the information shared by every person plus the
     * attributes that belong to this role.
     *
     * @param id           identification of the seller
     * @param name         full name of the seller
     * @param phone        contact phone of the seller
     * @param employeeCode employee code of the seller
     * @param shift        shift assigned to the seller
     */
    public Seller(String id, String name, String phone, String employeeCode, String shift) {
        super(id, name, phone);
        this.employeeCode = employeeCode;
        this.shift = shift;
    }

    /**
     * Returns the role played by this person inside the store.
     *
     * @return {@code Vendedor}
     */
    @Override
    public String getRole() {
        return "Vendedor";
    }

    /**
     * Returns the description line of the seller, prefixed with its role and
     * adding the employee code and the shift to the information shared by every
     * person.
     *
     * @return the description line of the seller
     */
    @Override
    public String getSummary() {
        return "[" + getRole() + "] " + getId() + " | " + getName()
                + " | " + getPhone() + " | " + employeeCode + " | " + shift;
    }

    /**
     * Returns the employee code of the seller.
     * <p>
     * Together with the identification, this value identifies the seller, so
     * {@code PersonService.findSellerById(String)} accepts either of the two.
     *
     * @return employee code of the seller
     */
    public String getEmployeeCode() {
        return employeeCode;
    }

    /**
     * Sets the employee code of the seller.
     *
     * @param employeeCode employee code of the seller
     */
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    /**
     * Returns the shift assigned to the seller.
     *
     * @return shift assigned to the seller
     */
    public String getShift() {
        return shift;
    }

    /**
     * Sets the shift assigned to the seller.
     *
     * @param shift shift assigned to the seller
     */
    public void setShift(String shift) {
        this.shift = shift;
    }
}