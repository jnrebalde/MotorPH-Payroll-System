import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

/** Reads and updates employee information in a CSV file. */
public class EmployeeCSV {

    public String filename = "data/employees.csv";
    private ArrayList<Employee> employees = new ArrayList<>();

    public EmployeeCSV() {
        readCSV();
    }

    public ArrayList<Employee> getEmployees() {
        return employees;
    }

    // Reads the CSV using the same CSVParser approach as the original project.
    public final void readCSV() {
        employees.clear();
        File file = new File(filename);
        if (!file.exists()) {
            return;
        }

        try {
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8);
            org.apache.commons.csv.CSVParser parser = CSVFormat.DEFAULT.parse(reader);
            int rowNumber = 0;

            for (CSVRecord record : parser) {
                if (rowNumber == 0) {
                    rowNumber++;
                    continue; // Skip the header row.
                }

                String type = record.get(0);
                String employeeNumber = record.get(1);
                String firstName = record.get(2);
                String lastName = record.get(3);
                LocalDate birthDate = LocalDate.parse(record.get(4));
                String address = record.get(5);
                String contactNo = record.get(6);
                String sssNo = record.get(7);
                String philHealthNo = record.get(8);
                String tinNo = record.get(9);
                String pagIbigNo = record.get(10);
                String status = record.get(11);
                String position = record.get(12);
                String supervisor = record.get(13);

                if (type.equalsIgnoreCase("REGULAR")) {
                    employees.add(new RegularEmployee(employeeNumber, firstName, lastName,
                            birthDate, address, contactNo, sssNo, philHealthNo, tinNo,
                            pagIbigNo, status, position, supervisor,
                            getAmount(record.get(14)), getAmount(record.get(15)),
                            getAmount(record.get(16)), getAmount(record.get(17))));
                } else if (type.equalsIgnoreCase("CONTRACT")) {
                    employees.add(new ContractEmployee(employeeNumber, firstName, lastName,
                            birthDate, address, contactNo, sssNo, philHealthNo, tinNo,
                            pagIbigNo, status, position, supervisor,
                            getAmount(record.get(18))));
                }
                rowNumber++;
            }

            parser.close();
            reader.close();
        } catch (Exception ex) {
            System.out.println("Error in reading Employee CSV: " + ex.getMessage());
        }
    }

    public void importCSV(String selectedFile) {
        filename = selectedFile;
        readCSV();
    }

    public void addEmployee(Employee employee) {
        if (employee == null || findEmployee(employee.getEmployeeNumber()) != null) {
            throw new IllegalArgumentException("Employee is invalid or already exists.");
        }

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename, true));
            writer.writeNext(employeeToRow(employee), false);
            writer.close();
            employees.add(employee);
        } catch (Exception ex) {
            throw new IllegalStateException("Error adding employee: " + ex.getMessage(), ex);
        }
    }

    public void updateEmployee(String oldEmployeeNumber, Employee updatedEmployee) {
        if (updatedEmployee == null) {
            throw new IllegalArgumentException("Updated employee information is missing.");
        }
        if (!oldEmployeeNumber.equals(updatedEmployee.getEmployeeNumber())) {
            Employee duplicate = findEmployee(updatedEmployee.getEmployeeNumber());
            if (duplicate != null) {
                throw new IllegalArgumentException("Employee number already exists.");
            }
        }
        rewriteEmployeeFile(oldEmployeeNumber, updatedEmployee, false);
        readCSV();
    }

    public void deleteEmployee(String employeeNumber) {
        rewriteEmployeeFile(employeeNumber, null, true);
        readCSV();
    }

    // Reads every CSV row first, changes only the matching employee, then safely rewrites the file.
    private void rewriteEmployeeFile(String employeeNumber, Employee updatedEmployee, boolean delete) {
        File source = new File(filename);
        if (!source.exists()) {
            throw new IllegalStateException("Employee CSV file was not found: " + filename);
        }

        ArrayList<String[]> rows = new ArrayList<>();
        boolean found = false;

        try {
            CSVReader reader = new CSVReader(new FileReader(source));
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length > 1 && row[1].trim().equals(employeeNumber.trim())) {
                    found = true;
                    if (!delete) {
                        rows.add(employeeToRow(updatedEmployee));
                    }
                } else {
                    rows.add(row);
                }
            }
            reader.close();

            if (!found) {
                throw new IllegalArgumentException("Employee not found: " + employeeNumber);
            }

            File temporaryFile = new File(filename + ".tmp");
            CSVWriter writer = new CSVWriter(new FileWriter(temporaryFile));
            for (String[] savedRow : rows) {
                writer.writeNext(savedRow, false);
            }
            writer.close();

            // Keep a backup before replacing the original employee CSV.
            Files.copy(source.toPath(), Path.of(filename + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(temporaryFile.toPath(), source.toPath(), StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception moveException) {
                Files.move(temporaryFile.toPath(), source.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error saving employee CSV: " + ex.getMessage(), ex);
        }
    }

    private String[] employeeToRow(Employee employee) {
        String type;
        String monthly = "";
        String rice = "";
        String phone = "";
        String clothing = "";
        String hourly = "";

        if (employee instanceof RegularEmployee) {
            RegularEmployee regular = (RegularEmployee) employee;
            type = "REGULAR";
            monthly = regular.getMonthlySalary().toPlainString();
            rice = regular.getRiceSubsidy().toPlainString();
            phone = regular.getPhoneAllowance().toPlainString();
            clothing = regular.getClothingAllowance().toPlainString();
        } else {
            ContractEmployee contract = (ContractEmployee) employee;
            type = "CONTRACT";
            hourly = contract.getHourlyRate().toPlainString();
        }

        return new String[]{type, employee.getEmployeeNumber(), employee.getFirstName(),
                employee.getLastName(), employee.getBirthDate().toString(),
                employee.getAddress(), employee.getContactNo(), employee.getSssNo(),
                employee.getPhilHealthNo(), employee.getTinNo(), employee.getPagIbigNo(),
                employee.getEmploymentStatus(), employee.getPosition(), employee.getSupervisor(),
                monthly, rice, phone, clothing, hourly};
    }

    private Employee findEmployee(String employeeNumber) {
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equalsIgnoreCase(employeeNumber.trim())) {
                return employee;
            }
        }
        return null;
    }

    private int findEmployeeIndex(String employeeNumber) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmployeeNumber().equalsIgnoreCase(employeeNumber.trim())) {
                return i;
            }
        }
        return -1;
    }

    private BigDecimal getAmount(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }
}
