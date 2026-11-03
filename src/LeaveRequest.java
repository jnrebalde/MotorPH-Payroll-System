import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class LeaveRequest {
    private final String leaveID;
    private final Employee employee;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveReason;
    private String leaveStatus;

    public  LeaveRequest(String leaveID, Employee employee, String leaveType,
                         LocalDate startDate, LocalDate endDate,
                         String leaveReason, String leaveStatus) {
        this.leaveID = validateLeaveData(leaveID, "Leave ID");
        this.employee = Objects.requireNonNull(employee, "Employee cannot be null");
        this.leaveType = validateLeaveData(leaveType, "Leave type");
        this.leaveStatus = validateLeaveData(leaveStatus, "Leave status");
        this.leaveReason = leaveReason;

        validateLeaveDate(startDate, endDate);
        this.startDate = startDate;
        this.endDate = endDate;

    }

    private String validateLeaveData(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    private void validateLeaveDate (LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Invalid leave date");
        }
    }

    public String getLeaveID() {
        return leaveID;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = validateLeaveData(leaveType, "Leave type");
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        validateLeaveDate(startDate, this.endDate);
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        validateLeaveDate(this.startDate, endDate);
        this.endDate = endDate;
    }

    public String getLeaveReason() {
        return leaveReason;
    }

    public void setLeaveReason(String leaveReason) {
        this.leaveReason = leaveReason;
    }

    public String getLeaveStatus() {
        return leaveStatus;
    }

    public void setLeaveStatus(String leaveStatus) {
        this.leaveStatus = validateLeaveData(leaveStatus, "Leave status");
    }

    public int calculateLeaveDays(){
        long leaveDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return Math.toIntExact(leaveDays);
    }
}

