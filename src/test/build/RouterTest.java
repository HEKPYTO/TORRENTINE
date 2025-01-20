package test.build;

import base.Device;
import base.Router;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RouterTest {
    private Router router;
    private Device device1;
    private Device device2;

    @BeforeEach
    void setUp() {
        router = new Router("RTR001", "192.168.1.1", "NYC", 1000);
        device1 = new Device("DEV001", "192.168.1.2", "NYC");
        device2 = new Device("DEV002", "192.168.1.3", "LAX");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        // Test inherited public methods
        assertEquals("RTR001", router.getDeviceID());
        assertEquals("192.168.1.1", router.getIpAddress());
        assertEquals("NYC", router.getLocation());
        assertTrue(router.isOnline());

        // Test Router's public methods
        assertEquals(1000, router.getBandwidth());
        assertTrue(router.getRoutingTable().isEmpty());
        assertTrue(router.getConnectedDevices().isEmpty());
    }

    @Test
    void addDeviceShouldRegisterNewDevice() {
        router.addDevice(device1);

        assertEquals(1, router.getConnectedDevices().size());
        assertTrue(router.getConnectedDevices().contains(device1));
        assertEquals("DEV001", router.getRoutingTable().get("192.168.1.2"));
    }

    @Test
    void removeDeviceShouldUpdateConnections() {
        router.addDevice(device1);
        router.addDevice(device2);

        assertTrue(router.removeDevice(device1), "Should successfully remove existing device");
        assertEquals(1, router.getConnectedDevices().size(), "Should have one remaining device");
        assertFalse(router.getRoutingTable().containsKey("192.168.1.2"), "Should remove from routing table");
        assertTrue(router.getConnectedDevices().contains(device2), "Should maintain unremoved device");
    }

    @Test
    void removeDeviceShouldHandleEdgeCases() {
        router.addDevice(device1);
        router.addDevice(device2);
        router.addDevice(device1); // Add duplicate device

        assertTrue(router.removeDevice(device1), "Should remove first occurrence of device");
        assertEquals(2, router.getConnectedDevices().size(), "Should have two devices remaining");
        assertFalse(router.getRoutingTable().containsKey("192.168.1.2"), "Should remove routing entry even with duplicate device");

        assertTrue(router.removeDevice(device1), "Should remove duplicate device");
        assertEquals(1, router.getConnectedDevices().size(), "Should have one device remaining");
        assertFalse(router.getRoutingTable().containsKey("192.168.1.2"), "Routing entry should remain removed");
        assertTrue(router.getRoutingTable().containsKey("192.168.1.3"), "Other device's routing entry should remain");

        Device nonExistentDevice = new Device("DEV999", "192.168.1.100", "NYC");
        assertFalse(router.removeDevice(nonExistentDevice), "Should return false for non-existent device");
    }

    @Test
    void getRoutingTableShouldReturnCurrentRoutes() {
        router.addDevice(device1);
        router.addDevice(device2);

        Map<String, String> routingTable = router.getRoutingTable();
        assertEquals(2, routingTable.size());
        assertEquals("DEV001", routingTable.get("192.168.1.2"));
        assertEquals("DEV002", routingTable.get("192.168.1.3"));
    }

    @Test
    void getConnectedDevicesShouldReturnAllDevices() {
        router.addDevice(device1);
        router.addDevice(device2);
        router.addDevice(device1); // Adding duplicate

        List<Device> devices = router.getConnectedDevices();
        assertEquals(3, devices.size());
        assertTrue(devices.contains(device1));
        assertTrue(devices.contains(device2));
    }

    @Test
    void getBandwidthShouldReturnInitializedValue() {
        assertEquals(1000, router.getBandwidth());
    }

    @Test
    void shouldHandleNullDevice() {
        router.addDevice(null);
        assertEquals(0, router.getConnectedDevices().size(), "Should not add null device");
        assertTrue(router.getRoutingTable().isEmpty(), "Routing table should remain empty");
    }
}