package test.build;

import base.Computer;
import base.NetworkDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class ComputerTest {
    private Computer computer;
    private NetworkDevice testDevice;

    @BeforeEach
    void setUp() {
        computer = new Computer("CMP001", "192.168.1.1", "NYC",
                1000, 50.0, 100.0, 1000000L);
        testDevice = new NetworkDevice("DEV001", "192.168.1.2", "NYC");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        // Test NetworkDevice inherited properties
        assertEquals("CMP001", computer.getDeviceID());
        assertEquals("192.168.1.1", computer.getIpAddress());
        assertEquals("NYC", computer.getLocation());
        assertTrue(computer.isOnline());

        // Test Router inherited properties
        assertEquals(1000, computer.getBandwidth());
        assertTrue(computer.getRoutingTable().isEmpty());
        assertTrue(computer.getConnectedDevices().isEmpty());

        // Test Computer specific properties
        assertEquals(50.0, computer.getMaxUploadSpeed());
        assertEquals(100.0, computer.getMaxDownloadSpeed());
        assertEquals(1000000L, computer.getStorageCapacity());
        assertEquals(0L, computer.getUsedStorage());
    }

    @Test
    void shouldInheritRouterFunctionality() {
        computer.addDevice(testDevice);

        assertEquals(1, computer.getConnectedDevices().size());
        assertTrue(computer.getConnectedDevices().contains(testDevice));
        assertEquals("DEV001", computer.getRoutingTable().get("192.168.1.2"));
    }

    @Test
    void gettersShouldReturnCorrectValues() {
        assertEquals(50.0, computer.getMaxUploadSpeed());
        assertEquals(100.0, computer.getMaxDownloadSpeed());
        assertEquals(1000000L, computer.getStorageCapacity());
        assertEquals(0L, computer.getUsedStorage());
    }
}
