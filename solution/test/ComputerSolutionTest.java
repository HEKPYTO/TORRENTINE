package test.solution;

import base.Computer;
import base.Device;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ComputerSolutionTest {
    private Computer computer;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        computer = new Computer("CMP001", "192.168.1.1", "NYC", 1000, 50.0, 100.0, 1000000L);
        testDevice = new Device("DEV001", "192.168.1.2", "NYC");
    }

    @Test
    void shouldInheritRouterFunctionality() {
        computer.addDevice(testDevice);
        assertEquals(1, computer.getConnectedDevices().size());
        assertTrue(computer.getConnectedDevices().contains(testDevice));
        assertEquals("DEV001", computer.getRoutingTable().get("192.168.1.2"));

        computer.setBandwidth(2000);
        assertEquals(2000, computer.getBandwidth());
        computer.setBandwidth(-1000);
        assertEquals(0, computer.getBandwidth());
    }

    @Test
    void shouldCalculateStorageSpaceCorrectly() {
        assertTrue(computer.hasStorageSpace(0L));

        assertTrue(computer.hasStorageSpace(1000000L));

        assertFalse(computer.hasStorageSpace(1000001L));

        assertTrue(computer.hasStorageSpace(-1L));

        assertTrue(computer.hasStorageSpace(computer.getStorageCapacity()));
        assertFalse(computer.hasStorageSpace(computer.getStorageCapacity() + 1));
    }

    @Test
    void shouldManageStorageCapacity() {
        computer.setStorageCapacity(2000000L);
        assertEquals(2000000L, computer.getStorageCapacity(), "Should update to valid capacity");

        computer.setStorageCapacity(-1L);
        assertEquals(0L, computer.getStorageCapacity(), "Should convert negative to zero");

        computer.setStorageCapacity(0L);
        assertEquals(0L, computer.getStorageCapacity(), "Should accept zero capacity");

        computer.setStorageCapacity(500000L);
        computer.setUsedStorage(300000L);
        assertEquals(300000L, computer.getUsedStorage());

        computer.setStorageCapacity(200000L);
        assertEquals(300000L, computer.getUsedStorage(), "Used storage remains unchanged when capacity decreases");

        computer.setUsedStorage(300000L);
        assertEquals(200000L, computer.getUsedStorage(), "New used storage respects current capacity");
    }

    @Test
    void shouldManageUsedStorage() {
        computer.setUsedStorage(500000L);
        assertEquals(500000L, computer.getUsedStorage(), "Should set valid used storage");

        computer.setUsedStorage(-1L);
        assertEquals(0L, computer.getUsedStorage(), "Should handle negative value");

        computer.setUsedStorage(2000000L);
        assertEquals(1000000L, computer.getUsedStorage(), "Should cap at capacity");
    }

    @Test
    void shouldProvideCorrectSpeedValues() {
        assertEquals(50.0, computer.getMaxUploadSpeed());
        assertEquals(50.0, computer.getMaxUploadSpeed(), "Upload speed should be consistent");

        assertEquals(100.0, computer.getMaxDownloadSpeed());
        assertEquals(100.0, computer.getMaxDownloadSpeed(), "Download speed should be consistent");
    }

    @Test
    void shouldProvideCorrectStorageValues() {
        assertEquals(1000000L, computer.getStorageCapacity());
        assertEquals(1000000L, computer.getStorageCapacity(), "Storage capacity should be consistent");

        assertEquals(0L, computer.getUsedStorage());
        assertEquals(0L, computer.getUsedStorage(), "Used storage should be consistent");
    }

    @Test
    void shouldInheritNetworkDeviceFunctionality() {
        assertTrue(computer.isOnline());
        computer.setOnline(false);
        assertFalse(computer.isOnline());

        Computer sameComputer = new Computer("CMP001", "192.168.1.100", "LAX", 2000, 60.0, 120.0, 2000000L);
        Computer differentComputer = new Computer("CMP002", "192.168.1.1", "NYC", 1000, 50.0, 100.0, 1000000L);

        assertEquals(computer, sameComputer, "Computers with same deviceID should be equal");
        assertNotEquals(computer, differentComputer, "Computers with different deviceID should not be equal");
    }
}