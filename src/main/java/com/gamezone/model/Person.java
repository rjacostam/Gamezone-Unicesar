package com.gamezone.model;

/**
 * Base class for people interacting with the store.
 * Holds common contact data. Abstract because only concrete roles exist.
 */
public abstract class Person {
    private String name;
    private String nationalId;
    private String phone;

    /**
     * Creates a person.
     * @param name full name
     * @param nationalId identification number
     * @param phone contact phone
     */
    public Person(String name, String nationalId, String phone) {
        this.name = name;
        this.nationalId = nationalId;
        this.phone = phone;
    }

    /** @return full name */
    public String getName() { return name; }
    /** @param name full name */
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
