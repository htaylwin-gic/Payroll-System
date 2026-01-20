package com.empManagement.empManagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_payroll")
@Data
@NoArgsConstructor
public class EmployeePayroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String monthYear;
    private String payrollDate;
    private Double basicSalary;
    private Integer workingDays;
    private Integer presentDays;
    private Integer overtimeHours;
    private Integer leaveDays;
    private Integer lateDays;
    private Integer myanmarServiceYears;
    private Integer gicjpServiceYears;
    private String assignmentLevel;
    private String managementLevel;
    private Double leaveDeduction;
    private Double lateDeduction;
    private Double incomeTax;
    private Double loanReturn;
    private Double ssc;
    private Double companyTrip;
    private Double allowance;
    private Double overtime;
    private Double home;
    private Double bonus;
    private Double businessTrip;
    private Double continuedYear;
    private Double homeTownVisit;
    private Double manualAdjust;
    private Double attendancePerfect;
    private Double exchangeBenefit;
    private Double travelFee;
    private Double basicSalaryAfterDeduction;
    private Double netSalary;
    private Double totalPayment;
    private Double educationAllowance;
    private Double evaluationAllowance;
    private Double japaneseJlptAllowance;
    private Double japaneseNatAllowance;
    private Double englishAllowance;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private LocalDateTime createdAt = LocalDateTime.now();
}