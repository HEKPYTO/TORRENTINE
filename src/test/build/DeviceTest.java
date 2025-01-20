package test.build;

import base.Device;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class DeviceTest {
    private Device device;

    @BeforeEach
    void setUp() {
        device = new Device("DEV001", "192.168.1.1", "NYC");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("DEV001", device.getDeviceID());
        assertEquals("192.168.1.1", device.getIpAddress());
        assertEquals("NYC", device.getLocation());
        assertTrue(device.isOnline());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "256.1.2.3",        // Invalid first octet
            "1.256.2.3",        // Invalid second octet
            "1.2.256.3",        // Invalid third octet
            "1.2.3.256",        // Invalid fourth octet
            "192.168.1",        // Too few octets
            "192.168.1.1.1",    // Too many octets
            "192.168.1.a",      // Non-numeric
            ".168.1.1",         // Missing octet
            "192.168..1"        // Empty octet
    })
    void constructorShouldThrowOnInvalidIP(String invalidIP) {
        assertThrows(IllegalArgumentException.class,
                () -> new Device("DEV001", invalidIP, "NYC"));
    }

    @Test
    void setOnlineShouldUpdateStatus() {
        device.setOnline(false);
        assertFalse(device.isOnline());
        device.setOnline(true);
        assertTrue(device.isOnline());
    }

    @Test
    void equalsShouldCheckDeviceIDOnly() {
        Device sameDevice = new Device("DEV001", "192.168.1.2", "LAX");
        Device differentDevice = new Device("DEV002", "192.168.1.1", "NYC");

        assertEquals(device, sameDevice, "Devices with same ID should be equal");
        assertNotEquals(device, differentDevice, "Devices with different IDs should not be equal");
        assertNotEquals(device, null, "Device should not be equal to null");
        assertNotEquals(device, "DEV001", "Device should not be equal to a string");
    }

    @Test
    void validIPsShouldBeAccepted() {
        // Test boundary cases for valid IPs
        assertDoesNotThrow(() -> new Device("DEV002", "0.0.0.0", "NYC"));
        assertDoesNotThrow(() -> new Device("DEV003", "255.255.255.255", "NYC"));
        assertDoesNotThrow(() -> new Device("DEV004", "192.168.0.1", "NYC"));
        assertDoesNotThrow(() -> new Device("DEV005", "10.0.0.0", "NYC"));
        assertDoesNotThrow(() -> new Device("DEV006", "172.16.0.1", "NYC"));
    }
}