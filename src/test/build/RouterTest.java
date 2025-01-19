package test.build;

import base.Router;
import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RouterTest {
    private Router router;
    private NetworkDevice device1;
    private NetworkDevice device2;

    @BeforeEach
    void setUp() {
        router = new Router("RTR001", "192.168.1.1", "NYC", 1000);
        device1 = new NetworkDevice("DEV001", "192.168.1.2", "NYC");
        device2 = new NetworkDevice("DEV002", "192.168.1.3", "LAX");
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

        List<NetworkDevice> devices = router.getConnectedDevices();
        assertEquals(3, devices.size());
        assertTrue(devices.contains(device1));
        assertTrue(devices.contains(device2));
    }

    @Test
    void getBandwidthShouldReturnInitializedValue() {
        assertEquals(1000, router.getBandwidth());
    }
}