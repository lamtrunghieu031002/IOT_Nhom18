package model;

public class MeasurementHistory {

    private int id;
    private String subjectName;
    private String alcoholLevel;   // "8.77 mg/L"
    private String violationLevel; // "HIGH"
    private String testTime;       //  "06/12/2025 21:16"
    private String location;       // Hà Nội
    private String officerFullName;

    public MeasurementHistory(int id, String subjectName, String alcoholLevel,
                              String violationLevel, String testTime,
                              String location, String officerFullName) {
        this.id = id;
        this.subjectName = subjectName;
        this.alcoholLevel = alcoholLevel;
        this.violationLevel = violationLevel;
        this.testTime = testTime;
        this.location = location;
        this.officerFullName = officerFullName;
    }

    public int getId() { return id; }
    public String getSubjectName() { return subjectName; }
    public String getAlcoholLevel() { return alcoholLevel; }
    public String getViolationLevel() { return violationLevel; }
    public String getTestTime() { return testTime; }
    public String getLocation() { return location; }
    public String getOfficerFullName() { return officerFullName; }

}
