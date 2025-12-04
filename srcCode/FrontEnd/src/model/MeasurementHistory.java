package model;

public class MeasurementHistory {
    private Integer id;
    private String name;
    private String cccd;
    private String hometown;
    private String time;
    private String device;
    private double result;

    public MeasurementHistory(Integer id, String name, String cccd, String hometown, String time, String device, double result) {
        this.id = id;
        this.name = name;
        this.cccd = cccd;
        this.hometown = hometown;
        this.time = time;
        this.device = device;
        this.result = result;
    }

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getCccd() { return cccd; }
    public String getHometown() { return hometown; }
    public String getTime() { return time; }
    public String getDevice() { return device; }
    public double getResult() { return result; }
}
