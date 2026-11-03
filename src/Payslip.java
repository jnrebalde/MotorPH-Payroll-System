
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class Payslip {

    private final PayrollRecord payrollRecord;

    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("en-PH"));

    public Payslip(PayrollRecord payrollRecord) {

        if (payrollRecord == null) {
            throw new IllegalArgumentException(
                    "Payroll record cannot be null.");
        }

        this.payrollRecord = payrollRecord;
    }

    public PayrollRecord getPayrollRecord() {
        return payrollRecord;
    }

    public String generatePayslip() {

        Employee employee = payrollRecord.getEmployee();

        StringBuilder payslip = new StringBuilder();

        payslip.append("========================================\n");
        payslip.append("              MOTORPH PAYSLIP            \n");
        payslip.append("========================================\n\n");

        // Employee Information

        payslip.append("EMPLOYEE INFORMATION\n");
        payslip.append("----------------------------------------\n");
        payslip.append("Employee Number: ")
                .append(employee.getEmployeeNumber()).append("\n");
        payslip.append("Employee Name: ")
                .append(employee.getFirstName()).append(" ")
                .append(employee.getLastName()).append("\n");
        payslip.append("Position: ")
                .append(employee.getPosition()).append("\n");
        payslip.append("Employment Status: ")
                .append(employee.getEmploymentStatus()).append("\n");

        // Payroll Information

        payslip.append("\nPAYROLL INFORMATION\n");
        payslip.append("----------------------------------------\n");
        payslip.append("Payroll ID: ")
                .append(payrollRecord.getPayrollID()).append("\n");
        payslip.append("Payroll Date: ")
                .append(payrollRecord.getPayrollDate()).append("\n");
        payslip.append("Cutoff: ")
                .append(payrollRecord.getCutoff()).append("\n");

        // Attendance Information

        payslip.append("\nATTENDANCE SUMMARY\n");
        payslip.append("----------------------------------------\n");
        payslip.append("Regular Hours Worked: ")
                .append(payrollRecord.getRegularHoursWorked())
                .append(" hours\n");
        payslip.append("Overtime Hours: ")
                .append(payrollRecord.getOvertimeHours())
                .append(" hours\n");

        // Earnings

        payslip.append("\nEARNINGS\n");
        payslip.append("----------------------------------------\n");
        payslip.append("Basic Salary: ")
                .append(formatAmount(payrollRecord.getBasicSalary()))
                .append("\n");
        payslip.append("Overtime Pay: ")
                .append(formatAmount(payrollRecord.getOvertimePay()))
                .append("\n");
        payslip.append("Allowances: ")
                .append(formatAmount(payrollRecord.getAllowances()))
                .append("\n");
        payslip.append("Other Income: ")
                .append(formatAmount(payrollRecord.getOtherIncome()))
                .append("\n");
        payslip.append("----------------------------------------\n");
        payslip.append("TOTAL EARNINGS: ")
                .append(formatAmount(payrollRecord.getTotalEarnings()))
                .append("\n");

        // Deductions

        payslip.append("\nDEDUCTIONS\n");
        payslip.append("----------------------------------------\n");
        payslip.append("SSS: ")
                .append(formatAmount(payrollRecord.getSSSDeduction()))
                .append("\n");
        payslip.append("PhilHealth: ")
                .append(formatAmount(payrollRecord.getPhilHealthDeduction()))
                .append("\n");
        payslip.append("Pag-IBIG: ")
                .append(formatAmount(payrollRecord.getPagIBIGDeduction()))
                .append("\n");
        payslip.append("Withholding Tax: ")
                .append(formatAmount(payrollRecord.getWithholdingTax()))
                .append("\n");
        payslip.append("Loan Deduction: ")
                .append(formatAmount(payrollRecord.getLoanDeduction()))
                .append("\n");
        payslip.append("Other Deductions: ")
                .append(formatAmount(payrollRecord.getOtherDeductions()))
                .append("\n");
        payslip.append("----------------------------------------\n");
        payslip.append("TOTAL DEDUCTIONS: ")
                .append(formatAmount(payrollRecord.getTotalDeductions()))
                .append("\n");

        // Net Pay

        payslip.append("\n========================================\n");
        payslip.append("NET PAY: ")
                .append(formatAmount(payrollRecord.getNetPay()))
                .append("\n");
        payslip.append("========================================\n");

        return payslip.toString();
    }

    // Formats monetary values in Philippine Peso.

    private String formatAmount(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount);
    }
}
