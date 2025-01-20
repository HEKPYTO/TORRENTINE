package test.solution;

import base.Device;
import base.Hub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubSolutionTest {
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
    void shouldManageDeviceConnections() {
        assertTrue(hub.addDevice(device1));
        assertTrue(hub.addDevice(device2));
        assertTrue(hub.addDevice(device3));

        assertEquals(3, hub.getConnectedDeviceCount());
        assertEquals(PORT_COUNT - 3, hub.getAvailablePorts());
        assertTrue(hub.getConnectedDevices().containsAll(List.of(device1, device2, device3)));

        assertTrue(hub.removeDevice(device1));
        assertEquals(2, hub.getConnectedDeviceCount());
        assertEquals(PORT_COUNT - 2, hub.getAvailablePorts());
        assertFalse(hub.getConnectedDevices().contains(device1));
    }

    @Test
    void shouldHandleBroadcastScenarios() {
        hub.addDevice(device1);
        hub.addDevice(device2);
        hub.addDevice(device3);

        assertEquals(2, hub.broadcast(device1),
                "Should count other online devices");

        device2.setOnline(false);
        assertEquals(1, hub.broadcast(device1),
                "Should only count online devices");

        device1.setOnline(false);
        assertEquals(0, hub.broadcast(device1),
                "Should not broadcast from offline source");

        hub.setOnline(false);
        assertEquals(0, hub.broadcast(device3),
                "Should not broadcast when hub offline");
    }

    @Test
    void shouldManageBandwidthAllocation() {
        hub.addDevice(device1);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput(),
                "Single device should get full bandwidth");

        hub.addDevice(device2);
        assertEquals(BANDWIDTH / 2.0, hub.getCurrentThroughput(),
                "Two devices should share bandwidth equally");

        hub.addDevice(device3);
        assertEquals(BANDWIDTH / 3.0, hub.getCurrentThroughput(),
                "Three devices should share bandwidth equally");

        device2.setOnline(false);
        assertEquals(BANDWIDTH / 2.0, hub.getCurrentThroughput(),
                "Offline devices should be excluded from bandwidth calculation");

        device1.setOnline(false);
        device3.setOnline(false);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput(),
                "No online devices should result in full bandwidth");
    }

    @Test
    void shouldHandleNetworkChanges() {
        hub.addDevice(device1);
        hub.addDevice(device2);

        device1.setOnline(false);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput(),
                "Single online device should get full bandwidth");

        device2.setOnline(false);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput(),
                "All offline should show full bandwidth");

        assertTrue(hub.removeDevice(device1));
        assertTrue(hub.addDevice(device1));
        device1.setOnline(true);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput(),
                "Reconnected device should get full bandwidth");
    }
}