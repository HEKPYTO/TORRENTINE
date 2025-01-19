package test.solution;

import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class NetworkDeviceSolutionTest {
    private NetworkDevice device;
    private static final String VALID_DEVICE_ID = "NET001";
    private static final String VALID_IP = "172.16.1.1";
    private static final String VALID_LOCATION = "SEA";

    @BeforeEach
    void setUp() {
        device = new NetworkDevice(VALID_DEVICE_ID, VALID_IP, VALID_LOCATION);
    }

    @Test
    void shouldManageOnlineState() {
        assertTrue(device.isOnline(), "Device should be online by default");

        device.setOnline(false);
        assertFalse(device.isOnline(), "Device should be offline after setting false");

        device.setOnline(true);
        assertTrue(device.isOnline(), "Device should be online after setting true");

        for (int i = 0; i < 5; i++) {
            device.setOnline(false);
            assertFalse(device.isOnline(), "Device should be offline in iteration " + i);
            device.setOnline(true);
            assertTrue(device.isOnline(), "Device should be online in iteration " + i);
        }
    }

    @Test
    void shouldImplementEqualsCorrectly() {
        NetworkDevice sameIdDevice1 = new NetworkDevice(VALID_DEVICE_ID, "192.168.1.1", "PDX");
        NetworkDevice sameIdDevice2 = new NetworkDevice(VALID_DEVICE_ID, "10.0.0.1", "SFO");

        NetworkDevice differentIdDevice = new NetworkDevice("NET002", VALID_IP, VALID_LOCATION);

        assertEquals(device, sameIdDevice1, "Devices with same ID should be equal");
        assertEquals(sameIdDevice1, sameIdDevice2, "Devices with same ID should be equal");
        assertEquals(device, sameIdDevice2, "Equality should be transitive");

        assertNotEquals(device, differentIdDevice, "Devices with different IDs should not be equal");
    }

    @Test
    void shouldHandleEqualsEdgeCases() {
        NetworkDevice upperCase = new NetworkDevice("NET001", VALID_IP, VALID_LOCATION);
        NetworkDevice lowerCase = new NetworkDevice("net001", VALID_IP, VALID_LOCATION);
        NetworkDevice mixedCase = new NetworkDevice("Net001", VALID_IP, VALID_LOCATION);

        assertNotEquals(upperCase, lowerCase, "Different case IDs should not be equal");
        assertNotEquals(upperCase, mixedCase, "Different case IDs should not be equal");
        assertNotEquals(lowerCase, mixedCase, "Different case IDs should not be equal");

        NetworkDevice withHyphen = new NetworkDevice("NET-001", VALID_IP, VALID_LOCATION);
        NetworkDevice withUnderscore = new NetworkDevice("NET_001", VALID_IP, VALID_LOCATION);
        NetworkDevice withDot = new NetworkDevice("NET.001", VALID_IP, VALID_LOCATION);

        assertNotEquals(device, withHyphen, "IDs with different special characters should not be equal");
        assertNotEquals(device, withUnderscore, "IDs with different special characters should not be equal");
        assertNotEquals(device, withDot, "IDs with different special characters should not be equal");
    }

    @Test
    void shouldProvideImmutableProperties() {
        String initialId = device.getDeviceID();
        String initialIp = device.getIpAddress();
        String initialLocation = device.getLocation();

        assertEquals(initialId, device.getDeviceID(), "DeviceID should be immutable");
        assertEquals(initialIp, device.getIpAddress(), "IP should be immutable");
        assertEquals(initialLocation, device.getLocation(), "Location should be immutable");
    }
}