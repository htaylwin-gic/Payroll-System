package com.empManagement.empManagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRequest {
    private Long id;
    private Double basicSalary;

    private Integer employeeId;
    private String monthYear;
    private Integer workingDays;
    private Integer presentDays;
    private Integer overtimeHours;
    private Integer leaveDays;
    private Integer lateDays;

    private Integer myanmarYears;
    private Integer gicjpYears;
    private String assignmentLevel;
    private String managementLevel;

    // Allowance fields
    private Double educationAllowance = 0.0;
    private Double evaluationAllowance = 0.0;
    private Double japaneseJlptAllowance = 0.0;
    private Double japaneseNatAllowance = 0.0;
    private Double englishAllowance = 0.0;

    // Deductions
    private Double leaveDeduction = 0.0;
    private Double lateDeduction = 0.0;
    private Double incomeTax = 0.0;
    private Double loanReturn = 0.0;
    private Double ssc = 0.0;
    private Double companyTrip = 0.0;

    // Allowances and additions
    private Double allowance = 0.0;
    private Double overtime = 0.0;
    private Double home = 0.0;
    private Double bonus = 0.0;
    private Double businessTrip = 0.0;
    private Double continuedYear = 0.0;
    private Double homeTownVisit = 0.0;
    private Double manualAdjust = 0.0;
    private Double attendancePerfect = 0.0;
    private Double exchangeBenefit = 0.0;
    private Double travelFee = 0.0;

    private Integer busRoute;
    private Double transportationFee;

}