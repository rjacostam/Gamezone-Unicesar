package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Person;
import java.util.List;

/**
 * Checks the data and the rules of the people of the store before they are
 * registered. Every method is static: a validator holds no state and is never
 * instantiated, so it cannot be created from outside this class.
 */
public final class PersonValidator {

    /**
     * Hides the constructor because the class only offers static methods.
     */
    private PersonValidator() {
    }

    /**
     * Checks the common data of a person before it is registered. The phone must
     * have only digits and the email must have an at sign and a dot.
     * @param id person identifier
     * @param name full name
     * @param email email address
     * @param phone contact phone
     * @throws InvalidDataException if a required field is empty, if the phone has
     *                              something other than digits or if the email
     *                              does not have an at sign and a dot
     */
    public static void validatePersonData(String id, String name, String email, String phone) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("identificador", "es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("nombre", "es obligatorio");
        }
        if (phone == null || phone.isBlank()) {
            throw new InvalidDataException("telefono", "es obligatorio");
        }
        if (!phone.chars().allMatch(Character::isDigit)) {
            throw new InvalidDataException("telefono", "solo puede contener digitos");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidDataException("correo", "es obligatorio");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new InvalidDataException("correo", "no tiene un formato valido");
        }
    }

    /**
     * Checks that a person was found by its identifier.
     * @param person person found, or {@code null} when it does not exist
     * @param personId identifier that was searched
     * @throws ResourceNotFoundException if the person is {@code null}
     */
    public static void validatePersonExists(Person person, String personId) {
        if (person == null) {
            throw new ResourceNotFoundException("la persona", personId);
        }
    }

    /**
     * Checks that the identifier is not used by a person already registered.
     * @param existing people already registered
     * @param newId identifier being registered
     * @throws BusinessRuleException if another person already uses that identifier
     */
    public static void validateUniquePerson(List<? extends Person> existing, String newId) {
        if (existing == null) {
            return;
        }
        for (Person person : existing) {
            if (person.getId().equalsIgnoreCase(newId)) {
                throw new BusinessRuleException(
                        "Ya existe una persona con el identificador " + newId + ".");
            }
        }
    }
}
