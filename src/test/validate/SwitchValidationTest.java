package test.validate;

import base.Switch;
import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwitchValidationTest {
    private Switch networkSwitch;
    private NetworkDevice device1;
    private NetworkDevice device2;
    private NetworkDevice device3;
    private static final int PORT_COUNT = 32;
    private static final int SWITCHING_SPEED = 40000;

    @BeforeEach
    void setUp() {
        networkSwitch = new Switch("ESW001", "10.0.1.1", "LAX", PORT_COUNT, SWITCHING_SPEED);
        device1 = new NetworkDevice("HOST001", "10.0.1.10", "LAX");
        device2 = new NetworkDevice("HOST002", "10.0.1.11", "LAX");
        device3 = new NetworkDevice("HOST003", "10.0.1.12", "LAX");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("ESW001", networkSwitch.getDeviceID(), "Device ID should match");
        assertEquals("10.0.1.1", networkSwitch.getIpAddress(), "IP should match");
        assertEquals("LAX", networkSwitch.getLocation(), "Location should match");
        assertTrue(networkSwitch.isOnline(), "Should be initially online");

        assertEquals(PORT_COUNT, networkSwitch.getPortCount(), "Port count should match");
        assertEquals(SWITCHING_SPEED, networkSwitch.getSwitchingSpeed(), "Speed should match");
        assertEquals(PORT_COUNT, networkSwitch.getAvailablePorts(), "All ports should be available");
        assertTrue(networkSwitch.getOccupiedPorts().isEmpty(), "No ports should be occupied");
        assertTrue(networkSwitch.getPortMap().isEmpty(), "Port map should be empty");

        Switch switch2 = new Switch("ESW002", "10.0.2.1", "SFO", 48, 100000);
        assertEquals(48, switch2.getPortCount(), "Different port count should match");
        assertEquals(100000, switch2.getSwitchingSpeed(), "Different speed should match");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100, 33, 100, -2147483648, 2147483647})
    void shouldRejectInvalidPorts(int invalidPort) {
        assertFalse(networkSwitch.connectDevice(invalidPort, device1), "Should reject connection to invalid port: " + invalidPort);
        assertNull(networkSwitch.getConnectedDevice(invalidPort), "Should not have device on invalid port: " + invalidPort);

        assertFalse(networkSwitch.disconnectDevice(invalidPort), "Should reject disconnection from invalid port: " + invalidPort);

        assertEquals(PORT_COUNT, networkSwitch.getAvailablePorts(), "Available ports should be unchanged after invalid port attempt");
        assertTrue(networkSwitch.getOccupiedPorts().isEmpty(), "No ports should be occupied after invalid attempt");
    }

    @Test
    void shouldHandlePortConnections() {
        assertTrue(networkSwitch.connectDevice(1, device1), "Should connect to first port");
        assertEquals(device1, networkSwitch.getConnectedDevice(1), "Should get device from first port");

        assertTrue(networkSwitch.connectDevice(PORT_COUNT, device2), "Should connect to last port");
        assertEquals(device2, networkSwitch.getConnectedDevice(PORT_COUNT), "Should get device from last port");

        assertTrue(networkSwitch.connectDevice(PORT_COUNT / 2, device3), "Should connect to middle port");
        assertEquals(device3, networkSwitch.getConnectedDevice(PORT_COUNT / 2), "Should get device from middle port");

        assertEquals(PORT_COUNT - 3, networkSwitch.getAvailablePorts(), "Should have correct available ports");
        assertEquals(3, networkSwitch.getOccupiedPorts().size(), "Should have correct occupied ports");
    }

    @Test
    void shouldHandlePortDisconnections() {
        networkSwitch.connectDevice(1, device1);
        networkSwitch.connectDevice(PORT_COUNT, device2);
        networkSwitch.connectDevice(PORT_COUNT / 2, device3);
        assertEquals(3, networkSwitch.getOccupiedPorts().size(), "Should start with three connections");

        assertTrue(networkSwitch.disconnectDevice(1), "Should disconnect from first port");
        assertNull(networkSwitch.getConnectedDevice(1), "First port should be empty");

        assertTrue(networkSwitch.disconnectDevice(PORT_COUNT), "Should disconnect from last port");
        assertNull(networkSwitch.getConnectedDevice(PORT_COUNT), "Last port should be empty");

        assertTrue(networkSwitch.disconnectDevice(PORT_COUNT / 2), "Should disconnect from middle port");
        assertNull(networkSwitch.getConnectedDevice(PORT_COUNT / 2), "Middle port should be empty");

        assertEquals(PORT_COUNT, networkSwitch.getAvailablePorts(), "All ports should be available");
        assertTrue(networkSwitch.getOccupiedPorts().isEmpty(), "No ports should be occupied");
    }

    @Test
    void shouldHandlePortReuse() {
        assertTrue(networkSwitch.connectDevice(1, device1), "Should connect first device");
        assertTrue(networkSwitch.disconnectDevice(1), "Should disconnect first device");
        assertTrue(networkSwitch.connectDevice(1, device2), "Should connect second device to same port");
        assertEquals(device2, networkSwitch.getConnectedDevice(1), "Should get new device from reused port");

        for (int i = 0; i < 5; i++) {
            assertTrue(networkSwitch.disconnectDevice(1), "Should disconnect in cycle " + i);
            assertTrue(networkSwitch.connectDevice(1, device1), "Should connect device1 in cycle " + i);
            assertTrue(networkSwitch.disconnectDevice(1), "Should disconnect in cycle " + i);
            assertTrue(networkSwitch.connectDevice(1, device2), "Should connect device2 in cycle " + i);
        }
    }

    @Test
    void shouldHandleMultipleOperations() {
        for (int i = 2; i <= PORT_COUNT; i += 2) {
            assertTrue(networkSwitch.connectDevice(i, new NetworkDevice("HOST" + i, "10.0.1." + i, "LAX")), "Should connect to port " + i);
        }

        assertEquals(PORT_COUNT / 2, networkSwitch.getOccupiedPorts().size(), "Half the ports should be occupied");

        for (int i = 1; i <= PORT_COUNT; i += 2) {
            assertTrue(networkSwitch.connectDevice(i, new NetworkDevice("HOST" + i, "10.0.1." + i, "LAX")), "Should connect to port " + i);
        }

        assertEquals(0, networkSwitch.getAvailablePorts(), "No ports should be available");

        for (int i = 1; i <= PORT_COUNT; i++) {
            assertTrue(networkSwitch.disconnectDevice(i), "Should disconnect port " + i);
        }

        assertEquals(PORT_COUNT, networkSwitch.getAvailablePorts(), "All ports should be available");
        assertTrue(networkSwitch.getOccupiedPorts().isEmpty(), "No ports should be occupied");
    }

    @Test
    void shouldMaintainPortOrder() {
        networkSwitch.connectDevice(5, device1);
        networkSwitch.connectDevice(2, device2);
        networkSwitch.connectDevice(8, device3);

        List<Integer> ports = networkSwitch.getOccupiedPorts();
        assertEquals(3, ports.size(), "Should have three ports");
        assertEquals(2, ports.get(0), "First port should be 2");
        assertEquals(5, ports.get(1), "Second port should be 5");
        assertEquals(8, ports.get(2), "Third port should be 8");

        networkSwitch.disconnectDevice(5);
        networkSwitch.connectDevice(3, device1);

        ports = networkSwitch.getOccupiedPorts();
        assertEquals(3, ports.size(), "Should maintain three ports");
        assertEquals(2, ports.get(0), "First port should be 2");
        assertEquals(3, ports.get(1), "Second port should be 3");
        assertEquals(8, ports.get(2), "Third port should be 8");
    }

    @Test
    void shouldHandleNullDevice() {
        assertFalse(networkSwitch.connectDevice(1, null), "Should reject null device");
        assertEquals(PORT_COUNT, networkSwitch.getAvailablePorts(), "Should not change available ports");
        assertTrue(networkSwitch.getOccupiedPorts().isEmpty(), "Should not occupy any ports");

        networkSwitch.connectDevice(1, device1);
        assertFalse(networkSwitch.connectDevice(2, null), "Should reject null device after valid connection");
        assertEquals(1, networkSwitch.getOccupiedPorts().size(), "Should maintain existing connection");
        assertEquals(device1, networkSwitch.getConnectedDevice(1), "Should preserve valid connection");
    }

    @Test
    void shouldInheritNetworkDeviceFunctionality() {
        assertTrue(networkSwitch.isOnline(), "Should start online");
        networkSwitch.setOnline(false);
        assertFalse(networkSwitch.isOnline(), "Should go offline");
        networkSwitch.setOnline(true);
        assertTrue(networkSwitch.isOnline(), "Should go back online");

        Switch sameSwitch = new Switch("ESW001", "10.0.2.1", "SFO", 48, 100000);
        Switch differentSwitch = new Switch("ESW002", "10.0.1.1", "LAX", PORT_COUNT, SWITCHING_SPEED);

        assertEquals(networkSwitch, sameSwitch, "Should equal switch with same ID");
        assertNotEquals(networkSwitch, differentSwitch, "Should not equal switch with different ID");
    }

    @Test
    void shouldMaintainPortOrdering() {
        networkSwitch.connectDevice(20, device1);
        networkSwitch.connectDevice(5, device2);
        networkSwitch.connectDevice(15, device3);

        List<Integer> ports = networkSwitch.getOccupiedPorts();
        assertEquals(3, ports.size());
        assertTrue(ports.get(0) < ports.get(1), "First port should be lowest");
        assertTrue(ports.get(1) < ports.get(2), "Last port should be highest");

        networkSwitch.disconnectDevice(15);
        networkSwitch.connectDevice(10, device3);
        ports = networkSwitch.getOccupiedPorts();
        assertTrue(ports.get(0) < ports.get(1), "Order should be maintained after reconnection");
        assertTrue(ports.get(1) < ports.get(2), "Order should be maintained after reconnection");
    }
}