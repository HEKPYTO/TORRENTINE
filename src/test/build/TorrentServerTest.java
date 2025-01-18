package test.build;

import base.torrent.TorrentServer;
import model.TorrentFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TorrentServerTest {
    private TorrentServer server;
    private TorrentFile testFile;
    private static final int PIECE_SIZE = 262144; // 256KB

    @BeforeEach
    void setUp() {
        server = new TorrentServer("SRV001", "192.168.1.1", "NYC",
                1000, 100.0, 100.0, 10000000L);
        testFile = new TorrentFile("hash123", "test.mp4", 1048576L, PIECE_SIZE); // 1MB file
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("SRV001", server.getDeviceID());
        assertEquals("192.168.1.1", server.getIpAddress());
        assertEquals("NYC", server.getLocation());
        assertTrue(server.isOnline());
        assertEquals(100.0, server.getMaxUploadSpeed());
        assertEquals(100.0, server.getMaxDownloadSpeed());
        assertEquals(10000000L, server.getStorageCapacity());
    }

    @Test
    void hostFileShouldInitializeEmptyPeerSet() {
        server.hostFile(testFile);
        Set<String> peers = server.getActivePeers("hash123");
        assertNotNull(peers, "Peer set should be initialized");
        assertTrue(peers.isEmpty(), "Initial peer set should be empty");
    }

    @Test
    void shouldRegisterAndTrackPeers() {
        server.hostFile(testFile);

        // Register peers
        server.registerPeer("hash123", "PEER1");
        Set<String> peers = server.getActivePeers("hash123");
        assertEquals(1, peers.size());
        assertTrue(peers.contains("PEER1"));

        // Register another peer
        server.registerPeer("hash123", "PEER2");
        peers = server.getActivePeers("hash123");
        assertEquals(2, peers.size());
        assertTrue(peers.contains("PEER2"));
    }

    @Test
    void shouldReturnEmptySetForUnknownHash() {
        Set<String> peers = server.getActivePeers("unknown");
        assertNotNull(peers, "Should return empty set for unknown hash");
        assertTrue(peers.isEmpty(), "Peer set for unknown hash should be empty");
    }

    @Test
    void registerPeerShouldHandleUnknownHash() {
        server.registerPeer("unknown", "PEER1");
        Set<String> peers = server.getActivePeers("unknown");
        assertTrue(peers.isEmpty(), "No peers should be registered for unknown hash");
    }

    @Test
    void shouldPreventDuplicatePeers() {
        server.hostFile(testFile);
        server.registerPeer("hash123", "PEER1");
        server.registerPeer("hash123", "PEER1"); // Register same peer twice

        Set<String> peers = server.getActivePeers("hash123");
        assertEquals(1, peers.size(), "Duplicate peer registration should be ignored");
    }

    @Test
    void shouldHandleMultipleFiles() {
        TorrentFile testFile2 = new TorrentFile("hash456", "movie.mp4", 2097152L, PIECE_SIZE);

        server.hostFile(testFile);
        server.hostFile(testFile2);

        // Register peers for both files
        server.registerPeer("hash123", "PEER1");
        server.registerPeer("hash456", "PEER2");

        // Verify peers are tracked separately
        Set<String> peers1 = server.getActivePeers("hash123");
        Set<String> peers2 = server.getActivePeers("hash456");

        assertEquals(1, peers1.size());
        assertEquals(1, peers2.size());
        assertTrue(peers1.contains("PEER1"));
        assertTrue(peers2.contains("PEER2"));
    }

    @Test
    void shouldProvideDefensiveCopyOfPeerSet() {
        server.hostFile(testFile);
        server.registerPeer("hash123", "PEER1");

        Set<String> peers = server.getActivePeers("hash123");
        peers.add("PEER2"); // Try to modify the returned set

        Set<String> actualPeers = server.getActivePeers("hash123");
        assertEquals(1, actualPeers.size(),
                "Original peer set should not be affected by modifications to returned set");
        assertFalse(actualPeers.contains("PEER2"),
                "Added peer should not appear in original set");
    }

    @Test
    void shouldHandleNullHashAndPeer() {
        // Test null hash
        Set<String> peersForNullHash = server.getActivePeers(null);
        assertNotNull(peersForNullHash, "Should return empty set for null hash");
        assertTrue(peersForNullHash.isEmpty(), "Peer set for null hash should be empty");

        // Test null peer ID with valid hash
        server.hostFile(testFile);
        server.registerPeer("hash123", null);
        Set<String> peers = server.getActivePeers("hash123");
        assertTrue(peers.isEmpty(), "Should not register null peer ID");
    }

    @Test
    void shouldReturnEmptySetForNullFile() {
        server.hostFile(null); // Should not throw exception
        Set<String> peers = server.getActivePeers("any-hash");
        assertNotNull(peers, "Should return empty set after hosting null file");
        assertTrue(peers.isEmpty(), "Should have no peers after hosting null file");
    }
}