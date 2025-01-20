package base;

import java.util.*;

public class Switch extends NetworkDevice {
    private final int portCount;
    private final Map<Integer, NetworkDevice> portMap;
    private final int switchingSpeed; // in Mbps

    public Switch(String deviceID, String ipAddress, String location,
                  int portCount, int switchingSpeed) {
        super(deviceID, ipAddress, location);
        this.portCount = portCount;
        this.switchingSpeed = switchingSpeed;
        this.portMap = new HashMap<>();
    }

    public boolean connectDevice(int port, NetworkDevice device) {
        if (device == null) return false;
        if (port < 1 || port > portCount) {
            return false;
        }
        if (portMap.containsKey(port)) {
            return false;
        }
        portMap.put(port, device);
        return true;
    }

    public boolean disconnectDevice(int port) {
        if (port < 1 || port > portCount) {
            return false;
        }
        return portMap.remove(port) != null;
    }

    public NetworkDevice getConnectedDevice(int port) {
        return portMap.get(port);
    }

    public int getPortCount() {
        return portCount;
    }

    public int getSwitchingSpeed() {
        return switchingSpeed;
    }

    public int getAvailablePorts() {
        return portCount - portMap.size();
    }

    public List<Integer> getOccupiedPorts() {
        List<Integer> ports = new ArrayList<>(portMap.keySet());
        Collections.sort(ports);
        return Collections.unmodifiableList(ports);
    }

    public Map<Integer, NetworkDevice> getPortMap() {
        return Collections.unmodifiableMap(portMap);
    }
}