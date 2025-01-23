package test.build;

import torrent.TorrentClient;
import model.TorrentFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class TorrentClientTest {
    private TorrentClient client;
    private TorrentClient peer;
    private TorrentFile testFile;
    private static final int PIECE_SIZE = 262144; // 256KB

    @BeforeEach
    void setUp() {
        client = new TorrentClient("CLIENT1", "192.168.1.10", "NYC",
                1000, 50.0, 100.0, 10000000L);
        peer = new TorrentClient("PEER1", "192.168.1.11", "LAX",
                1000, 50.0, 100.0, 10000000L);
        testFile = new TorrentFile("hash123", "test.mp4", 1048576L, PIECE_SIZE); // 1MB file
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("CLIENT1", client.getDeviceID());
        assertEquals("192.168.1.10", client.getIpAddress());
        assertEquals(50.0, client.getMaxUploadSpeed());
        assertEquals(100.0, client.getMaxDownloadSpeed());
        assertEquals(10000000L, client.getStorageCapacity());
        assertEquals(0L, client.getUsedStorage());
    }

    @Test
    void initializeDownloadShouldHandleValidFile() {
        client.initializeDownload(testFile);
        TorrentFile downloadingFile = client.getDownloadingFile("hash123");
        assertNotNull(downloadingFile);
        assertEquals("test.mp4", downloadingFile.getFileName());
        assertEquals(0.0, client.getDownloadProgress("hash123"));
        assertEquals(0, client.getCompletedPieceCount("hash123"));
        assertEquals(4, client.getTotalPieceCount("hash123"));
    }

    @Test
    void initializeDownloadShouldHandleInsufficientStorage() {
        TorrentClient smallClient = new TorrentClient("SMALL", "192.168.1.12", "CHI",
                1000, 50.0, 100.0, 500000L); // Only 500KB storage
        smallClient.initializeDownload(testFile); // Trying to download 1MB file
        assertNull(smallClient.getDownloadingFile("hash123"));
    }

    @Test
    void shouldRequirePeerToHavePiece() {
        // Setup clients
        client.initializeDownload(testFile);
        peer.initializeDownload(testFile);

        // Peer doesn't have the piece yet
        assertFalse(client.requestPiece("hash123", 0, peer),
                "Request should fail if peer doesn't have the piece");

        // Give peer the piece
        peer.getDownloadingFile("hash123").markPieceCompleted(0);

        assertTrue(peer.getDownloadingFile("hash123").isPieceCompleted(0));

        // Now the request should succeed
        assertTrue(client.requestPiece("hash123", 0, peer),
                "Request should succeed when peer has the piece");
    }

    @Test
    void shouldHandleInvalidPieceRequests() {
        client.initializeDownload(testFile);
        peer.initializeDownload(testFile);

        // Test piece index boundaries
        assertFalse(client.requestPiece("hash123", -1, peer),
                "Negative piece index should fail");
        assertFalse(client.requestPiece("hash123", 4, peer),
                "Piece index beyond file size should fail");

        // Test invalid parameters
        assertFalse(client.requestPiece("nonexistent", 0, peer),
                "Non-existent file hash should fail");
        assertFalse(client.requestPiece("hash123", 0, null),
                "Null peer should fail");
        assertFalse(client.requestPiece(null, 0, peer),
                "Null hash should fail");
    }

    @Test
    void shouldGetNextNeededPiece() {
        client.initializeDownload(testFile);

        assertEquals(0, client.getNextNeededPiece("hash123"),
                "First needed piece should be 0");

        client.getDownloadingFile("hash123").markPieceCompleted(0);
        assertEquals(1, client.getNextNeededPiece("hash123"),
                "Next needed piece should be 1 after completing piece 0");

        // Complete all pieces
        for (int i = 1; i < 4; i++) {
            client.getDownloadingFile("hash123").markPieceCompleted(i);
        }
        assertEquals(-1, client.getNextNeededPiece("hash123"),
                "Should return -1 when all pieces are complete");
    }

    @Test
    void shouldHandleNonExistentFile() {
        assertNull(client.getDownloadingFile("nonexistent"));
        assertEquals(0.0, client.getDownloadProgress("nonexistent"));
        assertEquals(0, client.getCompletedPieceCount("nonexistent"));
        assertEquals(0, client.getTotalPieceCount("nonexistent"));
        assertEquals(-1, client.getNextNeededPiece("nonexistent"));
        assertFalse(client.isDownloadComplete("nonexistent"));
    }
}