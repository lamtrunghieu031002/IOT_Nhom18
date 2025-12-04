package model;
public class Device {
    private String macAddress;
    private String name;
    private String model;
    private String dateAdded;
    private String status;

    // Constructor
    public Device(String macAddress, String name, String model, String dateAdded, String status) {
        this.macAddress = macAddress;
        this.name = name;
        this.model = model;
        this.dateAdded = dateAdded;
        this.status = status;
    }

    // Getters
    public String getMacAddress() { return macAddress; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public String getDateAdded() { return dateAdded; }
    public String getStatus() { return status; }
}
