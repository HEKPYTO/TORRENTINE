package test.solution;

import base.torrent.TorrentTracker;
import model.TorrentFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

class TorrentTrackerSolutionTest {
    private TorrentTracker tracker;
    private TorrentFile testFile;
    private static final int PIECE_SIZE = 262144;

    @BeforeEach
    void setUp() {
        tracker = new TorrentTracker("TRK001", "192.168.1.1", "NYC", 1000, 50.0, 100.0, 1000000L);
        testFile = new TorrentFile("hash123", "test.mp4", 524288L, PIECE_SIZE);
    }

    @Test
    void shouldManageFileTracking() {
        tracker.trackFile(testFile);
        assertTrue(tracker.isTracked(testFile.getInfoHash()));
        assertTrue(tracker.getPeers(testFile.getInfoHash()).isEmpty());
        assertEquals(testFile, tracker.getTrackedFile(testFile.getInfoHash()));

        tracker.trackFile(testFile);
        assertTrue(tracker.isTracked(testFile.getInfoHash()));
        assertEquals(testFile, tracker.getTrackedFile(testFile.getInfoHash()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"started", "completed", "stopped", "paused"})
    void shouldHandleAnnouncements(String event) {
        String infoHash = testFile.getInfoHash();
        tracker.trackFile(testFile);

        tracker.announce(infoHash, "PEER001", event);
        Set<String> peers = tracker.getPeers(infoHash);
        assertEquals(1, peers.size());
        assertTrue(peers.contains("PEER001"));

        tracker.announce(infoHash, "PEER002", event);
        peers = tracker.getPeers(infoHash);
        assertEquals(2, peers.size());
        assertTrue(peers.contains("PEER002"));
    }

    @Test
    void shouldHandleMultipleFiles() {
        TorrentFile testFile2 = new TorrentFile("hash456", "test2.mp4", 524288L, PIECE_SIZE);

        tracker.trackFile(testFile);
        tracker.trackFile(testFile2);

        tracker.announce(testFile.getInfoHash(), "PEER001", "started");
        tracker.announce(testFile2.getInfoHash(), "PEER002", "started");

        Set<String> peers1 = tracker.getPeers(testFile.getInfoHash());
        Set<String> peers2 = tracker.getPeers(testFile2.getInfoHash());

        assertEquals(1, peers1.size());
        assertEquals(1, peers2.size());
        assertTrue(peers1.contains("PEER001"));
        assertTrue(peers2.contains("PEER002"));
        assertTrue(tracker.isTracked(testFile.getInfoHash()));
        assertTrue(tracker.isTracked(testFile2.getInfoHash()));
    }

    @Test
    void shouldProvideDefensiveCopies() {
        tracker.trackFile(testFile);
        String infoHash = testFile.getInfoHash();
        tracker.announce(infoHash, "PEER001", "started");

        Set<String> peers = tracker.getPeers(infoHash);
        peers.add("PEER002");

        Set<String> originalPeers = tracker.getPeers(infoHash);
        assertEquals(1, originalPeers.size());
        assertTrue(originalPeers.contains("PEER001"));
        assertFalse(originalPeers.contains("PEER002"));
    }

    @Test
    void shouldInheritComputerFunctionality() {
        tracker.setBandwidth(2000);
        assertEquals(2000, tracker.getBandwidth());

        assertTrue(tracker.isOnline());
        tracker.setOnline(false);
        assertFalse(tracker.isOnline());

        assertEquals(1000000L, tracker.getStorageCapacity());
        assertEquals(0L, tracker.getUsedStorage());
    }
}