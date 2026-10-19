package com.gamezone.ui;

import com.gamezone.format.AmountFormat;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;

/**
 * Builds the printable receipt of a sale.
 * <p>
 * The receipt is presentation: its wording, its layout and its currency format belong to
 * the console interface and not to {@link Sale}, which only keeps the data and the amounts.
 * This class reads the sale through its getters and produces exactly the same text that the
 * menu showed before.
 */
public final class ReceiptGenerator {

    /** Line separator of the platform, the one {@code %n} produced before. */
    private static final String LINE = System.lineSeparator();

    private ReceiptGenerator() {
    }

    /**
     * Builds the receipt of a sale with its data, its products, the subtotal, the applied
     * discount, the cost of the extended warranties and the final total.
     *
     * @param sale sale to print
     * @return receipt text
     */
    public static String generate(Sale sale) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Venta ").append(sale.getId()).append(" | Fecha: ").append(sale.getDate()).append('\n');
        receipt.append("  Cliente: ").append(sale.getCustomer().getName())
                .append(" (").append(sale.getCustomer().getId()).append(")\n");
        receipt.append("  Vendedor: ").append(sale.getSeller().getName())
                .append(" (").append(sale.getSeller().getEmployeeCode()).append(")\n");
        receipt.append("  Productos:\n");
        for (Product product : sale.getProducts()) {
            receipt.append("    - ").append(product.getId()).append(" ").append(product.getTitle())
                    .append(": ").append(AmountFormat.moneyInComputerLocale(product.getPrice())).append(LINE);
        }
        receipt.append("  Subtotal: ").append(AmountFormat.moneyInComputerLocale(sale.getSubtotal())).append(LINE);
        if (sale.getAppliedPromotionName() != null) {
            receipt.append("  Descuento (").append(sale.getAppliedPromotionName()).append("): -")
                    .append(AmountFormat.moneyInComputerLocale(sale.getDiscountAmount())).append(LINE);
        } else {
            receipt.append("  Descuento: sin promoción aplicable\n");
        }
        receipt.append("  Garantías extendidas: ")
                .append(AmountFormat.moneyInComputerLocale(sale.getExtendedWarrantyCost())).append(LINE);
        receipt.append("  Total: ").append(AmountFormat.moneyInComputerLocale(sale.getTotal()));
        return receipt.toString();
    }
}
