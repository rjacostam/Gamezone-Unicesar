package com.gamezone.model;

/**
 * Clase base de la gente de la tienda.
 * Guarda datos comunes. Es abstracta porque solo hay roles concretos.
 */
public abstract class Person {
    private String name;
    private String nationalId;
    private String phone;

    /**
     * Crea una persona con datos basicos.
     * @param name nombre completo
     * @param nationalId identification number
     * @param phone contact phone
     */
    public Person(String name, String nationalId, String phone) {
        this.name = name;
        this.nationalId = nationalId;
        this.phone = phone;
    }

    /** @return nombre completo */
    public String getName() { return name; }
    /** @param name nombre completo */
    public void setName(String name) { this.name = name; }
    /** @return identification */
    public String getNationalId() { return nationalId; }
    /** @return phone */
    public String getPhone() { return phone; }
    /** @param phone contact phone */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Short role label implemented by subclasses.
     * @return role description
     */
    public abstract String getRoleLabel();
}
