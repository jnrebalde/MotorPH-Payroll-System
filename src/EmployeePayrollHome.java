
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Employee-only home page.
 * The logged-in employee can view only their own profile and payslips.
 */
@SuppressWarnings({"serial", "this-escape"})
public class EmployeePayrollHome extends JFrame {

    private static final Color NAVY = new Color(16, 43, 80);
    private static final Color BACKGROUND = new Color(245, 247, 250);

    private final String employeeId;
    private final Employee employee;
    private final PayrollCSV payrollCSV = new PayrollCSV();
    private final Path payrollFile = Path.of("data", "Payroll.csv");

    private List<PayrollRecord> employeePayrollRecords;

    private final DefaultTableModel payslipModel = new DefaultTableModel(
            new String[]{"Payroll ID", "Payroll Date", "Cutoff",
                    "Gross Pay", "Deductions", "Net Pay"}, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public EmployeePayrollHome(String employeeId) {
        this.employeeId = employeeId;

        EmployeeCSV employeeCSV = new EmployeeCSV();
        Employee found = null;

        for (Employee item : employeeCSV.getEmployees()) {
            if (item.getEmployeeNumber().equalsIgnoreCase(employeeId)) {
                found = item;
                break;
            }
        }

        employee = found;

        setTitle("MotorPH Payroll System - Employee Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("  MOTORPH  |  EMPLOYEE PAYROLL HOME");
        header.setOpaque(true);
        header.setBackground(NAVY);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setBorder(new EmptyBorder(18, 12, 18, 12));
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 16));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel profile = new JPanel(new GridLayout(0, 2, 12, 10));
        profile.setBackground(Color.WHITE);
        profile.setBorder(BorderFactory.createTitledBorder("My Employee Record"));

        if (employee == null) {
            profile.add(new JLabel("Employee record not found for: " + employeeId));
        } else {
            addDetail(profile, "Employee Number", employee.getEmployeeNumber());
            addDetail(profile, "Full Name",
                    employee.getFirstName() + " " + employee.getLastName());
            addDetail(profile, "Position", employee.getPosition());
            addDetail(profile, "Employment Status", employee.getEmploymentStatus());
            addDetail(profile, "Birth Date", employee.getBirthDate().toString());
            addDetail(profile, "Contact Number", employee.getContactNo());
            addDetail(profile, "Address", employee.getAddress());
            addDetail(profile, "Supervisor", employee.getSupervisor());
        }

        JTable payslipTable = new JTable(payslipModel);
        payslipTable.setRowHeight(28);
        payslipTable.setAutoCreateRowSorter(true);
        payslipTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        payslipTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    showSelectedPayslip(payslipTable);
                }
            }
        });

        JPanel payslips = new JPanel(new BorderLayout());
        payslips.setBackground(Color.WHITE);
        payslips.setBorder(BorderFactory.createTitledBorder("My Payslips"));
        payslips.add(new JScrollPane(payslipTable), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh Payslips");
        refresh.addActionListener(e -> loadPayslips());

        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> {
            dispose();
            new Login().setVisible(true);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(refresh);
        actions.add(logout);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(profile, BorderLayout.NORTH);
        center.add(payslips, BorderLayout.CENTER);

        content.add(center, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);

        loadPayslips();
    }

    /**
     * Loads the selected employee's saved payroll records.
     */
    private void loadPayslips() {
        payslipModel.setRowCount(0);

        try {
            EmployeeCSV employeeCSV = new EmployeeCSV();

            employeePayrollRecords = payrollCSV.getPayrollRecordsByEmployee(
                    payrollFile,
                    employeeId,
                    employeeCSV.getEmployees()
            );

            for (PayrollRecord record : employeePayrollRecords) {
                payslipModel.addRow(new Object[]{
                        record.getPayrollID(),
                        record.getPayrollDate(),
                        record.getCutoff(),
                        record.getTotalEarnings(),
                        record.getTotalDeductions(),
                        record.getNetPay()
                });
            }

        } catch (Exception ex) {
            employeePayrollRecords = null;

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load payslips: " + ex.getMessage(),
                    "Payslip Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Displays the complete details of the selected payroll record.
     */
    private void showSelectedPayslip(JTable table) {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0 || employeePayrollRecords == null) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        if (modelRow < 0 || modelRow >= employeePayrollRecords.size()) {
            return;
        }

        PayrollRecord record = employeePayrollRecords.get(modelRow);

        StringBuilder details = new StringBuilder();

        details.append("Payroll ID: ").append(record.getPayrollID()).append("\n");
        details.append("Employee Number: ")
                .append(record.getEmployee().getEmployeeNumber()).append("\n");
        details.append("Employee Name: ")
                .append(record.getEmployee().getFirstName()).append(" ")
                .append(record.getEmployee().getLastName()).append("\n");
        details.append("Payroll Date: ").append(record.getPayrollDate()).append("\n");
        details.append("Cutoff: ").append(record.getCutoff()).append("\n\n");

        details.append("EARNINGS\n");
        details.append("Basic Salary: ").append(record.getBasicSalary()).append("\n");
        details.append("Overtime Hours: ").append(record.getOvertimeHours()).append("\n");
        details.append("Overtime Pay: ").append(record.getOvertimePay()).append("\n");
        details.append("Allowances: ").append(record.getAllowances()).append("\n");
        details.append("Other Income: ").append(record.getOtherIncome()).append("\n");
        details.append("Total Earnings: ").append(record.getTotalEarnings()).append("\n\n");

        details.append("DEDUCTIONS\n");
        details.append("SSS: ").append(record.getSSSDeduction()).append("\n");
        details.append("PhilHealth: ").append(record.getPhilHealthDeduction()).append("\n");
        details.append("Pag-IBIG: ").append(record.getPagIBIGDeduction()).append("\n");
        details.append("Withholding Tax: ").append(record.getWithholdingTax()).append("\n");
        details.append("Loan Deduction: ").append(record.getLoanDeduction()).append("\n");
        details.append("Other Deductions: ").append(record.getOtherDeductions()).append("\n");
        details.append("Total Deductions: ").append(record.getTotalDeductions()).append("\n\n");

        details.append("NET PAY\n");
        details.append("Net Pay: ").append(record.getNetPay());

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(480, 480));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Payslip Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void addDetail(JPanel panel, String label, String value) {
        panel.add(new JLabel(label + ":"));
        panel.add(new JLabel(value == null ? "" : value));
    }
}
