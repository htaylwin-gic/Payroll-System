package com.empManagement.empManagement.dto;

import com.empManagement.empManagement.entity.Employee;
import java.util.ArrayList;
import java.util.List;

public class ExcelUploadResult {
    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<Employee> successfulEmployees = new ArrayList<>();
    private List<String> errorMessages = new ArrayList<>();

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<Employee> getSuccessfulEmployees() {
        return successfulEmployees;
    }

    public void setSuccessfulEmployees(List<Employee> successfulEmployees) {
        this.successfulEmployees = successfulEmployees;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
}