package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.entity.Employee;
import com.empManagement.empManagement.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@Controller
@RequestMapping("/status")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class StatusController {

    @Autowired
    private EmployeeService employeeService;

    private static final List<String> STATUS_TYPES = Arrays.asList(
            "Active", "Inactive", "On Leave", "Resigned", "Terminated");

    @PostMapping("/bulk-update")
    public String bulkUpdateStatus(
            @RequestParam("employeeIds") String employeeIds, // Changed to String to handle comma-separated values
            @RequestParam("newStatus") String newStatus,
            @RequestParam(value = "reason", required = false) String reason,
            RedirectAttributes redirectAttributes) {

        try {
            // Parse comma-separated employee IDs
            List<Integer> ids = new ArrayList<>();
            if (employeeIds != null && !employeeIds.isEmpty()) {
                String[] idArray = employeeIds.split(",");
                for (String id : idArray) {
                    try {
                        ids.add(Integer.parseInt(id.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid IDs
                    }
                }
            }

            if (ids.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No employees selected.");
                return "redirect:/status";
            }

            int updatedCount = 0;
            for (Integer id : ids) {
                Employee emp = employeeService.updateEmployeeStatus(id, newStatus);
                if (emp != null) {
                    updatedCount++;
                }
            }

            redirectAttributes.addFlashAttribute("success",
                    "Successfully updated " + updatedCount + " employee(s) to " + newStatus + " status.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error updating employees: " + e.getMessage());
        }

        return "redirect:/status";
    }

    @GetMapping("/export")
    public void exportStatusReport(
            @RequestParam(required = false) String status,
            HttpServletResponse response) throws IOException {

        List<Employee> employees = employeeService.getEmployeesByStatus(status);

        // Set response headers for CSV download
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");

        String filename = "employee_status_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println(
                    "Employee ID,Full Name,Position,Department,Status,Band Level,Basic Salary,Net Salary,Last Updated,Employment Status,Start Date");

            for (Employee emp : employees) {
                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        "EMP-" + emp.getId(),
                        escapeCsv(emp.getFull_name()),
                        escapeCsv(emp.getPosition()),
                        escapeCsv(emp.getDepartment()),
                        escapeCsv(emp.getStatus()),
                        escapeCsv(emp.getBandLevel()),
                        emp.getSalary() != null ? emp.getSalary() : "",
                        emp.getNetSalary() != null ? emp.getNetSalary() : "",
                        emp.getDate() != null ? emp.getDate() : "",
                        escapeCsv(emp.getEmployment_status()),
                        emp.getStart_date() != null ? emp.getStart_date() : ""));
            }
        }
    }

    private String escapeCsv(String input) {
        if (input == null)
            return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    @PostMapping("/send-notifications")
    public String sendStatusNotifications(
            @RequestParam(value = "employeeIds", required = false) String employeeIds,
            @RequestParam(value = "notificationType", defaultValue = "status_update") String notificationType,
            @RequestParam(value = "recipientType", required = false) String recipientType,
            @RequestParam(value = "message", required = false) String customMessage,
            RedirectAttributes redirectAttributes) {

        try {
            List<Employee> employees;

            if ("selected".equals(recipientType) && employeeIds != null && !employeeIds.isEmpty()) {
                // Send to specific employees
                employees = new ArrayList<>();
                String[] idArray = employeeIds.split(",");
                for (String idStr : idArray) {
                    try {
                        Integer id = Integer.parseInt(idStr.trim());
                        Employee emp = employeeService.getEmployeeById(id);
                        if (emp != null) {
                            employees.add(emp);
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid IDs
                    }
                }
            } else {
                // Send to all employees with the selected status (from context)
                employees = employeeService.getAllEmployees();
            }

            if (employees.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No employees found to notify.");
                return "redirect:/status";
            }

            // Simulate sending notifications
            int sentCount = 0;
            for (Employee emp : employees) {
                if (emp.getEmail() != null && !emp.getEmail().isEmpty()) {
                    System.out.println("Notification sent to: " + emp.getEmail() +
                            " - Type: " + notificationType);
                    sentCount++;
                }
            }

            if (sentCount > 0) {
                redirectAttributes.addFlashAttribute("success",
                        "Notifications sent successfully to " + sentCount + " employee(s).");
            } else {
                redirectAttributes.addFlashAttribute("warning",
                        "No employees with valid email addresses found.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error sending notifications: " + e.getMessage());
        }

        return "redirect:/status";
    }

    @GetMapping
    public String statusManagement(@RequestParam(required = false) String status, Model model) {
        List<Employee> employees = employeeService.getEmployeesByStatus(status);
        model.addAttribute("employees", employees);
        model.addAttribute("statusTypes", STATUS_TYPES);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("employeeCount", employees.size());
        return "pages/status/management";
    }

    @GetMapping("/update/{id}")
    public String updateStatusForm(@PathVariable("id") int id, Model model) {
        Employee emp = employeeService.getEmployeeById(id);
        model.addAttribute("employee", emp);
        model.addAttribute("statusTypes", STATUS_TYPES);
        return "pages/status/update";
    }

    @PostMapping("/update/{id}")
    public String updateStatus(@PathVariable("id") int id,
            @RequestParam("status") String status,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam(value = "effectiveDate", required = false) String effectiveDate,
            @RequestParam(value = "notes", required = false) String notes,
            RedirectAttributes redirectAttributes) {

        Employee emp = employeeService.updateEmployeeStatus(id, status);
        if (emp != null) {
            redirectAttributes.addFlashAttribute("success", "Status updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Employee not found!");
        }
        return "redirect:/status";
    }

    @GetMapping("/salary/{id}")
    public String salaryBreakdown(@PathVariable("id") int id, Model model) {
        Employee emp = employeeService.getEmployeeById(id);
        var breakdown = employeeService.getSalaryBreakdown(id);
        model.addAttribute("employee", emp);
        model.addAttribute("breakdown", breakdown);
        return "pages/status/salary-breakdown";
    }

    @GetMapping("/salary-summary")
    public String salarySummary(Model model) {
        List<Employee> allEmployees = employeeService.getAllEmployees();
        double totalMonthlySalary = employeeService.getTotalSalary();
        double averageSalary = employeeService.getAverageSalary();

        long activeCount = allEmployees.stream()
                .filter(emp -> emp.getStatus() != null && "Active".equalsIgnoreCase(emp.getStatus()))
                .count();

        // Calculate highest, lowest, median salaries
        List<Double> salaries = new ArrayList<>();

        for (Employee emp : allEmployees) {
            String netSalaryStr = emp.getNetSalary();
            if (netSalaryStr != null && !netSalaryStr.isEmpty() && !netSalaryStr.equals("0")) {
                try {
                    Double netSalary = Double.parseDouble(netSalaryStr);
                    salaries.add(netSalary);
                } catch (NumberFormatException e) {
                    // Skip invalid salary values
                    System.err.println("Invalid net salary value for employee " + emp.getId() + ": " + netSalaryStr);
                }
            }
        }

        Collections.sort(salaries);

        double highestSalary = 0.0;
        double lowestSalary = 0.0;
        double medianSalary = 0.0;

        if (!salaries.isEmpty()) {
            highestSalary = salaries.get(salaries.size() - 1);
            lowestSalary = salaries.get(0);

            int size = salaries.size();
            if (size % 2 == 0) {
                int middle1 = size / 2 - 1;
                int middle2 = size / 2;
                medianSalary = (salaries.get(middle1) + salaries.get(middle2)) / 2.0;
            } else {
                medianSalary = salaries.get(size / 2);
            }
        }

        model.addAttribute("totalEmployees", allEmployees.size());
        model.addAttribute("activeEmployees", (int) activeCount);
        model.addAttribute("totalMonthlySalary", totalMonthlySalary);
        model.addAttribute("averageSalary", averageSalary);
        model.addAttribute("highestSalary", highestSalary);
        model.addAttribute("lowestSalary", lowestSalary);
        model.addAttribute("medianSalary", medianSalary);
        model.addAttribute("employees", allEmployees);

        return "pages/status/salary-summary";
    }
}