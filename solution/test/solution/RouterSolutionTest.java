package test.solution;

import base.Device;
import base.Router;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;


class RouterSolutionTest {
    private Router router;
    private Device device1;
    private Device device2;

    @BeforeEach
    void setUp() {
        router = new Router("RTR001", "192.168.1.1", "NYC", 1000);
        device1 = new Device("DEV001", "192.168.1.2", "NYC");
        device2 = new Device("DEV002", "192.168.1.3", "NYC");
    }

    @Test
    void shouldManageBandwidthCorrectly() {
        router.setBandwidth(2000);
        assertEquals(2000, router.getBandwidth());

        router.setBandwidth(0);
        assertEquals(0, router.getBandwidth());

        router.setBandwidth(-500);
        assertEquals(0, router.getBandwidth(), "Negative bandwidth should be converted to zero");

        router.setBandwidth(1500);
        assertEquals(1500, router.getBandwidth());
        router.setBandwidth(-1);
        assertEquals(0, router.getBandwidth());
        router.setBandwidth(2500);
        assertEquals(2500, router.getBandwidth());
    }

    @Test
    void shouldManageRoutingTableCorrectly() {
        router.addDevice(device1);
        Map<String, String> routingTable = router.getRoutingTable();
        assertEquals(1, routingTable.size());
        assertEquals("DEV001", routingTable.get("192.168.1.2"));

        router.addDevice(device2);
        assertEquals(2, routingTable.size());
        assertEquals("DEV002", routingTable.get("192.168.1.3"));

        Device device3 = new Device("DEV003", "192.168.1.2", "NYC");
        router.addDevice(device3);
        assertEquals(2, routingTable.size());
        assertEquals("DEV003", routingTable.get("192.168.1.2"), "New device should override existing routing entry for same IP");
    }

    @Test
    void shouldManageConnectedDevicesCorrectly() {
        router.addDevice(device1);
        router.addDevice(device2);
        List<Device> devices = router.getConnectedDevices();
        assertEquals(2, devices.size());
        assertTrue(devices.contains(device1));
        assertTrue(devices.contains(device2));

        router.addDevice(device1);
        assertEquals(3, devices.size(), "Duplicate devices should be allowed in connected devices list");

        assertEquals(device1, devices.get(0));
        assertEquals(device2, devices.get(1));
        assertEquals(device1, devices.get(2));
    }

    @Test
    void shouldRemoveDevicesCorrectly() {
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
    void shouldInheritNetworkDeviceFunctionality() {
        assertTrue(router.isOnline());
        router.setOnline(false);
        assertFalse(router.isOnline());

        Router sameRouter = new Router("RTR001", "192.168.1.100", "LAX", 2000);
        Router differentRouter = new Router("RTR002", "192.168.1.1", "NYC", 1000);

        assertEquals(router, sameRouter, "Routers with same deviceID should be equal");
        assertNotEquals(router, differentRouter, "Routers with different deviceID should not be equal");
    }
}