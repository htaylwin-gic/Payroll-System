package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

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

        model.addAttribute("totalEmployees", employeeService.getEmployeeCount());
        model.addAttribute("activeEmployees", employeeService.getEmployeesByStatus("Active").size());
        model.addAttribute("onLeave", employeeService.getEmployeesByStatus("On Leave").size());
        model.addAttribute("newHires", 8);

        // Department/Team statistics
        Map<String, Integer> departmentStats = new HashMap<>();
        departmentStats.put("IT", (int) employeeService.getCountOfEmployeesIT());
        departmentStats.put("HR", (int) employeeService.getCountOfEmployeesHRM());
        departmentStats.put("Technical", (int) employeeService.getCountOfEmployeesTechnical());
        departmentStats.put("Design", (int) employeeService.getCountOfEmployeesDesign());
        departmentStats.put("Finance", 23);
        model.addAttribute("departmentStats", departmentStats);

        // Section Percentages (for charts)
        Map<String, Double> sectionPercentages = employeeService.getSectionPercentages();
        model.addAttribute("sectionPercentages", sectionPercentages);

        // Recent Activities
        Map<String, String> recentActivities = new HashMap<>();
        recentActivities.put("John Doe", "Joined IT Department");
        recentActivities.put("Jane Smith", "Promoted to Team Lead");
        recentActivities.put("Bob Wilson", "Completed training");
        recentActivities.put("Alice Brown", "On vacation leave");
        model.addAttribute("recentActivities", recentActivities);

        return "pages/dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}