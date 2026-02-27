package com.empManagement.empManagement.repository;

import com.empManagement.empManagement.entity.EmployeePayroll;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeePayrollRepository extends JpaRepository<EmployeePayroll, Long> {

    List<EmployeePayroll> findByEmployeeId(Integer employeeId);

    List<EmployeePayroll> findByMonthYear(String monthYear);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmployeePayroll ep WHERE ep.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") int employeeId);

    @Query("SELECT p FROM EmployeePayroll p WHERE p.employee.id = :employeeId AND p.monthYear = :monthYear")
    EmployeePayroll findByEmployeeAndMonthYear(@Param("employeeId") Integer employeeId,
            @Param("monthYear") String monthYear);

    @Query("SELECT DISTINCT p.monthYear FROM EmployeePayroll p ORDER BY p.monthYear DESC")
    List<String> findDistinctMonthYears();

    @Query("SELECT SUM(p.totalPayment) FROM EmployeePayroll p WHERE p.monthYear = :monthYear")
    Double getTotalPaymentByMonth(@Param("monthYear") String monthYear);
}