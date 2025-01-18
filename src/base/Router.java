package base;

import java.util.*;

public class Router extends NetworkDevice {
    protected int bandwidth;
    protected final Map<String, String> routingTable;
    protected final List<NetworkDevice> connectedDevices;

    public Router(String deviceID, String ipAddress, String location, int bandwidth) {
        super(deviceID, ipAddress, location);
        this.bandwidth = bandwidth;
        this.routingTable = new HashMap<>();
        this.connectedDevices = new ArrayList<>();
    }

    public void routePacket(String source, String destination) {
        System.out.println("Routing packet: " + source + " -> " + destination);
    }

    public void addDevice(NetworkDevice device) {
        connectedDevices.add(device);
        routingTable.put(device.getIpAddress(), device.getDeviceID());
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public Map<String, String> getRoutingTable() {
        return routingTable;
    }

    public List<NetworkDevice> getConnectedDevices() {
        return connectedDevices;
    }
}