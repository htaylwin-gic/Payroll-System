package com.empManagement.empManagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    private String gender;
    private String nationality;
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;
    private String NIC;
    private String position;
    private String department;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "employment_status")
    private String employmentStatus;

    @Column(name = "salary", nullable = true)
    private Double salary;

    // New fields for employee ID
    @Column(name = "employee_id", unique = true)
    private String employeeId;

    // New fields for transportation
    @Column(name = "transportation_steps")
    private Integer transportationSteps;

    // Salary Components
    @Column(name = "band_level")
    private String bandLevel;

    @Column(name = "education_level")
    private String educationLevel;

    @Column(name = "education_allowance")
    private String educationAllowance;

    @Column(name = "evaluation_grade")
    private String evaluationGrade;

    @Column(name = "evaluation_allowance")
    private String evaluationAllowance;

    @Column(name = "japanese_level")
    private String japaneseLevel;

    @Column(name = "japanese_allowance")
    private String japaneseAllowance;

    // New fields for Japanese NAT Test
    @Column(name = "japanese_nat_test")
    private String japaneseNatTest;

    @Column(name = "japanese_nat_allowance")
    private String japaneseNatAllowance;

    @Column(name = "english_toeic_score")
    private String englishToeicScore;

    @Column(name = "english_toefl_score")
    private String englishToeflScore;

    @Column(name = "english_ielts_score")
    private String englishIeltsScore;

    @Column(name = "english_allowance")
    private String englishAllowance;

    // New field for English Level
    @Column(name = "english_level")
    private String englishLevel;

    @Column(name = "myanmar_service_years")
    private Integer myanmarServiceYears;

    @Column(name = "gicp_service_years")
    private Integer gicpServiceYears;

    @Column(name = "service_allowance")
    private String serviceAllowance;

    @Column(name = "assignment_level")
    private String assignmentLevel;

    @Column(name = "assignment_allowance")
    private String assignmentAllowance;

    @Column(name = "management_level")
    private String managementLevel;

    @Column(name = "management_allowance")
    private String managementAllowance;

    @Column(name = "house_allowance")
    private String houseAllowance;

    @Column(name = "transportation_allowance")
    private String transportationAllowance;

    @Column(name = "perfect_attendance_allowance")
    private String perfectAttendanceAllowance;

    @Column(name = "ssc_deduction")
    private String sscDeduction;

    @Column(name = "company_trip_deduction")
    private String companyTripDeduction;

    private String date;
    private String time;

    @Column(name = "is_attedence")
    private String isAttedence;

    @Column(name = "is_payroll")
    private String isPayroll;

    private String status;

    @Column(name = "total_basic_salary")
    private String totalBasicSalary;

    @Column(name = "total_allowances")
    private String totalAllowances;

    @Column(name = "total_deductions")
    private String totalDeductions;

    @Column(name = "net_salary")
    private String netSalary;

    // For backward compatibility - add these as separate methods
    // Thymeleaf will use these getters/setters
    public String getFull_name() {
        return fullName;
    }

    public void setFull_name(String fullName) {
        this.fullName = fullName;
    }

    public String getDate_of_birth() {
        return dateOfBirth;
    }

    public void setDate_of_birth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone_number() {
        return phoneNumber;
    }

    public void setPhone_number(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStart_date() {
        return startDate;
    }

    public void setStart_date(String startDate) {
        this.startDate = startDate;
    }

    public String getEmployment_status() {
        return employmentStatus;
    }

    public void setEmployment_status(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    // Add getters/setters for new fields - these will be used by Thymeleaf
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getTransportationSteps() {
        return transportationSteps;
    }

    public void setTransportationSteps(Integer transportationSteps) {
        this.transportationSteps = transportationSteps;
    }

    public String getJapaneseNatTest() {
        return japaneseNatTest;
    }

    public void setJapaneseNatTest(String japaneseNatTest) {
        this.japaneseNatTest = japaneseNatTest;
    }

    public String getEnglishLevel() {
        return englishLevel;
    }

    public void setEnglishLevel(String englishLevel) {
        this.englishLevel = englishLevel;
    }

    public String getAssignmentLevel() {
        return assignmentLevel;
    }

    public void setAssignmentLevel(String assignmentLevel) {
        this.assignmentLevel = assignmentLevel;
    }

    public String getManagementLevel() {
        return managementLevel;
    }

    public void setManagementLevel(String managementLevel) {
        this.managementLevel = managementLevel;
    }

    // Add getters/setters for other fields that might be needed
    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getEvaluationGrade() {
        return evaluationGrade;
    }

    public void setEvaluationGrade(String evaluationGrade) {
        this.evaluationGrade = evaluationGrade;
    }

    public String getJapaneseLevel() {
        return japaneseLevel;
    }

    public void setJapaneseLevel(String japaneseLevel) {
        this.japaneseLevel = japaneseLevel;
    }
}