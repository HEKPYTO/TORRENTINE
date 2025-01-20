package base;

import java.util.*;

public class Router extends Device {
    protected int bandwidth;
    protected final Map<String, String> routingTable;
    protected final List<Device> connectedDevices;

    public Router(String deviceID, String ipAddress, String location, int bandwidth) {
        super(deviceID, ipAddress, location);
        setBandwidth(bandwidth);
        this.routingTable = new HashMap<>();
        this.connectedDevices = new ArrayList<>();
    }

    public boolean addDevice(Device device) {
        if (device == null) return false;
        connectedDevices.add(device);
        routingTable.put(device.getIpAddress(), device.getDeviceID());
        return true;
    }

    public boolean removeDevice(Device device) {
        if (device == null) {
            return false;
        }
        boolean removed = connectedDevices.remove(device);
        if (removed) {
            routingTable.remove(device.getIpAddress());
        }
        return removed;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = Math.max(0, bandwidth);
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public Map<String, String> getRoutingTable() {
        return routingTable;
    }

    public List<Device> getConnectedDevices() {
        return connectedDevices;
    }
}