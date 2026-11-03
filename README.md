# MotorPH Payroll System

## 1. Project Overview

The MotorPH Payroll System is a Java desktop application built with Swing. It manages employee information, attendance, leave requests, payroll calculations, payroll records, reports, and employee payslip viewing.

The application uses CSV files stored in the `data` folder as its data source. Keep the `data` folder in the project root when running the application.

## 2. Main Features

### Login and access
- Administrator login.
- Employee login using accounts maintained by the administrator.
- Employees are directed to an employee portal where they can view their own information and payslips.

### Employee Management
- View and manage employee records.
- Employee information is stored in `data/employees.csv`.

### Attendance Management
- Maintain employee attendance records, including date, time in, and time out.
- Attendance data is stored in `data/attendance.csv`.
- Attendance records are used as an input to payroll calculations.

### Leave Management
- Maintain employee leave requests and their status.
- Leave data is stored in `data/leaves.csv`.

### Payroll Processing
- Calculate semi-monthly payroll by employee and cutoff.
- Select a payroll month and cutoff. The cutoff dates are:
  - **FIRST:** 1st to 15th of the selected month.
  - **SECOND:** 16th to the last calendar day of the selected month.
- Payroll date must be after the cutoff end date.
- Payroll records are saved to `data/Payroll.csv`.
- The Payroll table loads saved records from the payroll CSV file.

### Payroll Reports and Payslips
- View saved payroll records.
- Generate or view employee payslip information from saved payroll data.
- Export payroll information using the available report functions.

## 3. Technology and Dependencies

- **Language:** Java
- **GUI:** Java Swing
- **Data storage:** CSV files
- **IDE:** IntelliJ IDEA (recommended)
- **Libraries included in `lib/`:**
  - `opencsv-3.8.jar`
  - `commons-csv-1.1.jar`

Use a JDK compatible with the Java version configured in the project. The project does not require a database server.

## 4. Project Structure

```text
MotorPH Payroll System/
├── src/                  Java source files
├── data/                 CSV data files used by the application
│   ├── employees.csv     Employee master records
│   ├── attendance.csv    Attendance records
│   ├── leaves.csv        Leave records
│   ├── User.csv          Employee login accounts
│   └── Payroll.csv       Saved payroll records
├── lib/                  Required third-party JAR libraries
└── README.md             Project documentation
```

## 5. How to Open and Run the Application

### Using IntelliJ IDEA

1. Extract the project ZIP file.
2. Open IntelliJ IDEA.
3. Select **Open** and choose the `MotorPH Payroll System` project folder (the folder containing `src`, `data`, and `lib`).
4. Allow IntelliJ IDEA to load the project.
5. Confirm that the JDK is configured under **File/Project Structure > Project**.
6. Add the JAR files in `lib/` as project dependencies if IntelliJ does not recognize them automatically:
   - `opencsv-3.8.jar`
   - `commons-csv-1.1.jar`
7. Open `src/Main.java`.
8. Run `Main`.

Run the application with the project root as the working directory. This is important because the application refers to its CSV files using relative paths such as `data/employees.csv`.

### Using the command line

From the `MotorPH Payroll System` project root, compile the source files:

```bash
mkdir -p out
javac -cp "lib/*" -d out src/*.java
```

Run the application:

```bash
java -cp "out:lib/*" Main
```

On Windows, use a semicolon instead of a colon in the runtime classpath:

```bat
java -cp "out;lib/*" Main
```

Keep the terminal's working directory at the project root when running the commands.

## 6. Login

The administrator credentials currently defined in `src/Login.java` are:

| Account | Username | Password |
|---|---|---|
| Administrator | `admin` | `admin123` |

Employee accounts are maintained by the administrator and stored in `data/User.csv`. Employee users cannot self-register through the login screen.

**Security warning:** The administrator credentials are hardcoded in the source code, and employee passwords are stored as plain text in the CSV file. This is suitable only for a classroom/demo project. Do not use these credentials or this password-storage approach for a production payroll system. Change the default administrator password before sharing the project.

## 7. CSV Data Files

The application expects the following files in the `data` folder:

| File | Purpose |
|---|---|
| `employees.csv` | Employee master data and compensation details |
| `attendance.csv` | Daily attendance records |
| `leaves.csv` | Leave requests and approval status |
| `User.csv` | Employee login accounts |
| `Payroll.csv` | Calculated and saved payroll records |

### Important data-handling notes

- Back up the entire `data` folder before making changes or replacing project files.
- Do not delete or rename the CSV files while the application is running.
- Do not change CSV column headers unless the corresponding Java code is also updated.
- Keep the CSV files in UTF-8 format where possible.
- Open and edit CSV files carefully. Spreadsheet applications may change date, number, or identifier formats.
- Payroll calculations depend on valid employee and attendance records. Check the employee number and attendance dates if an employee's payroll cannot be calculated.
- Saved payroll history is stored in `data/Payroll.csv`. Preserve this file when updating the application.

## 8. Payroll Processing Notes

- The payroll cycle is semi-monthly.
- The FIRST cutoff covers days 1–15.
- The SECOND cutoff covers day 16 through the final day of the month.
- The payroll date must be later than the cutoff end date.
- Regular employee compensation is based on the employee's monthly salary and the payroll cutoff rules implemented in the application.
- Contract employee compensation is based on recorded attendance days and the applicable daily rate.
- Attendance, overtime inputs, allowances, other income, statutory deductions, loans, and other deductions affect the calculated result according to the current implementation.

## 9. Troubleshooting

### The application cannot find CSV files
- Confirm that the `data` folder is inside the project root.
- Run the application with the project root as its working directory.
- Confirm that the expected CSV files exist and have not been renamed.

### Employee records do not appear
- Check `data/employees.csv`.
- Confirm that the header row is present and the employee records follow the expected column order.
- Check that the file is saved as a valid CSV.

### Attendance records are not detected during payroll calculation
- Check `data/attendance.csv`.
- Confirm that the employee number and attendance dates match the selected employee and cutoff.
- Confirm that the file contains valid rows and actual line breaks between records.
- Verify that attendance dates fall within the selected cutoff.

### Saved payroll does not appear in the Payroll table
- Confirm that `data/Payroll.csv` exists and contains a header row and payroll records.
- Confirm that the application is using the intended project folder and its `data` directory.
- Check that the payroll CSV headers have not been changed.
- Reopen or refresh the Payroll page after confirming the file contents.

### Compilation errors occur
- Confirm that a JDK is configured.
- Confirm that both JAR files in `lib/` are included in the classpath.
- Compile from the project root using the commands above.

## 10. Important Disclaimer

This application is intended for educational and demonstration purposes. It has not been certified as a production payroll or accounting system. Validate employee data, attendance, payroll calculations, statutory contributions, tax treatment, and deductions independently before relying on any generated payroll output.
