package test.solution;

import base.torrent.TorrentServer;
import model.TorrentFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

class TorrentServerSolutionTest {
    private TorrentServer server;
    private TorrentFile testFile;
    private static final int PIECE_SIZE = 262144;

    @BeforeEach
    void setUp() {
        server = new TorrentServer("SRV001", "192.168.1.1", "NYC", 1000, 50.0, 100.0, 1000000L);
        testFile = new TorrentFile("hash123", "test.mp4", 524288L, PIECE_SIZE);
    }

    @Test
    void shouldManageFileHosting() {
        server.hostFile(testFile);
        assertTrue(server.getActivePeers(testFile.getInfoHash()).isEmpty());

        server.hostFile(testFile);
        assertTrue(server.getActivePeers(testFile.getInfoHash()).isEmpty(), "Duplicate hosting should not affect existing file");
    }

    @Test
    void shouldManagePeerRegistration() {
        server.hostFile(testFile);
        String infoHash = testFile.getInfoHash();

        server.registerPeer(infoHash, "PEER001");
        Set<String> peers = server.getActivePeers(infoHash);
        assertEquals(1, peers.size());
        assertTrue(peers.contains("PEER001"));

        server.registerPeer(infoHash, "PEER002");
        peers = server.getActivePeers(infoHash);
        assertEquals(2, peers.size());
        assertTrue(peers.contains("PEER002"));

        server.registerPeer(infoHash, "PEER001");
        peers = server.getActivePeers(infoHash);
        assertEquals(2, peers.size(), "Duplicate peer registration should be ignored");
    }

    @Test
    void shouldHandleMultipleFiles() {
        TorrentFile testFile2 = new TorrentFile("hash456", "test2.mp4", 524288L, PIECE_SIZE);

        server.hostFile(testFile);
        server.hostFile(testFile2);

        server.registerPeer(testFile.getInfoHash(), "PEER001");
        server.registerPeer(testFile2.getInfoHash(), "PEER002");

        Set<String> peers1 = server.getActivePeers(testFile.getInfoHash());
        Set<String> peers2 = server.getActivePeers(testFile2.getInfoHash());

        assertEquals(1, peers1.size());
        assertEquals(1, peers2.size());
        assertTrue(peers1.contains("PEER001"));
        assertTrue(peers2.contains("PEER002"));
    }

    @Test
    void shouldProvideDefensiveCopies() {
        server.hostFile(testFile);
        server.registerPeer(testFile.getInfoHash(), "PEER001");

        Set<String> peers = server.getActivePeers(testFile.getInfoHash());
        peers.add("PEER002");

        peers = server.getActivePeers(testFile.getInfoHash());
        assertEquals(1, peers.size());
        assertFalse(peers.contains("PEER002"));
    }

    @Test
    void shouldInheritComputerFunctionality() {
        server.setBandwidth(2000);
        assertEquals(2000, server.getBandwidth());

        assertTrue(server.isOnline());
        server.setOnline(false);
        assertFalse(server.isOnline());

        assertEquals(1000000L, server.getStorageCapacity());
        assertEquals(0L, server.getUsedStorage());
    }
}
