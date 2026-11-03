import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class PayrollCalculator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public PayrollRecord calculatePayroll(
            String payrollID,
            Employee employee,
            LocalDate payrollDate,
            PayrollCutoff cutoff,
            LocalDate cutoffStart,
            LocalDate cutoffEnd,
            List<AttendanceRecord> attendanceRecords,
            BigDecimal overtimePay,
            BigDecimal otherIncome,
            Deduction sssDeduction,
            Deduction philHealthDeduction,
            Deduction pagIBIGDeduction,
            Deduction withholdingTax,
            Deduction loanDeduction,
            BigDecimal otherDeductions) {

        // Check required information.
        if (employee == null || payrollDate == null || cutoff == null
                || cutoffStart == null || cutoffEnd == null
                || attendanceRecords == null) {
            throw new IllegalArgumentException("Required payroll information is missing.");
        }

        if (cutoffEnd.isBefore(cutoffStart)) {
            throw new IllegalArgumentException("Cutoff end date cannot be before cutoff start date.");
        }
        if (!payrollDate.isAfter(cutoffEnd)) {
            throw new IllegalArgumentException("Payroll date must be after the payroll cutoff end date.");
        }

        // Check monetary values.
        overtimePay = validateAmount(overtimePay, "Overtime pay");
        otherIncome = validateAmount(otherIncome, "Other income");
        otherDeductions = validateAmount(otherDeductions, "Other deductions");

        // Get this employee's attendance hours within the cutoff.
        BigDecimal totalRegularHours = ZERO;
        BigDecimal totalOvertimeHours = ZERO;
        Set<LocalDate> daysWorked = new HashSet<>();

        for (AttendanceRecord record : attendanceRecords) {
            if (record == null) {
                continue;
            }

            // Skip attendance records belonging to other employees.
            if (!record.getEmployee().getEmployeeNumber()
                    .equals(employee.getEmployeeNumber())) {
                continue;
            }

            // Include only attendance within the selected cutoff.
            if (!record.getDate().isBefore(cutoffStart)
                    && !record.getDate().isAfter(cutoffEnd)) {

                daysWorked.add(record.getDate());
                totalRegularHours = totalRegularHours.add(
                        record.calculateRegularHours());

                // Overtime
                totalOvertimeHours = totalOvertimeHours.add(
                        record.calculateOvertimeHours());
            }
        }

        // Calculate basic pay based on the employee type.
        BigDecimal basicSalary;
        if (employee instanceof RegularEmployee) {
            // Regular employees receive their semi-monthly gross rate.
            basicSalary = ((RegularEmployee) employee).getMonthlySalary()
                    .divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
        } else if (employee instanceof ContractEmployee) {
            // Contractual employees are paid by attendance days, not semi-monthly salary.
            ContractEmployee contract = (ContractEmployee) employee;
            BigDecimal dailyRate = contract.getDailyRate();
            basicSalary = dailyRate.multiply(BigDecimal.valueOf(daysWorked.size()))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            basicSalary = employee.calculateBasicPay(totalRegularHours, cutoff);
        }

        // Get applicable allowances.
        BigDecimal allowances = employee.calculateAllowances(cutoff);

        // Statutory deductions are applied during the second cutoff only.
        BigDecimal sss = calculateMonthlyDeduction(cutoff, sssDeduction);
        BigDecimal philHealth = calculateMonthlyDeduction(cutoff, philHealthDeduction);
        BigDecimal pagIBIG = calculateMonthlyDeduction(cutoff, pagIBIGDeduction);

        // Calculate other deductions.
        BigDecimal tax = calculateDeduction(withholdingTax);
        BigDecimal loan = calculateDeduction(loanDeduction);

        // Create and return the payroll record.
        return new PayrollRecord(
                payrollID,
                employee,
                payrollDate,
                cutoff,
                totalRegularHours,
                totalOvertimeHours,
                basicSalary,
                overtimePay,
                allowances,
                otherIncome,
                sss,
                philHealth,
                pagIBIG,
                tax,
                loan,
                otherDeductions
        );
    }

    // Apply monthly deductions only during the second cutoff.
    private BigDecimal calculateMonthlyDeduction(
            PayrollCutoff cutoff, Deduction deduction) {

        if (cutoff == PayrollCutoff.FIRST || deduction == null) {
            return ZERO;
        }

        return calculateDeduction(deduction);
    }

    // Use the calculation method in each Deduction class.
    private BigDecimal calculateDeduction(Deduction deduction) {
        if (deduction == null) {
            return ZERO;
        }

        return validateAmount(deduction.computeDeduction(), "Calculated deduction");
    }

    // Check that an amount is not missing or negative.
    private BigDecimal validateAmount(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be null or negative.");
        }

        return amount;
    }
}
