
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class PayrollRecord {

    private final String payrollID;
    private final Employee employee;
    private final LocalDate payrollDate;
    private final PayrollCutoff cutoff;

    // Attendance
    private final BigDecimal regularHoursWorked;
    private final BigDecimal overtimeHours;

    // Earnings
    private BigDecimal basicSalary;
    private BigDecimal overtimePay;
    private BigDecimal allowances;
    private BigDecimal otherIncome;

    // Individual deductions
    private BigDecimal sssDeduction;
    private BigDecimal philHealthDeduction;
    private BigDecimal pagIBIGDeduction;
    private BigDecimal withholdingTax;
    private BigDecimal loanDeduction;
    private BigDecimal otherDeductions;

    public PayrollRecord(
            String payrollID,
            Employee employee,
            LocalDate payrollDate,
            PayrollCutoff cutoff,
            BigDecimal regularHoursWorked,
            BigDecimal overtimeHours,
            BigDecimal basicSalary,
            BigDecimal overtimePay,
            BigDecimal allowances,
            BigDecimal otherIncome,
            BigDecimal sssDeduction,
            BigDecimal philHealthDeduction,
            BigDecimal pagIBIGDeduction,
            BigDecimal withholdingTax,
            BigDecimal loanDeduction,
            BigDecimal otherDeductions) {

        if (payrollID == null || payrollID.isBlank()) {
            throw new IllegalArgumentException(
                    "Payroll ID cannot be blank.");
        }

        this.payrollID = payrollID.trim();
        this.employee = Objects.requireNonNull(
                employee, "Employee cannot be null.");
        this.payrollDate = Objects.requireNonNull(
                payrollDate, "Payroll date cannot be null.");
        this.cutoff = Objects.requireNonNull(
                cutoff, "Payroll cutoff cannot be null.");

        // Attendance
        this.regularHoursWorked = validateAmount(
                regularHoursWorked, "Regular hours worked");
        this.overtimeHours = validateAmount(
                overtimeHours, "Overtime hours");

        // Earnings
        this.basicSalary = validateAmount(basicSalary);
        this.overtimePay = validateAmount(overtimePay);
        this.allowances = validateAmount(allowances);
        this.otherIncome = validateAmount(otherIncome);

        // Deductions
        this.sssDeduction = validateAmount(sssDeduction);
        this.philHealthDeduction = validateAmount(philHealthDeduction);
        this.pagIBIGDeduction = validateAmount(pagIBIGDeduction);
        this.withholdingTax = validateAmount(withholdingTax);
        this.loanDeduction = validateAmount(loanDeduction);
        this.otherDeductions = validateAmount(otherDeductions);
    }

    // Basic Information Getters

    public String getPayrollID() {
        return payrollID;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getPayrollDate() {
        return payrollDate;
    }

    public PayrollCutoff getCutoff() {
        return cutoff;
    }

    // Attendance Getters

    public BigDecimal getRegularHoursWorked() {
        return regularHoursWorked;
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    // Earnings Getters

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public BigDecimal getOvertimePay() {
        return overtimePay;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public BigDecimal getOtherIncome() {
        return otherIncome;
    }

    public BigDecimal getTotalEarnings() {
        return basicSalary
                .add(overtimePay)
                .add(allowances)
                .add(otherIncome);
    }

    // Deduction Getters

    public BigDecimal getSSSDeduction() {
        return sssDeduction;
    }

    public BigDecimal getPhilHealthDeduction() {
        return philHealthDeduction;
    }

    public BigDecimal getPagIBIGDeduction() {
        return pagIBIGDeduction;
    }

    public BigDecimal getWithholdingTax() {
        return withholdingTax;
    }

    public BigDecimal getLoanDeduction() {
        return loanDeduction;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public BigDecimal getTotalDeductions() {
        return sssDeduction
                .add(philHealthDeduction)
                .add(pagIBIGDeduction)
                .add(withholdingTax)
                .add(loanDeduction)
                .add(otherDeductions);
    }

    // Net Pay

    public BigDecimal getNetPay() {
        return getTotalEarnings().subtract(getTotalDeductions());
    }

    // Earnings Setters

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = validateAmount(basicSalary);
    }

    public void setOvertimePay(BigDecimal overtimePay) {
        this.overtimePay = validateAmount(overtimePay);
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = validateAmount(allowances);
    }

    public void setOtherIncome(BigDecimal otherIncome) {
        this.otherIncome = validateAmount(otherIncome);
    }

    // Deduction Setters

    public void setSSSDeduction(BigDecimal amount) {
        this.sssDeduction = validateAmount(amount);
    }

    public void setPhilHealthDeduction(BigDecimal amount) {
        this.philHealthDeduction = validateAmount(amount);
    }

    public void setPagIBIGDeduction(BigDecimal amount) {
        this.pagIBIGDeduction = validateAmount(amount);
    }

    public void setWithholdingTax(BigDecimal amount) {
        this.withholdingTax = validateAmount(amount);
    }

    public void setLoanDeduction(BigDecimal amount) {
        this.loanDeduction = validateAmount(amount);
    }

    public void setOtherDeductions(BigDecimal amount) {
        this.otherDeductions = validateAmount(amount);
    }

    // Validation

    private BigDecimal validateAmount(BigDecimal amount) {
        Objects.requireNonNull(
                amount, "Amount cannot be null.");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Amount cannot be negative.");
        }

        return amount;
    }

    private BigDecimal validateAmount(
            BigDecimal amount, String fieldName) {

        Objects.requireNonNull(
                amount, fieldName + " cannot be null.");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative.");
        }

        return amount;
    }
}
