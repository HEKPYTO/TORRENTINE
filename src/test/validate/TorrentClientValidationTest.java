package test.validate;

import base.torrent.TorrentClient;
import model.TorrentFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class TorrentClientValidationTest {
    private TorrentClient client;
    private TorrentClient peer;
    private TorrentFile testFile;
    private static final int PIECE_SIZE = 262144;

    @BeforeEach
    void setUp() {
        client = new TorrentClient("TOR001", "172.16.1.10", "SFO",
                2000, 75.0, 150.0, 20000000L);
        peer = new TorrentClient("TOR002", "172.16.1.11", "LAX",
                2000, 75.0, 150.0, 20000000L);
        testFile = new TorrentFile("hash123", "sample.mkv", 2097152L, PIECE_SIZE);
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("TOR001", client.getDeviceID());
        assertEquals("172.16.1.10", client.getIpAddress());
        assertEquals("SFO", client.getLocation());
        assertTrue(client.isOnline());
        assertEquals(2000, client.getBandwidth());
        assertEquals(75.0, client.getMaxUploadSpeed());
        assertEquals(150.0, client.getMaxDownloadSpeed());
        assertEquals(20000000L, client.getStorageCapacity());
        assertEquals(0L, client.getUsedStorage());

        assertNull(client.getDownloadingFile("hash123"));
        assertEquals(0.0, client.getDownloadProgress("hash123"));
        assertEquals(0, client.getCompletedPieceCount("hash123"));
        assertEquals(0, client.getTotalPieceCount("hash123"));
        assertEquals(-1, client.getNextNeededPiece("hash123"));
        assertEquals(0.0, client.getRealUploadSpeed());
        assertEquals(0.0, client.getRealDownloadSpeed());
    }

    @Test
    void shouldHandleDownloadInitialization() {
        client.initializeDownload(testFile);
        TorrentFile downloadingFile = client.getDownloadingFile("hash123");
        assertNotNull(downloadingFile);
        assertEquals("sample.mkv", downloadingFile.getFileName());
        assertEquals(2097152L, downloadingFile.getFileSize());
        assertEquals(PIECE_SIZE, downloadingFile.getPieceSize());
        assertEquals(0.0, client.getDownloadProgress("hash123"));
        assertEquals(0, client.getCompletedPieceCount("hash123"));
        assertEquals(8, client.getTotalPieceCount("hash123"));

        client.initializeDownload(testFile);
        assertEquals(downloadingFile.getInfoHash(),
                client.getDownloadingFile("hash123").getInfoHash(),
                "Re-initialization should not affect existing file");

        TorrentClient smallClient = new TorrentClient("TOR003", "172.16.1.12", "PDX",
                2000, 75.0, 150.0, 1048576L);
        smallClient.initializeDownload(testFile);
        assertNull(smallClient.getDownloadingFile("hash123"));
    }

    @Test
    void shouldHandlePieceTransfers() {
        client.initializeDownload(testFile);
        peer.initializeDownload(testFile);

        assertFalse(client.requestPiece(null, 0, peer), "Null hash should fail");
        assertFalse(client.requestPiece("hash123", 0, null), "Null peer should fail");
        assertFalse(client.requestPiece("nonexistent", 0, peer), "Invalid hash should fail");
        assertFalse(client.requestPiece("hash123", -1, peer), "Negative index should fail");
        assertFalse(client.requestPiece("hash123", 8, peer), "Out of bounds index should fail");

        assertFalse(client.requestPiece("hash123", 0, peer), "Should fail if peer doesn't have piece");
        peer.getDownloadingFile("hash123").markPieceCompleted(0);
        assertTrue(client.requestPiece("hash123", 0, peer), "Should succeed if peer has piece");

        assertFalse(client.requestPiece("hash123", 0, peer), "Should fail for already completed piece");

        peer.getDownloadingFile("hash123").markPieceCompleted(1);
        client.setOnline(false);
        assertFalse(client.requestPiece("hash123", 1, peer), "Should fail if client is offline");

        client.setOnline(true);
        peer.setOnline(false);
        assertFalse(client.requestPiece("hash123", 1, peer), "Should fail if peer is offline");
    }

    @Test
    void shouldTrackDownloadProgress() {
        client.initializeDownload(testFile);
        peer.initializeDownload(testFile);

        for (int i = 0; i < 8; i++) {
            peer.getDownloadingFile("hash123").markPieceCompleted(i);
        }

        assertEquals(0.0, client.getDownloadProgress("hash123"), "Initial progress should be 0");
        client.requestPiece("hash123", 0, peer);
        assertEquals(0.125, client.getDownloadProgress("hash123"), "Progress after 1 piece");
        client.requestPiece("hash123", 1, peer);
        assertEquals(0.25, client.getDownloadProgress("hash123"), "Progress after 2 pieces");

        for (int i = 2; i < 8; i++) {
            client.requestPiece("hash123", i, peer);
        }
        assertEquals(1.0, client.getDownloadProgress("hash123"), "Final progress should be 1");
        assertTrue(client.isDownloadComplete("hash123"), "Download should be complete");
    }

    @Test
    void shouldManagePieceTracking() {
        client.initializeDownload(testFile);

        assertEquals(0, client.getNextNeededPiece("hash123"), "Should need piece 0 initially");
        assertEquals(0, client.getCompletedPieceCount("hash123"), "Should have no completed pieces");
        assertEquals(8, client.getTotalPieceCount("hash123"), "Should have correct total pieces");

        client.getDownloadingFile("hash123").markPieceCompleted(0);
        assertEquals(1, client.getNextNeededPiece("hash123"), "Should need piece 1 next");
        assertEquals(1, client.getCompletedPieceCount("hash123"), "Should have one completed piece");

        for (int i = 1; i < 8; i++) {
            client.getDownloadingFile("hash123").markPieceCompleted(i);
            if (i < 7) {
                assertEquals(i + 1, client.getNextNeededPiece("hash123"),
                        "Should need correct next piece after completing piece " + i);
            }
        }

        assertEquals(-1, client.getNextNeededPiece("hash123"), "Should need no pieces when complete");
        assertEquals(8, client.getCompletedPieceCount("hash123"), "Should have all pieces completed");
        assertTrue(client.isDownloadComplete("hash123"), "Should be marked as complete");
    }

    @Test
    void shouldHandleNonExistentFiles() {
        assertNull(client.getDownloadingFile("nonexistent"));
        assertEquals(0.0, client.getDownloadProgress("nonexistent"));
        assertEquals(0, client.getCompletedPieceCount("nonexistent"));
        assertEquals(0, client.getTotalPieceCount("nonexistent"));
        assertEquals(-1, client.getNextNeededPiece("nonexistent"));
        assertFalse(client.isDownloadComplete("nonexistent"));

        assertNull(client.getDownloadingFile(""));
        assertEquals(0.0, client.getDownloadProgress(""));
        assertEquals(0, client.getCompletedPieceCount(""));
        assertEquals(0, client.getTotalPieceCount(""));
        assertEquals(-1, client.getNextNeededPiece(""));
        assertFalse(client.isDownloadComplete(""));

        assertNull(client.getDownloadingFile(null));
        assertEquals(0.0, client.getDownloadProgress(null));
        assertEquals(0, client.getCompletedPieceCount(null));
        assertEquals(0, client.getTotalPieceCount(null));
        assertEquals(-1, client.getNextNeededPiece(null));
        assertFalse(client.isDownloadComplete(null));
    }

    @Test
    void shouldTrackPeerStatus() throws InterruptedException {
        client.initializeDownload(testFile);
        peer.initializeDownload(testFile);

        peer.getDownloadingFile(testFile.getInfoHash()).markPieceCompleted(0);
        peer.getDownloadingFile(testFile.getInfoHash()).markPieceCompleted(1);

        assertTrue(client.requestPiece(testFile.getInfoHash(), 0, peer));
        Thread.sleep(100);

        assertTrue(client.requestPiece(testFile.getInfoHash(), 1, peer));
        Thread.sleep(100);

        assertTrue(client.getRealDownloadSpeed() > 0, "Client download speed should be positive");
        assertTrue(peer.getRealUploadSpeed() > 0, "Peer upload speed should be positive");
    }
}
