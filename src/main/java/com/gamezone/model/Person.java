package com.gamezone.model;

/**
 * Abstract base class that represents any person interacting with the GameZone
 * Unicesar store.
 * <p>
 * This class concentrates the information shared by every participant of the
 * store: the identification, the name and the contact phone. The attributes that
 * belong to a specific role are declared by the concrete subclasses
 * {@link Customer} and {@link Seller}.
 * <p>
 * The class is declared {@code abstract} because the store never holds a person
 * without a role. The compiler therefore forbids {@code new Person(...)}, and
 * the two abstract methods {@link #getRole()} and {@link #getSummary()} force
 * every existing person to be either a customer or a seller.
 * <p>
 * The identification is the only attribute without a setter: it identifies the
 * person in the store, it is the value used by the sales and by the persistence
 * files, and changing it would break those references.
 * <p>
 * This class performs no validation and no file access: validation belongs to
 * the service layer, which owns the business rules and produces the messages
 * shown to the user. The setters therefore store the value they receive.
 *
 * @author Jose Cervantes
 * @since 1.0.0
 */
public abstract class Person {

    /** Identification of the person. It cannot be changed once assigned. */
    private final String id;

    /** Full name of the person. */
    private String name;

    /** Contact phone of the person. */
    private String phone;

    /**
     * Builds a person with the information shared by every role. The constructor
     * is {@code protected} so only the subclasses can build a person.
     *
     * @param id    identification of the person, stored as given
     * @param name  full name of the person, stored as given
     * @param phone contact phone of the person, stored as given
     */
    protected Person(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    /**
     * Returns the role played by this person inside the store.
     * <p>
     * The method is abstract because the role depends entirely on the
     * specialisation. It is also what lets the rest of the system work with a
     * {@code Person} reference without using {@code instanceof}.
     *
     * @return the role of this person, in Spanish because it is shown to the user
     */
    public abstract String getRole();

    /**
     * Returns the line that describes this person when it is listed, including
     * the information of its own role.
     * <p>
     * Each subclass builds its own line, so the menu can show every person with
     * a single call that does not need to know the concrete class.
     *
     * @return the description line of this person
     */
    public abstract String getSummary();

    /**
     * Returns the identification of the person.
     *
     * @return identification of the person
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the full name of the person.
     *
     * @return full name of the person
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the full name of the person.
     *
     * @param name full name of the person
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the contact phone of the person.
     *
     * @return contact phone of the person
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the contact phone of the person.
     *
     * @param phone contact phone of the person
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns a textual representation of the person, including its role. The
     * role is resolved polymorphically through {@link #getRole()}.
     *
     * @return textual representation of the person
     */
    @Override
    public String toString() {
        return getRole() + " [id=" + id + ", nombre=" + name + ", telefono=" + phone + "]";
    }
}