package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de negocio de personas.
 */
public class PersonService {
    private PersonRepository repository;
    private List<Person> persons;

    /**
     * Creates service loading persisted data.
     * @param repository person repository
     */
    public PersonService(PersonRepository repository) {
        this.repository = repository;
        this.persons = new ArrayList<>(repository.loadAll());
    }

    /**
     * Registers a customer.
     * @param name name
     * @param nationalId id
     * @param phone phone
     * @param email email
     * @return created customer
     */
    public Customer registerCustomer(String name, String nationalId, String phone, String email) {
        if (findCustomerById(nationalId) != null) {
            throw new IllegalArgumentException("Duplicate customer");
        }
        Customer c = new Customer(name, nationalId, phone, email);
        persons.add(c);
        repository.saveAll(persons);
        return c;
    }

    /**
     * Adds a seller if missing (used for seed data).
     * @param seller seller to ensure
     */
    public void ensureSeller(Seller seller) {
        if (findSellerByCode(seller.getEmployeeCode()) == null) {
            persons.add(seller);
            repository.saveAll(persons);
        }
    }

    /**
     * Lists customers.
     * @return customers
     */
    public List<Customer> findAllCustomers() {
        List<Customer> out = new ArrayList<>();
        for (Person p : persons) {
            if (p instanceof Customer c) {
                out.add(c);
            }
        }
        return out;
    }

    /**
     * Lists sellers.
     * @return sellers
     */
    public List<Seller> findAllSellers() {
        List<Seller> out = new ArrayList<>();
        for (Person p : persons) {
            if (p instanceof Seller s) {
                out.add(s);
            }
        }
        return out;
    }

    /**
     * Finds customer by id.
     * @param nationalId id
     * @return customer or null
     */
    public Customer findCustomerById(String nationalId) {
        for (Person p : persons) {
            if (p instanceof Customer c && c.getNationalId().equalsIgnoreCase(nationalId)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Finds seller by code.
     * @param code employee code
     * @return seller or null
     */
    public Seller findSellerByCode(String code) {
        for (Person p : persons) {
            if (p instanceof Seller s && s.getEmployeeCode().equalsIgnoreCase(code)) {
                return s;
            }
        }
        return null;
    }
}
