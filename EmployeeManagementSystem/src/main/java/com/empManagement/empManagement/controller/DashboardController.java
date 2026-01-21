package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        model.addAttribute("username", username);
        model.addAttribute("currentUri", "/dashboard");

        // Get all employees
        List<com.empManagement.empManagement.entity.Employee> allEmployees = employeeService.getAllEmployees();

        // Get total count
        long totalEmployees = allEmployees.size();
        model.addAttribute("totalEmployees", totalEmployees);

        // Get active employees
        long activeEmployees = allEmployees.stream()
                .filter(emp -> emp.getStatus() != null && emp.getStatus().equalsIgnoreCase("Active"))
                .count();
        model.addAttribute("activeEmployees", activeEmployees);

        // Get employees on leave
        long onLeave = allEmployees.stream()
                .filter(emp -> emp.getStatus() != null && emp.getStatus().equalsIgnoreCase("On Leave"))
                .count();
        model.addAttribute("onLeave", onLeave);

        // Calculate new hires (last 30 days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long newHires = allEmployees.stream()
                .filter(emp -> {
                    if (emp.getStart_date() == null)
                        return false;
                    try {
                        LocalDate startDate = LocalDate.parse(emp.getStart_date());
                        return !startDate.isBefore(thirtyDaysAgo);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
        model.addAttribute("newHires", newHires);

        // Department/Team statistics
        Map<String, Integer> departmentStats = new HashMap<>();

        // Count employees by department
        Map<String, Long> deptCounts = allEmployees.stream()
                .filter(emp -> emp.getDepartment() != null && !emp.getDepartment().isEmpty())
                .collect(Collectors.groupingBy(
                        com.empManagement.empManagement.entity.Employee::getDepartment,
                        Collectors.counting()));

        // Map to common department names
        for (Map.Entry<String, Long> entry : deptCounts.entrySet()) {
            String dept = entry.getKey();
            // Standardize department names
            if (dept.contains("IT") || dept.equalsIgnoreCase("Information Technology")) {
                departmentStats.put("IT", departmentStats.getOrDefault("IT", 0) + entry.getValue().intValue());
            } else if (dept.contains("HR") || dept.equalsIgnoreCase("Human Resources")) {
                departmentStats.put("HR", departmentStats.getOrDefault("HR", 0) + entry.getValue().intValue());
            } else if (dept.contains("Technical") || dept.equalsIgnoreCase("Technical Department")) {
                departmentStats.put("Technical",
                        departmentStats.getOrDefault("Technical", 0) + entry.getValue().intValue());
            } else if (dept.contains("Design") || dept.equalsIgnoreCase("Design Department")) {
                departmentStats.put("Design", departmentStats.getOrDefault("Design", 0) + entry.getValue().intValue());
            } else if (dept.contains("Finance") || dept.equalsIgnoreCase("Finance Department")) {
                departmentStats.put("Finance",
                        departmentStats.getOrDefault("Finance", 0) + entry.getValue().intValue());
            } else {
                departmentStats.put(dept, entry.getValue().intValue());
            }
        }

        // Ensure all departments exist even if 0
        departmentStats.putIfAbsent("IT", 0);
        departmentStats.putIfAbsent("HR", 0);
        departmentStats.putIfAbsent("Technical", 0);
        departmentStats.putIfAbsent("Design", 0);
        departmentStats.putIfAbsent("Finance", 0);

        model.addAttribute("departmentStats", departmentStats);

        // Calculate section percentages
        Map<String, Double> sectionPercentages = new HashMap<>();
        for (Map.Entry<String, Integer> entry : departmentStats.entrySet()) {
            double percentage = totalEmployees > 0 ? (entry.getValue().doubleValue() * 100.0 / totalEmployees) : 0;
            sectionPercentages.put(entry.getKey(), Math.round(percentage * 10.0) / 10.0);
        }
        model.addAttribute("sectionPercentages", sectionPercentages);

        // Recent Activities
        Map<String, String> recentActivities = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order

        // Get recent employees (last 5 by ID or start date)
        List<com.empManagement.empManagement.entity.Employee> recentEmployees = allEmployees.stream()
                .filter(emp -> emp.getId() > 0)
                .sorted((e1, e2) -> Integer.compare(e2.getId(), e1.getId())) // Sort by ID descending
                .limit(5)
                .collect(Collectors.toList());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd");
        for (com.empManagement.empManagement.entity.Employee emp : recentEmployees) {
            String activity = "";
            String name = emp.getFull_name() != null ? emp.getFull_name() : "Employee";
            String dept = emp.getDepartment() != null ? emp.getDepartment() : "Department";
            String startDate = emp.getStart_date() != null ? emp.getStart_date() : "";

            if (!startDate.isEmpty()) {
                try {
                    LocalDate date = LocalDate.parse(startDate);
                    activity = "Joined " + dept + " on " + date.format(dateFormatter);
                } catch (Exception e) {
                    activity = "Joined " + dept + " Department";
                }
            } else {
                activity = "Joined " + dept + " Department";
            }

            recentActivities.put(name, activity);
        }

        // If no recent employees, add sample data
        if (recentActivities.isEmpty()) {
            recentActivities.put("John Doe", "Joined IT Department");
            recentActivities.put("Jane Smith", "Promoted to Team Lead");
            recentActivities.put("Bob Wilson", "Completed training");
            recentActivities.put("Alice Brown", "On vacation leave");
            recentActivities.put("Mike Johnson", "Updated profile information");
        }

        model.addAttribute("recentActivities", recentActivities);

        return "pages/dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}