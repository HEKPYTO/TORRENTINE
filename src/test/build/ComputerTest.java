package test.build;

import base.Computer;
import base.Device;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class ComputerTest {
    private Computer computer;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        computer = new Computer("CMP001", "192.168.1.1", "NYC",
                1000, 50.0, 100.0, 1000000L);
        testDevice = new Device("DEV001", "192.168.1.2", "NYC");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("CMP001", computer.getDeviceID());
        assertEquals("192.168.1.1", computer.getIpAddress());
        assertEquals("NYC", computer.getLocation());
        assertTrue(computer.isOnline());

        assertEquals(1000, computer.getBandwidth());
        assertTrue(computer.getRoutingTable().isEmpty());
        assertTrue(computer.getConnectedDevices().isEmpty());

        assertEquals(50.0, computer.getMaxUploadSpeed());
        assertEquals(100.0, computer.getMaxDownloadSpeed());
        assertEquals(1000000L, computer.getStorageCapacity());
        assertEquals(0L, computer.getUsedStorage());
    }

    @Test
    void shouldSetStorageCapacityCorrectly() {
        computer.setStorageCapacity(2000000L);
        assertEquals(2000000L, computer.getStorageCapacity());

        computer.setStorageCapacity(0);
        assertEquals(0L, computer.getStorageCapacity());

        computer.setStorageCapacity(-1000);
        assertEquals(0L, computer.getStorageCapacity());
    }

    @Test
    void shouldSetUsedStorageCorrectly() {
        computer.setUsedStorage(500000L);
        assertEquals(500000L, computer.getUsedStorage());

        computer.setUsedStorage(-1000L);
        assertEquals(0L, computer.getUsedStorage());

        computer.setUsedStorage(2000000L);
        assertEquals(1000000L, computer.getUsedStorage(), "Should cap at storage capacity");
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
