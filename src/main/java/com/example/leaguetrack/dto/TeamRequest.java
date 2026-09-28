package com.example.leaguetrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeamRequest {

    @NotBlank(message = "Team name cannot be blank")
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    private String name;

    @Size(max = 100, message = "Captain name cannot exceed 100 characters")
    private String captain;

    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    private String department;

    @Size(max = 50, message = "Contact info cannot exceed 50 characters")
    private String contactNumber;

    public TeamRequest() {}

    public TeamRequest(String name, String captain, String department, String contactNumber) {
        this.name = name;
        this.captain = captain;
        this.department = department;
        this.contactNumber = contactNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaptain() {
        return captain;
    }

    public void setCaptain(String captain) {
        this.captain = captain;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
