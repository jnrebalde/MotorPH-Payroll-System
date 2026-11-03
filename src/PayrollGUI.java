import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;

@SuppressWarnings({"serial", "this-escape"})
public class PayrollGUI extends JFrame {

    // Navy-blue theme
    private static final Color NAVY = new Color(16, 43, 80);
    private static final Color SIDEBAR = new Color(23, 59, 104);
    private static final Color ACTIVE = new Color(49, 95, 155);
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color MUTED = new Color(103, 116, 136);
    private static final Color BORDER = new Color(220, 226, 234);
    private static final NumberFormat PHP =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"));

    private final boolean admin;
    private final PayrollCSV csvManager = new PayrollCSV();
    private List<PayrollRecord> payrollRecords = new ArrayList<>();

    private final DefaultListModel<PayrollRecord> employeeListModel =
            new DefaultListModel<>();
    private final JList<PayrollRecord> employeeList =
            new JList<>(employeeListModel);

    private final JTextField searchField = new JTextField();
    private final JLabel selectedName = new JLabel("No employee selected");
    private final JLabel selectedMeta =
            new JLabel("Import or calculate payroll to get started");
    private final JLabel grossValue = valueLabel();
    private final JLabel deductionValue = valueLabel();
    private final JLabel netValue = valueLabel();
    private final JLabel periodLabel = new JLabel("Payroll period: —");

    private final DefaultTableModel payrollTableModel = new DefaultTableModel(
            new String[]{"Payroll ID", "Employee No.", "Employee Name", "Date", "Cutoff",
                    "Gross Pay", "Deductions", "Net Pay"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable payrollTable = new JTable(payrollTableModel);

    private final JPanel contentPanel = new JPanel(new BorderLayout());
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pages = new JPanel(pageLayout);

    private final EmployeeGUI employeeManagementPanel =
            new EmployeeGUI();
    private final AttendanceGUI attendanceManagementPanel =
            new AttendanceGUI();
    private final JPanel compensationPanel = detailPanel("Compensation");
    private final JPanel deductionsPanel = detailPanel("Deductions");

    private final JButton dashboardButton = navigationButton("Dashboard");
    private final JButton employeeButton = navigationButton("Employee");
    private final JButton payrollButton = navigationButton("Payroll");
    private final JButton attendanceButton = navigationButton("Attendance");
    private final JButton leaveButton = navigationButton("Leave");
    private final JButton reportsButton = navigationButton("Reports");
    private final JButton usersButton = navigationButton("Users");
    private final UserCSV userCSV = new UserCSV();
    private final DefaultTableModel userTableModel = new DefaultTableModel(
            new String[]{"Employee ID", "Full Name", "Email", "Username"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable userTable = new JTable(userTableModel);

    public PayrollGUI(boolean admin) {
        this.admin = admin;
        setTitle("MotorPH Payroll System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1380, 850);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildPages(), BorderLayout.CENTER);

        employeeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeList.setCellRenderer(new EmployeeRenderer());
        employeeList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showRecord(employeeList.getSelectedValue());
            }
        });

        searchField.setToolTipText("Search employee number or name");
        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    @Override
                    public void insertUpdate(javax.swing.event.DocumentEvent e) {
                        filterEmployees();
                    }

                    @Override
                    public void removeUpdate(javax.swing.event.DocumentEvent e) {
                        filterEmployees();
                    }

                    @Override
                    public void changedUpdate(javax.swing.event.DocumentEvent e) {
                        filterEmployees();
                    }
                });

        showEmptyState();
        showPage("Employee");
        setActiveNavigation(employeeButton);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(12, 24, 12, 24));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);

        JLabel logo = new JLabel("M");
        logo.setOpaque(true);
        logo.setBackground(NAVY);
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 25));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(46, 46));

        JLabel title = new JLabel("MotorPH Payroll System");
        title.setFont(new Font("SansSerif", Font.PLAIN, 25));
        title.setForeground(NAVY);

        brand.add(logo);
        brand.add(title);

        periodLabel.setForeground(NAVY);
        periodLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        header.add(brand, BorderLayout.WEST);
        header.add(periodLabel, BorderLayout.EAST);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 24, 12, 24)));

        return header;
    }

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 18, 12));

        JLabel heading = new JLabel("EMPLOYEE");
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setBorder(new EmptyBorder(4, 12, 20, 4));

        JPanel navigation = new JPanel();
        navigation.setOpaque(false);
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));

        addNavigation(navigation, employeeButton, "Employee");
        addNavigation(navigation, payrollButton, "Payroll");
        addNavigation(navigation, attendanceButton, "Attendance");
        addNavigation(navigation, leaveButton, "Leave");
        addNavigation(navigation, reportsButton, "Reports");
        if (admin) addNavigation(navigation, usersButton, "Users");

        employeeButton.addActionListener(e -> navigate("Employee", employeeButton));
        payrollButton.addActionListener(e -> navigate("Payroll", payrollButton));
        attendanceButton.addActionListener(e -> navigate("Attendance", attendanceButton));
        leaveButton.addActionListener(e -> navigate("Leave", leaveButton));
        reportsButton.addActionListener(e -> navigate("Reports", reportsButton));
        usersButton.addActionListener(e -> {
            navigate("Users", usersButton);
            refreshUserTable();
        });

        JButton logoutButton = navigationButton("Log out");
        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new Login().setVisible(true));
        });

        sidebar.add(heading, BorderLayout.NORTH);
        sidebar.add(navigation, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(12, 0, 0, 0));
        bottom.add(logoutButton, BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JComponent buildUsersPage() {
        JPanel page = new JPanel(new BorderLayout(0, 15));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel title = new JLabel("User Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Manage login accounts linked to existing employees");
        subtitle.setForeground(MUTED);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        JButton add = new JButton("Add User");
        JButton update = new JButton("Update User");
        JButton delete = new JButton("Delete User");
        JButton reload = new JButton("Reload CSV");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        actions.add(add);
        actions.add(update);
        actions.add(delete);
        actions.add(reload);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(actions, BorderLayout.SOUTH);
        page.add(top, BorderLayout.NORTH);

        userTable.setRowHeight(28);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setAutoCreateRowSorter(true);
        page.add(new JScrollPane(userTable), BorderLayout.CENTER);

        add.addActionListener(e -> showUserForm(null));
        update.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0) { showUserMessage("Select a user first."); return; }
            row = userTable.convertRowIndexToModel(row);
            showUserForm(userTableModel.getValueAt(row, 3).toString());
        });
        delete.addActionListener(e -> deleteSelectedUser());
        reload.addActionListener(e -> refreshUserTable());
        refreshUserTable();
        return page;
    }

    private void refreshUserTable() {
        userTableModel.setRowCount(0);
        for (String[] user : userCSV.readUsers()) {
            userTableModel.addRow(new Object[]{user[0], user[1], user[2], user[3]});
        }
    }

    private void showUserForm(String originalUsername) {
        if (!admin) { showUserMessage("Only the administrator can manage users."); return; }

        String[] selected = null;
        if (originalUsername != null) {
            for (String[] user : userCSV.readUsers()) {
                if (user[3].equalsIgnoreCase(originalUsername)) selected = user;
            }
            if (selected == null) { showUserMessage("User account was not found."); return; }
        }

        String employeeId = selected == null ? JOptionPane.showInputDialog(this,
                "Enter an existing Employee Number:", "Verify Employee", JOptionPane.QUESTION_MESSAGE)
                : selected[0];
        if (employeeId == null) return;
        employeeId = employeeId.trim();
        if (employeeId.isEmpty()) { showUserMessage("Employee number is required."); return; }

        EmployeeCSV employeeCSV = new EmployeeCSV();
        employeeCSV.readCSV();
        Employee matchedEmployee = null;
        for (Employee employee : employeeCSV.getEmployees()) {
            if (employee.getEmployeeNumber().equalsIgnoreCase(employeeId)) {
                matchedEmployee = employee;
                break;
            }
        }
        if (matchedEmployee == null) {
            showUserMessage("Employee number does not exist in employees.csv. Create the employee record first.");
            return;
        }

        String employeeName = matchedEmployee.getFirstName() + " " + matchedEmployee.getLastName();
        JTextField email = new JTextField(selected == null ? "" : selected[2], 20);
        JTextField username = new JTextField(selected == null ? "" : selected[3], 20);
        JPasswordField password = new JPasswordField(selected == null ? "" : selected[4], 20);
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 10));
        form.add(new JLabel("Employee Number:")); form.add(new JLabel(matchedEmployee.getEmployeeNumber()));
        form.add(new JLabel("Employee Name:")); form.add(new JLabel(employeeName));
        form.add(new JLabel("Email:")); form.add(email);
        form.add(new JLabel("Username:")); form.add(username);
        form.add(new JLabel("Password:")); form.add(password);
        int choice = JOptionPane.showConfirmDialog(this, form,
                originalUsername == null ? "Add User" : "Update User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String result;
        if (originalUsername == null) {
            result = userCSV.addUser(employeeId, employeeName, email.getText(),
                    username.getText(), new String(password.getPassword()));
        } else {
            result = userCSV.updateUser(originalUsername, employeeId, employeeName,
                    email.getText(), username.getText(), new String(password.getPassword()));
        }
        if ("SUCCESS".equals(result)) {
            refreshUserTable();
            showUserMessage("User account saved successfully.");
        } else showUserMessage(result);
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) { showUserMessage("Select a user first."); return; }
        row = userTable.convertRowIndexToModel(row);
        String username = userTableModel.getValueAt(row, 3).toString();
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete the login account for " + username + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            String result = userCSV.deleteUser(username);
            if ("SUCCESS".equals(result)) refreshUserTable();
            else showUserMessage(result);
        }
    }

    private void showUserMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "User Management",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void addNavigation(JPanel panel, JButton button, String text) {
        button.setText("   " + text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(button);
        panel.add(Box.createVerticalStrut(12));
    }

    private JButton navigationButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR);
        button.setBorder(new EmptyBorder(13, 12, 13, 8));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void navigate(String page, JButton selectedButton) {
        showPage(page);
        setActiveNavigation(selectedButton);
        if ("Payroll".equals(page)) {
            refreshPayrollTable();
        }
    }

    private void setActiveNavigation(JButton selected) {
        JButton[] buttons = {
                employeeButton, payrollButton, attendanceButton,
                leaveButton, reportsButton, usersButton
        };

        for (JButton button : buttons) {
            button.setBackground(button == selected ? ACTIVE : SIDEBAR);
        }
    }

    private JComponent buildPages() {
        pages.setBackground(BACKGROUND);
        pages.add(buildEmployeePage(), "Employee");
        pages.add(buildPayrollPage(), "Payroll");
        pages.add(attendanceManagementPanel, "Attendance");
        pages.add(new LeaveGUI(), "Leave");
        if (admin) pages.add(buildUsersPage(), "Users");
        pages.add(buildReportsPage(), "Reports");

        contentPanel.setBackground(BACKGROUND);
        contentPanel.add(pages, BorderLayout.CENTER);
        return contentPanel;
    }

    private JComponent buildEmployeePage() {
        return employeeManagementPanel;
    }

    private JComponent buildPayrollPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel title = new JLabel("Payroll Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(NAVY);

        JButton calculateButton = new JButton("Calculate Payroll");
        calculateButton.addActionListener(e -> calculatePayroll());

        JButton viewPayslip = new JButton("View Payslip");
        viewPayslip.addActionListener(e -> viewPayrollPayslip());

        JButton exportButton = new JButton("Export Payroll CSV");
        exportButton.addActionListener(e -> exportPayroll());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(calculateButton);
        actions.add(viewPayslip);
        actions.add(exportButton);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(actions, BorderLayout.SOUTH);

        payrollTable.setRowHeight(26);
        payrollTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        payrollTable.setAutoCreateRowSorter(true);

        page.add(top, BorderLayout.NORTH);
        page.add(new JScrollPane(payrollTable), BorderLayout.CENTER);
        refreshPayrollTable();
        return page;
    }

    private void calculatePayroll() {
        EmployeeCSV employeeCSV = new EmployeeCSV();
        AttendanceCSV attendanceCSV = new AttendanceCSV();
        LeaveCSV leaveCSV = new LeaveCSV();
        ArrayList<Employee> employees = employeeCSV.getEmployees();
        attendanceCSV.readCSV(employees);
        leaveCSV.readCSV(employees);

        if (employees.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No employees were found in data/employees.csv.");
            return;
        }

        JComboBox<Employee> employeeChoice = new JComboBox<>(employees.toArray(new Employee[0]));
        JComboBox<PayrollCutoff> cutoffChoice = new JComboBox<>(PayrollCutoff.values());
        JComboBox<Month> monthChoice = new JComboBox<>(Month.values());
        monthChoice.setSelectedItem(LocalDate.now().getMonth());
        monthChoice.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Month) {
                    setText(((Month) value).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
                }
                return this;
            }
        });
        int selectedYear = LocalDate.now().getYear();
        JTextField startField = new JTextField();
        JTextField endField = new JTextField();
        JTextField payrollDateField = new JTextField(LocalDate.now().toString());
        JTextField otherIncomeField = new JTextField("0.00");
        JTextField loanField = new JTextField("0.00");
        JTextField otherDeductionField = new JTextField("0.00");

        Runnable updateCutoffDates = () -> {
            Month selectedMonth = (Month) monthChoice.getSelectedItem();
            PayrollCutoff selected = (PayrollCutoff) cutoffChoice.getSelectedItem();
            LocalDate periodDate = LocalDate.of(selectedYear, selectedMonth, 1);
            if (selected == PayrollCutoff.FIRST) {
                startField.setText(periodDate.toString());
                endField.setText(periodDate.withDayOfMonth(15).toString());
            } else {
                startField.setText(periodDate.withDayOfMonth(16).toString());
                endField.setText(periodDate.withDayOfMonth(periodDate.lengthOfMonth()).toString());
            }
            // Default payroll date to the day after the cutoff ends.
            payrollDateField.setText(LocalDate.parse(endField.getText()).plusDays(1).toString());
        };
        cutoffChoice.addActionListener(e -> updateCutoffDates.run());
        monthChoice.addActionListener(e -> updateCutoffDates.run());
        updateCutoffDates.run();

        // Keep the employee dropdown compact. Employee.toString() contains the
        // full employee profile, which can make Swing expand the dialog off-screen.
        employeeChoice.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Employee) {
                    Employee selectedEmployee = (Employee) value;
                    setText(selectedEmployee.getEmployeeNumber() + " - "
                            + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());
                }
                return this;
            }
        });
        employeeChoice.setPreferredSize(new Dimension(300, 28));
        cutoffChoice.setPreferredSize(new Dimension(300, 28));
        monthChoice.setPreferredSize(new Dimension(300, 28));

        JTextField[] inputFields = {startField, endField, payrollDateField,
                otherIncomeField, loanField, otherDeductionField};
        for (JTextField field : inputFields) {
            field.setColumns(18);
            field.setPreferredSize(new Dimension(300, 28));
            field.setBackground(Color.WHITE);
            field.setForeground(Color.BLACK);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(170, 180, 195)),
                    BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 14, 12, 14));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 6, 5, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0;
        int row = 0;
        addPayrollFormRow(form, constraints, row++, "Employee", employeeChoice);
        addPayrollFormRow(form, constraints, row++, "Payroll month", monthChoice);
        addPayrollFormRow(form, constraints, row++, "Payroll cutoff", cutoffChoice);
        addPayrollFormRow(form, constraints, row++, "Cutoff start (YYYY-MM-DD)", startField);
        addPayrollFormRow(form, constraints, row++, "Cutoff end (YYYY-MM-DD)", endField);
        addPayrollFormRow(form, constraints, row++, "Payroll date (YYYY-MM-DD)", payrollDateField);
        addPayrollFormRow(form, constraints, row++, "Other earnings (allowance / incentives / others)", otherIncomeField);
        addPayrollFormRow(form, constraints, row++, "Loan deduction", loanField);
        addPayrollFormRow(form, constraints, row, "Other deductions", otherDeductionField);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setPreferredSize(new Dimension(650, 470));
        int answer = JOptionPane.showConfirmDialog(this, formScroll,
                "Calculate Semi-Monthly Payroll", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;

        try {
            Employee employee = (Employee) employeeChoice.getSelectedItem();
            PayrollCutoff cutoff = (PayrollCutoff) cutoffChoice.getSelectedItem();
            LocalDate cutoffStart = LocalDate.parse(startField.getText().trim());
            LocalDate cutoffEnd = LocalDate.parse(endField.getText().trim());
            LocalDate payrollDate = LocalDate.parse(payrollDateField.getText().trim());
            validateCutoffDates(cutoff, cutoffStart, cutoffEnd);
            if (!payrollDate.isAfter(cutoffEnd)) {
                throw new IllegalArgumentException(
                        "Payroll date must be after the payroll cutoff end date (" + cutoffEnd + ").");
            }

            BigDecimal otherIncome = amount(otherIncomeField.getText());
            BigDecimal loanAmount = amount(loanField.getText());
            BigDecimal otherDeductions = amount(otherDeductionField.getText());

            // Regular employees are expected to work Monday through Friday.
            // Missing weekdays are treated as absences unless covered by approved leave.
            int expectedWorkdays = 0;
            int daysWorked = countDaysWorked(employee, attendanceCSV.getAttendanceRecords(), cutoffStart, cutoffEnd);
            int approvedLeaveDays = 0;
            int absentDays = 0;
            BigDecimal absenceDeduction = BigDecimal.ZERO;

            if (employee instanceof RegularEmployee) {
                int attendanceDaysInCutoff = countAttendanceRecords(employee,
                        attendanceCSV.getAttendanceRecords(), cutoffStart, cutoffEnd);
                if (attendanceDaysInCutoff == 0) {
                    throw new IllegalArgumentException("No attendance records were found for this regular employee in the selected cutoff. Check the attendance CSV before calculating payroll.");
                }

                expectedWorkdays = countWeekdays(cutoffStart, cutoffEnd);
                approvedLeaveDays = countApprovedLeaveWeekdays(employee,
                        leaveCSV.getLeaveRequests(), cutoffStart, cutoffEnd,
                        attendanceCSV.getAttendanceRecords());
                absentDays = Math.max(0, expectedWorkdays - countWeekdayAttendance(employee,
                        attendanceCSV.getAttendanceRecords(), cutoffStart, cutoffEnd) - approvedLeaveDays);

                RegularEmployee regular = (RegularEmployee) employee;
                BigDecimal dailyRate = regular.getHourlyRate().multiply(BigDecimal.valueOf(8));
                absenceDeduction = dailyRate.multiply(BigDecimal.valueOf(absentDays))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                otherDeductions = otherDeductions.add(absenceDeduction);
            }

            BigDecimal hourlyRate;
            BigDecimal monthlySalary = BigDecimal.ZERO;
            if (employee instanceof RegularEmployee) {
                RegularEmployee regular = (RegularEmployee) employee;
                monthlySalary = regular.getMonthlySalary();
                hourlyRate = regular.getHourlyRate();
            } else if (employee instanceof ContractEmployee) {
                hourlyRate = ((ContractEmployee) employee).getHourlyRate();
            } else {
                throw new IllegalArgumentException("Unsupported employee type.");
            }

            // Overtime is derived directly from attendance records.
            // Sunday overtime uses the Sunday multiplier; other overtime uses the regular multiplier.
            BigDecimal regularOvertimeHours = BigDecimal.ZERO;
            BigDecimal sundayOvertimeHours = BigDecimal.ZERO;
            for (AttendanceRecord attendance : attendanceCSV.getAttendanceRecords()) {
                if (attendance.getEmployee().getEmployeeNumber().equals(employee.getEmployeeNumber())
                        && !attendance.getDate().isBefore(cutoffStart)
                        && !attendance.getDate().isAfter(cutoffEnd)) {
                    BigDecimal attendanceOvertime = attendance.calculateOvertimeHours();
                    if (attendance.getDate().getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                        sundayOvertimeHours = sundayOvertimeHours.add(attendanceOvertime);
                    } else {
                        regularOvertimeHours = regularOvertimeHours.add(attendanceOvertime);
                    }
                }
            }
            BigDecimal regularOTRate = hourlyRate.multiply(new BigDecimal("1.25"));
            BigDecimal sundayOTRate = hourlyRate.multiply(new BigDecimal("1.30"));
            BigDecimal overtimePay = regularOTRate.multiply(regularOvertimeHours)
                    .add(sundayOTRate.multiply(sundayOvertimeHours))
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            // Statutory contributions are deducted on the second cutoff only.
            Deduction sss = monthlySalary.signum() > 0
                    ? new SSSDeduction("SSS", "Employee SSS contribution", monthlySalary) : null;
            Deduction philHealth = monthlySalary.signum() > 0
                    ? new PhilHealthDeduction("PhilHealth", "Employee PhilHealth contribution", monthlySalary) : null;
            Deduction pagIBIG = monthlySalary.signum() > 0
                    ? new PagIBIGDeduction("Pag-IBIG", "Employee Pag-IBIG contribution", monthlySalary) : null;

            BigDecimal basicPayForTax;
            if (employee instanceof RegularEmployee) {
                basicPayForTax = monthlySalary.divide(BigDecimal.valueOf(2), 2,
                        java.math.RoundingMode.HALF_UP);
            } else {
                basicPayForTax = ((ContractEmployee) employee).getDailyRate()
                        .multiply(BigDecimal.valueOf(countDaysWorked(employee,
                                attendanceCSV.getAttendanceRecords(), cutoffStart, cutoffEnd)));
            }
            BigDecimal taxablePay = basicPayForTax
                    .add(employee.calculateAllowances(cutoff))
                    .add(overtimePay).add(otherIncome)
                    .subtract(absenceDeduction).max(BigDecimal.ZERO);
            Deduction tax = new WithholdingTaxDeduction("Withholding Tax",
                    "Estimated withholding tax based on this cutoff's earnings", taxablePay);
            Deduction loan = loanAmount.signum() > 0
                    ? new Deduction("Loan", "User-entered loan deduction") {
                @Override public BigDecimal computeDeduction() { return loanAmount; }
            } : null;

            String payrollID = "PR-" + employee.getEmployeeNumber() + "-"
                    + cutoffStart.toString().replace("-", "") + "-" + cutoff;
            PayrollCalculator calculator = new PayrollCalculator();
            PayrollRecord record = calculator.calculatePayroll(
                    payrollID, employee, payrollDate, cutoff, cutoffStart, cutoffEnd,
                    attendanceCSV.getAttendanceRecords(), overtimePay, otherIncome,
                    sss, philHealth, pagIBIG, tax, loan, otherDeductions);

            for (PayrollRecord existing : payrollRecords) {
                if (existing.getPayrollID().equalsIgnoreCase(payrollID)) {
                    throw new IllegalArgumentException("A payroll record already exists for this employee and cutoff.");
                }
            }

            // Save the calculated payroll immediately to the permanent CSV register.
            // The record is added to the screen only after the file write succeeds.
            Path payrollFile = Path.of("data", "Payroll.csv");
            csvManager.appendPayrollRecord(record, payrollFile);

            payrollRecords.add(record);
            refreshPayrollTable();
            rebuildEmployeeList();
            String basePayLabel = employee instanceof RegularEmployee
                    ? "Semi-monthly gross rate" : "Daily rate x " + daysWorked + " attendance day(s)";
            String absenceSummary = employee instanceof RegularEmployee
                    ? "\nExpected Monday-Friday workdays: " + expectedWorkdays
                      + "\nApproved leave days: " + approvedLeaveDays
                      + "\nAbsences: " + absentDays
                      + "\nAbsence deduction: " + PHP.format(absenceDeduction)
                    : "";
            JOptionPane.showMessageDialog(this,
                    "Payroll calculated.\n" + basePayLabel + ": " + PHP.format(record.getBasicSalary())
                            + absenceSummary
                            + "\nOvertime pay: " + PHP.format(record.getOvertimePay())
                            + "\nGross pay: " + PHP.format(record.getTotalEarnings())
                            + "\nDeductions: " + PHP.format(record.getTotalDeductions())
                            + "\nNet pay: " + PHP.format(record.getNetPay())
                            + "\n\nUse Export Payroll CSV to save the records.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to calculate payroll: " + ex.getMessage(),
                    "Payroll Calculation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPayrollFormRow(JPanel form, GridBagConstraints constraints,
                                   int row, String labelText, Component input) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        form.add(new JLabel(labelText), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(input, constraints);
    }

    private void validateCutoffDates(PayrollCutoff cutoff, LocalDate start, LocalDate end) {
        if (cutoff == null || start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Enter a valid payroll cutoff date range.");
        }
        if (cutoff == PayrollCutoff.FIRST
                && (start.getDayOfMonth() != 1 || end.getDayOfMonth() != 15
                || !start.getMonth().equals(end.getMonth()) || start.getYear() != end.getYear())) {
            throw new IllegalArgumentException("First cutoff must cover the 1st through 15th of the same month.");
        }
        if (cutoff == PayrollCutoff.SECOND
                && (start.getDayOfMonth() != 16 || end.getDayOfMonth() != end.lengthOfMonth()
                || !start.getMonth().equals(end.getMonth()) || start.getYear() != end.getYear())) {
            throw new IllegalArgumentException("Second cutoff must cover the 16th through the last day of the same month.");
        }
    }

    private int countAttendanceRecords(Employee employee, List<AttendanceRecord> records,
                                       LocalDate start, LocalDate end) {
        int count = 0;
        for (AttendanceRecord record : records) {
            if (record != null
                    && record.getEmployee().getEmployeeNumber().equals(employee.getEmployeeNumber())
                    && !record.getDate().isBefore(start) && !record.getDate().isAfter(end)) {
                count++;
            }
        }
        return count;
    }

    private int countWeekdays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    private int countWeekdayAttendance(Employee employee, List<AttendanceRecord> records,
                                       LocalDate start, LocalDate end) {
        java.util.Set<LocalDate> days = new java.util.HashSet<>();
        for (AttendanceRecord record : records) {
            if (record != null
                    && record.getEmployee().getEmployeeNumber().equals(employee.getEmployeeNumber())
                    && !record.getDate().isBefore(start) && !record.getDate().isAfter(end)
                    && record.getDate().getDayOfWeek() != java.time.DayOfWeek.SATURDAY
                    && record.getDate().getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
                days.add(record.getDate());
            }
        }
        return days.size();
    }

    private int countApprovedLeaveWeekdays(Employee employee, List<LeaveRequest> leaves,
                                           LocalDate start, LocalDate end,
                                           List<AttendanceRecord> attendance) {
        java.util.Set<LocalDate> approvedDays = new java.util.HashSet<>();
        for (LeaveRequest leave : leaves) {
            if (leave == null
                    || !leave.getEmployee().getEmployeeNumber().equals(employee.getEmployeeNumber())
                    || !"APPROVED".equalsIgnoreCase(leave.getLeaveStatus())) {
                continue;
            }
            LocalDate leaveStart = leave.getStartDate().isBefore(start) ? start : leave.getStartDate();
            LocalDate leaveEnd = leave.getEndDate().isAfter(end) ? end : leave.getEndDate();
            for (LocalDate date = leaveStart; !date.isAfter(leaveEnd); date = date.plusDays(1)) {
                if (date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
                    approvedDays.add(date);
                }
            }
        }
        // A day with an attendance record is worked, not an absence/leave day.
        for (AttendanceRecord record : attendance) {
            if (record != null
                    && record.getEmployee().getEmployeeNumber().equals(employee.getEmployeeNumber())) {
                approvedDays.remove(record.getDate());
            }
        }
        return approvedDays.size();
    }

    private int countDaysWorked(Employee employee, List<AttendanceRecord> records,
                                LocalDate start, LocalDate end) {
        java.util.Set<LocalDate> days = new java.util.HashSet<>();
        for (AttendanceRecord record : records) {
            if (record != null
                    && record.getEmployee().getEmployeeNumber().equals(employee.getEmployeeNumber())
                    && !record.getDate().isBefore(start) && !record.getDate().isAfter(end)) {
                days.add(record.getDate());
            }
        }
        return days.size();
    }

    private BigDecimal amount(String value) {
        BigDecimal amount = new BigDecimal(value.trim());
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amounts cannot be negative.");
        }
        return amount;
    }

    private void refreshPayrollTable() {
        payrollTableModel.setRowCount(0);

        Path payrollFile = Path.of("data", "Payroll.csv");
        if (!java.nio.file.Files.exists(payrollFile)) {
            return;
        }

        try {
            DefaultTableModel savedRecords = csvManager.readCSV(payrollFile);
            EmployeeCSV employeeCSV = new EmployeeCSV();
            ArrayList<Employee> employees = employeeCSV.getEmployees();
            payrollRecords = csvManager.loadPayrollRecords(payrollFile, employees);
            int payrollIdColumn = findPayrollColumn(savedRecords, "Payroll ID");
            int employeeNumberColumn = findPayrollColumn(savedRecords, "Employee Number");
            int firstNameColumn = findPayrollColumn(savedRecords, "First Name");
            int lastNameColumn = findPayrollColumn(savedRecords, "Last Name");
            int payrollDateColumn = findPayrollColumn(savedRecords, "Payroll Date");
            int cutoffColumn = findPayrollColumn(savedRecords, "Cutoff");
            int earningsColumn = findPayrollColumn(savedRecords, "Total Earnings");
            int deductionsColumn = findPayrollColumn(savedRecords, "Total Deductions");
            int netPayColumn = findPayrollColumn(savedRecords, "Net Pay");

            int[] requiredColumns = {payrollIdColumn, employeeNumberColumn,
                    firstNameColumn, lastNameColumn, payrollDateColumn, cutoffColumn,
                    earningsColumn, deductionsColumn, netPayColumn};
            for (int column : requiredColumns) {
                if (column < 0) {
                    throw new IOException("Payroll.csv is missing a required column. "
                            + "Check the CSV headers before loading payroll.");
                }
            }

            for (int row = 0; row < savedRecords.getRowCount(); row++) {
                String employeeName = savedRecords.getValueAt(row, firstNameColumn)
                        + " " + savedRecords.getValueAt(row, lastNameColumn);
                payrollTableModel.addRow(new Object[]{
                        savedRecords.getValueAt(row, payrollIdColumn),
                        savedRecords.getValueAt(row, employeeNumberColumn),
                        employeeName,
                        savedRecords.getValueAt(row, payrollDateColumn),
                        savedRecords.getValueAt(row, cutoffColumn),
                        formatSavedMoney(savedRecords.getValueAt(row, earningsColumn)),
                        formatSavedMoney(savedRecords.getValueAt(row, deductionsColumn)),
                        formatSavedMoney(savedRecords.getValueAt(row, netPayColumn))
                });
            }
            rebuildEmployeeList();
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load saved payroll records from data/Payroll.csv: "
                            + ex.getMessage(),
                    "Payroll Loading Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int findPayrollColumn(DefaultTableModel model, String columnName) {
        for (int column = 0; column < model.getColumnCount(); column++) {
            if (columnName.equalsIgnoreCase(model.getColumnName(column).trim())) {
                return column;
            }
        }
        return -1;
    }

    private String formatSavedMoney(Object value) {
        try {
            return PHP.format(new BigDecimal(String.valueOf(value).replace(",", "")));
        } catch (NumberFormatException ex) {
            return String.valueOf(value);
        }
    }

    private void viewPayrollPayslip() {
        int row = payrollTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a payroll record first.");
            return;
        }

        int modelRow = payrollTable.convertRowIndexToModel(row);
        String selectedPayrollId = String.valueOf(
                payrollTableModel.getValueAt(modelRow, 0));
        PayrollRecord record = null;
        for (PayrollRecord savedRecord : payrollRecords) {
            if (savedRecord.getPayrollID().equalsIgnoreCase(selectedPayrollId)) {
                record = savedRecord;
                break;
            }
        }
        if (record == null) {
            JOptionPane.showMessageDialog(this,
                    "This payroll record is loaded from Payroll.csv, but its full "
                            + "payslip details are not available in the current session.",
                    "Payslip Not Available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Payslip payslip = new Payslip(record);

        JTextArea textArea = new JTextArea(payslip.generatePayslip());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
                "Employee Payslip", JOptionPane.INFORMATION_MESSAGE);
    }

    private JComponent buildReportsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel title = new JLabel("Payroll Reports");
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(NAVY);

        JLabel description = new JLabel(
                "Select the payroll month, year, and cutoff to export the matching saved records.");
        description.setForeground(MUTED);

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthChoice = new JComboBox<>(months);
        monthChoice.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        JComboBox<Integer> yearChoice = new JComboBox<>(getAvailablePayrollYears());
        yearChoice.setSelectedItem(LocalDate.now().getYear());
        JComboBox<PayrollCutoff> cutoffChoice = new JComboBox<>(PayrollCutoff.values());

        JLabel recordCount = new JLabel();
        recordCount.setForeground(MUTED);

        Runnable updateCount = () -> {
            int month = monthChoice.getSelectedIndex() + 1;
            Integer year = (Integer) yearChoice.getSelectedItem();
            PayrollCutoff cutoff = (PayrollCutoff) cutoffChoice.getSelectedItem();
            int count = year == null ? 0 : countPayrollRecords(cutoff, month, year);
            recordCount.setText("Records available: " + count);
        };

        monthChoice.addActionListener(e -> updateCount.run());
        yearChoice.addActionListener(e -> updateCount.run());
        cutoffChoice.addActionListener(e -> updateCount.run());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            yearChoice.setModel(new javax.swing.DefaultComboBoxModel<>(getAvailablePayrollYears()));
            updateCount.run();
        });

        JButton exportButton = new JButton("Export Selected Period");
        exportButton.addActionListener(e -> {
            Integer year = (Integer) yearChoice.getSelectedItem();
            if (year != null) {
                exportPayrollByPeriod((PayrollCutoff) cutoffChoice.getSelectedItem(),
                        monthChoice.getSelectedIndex() + 1, year);
            }
        });

        JPanel selection = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selection.setOpaque(false);
        selection.add(new JLabel("Month:"));
        selection.add(monthChoice);
        selection.add(new JLabel("Year:"));
        selection.add(yearChoice);
        selection.add(new JLabel("Cutoff:"));
        selection.add(cutoffChoice);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(refreshButton);
        actions.add(exportButton);
        actions.add(recordCount);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(description);
        content.add(Box.createVerticalStrut(18));
        content.add(selection);
        content.add(Box.createVerticalStrut(12));
        content.add(actions);

        JPanel card = whiteCard(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.add(content, BorderLayout.NORTH);

        page.add(title, BorderLayout.NORTH);
        page.add(card, BorderLayout.CENTER);
        updateCount.run();
        return page;
    }

    private Integer[] getAvailablePayrollYears() {
        java.util.TreeSet<Integer> years = new java.util.TreeSet<>();
        years.add(LocalDate.now().getYear());
        try {
            DefaultTableModel savedRecords = csvManager.readCSV(Path.of("data", "Payroll.csv"));
            int dateColumn = -1;
            int cutoffColumn = -1;
            for (int column = 0; column < savedRecords.getColumnCount(); column++) {
                String header = savedRecords.getColumnName(column).trim();
                if ("Payroll Date".equalsIgnoreCase(header)) dateColumn = column;
                if ("Cutoff".equalsIgnoreCase(header)) cutoffColumn = column;
            }
            if (dateColumn >= 0) {
                for (int row = 0; row < savedRecords.getRowCount(); row++) {
                    try {
                        LocalDate date = LocalDate.parse(
                                String.valueOf(savedRecords.getValueAt(row, dateColumn)).trim());
                        if (cutoffColumn >= 0 && "SECOND".equalsIgnoreCase(
                                String.valueOf(savedRecords.getValueAt(row, cutoffColumn)).trim())) {
                            date = date.minusMonths(1);
                        }
                        years.add(date.getYear());
                    } catch (java.time.format.DateTimeParseException ignored) {
                        // Invalid dates are reported when the user attempts export.
                    }
                }
            }
        } catch (IOException ignored) {
            // Keep the current year available when no payroll file exists yet.
        }
        return years.toArray(new Integer[0]);
    }

    private int countPayrollRecords(PayrollCutoff cutoff, int month, int year) {
        if (cutoff == null) return 0;
        try {
            DefaultTableModel savedRecords = csvManager.readCSV(Path.of("data", "Payroll.csv"));
            int dateColumn = -1;
            int cutoffColumn = -1;
            for (int column = 0; column < savedRecords.getColumnCount(); column++) {
                String header = savedRecords.getColumnName(column).trim();
                if ("Payroll Date".equalsIgnoreCase(header)) dateColumn = column;
                if ("Cutoff".equalsIgnoreCase(header)) cutoffColumn = column;
            }
            if (dateColumn < 0 || cutoffColumn < 0) return 0;

            int count = 0;
            for (int row = 0; row < savedRecords.getRowCount(); row++) {
                String cutoffValue = String.valueOf(savedRecords.getValueAt(row, cutoffColumn)).trim();
                if (!cutoff.name().equalsIgnoreCase(cutoffValue)) continue;
                try {
                    LocalDate date = LocalDate.parse(
                            String.valueOf(savedRecords.getValueAt(row, dateColumn)).trim());
                    if (cutoff == PayrollCutoff.SECOND) date = date.minusMonths(1);
                    if (date.getMonthValue() == month && date.getYear() == year) count++;
                } catch (java.time.format.DateTimeParseException ignored) {
                    // Invalid rows are excluded from the count and reported on export.
                }
            }
            return count;
        } catch (IOException ex) {
            return 0;
        }
    }

    private void exportPayrollByPeriod(PayrollCutoff cutoff, int month, int year) {
        if (cutoff == null) {
            JOptionPane.showMessageDialog(this, "Select a payroll cutoff.");
            return;
        }

        String monthName = java.time.Month.of(month).name();
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(
                "MotorPH_Payroll_" + year + "_" + monthName + "_" + cutoff + ".csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destination = chooser.getSelectedFile().toPath();
        if (!destination.toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            destination = Path.of(destination + ".csv");
        }

        try {
            int exported = csvManager.exportPayrollByCutoffAndMonth(
                    Path.of("data", "Payroll.csv"), cutoff, month, year, destination);
            if (exported == 0) {
                JOptionPane.showMessageDialog(this,
                        "No saved payroll records found for " + monthName + " "
                                + year + " - " + cutoff + " cutoff.");
                return;
            }
            JOptionPane.showMessageDialog(this,
                    "Payroll report exported successfully.\\nPeriod: "
                            + monthName + " " + year + " - " + cutoff
                            + "\\nRecords: " + exported
                            + "\\nFile: " + destination);
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel metricCard(String title, JLabel value, Color accent) {
        JPanel card = whiteCard(new BorderLayout(8, 6));
        card.setBorder(new EmptyBorder(15, 16, 15, 16));

        JLabel label = new JLabel(title);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));

        value.setForeground(accent);
        value.setFont(new Font("SansSerif", Font.BOLD, 21));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(label);
        text.add(Box.createVerticalStrut(9));
        text.add(value);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel detailPanel(String title) {
        JPanel panel = whiteCard(new BorderLayout());
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        heading.setForeground(NAVY);
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    private JPanel whiteCard(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(1, 1, 1, 1)));
        return panel;
    }

    private void showPage(String page) {
        pageLayout.show(pages, page);
    }


    private void rebuildEmployeeList() {
        employeeListModel.clear();
        Map<String, PayrollRecord> unique = new LinkedHashMap<>();

        for (PayrollRecord record : payrollRecords) {
            unique.put(record.getEmployee().getEmployeeNumber(), record);
        }

        for (PayrollRecord record : unique.values()) {
            employeeListModel.addElement(record);
        }

        if (!payrollRecords.isEmpty()) {
            periodLabel.setText("Pay period: " + payrollRecords.get(0).getPayrollDate());
        } else {
            periodLabel.setText("Payroll period: —");
        }

        if (!employeeListModel.isEmpty()) {
            employeeList.setSelectedIndex(0);
        } else {
            showEmptyState();
        }
    }

    private void filterEmployees() {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        employeeListModel.clear();

        Map<String, PayrollRecord> unique = new LinkedHashMap<>();
        for (PayrollRecord record : payrollRecords) {
            unique.put(record.getEmployee().getEmployeeNumber(), record);
        }

        for (PayrollRecord record : unique.values()) {
            Employee employee = record.getEmployee();
            String searchable = (employee.getEmployeeNumber() + " "
                    + employee.getFirstName() + " "
                    + employee.getLastName()).toLowerCase(Locale.ROOT);

            if (searchable.contains(query)) {
                employeeListModel.addElement(record);
            }
        }

        if (!employeeListModel.isEmpty()) {
            employeeList.setSelectedIndex(0);
        } else {
            showEmptyState();
        }
    }

    private void showRecord(PayrollRecord record) {
        if (record == null) {
            return;
        }

        Employee employee = record.getEmployee();
        selectedName.setText("Welcome, " + employee.getFirstName()
                + " " + employee.getLastName());
        selectedMeta.setText(employee.getPosition() + "  ·  "
                + employee.getEmployeeNumber() + "  ·  "
                + employee.getEmploymentStatus());

        grossValue.setText(PHP.format(record.getTotalEarnings()));
        deductionValue.setText(PHP.format(record.getTotalDeductions()));
        netValue.setText(PHP.format(record.getNetPay()));

        periodLabel.setText("Pay period: " + record.getPayrollDate()
                + "  ·  " + record.getCutoff());

        fillDetails(compensationPanel, new String[][]{
                {"Employee type", employee.getEmploymentStatus()},
                {"Regular hours", record.getRegularHoursWorked().toString()},
                {"Overtime hours", record.getOvertimeHours().toString()},
                {"Basic salary", PHP.format(record.getBasicSalary())},
                {"Overtime pay", PHP.format(record.getOvertimePay())},
                {"Allowances", PHP.format(record.getAllowances())},
                {"Other income", PHP.format(record.getOtherIncome())},
                {"Gross pay", PHP.format(record.getTotalEarnings())}
        });

        fillDetails(deductionsPanel, new String[][]{
                {"SSS", PHP.format(record.getSSSDeduction())},
                {"PhilHealth", PHP.format(record.getPhilHealthDeduction())},
                {"Pag-IBIG", PHP.format(record.getPagIBIGDeduction())},
                {"Withholding tax", PHP.format(record.getWithholdingTax())},
                {"Loan", PHP.format(record.getLoanDeduction())},
                {"Other deductions", PHP.format(record.getOtherDeductions())},
                {"Total deductions", PHP.format(record.getTotalDeductions())}
        });
    }

    private void fillDetails(JPanel panel, String[][] rows) {
        panel.removeAll();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String title = panel == compensationPanel ? "Compensation" : "Deductions";
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        heading.setForeground(NAVY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(heading);
        panel.add(Box.createVerticalStrut(12));

        for (String[] row : rows) {
            JPanel line = new JPanel(new BorderLayout(8, 0));
            line.setOpaque(false);
            line.setBorder(new EmptyBorder(6, 0, 6, 0));

            JLabel left = new JLabel(row[0]);
            left.setForeground(MUTED);

            JLabel right = new JLabel(row[1]);
            right.setForeground(NAVY);
            right.setFont(new Font("SansSerif", Font.BOLD, 12));

            line.add(left, BorderLayout.WEST);
            line.add(right, BorderLayout.EAST);
            panel.add(line);
        }

        panel.revalidate();
        panel.repaint();
    }

    private void showEmptyState() {
        selectedName.setText("No employee selected");
        selectedMeta.setText("Import or calculate payroll to get started");
        grossValue.setText(PHP.format(0));
        deductionValue.setText(PHP.format(0));
        netValue.setText(PHP.format(0));

        fillDetails(compensationPanel,
                new String[][]{{"Status", "Waiting for payroll data"}});
        fillDetails(deductionsPanel,
                new String[][]{{"Status", "Waiting for payroll data"}});
    }

    private void exportPayroll() {
        refreshPayrollTable();
        if (payrollRecords.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No saved payroll records are available to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("MotorPH_Payroll.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();
        if (!path.toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            path = Path.of(path + ".csv");
        }

        try {
            csvManager.exportPayrollRecords(payrollRecords, path);
            JOptionPane.showMessageDialog(
                    this,
                    "Payroll CSV exported successfully.\n" + path);
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Export Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JLabel valueLabel() {
        return new JLabel("₱ 0.00");
    }

    private static class EmployeeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean selected, boolean focus) {

            super.getListCellRendererComponent(
                    list, value, index, selected, focus);

            PayrollRecord record = (PayrollRecord) value;
            Employee employee = record.getEmployee();

            setText("<html><b>" + employee.getFirstName() + " "
                    + employee.getLastName() + "</b><br><font color='#687891'>#"
                    + employee.getEmployeeNumber() + " · "
                    + employee.getPosition() + "</font><br><font color='#15966A'>"
                    + employee.getEmploymentStatus() + "</font></html>");

            setBorder(new EmptyBorder(10, 8, 10, 8));
            return this;
        }
    }
}
