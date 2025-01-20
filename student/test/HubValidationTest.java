package test.validate;

import base.Device;
import base.Hub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubValidationTest {
    private Hub hub;
    private Device device1;
    private Device device2;
    private Device device3;
    private static final int PORT_COUNT = 16;
    private static final int BANDWIDTH = 1000;
    private static final boolean DUAL_SPEED = true;

    @BeforeEach
    void setUp() {
        hub = new Hub("HUB100", "10.0.1.1", "SFO", PORT_COUNT, BANDWIDTH, DUAL_SPEED);
        device1 = new Device("HOST001", "10.0.1.10", "SFO");
        device2 = new Device("HOST002", "10.0.1.11", "SFO");
        device3 = new Device("HOST003", "10.0.1.12", "SFO");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("HUB100", hub.getDeviceID(), "Device ID should match");
        assertEquals("10.0.1.1", hub.getIpAddress(), "IP should match");
        assertEquals("SFO", hub.getLocation(), "Location should match");
        assertTrue(hub.isOnline(), "Should be initially online");

        assertEquals(PORT_COUNT, hub.getPortCount(), "Port count should match");
        assertEquals(BANDWIDTH, hub.getBandwidth(), "Bandwidth should match");
        assertTrue(hub.isDualSpeed(), "Should be dual speed");
        assertEquals(0, hub.getConnectedDeviceCount(), "Should have no connected devices");
        assertEquals(PORT_COUNT, hub.getAvailablePorts(), "All ports should be available");
        assertTrue(hub.getConnectedDevices().isEmpty(), "Connected devices list should be empty");

        Hub hub2 = new Hub("HUB200", "10.0.2.1", "LAX", 24, 100, false);
        assertEquals(24, hub2.getPortCount(), "Different port count should match");
        assertEquals(100, hub2.getBandwidth(), "Different bandwidth should match");
        assertFalse(hub2.isDualSpeed(), "Should not be dual speed");
    }

    @Test
    void shouldValidateDeviceConnections() {
        assertFalse(hub.addDevice(null), "Should reject null device");
        assertEquals(0, hub.getConnectedDeviceCount(), "Should not increase count for null");
        assertEquals(PORT_COUNT, hub.getAvailablePorts(), "Available ports should not change");
        assertTrue(hub.getConnectedDevices().isEmpty(), "Should remain empty after null");

        assertTrue(hub.addDevice(device1), "Should accept first connection");
        assertFalse(hub.addDevice(device1), "Should reject duplicate device");
        assertEquals(1, hub.getConnectedDeviceCount(), "Count should not increase for duplicate");
        assertEquals(PORT_COUNT - 1, hub.getAvailablePorts(), "Ports should reflect single connection");

        for (int i = 0; i < PORT_COUNT; i++) {
            Device device = new Device(String.format("HOST%03d", i + 10), String.format("10.0.1.%d", i + 20), "SFO");
            if (i < PORT_COUNT - 1) {
                assertTrue(hub.addDevice(device), "Should accept device within capacity");
            } else {
                assertFalse(hub.addDevice(device), "Should reject device beyond capacity");
            }
        }
        assertEquals(PORT_COUNT, hub.getConnectedDeviceCount(), "Should reach max capacity");
        assertEquals(0, hub.getAvailablePorts(), "Should have no available ports");
    }

    @Test
    void shouldValidateDeviceDisconnection() {
        assertFalse(hub.removeDevice(device1), "Should reject non-existent device");
        assertEquals(0, hub.getConnectedDeviceCount(), "Count should not change");

        hub.addDevice(device1);
        assertTrue(hub.removeDevice(device1), "Should disconnect existing device");
        assertEquals(0, hub.getConnectedDeviceCount(), "Count should decrease");
        assertEquals(PORT_COUNT, hub.getAvailablePorts(), "All ports should be available");

        hub.addDevice(device1);
        hub.addDevice(device2);
        assertTrue(hub.removeDevice(device1), "Should disconnect first device");
        assertTrue(hub.removeDevice(device2), "Should disconnect second device");
        assertEquals(0, hub.getConnectedDeviceCount(), "Should have no devices");

        assertFalse(hub.removeDevice(device1), "Should reject already disconnected device");
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

    @Test
    void shouldValidateBroadcastScenarios() {
        assertEquals(0, hub.broadcast(device1), "Should reject unconnected source");
        assertEquals(0, hub.broadcast(null), "Should reject null source");

        hub.addDevice(device1);
        assertEquals(0, hub.broadcast(device1), "Single device should have no recipients");

        hub.addDevice(device2);
        device2.setOnline(false);
        assertEquals(0, hub.broadcast(device1), "Should count only online recipients");

        device2.setOnline(true);
        hub.addDevice(device3);
        assertEquals(2, hub.broadcast(device1), "Should count all online recipients");

        hub.setOnline(false);
        assertEquals(0, hub.broadcast(device1), "Should reject when hub offline");
    }

    @ParameterizedTest
    @CsvSource({"1, 1000.0", "2, 500.0", "4, 250.0", "8, 125.0", "16, 62.5"})
    void shouldValidateThroughputCalculations(int deviceCount, double expectedThroughput) {

        for (int i = 0; i < deviceCount; i++) {
            Device device = new Device(String.format("HOST%03d", i + 10), String.format("10.0.1.%d", i + 20), "SFO");
            hub.addDevice(device);
        }
        assertEquals(expectedThroughput, hub.getCurrentThroughput(), 0.1, "Should calculate correct throughput for " + deviceCount + " devices");

        if (deviceCount > 1) {
            int halfDevices = deviceCount / 2;
            List<Device> devices = hub.getConnectedDevices();
            for (int i = 0; i < halfDevices; i++) {
                devices.get(i).setOnline(false);
            }
            assertEquals(expectedThroughput * 2, hub.getCurrentThroughput(), 0.1, "Should recalculate throughput with offline devices");
        }
    }
}