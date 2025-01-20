package test.validate;

import base.Hub;
import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubValidationTest {
    private Hub hub;
    private NetworkDevice device1;
    private NetworkDevice device2;
    private NetworkDevice device3;
    private static final int PORT_COUNT = 16;
    private static final int BANDWIDTH = 1000;
    private static final boolean DUAL_SPEED = true;

    @BeforeEach
    void setUp() {
        hub = new Hub("HUB100", "10.0.1.1", "SFO", PORT_COUNT, BANDWIDTH, DUAL_SPEED);
        device1 = new NetworkDevice("HOST001", "10.0.1.10", "SFO");
        device2 = new NetworkDevice("HOST002", "10.0.1.11", "SFO");
        device3 = new NetworkDevice("HOST003", "10.0.1.12", "SFO");
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
        assertFalse(hub.connectDevice(null), "Should reject null device");
        assertEquals(0, hub.getConnectedDeviceCount(), "Should not increase count for null");
        assertEquals(PORT_COUNT, hub.getAvailablePorts(), "Available ports should not change");
        assertTrue(hub.getConnectedDevices().isEmpty(), "Should remain empty after null");

        assertTrue(hub.connectDevice(device1), "Should accept first connection");
        assertFalse(hub.connectDevice(device1), "Should reject duplicate device");
        assertEquals(1, hub.getConnectedDeviceCount(), "Count should not increase for duplicate");
        assertEquals(PORT_COUNT - 1, hub.getAvailablePorts(), "Ports should reflect single connection");

        for (int i = 0; i < PORT_COUNT; i++) {
            NetworkDevice device = new NetworkDevice(String.format("HOST%03d", i + 10), String.format("10.0.1.%d", i + 20), "SFO");
            if (i < PORT_COUNT - 1) {
                assertTrue(hub.connectDevice(device), "Should accept device within capacity");
            } else {
                assertFalse(hub.connectDevice(device), "Should reject device beyond capacity");
            }
        }
        assertEquals(PORT_COUNT, hub.getConnectedDeviceCount(), "Should reach max capacity");
        assertEquals(0, hub.getAvailablePorts(), "Should have no available ports");
    }

    @Test
    void shouldValidateDeviceDisconnection() {
        assertFalse(hub.disconnectDevice(device1), "Should reject non-existent device");
        assertEquals(0, hub.getConnectedDeviceCount(), "Count should not change");

        hub.connectDevice(device1);
        assertTrue(hub.disconnectDevice(device1), "Should disconnect existing device");
        assertEquals(0, hub.getConnectedDeviceCount(), "Count should decrease");
        assertEquals(PORT_COUNT, hub.getAvailablePorts(), "All ports should be available");

        hub.connectDevice(device1);
        hub.connectDevice(device2);
        assertTrue(hub.disconnectDevice(device1), "Should disconnect first device");
        assertTrue(hub.disconnectDevice(device2), "Should disconnect second device");
        assertEquals(0, hub.getConnectedDeviceCount(), "Should have no devices");

        assertFalse(hub.disconnectDevice(device1), "Should reject already disconnected device");
    }

    @Test
    void shouldCalculateCorrectBroadcastCount() {
        assertEquals(0, hub.broadcast(device1, new byte[]{1, 2, 3}));

        hub.connectDevice(device1);
        hub.connectDevice(device2);
        hub.connectDevice(device3);

        assertEquals(2, hub.broadcast(device1, new byte[]{1, 2, 3}));

        device2.setOnline(false);
        assertEquals(1, hub.broadcast(device1, new byte[]{1, 2, 3}));

        device1.setOnline(false);
        assertEquals(0, hub.broadcast(device1, new byte[]{1, 2, 3}));

        device1.setOnline(true);
        hub.disconnectDevice(device1);
        assertEquals(0, hub.broadcast(device1, new byte[]{1, 2, 3}));
    }

    @Test
    void shouldHandleInvalidBroadcastScenarios() {
        hub.setOnline(false);
        assertEquals(0, hub.broadcast(device1, new byte[]{1, 2, 3}));

        hub.setOnline(true);
        assertEquals(0, hub.broadcast(device1, new byte[]{1, 2, 3}));


        assertEquals(0, hub.broadcast(null, new byte[]{1, 2, 3}));
    }

    @Test
    void shouldValidateBroadcastScenarios() {
        assertEquals(0, hub.broadcast(device1, new byte[]{1}), "Should reject unconnected source");
        assertEquals(0, hub.broadcast(null, new byte[]{1}), "Should reject null source");

        hub.connectDevice(device1);
        assertEquals(0, hub.broadcast(device1, new byte[]{1}), "Single device should have no recipients");

        hub.connectDevice(device2);
        device2.setOnline(false);
        assertEquals(0, hub.broadcast(device1, new byte[]{1}), "Should count only online recipients");

        device2.setOnline(true);
        hub.connectDevice(device3);
        assertEquals(2, hub.broadcast(device1, new byte[]{1}), "Should count all online recipients");

        hub.setOnline(false);
        assertEquals(0, hub.broadcast(device1, new byte[]{1}), "Should reject when hub offline");
    }

    @ParameterizedTest
    @CsvSource({"1, 1000.0", "2, 500.0", "4, 250.0", "8, 125.0", "16, 62.5"})
    void shouldValidateThroughputCalculations(int deviceCount, double expectedThroughput) {

        for (int i = 0; i < deviceCount; i++) {
            NetworkDevice device = new NetworkDevice(String.format("HOST%03d", i + 10), String.format("10.0.1.%d", i + 20), "SFO");
            hub.connectDevice(device);
        }
        assertEquals(expectedThroughput, hub.getCurrentThroughput(), 0.1, "Should calculate correct throughput for " + deviceCount + " devices");

        if (deviceCount > 1) {
            int halfDevices = deviceCount / 2;
            List<NetworkDevice> devices = hub.getConnectedDevices();
            for (int i = 0; i < halfDevices; i++) {
                devices.get(i).setOnline(false);
            }
            assertEquals(expectedThroughput * 2, hub.getCurrentThroughput(), 0.1, "Should recalculate throughput with offline devices");
        }
    }
}