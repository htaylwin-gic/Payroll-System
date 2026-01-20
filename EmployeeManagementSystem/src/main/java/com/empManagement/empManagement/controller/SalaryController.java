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
import java.util.List;

@Controller
@RequestMapping("/salary")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class SalaryController {

    @Autowired
    private EmployeeService employeeService;

    private static final List<String> BAND_LEVELS = Arrays.asList("Band1", "Band2", "Band3", "Band4", "Band5", "Band6",
            "Band7", "Band8");
    private static final List<String> EDUCATION_LEVELS = Arrays.asList("Diploma", "Master", "FE Passer");
    private static final List<String> EVALUATION_GRADES = Arrays.asList("A", "B", "C");
    private static final List<String> JAPANESE_LEVELS = Arrays.asList("N1", "N2", "N3", "JLPIT");
    private static final List<String> ASSIGNMENT_LEVELS = Arrays.asList("Sub TL", "TL", "HL", "Senior TL");
    private static final List<String> MANAGEMENT_LEVELS = Arrays.asList("Sub Leader", "Leader", "Sub Manager",
            "Manager");

    @GetMapping("/update/{id}")
    public String updateSalaryForm(@PathVariable("id") int id, Model model) {
        Employee emp = employeeService.getEmployeeById(id);
        model.addAttribute("employee", emp);
        model.addAttribute("bandLevels", BAND_LEVELS);
        model.addAttribute("educationLevels", EDUCATION_LEVELS);
        model.addAttribute("evaluationGrades", EVALUATION_GRADES);
        model.addAttribute("japaneseLevels", JAPANESE_LEVELS);
        model.addAttribute("assignmentLevels", ASSIGNMENT_LEVELS);
        model.addAttribute("managementLevels", MANAGEMENT_LEVELS);
        return "pages/salary/update";
    }

    @PostMapping("/update/{id}")
    public String updateSalary(@PathVariable("id") int id,
            @ModelAttribute Employee updatedEmployee,
            RedirectAttributes redirectAttributes) {
        Employee emp = employeeService.getEmployeeById(id);

        // Update salary-related fields
        if (updatedEmployee.getBandLevel() != null) {
            emp.setBandLevel(updatedEmployee.getBandLevel());
        }
        if (updatedEmployee.getEducationLevel() != null) {
            emp.setEducationLevel(updatedEmployee.getEducationLevel());
        }
        if (updatedEmployee.getEvaluationGrade() != null) {
            emp.setEvaluationGrade(updatedEmployee.getEvaluationGrade());
        }
        if (updatedEmployee.getJapaneseLevel() != null) {
            emp.setJapaneseLevel(updatedEmployee.getJapaneseLevel());
        }
        if (updatedEmployee.getEnglishToeicScore() != null) {
            emp.setEnglishToeicScore(updatedEmployee.getEnglishToeicScore());
        }
        if (updatedEmployee.getEnglishToeflScore() != null) {
            emp.setEnglishToeflScore(updatedEmployee.getEnglishToeflScore());
        }
        if (updatedEmployee.getEnglishIeltsScore() != null) {
            emp.setEnglishIeltsScore(updatedEmployee.getEnglishIeltsScore());
        }
        if (updatedEmployee.getAssignmentLevel() != null) {
            emp.setAssignmentLevel(updatedEmployee.getAssignmentLevel());
        }
        if (updatedEmployee.getManagementLevel() != null) {
            emp.setManagementLevel(updatedEmployee.getManagementLevel());
        }
        if (updatedEmployee.getHouseAllowance() != null) {
            emp.setHouseAllowance(updatedEmployee.getHouseAllowance());
        }

        // Update the salary
        employeeService.updateEmployeeWithSalary(emp);

        redirectAttributes.addFlashAttribute("success", "Salary details updated successfully!");
        return "redirect:/status/salary/" + id;
    }
}