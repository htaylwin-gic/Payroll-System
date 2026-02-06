package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.dto.PayrollRequest;
import com.empManagement.empManagement.entity.Employee;
import com.empManagement.empManagement.entity.EmployeePayroll;
import com.empManagement.empManagement.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/payroll")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class PayrollController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/manage")
    public String payrollManagement(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        model.addAttribute("totalSalary", employeeService.getTotalSalary());
        model.addAttribute("averageSalary", employeeService.getAverageSalary());
        return "pages/payroll/management";
    }

    @GetMapping("/calculate/{employeeId}")
    public String calculatePayrollForm(@PathVariable("employeeId") int employeeId, Model model) {
        Employee emp = employeeService.getEmployeeById(employeeId);
        if (emp == null) {
            return "redirect:/payroll/manage";
        }

        PayrollRequest payrollRequest = new PayrollRequest();
        payrollRequest.setEmployeeId(employeeId);
        payrollRequest.setMonthYear(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM,yyyy")));

        // Set default values
        payrollRequest.setWorkingDays(22);
        payrollRequest.setLeaveDays(0);
        payrollRequest.setPresentDays(22);

        // Calculate Present Days (Working Days - Leave Days)
        int presentDays = payrollRequest.getWorkingDays() - payrollRequest.getLeaveDays();
        payrollRequest.setPresentDays(presentDays);

        // Set employee data
        if (emp.getMyanmarServiceYears() != null) {
            payrollRequest.setMyanmarYears(emp.getMyanmarServiceYears());
        }

        if (emp.getGicpServiceYears() != null) {
            payrollRequest.setGicjpYears(emp.getGicpServiceYears());
        }

        if (emp.getAssignmentLevel() != null) {
            payrollRequest.setAssignmentLevel(emp.getAssignmentLevel());
        }

        if (emp.getManagementLevel() != null) {
            payrollRequest.setManagementLevel(emp.getManagementLevel());
        }

        // AUTO-POPULATE ALLOWANCES
        payrollRequest.setEducationAllowance(emp.getEducationAllowanceValue());
        payrollRequest.setEvaluationAllowance(emp.getEvaluationAllowanceValue());
        payrollRequest.setJapaneseJlptAllowance(emp.getJapaneseJlptAllowanceValue());
        payrollRequest.setJapaneseNatAllowance(emp.getJapaneseNatAllowanceValue());
        payrollRequest.setEnglishAllowance(emp.getEnglishAllowanceValue());

        // Set transportation fee if available
        if (emp.getTransportationFee() != null) {
            double travelFee = emp.getTransportationFee() * presentDays;
            payrollRequest.setTravelFee(travelFee);
        }

        // Set other default values
        payrollRequest.setIncomeTax(0.0);
        payrollRequest.setLoanReturn(0.0);
        payrollRequest.setSsc(0.0);
        payrollRequest.setBonus(0.0);
        payrollRequest.setBusinessTrip(0.0);
        payrollRequest.setOvertime(0.0);
        payrollRequest.setAllowance(0.0);
        payrollRequest.setLeaveDeduction(0.0);
        payrollRequest.setLateDeduction(0.0);

        model.addAttribute("employee", emp);
        model.addAttribute("payrollRequest", payrollRequest);

        System.out.println("Employee Band Level: " + emp.getBandLevel());
        System.out.println("Education Allowance: " + emp.getEducationAllowanceValue());
        System.out.println("Japanese JLPT Allowance: " + emp.getJapaneseJlptAllowanceValue());

        return "pages/payroll/calculate";
    }

    @PostMapping("/calculate")
    public String calculatePayroll(@ModelAttribute PayrollRequest payrollRequest,
            RedirectAttributes redirectAttributes) {
        try {
            if (payrollRequest.getIncomeTax() == null)
                payrollRequest.setIncomeTax(0.0);
            if (payrollRequest.getLoanReturn() == null)
                payrollRequest.setLoanReturn(0.0);
            if (payrollRequest.getSsc() == null)
                payrollRequest.setSsc(0.0);
            if (payrollRequest.getBonus() == null)
                payrollRequest.setBonus(0.0);
            if (payrollRequest.getBusinessTrip() == null)
                payrollRequest.setBusinessTrip(0.0);
            if (payrollRequest.getOvertime() == null)
                payrollRequest.setOvertime(0.0);
            if (payrollRequest.getAllowance() == null)
                payrollRequest.setAllowance(0.0);
            if (payrollRequest.getLeaveDeduction() == null)
                payrollRequest.setLeaveDeduction(0.0);
            if (payrollRequest.getLateDeduction() == null)
                payrollRequest.setLateDeduction(0.0);
            if (payrollRequest.getTravelFee() == null)
                payrollRequest.setTravelFee(0.0);
            if (payrollRequest.getHome() == null)
                payrollRequest.setHome(0.0);
            if (payrollRequest.getContinuedYear() == null)
                payrollRequest.setContinuedYear(0.0);
            if (payrollRequest.getHomeTownVisit() == null)
                payrollRequest.setHomeTownVisit(0.0);
            if (payrollRequest.getManualAdjust() == null)
                payrollRequest.setManualAdjust(0.0);
            if (payrollRequest.getAttendancePerfect() == null)
                payrollRequest.setAttendancePerfect(0.0);
            if (payrollRequest.getExchangeBenefit() == null)
                payrollRequest.setExchangeBenefit(0.0);
            if (payrollRequest.getCompanyTrip() == null)
                payrollRequest.setCompanyTrip(0.0);

            EmployeePayroll payroll = employeeService.calculatePayroll(
                    payrollRequest.getEmployeeId(),
                    payrollRequest.getMonthYear(),
                    payrollRequest);

            redirectAttributes.addFlashAttribute("success",
                    "Payroll calculated successfully for " + payroll.getMonthYear());
            return "redirect:/payroll/view/" + payrollRequest.getEmployeeId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error calculating payroll: " + e.getMessage());
            return "redirect:/payroll/calculate/" + payrollRequest.getEmployeeId();
        }
    }

    @GetMapping("/view/{employeeId}")
    public String viewEmployeePayroll(@PathVariable("employeeId") int employeeId, Model model) {
        List<EmployeePayroll> payrolls = employeeService.getEmployeePayroll(employeeId);
        Employee emp = employeeService.getEmployeeById(employeeId);

        // Calculate totals here to avoid SpEL errors in HTML
        double totalPaid = payrolls.stream()
                .mapToDouble(p -> p.getTotalPayment() != null ? p.getTotalPayment() : 0)
                .sum();

        double averageMonthly = payrolls.isEmpty() ? 0 : totalPaid / payrolls.size();

        model.addAttribute("payrolls", payrolls);
        model.addAttribute("employee", emp);
        model.addAttribute("totalPaidAmount", totalPaid);
        model.addAttribute("averageMonthlyAmount", averageMonthly);
        return "pages/payroll/history";
    }

    @PostMapping("/bulk-calculate")
    public String bulkCalculatePayroll(
            @RequestParam("employeeIds") String employeeIdsStr,
            @RequestParam("month") String month,
            RedirectAttributes redirectAttributes) {
        try {
            if (employeeIdsStr == null || employeeIdsStr.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No employees selected");
                return "redirect:/payroll/manage";
            }

            // 1. Parse IDs from the comma-separated string
            List<Integer> employeeIds = Arrays.stream(employeeIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            int successCount = 0;
            List<String> failedEmployees = new ArrayList<>();

            // 2. Format the month from YYYY-MM to MMM,yyyy if necessary
            String formattedMonth = month;
            if (month.matches("\\d{4}-\\d{2}")) {
                LocalDate date = LocalDate.parse(month + "-01");
                formattedMonth = date.format(DateTimeFormatter.ofPattern("MMM,yyyy"));
            }

            // 3. Process each employee
            for (Integer id : employeeIds) {
                try {
                    Employee emp = employeeService.getEmployeeById(id);
                    if (emp == null)
                        continue;

                    // Create default request
                    PayrollRequest req = new PayrollRequest();
                    req.setEmployeeId(id);
                    req.setMonthYear(formattedMonth);
                    req.setWorkingDays(22); // Default
                    req.setLeaveDays(0);
                    req.setPresentDays(22);

                    // Map employee allowances to the request
                    req.setEducationAllowance(emp.getEducationAllowanceValue());
                    req.setJapaneseJlptAllowance(emp.getJapaneseJlptAllowanceValue());
                    req.setEnglishAllowance(emp.getEnglishAllowanceValue());

                    if (emp.getTransportationFee() != null) {
                        req.setTravelFee(emp.getTransportationFee() * 22);
                    }

                    // Call service to persist and calculate
                    employeeService.calculatePayroll(id, formattedMonth, req);
                    successCount++;
                } catch (Exception e) {
                    failedEmployees.add("ID " + id);
                }
            }

            // 4. Feedback to user
            String msg = "Successfully calculated payroll for " + successCount + " employees.";
            if (!failedEmployees.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", msg + " Failed: " + String.join(", ", failedEmployees));
            } else {
                redirectAttributes.addFlashAttribute("success", msg);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Bulk error: " + e.getMessage());
        }
        return "redirect:/payroll/manage";
    }

    @GetMapping("/monthly")
    public String monthlyPayrollReport(@RequestParam(value = "monthYear", required = false) String monthYear,
            Model model) {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM,yyyy"));
        String selectedMonth = (monthYear != null && !monthYear.isEmpty()) ? monthYear : currentMonth;

        List<EmployeePayroll> payrolls = employeeService.getPayrollsByMonth(selectedMonth);
        Double totalPayment = employeeService.getTotalPaymentByMonth(selectedMonth);
        List<String> monthYears = employeeService.getDistinctMonthYears();

        // Calculate Highest and Lowest Salary safely
        double highestSalary = payrolls.stream()
                .mapToDouble(p -> p.getTotalPayment() != null ? p.getTotalPayment() : 0)
                .max().orElse(0.0);

        double lowestSalary = payrolls.stream()
                .mapToDouble(p -> p.getTotalPayment() != null ? p.getTotalPayment() : 0)
                .min().orElse(0.0);

        // Calculate Average Attendance Rate
        double attendanceRate = 0.0;
        if (!payrolls.isEmpty()) {
            double totalPresent = payrolls.stream()
                    .mapToDouble(p -> p.getPresentDays() != null ? p.getPresentDays() : 0).sum();
            double totalWorking = payrolls.stream()
                    .mapToDouble(p -> p.getWorkingDays() != null ? p.getWorkingDays() : 0).sum();
            attendanceRate = (totalWorking > 0) ? (totalPresent / totalWorking) * 100 : 0;
        }

        // Existing department and total calculations
        Map<String, Double> departmentTotals = payrolls.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getEmployee() != null ? p.getEmployee().getDepartment() : "Unknown",
                        Collectors.summingDouble(p -> p.getTotalPayment() != null ? p.getTotalPayment() : 0)));

        double totalBasicSalary = payrolls.stream()
                .mapToDouble(p -> p.getBasicSalary() != null ? p.getBasicSalary() : 0).sum();

        // Pass all calculated data to the view
        model.addAttribute("payrolls", payrolls);
        model.addAttribute("totalPayment", totalPayment != null ? totalPayment : 0.0);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("monthYears", monthYears);
        model.addAttribute("departmentTotals", departmentTotals);
        model.addAttribute("totalBasicSalary", totalBasicSalary);

        // New attributes for the report
        model.addAttribute("highestSalary", highestSalary);
        model.addAttribute("lowestSalary", lowestSalary);
        model.addAttribute("attendanceRate", String.format("%.2f", attendanceRate));

        return "pages/payroll/monthly-report";
    }

    @GetMapping("/export/selected")
    public ResponseEntity<byte[]> exportSelectedPayrolls(
            @RequestParam("employeeIds") String employeeIdsStr) {
        try {
            // Parse employee IDs
            List<Integer> employeeIds = Arrays.stream(employeeIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            // Get payroll data for selected employees
            List<EmployeePayroll> payrolls = new ArrayList<>();
            for (Integer id : employeeIds) {
                payrolls.addAll(employeeService.getEmployeePayroll(id));
            }

            // Generate CSV data
            StringBuilder csvData = new StringBuilder();
            csvData.append("Employee ID,Employee Name,Month,Basic Salary,Allowances,Total Payment\n");

            for (EmployeePayroll payroll : payrolls) {
                csvData.append(payroll.getEmployee().getId()).append(",");
                csvData.append(payroll.getEmployee().getFull_name()).append(",");
                csvData.append(payroll.getMonthYear()).append(",");
                csvData.append(payroll.getBasicSalary() != null ? payroll.getBasicSalary() : 0).append(",");
                csvData.append(payroll.getAllowance() != null ? payroll.getAllowance() : 0).append(",");
                csvData.append(payroll.getTotalPayment() != null ? payroll.getTotalPayment() : 0).append("\n");
            }

            byte[] csvBytes = csvData.toString().getBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=selected_payrolls_" +
                                    LocalDate.now() + ".csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/all")
    public ResponseEntity<byte[]> exportAllPayrolls() {
        try {
            List<EmployeePayroll> allPayrolls = employeeService.getAllPayrolls();

            // Generate CSV data
            StringBuilder csvData = new StringBuilder();
            csvData.append("Employee ID,Employee Name,Department,Month,Basic Salary,Allowances,Total Payment,Status\n");

            for (EmployeePayroll payroll : allPayrolls) {
                csvData.append(payroll.getEmployee().getId()).append(",");
                csvData.append(payroll.getEmployee().getFull_name()).append(",");
                csvData.append(payroll.getEmployee().getDepartment()).append(",");
                csvData.append(payroll.getMonthYear()).append(",");
                csvData.append(payroll.getBasicSalary() != null ? payroll.getBasicSalary() : 0).append(",");
                csvData.append(payroll.getAllowance() != null ? payroll.getAllowance() : 0).append(",");
                csvData.append(payroll.getTotalPayment() != null ? payroll.getTotalPayment() : 0).append(",");
                csvData.append("Processed").append("\n");
            }

            byte[] csvBytes = csvData.toString().getBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=all_payrolls_" +
                                    LocalDate.now() + ".csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports")
    public String generateReports(@RequestParam(value = "month", required = false) String month,
            Model model) {
        String selectedMonth = (month != null && !month.isEmpty()) ? month
                : LocalDate.now().format(DateTimeFormatter.ofPattern("MMM,yyyy"));

        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("reportData", employeeService.getPayrollReportData(selectedMonth));
        return "pages/payroll/reports";
    }

    @GetMapping("/edit/{id}")
    public String editPayroll(@PathVariable Long id, Model model) {
        EmployeePayroll payroll = employeeService.getPayrollById(id);
        Employee emp = payroll.getEmployee();

        PayrollRequest payrollRequest = new PayrollRequest();
        payrollRequest.setId(id);
        payrollRequest.setEmployeeId(emp.getId());
        payrollRequest.setMonthYear(payroll.getMonthYear());
        payrollRequest.setWorkingDays(payroll.getWorkingDays());
        payrollRequest.setLeaveDays(payroll.getLeaveDays());
        payrollRequest.setPresentDays(payroll.getPresentDays());
        payrollRequest.setOvertimeHours(payroll.getOvertimeHours());

        payrollRequest.setBasicSalary(payroll.getBasicSalary());
        payrollRequest.setAllowance(payroll.getAllowance());
        payrollRequest.setBonus(payroll.getBonus());
        payrollRequest.setTravelFee(payroll.getTravelFee());

        model.addAttribute("payrollRequest", payrollRequest);
        model.addAttribute("employee", emp);

        return "pages/payroll/calculate";
    }

    @GetMapping("/delete/{id}")
    public String deletePayroll(@PathVariable Long id, RedirectAttributes ra) {
        try {
            EmployeePayroll payroll = employeeService.getPayrollById(id);
            int employeeId = payroll.getEmployee().getId();

            employeeService.deletePayroll(id);

            ra.addFlashAttribute("success", "Payroll record deleted successfully.");
            return "redirect:/payroll/view/" + employeeId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting record: " + e.getMessage());
            return "redirect:/payroll/manage";
        }
    }

    @GetMapping("/download-slip/{id}")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Long id) {
        try {
            EmployeePayroll payroll = employeeService.getPayrollById(id);

            StringBuilder csv = new StringBuilder();
            csv.append("Field,Value\n");
            csv.append("Employee Name,").append(payroll.getEmployee().getFull_name()).append("\n");
            csv.append("Month,").append(payroll.getMonthYear()).append("\n");
            csv.append("Basic Salary,").append(payroll.getBasicSalary()).append("\n");
            csv.append("Net Salary,").append(payroll.getNetSalary()).append("\n");
            csv.append("Total Payment,").append(payroll.getTotalPayment()).append("\n");

            byte[] data = csv.toString().getBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip_" + id + ".csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export-history/{employeeId}")
    public ResponseEntity<byte[]> exportEmployeeHistory(@PathVariable Integer employeeId) {
        List<EmployeePayroll> history = employeeService.getPayrollHistoryByEmployee(employeeId);

        StringBuilder csv = new StringBuilder("Month,Basic,Allowance,Deductions,Net Salary\n");
        for (EmployeePayroll p : history) {
            csv.append(p.getMonthYear()).append(",")
                    .append(p.getBasicSalary()).append(",")
                    .append(p.getAllowance()).append(",")
                    .append(p.getLeaveDeduction()).append(",")
                    .append(p.getNetSalary()).append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=history_emp_" + employeeId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString().getBytes());
    }

}