package base;

import java.util.*;

public class Switch extends Device {
    private final int portCount;
    private final Map<Integer, Device> portMap;
    private final int switchingSpeed; // in Mbps

    public Switch(String deviceID, String ipAddress, String location,
                  int portCount, int switchingSpeed) {
        super(deviceID, ipAddress, location);
        this.portCount = portCount;
        this.switchingSpeed = switchingSpeed;
        this.portMap = new HashMap<>();
    }

    public boolean addDevice(int port, Device device) {
        if (device == null) return false;
        else if (port < 1 || port > portCount) {
            return false;
        }
        else if (portMap.containsKey(port)) {
            return false;
        }
        portMap.put(port, device);
        return true;
    }

    public boolean removeDevice(int port) {
        return port > 0 && port <= portCount && portMap.remove(port) != null;
    }

    public Device getConnectedDevice(int port) {
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

    public Map<Integer, Device> getPortMap() {
        return Collections.unmodifiableMap(portMap);
    }
}