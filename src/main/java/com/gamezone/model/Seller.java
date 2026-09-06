package com.gamezone.model;

/**
 * Vendedor que atiende ventas.
 */
public class Seller extends Person {
    private String employeeCode;
    private String shift;

    /**
     * Crea un vendedor nuevo.
     * @param name nombre completo
     * @param nationalId identification
     * @param phone phone
     * @param employeeCode employee code
     * @param shift work shift
     */
    public Seller(String name, String nationalId, String phone, String employeeCode, String shift) {
        super(name, nationalId, phone);
        this.employeeCode = employeeCode;
        this.shift = shift;
    }

    /** @return employee code */
    public String getEmployeeCode() { return employeeCode; }
    /** @return work shift */
    public String getShift() { return shift; }
    /** @param shift work shift */
    public void setShift(String shift) { this.shift = shift; }

    /**
     * Role label for sellers.
     * @return role label
     */
    @Override
    public String getRoleLabel() {
        return "Seller: " + getName() + " [" + employeeCode + ", " + shift + "]";
    }
}
