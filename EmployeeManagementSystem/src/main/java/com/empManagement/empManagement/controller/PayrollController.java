package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.dto.PayrollRequest;
import com.empManagement.empManagement.entity.Employee;
import com.empManagement.empManagement.entity.EmployeePayroll;
import com.empManagement.empManagement.service.EmployeeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
        payrollRequest.setPresentDays(20);
        payrollRequest.setLeaveDays(2);

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

        model.addAttribute("employee", emp);
        model.addAttribute("payrollRequest", payrollRequest);

        System.out.println("Employee Band Level: " + emp.getBandLevel());

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
            // Add similar for all other Double fields...

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
                .mapToDouble(p -> p.getTotalPayment())
                .sum();

        double averageMonthly = payrolls.isEmpty() ? 0 : totalPaid / payrolls.size();

        model.addAttribute("payrolls", payrolls);
        model.addAttribute("employee", emp);
        model.addAttribute("totalPaidAmount", totalPaid);
        model.addAttribute("averageMonthlyAmount", averageMonthly);
        return "pages/payroll/history";
    }

    @GetMapping("/monthly")
    public String monthlyPayrollReport(@RequestParam(value = "monthYear", required = false) String monthYear,
            Model model) {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM,yyyy"));
        String selectedMonth = (monthYear != null && !monthYear.isEmpty()) ? monthYear : currentMonth;

        List<EmployeePayroll> payrolls = employeeService.getPayrollsByMonth(selectedMonth);
        Double totalPayment = employeeService.getTotalPaymentByMonth(selectedMonth);
        List<String> monthYears = employeeService.getDistinctMonthYears();

        // Calculate department totals
        Map<String, Double> departmentTotals = payrolls.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getEmployee().getDepartment(),
                        Collectors.summingDouble(EmployeePayroll::getTotalPayment)));

        // Calculate payment breakdown
        double totalBasicSalary = payrolls.stream().mapToDouble(EmployeePayroll::getBasicSalary).sum();
        double totalAdditions = payrolls.stream()
                .mapToDouble(p -> p.getAllowance() + p.getOvertime() + p.getBonus() + p.getHome() +
                        p.getBusinessTrip() + p.getContinuedYear() + p.getHomeTownVisit() +
                        p.getManualAdjust() + p.getAttendancePerfect() + p.getExchangeBenefit())
                .sum();
        double totalDeductions = payrolls.stream()
                .mapToDouble(p -> p.getLeaveDeduction() + p.getLateDeduction() + p.getIncomeTax() +
                        p.getLoanReturn() + p.getSsc() + p.getCompanyTrip())
                .sum();

        model.addAttribute("payrolls", payrolls);
        model.addAttribute("totalPayment", totalPayment);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("monthYears", monthYears);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("departmentTotals", departmentTotals);
        model.addAttribute("totalBasicSalary", totalBasicSalary);
        model.addAttribute("totalAdditions", totalAdditions);
        model.addAttribute("totalDeductions", totalDeductions);

        return "pages/payroll/monthly-report";
    }

    private String convertMapToJson(Map<String, Double> map) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}