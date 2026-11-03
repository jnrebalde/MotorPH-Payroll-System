import java.math.BigDecimal;
import java.math.RoundingMode;

public class PagIBIGDeduction extends Deduction {
    public final BigDecimal monthlySalary;

    // Pag-IBIG Rules Constants
    private static final BigDecimal EMPLOYEE_RATE_LOW = new BigDecimal("0.01");
    private static final BigDecimal EMPLOYEE_RATE_HIGH = new BigDecimal("0.02");
    private static final BigDecimal EMPLOYER_RATE = new BigDecimal("0.02");

    private static final BigDecimal SALARY_THRESHOLD = new BigDecimal("1500.00");
    private static final BigDecimal MAXIMUM_SALARY = new BigDecimal("10000.00");

    public PagIBIGDeduction(String deductionName, String description, BigDecimal monthlySalary) {
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
        BigDecimal salaryBasis = monthlySalary.min(MAXIMUM_SALARY);

        BigDecimal employeeRate = salaryBasis.compareTo(SALARY_THRESHOLD) <= 0
                ? EMPLOYEE_RATE_LOW
                : EMPLOYEE_RATE_HIGH;

        return salaryBasis.multiply(employeeRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
