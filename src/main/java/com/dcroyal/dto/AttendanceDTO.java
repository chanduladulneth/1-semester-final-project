package com.dcroyal.dto;

/**
 * AttendanceDTO.java
 * Data Transfer Object for employee_attendance table.
 * Student-level clean Java code with standard getters and setters.
 */
public class AttendanceDTO {

    private int     attendanceId;
    private int     userId;
    private String  attendanceDate;
    private String  checkIn;
    private String  checkOut;
    private String  status; // PRESENT, ABSENT, LATE, HALF_DAY, LEAVE
    private Integer recordedBy;

    public AttendanceDTO() {
    }

    public AttendanceDTO(int attendanceId, int userId, String attendanceDate,
                         String checkIn, String checkOut, String status, Integer recordedBy) {
        this.attendanceId   = attendanceId;
        this.userId         = userId;
        this.attendanceDate = attendanceDate;
        this.checkIn        = checkIn;
        this.checkOut       = checkOut;
        this.status         = status;
        this.recordedBy     = recordedBy;
    }

    public int     getAttendanceId()                 { return attendanceId; }
    public void    setAttendanceId(int id)           { this.attendanceId = id; }

    public int     getUserId()                       { return userId; }
    public void    setUserId(int userId)             { this.userId = userId; }

    public String  getAttendanceDate()               { return attendanceDate; }
    public void    setAttendanceDate(String date)    { this.attendanceDate = date; }

    public String  getCheckIn()                      { return checkIn; }
    public void    setCheckIn(String checkIn)        { this.checkIn = checkIn; }

    public String  getCheckOut()                     { return checkOut; }
    public void    setCheckOut(String checkOut)      { this.checkOut = checkOut; }

    public String  getStatus()                       { return status; }
    public void    setStatus(String status)          { this.status = status; }

    public Integer getRecordedBy()                   { return recordedBy; }
    public void    setRecordedBy(Integer recordedBy) { this.recordedBy = recordedBy; }
}
