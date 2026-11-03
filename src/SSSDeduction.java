import java.math.BigDecimal;
import java.math.RoundingMode;

public class SSSDeduction extends Deduction {
    private final BigDecimal monthlySalary;

    // SSS Rules Constants
    private static final BigDecimal EMPLOYEE_RATE = new BigDecimal("0.05");
    private static final BigDecimal EMPLOYER_RATE = new BigDecimal("0.10");
    private static final BigDecimal TOTAL_RATE = new BigDecimal("0.15");

    private static final BigDecimal MINIMUM_MSC = new BigDecimal("5000.00");
    private static final BigDecimal MAXIMUM_MSC = new BigDecimal("35000.00");
    private static final BigDecimal MSC_INCREMENT = new BigDecimal("500.00");

    public SSSDeduction(String deductionName, String description,
                        BigDecimal monthlySalary) {
        super(deductionName, description);

        if (monthlySalary == null ||
        monthlySalary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monthly salary must be greater than zero");
        }

        this.monthlySalary = monthlySalary;
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public BigDecimal getMonthlySalaryCredit() {
        BigDecimal msc = monthlySalary
                .add(MSC_INCREMENT.divide(BigDecimal.valueOf(2)))
                .divide(MSC_INCREMENT, 0, RoundingMode.DOWN)
                .multiply(MSC_INCREMENT);

        return msc.max(MINIMUM_MSC).min(MAXIMUM_MSC);
    }

    @Override
    public BigDecimal computeDeduction() {
        BigDecimal msc = getMonthlySalaryCredit();

        if (msc.compareTo(new BigDecimal("5000")) < 0) {
            return new BigDecimal("250.00");
        }

        if (msc.compareTo(MAXIMUM_MSC) >= 0) {
            return new BigDecimal("1750.00");
        }

        return msc.multiply(EMPLOYEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

