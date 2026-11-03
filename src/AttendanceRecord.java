
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class AttendanceRecord {

    private static final int STANDARD_WORK_HOURS = 8;
    private static final int BREAK_MINUTES = 60;

    private final Employee employee;
    private final LocalDate date;
    private final LocalTime timeIn;
    private final LocalTime timeOut;

    public AttendanceRecord(
            Employee employee,
            LocalDate date,
            LocalTime timeIn,
            LocalTime timeOut) {

        this.employee = Objects.requireNonNull(
                employee, "Employee cannot be null.");

        this.date = Objects.requireNonNull(
                date, "Attendance date cannot be null.");

        this.timeIn = Objects.requireNonNull(
                timeIn, "Time-in cannot be null.");

        this.timeOut = Objects.requireNonNull(
                timeOut, "Time-out cannot be null.");

        if (timeIn.equals(timeOut)) {
            throw new IllegalArgumentException(
                    "Time-in and time-out cannot be identical.");
        }
    }

    // Getters

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTimeIn() {
        return timeIn;
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    // Calculates actual working hours after deducting the standard break.

    public BigDecimal calculateHoursWorked() {

        LocalDateTime start = date.atTime(timeIn);
        LocalDateTime end = date.atTime(timeOut);

        // Handles overnight shifts.
        if (end.isBefore(start)) {
            end = end.plusDays(1);
        }

        long totalMinutes = Duration.between(start, end).toMinutes();

        long workingMinutes = Math.max(
                0, totalMinutes - BREAK_MINUTES
        );

        return BigDecimal.valueOf(workingMinutes)
                .divide(
                        BigDecimal.valueOf(60),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    // Calculates overtime hours.

    public BigDecimal calculateOvertimeHours() {

        BigDecimal standardHours =
                BigDecimal.valueOf(STANDARD_WORK_HOURS);

        BigDecimal overtime = calculateHoursWorked()
                .subtract(standardHours);

        return overtime.max(BigDecimal.ZERO);
    }

    // Calculates regular hours excluding overtime.

    public BigDecimal calculateRegularHours() {

        return calculateHoursWorked()
                .subtract(calculateOvertimeHours())
                .max(BigDecimal.ZERO);
    }
}
