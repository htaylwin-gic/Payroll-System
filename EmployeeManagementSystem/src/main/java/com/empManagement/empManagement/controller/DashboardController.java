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

        List<com.empManagement.empManagement.entity.Employee> allEmployees = employeeService.getAllEmployees();
        long totalEmployees = allEmployees.size();
        model.addAttribute("totalEmployees", totalEmployees);

        long activeEmployees = allEmployees.stream()
                .filter(emp -> emp.getStatus() != null && emp.getStatus().equalsIgnoreCase("Active"))
                .count();
        model.addAttribute("activeEmployees", activeEmployees);

        long onLeave = allEmployees.stream()
                .filter(emp -> emp.getStatus() != null && emp.getStatus().equalsIgnoreCase("On Leave"))
                .count();
        model.addAttribute("onLeave", onLeave);

        // New Hires Logic (Last 30 days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long newHires = allEmployees.stream()
                .filter(emp -> {
                    if (emp.getStart_date() == null || emp.getStart_date().isEmpty())
                        return false;
                    try {
                        LocalDate startDate = LocalDate.parse(emp.getStart_date());
                        return !startDate.isBefore(thirtyDaysAgo);
                    } catch (Exception e) {
                        return false;
                    }
                }).count();
        model.addAttribute("newHires", newHires);

        // TOP CARDS PERCENTAGES (Fixed the 0% issue)
        double activePercent = totalEmployees > 0 ? (activeEmployees * 100.0 / totalEmployees) : 0;
        double leavePercent = totalEmployees > 0 ? (onLeave * 100.0 / totalEmployees) : 0;
        double newHiresPercent = totalEmployees > 0 ? (newHires * 100.0 / totalEmployees) : 0;

        model.addAttribute("activePercent", Math.round(activePercent));
        model.addAttribute("leavePercent", Math.round(leavePercent));
        model.addAttribute("newHiresPercent", Math.round(newHiresPercent));

        Map<String, Long> departmentStats = allEmployees.stream()
                .filter(emp -> emp.getDepartment() != null && !emp.getDepartment().isEmpty())
                .collect(Collectors.groupingBy(
                        com.empManagement.empManagement.entity.Employee::getDepartment,
                        Collectors.counting()));

        model.addAttribute("departmentStats", departmentStats);

        Map<String, Double> sectionPercentages = new HashMap<>();
        departmentStats.forEach((dept, count) -> {
            double percentage = totalEmployees > 0 ? (count * 100.0 / totalEmployees) : 0;
            sectionPercentages.put(dept, Math.round(percentage * 10.0) / 10.0);
        });
        model.addAttribute("sectionPercentages", sectionPercentages);

        Map<String, String> recentActivities = new LinkedHashMap<>();
        List<com.empManagement.empManagement.entity.Employee> recentEmployees = allEmployees.stream()
                .sorted((e1, e2) -> Integer.compare(e2.getId(), e1.getId()))
                .limit(5)
                .collect(Collectors.toList());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd");
        for (com.empManagement.empManagement.entity.Employee emp : recentEmployees) {
            String name = emp.getFull_name() != null ? emp.getFull_name() : "Employee";
            String dept = emp.getDepartment() != null ? emp.getDepartment() : "Dept";
            recentActivities.put(name, "Joined " + dept + " Department");
        }
        model.addAttribute("recentActivities", recentActivities);

        return "pages/dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}