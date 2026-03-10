package com.empManagement.empManagement.repository;

import com.empManagement.empManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(String role);

    @Query("SELECT u FROM User u WHERE u.employeeId = :employeeId")
    Optional<User> findByEmployeeId(@Param("employeeId") Integer employeeId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginDate = :date, u.lastLoginIp = :ip WHERE u.username = :username")
    void updateLastLogin(@Param("username") String username, @Param("date") LocalDateTime date, @Param("ip") String ip);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedAttempts = :attempts WHERE u.username = :username")
    void updateFailedAttempts(@Param("username") String username, @Param("attempts") int attempts);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.accountNonLocked = false, u.lockTime = :lockTime WHERE u.username = :username")
    void lockUser(@Param("username") String username, @Param("lockTime") LocalDateTime lockTime);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.accountNonLocked = true, u.failedAttempts = 0, u.lockTime = null WHERE u.username = :username")
    void unlockUser(@Param("username") String username);
}