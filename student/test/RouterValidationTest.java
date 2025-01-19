package test.validate;

import base.Router;
import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

class RouterValidationTest {
    private Router router;
    private NetworkDevice device1;
    private NetworkDevice device2;

    @BeforeEach
    void setUp() {
        router = new Router("RTR100", "172.16.1.1", "SFO", 2000);
        device1 = new NetworkDevice("NET001", "172.16.1.10", "SFO");
        device2 = new NetworkDevice("NET002", "172.16.1.20", "SFO");
    }

    @Test
    void shouldInitializeCorrectly() {
        assertEquals("RTR100", router.getDeviceID(), "Router ID should match constructor input");
        assertEquals("172.16.1.1", router.getIpAddress(), "IP should match constructor input");
        assertEquals("SFO", router.getLocation(), "Location should match constructor input");
        assertTrue(router.isOnline(), "Router should be initially online");

        assertEquals(2000, router.getBandwidth(), "Bandwidth should match constructor input");
        assertTrue(router.getRoutingTable().isEmpty(), "Initial routing table should be empty");
        assertTrue(router.getConnectedDevices().isEmpty(), "Initial connected devices list should be empty");

        Router router2 = new Router("RTR200", "172.16.2.1", "LAX", 3000);
        assertEquals("RTR200", router2.getDeviceID());
        assertEquals("172.16.2.1", router2.getIpAddress());
        assertEquals("LAX", router2.getLocation());
        assertEquals(3000, router2.getBandwidth());
    }

    @Test
    void shouldManageBandwidthCorrectly() {
        assertEquals(2000, router.getBandwidth(), "Initial bandwidth should match constructor value");

        router.setBandwidth(1000);
        assertEquals(1000, router.getBandwidth(), "Should accept lower positive bandwidth");

        router.setBandwidth(5000);
        assertEquals(5000, router.getBandwidth(), "Should accept higher positive bandwidth");

        router.setBandwidth(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, router.getBandwidth(), "Should accept maximum integer value");

        router.setBandwidth(0);
        assertEquals(0, router.getBandwidth(), "Should accept zero bandwidth");

        router.setBandwidth(-1);
        assertEquals(0, router.getBandwidth(), "Should convert -1 to zero");

        router.setBandwidth(-1000);
        assertEquals(0, router.getBandwidth(), "Should convert large negative value to zero");

        router.setBandwidth(Integer.MIN_VALUE);
        assertEquals(0, router.getBandwidth(), "Should convert minimum integer value to zero");

        router.setBandwidth(2000);
        assertEquals(2000, router.getBandwidth(), "Should restore to positive value correctly");
    }

    @Test
    void shouldManageRoutingTableCorrectly() {
        router.addDevice(device1);
        Map<String, String> routingTable = router.getRoutingTable();
        assertEquals(1, routingTable.size(), "Table should have one entry");
        assertEquals("NET001", routingTable.get("172.16.1.10"), "Should map IP to correct device ID");

        router.addDevice(device2);
        assertEquals(2, routingTable.size(), "Table should have two entries");
        assertEquals("NET002", routingTable.get("172.16.1.20"), "Should map second IP to correct device ID");

        NetworkDevice device3 = new NetworkDevice("NET003", "172.16.1.10", "SFO");
        router.addDevice(device3);
        assertEquals(2, routingTable.size(), "Table size should remain same with IP conflict");
        assertEquals("NET003", routingTable.get("172.16.1.10"), "Should update mapping for conflicting IP");

        NetworkDevice device4 = new NetworkDevice("NET004", "172.16.1.10", "SFO");
        router.addDevice(device4);
        assertEquals(2, routingTable.size(), "Table size should remain stable with multiple conflicts");
        assertEquals("NET004", routingTable.get("172.16.1.10"), "Should update mapping for multiple conflicts");
    }

    @Test
    void shouldManageConnectedDevicesCorrectly() {
        List<NetworkDevice> devices = router.getConnectedDevices();
        assertTrue(devices.isEmpty(), "Initial device list should be empty");

        router.addDevice(device1);
        devices = router.getConnectedDevices();
        assertEquals(1, devices.size(), "Should have one device");
        assertTrue(devices.contains(device1), "Should contain first device");

        router.addDevice(device2);
        devices = router.getConnectedDevices();
        assertEquals(2, devices.size(), "Should have two devices");
        assertTrue(devices.contains(device2), "Should contain second device");

        router.addDevice(device1);
        router.addDevice(device2);
        devices = router.getConnectedDevices();
        assertEquals(4, devices.size(), "Should allow duplicate devices");

        assertEquals(device1, devices.get(0), "First addition should be first");
        assertEquals(device2, devices.get(1), "Second addition should be second");
        assertEquals(device1, devices.get(2), "Third addition should be first device again");
        assertEquals(device2, devices.get(3), "Fourth addition should be second device again");
    }

    @Test
    void shouldInheritNetworkDeviceFunctionality() {
        assertTrue(router.isOnline(), "Should be initially online");
        router.setOnline(false);
        assertFalse(router.isOnline(), "Should be offline after setting");
        router.setOnline(true);
        assertTrue(router.isOnline(), "Should be online after resetting");

        Router sameIdRouter = new Router("RTR100", "172.16.2.1", "LAX", 3000);
        Router differentIdRouter = new Router("RTR200", "172.16.1.1", "SFO", 2000);

        assertEquals(router, sameIdRouter, "Routers with same ID should be equal");
        assertEquals(sameIdRouter, router, "Equality should be symmetric");

        assertNotEquals(router, differentIdRouter, "Routers with different IDs should not be equal");
        assertNotEquals(differentIdRouter, router, "Inequality should be symmetric");
    }
}