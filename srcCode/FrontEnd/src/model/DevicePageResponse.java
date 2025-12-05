package model;

import java.util.List;

public class DevicePageResponse {
    public List<Device> devices;
    public int page;
    public int size;
    public int total;
    public int totalPages;

    public DevicePageResponse(List<Device> devices, int page, int size, int total, int totalPages) {
        this.devices = devices;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
