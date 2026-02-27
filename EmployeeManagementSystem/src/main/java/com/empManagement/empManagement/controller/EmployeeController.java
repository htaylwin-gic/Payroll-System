package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.entity.Employee;
import com.empManagement.empManagement.service.EmployeeService;
import com.empManagement.empManagement.service.ExcelUploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/employees")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ExcelUploadService excelUploadService;

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
    public String employeeManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Employee> employeePage = employeeService.getAllEmployees(page, size);

        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());
        model.addAttribute("totalElements", employeePage.getTotalElements());
        model.addAttribute("pageSize", size);

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
        try {
            // Check if employee has payroll records
            boolean hasPayrolls = employeeService.hasPayrollRecords(id);

            if (hasPayrolls) {
                redirectAttributes.addFlashAttribute("error",
                        "Cannot delete employee with existing payroll records. Please delete payroll records first.");
                return "redirect:/employees/manage";
            }

            employeeService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Employee deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting employee: " + e.getMessage());
        }
        return "redirect:/employees/manage";
    }

    @GetMapping("/search")
    public String searchEmployees(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Employee> employeePage = employeeService.searchEmployees(keyword, page, size);

        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());
        model.addAttribute("totalElements", employeePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("searchKeyword", keyword);

        return "pages/employee/management";
    }

    @GetMapping("/details/{id}")
    public String employeeDetails(@PathVariable("id") int id, Model model) {
        Employee emp = employeeService.getEmployeeById(id);
        model.addAttribute("employee", emp);
        return "pages/employee/details";
    }

    @GetMapping("/upload-excel")
    public String showUploadExcelForm() {
        return "pages/employee/upload-excel";
    }

    @PostMapping("/upload-excel")
    public String uploadExcelFile(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/employees/upload-excel";
        }

        // Check file type
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            redirectAttributes.addFlashAttribute("error", "Please upload an Excel file (.xlsx or .xls)");
            return "redirect:/employees/upload-excel";
        }

        try {
            // Parse Excel file
            List<Employee> employees = excelUploadService.parseExcelFile(file);

            if (employees.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning", "No valid employee data found in the file");
                return "redirect:/employees/upload-excel";
            }

            // Save all employees
            int successCount = 0;
            List<String> errors = new ArrayList<>();

            for (Employee emp : employees) {
                try {
                    // Check if employee ID already exists
                    Employee existingEmp = employeeService.findByEmployeeId(emp.getEmployeeId());
                    if (existingEmp != null) {
                        errors.add("Employee ID " + emp.getEmployeeId() + " already exists - skipped");
                        continue;
                    }

                    employeeService.save(emp);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Error saving employee " + emp.getEmployeeId() + ": " + e.getMessage());
                }
            }

            if (successCount > 0) {
                redirectAttributes.addFlashAttribute("success",
                        successCount + " employees imported successfully!");
            }

            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("warnings", errors);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error processing Excel file: " + e.getMessage());
            return "redirect:/employees/upload-excel";
        }

        return "redirect:/employees/manage";
    }

    @PostMapping("/saveEmployee")
    public String saveEmployee(@ModelAttribute("employee") Employee employee) {
        if (employee.getStatus() == null || employee.getStatus().isEmpty()) {
            employee.setStatus("Active");
        }

        employeeService.save(employee);
        return "redirect:/dashboard";
    }
}