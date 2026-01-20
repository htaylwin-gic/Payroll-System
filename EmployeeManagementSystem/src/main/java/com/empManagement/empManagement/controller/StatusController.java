package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.entity.Employee;
import com.empManagement.empManagement.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
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

        // Calculate active count
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
            highestSalary = salaries.get(salaries.size() - 1); // Last element is highest after sorting
            lowestSalary = salaries.get(0); // First element is lowest after sorting

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