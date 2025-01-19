package test.validate;

import base.torrent.TorrentTracker;
import model.TorrentFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TorrentTrackerValidationTest {
    private TorrentTracker tracker;
    private TorrentFile testFile;
    private static final int PIECE_SIZE = 262144;

    @BeforeEach
    void setUp() {
        tracker = new TorrentTracker("TTR001", "172.16.1.1", "SFO",
                2000, 150.0, 150.0, 20000000L);
        testFile = new TorrentFile("hash123", "sample.mkv", 2097152L, PIECE_SIZE);
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("TTR001", tracker.getDeviceID(), "Device ID should match");
        assertEquals("172.16.1.1", tracker.getIpAddress(), "IP address should match");
        assertEquals("SFO", tracker.getLocation(), "Location should match");
        assertTrue(tracker.isOnline(), "Should be initially online");
        assertEquals(2000, tracker.getBandwidth(), "Bandwidth should match");
        assertEquals(150.0, tracker.getMaxUploadSpeed(), "Upload speed should match");
        assertEquals(150.0, tracker.getMaxDownloadSpeed(), "Download speed should match");
        assertEquals(20000000L, tracker.getStorageCapacity(), "Storage capacity should match");

        assertFalse(tracker.isTracked("hash123"), "No files should be initially tracked");
        assertTrue(tracker.getPeers("hash123").isEmpty(), "Initial peer set should be empty");
        assertNull(tracker.getTrackedFile("hash123"), "No files should be initially stored");
    }

    @Test
    void shouldHandleFileTracking() {
        tracker.trackFile(testFile);
        assertTrue(tracker.isTracked(testFile.getInfoHash()), "File should be tracked");
        assertEquals(testFile, tracker.getTrackedFile(testFile.getInfoHash()), "Should return tracked file");
        assertTrue(tracker.getPeers(testFile.getInfoHash()).isEmpty(), "Initial peer set should be empty");

        tracker.trackFile(testFile);
        assertTrue(tracker.isTracked(testFile.getInfoHash()), "File should remain tracked");
        assertEquals(testFile, tracker.getTrackedFile(testFile.getInfoHash()), "Should maintain original file");
        assertTrue(tracker.getPeers(testFile.getInfoHash()).isEmpty(), "Peer set should remain empty");
    }

    @Test
    void shouldHandleInvalidFileTracking() {
        tracker.trackFile(null);
        assertFalse(tracker.isTracked(null), "Null file should not be tracked");
        assertTrue(tracker.getPeers(null).isEmpty(), "Null file should have no peers");
        assertNull(tracker.getTrackedFile(null), "Null file should not be stored");

        TorrentFile invalidFile = new TorrentFile(null, "invalid.mkv", 1048576L, PIECE_SIZE);
        tracker.trackFile(invalidFile);
        assertFalse(tracker.isTracked(null), "File with null hash should not be tracked");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "started", "completed", "stopped", "paused",
            "checking", "error", "ready", "downloading",
            "seeding", "finished", "waiting", "connecting"
    })
    void shouldHandleAllAnnounceEvents(String event) {
        tracker.trackFile(testFile);
        String infoHash = testFile.getInfoHash();

        tracker.announce(infoHash, "PEER001", event);
        assertTrue(tracker.getPeers(infoHash).contains("PEER001"),
                "Peer should be registered for event: " + event);

        tracker.announce(infoHash, "PEER001", event);
        assertEquals(1, tracker.getPeers(infoHash).size(),
                "Duplicate announcement should not create duplicate entry");

        tracker.announce(infoHash, "PEER002", event);
        var peers = tracker.getPeers(infoHash);
        assertEquals(2, peers.size(), "Should track multiple peers for same event");
        assertTrue(peers.contains("PEER002"), "Second peer should be registered");
    }

    @Test
    void shouldManageMultipleFiles() {
        TorrentFile file1 = new TorrentFile("hash123", "video1.mkv", 2097152L, PIECE_SIZE);
        TorrentFile file2 = new TorrentFile("hash456", "video2.mkv", 3145728L, PIECE_SIZE);
        TorrentFile file3 = new TorrentFile("hash789", "video3.mkv", 4194304L, PIECE_SIZE);

        tracker.trackFile(file1);
        tracker.trackFile(file2);
        tracker.trackFile(file3);

        assertTrue(tracker.isTracked("hash123"), "First file should be tracked");
        assertTrue(tracker.isTracked("hash456"), "Second file should be tracked");
        assertTrue(tracker.isTracked("hash789"), "Third file should be tracked");

        tracker.announce("hash123", "PEER001", "started");
        tracker.announce("hash456", "PEER002", "started");
        tracker.announce("hash789", "PEER003", "started");

        tracker.announce("hash123", "PEER002", "started");
        tracker.announce("hash456", "PEER003", "started");
        tracker.announce("hash789", "PEER001", "started");

        assertEquals(2, tracker.getPeers("hash123").size(), "First file should have two peers");
        assertEquals(2, tracker.getPeers("hash456").size(), "Second file should have two peers");
        assertEquals(2, tracker.getPeers("hash789").size(), "Third file should have two peers");

        assertEquals(file1, tracker.getTrackedFile("hash123"), "Should retrieve first file");
        assertEquals(file2, tracker.getTrackedFile("hash456"), "Should retrieve second file");
        assertEquals(file3, tracker.getTrackedFile("hash789"), "Should retrieve third file");
    }

    @Test
    void shouldProvideDefensiveCopies() {
        tracker.trackFile(testFile);
        tracker.announce(testFile.getInfoHash(), "PEER001", "started");

        Set<String> peers = tracker.getPeers(testFile.getInfoHash());
        peers.add("PEER002");
        peers.remove("PEER001");
        peers.clear();

        Set<String> originalPeers = tracker.getPeers(testFile.getInfoHash());
        assertEquals(1, originalPeers.size(), "Original set size should be unchanged");
        assertTrue(originalPeers.contains("PEER001"), "Original peer should remain");
        assertFalse(originalPeers.contains("PEER002"), "Added peer should not appear");
    }

    @Test
    void shouldInheritComputerFunctionality() {
        assertTrue(tracker.isOnline(), "Should be initially online");
        tracker.setOnline(false);
        assertFalse(tracker.isOnline(), "Should be offline after setting");
        tracker.setOnline(true);
        assertTrue(tracker.isOnline(), "Should be online after resetting");

        tracker.setBandwidth(3000);
        assertEquals(3000, tracker.getBandwidth(), "Should update bandwidth");
        tracker.setBandwidth(-1);
        assertEquals(0, tracker.getBandwidth(), "Should handle negative bandwidth");

        assertEquals(20000000L, tracker.getStorageCapacity(), "Should maintain storage capacity");
        assertTrue(tracker.hasStorageSpace(10000000L), "Should have sufficient storage");
        assertFalse(tracker.hasStorageSpace(30000000L), "Should detect insufficient storage");
    }
}