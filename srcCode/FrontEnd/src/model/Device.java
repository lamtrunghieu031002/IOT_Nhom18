package model;

public class Device {
    private String deviceId;
    private String name;
    private String model;
    private String status;
    private String createdAt;

    public Device(String deviceId, String name, String model, String status, String createdAt) {
        this.deviceId = deviceId;
        this.name = name;
        this.model = model;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getDeviceId() { return deviceId; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
