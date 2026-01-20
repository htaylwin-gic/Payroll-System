package com.empManagement.empManagement.service;

import com.empManagement.empManagement.entity.Employee;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class SalaryCalculationService {

    // Band level basic salaries
    private static final double BAND1_SALARY = 350000;
    private static final double BAND2_SALARY = 550000;
    private static final double BAND3_SALARY = 650000;
    private static final double BAND4_SALARY = 750000;
    private static final double BAND5_SALARY = 850000;
    private static final double BAND6_SALARY = 1150000;
    private static final double BAND7_SALARY = 1350000;
    private static final double BAND8_SALARY = 1550000;

    // Education allowances
    private static final double DIPLOMA_ALLOWANCE = 30000;
    private static final double MASTER_ALLOWANCE = 50000;
    private static final double FE_PASSER_ALLOWANCE = 30000;

    // Evaluation allowances
    private static final double GRADE_A_ALLOWANCE = 100000;
    private static final double GRADE_B_ALLOWANCE = 50000;
    private static final double GRADE_C_ALLOWANCE = 10000;

    // Japanese allowances
    private static final double JAPANESE_N1 = 300000;
    private static final double JAPANESE_N2 = 150000;
    private static final double JAPANESE_N3 = 40000;
    private static final double JAPANESE_JLPIT = 20000;

    // Assignment allowances
    private static final double SUB_TL_ALLOWANCE = 100000;
    private static final double TL_ALLOWANCE = 300000;
    private static final double HL_ALLOWANCE = 200000;
    private static final double SENIOR_TL_ALLOWANCE = 300000;

    // Management allowances
    private static final double SUB_LEADER_ALLOWANCE = 500000;
    private static final double LEADER_ALLOWANCE = 600000;
    private static final double SUB_MANAGER_ALLOWANCE = 700000;
    private static final double MANAGER_ALLOWANCE = 1100000;

    // Default allowances
    private static final double DEFAULT_HOUSE_ALLOWANCE = 100000;
    private static final double DEFAULT_TRANSPORT_ALLOWANCE = 50000;
    private static final double DEFAULT_ATTENDANCE_ALLOWANCE = 10000;

    // Calculate basic salary based on band level
    public double calculateBasicSalary(String bandLevel) {
        if (bandLevel == null)
            return 0;

        return switch (bandLevel) {
            case "Band1" -> BAND1_SALARY;
            case "Band2" -> BAND2_SALARY;
            case "Band3" -> BAND3_SALARY;
            case "Band4" -> BAND4_SALARY;
            case "Band5" -> BAND5_SALARY;
            case "Band6" -> BAND6_SALARY;
            case "Band7" -> BAND7_SALARY;
            case "Band8" -> BAND8_SALARY;
            default -> 0;
        };
    }

    // Calculate education allowance
    public double calculateEducationAllowance(String educationLevel) {
        if (educationLevel == null)
            return 0;

        return switch (educationLevel) {
            case "Diploma" -> DIPLOMA_ALLOWANCE;
            case "Master" -> MASTER_ALLOWANCE;
            case "FE Passer" -> FE_PASSER_ALLOWANCE;
            default -> 0;
        };
    }

    // Calculate evaluation allowance
    public double calculateEvaluationAllowance(String evaluationGrade) {
        if (evaluationGrade == null)
            return 0;

        return switch (evaluationGrade) {
            case "A" -> GRADE_A_ALLOWANCE;
            case "B" -> GRADE_B_ALLOWANCE;
            case "C" -> GRADE_C_ALLOWANCE;
            default -> 0;
        };
    }

    // Calculate Japanese allowance
    public double calculateJapaneseAllowance(String japaneseLevel) {
        if (japaneseLevel == null)
            return 0;

        return switch (japaneseLevel) {
            case "N1" -> JAPANESE_N1;
            case "N2" -> JAPANESE_N2;
            case "N3" -> JAPANESE_N3;
            case "JLPIT" -> JAPANESE_JLPIT;
            default -> 0;
        };
    }

    // Calculate English allowance
    public double calculateEnglishAllowance(Integer toeicScore, Integer toeflScore, Double ieltsScore) {
        if (toeicScore != null && toeicScore >= 730)
            return 100000;
        if (toeflScore != null && toeflScore >= 650)
            return 100000;
        if (ieltsScore != null && ieltsScore >= 6.5)
            return 100000;

        if (toeicScore != null && toeicScore >= 600)
            return 50000;
        if (toeflScore != null && toeflScore >= 505)
            return 50000;
        if (ieltsScore != null && ieltsScore >= 5.5)
            return 50000;

        return 0;
    }

    // Calculate service years from start date
    public int calculateServiceYears(String startDate) {
        if (startDate == null || startDate.isEmpty())
            return 0;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate now = LocalDate.now();
            return Period.between(start, now).getYears();
        } catch (DateTimeParseException e) {
            // Try alternative format
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate start = LocalDate.parse(startDate, formatter);
                LocalDate now = LocalDate.now();
                return Period.between(start, now).getYears();
            } catch (DateTimeParseException ex) {
                return 0;
            }
        }
    }

    // Calculate service allowance
    public double calculateServiceAllowance(int myanmarYears, Integer gicpYears) {
        double allowance = 0;

        // Myanmar service years
        if (myanmarYears >= 6) {
            allowance += 90000;
        } else if (myanmarYears >= 1) {
            allowance += 15000 * myanmarYears;
        }

        // GICP service years
        if (gicpYears != null) {
            if (gicpYears >= 6) {
                allowance += 300000;
            } else if (gicpYears >= 1) {
                allowance += 50000 * gicpYears;
            }
        }

        return allowance;
    }

    // Calculate assignment allowance
    public double calculateAssignmentAllowance(String assignmentLevel) {
        if (assignmentLevel == null)
            return 0;

        return switch (assignmentLevel) {
            case "Sub TL" -> SUB_TL_ALLOWANCE;
            case "TL" -> TL_ALLOWANCE;
            case "HL" -> HL_ALLOWANCE;
            case "Senior TL" -> SENIOR_TL_ALLOWANCE;
            default -> 0;
        };
    }

    // Calculate management allowance
    public double calculateManagementAllowance(String managementLevel) {
        if (managementLevel == null)
            return 0;

        return switch (managementLevel) {
            case "Sub Leader" -> SUB_LEADER_ALLOWANCE;
            case "Leader" -> LEADER_ALLOWANCE;
            case "Sub Manager" -> SUB_MANAGER_ALLOWANCE;
            case "Manager" -> MANAGER_ALLOWANCE;
            default -> 0;
        };
    }

    // Calculate and update employee salary
    public void calculateAndUpdateEmployeeSalary(Employee employee) {
        double basicSalary = calculateBasicSalary(employee.getBandLevel());
        employee.setSalary(basicSalary);

        // Calculate all allowances
        double educationAllowance = calculateEducationAllowance(employee.getEducationLevel());
        double evaluationAllowance = calculateEvaluationAllowance(employee.getEvaluationGrade());
        double japaneseAllowance = calculateJapaneseAllowance(employee.getJapaneseLevel());

        Integer toeic = parseInteger(employee.getEnglishToeicScore());
        Integer toefl = parseInteger(employee.getEnglishToeflScore());
        Double ielts = parseDouble(employee.getEnglishIeltsScore());
        double englishAllowance = calculateEnglishAllowance(toeic, toefl, ielts);

        int serviceYears = calculateServiceYears(employee.getStartDate());
        employee.setMyanmarServiceYears(serviceYears);
        double serviceAllowance = calculateServiceAllowance(serviceYears, employee.getGicpServiceYears());

        double assignmentAllowance = calculateAssignmentAllowance(employee.getAssignmentLevel());
        double managementAllowance = calculateManagementAllowance(employee.getManagementLevel());

        double houseAllowance = parseDouble(employee.getHouseAllowance(), DEFAULT_HOUSE_ALLOWANCE);
        double transportAllowance = parseDouble(employee.getTransportationAllowance(), DEFAULT_TRANSPORT_ALLOWANCE);
        double attendanceAllowance = parseDouble(employee.getPerfectAttendanceAllowance(),
                DEFAULT_ATTENDANCE_ALLOWANCE);

        // Set allowance strings
        employee.setEducationAllowance(String.valueOf(educationAllowance));
        employee.setEvaluationAllowance(String.valueOf(evaluationAllowance));
        employee.setJapaneseAllowance(String.valueOf(japaneseAllowance));
        employee.setEnglishAllowance(String.valueOf(englishAllowance));
        employee.setServiceAllowance(String.valueOf(serviceAllowance));
        employee.setAssignmentAllowance(String.valueOf(assignmentAllowance));
        employee.setManagementAllowance(String.valueOf(managementAllowance));
        employee.setHouseAllowance(String.valueOf(houseAllowance));
        employee.setTransportationAllowance(String.valueOf(transportAllowance));
        employee.setPerfectAttendanceAllowance(String.valueOf(attendanceAllowance));

        // Calculate totals
        double totalAllowances = educationAllowance + evaluationAllowance + japaneseAllowance +
                englishAllowance + serviceAllowance + assignmentAllowance +
                managementAllowance + houseAllowance + transportAllowance + attendanceAllowance;

        double sscDeduction = parseDouble(employee.getSscDeduction(), 0);
        double companyTripDeduction = parseDouble(employee.getCompanyTripDeduction(), 0);
        double totalDeductions = sscDeduction + companyTripDeduction;

        double netSalary = basicSalary + totalAllowances - totalDeductions;

        // Set calculated values
        employee.setTotalBasicSalary(String.valueOf(basicSalary));
        employee.setTotalAllowances(String.valueOf(totalAllowances));
        employee.setTotalDeductions(String.valueOf(totalDeductions));
        employee.setNetSalary(String.valueOf(netSalary));
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty())
            return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty())
            return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String value, double defaultValue) {
        if (value == null || value.isEmpty())
            return defaultValue;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}