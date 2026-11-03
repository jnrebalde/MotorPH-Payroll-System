import java.math.BigDecimal;
import java.math.RoundingMode;

public class PhilHealthDeduction extends Deduction {

    private final BigDecimal monthlySalary;

    // PhilHealth Rules Constants
    private static final BigDecimal PREMIUM_RATE = new BigDecimal("0.05"); // 5%
    private static final BigDecimal SALARY_FLOOR = new BigDecimal("10000.00");
    private static final BigDecimal SALARY_CEILING = new BigDecimal("100000.00");
    private static final BigDecimal SPLIT_COUNTERPART = new BigDecimal("2");

    public PhilHealthDeduction(String deductionName, String description,
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
    @Override
    public BigDecimal computeDeduction() {
            if (monthlySalary.compareTo(SALARY_FLOOR) < 0) {
                return new BigDecimal("250.00");
            }
            BigDecimal salary = monthlySalary.min(SALARY_CEILING);

            return salary.multiply(PREMIUM_RATE)
                    .divide(SPLIT_COUNTERPART, 2, RoundingMode.HALF_UP);
    }
}
