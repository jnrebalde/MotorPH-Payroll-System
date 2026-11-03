import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;

/** Simple screen for submitting and maintaining leave requests. */
@SuppressWarnings({"serial", "this-escape"})
public class LeaveGUI extends JPanel {
    private final EmployeeCSV employeeCSV = new EmployeeCSV();
    private final LeaveCSV leaveCSV = new LeaveCSV();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Leave ID", "Employee No.", "Employee Name", "Leave Type", "Start Date", "End Date", "Days", "Status", "Reason"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public LeaveGUI() {
        setLayout(new BorderLayout(10, 12));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        JLabel title = new JLabel("Leave Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(new Color(16, 43, 80));

        JButton add = new JButton("Add Leave Request");
        JButton edit = new JButton("Edit / Update Status");
        JButton delete = new JButton("Delete Request");
        JButton load = new JButton("Load CSV");
        JButton reload = new JButton("Reload CSV");
        add.addActionListener(e -> addLeave());
        edit.addActionListener(e -> editLeave());
        delete.addActionListener(e -> deleteLeave());
        load.addActionListener(e -> loadCsv());
        reload.addActionListener(e -> load());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        actions.add(add); actions.add(edit); actions.add(delete); actions.add(load); actions.add(reload);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(actions, BorderLayout.SOUTH);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        load();
    }

    private void load() {
        try {
            employeeCSV.readCSV();
            leaveCSV.readCSV(employeeCSV.getEmployees());
            refreshTable();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void loadCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select leave CSV file");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                employeeCSV.readCSV();
                leaveCSV.importCSV(chooser.getSelectedFile().getAbsolutePath(), employeeCSV.getEmployees());
                refreshTable();
                JOptionPane.showMessageDialog(this, leaveCSV.getLeaveRequests().size() + " leave request(s) loaded.");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (LeaveRequest request : leaveCSV.getLeaveRequests()) {
            Employee employee = request.getEmployee();
            model.addRow(new Object[]{request.getLeaveID(), employee.getEmployeeNumber(),
                    employee.getFirstName() + " " + employee.getLastName(), request.getLeaveType(),
                    request.getStartDate(), request.getEndDate(), request.calculateLeaveDays(),
                    request.getLeaveStatus(), request.getLeaveReason()});
        }
    }

    private void addLeave() {
        LeaveRequest request = showForm(null);
        if (request != null) {
            try { leaveCSV.addLeave(request); refreshTable(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void editLeave() {
        int row = selectedRow();
        if (row < 0) { showError("Select a leave request first."); return; }
        LeaveRequest old = leaveCSV.getLeaveRequests().get(row);
        LeaveRequest updated = showForm(old);
        if (updated != null) {
            try { leaveCSV.updateLeave(old.getLeaveID(), updated); refreshTable(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void deleteLeave() {
        int row = selectedRow();
        if (row < 0) { showError("Select a leave request first."); return; }
        LeaveRequest request = leaveCSV.getLeaveRequests().get(row);
        int answer = JOptionPane.showConfirmDialog(this, "Delete leave request " + request.getLeaveID() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            try { leaveCSV.deleteLeave(request.getLeaveID()); refreshTable(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private LeaveRequest showForm(LeaveRequest old) {
        employeeCSV.readCSV();
        ArrayList<Employee> employees = employeeCSV.getEmployees();
        if (employees.isEmpty()) { showError("Load employees before creating a leave request."); return null; }
        JComboBox<Employee> employee = new JComboBox<>(employees.toArray(new Employee[0]));
        employee.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Employee) {
                    Employee e = (Employee) value;
                    setText(e.getEmployeeNumber() + " - " + e.getFirstName() + " " + e.getLastName());
                }
                return this;
            }
        });
        JTextField id = new JTextField(old == null ? "LV-" + (System.currentTimeMillis() % 100000) : old.getLeaveID());
        JComboBox<String> type = new JComboBox<>(new String[]{"Vacation Leave", "Sick Leave", "Emergency Leave", "Other"});
        JTextField start = new JTextField(old == null ? LocalDate.now().toString() : old.getStartDate().toString());
        JTextField end = new JTextField(old == null ? LocalDate.now().toString() : old.getEndDate().toString());
        JComboBox<String> status = new JComboBox<>(new String[]{"Pending", "Approved", "Rejected"});
        JTextField reason = new JTextField(old == null ? "" : old.getLeaveReason());
        if (old != null) {
            employee.setSelectedItem(old.getEmployee()); type.setSelectedItem(old.getLeaveType()); status.setSelectedItem(old.getLeaveStatus());
        }
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Leave ID")); form.add(id);
        form.add(new JLabel("Employee")); form.add(employee);
        form.add(new JLabel("Leave Type")); form.add(type);
        form.add(new JLabel("Start Date (YYYY-MM-DD)")); form.add(start);
        form.add(new JLabel("End Date (YYYY-MM-DD)")); form.add(end);
        form.add(new JLabel("Status")); form.add(status);
        form.add(new JLabel("Reason")); form.add(reason);
        int result = JOptionPane.showConfirmDialog(this, form, old == null ? "Add Leave Request" : "Edit Leave Request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;
        try {
            return new LeaveRequest(id.getText(), (Employee) employee.getSelectedItem(),
                    (String) type.getSelectedItem(), LocalDate.parse(start.getText().trim()),
                    LocalDate.parse(end.getText().trim()), reason.getText(), (String) status.getSelectedItem());
        } catch (Exception ex) { showError("Check the leave details: " + ex.getMessage()); return null; }
    }

    private int selectedRow() {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : table.convertRowIndexToModel(row);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Leave Management", JOptionPane.ERROR_MESSAGE);
    }
}
