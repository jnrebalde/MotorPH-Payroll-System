import com.opencsv.CSVWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Reads and saves attendance records using a simple CSV file. */
public class AttendanceCSV {

    public String filename = "data/attendance.csv";
    private ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();

    public ArrayList<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }

    public void readCSV(ArrayList<Employee> employees) {
        attendanceRecords.clear();
        File file = new File(filename);
        if (!file.exists()) return;

        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()),
                    StandardCharsets.UTF_8);

            // Recover older files that were saved with the two characters "\\n"
            // instead of actual line breaks.
            content = content.replace("\\r\\n", "\\n")
                    .replace("\\n", System.lineSeparator());

            try (StringReader reader = new StringReader(content);
                 org.apache.commons.csv.CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
                int rowNumber = 0;
                for (CSVRecord record : parser) {
                    if (rowNumber++ == 0 || record.size() < 4) continue;

                    String employeeNumber = record.get(0).trim();
                    Employee employee = findEmployee(employees, employeeNumber);
                    if (employee != null) {
                        attendanceRecords.add(new AttendanceRecord(employee,
                                LocalDate.parse(record.get(1).trim()),
                                LocalTime.parse(record.get(2).trim()),
                                LocalTime.parse(record.get(3).trim())));
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error reading attendance CSV: " + ex.getMessage());
        }
    }

    public void importCSV(String selectedFile, ArrayList<Employee> employees) {
        filename = selectedFile;
        readCSV(employees);
    }

    public void addAttendance(AttendanceRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Attendance record is required.");
        }
        if (findRecord(record.getEmployee().getEmployeeNumber(), record.getDate()) != null) {
            throw new IllegalArgumentException("Attendance already exists for this employee and date.");
        }

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename, true));
            writer.writeNext(attendanceToRow(record), false);
            writer.close();
            attendanceRecords.add(record);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error adding attendance: " + ex.getMessage());
        }
    }

    public void updateAttendance(String employeeNumber, LocalDate oldDate, AttendanceRecord updated) {
        ArrayList<String> rows = new ArrayList<>();
        boolean found = false;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",", -1);
                if (columns.length > 1 && columns[0].replace("\\\"","").equals(employeeNumber)
                        && columns[1].replace("\\\"","").equals(oldDate.toString())) {
                    rows.add(toCsvLine(attendanceToRow(updated)));
                    found = true;
                } else {
                    rows.add(line);
                }
            }
            reader.close();

            if (!found) throw new IllegalArgumentException("Attendance record not found.");

            writeRows(rows);
            int index = findRecordIndex(employeeNumber, oldDate);
            if (index != -1) attendanceRecords.set(index, updated);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error updating attendance: " + ex.getMessage());
        }
    }

    public void deleteAttendance(String employeeNumber, LocalDate date) {
        ArrayList<String> rows = new ArrayList<>();
        boolean found = false;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",", -1);
                if (columns.length > 1 && columns[0].replace("\\\"","").equals(employeeNumber)
                        && columns[1].replace("\\\"","").equals(date.toString())) {
                    found = true;
                } else {
                    rows.add(line);
                }
            }
            reader.close();

            if (!found) throw new IllegalArgumentException("Attendance record not found.");

            writeRows(rows);
            int index = findRecordIndex(employeeNumber, date);
            if (index != -1) attendanceRecords.remove(index);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error deleting attendance: " + ex.getMessage());
        }
    }

    private void writeRows(ArrayList<String> rows) throws Exception {
        FileWriter writer = new FileWriter(filename);
        for (int i = 0; i < rows.size(); i++) {
            writer.write(rows.get(i));
            writer.write(System.lineSeparator());
        }
        writer.close();
    }

    private String[] attendanceToRow(AttendanceRecord record) {
        return new String[]{record.getEmployee().getEmployeeNumber(),
                record.getDate().toString(), record.getTimeIn().toString(),
                record.getTimeOut().toString()};
    }

    private String toCsvLine(String[] values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) line.append(",");
            line.append(values[i]);
        }
        return line.toString();
    }

    private ArrayList<Employee> getEmployeesFromRecords() {
        ArrayList<Employee> employees = new ArrayList<>();
        for (AttendanceRecord record : attendanceRecords) {
            if (!employees.contains(record.getEmployee())) {
                employees.add(record.getEmployee());
            }
        }
        return employees;
    }

    private Employee findEmployee(List<Employee> employees, String employeeNumber) {
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equals(employeeNumber)) return employee;
        }
        return null;
    }

    private AttendanceRecord findRecord(String employeeNumber, LocalDate date) {
        int index = findRecordIndex(employeeNumber, date);
        return index == -1 ? null : attendanceRecords.get(index);
    }

    private int findRecordIndex(String employeeNumber, LocalDate date) {
        for (int i = 0; i < attendanceRecords.size(); i++) {
            AttendanceRecord record = attendanceRecords.get(i);
            if (record.getEmployee().getEmployeeNumber().equals(employeeNumber)
                    && record.getDate().equals(date)) return i;
        }
        return -1;
    }
}
