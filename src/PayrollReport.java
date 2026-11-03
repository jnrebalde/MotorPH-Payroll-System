
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

public class PayrollReport {

    private final List<PayrollRecord> payrollRecords;

    public PayrollReport(List<PayrollRecord> payrollRecords) {
        if (payrollRecords == null) {
            throw new IllegalArgumentException(
                    "Payroll records cannot be null."
            );
        }

        this.payrollRecords = new ArrayList<>(payrollRecords);
    }

    public List<PayrollRecord> getPayrollRecords() {
        return new ArrayList<>(payrollRecords);
    }

    public int getTotalEmployees() {
        return payrollRecords.size();
    }

    public BigDecimal getTotalEarnings() {
        return payrollRecords.stream()
                .map(PayrollRecord::getTotalEarnings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDeductions() {
        return payrollRecords.stream()
                .map(PayrollRecord::getTotalDeductions)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalNetPay() {
        return payrollRecords.stream()
                .map(PayrollRecord::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String generateReport() {

        StringBuilder report = new StringBuilder();

        report.append("============================================\n");
        report.append("           MOTORPH PAYROLL REPORT            \n");
        report.append("============================================\n\n");

        report.append("Total Employees: ")
                .append(getTotalEmployees()).append("\n");

        report.append("Total Earnings: ")
                .append(getTotalEarnings()).append("\n");

        report.append("Total Deductions: ")
                .append(getTotalDeductions()).append("\n");

        report.append("--------------------------------------------\n");

        report.append("TOTAL NET PAY: ")
                .append(getTotalNetPay()).append("\n");

        report.append("============================================\n");

        return report.toString();
    }
}