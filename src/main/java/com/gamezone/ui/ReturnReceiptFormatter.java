package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import java.util.Locale;

/**
 * Builds the text of the return receipt that the console menu shows to the user.
 *
 * <p>This class exists because the wording and the layout of a document are a
 * responsibility of the presentation, not of the domain. It was registered as
 * violation V-05 of the consolidated GRASP audit, where {@code Return} was
 * building a document while it was also holding the refund rules.
 *
 * <p>The return keeps everything it owns: the identifier, the date, the reason,
 * the refunded amounts and the proportion of the original discount that applies
 * to each returned unit. This class only asks for those values through the
 * public interface of the return and arranges them in the order and with the
 * labels the user reads.
 *
 * <p>This is the only place where the receipt of a return is built. The text is
 * the same text {@code Return.generateReturnReceipt} produced, character by
 * character, so moving the menu over changed nothing the user sees. That method
 * was removed once the menu stopped calling it, and this class took its place.
 *
 * @see WarrantyCertificateFormatter
 */
public final class ReturnReceiptFormatter {

    /**
     * Not instantiable: the class only offers one static answer.
     */
    private ReturnReceiptFormatter() {
    }

    /**
     * Builds the receipt of a return, line by line, exactly as the model used to
     * build it.
     * @param returnToFormat the return to describe
     * @return the receipt, ready to be shown to the user
     */
    public static String format(Return returnToFormat) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== GameZone Unicesar - Comprobante de devolucion ===\n");
        receipt.append("Devolucion: ").append(returnToFormat.getId()).append("\n");
        receipt.append("Fecha: ").append(returnToFormat.getDate()).append("\n");
        Sale sale = returnToFormat.getSale();
        if (sale != null) {
            receipt.append("Venta original: ").append(sale.getId()).append("\n");
            receipt.append("Fecha de la venta: ").append(sale.getDate()).append("\n");
            if (sale.getCustomer() != null) {
                receipt.append("Cliente: ").append(sale.getCustomer().getName()).append("\n");
            }
        }
        receipt.append("Productos devueltos:\n");
        double ratio = returnToFormat.discountRatio();
        for (Product product : returnToFormat.getReturnedProducts()) {
            double listPrice = product.getPrice();
            double itemDiscount = listPrice * ratio;
            receipt.append("- ").append(product.getId()).append(" ")
                    .append(product.getTitle())
                    .append(" | Precio: ").append(String.format(Locale.US, "$%,.2f", listPrice))
                    .append(" | Descuento: ").append(String.format(Locale.US, "-$%,.2f", itemDiscount))
                    .append(" | Reembolso: ")
                    .append(String.format(Locale.US, "$%,.2f", listPrice - itemDiscount))
                    .append("\n");
        }
        receipt.append("Motivo: ").append(returnToFormat.getReason()).append("\n");
        receipt.append("Reembolso por garantías anuladas: ")
                .append(String.format(Locale.US, "$%,.2f", returnToFormat.getWarrantyRefund())).append("\n");
        receipt.append("Monto reembolsado: ")
                .append(String.format(Locale.US, "$%,.2f", returnToFormat.getRefundAmount())).append("\n");
        return receipt.toString();
    }
}
