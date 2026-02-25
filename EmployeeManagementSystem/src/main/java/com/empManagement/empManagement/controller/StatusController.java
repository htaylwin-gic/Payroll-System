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

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/status")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class StatusController {

    @Autowired
    private EmployeeService employeeService;

    private static final List<String> STATUS_TYPES = Arrays.asList("Active", "Inactive");

    @GetMapping
    public String statusManagement(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        List<Employee> allEmployees = employeeService.getAllEmployees();

        for (Employee emp : allEmployees) {
            if (emp.getStatus() == null || emp.getStatus().trim().isEmpty()) {
                emp.setStatus("Active");
            }
        }

        // Filter employees based on selected status
        List<Employee> filteredEmployees;
        String selectedStatus = status;

        if (status != null && !status.isEmpty()) {
            if ("all".equals(status)) {
                filteredEmployees = allEmployees;
                selectedStatus = "all";
            } else {
                filteredEmployees = allEmployees.stream()
                        .filter(emp -> status.equals(emp.getStatus()))
                        .collect(Collectors.toList());
                selectedStatus = status;
            }
        } else {
            filteredEmployees = allEmployees;
            selectedStatus = "all";
        }

        long activeCount = allEmployees.stream()
                .filter(emp -> "Active".equals(emp.getStatus()))
                .count();
        long inactiveCount = allEmployees.stream()
                .filter(emp -> "Inactive".equals(emp.getStatus()))
                .count();

        int totalFiltered = filteredEmployees.size();
        int start = Math.min(page * size, totalFiltered);
        int end = Math.min(start + size, totalFiltered);
        List<Employee> paginatedEmployees = filteredEmployees.subList(start, end);

        int totalPages = (int) Math.ceil((double) totalFiltered / size);
        int currentPage = page;

        model.addAttribute("employees", paginatedEmployees);
        model.addAttribute("allEmployees", allEmployees);
        model.addAttribute("statusTypes", STATUS_TYPES);
        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("totalCount", allEmployees.size());
        model.addAttribute("filteredCount", totalFiltered);

        // Pagination attributes
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("hasNext", currentPage < totalPages - 1);
        model.addAttribute("hasPrev", currentPage > 0);

        return "pages/status/management";
    }

    @GetMapping("/update/{id}")
    public String updateStatusForm(@PathVariable("id") int id, Model model) {
        Employee emp = employeeService.getEmployeeById(id);
        if (emp.getStatus() == null || emp.getStatus().trim().isEmpty()) {
            emp.setStatus("Active");
        }
        model.addAttribute("employee", emp);
        model.addAttribute("statusTypes", STATUS_TYPES);
        return "pages/status/update";
    }

    @PostMapping("/update/{id}")
    public String updateStatus(@PathVariable("id") int id,
            @RequestParam("status") String status,
            @RequestParam(value = "reason", required = false) String reason,
            RedirectAttributes redirectAttributes) {

        Employee emp = employeeService.updateEmployeeStatus(id, status);
        if (emp != null) {
            String message = String.format("Employee %s status updated to %s successfully!",
                    emp.getFull_name(), status);
            redirectAttributes.addFlashAttribute("success", message);
        } else {
            redirectAttributes.addFlashAttribute("error", "Employee not found!");
        }
        return "redirect:/status";
    }

    @PostMapping("/bulk-update")
    public String bulkUpdateStatus(
            @RequestParam("employeeIds") String employeeIds,
            @RequestParam("newStatus") String newStatus,
            @RequestParam(value = "reason", required = false) String reason,
            RedirectAttributes redirectAttributes) {

        try {
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
            List<String> employeeNames = new ArrayList<>();

            for (Integer id : ids) {
                Employee emp = employeeService.updateEmployeeStatus(id, newStatus);
                if (emp != null) {
                    updatedCount++;
                    employeeNames.add(emp.getFull_name());
                }
            }

            String message = String.format("Successfully updated %d employee(s) to %s status: %s",
                    updatedCount, newStatus, String.join(", ", employeeNames));
            redirectAttributes.addFlashAttribute("success", message);

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

        List<Employee> allEmployees = employeeService.getAllEmployees();

        for (Employee emp : allEmployees) {
            if (emp.getStatus() == null || emp.getStatus().trim().isEmpty()) {
                emp.setStatus("Active");
            }
        }

        List<Employee> employeesToExport;
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            employeesToExport = allEmployees.stream()
                    .filter(emp -> status.equals(emp.getStatus()))
                    .collect(Collectors.toList());
        } else {
            employeesToExport = allEmployees;
        }

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");

        String filename = "employee_status_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println(
                    "Employee ID,Full Name,Position,Department,Status,Basic Salary,Net Salary,Last Updated,Employment Status,Start Date");

            for (Employee emp : employeesToExport) {
                String statusValue = emp.getStatus() != null && !emp.getStatus().isEmpty() ? emp.getStatus() : "Active";

                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        "EMP-" + emp.getId(),
                        escapeCsv(emp.getFull_name()),
                        escapeCsv(emp.getPosition() != null ? emp.getPosition() : ""),
                        escapeCsv(emp.getDepartment() != null ? emp.getDepartment() : ""),
                        escapeCsv(statusValue),
                        emp.getSalary() != null ? emp.getSalary() : "0",
                        emp.getNetSalary() != null && !emp.getNetSalary().isEmpty() ? emp.getNetSalary() : "0",
                        emp.getDate() != null ? emp.getDate() : "",
                        escapeCsv(emp.getEmployment_status() != null ? emp.getEmployment_status() : ""),
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

    @GetMapping("/salary-summary")
    public String salarySummary(Model model) {
        List<Employee> allEmployees = employeeService.getAllEmployees();

        for (Employee emp : allEmployees) {
            if (emp.getStatus() == null || emp.getStatus().trim().isEmpty()) {
                emp.setStatus("Active");
            }
        }

        double totalMonthlySalary = employeeService.getTotalSalary();
        double averageSalary = employeeService.getAverageSalary();

        long activeCount = allEmployees.stream()
                .filter(emp -> "Active".equals(emp.getStatus()))
                .count();
        long inactiveCount = allEmployees.stream()
                .filter(emp -> "Inactive".equals(emp.getStatus()))
                .count();

        List<Double> salaries = new ArrayList<>();
        for (Employee emp : allEmployees) {
            if (emp.getSalary() != null && emp.getSalary() > 0) {
                salaries.add(emp.getSalary());
            }
        }

        double highestSalary = salaries.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double lowestSalary = salaries.stream().mapToDouble(Double::doubleValue).min().orElse(0);

        double medianSalary = 0;
        if (!salaries.isEmpty()) {
            List<Double> sortedSalaries = salaries.stream().sorted().collect(Collectors.toList());
            int size = sortedSalaries.size();
            if (size % 2 == 0) {
                medianSalary = (sortedSalaries.get(size / 2 - 1) + sortedSalaries.get(size / 2)) / 2.0;
            } else {
                medianSalary = sortedSalaries.get(size / 2);
            }
        }

        List<Employee> activeEmployees = allEmployees.stream()
                .filter(emp -> "Active".equals(emp.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("totalEmployees", allEmployees.size());
        model.addAttribute("activeEmployees", activeCount);
        model.addAttribute("inactiveEmployees", inactiveCount);
        model.addAttribute("totalMonthlySalary", totalMonthlySalary);
        model.addAttribute("averageSalary", averageSalary);
        model.addAttribute("highestSalary", highestSalary);
        model.addAttribute("lowestSalary", lowestSalary);
        model.addAttribute("medianSalary", medianSalary);
        model.addAttribute("activeEmployeesList", activeEmployees);
        model.addAttribute("allEmployees", allEmployees);

        return "pages/status/salary-summary";
    }

    @GetMapping("/salary/{id}")
    public String salaryBreakdown(@PathVariable("id") int id, Model model) {
        Employee emp = employeeService.getEmployeeById(id);

        if (emp == null) {
            return "redirect:/status?error=Employee not found";
        }

        // Calculate salary breakdown
        double basicSalary = emp.getSalary() != null ? emp.getSalary() : 0;
        double tax = basicSalary * 0.1;
        double insurance = basicSalary * 0.05;
        double otherDeductions = basicSalary * 0.02;
        double totalDeductions = tax + insurance + otherDeductions;
        double netSalary = basicSalary - totalDeductions;

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("basicSalary", basicSalary);
        breakdown.put("tax", tax);
        breakdown.put("insurance", insurance);
        breakdown.put("otherDeductions", otherDeductions);
        breakdown.put("totalDeductions", totalDeductions);
        breakdown.put("netSalary", netSalary);

        model.addAttribute("employee", emp);
        model.addAttribute("breakdown", breakdown);

        return "pages/status/salary-breakdown";
    }
}