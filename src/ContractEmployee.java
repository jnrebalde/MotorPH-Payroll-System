
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class ContractEmployee extends Employee {

    private BigDecimal hourlyRate;

    public ContractEmployee(
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
            BigDecimal hourlyRate) {

        super(employeeNumber, firstName, lastName, birthDate,
                address, contactNo, sssNo, philHealthNo, tinNo,
                pagIbigNo, employmentStatus, position, supervisor);

        this.hourlyRate = validateAmount(hourlyRate, "Hourly rate");

        if (this.hourlyRate.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "Hourly rate must be greater than zero.");
        }
    }

    // Hourly Rate

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = validateAmount(hourlyRate, "Hourly rate");

        if (this.hourlyRate.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "Hourly rate must be greater than zero.");
        }
    }

    // Daily pay is based on an eight-hour workday.
    public BigDecimal getDailyRate() {
        return hourlyRate.multiply(BigDecimal.valueOf(8))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Calculates basic pay based on regular hours worked.

    @Override
    public BigDecimal calculateBasicPay(
            BigDecimal regularHoursWorked,
            PayrollCutoff cutoff) {

        Objects.requireNonNull(
                regularHoursWorked,
                "Regular hours worked cannot be null.");

        Objects.requireNonNull(cutoff, "Payroll cutoff cannot be null.");

        if (regularHoursWorked.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Regular hours worked cannot be negative.");
        }

        return hourlyRate.multiply(regularHoursWorked)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Contract Employee{" +
                super.toString() +
                ", hourlyRate=" + hourlyRate +
                '}';
    }
}
