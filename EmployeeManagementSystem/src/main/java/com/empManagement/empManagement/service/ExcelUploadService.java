package com.empManagement.empManagement.service;

import com.empManagement.empManagement.entity.Employee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelUploadService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Salary mapping based on band levels
    private static final java.util.Map<String, Double> BAND_SALARIES = new java.util.HashMap<>();

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

    public List<Employee> parseExcelFile(MultipartFile file) throws IOException {
        List<Employee> employees = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Employee employee = createEmployeeFromRow(row);
                if (employee != null && isValidEmployee(employee)) {
                    employees.add(employee);
                }
            }
        }

        return employees;
    }

    private Employee createEmployeeFromRow(Row row) {
        Employee emp = new Employee();

        try {
            // Personal Information
            emp.setEmployeeId(getCellValueAsString(row.getCell(0))); // Employee ID
            emp.setFullName(getCellValueAsString(row.getCell(1))); // Full Name (using setFullName instead of
                                                                   // setFull_name)
            emp.setDateOfBirth(getCellValueAsString(row.getCell(2))); // Date of Birth (stored as String)
            emp.setGender(getCellValueAsString(row.getCell(3))); // Gender
            emp.setNationality(getCellValueAsString(row.getCell(4))); // Nationality
            emp.setNIC(getCellValueAsString(row.getCell(5))); // NIC

            // Contact Information
            emp.setEmail(getCellValueAsString(row.getCell(6))); // Email
            emp.setPhoneNumber(getCellValueAsString(row.getCell(7))); // Phone Number
            emp.setAddress(getCellValueAsString(row.getCell(8))); // Address

            // Assignment & Management
            emp.setAssignmentLevel(getCellValueAsString(row.getCell(9))); // Assignment Level
            emp.setManagementLevel(getCellValueAsString(row.getCell(10))); // Management Level

            // Transportation
            String busRouteStr = getCellValueAsString(row.getCell(11)); // Bus Route
            if (busRouteStr != null && !busRouteStr.isEmpty()) {
                try {
                    Integer busRoute = Integer.parseInt(busRouteStr);
                    emp.setBusRoute(busRoute); // busRoute is Integer
                    emp.setTransportationFee(busRoute * 800.0); // transportationFee is Double
                } catch (NumberFormatException e) {
                    emp.setBusRoute(0);
                    emp.setTransportationFee(0.0);
                }
            }

            // Language Skills
            emp.setEducationLevel(getCellValueAsString(row.getCell(12))); // Education Level
            emp.setEvaluationGrade(getCellValueAsString(row.getCell(13))); // Evaluation Grade
            emp.setJapaneseLevel(getCellValueAsString(row.getCell(14))); // Japanese Level
            emp.setJapaneseNatTest(getCellValueAsString(row.getCell(15))); // Japanese NAT Test
            emp.setEnglishLevel(getCellValueAsString(row.getCell(16))); // English Level

            // Employment Information
            emp.setDepartment(getCellValueAsString(row.getCell(17))); // Department
            emp.setPosition(getCellValueAsString(row.getCell(18))); // Position
            emp.setBandLevel(getCellValueAsString(row.getCell(19))); // Band Level
            emp.setStartDate(getCellValueAsString(row.getCell(20))); // Start Date (stored as String)

            // Auto-calculate salary based on band level
            String bandLevel = emp.getBandLevel();
            if (bandLevel != null && BAND_SALARIES.containsKey(bandLevel)) {
                emp.setSalary(BAND_SALARIES.get(bandLevel));
            }

            emp.setEmploymentStatus(getCellValueAsString(row.getCell(21))); // Employment Status

            // Set default status if not provided
            if (emp.getEmploymentStatus() == null || emp.getEmploymentStatus().isEmpty()) {
                emp.setEmploymentStatus("Active");
            }

        } catch (Exception e) {
            System.err.println("Error parsing row: " + e.getMessage());
            return null;
        }

        return emp;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    }
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getStringCellValue());
                } catch (IllegalStateException e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (IllegalStateException ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    private LocalDate getCellValueAsLocalDate(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else {
                String dateStr = getCellValueAsString(cell);
                if (dateStr != null && !dateStr.isEmpty()) {
                    return LocalDate.parse(dateStr, DATE_FORMATTER);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing date: " + e.getMessage());
        }

        return null;
    }

    private boolean isValidEmployee(Employee emp) {
        return emp.getEmployeeId() != null && !emp.getEmployeeId().isEmpty() &&
                emp.getFullName() != null && !emp.getFullName().isEmpty() &&
                emp.getEmail() != null && !emp.getEmail().isEmpty() &&
                emp.getPhoneNumber() != null && !emp.getPhoneNumber().isEmpty() &&
                emp.getDepartment() != null && !emp.getDepartment().isEmpty();
    }
}