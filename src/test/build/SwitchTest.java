package test.build;

import base.Switch;
import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SwitchTest {
    private Switch networkSwitch;
    private NetworkDevice testDevice1;
    private NetworkDevice testDevice2;
    private static final int PORT_COUNT = 24;
    private static final int SWITCHING_SPEED = 1000;

    @BeforeEach
    void setUp() {
        networkSwitch = new Switch("SW001", "192.168.1.1", "NYC", PORT_COUNT, SWITCHING_SPEED);
        testDevice1 = new NetworkDevice("DEV001", "192.168.1.2", "NYC");
        testDevice2 = new NetworkDevice("DEV002", "192.168.1.3", "NYC");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("SW001", networkSwitch.getDeviceID(), "Device ID should match");
        assertEquals("192.168.1.1", networkSwitch.getIpAddress(), "IP address should match");
        assertEquals("NYC", networkSwitch.getLocation(), "Location should match");
        assertTrue(networkSwitch.isOnline(), "Should be initially online");

        assertEquals(PORT_COUNT, networkSwitch.getPortCount(), "Port count should match");
        assertEquals(SWITCHING_SPEED, networkSwitch.getSwitchingSpeed(), "Speed should match");
        assertEquals(PORT_COUNT, networkSwitch.getAvailablePorts(), "All ports should be available");
        assertTrue(networkSwitch.getOccupiedPorts().isEmpty(), "No ports should be occupied");
        assertTrue(networkSwitch.getPortMap().isEmpty(), "Port map should be empty");
    }

    @Test
    void shouldManageValidConnections() {
        assertTrue(networkSwitch.connectDevice(1, testDevice1), "Should connect to valid port");
        assertEquals(testDevice1, networkSwitch.getConnectedDevice(1), "Should retrieve connected device");
        assertEquals(PORT_COUNT - 1, networkSwitch.getAvailablePorts(), "Should decrease available ports");
        assertEquals(1, networkSwitch.getOccupiedPorts().size(), "Should have one occupied port");
        assertTrue(networkSwitch.getOccupiedPorts().contains(1), "Should contain connected port");

        assertTrue(networkSwitch.connectDevice(2, testDevice2), "Should connect second device");
        assertEquals(2, networkSwitch.getOccupiedPorts().size(), "Should have two occupied ports");
        assertEquals(PORT_COUNT - 2, networkSwitch.getAvailablePorts(), "Should decrease available ports");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 25, 100})
    void shouldRejectInvalidPorts(int invalidPort) {
        assertFalse(networkSwitch.connectDevice(invalidPort, testDevice1), "Should reject invalid port");
        assertNull(networkSwitch.getConnectedDevice(invalidPort), "Should not have device on invalid port");
        assertEquals(PORT_COUNT, networkSwitch.getAvailablePorts(), "Available ports should not change");
    }

    @Test
    void shouldHandlePortReuse() {
        assertTrue(networkSwitch.connectDevice(1, testDevice1), "Should connect first device");
        assertFalse(networkSwitch.connectDevice(1, testDevice2), "Should reject duplicate port use");
        assertEquals(testDevice1, networkSwitch.getConnectedDevice(1), "Should maintain original connection");

        assertTrue(networkSwitch.disconnectDevice(1), "Should disconnect device");
        assertTrue(networkSwitch.connectDevice(1, testDevice2), "Should allow reconnection to freed port");
        assertEquals(testDevice2, networkSwitch.getConnectedDevice(1), "Should have new device on port");
    }

    @Test
    void shouldProvideImmutableCollections() {
        networkSwitch.connectDevice(1, testDevice1);
        networkSwitch.connectDevice(2, testDevice2);

        Map<Integer, NetworkDevice> portMap = networkSwitch.getPortMap();
        assertThrows(UnsupportedOperationException.class, () ->
                        portMap.put(3, testDevice1),
                "Should not allow adding to port map"
        );
        assertThrows(UnsupportedOperationException.class, () ->
                        portMap.remove(1),
                "Should not allow removing from port map"
        );

        List<Integer> occupiedPorts = networkSwitch.getOccupiedPorts();
        assertThrows(UnsupportedOperationException.class, () ->
                        occupiedPorts.add(3),
                "Should not allow adding to occupied ports"
        );
        assertThrows(UnsupportedOperationException.class, () ->
                        occupiedPorts.remove(0),
                "Should not allow removing from occupied ports"
        );
    }

    @Test
    void shouldMaintainPortOrder() {
        networkSwitch.connectDevice(5, testDevice1);
        networkSwitch.connectDevice(2, testDevice2);

        List<Integer> occupiedPorts = networkSwitch.getOccupiedPorts();
        assertEquals(2, occupiedPorts.get(0), "First port should be lowest");
        assertEquals(5, occupiedPorts.get(1), "Second port should be highest");

        NetworkDevice testDevice3 = new NetworkDevice("DEV003", "192.168.1.4", "NYC");
        networkSwitch.connectDevice(3, testDevice3);

        occupiedPorts = networkSwitch.getOccupiedPorts();
        assertEquals(2, occupiedPorts.get(0), "First port should remain lowest");
        assertEquals(3, occupiedPorts.get(1), "Second port should be middle");
        assertEquals(5, occupiedPorts.get(2), "Third port should remain highest");
    }

    @Test
    void shouldHandleFullCapacity() {
        for (int i = 1; i <= PORT_COUNT; i++) {
            NetworkDevice device = new NetworkDevice(
                    String.format("DEV%03d", i),
                    String.format("192.168.1.%d", i + 1),
                    "NYC"
            );
            assertTrue(networkSwitch.connectDevice(i, device),
                    "Should connect device " + i);
        }

        assertEquals(0, networkSwitch.getAvailablePorts(), "Should have no available ports");
        assertEquals(PORT_COUNT, networkSwitch.getOccupiedPorts().size(), "All ports should be occupied");

        NetworkDevice extraDevice = new NetworkDevice("DEV999", "192.168.1.100", "NYC");
        assertFalse(networkSwitch.connectDevice(1, extraDevice),
                "Should reject connection when port is occupied");
    }

    @Test
    void shouldHandleDeviceStates() {
        networkSwitch.connectDevice(1, testDevice1);
        networkSwitch.connectDevice(2, testDevice2);

        testDevice1.setOnline(false);
        testDevice2.setOnline(false);

        assertEquals(testDevice1, networkSwitch.getConnectedDevice(1),
                "Should maintain offline device connection");
        assertEquals(testDevice2, networkSwitch.getConnectedDevice(2),
                "Should maintain offline device connection");
        assertEquals(2, networkSwitch.getOccupiedPorts().size(),
                "Should maintain port count");
    }
}