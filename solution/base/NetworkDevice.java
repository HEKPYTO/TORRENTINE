package base;

import util.IPUtils;

public class NetworkDevice {
    private final String deviceID;
    private final String ipAddress;
    private final String location;
    private boolean isOnline;

    public NetworkDevice(String deviceID, String ipAddress, String location) {
        this.deviceID = deviceID;
        this.ipAddress = IPUtils.validateIP(ipAddress);
        this.location = location;
        this.isOnline = true;
    }

    public String getDeviceID() { return deviceID; }
    public String getIpAddress() { return ipAddress; }
    public String getLocation() { return location; }
    public boolean isOnline() { return isOnline; }

    public void setOnline(boolean status) {
        this.isOnline = status;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NetworkDevice && ((NetworkDevice) obj).deviceID.equals(this.deviceID);
    }
}