package test.validate;

import base.torrent.TorrentServer;
import model.TorrentFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TorrentServerValidationTest {
    private TorrentServer server;
    private TorrentFile testFile;
    private static final int PIECE_SIZE = 262144;

    @BeforeEach
    void setUp() {
        server = new TorrentServer("TSV001", "172.16.1.1", "SFO",
                2000, 150.0, 150.0, 20000000L);
        testFile = new TorrentFile("hash123", "sample.mkv", 2097152L, PIECE_SIZE);
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("TSV001", server.getDeviceID(), "Device ID should match");
        assertEquals("172.16.1.1", server.getIpAddress(), "IP address should match");
        assertEquals("SFO", server.getLocation(), "Location should match");
        assertTrue(server.isOnline(), "Server should be initially online");
        assertEquals(2000, server.getBandwidth(), "Bandwidth should match");
        assertEquals(150.0, server.getMaxUploadSpeed(), "Upload speed should match");
        assertEquals(150.0, server.getMaxDownloadSpeed(), "Download speed should match");
        assertEquals(20000000L, server.getStorageCapacity(), "Storage capacity should match");

        assertTrue(server.getActivePeers("hash123").isEmpty(), "Initial peer set should be empty");
        assertTrue(server.getActivePeers(null).isEmpty(), "Null hash should return empty set");
    }

    @Test
    void shouldHandleFileHosting() {
        server.hostFile(testFile);
        Set<String> peers = server.getActivePeers("hash123");
        assertNotNull(peers, "Peer set should be initialized");
        assertTrue(peers.isEmpty(), "Initial peer set should be empty");

        server.hostFile(testFile);
        peers = server.getActivePeers("hash123");
        assertTrue(peers.isEmpty(), "Duplicate hosting should not affect peer set");

        TorrentFile testFile2 = new TorrentFile("hash456", "movie.mkv", 3145728L, PIECE_SIZE);
        server.hostFile(testFile2);
        Set<String> peers2 = server.getActivePeers("hash456");
        assertNotNull(peers2, "Second file peer set should be initialized");
        assertTrue(peers2.isEmpty(), "Second file peer set should be empty");
    }

    @Test
    void shouldHandleInvalidFileHosting() {
        server.hostFile(null);
        Set<String> peers = server.getActivePeers("any-hash");
        assertTrue(peers.isEmpty(), "Null file should not create peer set");

        TorrentFile invalidFile = new TorrentFile(null, "invalid.mkv", 1048576L, PIECE_SIZE);
        server.hostFile(invalidFile);
        peers = server.getActivePeers(null);
        assertTrue(peers.isEmpty(), "File with null hash should not create peer set");

        TorrentFile emptyHashFile = new TorrentFile("", "empty.mkv", 1048576L, PIECE_SIZE);
        server.hostFile(emptyHashFile);
        peers = server.getActivePeers("");
        assertTrue(peers.isEmpty(), "File with empty hash should not create peer set");
    }

    @Test
    void shouldManagePeerRegistration() {
        server.hostFile(testFile);
        String infoHash = testFile.getInfoHash();

        server.registerPeer(infoHash, "PEER001");
        Set<String> peers = server.getActivePeers(infoHash);
        assertEquals(1, peers.size(), "Should have one registered peer");
        assertTrue(peers.contains("PEER001"), "Should contain registered peer");

        server.registerPeer(infoHash, "PEER002");
        server.registerPeer(infoHash, "PEER003");
        peers = server.getActivePeers(infoHash);
        assertEquals(3, peers.size(), "Should have three registered peers");
        assertTrue(peers.contains("PEER002"), "Should contain second peer");
        assertTrue(peers.contains("PEER003"), "Should contain third peer");

        server.registerPeer(infoHash, "PEER001");
        peers = server.getActivePeers(infoHash);
        assertEquals(3, peers.size(), "Duplicate registration should be ignored");
    }

    @Test
    void shouldHandleInvalidPeerRegistration() {
        server.hostFile(testFile);
        String infoHash = testFile.getInfoHash();

        server.registerPeer(infoHash, null);
        assertTrue(server.getActivePeers(infoHash).isEmpty(), "Null peer ID should not be registered");

        server.registerPeer(null, "PEER001");
        assertTrue(server.getActivePeers(null).isEmpty(), "Null info hash should not register peer");

        server.registerPeer("unknown", "PEER001");
        assertTrue(server.getActivePeers("unknown").isEmpty(), "Unknown hash should not register peer");
    }

    @Test
    void shouldManageMultipleFiles() {
        TorrentFile testFile2 = new TorrentFile("hash456", "video.mkv", 3145728L, PIECE_SIZE);
        TorrentFile testFile3 = new TorrentFile("hash789", "audio.mkv", 1048576L, PIECE_SIZE);

        server.hostFile(testFile);
        server.hostFile(testFile2);
        server.hostFile(testFile3);

        server.registerPeer("hash123", "PEER001");
        server.registerPeer("hash456", "PEER002");
        server.registerPeer("hash789", "PEER003");

        Set<String> peers1 = server.getActivePeers("hash123");
        Set<String> peers2 = server.getActivePeers("hash456");
        Set<String> peers3 = server.getActivePeers("hash789");

        assertEquals(1, peers1.size(), "First file should have one peer");
        assertEquals(1, peers2.size(), "Second file should have one peer");
        assertEquals(1, peers3.size(), "Third file should have one peer");
        assertTrue(peers1.contains("PEER001"), "First file should have correct peer");
        assertTrue(peers2.contains("PEER002"), "Second file should have correct peer");
        assertTrue(peers3.contains("PEER003"), "Third file should have correct peer");

        server.registerPeer("hash123", "PEER002");
        server.registerPeer("hash456", "PEER003");
        server.registerPeer("hash789", "PEER001");

        assertEquals(2, server.getActivePeers("hash123").size(), "First file should have two peers");
        assertEquals(2, server.getActivePeers("hash456").size(), "Second file should have two peers");
        assertEquals(2, server.getActivePeers("hash789").size(), "Third file should have two peers");
    }

    @Test
    void shouldProvideDefensiveCopies() {
        server.hostFile(testFile);
        server.registerPeer("hash123", "PEER001");

        Set<String> peers = server.getActivePeers("hash123");
        peers.add("PEER002");
        peers.remove("PEER001");
        peers.clear();

        Set<String> actualPeers = server.getActivePeers("hash123");
        assertEquals(1, actualPeers.size(), "Original set size should be unchanged");
        assertTrue(actualPeers.contains("PEER001"), "Original peer should still exist");
        assertFalse(actualPeers.contains("PEER002"), "Added peer should not appear in original");
    }

    @Test
    void shouldInheritComputerFunctionality() {
        assertTrue(server.isOnline(), "Should be initially online");
        server.setOnline(false);
        assertFalse(server.isOnline(), "Should be offline after setting");
        server.setOnline(true);
        assertTrue(server.isOnline(), "Should be online after resetting");

        server.setBandwidth(3000);
        assertEquals(3000, server.getBandwidth(), "Should update bandwidth");
        server.setBandwidth(-1);
        assertEquals(0, server.getBandwidth(), "Should handle negative bandwidth");

        assertTrue(server.hasStorageSpace(10000000L), "Should have sufficient storage");
        assertFalse(server.hasStorageSpace(30000000L), "Should detect insufficient storage");
    }
}