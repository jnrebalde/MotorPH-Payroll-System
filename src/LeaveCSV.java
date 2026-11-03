import com.opencsv.CSVWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

/** Reads and saves employee leave requests using a CSV file. */
public class LeaveCSV {
    public String filename = "data/leaves.csv";
    private final ArrayList<LeaveRequest> leaveRequests = new ArrayList<>();

    public ArrayList<LeaveRequest> getLeaveRequests() {
        return leaveRequests;
    }

    public void readCSV(ArrayList<Employee> employees) {
        leaveRequests.clear();
        File file = new File(filename);
        if (!file.exists()) return;

        try {
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8);
            org.apache.commons.csv.CSVParser parser = CSVFormat.DEFAULT.parse(reader);
            int rowNumber = 0;
            for (CSVRecord record : parser) {
                if (rowNumber++ == 0) continue;
                Employee employee = findEmployee(employees, record.get(1));
                if (employee != null) {
                    leaveRequests.add(new LeaveRequest(record.get(0), employee,
                            record.get(2), LocalDate.parse(record.get(3)),
                            LocalDate.parse(record.get(4)), record.get(5), record.get(6)));
                }
            }
            parser.close();
            reader.close();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error reading leave CSV: " + ex.getMessage());
        }
    }

    public void importCSV(String selectedFile, ArrayList<Employee> employees) {
        filename = selectedFile;
        readCSV(employees);
    }

    public void addLeave(LeaveRequest request) {
        if (request == null || findLeave(request.getLeaveID()) != null)
            throw new IllegalArgumentException("Leave request is invalid or its ID already exists.");
        leaveRequests.add(request);
        saveCSV();
    }

    public void updateLeave(String leaveID, LeaveRequest updated) {
        int index = findLeaveIndex(leaveID);
        if (index < 0) throw new IllegalArgumentException("Leave request not found.");
        LeaveRequest duplicate = findLeave(updated.getLeaveID());
        if (duplicate != null && !leaveID.equals(updated.getLeaveID()))
            throw new IllegalArgumentException("Leave ID already exists.");
        leaveRequests.set(index, updated);
        saveCSV();
    }

    public void deleteLeave(String leaveID) {
        int index = findLeaveIndex(leaveID);
        if (index < 0) throw new IllegalArgumentException("Leave request not found.");
        leaveRequests.remove(index);
        saveCSV();
    }

    public void saveCSV() {
        File file = new File(filename);
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file, false));
            writer.writeNext(new String[]{"LeaveID", "EmployeeNumber", "LeaveType", "StartDate", "EndDate", "Reason", "Status"});
            for (LeaveRequest request : leaveRequests) {
                writer.writeNext(new String[]{request.getLeaveID(),
                        request.getEmployee().getEmployeeNumber(), request.getLeaveType(),
                        request.getStartDate().toString(), request.getEndDate().toString(),
                        request.getLeaveReason() == null ? "" : request.getLeaveReason(),
                        request.getLeaveStatus()});
            }
            writer.close();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error saving leave CSV: " + ex.getMessage());
        }
    }

    private Employee findEmployee(ArrayList<Employee> employees, String employeeNumber) {
        for (Employee employee : employees)
            if (employee.getEmployeeNumber().equals(employeeNumber)) return employee;
        return null;
    }

    private LeaveRequest findLeave(String leaveID) {
        int index = findLeaveIndex(leaveID);
        return index < 0 ? null : leaveRequests.get(index);
    }

    private int findLeaveIndex(String leaveID) {
        for (int i = 0; i < leaveRequests.size(); i++)
            if (leaveRequests.get(i).getLeaveID().equalsIgnoreCase(leaveID)) return i;
        return -1;
    }
}
