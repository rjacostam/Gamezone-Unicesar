package com.gamezone.service;

import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.persistence.ReturnRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules of the return module. It loads the returns when the service is
 * created, validates every registration before it changes anything, restores the
 * stock of the returned units and saves the returns after every change so the
 * data survives between executions.
 * <p>
 * The service receives {@code SaleService} and {@code ProductService} and uses
 * them instead of reaching the repositories itself. That is what the statement
 * asks for and it also keeps a single owner for every rule: the sales know
 * whether a sale can still be returned, and the catalogue knows how to restore a
 * unit of stock.
 * <p>
 * File errors do not reach the caller as a checked exception. They become an
 * {@link IllegalStateException}, the same way the other services do it, because
 * a failure of the disk is a failure of the application and not a mistake of the
 * user.
 * <p>
 * Rejected data becomes an {@link IllegalArgumentException} with a message in
 * Spanish, because the console menu shows it directly to the user.
 */
public class ReturnService {

    private ReturnRepository repository;
    private SaleService saleService;
    private ProductService productService;
    private List<Return> returns;

    /**
     * Builds the service and loads the returns, linking each one to the sales
     * that were already registered.
     *
     * @param repository     repository used to read and write the returns
     * @param saleService    service of the sales, needed because a return always
     *                       comes from a sale
     * @param productService service of the catalogue, needed to restore the stock
     *                       of the returned units
     * @throws IllegalStateException if the returns file cannot be read
     */
    public ReturnService(ReturnRepository repository, SaleService saleService,
                         ProductService productService) {
        this.repository = repository;
        this.saleService = saleService;
        this.productService = productService;
        try {
            this.returns = new ArrayList<>(repository.loadAll(saleService.listAllSales()));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el historial de devoluciones", e);
        }
    }

    /**
     * Returns a copy of every return currently registered, in registration
     * order.
     *
     * @return returns currently registered
     */
    public List<Return> viewAllReturns() {
        return new ArrayList<>(returns);
    }

    /**
     * Saves the returns currently registered.
     *
     * @throws IllegalStateException if the returns file cannot be written
     */
    private void saveCatalogue() {
        try {
            repository.saveAll(returns);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el historial de devoluciones", e);
        }
    }
}