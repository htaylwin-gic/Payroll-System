package com.empManagement.empManagement.repository;

import com.empManagement.empManagement.entity.Employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.NIC) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Employee> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE LOWER(e.isAttedence) = LOWER(:isAttedence)")
    long countByIsAttedence(@Param("isAttedence") String isAttedence);

    @Query("SELECT COUNT(e) FROM Employee e WHERE LOWER(e.department) = LOWER(:department)")
    long countByDepartment(@Param("department") String department);

    @Query("SELECT e FROM Employee e WHERE LOWER(e.isAttedence) = LOWER(:isAttedence)")
    List<Employee> findByIsAttedence(@Param("isAttedence") String isAttedence);

    @Query("SELECT e FROM Employee e WHERE e.status = :status OR :status IS NULL")
    List<Employee> findByStatus(@Param("status") String status);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = 'Active'")
    long countActiveEmployees();

    @Query("SELECT COUNT(e) FROM Employee e WHERE LOWER(e.isAttedence) = LOWER('Yes')")
    long countWithAttendance();

    @Query("SELECT COUNT(e) FROM Employee e WHERE LOWER(e.isAttedence) = LOWER('No')")
    long countWithoutAttendance();

    @Query("SELECT e FROM Employee e WHERE LOWER(e.isAttedence) = LOWER('No')")
    List<Employee> findEmployeesWithNoAttendance();

    List<Employee> findAllByOrderByEmployeeIdAsc();

    @Query("SELECT e FROM Employee e WHERE e.busRoute = :busRoute")
    List<Employee> findByBusRoute(@Param("busRoute") Integer busRoute);

    Employee findByEmployeeId(String employeeId);
}