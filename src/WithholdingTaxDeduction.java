
import java.math.BigDecimal;
import java.math.RoundingMode;

public class WithholdingTaxDeduction extends Deduction {

    private final BigDecimal taxableCompensation;

    // BIR Semi-Monthly Tax Rules Constants
    private static final BigDecimal BRACKET_1 = new BigDecimal("10417");
    private static final BigDecimal BRACKET_2 = new BigDecimal("16667");
    private static final BigDecimal BRACKET_3 = new BigDecimal("33333");
    private static final BigDecimal BRACKET_4 = new BigDecimal("83333");
    private static final BigDecimal BRACKET_5 = new BigDecimal("333333");

    private static final BigDecimal TAX_2 = new BigDecimal("937.50");
    private static final BigDecimal TAX_3 = new BigDecimal("4270.70");
    private static final BigDecimal TAX_4 = new BigDecimal("16770.70");
    private static final BigDecimal TAX_5 = new BigDecimal("91770.70");

    public WithholdingTaxDeduction(String deductionName, String description,
                                   BigDecimal taxableCompensation) {
        super(deductionName, description);

        if (taxableCompensation == null ||
                taxableCompensation.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Taxable compensation cannot be negative."
            );
        }

        this.taxableCompensation = taxableCompensation;
    }

    public BigDecimal getTaxableCompensation() {
        return taxableCompensation;
    }

    @Override
    public BigDecimal computeDeduction() {

        BigDecimal tax;

        if (taxableCompensation.compareTo(BRACKET_1) <= 0) {
            tax = BigDecimal.ZERO;

        } else if (taxableCompensation.compareTo(BRACKET_2) <= 0) {
            tax = taxableCompensation.subtract(BRACKET_1)
                    .multiply(new BigDecimal("0.15"));

        } else if (taxableCompensation.compareTo(BRACKET_3) <= 0) {
            tax = TAX_2.add(
                    taxableCompensation.subtract(BRACKET_2)
                            .multiply(new BigDecimal("0.20"))
            );

        } else if (taxableCompensation.compareTo(BRACKET_4) <= 0) {
            tax = TAX_3.add(
                    taxableCompensation.subtract(BRACKET_3)
                            .multiply(new BigDecimal("0.25"))
            );

        } else if (taxableCompensation.compareTo(BRACKET_5) <= 0) {
            tax = TAX_4.add(
                    taxableCompensation.subtract(BRACKET_4)
                            .multiply(new BigDecimal("0.30"))
            );

        } else {
            tax = TAX_5.add(
                    taxableCompensation.subtract(BRACKET_5)
                            .multiply(new BigDecimal("0.35"))
            );
        }

        return tax.setScale(2, RoundingMode.HALF_UP);
    }
}