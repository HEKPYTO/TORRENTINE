package test.solution;

import base.Switch;
import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwitchSolutionTest {
    private Switch networkSwitch;
    private NetworkDevice device1;
    private NetworkDevice device2;
    private NetworkDevice device3;
    private static final int PORT_COUNT = 24;
    private static final int SWITCHING_SPEED = 1000;

    @BeforeEach
    void setUp() {
        networkSwitch = new Switch("CSW001", "172.16.1.1", "LAX", PORT_COUNT, SWITCHING_SPEED);
        device1 = new NetworkDevice("NODE001", "172.16.1.10", "LAX");
        device2 = new NetworkDevice("NODE002", "172.16.1.11", "LAX");
        device3 = new NetworkDevice("NODE003", "172.16.1.12", "LAX");
    }

    @Test
    void shouldHandleSequentialOperations() {
        assertTrue(networkSwitch.connectDevice(1, device1));
        assertEquals(PORT_COUNT - 1, networkSwitch.getAvailablePorts());
        assertEquals(1, networkSwitch.getOccupiedPorts().size());

        assertTrue(networkSwitch.connectDevice(2, device2));
        assertEquals(PORT_COUNT - 2, networkSwitch.getAvailablePorts());
        assertEquals(2, networkSwitch.getOccupiedPorts().size());

        assertEquals(device1, networkSwitch.getConnectedDevice(1));
        assertEquals(device2, networkSwitch.getConnectedDevice(2));

        assertTrue(networkSwitch.disconnectDevice(1));
        assertEquals(PORT_COUNT - 1, networkSwitch.getAvailablePorts());
        assertNull(networkSwitch.getConnectedDevice(1));

        assertTrue(networkSwitch.disconnectDevice(2));
        assertEquals(PORT_COUNT, networkSwitch.getAvailablePorts());
        assertTrue(networkSwitch.getOccupiedPorts().isEmpty());
    }

    @Test
    void shouldHandlePortBoundaries() {
        assertTrue(networkSwitch.connectDevice(1, device1));
        assertEquals(device1, networkSwitch.getConnectedDevice(1));

        assertTrue(networkSwitch.connectDevice(PORT_COUNT, device2));
        assertEquals(device2, networkSwitch.getConnectedDevice(PORT_COUNT));

        assertTrue(networkSwitch.connectDevice(PORT_COUNT / 2, device3));
        assertEquals(device3, networkSwitch.getConnectedDevice(PORT_COUNT / 2));

        assertEquals(3, networkSwitch.getOccupiedPorts().size());
        assertEquals(PORT_COUNT - 3, networkSwitch.getAvailablePorts());
    }

    @Test
    void shouldHandleReconnectionScenarios() {
        assertTrue(networkSwitch.connectDevice(5, device1));
        assertTrue(networkSwitch.connectDevice(10, device2));

        assertTrue(networkSwitch.disconnectDevice(5));
        assertTrue(networkSwitch.connectDevice(5, device3));
        assertEquals(device3, networkSwitch.getConnectedDevice(5));

        assertTrue(networkSwitch.disconnectDevice(10));
        assertTrue(networkSwitch.connectDevice(15, device2));
        assertEquals(device2, networkSwitch.getConnectedDevice(15));

        assertEquals(2, networkSwitch.getOccupiedPorts().size());
        assertEquals(PORT_COUNT - 2, networkSwitch.getAvailablePorts());
    }

    @Test
    void shouldHandleConnectionFailures() {
        networkSwitch.connectDevice(1, device1);
        networkSwitch.connectDevice(2, device2);

        assertFalse(networkSwitch.connectDevice(0, device3), "Should reject port 0");
        assertFalse(networkSwitch.connectDevice(PORT_COUNT + 1, device3), "Should reject port > max");
        assertFalse(networkSwitch.connectDevice(-1, device3), "Should reject negative port");
        assertFalse(networkSwitch.connectDevice(1, device3), "Should reject occupied port");
        assertFalse(networkSwitch.connectDevice(3, null), "Should reject null device");

        assertEquals(2, networkSwitch.getOccupiedPorts().size());
        assertEquals(PORT_COUNT - 2, networkSwitch.getAvailablePorts());
    }

    @Test
    void shouldHandleDisconnectionFailures() {
        networkSwitch.connectDevice(1, device1);

        assertFalse(networkSwitch.disconnectDevice(0), "Should reject port 0");
        assertFalse(networkSwitch.disconnectDevice(PORT_COUNT + 1), "Should reject port > max");
        assertFalse(networkSwitch.disconnectDevice(-1), "Should reject negative port");
        assertFalse(networkSwitch.disconnectDevice(2), "Should reject empty port");

        assertEquals(1, networkSwitch.getOccupiedPorts().size());
        assertEquals(device1, networkSwitch.getConnectedDevice(1));
    }

    @Test
    void shouldMaintainPortOrder() {
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

    @Test
    void shouldHandleDeviceStates() {
        networkSwitch.connectDevice(1, device1);
        networkSwitch.connectDevice(2, device2);

        device1.setOnline(false);
        device2.setOnline(false);

        assertEquals(device1, networkSwitch.getConnectedDevice(1), "Should keep offline device");
        assertEquals(device2, networkSwitch.getConnectedDevice(2), "Should keep offline device");
        assertEquals(2, networkSwitch.getOccupiedPorts().size(), "Should maintain port count");

        assertTrue(networkSwitch.disconnectDevice(1), "Should disconnect offline device");
        assertTrue(networkSwitch.connectDevice(3, device1), "Should connect offline device");
    }

    @ParameterizedTest
    @CsvSource({"1, 2, 3", "5, 10, 15", "1, 12, 24", "24, 12, 1"})
    void shouldHandleVariousPortCombinations(int port1, int port2, int port3) {
        assertTrue(networkSwitch.connectDevice(port1, device1));
        assertTrue(networkSwitch.connectDevice(port2, device2));
        assertTrue(networkSwitch.connectDevice(port3, device3));

        assertEquals(3, networkSwitch.getOccupiedPorts().size());
        assertEquals(PORT_COUNT - 3, networkSwitch.getAvailablePorts());

        List<Integer> ports = networkSwitch.getOccupiedPorts();
        assertTrue(ports.contains(port1));
        assertTrue(ports.contains(port2));
        assertTrue(ports.contains(port3));
    }
}