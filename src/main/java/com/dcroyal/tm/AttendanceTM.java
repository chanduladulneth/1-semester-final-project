package com.dcroyal.tm;

/**
 * AttendanceTM.java
 * Table Model for displaying employee attendance records in the TableView.
 * Student-level clean Java code with standard properties.
 */
public class AttendanceTM {

    private int    attendanceId;
    private int    userId;
    private String employeeName;
    private String roleName;
    private String attendanceDate;
    private String checkIn;
    private String checkOut;
    private String status;
    private String recordedByName;

    public AttendanceTM(int attendanceId, int userId, String employeeName, String roleName,
                        String attendanceDate, String checkIn, String checkOut,
                        String status, String recordedByName) {
        this.attendanceId     = attendanceId;
        this.userId           = userId;
        this.employeeName     = employeeName;
        this.roleName         = roleName;
        this.attendanceDate   = attendanceDate;
        this.checkIn          = (checkIn != null && !checkIn.isEmpty()) ? checkIn : "--:--:--";
        this.checkOut         = (checkOut != null && !checkOut.isEmpty()) ? checkOut : "--:--:--";
        this.status           = status;
        this.recordedByName   = (recordedByName != null && !recordedByName.isEmpty()) ? recordedByName : "System";
    }

    public int    getAttendanceId()                { return attendanceId; }
    public void   setAttendanceId(int id)          { this.attendanceId = id; }

    public int    getUserId()                      { return userId; }
    public void   setUserId(int userId)            { this.userId = userId; }

    public String getEmployeeName()                { return employeeName; }
    public void   setEmployeeName(String name)     { this.employeeName = name; }

    public String getRoleName()                    { return roleName; }
    public void   setRoleName(String role)         { this.roleName = role; }

    public String getAttendanceDate()              { return attendanceDate; }
    public void   setAttendanceDate(String date)   { this.attendanceDate = date; }

    public String getCheckIn()                     { return checkIn; }
    public void   setCheckIn(String checkIn)       { this.checkIn = checkIn; }

    public String getCheckOut()                    { return checkOut; }
    public void   setCheckOut(String checkOut)     { this.checkOut = checkOut; }

    public String getStatus()                      { return status; }
    public void   setStatus(String status)         { this.status = status; }

    public String getRecordedByName()              { return recordedByName; }
    public void   setRecordedByName(String by)     { this.recordedByName = by; }
}
