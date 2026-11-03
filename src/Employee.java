import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public abstract class Employee {
    private final String employeeNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String address;
    private String contactNo;
    private String sssNo;
    private String philHealthNo;
    private String tinNo;
    private String pagIbigNo;
    private String employmentStatus;
    private String position;
    private String supervisor;

    public Employee(String employeeNumber, String firstName, String lastName,
                    LocalDate birthDate, String address, String contactNo,
                    String sssNo, String philHealthNo, String tinNo,
                    String pagIbigNo, String employmentStatus, String position,
                    String supervisor) {
        this.employeeNumber = validateEmployeeData(employeeNumber, "Employee number");
        this.firstName = validateEmployeeData(firstName, "First name");
        this.lastName = validateEmployeeData(lastName, "Last name");
        this.birthDate = validateBirthDate(birthDate);
        this.address = address;
        this.contactNo = contactNo;
        this.sssNo = sssNo;
        this.philHealthNo = philHealthNo;
        this.tinNo = tinNo;
        this.pagIbigNo = pagIbigNo;
        this.employmentStatus = validateEmployeeData(employmentStatus, "Employment status");
        this.position = validateEmployeeData(position, "Position");
        this.supervisor = supervisor;
    }
    // Validates required text fields
    private String validateEmployeeData(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }
    // Validates birth date
    private LocalDate validateBirthDate (LocalDate birthDate) {
        Objects.requireNonNull(birthDate, "Birth date cannot be null");
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Invalid birth date");
        }
        return birthDate;
    }

    protected static BigDecimal validateAmount (BigDecimal amount, String fieldName) {
        Objects.requireNonNull(amount, fieldName + "cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + "cannot be negative");
        }
        return amount;
    }

    // Payroll calculation methods

    public abstract BigDecimal calculateBasicPay(
            BigDecimal regularHoursWorked,
            PayrollCutoff cutoff
    );

    public BigDecimal calculateAllowances(PayrollCutoff cutoff) {
        return BigDecimal.ZERO;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = validateEmployeeData(firstName, "First Name");
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = validateEmployeeData(lastName, "Last Name");
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = validateBirthDate(birthDate);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getSssNo() {
        return sssNo;
    }

    public void setSssNo(String sssNo) {
        this.sssNo = sssNo;
    }

    public String getPhilHealthNo() {
        return philHealthNo;
    }

    public void setPhilHealthNo(String philHealthNo) {
        this.philHealthNo = philHealthNo;
    }

    public String getTinNo() {
        return tinNo;
    }

    public void setTinNo(String tinNo) {
        this.tinNo = tinNo;
    }

    public String getPagIbigNo() {
        return pagIbigNo;
    }

    public void setPagIbigNo(String pagIbigNo) {
        this.pagIbigNo = pagIbigNo;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = validateEmployeeData(employmentStatus, "Employment Status");
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = validateEmployeeData(position, "Position");
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }


    @Override
    public String toString() {
        return "Employee{" +
                "employeeNumber='" + employeeNumber + '\'' +
                "firstName='" + firstName + '\'' +
                ",lastName='" + lastName + '\'' +
                ",birthDate=" + birthDate + '\'' +
                ", address='" + address + '\'' +
                ", employmentStatus='" + employmentStatus + '\'' +
                ", position='" + position + '\'' +
                ", supervisor='" + supervisor + '\''+
                '}';
    }

}