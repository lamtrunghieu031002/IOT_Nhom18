package model;

public class Device {
    private String deviceId;
    private String name;
    private String model;
    private String status;
    private String createdAt;

    public Device() {}
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
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setName(String name) { this.name = name; }
    public void setModel(String model) { this.model = model; }
    public void setStatus(String status) { this.status = status; }

    public void setCreatedAt(String s) {
        this.createdAt = s;
    }
}
