package test.solution;

import base.Hub;
import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubSolutionTest {
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
    void shouldManageDeviceConnections() {
        assertTrue(hub.connectDevice(device1));
        assertTrue(hub.connectDevice(device2));
        assertTrue(hub.connectDevice(device3));

        assertEquals(3, hub.getConnectedDeviceCount());
        assertEquals(PORT_COUNT - 3, hub.getAvailablePorts());
        assertTrue(hub.getConnectedDevices().containsAll(List.of(device1, device2, device3)));

        assertTrue(hub.disconnectDevice(device1));
        assertEquals(2, hub.getConnectedDeviceCount());
        assertEquals(PORT_COUNT - 2, hub.getAvailablePorts());
        assertFalse(hub.getConnectedDevices().contains(device1));
    }

    @Test
    void shouldHandleBroadcastScenarios() {
        hub.connectDevice(device1);
        hub.connectDevice(device2);
        hub.connectDevice(device3);

        assertEquals(2, hub.broadcast(device1, new byte[]{1, 2, 3}),
                "Should count other online devices");

        device2.setOnline(false);
        assertEquals(1, hub.broadcast(device1, new byte[]{1, 2, 3}),
                "Should only count online devices");

        device1.setOnline(false);
        assertEquals(0, hub.broadcast(device1, new byte[]{1, 2, 3}),
                "Should not broadcast from offline source");

        hub.setOnline(false);
        assertEquals(0, hub.broadcast(device3, new byte[]{1, 2, 3}),
                "Should not broadcast when hub offline");
    }

    @Test
    void shouldManageBandwidthAllocation() {
        hub.connectDevice(device1);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput(),
                "Single device should get full bandwidth");

        hub.connectDevice(device2);
        assertEquals(BANDWIDTH / 2.0, hub.getCurrentThroughput(),
                "Two devices should share bandwidth equally");

        hub.connectDevice(device3);
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
        hub.connectDevice(device1);
        hub.connectDevice(device2);

        device1.setOnline(false);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput(),
                "Single online device should get full bandwidth");

        device2.setOnline(false);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput(),
                "All offline should show full bandwidth");

        assertTrue(hub.disconnectDevice(device1));
        assertTrue(hub.connectDevice(device1));
        device1.setOnline(true);
        assertEquals(BANDWIDTH, hub.getCurrentThroughput(),
                "Reconnected device should get full bandwidth");
    }
}