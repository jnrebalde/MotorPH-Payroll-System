import com.opencsv.CSVWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/** Handles user login records using the original MO-IT103 User.csv columns. */
public class UserCSV {
    public String filename = "data/User.csv";
    private final String[] header = {"Employee ID", "Full Name", "Email", "Username", "Password"};

    public UserCSV() {
        createFile();
    }

    private void createFile() {
        try {
            File file = new File(filename);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!file.exists()) {
                CSVWriter writer = new CSVWriter(new FileWriter(file));
                writer.writeNext(header, false);
                writer.close();
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create User.csv", ex);
        }
    }

    public String getEmployeeIdForLogin(String username, String password) {
        for (String[] user : readUsers()) {
            if (user[3].equals(username) && user[4].equals(password)) return user[0];
        }
        return null;
    }

    public boolean validateLogin(String username, String password) {
        ArrayList<String[]> users = readUsers();
        for (String[] user : users) {
            if (user[3].equals(username) && user[4].equals(password)) return true;
        }
        return false;
    }

    public String addUser(String employeeId, String fullName, String email,
                          String username, String password) {
        employeeId = clean(employeeId);
        fullName = clean(fullName);
        email = clean(email);
        username = clean(username);
        password = clean(password);

        if (employeeId.isEmpty() || fullName.isEmpty() || email.isEmpty()
                || username.isEmpty() || password.isEmpty()) {
            return "All fields are required.";
        }
        if (!isValidEmail(email)) return "Enter a valid email address (example: name@email.com).";

        ArrayList<String[]> users = readUsers();
        for (String[] user : users) {
            if (user[0].equalsIgnoreCase(employeeId)) return "This employee ID already has a login account.";
            if (user[3].equalsIgnoreCase(username)) return "Username is already taken.";
        }

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename, true));
            writer.writeNext(new String[]{employeeId, fullName, email, username, password}, false);
            writer.close();
            return "SUCCESS";
        } catch (Exception ex) {
            return "Unable to save the user to User.csv: " + ex.getMessage();
        }
    }

    public ArrayList<String[]> readUsers() {
        ArrayList<String[]> users = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return users;
        try {
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);
            int row = 0;
            for (CSVRecord record : records) {
                if (row++ == 0) continue;
                if (record.size() >= 5) {
                    users.add(new String[]{record.get(0), record.get(1), record.get(2),
                            record.get(3), record.get(4)});
                }
            }
            reader.close();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to read User.csv", ex);
        }
        return users;
    }


    public String updateUser(String originalUsername, String employeeId, String fullName,
                             String email, String username, String password) {
        employeeId = clean(employeeId);
        fullName = clean(fullName);
        email = clean(email);
        username = clean(username);
        password = clean(password);
        if (employeeId.isEmpty() || fullName.isEmpty() || email.isEmpty()
                || username.isEmpty() || password.isEmpty()) return "All fields are required.";
        if (!isValidEmail(email)) return "Enter a valid email address (example: name@email.com).";

        ArrayList<String[]> users = readUsers();
        boolean found = false;
        for (String[] user : users) {
            if (user[3].equalsIgnoreCase(originalUsername)) found = true;
            else if (user[3].equalsIgnoreCase(username)) return "Username is already taken.";
            if (!user[3].equalsIgnoreCase(originalUsername)
                    && user[0].equalsIgnoreCase(employeeId)) return "Employee ID already has a login account.";
        }
        if (!found) return "User account was not found.";
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i)[3].equalsIgnoreCase(originalUsername)) {
                users.set(i, new String[]{employeeId, fullName, email, username, password});
                break;
            }
        }
        return saveUsers(users);
    }

    public String deleteUser(String username) {
        ArrayList<String[]> users = readUsers();
        boolean removed = false;
        for (int i = users.size() - 1; i >= 0; i--) {
            if (users.get(i)[3].equalsIgnoreCase(username)) {
                users.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed) return "User account was not found.";
        return saveUsers(users);
    }

    private String saveUsers(ArrayList<String[]> users) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename));
            writer.writeNext(header, false);
            for (String[] user : users) writer.writeNext(user, false);
            writer.close();
            return "SUCCESS";
        } catch (Exception ex) {
            return "Unable to update User.csv: " + ex.getMessage();
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
