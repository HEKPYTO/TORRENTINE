package test.solution;

import base.Device;
import base.Switch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwitchSolutionTest {
    private Switch networkSwitch;
    private Device device1;
    private Device device2;
    private Device device3;
    private static final int PORT_COUNT = 24;
    private static final int SWITCHING_SPEED = 1000;

    @BeforeEach
    void setUp() {
        networkSwitch = new Switch("CSW001", "172.16.1.1", "LAX", PORT_COUNT, SWITCHING_SPEED);
        device1 = new Device("NODE001", "172.16.1.10", "LAX");
        device2 = new Device("NODE002", "172.16.1.11", "LAX");
        device3 = new Device("NODE003", "172.16.1.12", "LAX");
    }

    @Test
    void shouldHandleSequentialOperations() {
        assertTrue(networkSwitch.addDevice(1, device1));
        assertEquals(PORT_COUNT - 1, networkSwitch.getAvailablePorts());
        assertEquals(1, networkSwitch.getOccupiedPorts().size());

        assertTrue(networkSwitch.addDevice(2, device2));
        assertEquals(PORT_COUNT - 2, networkSwitch.getAvailablePorts());
        assertEquals(2, networkSwitch.getOccupiedPorts().size());

        assertEquals(device1, networkSwitch.getConnectedDevice(1));
        assertEquals(device2, networkSwitch.getConnectedDevice(2));

        assertTrue(networkSwitch.removeDevice(1));
        assertEquals(PORT_COUNT - 1, networkSwitch.getAvailablePorts());
        assertNull(networkSwitch.getConnectedDevice(1));

        assertTrue(networkSwitch.removeDevice(2));
        assertEquals(PORT_COUNT, networkSwitch.getAvailablePorts());
        assertTrue(networkSwitch.getOccupiedPorts().isEmpty());
    }

    @Test
    void shouldHandlePortBoundaries() {
        assertTrue(networkSwitch.addDevice(1, device1));
        assertEquals(device1, networkSwitch.getConnectedDevice(1));

        assertTrue(networkSwitch.addDevice(PORT_COUNT, device2));
        assertEquals(device2, networkSwitch.getConnectedDevice(PORT_COUNT));

        assertTrue(networkSwitch.addDevice(PORT_COUNT / 2, device3));
        assertEquals(device3, networkSwitch.getConnectedDevice(PORT_COUNT / 2));

        assertEquals(3, networkSwitch.getOccupiedPorts().size());
        assertEquals(PORT_COUNT - 3, networkSwitch.getAvailablePorts());
    }

    @Test
    void shouldHandleReconnectionScenarios() {
        assertTrue(networkSwitch.addDevice(5, device1));
        assertTrue(networkSwitch.addDevice(10, device2));

        assertTrue(networkSwitch.removeDevice(5));
        assertTrue(networkSwitch.addDevice(5, device3));
        assertEquals(device3, networkSwitch.getConnectedDevice(5));

        assertTrue(networkSwitch.removeDevice(10));
        assertTrue(networkSwitch.addDevice(15, device2));
        assertEquals(device2, networkSwitch.getConnectedDevice(15));

        assertEquals(2, networkSwitch.getOccupiedPorts().size());
        assertEquals(PORT_COUNT - 2, networkSwitch.getAvailablePorts());
    }

    @Test
    void shouldHandleConnectionFailures() {
        networkSwitch.addDevice(1, device1);
        networkSwitch.addDevice(2, device2);

        assertFalse(networkSwitch.addDevice(0, device3), "Should reject port 0");
        assertFalse(networkSwitch.addDevice(PORT_COUNT + 1, device3), "Should reject port > max");
        assertFalse(networkSwitch.addDevice(-1, device3), "Should reject negative port");
        assertFalse(networkSwitch.addDevice(1, device3), "Should reject occupied port");
        assertFalse(networkSwitch.addDevice(3, null), "Should reject null device");

        assertEquals(2, networkSwitch.getOccupiedPorts().size());
        assertEquals(PORT_COUNT - 2, networkSwitch.getAvailablePorts());
    }

    @Test
    void shouldHandleDisconnectionFailures() {
        networkSwitch.addDevice(1, device1);

        assertFalse(networkSwitch.removeDevice(0), "Should reject port 0");
        assertFalse(networkSwitch.removeDevice(PORT_COUNT + 1), "Should reject port > max");
        assertFalse(networkSwitch.removeDevice(-1), "Should reject negative port");
        assertFalse(networkSwitch.removeDevice(2), "Should reject empty port");

        assertEquals(1, networkSwitch.getOccupiedPorts().size());
        assertEquals(device1, networkSwitch.getConnectedDevice(1));
    }

    @Test
    void shouldMaintainPortOrder() {
        networkSwitch.addDevice(20, device1);
        networkSwitch.addDevice(5, device2);
        networkSwitch.addDevice(15, device3);

        List<Integer> ports = networkSwitch.getOccupiedPorts();
        assertEquals(3, ports.size());
        assertTrue(ports.get(0) < ports.get(1), "First port should be lowest");
        assertTrue(ports.get(1) < ports.get(2), "Last port should be highest");

        networkSwitch.removeDevice(15);
        networkSwitch.addDevice(10, device3);
        ports = networkSwitch.getOccupiedPorts();
        assertTrue(ports.get(0) < ports.get(1), "Order should be maintained after reconnection");
        assertTrue(ports.get(1) < ports.get(2), "Order should be maintained after reconnection");
    }

    @Test
    void shouldHandleDeviceStates() {
        networkSwitch.addDevice(1, device1);
        networkSwitch.addDevice(2, device2);

        device1.setOnline(false);
        device2.setOnline(false);

        assertEquals(device1, networkSwitch.getConnectedDevice(1), "Should keep offline device");
        assertEquals(device2, networkSwitch.getConnectedDevice(2), "Should keep offline device");
        assertEquals(2, networkSwitch.getOccupiedPorts().size(), "Should maintain port count");

        assertTrue(networkSwitch.removeDevice(1), "Should disconnect offline device");
        assertTrue(networkSwitch.addDevice(3, device1), "Should connect offline device");
    }

    @ParameterizedTest
    @CsvSource({"1, 2, 3", "5, 10, 15", "1, 12, 24", "24, 12, 1"})
    void shouldHandleVariousPortCombinations(int port1, int port2, int port3) {
        assertTrue(networkSwitch.addDevice(port1, device1));
        assertTrue(networkSwitch.addDevice(port2, device2));
        assertTrue(networkSwitch.addDevice(port3, device3));

        assertEquals(3, networkSwitch.getOccupiedPorts().size());
        assertEquals(PORT_COUNT - 3, networkSwitch.getAvailablePorts());

        List<Integer> ports = networkSwitch.getOccupiedPorts();
        assertTrue(ports.contains(port1));
        assertTrue(ports.contains(port2));
        assertTrue(ports.contains(port3));
    }
}