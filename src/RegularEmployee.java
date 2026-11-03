
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class RegularEmployee extends Employee {

    private BigDecimal monthlySalary;
    private BigDecimal riceSubsidy;
    private BigDecimal phoneAllowance;
    private BigDecimal clothingAllowance;

    private static final BigDecimal MONTHS_PER_YEAR =
            BigDecimal.valueOf(12);

    private static final BigDecimal DAYS_PER_YEAR =
            BigDecimal.valueOf(365);

    private static final BigDecimal HOURS_PER_DAY =
            BigDecimal.valueOf(8);

    public RegularEmployee(
            String employeeNumber,
            String firstName,
            String lastName,
            LocalDate birthDate,
            String address,
            String contactNo,
            String sssNo,
            String philHealthNo,
            String tinNo,
            String pagIbigNo,
            String employmentStatus,
            String position,
            String supervisor,
            BigDecimal monthlySalary,
            BigDecimal riceSubsidy,
            BigDecimal phoneAllowance,
            BigDecimal clothingAllowance) {

        super(employeeNumber, firstName, lastName, birthDate,
                address, contactNo, sssNo, philHealthNo, tinNo,
                pagIbigNo, employmentStatus, position, supervisor);

        this.monthlySalary = validateAmount(
                monthlySalary, "Monthly salary");

        this.riceSubsidy = validateAmount(
                riceSubsidy, "Rice subsidy");

        this.phoneAllowance = validateAmount(
                phoneAllowance, "Phone allowance");

        this.clothingAllowance = validateAmount(
                clothingAllowance, "Clothing allowance");
    }

    // Monthly Salary

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(BigDecimal monthlySalary) {
        this.monthlySalary = validateAmount(
                monthlySalary, "Monthly salary");
    }

    // Automatically calculates hourly rate from monthly salary.

    public BigDecimal getHourlyRate() {
        return monthlySalary
                .multiply(MONTHS_PER_YEAR)
                .divide(
                        DAYS_PER_YEAR.multiply(HOURS_PER_DAY),
                        6,
                        RoundingMode.HALF_UP
                );
    }

    // Rice Subsidy

    public BigDecimal getRiceSubsidy() {
        return riceSubsidy;
    }

    public void setRiceSubsidy(BigDecimal riceSubsidy) {
        this.riceSubsidy = validateAmount(
                riceSubsidy, "Rice subsidy");
    }

    // Phone Allowance

    public BigDecimal getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(BigDecimal phoneAllowance) {
        this.phoneAllowance = validateAmount(
                phoneAllowance, "Phone allowance");
    }

    // Clothing Allowance

    public BigDecimal getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(BigDecimal clothingAllowance) {
        this.clothingAllowance = validateAmount(
                clothingAllowance, "Clothing allowance");
    }

    // Calculates basic salary for the cutoff.

    @Override
    public BigDecimal calculateBasicPay(
            BigDecimal regularHoursWorked,
            PayrollCutoff cutoff) {

        Objects.requireNonNull(cutoff, "Payroll cutoff cannot be null.");

        return monthlySalary.divide(
                BigDecimal.valueOf(2),
                2,
                RoundingMode.HALF_UP
        );
    }

    // Allowances are included in the second cutoff only.

    @Override
    public BigDecimal calculateAllowances(PayrollCutoff cutoff) {

        Objects.requireNonNull(cutoff, "Payroll cutoff cannot be null.");

        if (cutoff == PayrollCutoff.FIRST) {
            return BigDecimal.ZERO;
        }

        return riceSubsidy
                .add(phoneAllowance)
                .add(clothingAllowance);
    }

    @Override
    public String toString() {
        return "Regular Employee{" +
                super.toString() +
                ", monthlySalary=" + monthlySalary +
                ", hourlyRate=" + getHourlyRate() +
                ", riceSubsidy=" + riceSubsidy +
                ", phoneAllowance=" + phoneAllowance +
                ", clothingAllowance=" + clothingAllowance +
                '}';
    }
}
