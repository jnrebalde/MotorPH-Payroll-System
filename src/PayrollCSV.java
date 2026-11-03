import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/** Exports calculated payroll records to a spreadsheet-compatible CSV file. */
public class PayrollCSV {

    public void exportPayrollRecords(List<PayrollRecord> records, Path filePath)
            throws IOException {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("There are no payroll records to export.");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }

        Path parent = filePath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(String.join(",",
                    "Payroll ID", "Employee Number", "First Name", "Last Name",
                    "Position", "Employment Status", "Payroll Date", "Cutoff",
                    "Regular Hours", "Overtime Hours", "Basic Salary", "Overtime Pay",
                    "Allowances", "Other Income", "Total Earnings", "SSS", "PhilHealth",
                    "Pag-IBIG", "Withholding Tax", "Loan Deduction", "Other Deductions",
                    "Total Deductions", "Net Pay"));
            writer.newLine();

            for (PayrollRecord record : records) {
                Employee employee = record.getEmployee();
                String[] values = {
                        record.getPayrollID(), employee.getEmployeeNumber(),
                        employee.getFirstName(), employee.getLastName(), employee.getPosition(),
                        employee.getEmploymentStatus(), record.getPayrollDate().toString(),
                        record.getCutoff().toString(), record.getRegularHoursWorked().toPlainString(),
                        record.getOvertimeHours().toPlainString(), money(record.getBasicSalary()),
                        money(record.getOvertimePay()), money(record.getAllowances()),
                        money(record.getOtherIncome()), money(record.getTotalEarnings()),
                        money(record.getSSSDeduction()), money(record.getPhilHealthDeduction()),
                        money(record.getPagIBIGDeduction()), money(record.getWithholdingTax()),
                        money(record.getLoanDeduction()), money(record.getOtherDeductions()),
                        money(record.getTotalDeductions()), money(record.getNetPay())
                };
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) writer.write(",");
                    writer.write(escape(values[i]));
                }
                writer.newLine();
            }
        }
    }



    /** Saves one calculated payroll record to the permanent payroll register. */
    public void appendPayrollRecord(PayrollRecord record, Path filePath)
            throws IOException {
        if (record == null || filePath == null) {
            throw new IllegalArgumentException("Payroll record and file path are required.");
        }

        Path parent = filePath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        boolean fileExists = Files.exists(filePath) && Files.size(filePath) > 0;
        if (fileExists) {
            DefaultTableModel existing = readCSV(filePath);
            if (existing.getColumnCount() == 0
                    || !"Payroll ID".equalsIgnoreCase(existing.getColumnName(0).trim())) {
                throw new IOException("The existing Payroll.csv has an unsupported format. "
                        + "It was not changed. Back up the file and verify its columns first.");
            }
            for (int row = 0; row < existing.getRowCount(); row++) {
                String existingId = String.valueOf(existing.getValueAt(row, 0));
                if (record.getPayrollID().equalsIgnoreCase(existingId.trim())) {
                    throw new IOException("This payroll record already exists in Payroll.csv.");
                }
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND)) {
            if (!fileExists) {
                writer.write(String.join(",",
                        "Payroll ID", "Employee Number", "First Name", "Last Name",
                        "Position", "Employment Status", "Payroll Date", "Cutoff",
                        "Regular Hours", "Overtime Hours", "Basic Salary", "Overtime Pay",
                        "Allowances", "Other Income", "Total Earnings", "SSS", "PhilHealth",
                        "Pag-IBIG", "Withholding Tax", "Loan Deduction", "Other Deductions",
                        "Total Deductions", "Net Pay"));
                writer.newLine();
            }

            Employee employee = record.getEmployee();
            String[] values = {
                    record.getPayrollID(), employee.getEmployeeNumber(),
                    employee.getFirstName(), employee.getLastName(), employee.getPosition(),
                    employee.getEmploymentStatus(), record.getPayrollDate().toString(),
                    record.getCutoff().toString(), record.getRegularHoursWorked().toPlainString(),
                    record.getOvertimeHours().toPlainString(), money(record.getBasicSalary()),
                    money(record.getOvertimePay()), money(record.getAllowances()),
                    money(record.getOtherIncome()), money(record.getTotalEarnings()),
                    money(record.getSSSDeduction()), money(record.getPhilHealthDeduction()),
                    money(record.getPagIBIGDeduction()), money(record.getWithholdingTax()),
                    money(record.getLoanDeduction()), money(record.getOtherDeductions()),
                    money(record.getTotalDeductions()), money(record.getNetPay())
            };
            for (int i = 0; i < values.length; i++) {
                if (i > 0) writer.write(",");
                writer.write(escape(values[i]));
            }
            writer.newLine();
        }
    }


    /** Exports only the saved payroll rows belonging to the selected cutoff. */
    public int exportPayrollByCutoff(Path sourceFile, PayrollCutoff cutoff,
                                     Path destinationFile) throws IOException {
        if (sourceFile == null || cutoff == null || destinationFile == null) {
            throw new IllegalArgumentException("Source, cutoff, and destination are required.");
        }
        if (!Files.exists(sourceFile)) {
            throw new IOException("Saved payroll file was not found: " + sourceFile);
        }

        DefaultTableModel model = readCSV(sourceFile);
        int cutoffColumn = -1;
        for (int column = 0; column < model.getColumnCount(); column++) {
            if ("Cutoff".equalsIgnoreCase(model.getColumnName(column).trim())) {
                cutoffColumn = column;
                break;
            }
        }
        if (cutoffColumn < 0) {
            throw new IOException("The Payroll.csv file does not contain a Cutoff column.");
        }

        List<String[]> matchingRows = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            String value = String.valueOf(model.getValueAt(row, cutoffColumn)).trim();
            if (cutoff.name().equalsIgnoreCase(value)) {
                String[] values = new String[model.getColumnCount()];
                for (int column = 0; column < model.getColumnCount(); column++) {
                    Object cell = model.getValueAt(row, column);
                    values[column] = cell == null ? "" : cell.toString();
                }
                matchingRows.add(values);
            }
        }

        if (matchingRows.isEmpty()) {
            return 0;
        }

        Path parent = destinationFile.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);

        try (BufferedWriter writer = Files.newBufferedWriter(
                destinationFile, StandardCharsets.UTF_8)) {
            for (int column = 0; column < model.getColumnCount(); column++) {
                if (column > 0) writer.write(",");
                writer.write(escape(model.getColumnName(column)));
            }
            writer.newLine();

            for (String[] values : matchingRows) {
                for (int column = 0; column < values.length; column++) {
                    if (column > 0) writer.write(",");
                    writer.write(escape(values[column]));
                }
                writer.newLine();
            }
        }
        return matchingRows.size();
    }


    /** Exports payroll records for one cutoff and its actual payroll period month/year. */
    public int exportPayrollByCutoffAndMonth(Path sourceFile, PayrollCutoff cutoff,
                                             int month, int year, Path destinationFile) throws IOException {
        if (sourceFile == null || cutoff == null || destinationFile == null
                || month < 1 || month > 12) {
            throw new IllegalArgumentException("Valid source, cutoff, month, year, and destination are required.");
        }
        if (!Files.exists(sourceFile)) {
            throw new IOException("Saved payroll file was not found: " + sourceFile);
        }

        DefaultTableModel model = readCSV(sourceFile);
        int cutoffColumn = -1;
        int dateColumn = -1;
        for (int column = 0; column < model.getColumnCount(); column++) {
            String header = model.getColumnName(column).trim();
            if ("Cutoff".equalsIgnoreCase(header)) cutoffColumn = column;
            if ("Payroll Date".equalsIgnoreCase(header)) dateColumn = column;
        }
        if (cutoffColumn < 0 || dateColumn < 0) {
            throw new IOException("Payroll.csv must contain Payroll Date and Cutoff columns.");
        }

        List<String[]> matchingRows = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            String cutoffValue = String.valueOf(model.getValueAt(row, cutoffColumn)).trim();
            String dateValue = String.valueOf(model.getValueAt(row, dateColumn)).trim();
            if (!cutoff.name().equalsIgnoreCase(cutoffValue)) continue;

            try {
                java.time.LocalDate payrollDate = java.time.LocalDate.parse(dateValue);
                // SECOND cutoff is paid in the following month, so report it
                // under the month in which the cutoff period actually occurred.
                java.time.LocalDate periodDate = cutoff == PayrollCutoff.SECOND
                        ? payrollDate.minusMonths(1) : payrollDate;
                if (periodDate.getMonthValue() == month && periodDate.getYear() == year) {
                    String[] values = new String[model.getColumnCount()];
                    for (int column = 0; column < model.getColumnCount(); column++) {
                        Object cell = model.getValueAt(row, column);
                        values[column] = cell == null ? "" : cell.toString();
                    }
                    matchingRows.add(values);
                }
            } catch (java.time.format.DateTimeParseException ex) {
                throw new IOException("Invalid Payroll Date in Payroll.csv at data row "
                        + (row + 2) + ": " + dateValue, ex);
            }
        }

        if (matchingRows.isEmpty()) return 0;

        Path parent = destinationFile.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);

        try (BufferedWriter writer = Files.newBufferedWriter(destinationFile, StandardCharsets.UTF_8)) {
            for (int column = 0; column < model.getColumnCount(); column++) {
                if (column > 0) writer.write(",");
                writer.write(escape(model.getColumnName(column)));
            }
            writer.newLine();
            for (String[] values : matchingRows) {
                for (int column = 0; column < values.length; column++) {
                    if (column > 0) writer.write(",");
                    writer.write(escape(values[column]));
                }
                writer.newLine();
            }
        }
        return matchingRows.size();
    }


    /** Reconstructs saved payroll rows as PayrollRecord objects for payslips. */
    public List<PayrollRecord> loadPayrollRecords(Path filePath, List<Employee> employees)
            throws IOException {
        if (filePath == null || employees == null) {
            throw new IllegalArgumentException("Payroll file and employee list are required.");
        }
        List<PayrollRecord> records = new ArrayList<>();
        if (!Files.exists(filePath) || Files.size(filePath) == 0) return records;
        DefaultTableModel model = readCSV(filePath);

        String[] names = {"Payroll ID", "Employee Number", "Payroll Date", "Cutoff",
                "Regular Hours", "Overtime Hours", "Basic Salary", "Overtime Pay",
                "Allowances", "Other Income", "SSS", "PhilHealth", "Pag-IBIG",
                "Withholding Tax", "Loan Deduction", "Other Deductions"};
        int[] columns = new int[names.length];
        for (int i = 0; i < names.length; i++) {
            columns[i] = columnIndex(model, names[i]);
            if (columns[i] < 0) throw new IOException("Payroll.csv is missing required column: " + names[i]);
        }

        for (int row = 0; row < model.getRowCount(); row++) {
            int csvRow = row + 2;
            try {
                String employeeNo = cell(model, row, columns[1]);
                Employee employee = findEmployee(employees, employeeNo);
                if (employee == null) throw new IllegalArgumentException(
                        "Employee " + employeeNo + " was not found in employees.csv.");
                PayrollRecord record = new PayrollRecord(
                        cell(model, row, columns[0]), employee,
                        java.time.LocalDate.parse(cell(model, row, columns[2])),
                        PayrollCutoff.valueOf(cell(model, row, columns[3]).toUpperCase(java.util.Locale.ROOT)),
                        decimal(model, row, columns[4]), decimal(model, row, columns[5]),
                        decimal(model, row, columns[6]), decimal(model, row, columns[7]),
                        decimal(model, row, columns[8]), decimal(model, row, columns[9]),
                        decimal(model, row, columns[10]), decimal(model, row, columns[11]),
                        decimal(model, row, columns[12]), decimal(model, row, columns[13]),
                        decimal(model, row, columns[14]), decimal(model, row, columns[15]));
                records.add(record);
            } catch (RuntimeException ex) {
                throw new IOException("Unable to reconstruct payroll record at CSV row "
                        + csvRow + ": " + ex.getMessage(), ex);
            }
        }
        return records;
    }


    public List<PayrollRecord> getPayrollRecordsByEmployee(
            Path filePath,
            String employeeNumber,
            List<Employee> employees) throws IOException {

        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee number is required.");
        }

        List<PayrollRecord> allRecords =
                loadPayrollRecords(filePath, employees);

        List<PayrollRecord> employeeRecords = new ArrayList<>();

        for (PayrollRecord record : allRecords) {
            if (record.getEmployee().getEmployeeNumber()
                    .equalsIgnoreCase(employeeNumber.trim())) {

                employeeRecords.add(record);
            }
        }

        return employeeRecords;
    }


    private int columnIndex(DefaultTableModel model, String name) {
        for (int column = 0; column < model.getColumnCount(); column++) {
            if (name.equalsIgnoreCase(model.getColumnName(column).trim())) return column;
        }
        return -1;
    }

    private String cell(DefaultTableModel model, int row, int column) {
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString().trim();
    }

    private BigDecimal decimal(DefaultTableModel model, int row, int column) {
        String value = cell(model, row, column).replace(",", "");
        return value.isEmpty() ? BigDecimal.ZERO : new BigDecimal(value);
    }

    private Employee findEmployee(List<Employee> employees, String employeeNumber) {
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equalsIgnoreCase(employeeNumber.trim())) return employee;
        }
        return null;
    }

    /** Reads a CSV file and returns its contents as a table model. */
    public DefaultTableModel readCSV(Path filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }

        DefaultTableModel model = new DefaultTableModel();
        try (java.io.BufferedReader reader = Files.newBufferedReader(
                filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstRow = true;
            int expectedColumns = -1;

            while ((line = reader.readLine()) != null) {
                List<String> values = parseCsvLine(line);
                if (firstRow) {
                    expectedColumns = values.size();
                    for (String header : values) {
                        model.addColumn(header);
                    }
                    firstRow = false;
                } else {
                    while (values.size() < expectedColumns) values.add("");
                    if (values.size() > expectedColumns) {
                        throw new IOException("CSV row has more columns than the header.");
                    }
                    model.addRow(values.toArray());
                }
            }
        }
        if (model.getColumnCount() == 0) {
            throw new IOException("The CSV file is empty.");
        }
        return model;
    }

    /** Handles commas and escaped quotes inside quoted CSV fields. */
    private List<String> parseCsvLine(String line) throws IOException {
        List<String> values = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(field.toString());
                field.setLength(0);
            } else {
                field.append(ch);
            }
        }
        if (quoted) throw new IOException("Invalid CSV: unclosed quoted field.");
        values.add(field.toString());
        return values;
    }

    private String money(BigDecimal amount) {
        return amount.toPlainString();
    }

    private String escape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"")
                || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
