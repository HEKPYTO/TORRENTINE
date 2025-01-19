package test.solution;

import base.torrent.TorrentClient;
import model.TorrentFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class TorrentClientSolutionTest {
    private TorrentClient client;
    private TorrentClient peer;
    private TorrentFile testFile;
    private static final int PIECE_SIZE = 262144;

    @BeforeEach
    void setUp() {
        client = new TorrentClient("CLI001", "192.168.1.1", "NYC", 1000, 50.0, 100.0, 1000000L);
        peer = new TorrentClient("CLI002", "192.168.1.2", "NYC", 1000, 50.0, 100.0, 1000000L);
        testFile = new TorrentFile("hash123", "test.mp4", 524288L, PIECE_SIZE);
    }

    @Test
    void shouldManageDownloadInitialization() {
        client.initializeDownload(testFile);
        TorrentFile downloadingFile = client.getDownloadingFile(testFile.getInfoHash());

        assertNotNull(downloadingFile);
        assertEquals(testFile.getFileName(), downloadingFile.getFileName());
        assertEquals(testFile.getFileSize(), downloadingFile.getFileSize());
        assertEquals(testFile.getPieceSize(), downloadingFile.getPieceSize());
        assertEquals(0.0, client.getDownloadProgress(testFile.getInfoHash()));
        assertEquals(0, client.getCompletedPieceCount(testFile.getInfoHash()));
    }

    @Test
    void shouldHandlePieceTransfer() {
        client.initializeDownload(testFile);
        peer.initializeDownload(testFile);

        peer.getDownloadingFile(testFile.getInfoHash()).markPieceCompleted(0);

        assertTrue(client.requestPiece(testFile.getInfoHash(), 0, peer));
        assertTrue(client.getDownloadingFile(testFile.getInfoHash()).isPieceCompleted(0));

        assertEquals(0.5, client.getDownloadProgress(testFile.getInfoHash()));
        assertEquals(1, client.getCompletedPieceCount(testFile.getInfoHash()));
        assertEquals(1, client.getNextNeededPiece(testFile.getInfoHash()));
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

        assertEquals(2, client.getCompletedPieceCount(testFile.getInfoHash()));
        assertTrue(client.isDownloadComplete(testFile.getInfoHash()));
    }

    @Test
    void shouldRequireOnlineStatus() {
        client.initializeDownload(testFile);
        peer.initializeDownload(testFile);
        peer.getDownloadingFile(testFile.getInfoHash()).markPieceCompleted(0);

        client.setOnline(false);
        assertFalse(client.requestPiece(testFile.getInfoHash(), 0, peer));

        client.setOnline(true);
        peer.setOnline(false);
        assertFalse(client.requestPiece(testFile.getInfoHash(), 0, peer));

        peer.setOnline(true);
        assertTrue(client.requestPiece(testFile.getInfoHash(), 0, peer));
    }

    @Test
    void shouldManageMultipleFiles() {
        TorrentFile testFile2 = new TorrentFile("hash456", "test2.mp4", 524288L, PIECE_SIZE);

        client.initializeDownload(testFile);
        client.initializeDownload(testFile2);

        assertEquals(0.0, client.getDownloadProgress(testFile.getInfoHash()));
        assertEquals(0.0, client.getDownloadProgress(testFile2.getInfoHash()));

        TorrentFile file1 = client.getDownloadingFile(testFile.getInfoHash());
        for (int i = 0; i < file1.getPieceCount(); i++) {
            file1.markPieceCompleted(i);
        }

        assertTrue(client.isDownloadComplete(testFile.getInfoHash()));
        assertFalse(client.isDownloadComplete(testFile2.getInfoHash()));
    }
}