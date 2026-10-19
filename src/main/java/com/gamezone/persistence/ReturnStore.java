package com.gamezone.persistence;

import com.gamezone.model.Return;
import com.gamezone.model.Sale;

import java.util.List;

/**
 * Contract of the returns file that {@code ReturnService} needs.
 *
 * <p>Part of the correction of violation V-11: the service depended on the whole
 * {@code ReturnRepository}, a concrete class of the persistence package. This
 * contract has the two operations the service uses, and it is declared in the
 * service package so the dependency points at the service instead of at the
 * storage.
 */
public interface ReturnStore {

    /**
     * Writes every return in the file.
     *
     * @param returns returns to write
     * @throws com.gamezone.exceptions.PersistenceException if the file cannot be written
     */
    void saveAll(List<Return> returns);

    /**
     * Reads every return stored in the file, linked to the sales it belongs to.
     *
     * @param sales sales the returns can be linked to
     * @return the stored returns, empty when the file does not exist yet
     * @throws com.gamezone.exceptions.PersistenceException if the file cannot be read
     */
    List<Return> loadAll(List<Sale> sales);
}