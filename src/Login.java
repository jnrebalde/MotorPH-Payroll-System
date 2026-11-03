import javax.swing.*;
import java.awt.*;

@SuppressWarnings({"serial", "this-escape"})
public class Login extends JFrame {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final UserCSV userCSV = new UserCSV();

    public Login() {
        setTitle("MotorPH Payroll System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 230);
        setResizable(false);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("MOTORPH PAYROLL SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel fields = new JPanel(new GridLayout(2, 2, 8, 12));
        fields.add(new JLabel("Username:"));
        fields.add(usernameField);
        fields.add(new JLabel("Password:"));
        fields.add(passwordField);

        JButton loginButton = new JButton("Log In");
        loginButton.addActionListener(event -> login());
        passwordField.addActionListener(event -> login());

        JPanel content = new JPanel(new BorderLayout(12, 18));
        content.setBorder(BorderFactory.createEmptyBorder(22, 25, 22, 25));
        content.add(title, BorderLayout.NORTH);
        content.add(fields, BorderLayout.CENTER);
        content.add(loginButton, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        boolean isAdmin = ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);

        String employeeId = isAdmin ? null : userCSV.getEmployeeIdForLogin(username, password);
        if (isAdmin || employeeId != null) {
            dispose();
            if (isAdmin) new PayrollGUI(true).setVisible(true);
            else new EmployeePayrollHome(employeeId).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}
