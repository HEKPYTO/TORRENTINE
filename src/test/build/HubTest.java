package test.build;

import base.Device;
import base.Hub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubTest {
    private Hub hub;
    private Device device1;
    private Device device2;
    private Device device3;
    private static final int PORT_COUNT = 8;
    private static final int BANDWIDTH = 100;
    private static final boolean DUAL_SPEED = true;

    @BeforeEach
    void setUp() {
        hub = new Hub("HUB001", "192.168.1.1", "NYC", PORT_COUNT, BANDWIDTH, DUAL_SPEED);
        device1 = new Device("DEV001", "192.168.1.2", "NYC");
        device2 = new Device("DEV002", "192.168.1.3", "NYC");
        device3 = new Device("DEV003", "192.168.1.4", "NYC");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("HUB001", hub.getDeviceID());
        assertEquals("192.168.1.1", hub.getIpAddress());
        assertEquals("NYC", hub.getLocation());
        assertTrue(hub.isOnline());

        assertEquals(PORT_COUNT, hub.getPortCount());
        assertEquals(BANDWIDTH, hub.getBandwidth());
        assertTrue(hub.isDualSpeed());
        assertEquals(0, hub.getConnectedDeviceCount());
        assertEquals(PORT_COUNT, hub.getAvailablePorts());
        assertTrue(hub.getConnectedDevices().isEmpty());
    }

    @Test
    void shouldAddDeviceSuccessfully() {
        assertTrue(hub.addDevice(device1));
        assertEquals(1, hub.getConnectedDeviceCount());
        assertEquals(PORT_COUNT - 1, hub.getAvailablePorts());
        assertTrue(hub.getConnectedDevices().contains(device1));
    }

    @Test
    void shouldNotConnectNullDevice() {
        assertFalse(hub.addDevice(null));
        assertEquals(0, hub.getConnectedDeviceCount());
        assertEquals(PORT_COUNT, hub.getAvailablePorts());
        assertTrue(hub.getConnectedDevices().isEmpty());
    }

    @Test
    void shouldNotConnectDuplicateDevice() {
        assertTrue(hub.addDevice(device1));
        assertFalse(hub.addDevice(device1));
        assertEquals(1, hub.getConnectedDeviceCount());
        assertEquals(PORT_COUNT - 1, hub.getAvailablePorts());
    }

    @Test
    void shouldNotExceedPortCapacity() {
        for (int i = 0; i < PORT_COUNT; i++) {
            Device device = new Device("DEV" + String.format("%03d", i), "192.168.1." + (i + 10), "NYC");
            assertTrue(hub.addDevice(device));
        }

        assertFalse(hub.addDevice(new Device("DEV999", "192.168.1.100", "NYC")));
        assertEquals(PORT_COUNT, hub.getConnectedDeviceCount());
        assertEquals(0, hub.getAvailablePorts());
    }

    @Test
    void shouldRemoveDeviceSuccessfully() {
        hub.addDevice(device1);
        assertTrue(hub.removeDevice(device1));
        assertEquals(0, hub.getConnectedDeviceCount());
        assertEquals(PORT_COUNT, hub.getAvailablePorts());
        assertFalse(hub.getConnectedDevices().contains(device1));
    }

    @Test
    void shouldHandleNonexistentDeviceDisconnection() {
        assertFalse(hub.removeDevice(device1));
        hub.addDevice(device1);
        assertFalse(hub.removeDevice(device2));
        assertEquals(1, hub.getConnectedDeviceCount());
        assertTrue(hub.getConnectedDevices().contains(device1));
    }

    @Test
    void connectedDevicesListShouldBeDefensiveCopy() {
        hub.addDevice(device1);
        List<Device> devices = hub.getConnectedDevices();
        devices.add(device2);
        assertEquals(1, hub.getConnectedDeviceCount());
        assertFalse(hub.getConnectedDevices().contains(device2));
    }

    @Test
    void shouldCalculateCorrectBroadcastCount() {
        assertEquals(0, hub.broadcast(device1));

        hub.addDevice(device1);
        hub.addDevice(device2);
        hub.addDevice(device3);

        assertEquals(2, hub.broadcast(device1));

        device2.setOnline(false);
        assertEquals(1, hub.broadcast(device1));

        device1.setOnline(false);
        assertEquals(0, hub.broadcast(device1));

        device1.setOnline(true);
        hub.removeDevice(device1);
        assertEquals(0, hub.broadcast(device1));
    }

    @Test
    void shouldHandleInvalidBroadcastScenarios() {
        hub.setOnline(false);
        assertEquals(0, hub.broadcast(device1));

        hub.setOnline(true);
        assertEquals(0, hub.broadcast(device1));

        assertEquals(0, hub.broadcast(null));
    }

    @ParameterizedTest
    @CsvSource({"1, 100.0", "2, 50.0", "4, 25.0"})
    void shouldCalculateCorrectThroughput(int deviceCount, double expectedThroughput) {
        for (int i = 0; i < deviceCount; i++) {
            Device device = new Device("DEV" + String.format("%03d", i), "192.168.1." + (i + 10), "NYC");
            hub.addDevice(device);
        }
        assertEquals(expectedThroughput, hub.getCurrentThroughput());
    }

    @Test
    void shouldCalculateThroughputWithOfflineDevices() {
        hub.addDevice(device1);
        hub.addDevice(device2);
        hub.addDevice(device3);

        assertEquals(BANDWIDTH / 3.0, hub.getCurrentThroughput());

        device2.setOnline(false);
        assertEquals(BANDWIDTH / 2.0, hub.getCurrentThroughput());

        device1.setOnline(false);
        device3.setOnline(false);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput());
    }

    @Test
    void shouldHandleDeviceStateChanges() {
        hub.addDevice(device1);
        hub.addDevice(device2);

        device1.setOnline(false);
        assertEquals(2, hub.getConnectedDeviceCount());
        assertTrue(hub.getConnectedDevices().contains(device1));

        assertTrue(hub.removeDevice(device1));
        assertEquals(1, hub.getConnectedDeviceCount());
    }
}