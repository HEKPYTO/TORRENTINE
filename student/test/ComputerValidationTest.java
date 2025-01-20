package test.validate;

import base.Computer;
import base.Device;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ComputerValidationTest {
    private Computer computer;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        computer = new Computer("CPU001", "172.16.1.1", "SFO",
                2000, 75.0, 150.0, 2000000L);
        testDevice = new Device("NET001", "172.16.1.10", "SFO");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("CPU001", computer.getDeviceID(), "DeviceID should match constructor input");
        assertEquals("172.16.1.1", computer.getIpAddress(), "IP should match constructor input");
        assertEquals("SFO", computer.getLocation(), "Location should match constructor input");
        assertTrue(computer.isOnline(), "Should be initially online");

        assertEquals(2000, computer.getBandwidth(), "Bandwidth should match constructor input");
        assertTrue(computer.getRoutingTable().isEmpty(), "Initial routing table should be empty");
        assertTrue(computer.getConnectedDevices().isEmpty(), "Initial connected devices list should be empty");

        assertEquals(75.0, computer.getMaxUploadSpeed(), "Upload speed should match constructor input");
        assertEquals(150.0, computer.getMaxDownloadSpeed(), "Download speed should match constructor input");
        assertEquals(2000000L, computer.getStorageCapacity(), "Storage capacity should match constructor input");
        assertEquals(0L, computer.getUsedStorage(), "Initial used storage should be zero");

        Computer computer2 = new Computer("CPU002", "172.16.1.2", "LAX",
                3000, 100.0, 200.0, 3000000L);
        assertEquals("CPU002", computer2.getDeviceID());
        assertEquals("172.16.1.2", computer2.getIpAddress());
        assertEquals("LAX", computer2.getLocation());
        assertEquals(3000, computer2.getBandwidth());
        assertEquals(100.0, computer2.getMaxUploadSpeed());
        assertEquals(200.0, computer2.getMaxDownloadSpeed());
        assertEquals(3000000L, computer2.getStorageCapacity());
    }

    @Test
    void shouldValidateStorageCapacitySettings() {
        // Test positive capacity
        computer.setStorageCapacity(3000000L);
        assertEquals(3000000L, computer.getStorageCapacity(), "Should accept larger capacity");

        // Test zero capacity
        computer.setStorageCapacity(0L);
        assertEquals(0L, computer.getStorageCapacity(), "Should accept zero capacity");

        // Test negative capacity
        computer.setStorageCapacity(-1000L);
        assertEquals(0L, computer.getStorageCapacity(), "Should convert negative to zero");

        // Test maximum value
        computer.setStorageCapacity(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, computer.getStorageCapacity(), "Should accept maximum long value");
    }

    @Test
    void shouldValidateUsedStorageSettings() {
        computer.setUsedStorage(1000000L);
        assertEquals(1000000L, computer.getUsedStorage(), "Should set valid used storage");

        computer.setUsedStorage(3000000L);
        assertEquals(2000000L, computer.getUsedStorage(), "Should cap at capacity");

        computer.setUsedStorage(-500L);
        assertEquals(0L, computer.getUsedStorage(), "Should handle negative value");

        computer.setUsedStorage(0L);
        assertEquals(0L, computer.getUsedStorage(), "Should accept zero");

        computer.setUsedStorage(1500000L);
        computer.setStorageCapacity(1000000L);
        assertEquals(1500000L, computer.getUsedStorage(), "Used storage remains unchanged when capacity decreases");

        computer.setUsedStorage(1500000L);
        assertEquals(1000000L, computer.getUsedStorage(), "New used storage should respect current capacity");

        computer.setStorageCapacity(Long.MAX_VALUE);
        computer.setUsedStorage(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, computer.getUsedStorage(), "Should handle maximum long value");
    }

    @Test
    void shouldInheritRouterFunctionality() {
        computer.addDevice(testDevice);
        assertEquals(1, computer.getConnectedDevices().size(), "Should have one connected device");
        assertTrue(computer.getConnectedDevices().contains(testDevice), "Should contain added device");
        assertEquals("NET001", computer.getRoutingTable().get("172.16.1.10"), "Should map IP to device ID");

        Device testDevice2 = new Device("NET002", "172.16.1.20", "SFO");
        computer.addDevice(testDevice2);
        assertEquals(2, computer.getConnectedDevices().size(), "Should have two connected devices");
        assertTrue(computer.getConnectedDevices().contains(testDevice2), "Should contain second device");
        assertEquals("NET002", computer.getRoutingTable().get("172.16.1.20"), "Should map second IP");

        Device conflictDevice = new Device("NET003", "172.16.1.10", "SFO");
        computer.addDevice(conflictDevice);
        assertEquals("NET003", computer.getRoutingTable().get("172.16.1.10"), "Should update routing for conflicting IP");

        computer.setBandwidth(4000);
        assertEquals(4000, computer.getBandwidth(), "Should update bandwidth");
        computer.setBandwidth(-1000);
        assertEquals(0, computer.getBandwidth(), "Should handle negative bandwidth");
    }

    @Test
    void shouldManageStorageSpace() {
        assertTrue(computer.hasStorageSpace(0L), "Should have space for zero requirement");

        assertTrue(computer.hasStorageSpace(computer.getStorageCapacity()),
                "Should have space for exact capacity");

        assertFalse(computer.hasStorageSpace(computer.getStorageCapacity() + 1),
                "Should reject space exceeding capacity");

        assertTrue(computer.hasStorageSpace(1000000L), "Should have space for half capacity");
        assertTrue(computer.hasStorageSpace(500000L), "Should have space for quarter capacity");
        assertFalse(computer.hasStorageSpace(3000000L), "Should reject 1.5x capacity");

        assertTrue(computer.hasStorageSpace(1L), "Should have space for minimum value");
        assertFalse(computer.hasStorageSpace(Long.MAX_VALUE), "Should reject maximum long value");

        assertTrue(computer.hasStorageSpace(-1L), "Should handle negative space request");
    }

    @Test
    void shouldProvideCorrectGetterValues() {
        assertEquals(75.0, computer.getMaxUploadSpeed(), "Upload speed should be consistent");
        assertEquals(75.0, computer.getMaxUploadSpeed(), "Upload speed should remain same");
        assertEquals(150.0, computer.getMaxDownloadSpeed(), "Download speed should be consistent");
        assertEquals(150.0, computer.getMaxDownloadSpeed(), "Download speed should remain same");

        assertEquals(2000000L, computer.getStorageCapacity(), "Storage capacity should be consistent");
        assertEquals(2000000L, computer.getStorageCapacity(), "Storage capacity should remain same");
        assertEquals(0L, computer.getUsedStorage(), "Used storage should be consistent");
        assertEquals(0L, computer.getUsedStorage(), "Used storage should remain same");

        Computer computer2 = new Computer("CPU002", "172.16.1.2", "LAX",
                3000, 100.0, 200.0, 3000000L);
        assertEquals(100.0, computer2.getMaxUploadSpeed(), "Different upload speed");
        assertEquals(200.0, computer2.getMaxDownloadSpeed(), "Different download speed");
        assertEquals(3000000L, computer2.getStorageCapacity(), "Different capacity");
        assertEquals(0L, computer2.getUsedStorage(), "Different used storage");
    }

    @Test
    void shouldInheritNetworkDeviceFunctionality() {
        assertTrue(computer.isOnline(), "Should be initially online");
        computer.setOnline(false);
        assertFalse(computer.isOnline(), "Should be offline after setting");
        computer.setOnline(true);
        assertTrue(computer.isOnline(), "Should be online after resetting");

        Computer sameIDComputer = new Computer("CPU001", "172.16.1.2", "LAX",
                3000, 100.0, 200.0, 3000000L);
        Computer differentIDComputer = new Computer("CPU002", "172.16.1.1", "SFO",
                2000, 75.0, 150.0, 2000000L);

        assertEquals(computer, sameIDComputer, "Computers with same ID should be equal");
        assertNotEquals(computer, differentIDComputer, "Computers with different IDs should not be equal");
    }
}