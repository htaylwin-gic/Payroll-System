package com.empManagement.empManagement.service;

import com.empManagement.empManagement.dto.PayrollRequest;
import com.empManagement.empManagement.entity.Employee;
import com.empManagement.empManagement.entity.EmployeePayroll;
import com.empManagement.empManagement.repository.EmployeePayrollRepository;
import com.empManagement.empManagement.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeePayrollRepository payrollRepository;

    @Autowired
    private SalaryCalculationService salaryCalculationService;

    public Employee save(Employee employee) {
        // If employee has salary-related fields, calculate salary
        if (employee.getBandLevel() != null || employee.getStartDate() != null) {
            salaryCalculationService.calculateAndUpdateEmployeeSalary(employee);
        }
        return employeeRepository.save(employee);
    }

    public void saveWithoutCalculation(Employee employee) {
        employeeRepository.save(employee);
    }

    public Employee getEmployeeById(int id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public void deleteById(int id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> getByKeyword(String keyword) {
        return employeeRepository.findByKeyword(keyword);
    }

    // Payroll operations
    @Transactional
    public EmployeePayroll calculatePayroll(Integer employeeId, String monthYear, PayrollRequest payrollRequest) {
        Employee employee = getEmployeeById(employeeId);

        // Check if payroll already exists for this month
        EmployeePayroll existingPayroll = payrollRepository.findByEmployeeAndMonthYear(employeeId, monthYear);

        EmployeePayroll payroll;
        if (existingPayroll != null) {
            payroll = existingPayroll;
        } else {
            payroll = new EmployeePayroll();
            payroll.setEmployee(employee);
            payroll.setMonthYear(monthYear);
        }

        // Set data
        payroll.setWorkingDays(payrollRequest.getWorkingDays() != null ? payrollRequest.getWorkingDays() : 0);
        payroll.setPresentDays(payrollRequest.getPresentDays() != null ? payrollRequest.getPresentDays() : 0);
        payroll.setLeaveDays(payrollRequest.getLeaveDays() != null ? payrollRequest.getLeaveDays() : 0);
        payroll.setLateDays(payrollRequest.getLateDays() != null ? payrollRequest.getLateDays() : 0);
        payroll.setOvertimeHours(payrollRequest.getOvertimeHours() != null ? payrollRequest.getOvertimeHours() : 0);

        payroll.setEducationAllowance(
                payrollRequest.getEducationAllowance() != null ? payrollRequest.getEducationAllowance() : 0.0);
        payroll.setEvaluationAllowance(
                payrollRequest.getEvaluationAllowance() != null ? payrollRequest.getEvaluationAllowance() : 0.0);
        payroll.setJapaneseJlptAllowance(
                payrollRequest.getJapaneseJlptAllowance() != null ? payrollRequest.getJapaneseJlptAllowance() : 0.0);
        payroll.setJapaneseNatAllowance(
                payrollRequest.getJapaneseNatAllowance() != null ? payrollRequest.getJapaneseNatAllowance() : 0.0);
        payroll.setEnglishAllowance(
                payrollRequest.getEnglishAllowance() != null ? payrollRequest.getEnglishAllowance() : 0.0);

        payroll.setMyanmarServiceYears(payrollRequest.getMyanmarYears());
        payroll.setGicjpServiceYears(payrollRequest.getGicjpYears());
        payroll.setAssignmentLevel(payrollRequest.getAssignmentLevel());
        payroll.setManagementLevel(payrollRequest.getManagementLevel());

        payroll.setLeaveDeduction(
                payrollRequest.getLeaveDeduction() != null ? payrollRequest.getLeaveDeduction() : 0.0);
        payroll.setLateDeduction(payrollRequest.getLateDeduction() != null ? payrollRequest.getLateDeduction() : 0.0);
        payroll.setIncomeTax(payrollRequest.getIncomeTax() != null ? payrollRequest.getIncomeTax() : 0.0);
        payroll.setLoanReturn(payrollRequest.getLoanReturn() != null ? payrollRequest.getLoanReturn() : 0.0);
        payroll.setSsc(payrollRequest.getSsc() != null ? payrollRequest.getSsc() : 0.0);
        payroll.setCompanyTrip(payrollRequest.getCompanyTrip() != null ? payrollRequest.getCompanyTrip() : 0.0);

        payroll.setAllowance(payrollRequest.getAllowance() != null ? payrollRequest.getAllowance() : 0.0);
        payroll.setOvertime(payrollRequest.getOvertime() != null ? payrollRequest.getOvertime() : 0.0);
        payroll.setHome(payrollRequest.getHome() != null ? payrollRequest.getHome() : 0.0);
        payroll.setBonus(payrollRequest.getBonus() != null ? payrollRequest.getBonus() : 0.0);
        payroll.setBusinessTrip(payrollRequest.getBusinessTrip() != null ? payrollRequest.getBusinessTrip() : 0.0);
        payroll.setContinuedYear(payrollRequest.getContinuedYear() != null ? payrollRequest.getContinuedYear() : 0.0);
        payroll.setHomeTownVisit(payrollRequest.getHomeTownVisit() != null ? payrollRequest.getHomeTownVisit() : 0.0);
        payroll.setManualAdjust(payrollRequest.getManualAdjust() != null ? payrollRequest.getManualAdjust() : 0.0);
        payroll.setAttendancePerfect(
                payrollRequest.getAttendancePerfect() != null ? payrollRequest.getAttendancePerfect() : 0.0);
        payroll.setExchangeBenefit(
                payrollRequest.getExchangeBenefit() != null ? payrollRequest.getExchangeBenefit() : 0.0);
        payroll.setTravelFee(payrollRequest.getTravelFee() != null ? payrollRequest.getTravelFee() : 0.0);

        // Calculate basic salary from employee
        Double basicSalary = employee.getSalary() != null ? employee.getSalary() : 0.0;
        payroll.setBasicSalary(basicSalary);

        // Calculate payroll amounts
        calculatePayrollAmounts(payroll);

        // Mark employee as having payroll processed
        employee.setIsPayroll("Yes");
        employeeRepository.save(employee);

        return payrollRepository.save(payroll);
    }

    private void calculatePayrollAmounts(EmployeePayroll payroll) {
        Double basicSalary = payroll.getBasicSalary() != null ? payroll.getBasicSalary() : 0.0;
        Double basicAllowance = payroll.getAllowance() != null ? payroll.getAllowance() : 0.0;

        // Calculate total allowance details
        Double totalAllowanceDetails = getValueOrZero(payroll.getEducationAllowance()) +
                getValueOrZero(payroll.getEvaluationAllowance()) +
                getValueOrZero(payroll.getJapaneseJlptAllowance()) +
                getValueOrZero(payroll.getJapaneseNatAllowance()) +
                getValueOrZero(payroll.getEnglishAllowance());

        // Calculate service allowance
        Double serviceAllowance = calculateServiceAllowance(
                payroll.getMyanmarServiceYears(),
                payroll.getGicjpServiceYears());

        // Calculate assignment and management allowances
        Double assignmentAllowance = calculateAssignmentAllowance(payroll.getAssignmentLevel());
        Double managementAllowance = calculateManagementAllowance(payroll.getManagementLevel());

        // Calculate overtime amount if hours are provided
        Double overtimeAmount = payroll.getOvertime() != null ? payroll.getOvertime() : 0.0;

        // Calculate basic salary after deductions
        Double basicAfterDeduction = basicSalary + basicAllowance;
        basicAfterDeduction -= getValueOrZero(payroll.getLeaveDeduction());
        basicAfterDeduction -= getValueOrZero(payroll.getLateDeduction());
        payroll.setBasicSalaryAfterDeduction(basicAfterDeduction);

        // Calculate net salary
        Double netSalary = basicAfterDeduction;

        // Add all allowances and additions
        netSalary += getValueOrZero(payroll.getHome());
        netSalary += getValueOrZero(payroll.getBonus());
        netSalary += getValueOrZero(payroll.getBusinessTrip());
        netSalary += getValueOrZero(payroll.getContinuedYear());
        netSalary += getValueOrZero(payroll.getHomeTownVisit());
        netSalary += getValueOrZero(payroll.getManualAdjust());
        netSalary += getValueOrZero(payroll.getAttendancePerfect());
        netSalary += getValueOrZero(payroll.getExchangeBenefit());
        netSalary += totalAllowanceDetails;
        netSalary += serviceAllowance;
        netSalary += assignmentAllowance;
        netSalary += managementAllowance;
        netSalary += overtimeAmount;

        // Subtract deductions
        netSalary -= getValueOrZero(payroll.getIncomeTax());
        netSalary -= getValueOrZero(payroll.getLoanReturn());
        netSalary -= getValueOrZero(payroll.getSsc());
        netSalary -= getValueOrZero(payroll.getCompanyTrip());

        payroll.setNetSalary(netSalary);

        // Calculate total payment
        Double totalPayment = netSalary + getValueOrZero(payroll.getTravelFee());
        payroll.setTotalPayment(totalPayment);
    }

    private Double calculateManagementAllowance(String managementLevel) {
        if (managementLevel == null)
            return 0.0;

        switch (managementLevel) {
            case "Sub Leader":
                return 500000.0;
            case "Leader":
                return 600000.0;
            case "Sub Manager":
                return 700000.0;
            case "Manager":
                return 1100000.0;
            default:
                return 0.0;
        }
    }

    private Double calculateAssignmentAllowance(String assignmentLevel) {
        if (assignmentLevel == null)
            return 0.0;

        switch (assignmentLevel) {
            case "Sub TL":
                return 100000.0;
            case "TL":
                return 200000.0;
            case "PL":
                return 200000.0;
            case "Senior PL":
                return 300000.0;
            default:
                return 0.0;
        }
    }

    private Double calculateServiceAllowance(Integer myanmarYears, Integer gicjpYears) {
        double allowance = 0;

        if (myanmarYears != null && myanmarYears >= 6) {
            allowance += 90000;
        } else if (myanmarYears != null && myanmarYears >= 1) {
            allowance += myanmarYears * 15000;
        }

        if (gicjpYears != null && gicjpYears >= 6) {
            allowance += 300000;
        } else if (gicjpYears != null && gicjpYears >= 1) {
            allowance += gicjpYears * 50000;
        }

        return allowance;
    }

    private Double getValueOrZero(Double value) {
        return value != null ? value : 0.0;
    }

    public List<EmployeePayroll> getEmployeePayroll(Integer employeeId) {
        return payrollRepository.findByEmployeeId(employeeId);
    }

    public List<EmployeePayroll> getPayrollsByMonth(String monthYear) {
        return payrollRepository.findByMonthYear(monthYear);
    }

    public void deletePayroll(Long payrollId) {
        payrollRepository.deleteById(payrollId);
    }

    public EmployeePayroll getPayrollById(Long payrollId) {
        return payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + payrollId));
    }

    public List<String> getDistinctMonthYears() {
        return payrollRepository.findDistinctMonthYears();
    }

    public Double getTotalPaymentByMonth(String monthYear) {
        Double total = payrollRepository.getTotalPaymentByMonth(monthYear);
        return total != null ? total : 0.0;
    }

    // Status management
    public List<Employee> getEmployeesByStatus(String status) {
        if (status == null || status.isEmpty() || "all".equalsIgnoreCase(status)) {
            return employeeRepository.findAll();
        }
        return employeeRepository.findByStatus(status);
    }

    public Employee updateEmployeeStatus(int id, String status) {
        Employee employee = getEmployeeById(id);
        employee.setStatus(status);
        return employeeRepository.save(employee);
    }

    public Map<String, Long> getStatusCounts() {
        List<Employee> allEmployees = getAllEmployees();

        Map<String, Long> counts = new HashMap<>();
        counts.put("Total", (long) allEmployees.size());
        counts.put("Active", allEmployees.stream()
                .filter(e -> "Active".equals(e.getStatus()))
                .count());
        counts.put("Inactive", allEmployees.stream()
                .filter(e -> "Inactive".equals(e.getStatus()))
                .count());
        counts.put("On Leave", allEmployees.stream()
                .filter(e -> "On Leave".equals(e.getStatus()))
                .count());
        counts.put("Resigned", allEmployees.stream()
                .filter(e -> "Resigned".equals(e.getStatus()))
                .count());
        counts.put("Terminated", allEmployees.stream()
                .filter(e -> "Terminated".equals(e.getStatus()))
                .count());

        return counts;
    }

    // Salary management
    public Employee updateEmployeeWithSalary(Employee updatedEmployee) {
        Employee employee = getEmployeeById(updatedEmployee.getId());

        // Update fields
        if (updatedEmployee.getBandLevel() != null) {
            employee.setBandLevel(updatedEmployee.getBandLevel());
        }
        if (updatedEmployee.getEducationLevel() != null) {
            employee.setEducationLevel(updatedEmployee.getEducationLevel());
        }
        if (updatedEmployee.getEvaluationGrade() != null) {
            employee.setEvaluationGrade(updatedEmployee.getEvaluationGrade());
        }
        if (updatedEmployee.getJapaneseLevel() != null) {
            employee.setJapaneseLevel(updatedEmployee.getJapaneseLevel());
        }
        if (updatedEmployee.getEnglishToeicScore() != null) {
            employee.setEnglishToeicScore(updatedEmployee.getEnglishToeicScore());
        }
        if (updatedEmployee.getEnglishToeflScore() != null) {
            employee.setEnglishToeflScore(updatedEmployee.getEnglishToeflScore());
        }
        if (updatedEmployee.getEnglishIeltsScore() != null) {
            employee.setEnglishIeltsScore(updatedEmployee.getEnglishIeltsScore());
        }
        if (updatedEmployee.getAssignmentLevel() != null) {
            employee.setAssignmentLevel(updatedEmployee.getAssignmentLevel());
        }
        if (updatedEmployee.getManagementLevel() != null) {
            employee.setManagementLevel(updatedEmployee.getManagementLevel());
        }
        if (updatedEmployee.getHouseAllowance() != null) {
            employee.setHouseAllowance(updatedEmployee.getHouseAllowance());
        }
        if (updatedEmployee.getTransportationAllowance() != null) {
            employee.setTransportationAllowance(updatedEmployee.getTransportationAllowance());
        }
        if (updatedEmployee.getPerfectAttendanceAllowance() != null) {
            employee.setPerfectAttendanceAllowance(updatedEmployee.getPerfectAttendanceAllowance());
        }

        // Calculate and update salary
        salaryCalculationService.calculateAndUpdateEmployeeSalary(employee);

        return employeeRepository.save(employee);
    }

    public Map<String, Object> getSalaryBreakdown(int employeeId) {
        Employee employee = getEmployeeById(employeeId);

        Map<String, Object> breakdown = new LinkedHashMap<>();

        // Basic salary
        breakdown.put("Basic Salary",
                employee.getSalary() != null ? String.format("$%,.2f", employee.getSalary()) : "N/A");

        // Allowances
        Map<String, String> allowances = new LinkedHashMap<>();
        addIfNotEmpty(allowances, "Education Allowance", employee.getEducationAllowance());
        addIfNotEmpty(allowances, "Evaluation Allowance", employee.getEvaluationAllowance());
        addIfNotEmpty(allowances, "Japanese Allowance", employee.getJapaneseAllowance());
        addIfNotEmpty(allowances, "English Allowance", employee.getEnglishAllowance());
        addIfNotEmpty(allowances, "Service Allowance", employee.getServiceAllowance());
        addIfNotEmpty(allowances, "Assignment Allowance", employee.getAssignmentAllowance());
        addIfNotEmpty(allowances, "Management Allowance", employee.getManagementAllowance());
        addIfNotEmpty(allowances, "House Allowance", employee.getHouseAllowance());
        addIfNotEmpty(allowances, "Transportation Allowance", employee.getTransportationAllowance());
        addIfNotEmpty(allowances, "Perfect Attendance", employee.getPerfectAttendanceAllowance());
        breakdown.put("Allowances", allowances);

        // Deductions
        Map<String, String> deductions = new LinkedHashMap<>();
        addIfNotEmpty(deductions, "SSC Deduction", employee.getSscDeduction());
        addIfNotEmpty(deductions, "Company Trip", employee.getCompanyTripDeduction());
        breakdown.put("Deductions", deductions);

        // Totals
        breakdown.put("Total Basic Salary", formatCurrency(employee.getTotalBasicSalary()));
        breakdown.put("Total Allowances", formatCurrency(employee.getTotalAllowances()));
        breakdown.put("Total Deductions", formatCurrency(employee.getTotalDeductions()));
        breakdown.put("Net Salary", formatCurrency(employee.getNetSalary()));

        return breakdown;
    }

    private void addIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.isEmpty() && !"0".equals(value) && !"0.0".equals(value)) {
            map.put(key, formatCurrency(value));
        }
    }

    private String formatCurrency(String value) {
        if (value == null || value.isEmpty()) {
            return "$0.00";
        }
        try {
            double amount = Double.parseDouble(value);
            return String.format("$%,.2f", amount);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    // Dashboard statistics
    public long getEmployeeCount() {
        return employeeRepository.count();
    }

    public double getAverageSalary() {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty())
            return 0.0;

        double total = employees.stream()
                .mapToDouble(emp -> {
                    if (emp.getNetSalary() != null && !emp.getNetSalary().isEmpty()) {
                        try {
                            return Double.parseDouble(emp.getNetSalary());
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    }
                    return 0.0;
                })
                .sum();

        return total / employees.size();
    }

    public double getTotalSalary() {
        return employeeRepository.findAll().stream()
                .mapToDouble(emp -> {
                    if (emp.getNetSalary() != null && !emp.getNetSalary().isEmpty()) {
                        try {
                            return Double.parseDouble(emp.getNetSalary());
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    }
                    return 0.0;
                })
                .sum();
    }

    public long getCountOfEmployeesWithAttendance() {
        return employeeRepository.countWithAttendance();
    }

    public long getCountOfEmployeesWithOutAttendance() {
        return employeeRepository.countWithoutAttendance();
    }

    public List<Employee> getEmployeesWithNoAttendance() {
        return employeeRepository.findEmployeesWithNoAttendance();
    }

    // Department statistics
    public long getCountOfEmployeesIT() {
        return employeeRepository.countByDepartment("IT");
    }

    public long getCountOfEmployeesHRM() {
        return employeeRepository.countByDepartment("HRM");
    }

    public long getCountOfEmployeesTechnical() {
        return employeeRepository.countByDepartment("Technical");
    }

    public long getCountOfEmployeesDesign() {
        return employeeRepository.countByDepartment("Design");
    }

    public long getCountOfEmployeesFinance() {
        return employeeRepository.countByDepartment("Finance");
    }

    public Map<String, Double> getSectionPercentages() {
        long totalEmployees = getEmployeeCount();
        if (totalEmployees == 0)
            return new HashMap<>();

        Map<String, Double> percentages = new HashMap<>();

        percentages.put("IT", (getCountOfEmployeesIT() * 100.0) / totalEmployees);
        percentages.put("HRM", (getCountOfEmployeesHRM() * 100.0) / totalEmployees);
        percentages.put("Technical", (getCountOfEmployeesTechnical() * 100.0) / totalEmployees);
        percentages.put("Design", (getCountOfEmployeesDesign() * 100.0) / totalEmployees);
        percentages.put("Finance", (getCountOfEmployeesFinance() * 100.0) / totalEmployees);

        // Calculate "Other" for remaining percentage
        double totalPercentage = percentages.values().stream().mapToDouble(Double::doubleValue).sum();
        percentages.put("Other", 100.0 - totalPercentage);

        return percentages;
    }

    public List<EmployeePayroll> getAllPayrolls() {
        return payrollRepository.findAll();
    }

    public Object getPayrollReportData(String selectedMonth) {
        throw new UnsupportedOperationException("Unimplemented method 'getPayrollReportData'");
    }
}