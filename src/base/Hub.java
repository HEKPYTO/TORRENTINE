package base;

import java.util.*;

public class Hub extends NetworkDevice {
    private final int portCount;
    private final int bandwidth;
    private final List<NetworkDevice> connectedDevices;
    private final boolean isDualSpeed;

    public Hub(String deviceID, String ipAddress, String location,
               int portCount, int bandwidth, boolean isDualSpeed) {
        super(deviceID, ipAddress, location);
        this.portCount = portCount;
        this.bandwidth = bandwidth;
        this.isDualSpeed = isDualSpeed;
        this.connectedDevices = new ArrayList<>();
    }

    public boolean connectDevice(NetworkDevice device) {
        if (device == null) {
            return false;
        }
        if (connectedDevices.size() >= portCount) {
            return false;
        }
        if (!connectedDevices.contains(device)) {
            connectedDevices.add(device);
            return true;
        }
        return false;
    }

    public boolean disconnectDevice(NetworkDevice device) {
        return connectedDevices.remove(device);
    }

    public int getPortCount() {
        return portCount;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public boolean isDualSpeed() {
        return isDualSpeed;
    }

    public int getConnectedDeviceCount() {
        return connectedDevices.size();
    }

    public int getAvailablePorts() {
        return portCount - connectedDevices.size();
    }

    public List<NetworkDevice> getConnectedDevices() {
        return new ArrayList<>(connectedDevices);
    }

    public int broadcast(NetworkDevice sourceDevice, byte[] data) {
        // Check if hub is offline or source device is null
        if (!isOnline() || sourceDevice == null) {
            return 0;
        }

        // Check if source device is connected and online
        if (!connectedDevices.contains(sourceDevice) || !sourceDevice.isOnline()) {
            return 0;
        }

        // Count online devices that can receive the broadcast
        int broadcastCount = 0;
        for (NetworkDevice device : connectedDevices) {
            if (device != sourceDevice && device.isOnline()) {
                broadcastCount++;
            }
        }
        return broadcastCount;
    }

    public double getCurrentThroughput() {
        int activeDevices = (int) connectedDevices.stream()
                .filter(NetworkDevice::isOnline)
                .count();

        if (activeDevices <= 1) {
            return bandwidth;
        }
        // Hub shares bandwidth among all connected devices
        return (double) bandwidth / activeDevices;
    }
}