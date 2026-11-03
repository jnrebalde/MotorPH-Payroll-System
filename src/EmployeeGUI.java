import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Employee master-data screen backed by employees.csv. */
@SuppressWarnings({"serial", "this-escape"})
public class EmployeeGUI extends JPanel {

    private final EmployeeCSV management = new EmployeeCSV();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Employee No.", "Name", "Type", "Position", "Status", "Rate / Salary"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public EmployeeGUI() {
        setLayout(new BorderLayout(10, 12));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Employee Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(new Color(16, 43, 80));

        JButton add = new JButton("Add Employee");
        JButton edit = new JButton("Edit Employee");
        JButton delete = new JButton("Delete Employee");
        JButton loadCsv = new JButton("Load CSV");
        JButton reload = new JButton("Reload CSV");

        add.addActionListener(e -> addEmployee());
        edit.addActionListener(e -> editEmployee());
        delete.addActionListener(e -> deleteEmployee());
        loadCsv.addActionListener(e -> loadCsvFromFile());
        reload.addActionListener(e -> load());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(loadCsv);
        actions.add(reload);

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

    public void loadCsvFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select employee CSV file");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            management.importCSV(chooser.getSelectedFile().getAbsolutePath());
            refreshTable();
            JOptionPane.showMessageDialog(this, management.getEmployees().size() + " employee record(s) loaded.");
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void load() {
        try { management.readCSV(); refreshTable(); }
        catch (Exception ex) { showError("Could not load employee CSV: " + ex.getMessage()); }
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Employee e : management.getEmployees()) {
            String type;
            String compensation;
            if (e instanceof RegularEmployee) {
                RegularEmployee regular = (RegularEmployee) e;
                type = "Regular";
                compensation = regular.getMonthlySalary().toPlainString();
            } else {
                ContractEmployee contract = (ContractEmployee) e;
                type = "Contract";
                compensation = contract.getHourlyRate().toPlainString() + " / hr";
            }
            model.addRow(new Object[]{
                    e.getEmployeeNumber(),
                    e.getFirstName() + " " + e.getLastName(),
                    type, e.getPosition(), e.getEmploymentStatus(), compensation
            });
        }
    }

    private void addEmployee() {
        Employee employee = showEmployeeForm(null);
        if (employee == null) return;
        try { management.addEmployee(employee); refreshTable(); }
        catch (RuntimeException ex) { showError(ex.getMessage()); }
    }

    private void editEmployee() {
        int row = selectedModelRow();
        if (row < 0) { showError("Select an employee first."); return; }
        Employee old = management.getEmployees().get(row);
        Employee updated = showEmployeeForm(old);
        if (updated == null) return;
        try { management.updateEmployee(old.getEmployeeNumber(), updated); refreshTable(); }
        catch (RuntimeException ex) { showError(ex.getMessage()); }
    }

    private void deleteEmployee() {
        int row = selectedModelRow();
        if (row < 0) { showError("Select an employee first."); return; }
        Employee employee = management.getEmployees().get(row);
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete " + employee.getFirstName() + " " + employee.getLastName() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            try { management.deleteEmployee(employee.getEmployeeNumber()); refreshTable(); }
            catch (RuntimeException ex) { showError(ex.getMessage()); }
        }
    }

    private int selectedModelRow() {
        int viewRow = table.getSelectedRow();
        return viewRow < 0 ? -1 : table.convertRowIndexToModel(viewRow);
    }

    private Employee findByNumber(String number) {
        for (Employee e : management.getEmployees()) {
            if (e.getEmployeeNumber().equalsIgnoreCase(number.trim())) return e;
        }
        return null;
    }

    private Employee showEmployeeForm(Employee existing) {
        boolean editing = existing != null;
        JTextField number = field(editing ? existing.getEmployeeNumber() : "");
        JTextField first = field(editing ? existing.getFirstName() : "");
        JTextField last = field(editing ? existing.getLastName() : "");
        JTextField birth = field(editing ? existing.getBirthDate().toString() : "1990-01-01");
        JTextField address = field(editing ? existing.getAddress() : "");
        JTextField contact = field(editing ? existing.getContactNo() : "");
        JTextField sss = field(editing ? existing.getSssNo() : "");
        JTextField phil = field(editing ? existing.getPhilHealthNo() : "");
        JTextField tin = field(editing ? existing.getTinNo() : "");
        JTextField pagibig = field(editing ? existing.getPagIbigNo() : "");
        JTextField status = field(editing ? existing.getEmploymentStatus() : "Regular");
        JTextField position = field(editing ? existing.getPosition() : "");
        JTextField supervisor = field(editing ? existing.getSupervisor() : "");
        JComboBox<String> type = new JComboBox<>(new String[]{"REGULAR", "CONTRACT"});
        if (existing instanceof ContractEmployee) type.setSelectedItem("CONTRACT");

        String monthlyValue = "0";
        String riceValue = "0";
        String phoneValue = "0";
        String clothingValue = "0";
        String hourlyValue = "0";
        if (existing instanceof RegularEmployee) {
            RegularEmployee regular = (RegularEmployee) existing;
            monthlyValue = regular.getMonthlySalary().toPlainString();
            riceValue = regular.getRiceSubsidy().toPlainString();
            phoneValue = regular.getPhoneAllowance().toPlainString();
            clothingValue = regular.getClothingAllowance().toPlainString();
        }
        if (existing instanceof ContractEmployee) {
            ContractEmployee contract = (ContractEmployee) existing;
            hourlyValue = contract.getHourlyRate().toPlainString();
        }
        JTextField monthly = field(monthlyValue);
        JTextField rice = field(riceValue);
        JTextField phone = field(phoneValue);
        JTextField clothing = field(clothingValue);
        JTextField hourly = field(hourlyValue);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        addField(form, "Employee type", type);
        addField(form, "Employee number", number);
        addField(form, "First name", first);
        addField(form, "Last name", last);
        addField(form, "Birth date (YYYY-MM-DD)", birth);
        addField(form, "Address", address);
        addField(form, "Contact number", contact);
        addField(form, "SSS number", sss);
        addField(form, "PhilHealth number", phil);
        addField(form, "TIN", tin);
        addField(form, "Pag-IBIG number", pagibig);
        addField(form, "Employment status", status);
        addField(form, "Position", position);
        addField(form, "Supervisor", supervisor);
        addField(form, "Monthly salary (regular)", monthly);
        addField(form, "Rice subsidy", rice);
        addField(form, "Phone allowance", phone);
        addField(form, "Clothing allowance", clothing);
        addField(form, "Hourly rate (contract)", hourly);

        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(form),
                editing ? "Edit Employee" : "Add Employee",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;

        try {
            String employeeType = (String) type.getSelectedItem();
            if ("REGULAR".equals(employeeType)) {
                return new RegularEmployee(number.getText().trim(), first.getText().trim(),
                        last.getText().trim(), LocalDate.parse(birth.getText().trim()),
                        address.getText(), contact.getText(), sss.getText(), phil.getText(),
                        tin.getText(), pagibig.getText(), status.getText().trim(),
                        position.getText().trim(), supervisor.getText(),
                        new BigDecimal(monthly.getText().trim()),
                        new BigDecimal(rice.getText().trim()),
                        new BigDecimal(phone.getText().trim()),
                        new BigDecimal(clothing.getText().trim()));
            }
            return new ContractEmployee(number.getText().trim(), first.getText().trim(),
                    last.getText().trim(), LocalDate.parse(birth.getText().trim()),
                    address.getText(), contact.getText(), sss.getText(), phil.getText(),
                    tin.getText(), pagibig.getText(), status.getText().trim(),
                    position.getText().trim(), supervisor.getText(),
                    new BigDecimal(hourly.getText().trim()));
        } catch (RuntimeException ex) {
            showError("Invalid employee information: " + ex.getMessage());
            return null;
        }
    }

    private JTextField field(String value) {
        return new JTextField(value == null ? "" : value, 18);
    }

    private void addField(JPanel panel, String label, JComponent input) {
        panel.add(new JLabel(label));
        panel.add(input);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Employee Management", JOptionPane.ERROR_MESSAGE);
    }
}
