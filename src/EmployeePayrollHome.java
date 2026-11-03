import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/** Employee-only home page. The logged-in employee can view only their own profile and payslips. */
@SuppressWarnings({"serial", "this-escape"})
public class EmployeePayrollHome extends JFrame {
    private static final Color NAVY = new Color(16, 43, 80);
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private final String employeeId;
    private final Employee employee;
    private final DefaultTableModel payslipModel = new DefaultTableModel(
            new String[]{"Payroll ID", "Payroll Date", "Cutoff", "Gross Pay", "Deductions", "Net Pay"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public EmployeePayrollHome(String employeeId) {
        this.employeeId = employeeId;
        EmployeeCSV employeeCSV = new EmployeeCSV();
        Employee found = null;
        for (Employee item : employeeCSV.getEmployees()) {
            if (item.getEmployeeNumber().equalsIgnoreCase(employeeId)) found = item;
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
            addDetail(profile, "Full Name", employee.getFirstName() + " " + employee.getLastName());
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
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) showSelectedPayslip(payslipTable);
            }
        });
        JPanel payslips = new JPanel(new BorderLayout());
        payslips.setBackground(Color.WHITE);
        payslips.setBorder(BorderFactory.createTitledBorder("My Payslips"));
        payslips.add(new JScrollPane(payslipTable), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh Payslips");
        refresh.addActionListener(e -> loadPayslips());
        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> { dispose(); new Login().setVisible(true); });
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

    private void showSelectedPayslip(JTable table) {
        int selected = table.getSelectedRow();
        if (selected < 0) return;
        int modelRow = table.convertRowIndexToModel(selected);
        String payrollId = payslipModel.getValueAt(modelRow, 0).toString();
        File file = new File("data/Payroll.csv");
        try (FileReader reader = new FileReader(file)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);
            int row = 0;
            StringBuilder details = new StringBuilder();
            for (CSVRecord record : records) {
                if (row++ == 0 || record.size() < 23) continue;
                if (record.get(0).equals(payrollId)
                        && record.get(1).equalsIgnoreCase(employeeId)) {
                    String[] labels = {"Payroll ID", "Employee Number", "First Name", "Last Name",
                            "Position", "Employment Status", "Payroll Date", "Cutoff", "Regular Hours",
                            "Overtime Hours", "Basic Salary", "Overtime Pay", "Allowances", "Other Income",
                            "Total Earnings", "SSS", "PhilHealth", "Pag-IBIG", "Withholding Tax",
                            "Loan Deduction", "Other Deductions", "Total Deductions", "Net Pay"};
                    for (int i = 0; i < labels.length; i++) {
                        details.append(labels[i]).append(": ").append(record.get(i)).append("\n");
                    }
                    break;
                }
            }
            JOptionPane.showMessageDialog(this, details.length() == 0
                    ? "Payslip record was not found." : details.toString(),
                    "Payslip Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open payslip: " + ex.getMessage(),
                    "Payslip Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addDetail(JPanel panel, String label, String value) {
        panel.add(new JLabel(label + ":"));
        panel.add(new JLabel(value == null ? "" : value));
    }

    private void loadPayslips() {
        payslipModel.setRowCount(0);
        File file = new File("data/Payroll.csv");
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);
            int row = 0;
            for (CSVRecord record : records) {
                if (row++ == 0 || record.size() < 23) continue;
                if (!record.get(1).trim().equalsIgnoreCase(employeeId)) continue;
                payslipModel.addRow(new Object[]{record.get(0), record.get(6), record.get(7),
                        record.get(14), record.get(21), record.get(22)});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load data/Payroll.csv: " + ex.getMessage(),
                    "Payslip Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
