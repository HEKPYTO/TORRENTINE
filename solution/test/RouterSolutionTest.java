package test.solution;

import base.Router;
import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;


class RouterSolutionTest {
    private Router router;
    private NetworkDevice device1;
    private NetworkDevice device2;

    @BeforeEach
    void setUp() {
        router = new Router("RTR001", "192.168.1.1", "NYC", 1000);
        device1 = new NetworkDevice("DEV001", "192.168.1.2", "NYC");
        device2 = new NetworkDevice("DEV002", "192.168.1.3", "NYC");
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

        NetworkDevice device3 = new NetworkDevice("DEV003", "192.168.1.2", "NYC");
        router.addDevice(device3);
        assertEquals(2, routingTable.size());
        assertEquals("DEV003", routingTable.get("192.168.1.2"), "New device should override existing routing entry for same IP");
    }

    @Test
    void shouldManageConnectedDevicesCorrectly() {
        router.addDevice(device1);
        router.addDevice(device2);
        List<NetworkDevice> devices = router.getConnectedDevices();
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