package test.build;

import torrent.TorrentTracker;
import model.TorrentFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class TorrentTrackerTest {
    private TorrentTracker tracker;
    private TorrentFile testFile;
    private static final int PIECE_SIZE = 262144; // 256KB

    @BeforeEach
    void setUp() {
        tracker = new TorrentTracker("TRK001", "192.168.1.2", "NYC",
                1000, 50.0, 50.0, 1000000L);
        testFile = new TorrentFile("hash123", "test.mp4", 1048576L, PIECE_SIZE); // 1MB file
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("TRK001", tracker.getDeviceID());
        assertEquals("192.168.1.2", tracker.getIpAddress());
        assertEquals("NYC", tracker.getLocation());
        assertTrue(tracker.isOnline());
        assertEquals(50.0, tracker.getMaxUploadSpeed());
        assertEquals(50.0, tracker.getMaxDownloadSpeed());
        assertEquals(1000000L, tracker.getStorageCapacity());
    }

    @Test
    void shouldTrackNewFile() {
        tracker.trackFile(testFile);
        assertTrue(tracker.isTracked("hash123"), "File should be tracked");
        assertTrue(tracker.getPeers("hash123").isEmpty(), "Initial peer set should be empty");
        assertEquals(testFile, tracker.getTrackedFile("hash123"), "Should return tracked file");
    }

    @ParameterizedTest
    @ValueSource(strings = {"started", "completed", "stopped", "paused"})
    void shouldHandleVariousAnnounceEvents(String event) {
        tracker.trackFile(testFile);
        tracker.announce("hash123", "PEER1", event);

        assertTrue(tracker.getPeers("hash123").contains("PEER1"),
                "Peer should be registered regardless of event: " + event);
    }

    @Test
    void shouldTrackMultiplePeers() {
        tracker.trackFile(testFile);

        // Announce multiple peers
        tracker.announce("hash123", "PEER1", "started");
        tracker.announce("hash123", "PEER2", "started");
        tracker.announce("hash123", "PEER3", "started");

        var peers = tracker.getPeers("hash123");
        assertEquals(3, peers.size(), "Should track all announced peers");
        assertTrue(peers.contains("PEER1"));
        assertTrue(peers.contains("PEER2"));
        assertTrue(peers.contains("PEER3"));
    }

    @Test
    void shouldHandleDuplicateAnnouncements() {
        tracker.trackFile(testFile);

        // Announce same peer multiple times
        tracker.announce("hash123", "PEER1", "started");
        tracker.announce("hash123", "PEER1", "completed");
        tracker.announce("hash123", "PEER1", "started");

        assertEquals(1, tracker.getPeers("hash123").size(),
                "Duplicate announcements should not create duplicate entries");
    }

    @Test
    void shouldHandleMultipleFiles() {
        TorrentFile testFile2 = new TorrentFile("hash456", "movie.mp4", 2097152L, PIECE_SIZE);

        tracker.trackFile(testFile);
        tracker.trackFile(testFile2);

        tracker.announce("hash123", "PEER1", "started");
        tracker.announce("hash456", "PEER2", "started");

        assertEquals(1, tracker.getPeers("hash123").size());
        assertEquals(1, tracker.getPeers("hash456").size());
        assertTrue(tracker.getPeers("hash123").contains("PEER1"));
        assertTrue(tracker.getPeers("hash456").contains("PEER2"));
        assertTrue(tracker.isTracked("hash123"));
        assertTrue(tracker.isTracked("hash456"));
    }

    @Test
    void shouldIgnoreUnknownFiles() {
        tracker.announce("unknown", "PEER1", "started");
        assertFalse(tracker.isTracked("unknown"), "Unknown file should not be tracked");
        assertTrue(tracker.getPeers("unknown").isEmpty(), "Unknown file should have no peers");
        assertNull(tracker.getTrackedFile("unknown"), "Unknown file should return null");
    }

    @Test
    void shouldProvideDefensiveCopyOfPeerSet() {
        tracker.trackFile(testFile);
        tracker.announce("hash123", "PEER1", "started");

        var peers = tracker.getPeers("hash123");
        peers.add("PEER2"); // Try to modify the returned set

        assertFalse(tracker.getPeers("hash123").contains("PEER2"),
                "Modified copy should not affect original peer set");
    }

    @Test
    void shouldHandleNullInputs() {
        assertDoesNotThrow(() -> {
            tracker.trackFile(null);
            assertFalse(tracker.isTracked(null), "Null hash should not be tracked");
            assertTrue(tracker.getPeers(null).isEmpty(), "Null hash should return empty peer set");
            assertNull(tracker.getTrackedFile(null), "Null hash should return null file");

            tracker.announce(null, "PEER1", "started");
            tracker.announce("hash123", null, "started");
            tracker.announce("hash123", "PEER1", null);
        });
    }
}