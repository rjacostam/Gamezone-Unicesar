package com.gamezone.ui;

import com.gamezone.format.AmountFormat;

import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import java.time.LocalDate;

/**
 * Builds the text of the warranty certificate that the console menu shows to the
 * user.
 *
 * <p>This class exists because the wording and the layout of a document are a
 * responsibility of the presentation, not of the domain. It was registered as
 * violation V-05 of the consolidated GRASP audit, where {@code Warranty} was
 * building a document while it was also holding the coverage rules.
 *
 * <p>The warranty keeps everything it owns: its identifier, its dates, its type,
 * its coverage and its additional cost. This class only asks for those values
 * through the public interface of the warranty and arranges them in the order
 * and with the labels the user reads.
 *
 * <p>This is the only place where the certificate of a warranty is built. The
 * text is the same text {@code Warranty.generateWarrantyCertificate} produced,
 * character by character, so moving the menu over changed nothing the user sees.
 * That method was removed once the menu stopped calling it, and this class took
 * its place.
 *
 * @see ReturnReceiptFormatter
 */
public final class WarrantyCertificateFormatter {

    /**
     * Not instantiable: the class only offers one static answer.
     */
    private WarrantyCertificateFormatter() {
    }

    /**
     * Builds the certificate of a warranty, line by line, exactly as the model
     * used to build it.
     * @param warranty the warranty to describe
     * @return the certificate, ready to be shown to the user
     */
    public static String format(Warranty warranty) {
        boolean active = warranty.isActive(LocalDate.now());
        StringBuilder certificate = new StringBuilder();
        certificate.append("=== GameZone Unicesar - Certificado de garantia ===\n");
        certificate.append("Garantia: ").append(warranty.getId())
                .append(" (").append(warranty.getWarrantyType()).append(")\n");
        Product product = warranty.getProduct();
        if (product != null) {
            certificate.append("Producto: ").append(product.getId())
                    .append(" - ").append(product.getTitle()).append("\n");
        }
        Sale sale = warranty.getSale();
        if (sale != null) {
            certificate.append("Venta: ").append(sale.getId()).append("\n");
            if (sale.getCustomer() != null) {
                certificate.append("Cliente: ").append(sale.getCustomer().getName()).append("\n");
            }
        }
        certificate.append("Inicio: ").append(warranty.getStartDate())
                .append(" | Fin: ").append(warranty.getEndDate()).append("\n");
        certificate.append("Cobertura: ").append(warranty.coverageDescription()).append("\n");
        certificate.append("Costo adicional: ")
                .append(AmountFormat.money(warranty.getAdditionalCost())).append("\n");
        certificate.append("Estado: ")
                .append(active ? "Vigente" : "No vigente").append("\n");
        return certificate.toString();
    }
}
