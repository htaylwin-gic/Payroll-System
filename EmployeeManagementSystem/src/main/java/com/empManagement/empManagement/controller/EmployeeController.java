package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.entity.Employee;
import com.empManagement.empManagement.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/employees")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // Band to salary mapping
    private static final Map<String, Double> BAND_SALARIES = new HashMap<>();

    static {
        BAND_SALARIES.put("Band1", 350000.0);
        BAND_SALARIES.put("Band1+", 440000.0);
        BAND_SALARIES.put("Band2", 550000.0);
        BAND_SALARIES.put("Band3", 650000.0);
        BAND_SALARIES.put("Band4", 750000.0);
        BAND_SALARIES.put("Band5", 850000.0);
        BAND_SALARIES.put("Band6", 1150000.0);
        BAND_SALARIES.put("Band7", 1350000.0);
        BAND_SALARIES.put("Band8", 1550000.0);
    }

    @GetMapping("/manage")
    public String employeeManagement(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        return "pages/employee/management";
    }

    @GetMapping("/add")
    public String addEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "pages/employee/add";
    }

    @PostMapping("/add")
    public String addEmployee(@ModelAttribute Employee employee,
            RedirectAttributes redirectAttributes) {
        employeeService.save(employee);
        redirectAttributes.addFlashAttribute("success", "Employee added successfully!");
        return "redirect:/employees/manage";
    }

    @GetMapping("/edit/{id}")
    public String editEmployeeForm(@PathVariable("id") int id, Model model) {
        Employee emp = employeeService.getEmployeeById(id);
        model.addAttribute("employee", emp);
        return "pages/employee/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateEmployee(@PathVariable("id") int id,
            @ModelAttribute Employee employee,
            RedirectAttributes redirectAttributes) {
        employee.setId(id);
        employeeService.save(employee);
        redirectAttributes.addFlashAttribute("success", "Employee updated successfully!");
        return "redirect:/employees/manage";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteEmployee(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        employeeService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Employee deleted successfully!");
        return "redirect:/employees/manage";
    }

    @GetMapping("/search")
    public String searchEmployees(@RequestParam("keyword") String keyword, Model model) {
        List<Employee> results = employeeService.getByKeyword(keyword);
        model.addAttribute("employees", results);
        model.addAttribute("searchKeyword", keyword);
        return "pages/employee/management";
    }

    @GetMapping("/details/{id}")
    public String employeeDetails(@PathVariable("id") int id, Model model) {
        Employee emp = employeeService.getEmployeeById(id);
        model.addAttribute("employee", emp);
        return "pages/employee/details";
    }
}