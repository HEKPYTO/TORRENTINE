package test.validate;

import base.Device;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DeviceValidationTest {
    private Device device;

    @BeforeEach
    void setUp() {
        device = new Device("NET001", "172.16.1.1", "SFO");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("NET001", device.getDeviceID(), "DeviceID should match constructor input");
        assertEquals("172.16.1.1", device.getIpAddress(), "IP address should match constructor input");
        assertEquals("SFO", device.getLocation(), "Location should match constructor input");
        assertTrue(device.isOnline(), "Device should be initially online");

        Device otherDevice = new Device("NET002", "10.0.0.1", "NYC");
        assertEquals("NET002", otherDevice.getDeviceID());
        assertEquals("10.0.0.1", otherDevice.getIpAddress());
        assertEquals("NYC", otherDevice.getLocation());
        assertTrue(otherDevice.isOnline());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "256.1.2.3", "1.256.2.3", "1.2.256.3", "1.2.3.256", "-1.2.3.4", "a.b.c.d", "192.168.1.a", "192.168.1", ".168.1.1", "192..1.1", "192.168.1.1..", "..192.168.1.1", " 192.168.1.1", "192.168.1.1 ", "192. 168.1.1", "192.168.01.1", "192.168.1.01", "01.168.1.1",})
    void constructorShouldThrowOnInvalidIP(String invalidIP) {
        assertThrows(IllegalArgumentException.class,
                () -> new Device("NET001", invalidIP, "SFO"),
                "Should throw IllegalArgumentException for invalid IP: " + invalidIP);
    }

    @Test
    void deviceIDGetterShouldReturnCorrectValue() {
        assertEquals("NET001", device.getDeviceID());

        Device device2 = new Device("NET-002", "172.16.1.2", "SFO");
        assertEquals("NET-002", device2.getDeviceID());

        Device device3 = new Device("NET_003", "172.16.1.3", "SFO");
        assertEquals("NET_003", device3.getDeviceID());
    }

    @Test
    void ipAddressGetterShouldReturnCorrectValue() {
        assertEquals("172.16.1.1", device.getIpAddress());

        Device device2 = new Device("NET002", "10.0.0.1", "SFO");
        assertEquals("10.0.0.1", device2.getIpAddress());

        Device device3 = new Device("NET003", "192.168.0.1", "SFO");
        assertEquals("192.168.0.1", device3.getIpAddress());
    }

    @Test
    void locationGetterShouldReturnCorrectValue() {
        assertEquals("SFO", device.getLocation());

        Device device2 = new Device("NET002", "172.16.1.2", "NYC");
        assertEquals("NYC", device2.getLocation());

        Device device3 = new Device("NET003", "172.16.1.3", "LAX");
        assertEquals("LAX", device3.getLocation());
    }

    @Test
    void onlineStatusShouldBeManaged() {
        assertTrue(device.isOnline(), "Device should be initially online");

        device.setOnline(false);
        assertFalse(device.isOnline(), "Device should be offline after setting false");

        device.setOnline(true);
        assertTrue(device.isOnline(), "Device should be online after setting true");

        device.setOnline(false);
        assertFalse(device.isOnline());
        device.setOnline(true);
        assertTrue(device.isOnline());
        device.setOnline(false);
        assertFalse(device.isOnline());

        device.setOnline(false);
        device.setOnline(false);
        assertFalse(device.isOnline(), "Device should remain offline after multiple false settings");

        device.setOnline(true);
        device.setOnline(true);
        assertTrue(device.isOnline(), "Device should remain online after multiple true settings");
    }

    @Test
    void equalsShouldImplementCorrectly() {
        Device sameId1 = new Device("NET001", "10.0.0.1", "NYC");
        Device sameId2 = new Device("NET001", "192.168.1.1", "LAX");
        assertEquals(device, sameId1, "Devices with same ID should be equal regardless of IP");
        assertEquals(device, sameId2, "Devices with same ID should be equal regardless of location");
        assertEquals(sameId1, sameId2, "Transitivity check for devices with same ID");

        Device differentId1 = new Device("NET002", "172.16.1.1", "SFO");
        Device differentId2 = new Device("NET003", "172.16.1.1", "SFO");
        assertNotEquals(device, differentId1, "Devices with different IDs should not be equal");
        assertNotEquals(device, differentId2, "Devices with different IDs should not be equal");
        assertNotEquals(differentId1, differentId2, "Different devices should not be equal");
    }

    @Test
    void validIPsShouldBeAccepted() {
        assertDoesNotThrow(() -> new Device("NET002", "0.0.0.0", "SFO"),
                "Minimum IP values should be accepted");
        assertDoesNotThrow(() -> new Device("NET003", "255.255.255.255", "SFO"),
                "Maximum IP values should be accepted");

        assertDoesNotThrow(() -> new Device("NET004", "10.0.0.1", "SFO"),
                "Class A private network should be accepted");
        assertDoesNotThrow(() -> new Device("NET005", "172.16.0.1", "SFO"),
                "Class B private network should be accepted");
        assertDoesNotThrow(() -> new Device("NET006", "192.168.0.1", "SFO"),
                "Class C private network should be accepted");

        assertDoesNotThrow(() -> new Device("NET007", "127.0.0.1", "SFO"),
                "Loopback address should be accepted");
        assertDoesNotThrow(() -> new Device("NET008", "169.254.0.1", "SFO"),
                "Link-local address should be accepted");
        assertDoesNotThrow(() -> new Device("NET009", "224.0.0.1", "SFO"),
                "Multicast address should be accepted");
        assertDoesNotThrow(() -> new Device("NET010", "8.8.8.8", "SFO"),
                "Public IP address should be accepted");
    }
}