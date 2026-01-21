package com.empManagement.empManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private EmployeeService employeeService;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("employeeCount", employeeService.getEmployeeCount());
        stats.put("attendanceCount", employeeService.getCountOfEmployeesWithAttendance());
        stats.put("leaveCount", employeeService.getCountOfEmployeesWithOutAttendance());
        stats.put("averageSalary", employeeService.getAverageSalary());
        stats.put("totalSalary", employeeService.getTotalSalary());
        stats.put("employeesWithNoAttendance", employeeService.getEmployeesWithNoAttendance());
        stats.put("departmentStats", getDepartmentStats());
        stats.put("sectionPercentages", employeeService.getSectionPercentages());

        return stats;
    }

    private Map<String, Long> getDepartmentStats() {
        Map<String, Long> departmentStats = new HashMap<>();

        departmentStats.put("IT", employeeService.getCountOfEmployeesIT());
        departmentStats.put("HRM", employeeService.getCountOfEmployeesHRM());
        departmentStats.put("Technical", employeeService.getCountOfEmployeesTechnical());
        departmentStats.put("Design", employeeService.getCountOfEmployeesDesign());
        departmentStats.put("Finance", employeeService.getCountOfEmployeesFinance());
        return departmentStats;
    }
}