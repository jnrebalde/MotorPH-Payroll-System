import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/** Simple attendance screen for adding and maintaining time records. */
@SuppressWarnings({"serial", "this-escape"})
public class AttendanceGUI extends JPanel {

    private final EmployeeCSV employeeCSV = new EmployeeCSV();
    private final AttendanceCSV attendanceCSV = new AttendanceCSV();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Employee No.", "Employee Name", "Date", "Time In", "Time Out", "Hours", "Regular", "OT"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public AttendanceGUI() {
        setLayout(new BorderLayout(10, 12));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Attendance Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(new Color(16, 43, 80));

        JButton add = new JButton("Add Attendance");
        JButton edit = new JButton("Edit Attendance");
        JButton delete = new JButton("Delete Attendance");
        JButton load = new JButton("Load CSV");
        JButton reload = new JButton("Reload CSV");
        add.addActionListener(e -> addAttendance());
        edit.addActionListener(e -> editAttendance());
        delete.addActionListener(e -> deleteAttendance());
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
            attendanceCSV.readCSV(employeeCSV.getEmployees());
            refreshTable();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void loadCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select attendance CSV file");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                employeeCSV.readCSV();
                attendanceCSV.importCSV(chooser.getSelectedFile().getAbsolutePath(), employeeCSV.getEmployees());
                refreshTable();
                JOptionPane.showMessageDialog(this, attendanceCSV.getAttendanceRecords().size() + " attendance record(s) loaded.");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (AttendanceRecord record : attendanceCSV.getAttendanceRecords()) {
            model.addRow(new Object[]{record.getEmployee().getEmployeeNumber(),
                    record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName(),
                    record.getDate(), record.getTimeIn(), record.getTimeOut(),
                    record.calculateHoursWorked(), record.calculateRegularHours(),
                    record.calculateOvertimeHours()});
        }
    }

    private void addAttendance() {
        AttendanceRecord record = showForm(null);
        if (record != null) {
            try { attendanceCSV.addAttendance(record); refreshTable(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void editAttendance() {
        int row = selectedRow();
        if (row < 0) { showError("Select an attendance record first."); return; }
        AttendanceRecord old = attendanceCSV.getAttendanceRecords().get(row);
        AttendanceRecord updated = showForm(old);
        if (updated != null) {
            try { attendanceCSV.updateAttendance(old.getEmployee().getEmployeeNumber(), old.getDate(), updated); refreshTable(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void deleteAttendance() {
        int row = selectedRow();
        if (row < 0) { showError("Select an attendance record first."); return; }
        AttendanceRecord record = attendanceCSV.getAttendanceRecords().get(row);
        int answer = JOptionPane.showConfirmDialog(this, "Delete selected attendance record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            try { attendanceCSV.deleteAttendance(record.getEmployee().getEmployeeNumber(), record.getDate()); refreshTable(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private AttendanceRecord showForm(AttendanceRecord existing) {
        ArrayList<Employee> employees = employeeCSV.getEmployees();
        if (employees.isEmpty()) { showError("Load employee records first."); return null; }
        JComboBox<Employee> employeeBox = new JComboBox<>();
        for (Employee employee : employees) employeeBox.addItem(employee);
        employeeBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Employee) {
                    Employee employee = (Employee) value;
                    setText(employee.getEmployeeNumber() + " - " + employee.getFirstName() + " " + employee.getLastName());
                }
                return this;
            }
        });
        JTextField date = new JTextField(existing == null ? LocalDate.now().toString() : existing.getDate().toString());
        JTextField timeIn = new JTextField(existing == null ? "08:00" : existing.getTimeIn().toString());
        JTextField timeOut = new JTextField(existing == null ? "17:00" : existing.getTimeOut().toString());
        if (existing != null) employeeBox.setSelectedItem(existing.getEmployee());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Employee:")); form.add(employeeBox);
        form.add(new JLabel("Date (YYYY-MM-DD):")); form.add(date);
        form.add(new JLabel("Time In (HH:MM):")); form.add(timeIn);
        form.add(new JLabel("Time Out (HH:MM):")); form.add(timeOut);
        int answer = JOptionPane.showConfirmDialog(this, form, existing == null ? "Add Attendance" : "Edit Attendance", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return null;
        try {
            return new AttendanceRecord((Employee) employeeBox.getSelectedItem(),
                    LocalDate.parse(date.getText().trim()), LocalTime.parse(timeIn.getText().trim()),
                    LocalTime.parse(timeOut.getText().trim()));
        } catch (Exception ex) { showError("Check the date and time format. " + ex.getMessage()); return null; }
    }

    private int selectedRow() {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : table.convertRowIndexToModel(row);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Attendance", JOptionPane.ERROR_MESSAGE);
    }
}
