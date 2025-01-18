package test.build;

import model.TorrentFile;
import model.Piece;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class TorrentFileTest {
    private TorrentFile torrentFile;
    private static final int PIECE_SIZE = 262144; // 256KB

    @BeforeEach
    void setUp() {
        torrentFile = new TorrentFile("hash123", "test.mp4", 1048576L, PIECE_SIZE); // 1MB file
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals("hash123", torrentFile.getInfoHash());
        assertEquals("test.mp4", torrentFile.getFileName());
        assertEquals(1048576L, torrentFile.getFileSize());
        assertEquals(PIECE_SIZE, torrentFile.getPieceSize());
        assertEquals(4, torrentFile.getPieceCount()); // 1MB/256KB = 4 pieces
        assertEquals(0, torrentFile.getCompletedPieceCount());
        assertEquals(0.0, torrentFile.getProgress());
    }

    @Test
    void shouldInitializeAllPieces() {
        assertEquals(4, torrentFile.getPieces().size());

        // Verify each piece
        for (int i = 0; i < torrentFile.getPieceCount(); i++) {
            Piece piece = torrentFile.getPieces().get(i);
            assertEquals(i, piece.getIndex());
            assertEquals(PIECE_SIZE, piece.getSize());
            assertFalse(piece.isDownloaded());
            assertFalse(piece.isVerified());
        }
    }

    @Test
    void shouldHandleExactlyDivisibleFileSize() {
        TorrentFile exactFile = new TorrentFile("hash456", "exact.mp4",
                524288L, PIECE_SIZE); // Exactly 2 pieces

        assertEquals(2, exactFile.getPieceCount());
        assertEquals(PIECE_SIZE, exactFile.getPieces().get(0).getSize());
        assertEquals(PIECE_SIZE, exactFile.getPieces().get(1).getSize());
    }

    @Test
    void shouldHandleSmallFileSize() {
        TorrentFile smallFile = new TorrentFile("hash789", "small.mp4",
                1000L, PIECE_SIZE); // Much smaller than piece size

        assertEquals(1, smallFile.getPieceCount());
        assertEquals(1000, smallFile.getPieces().get(0).getSize());
    }

    @Test
    void shouldTrackPieceCompletion() {
        // Initially no pieces completed
        assertFalse(torrentFile.isPieceCompleted(0));
        assertEquals(0, torrentFile.getCompletedPieceCount());

        // Mark first piece completed
        torrentFile.markPieceCompleted(0);
        assertTrue(torrentFile.isPieceCompleted(0));
        assertTrue(torrentFile.getPieces().get(0).isDownloaded());
        assertEquals(1, torrentFile.getCompletedPieceCount());

        // Mark another piece completed
        torrentFile.markPieceCompleted(2);
        assertTrue(torrentFile.isPieceCompleted(2));
        assertTrue(torrentFile.getPieces().get(2).isDownloaded());
        assertEquals(2, torrentFile.getCompletedPieceCount());

        // Verify piece 1 still not completed
        assertFalse(torrentFile.isPieceCompleted(1));
        assertFalse(torrentFile.getPieces().get(1).isDownloaded());
    }

    @Test
    void shouldCalculateProgress() {
        assertEquals(0.0, torrentFile.getProgress());

        torrentFile.markPieceCompleted(0);
        assertEquals(0.25, torrentFile.getProgress());

        torrentFile.markPieceCompleted(1);
        assertEquals(0.5, torrentFile.getProgress());

        torrentFile.markPieceCompleted(2);
        torrentFile.markPieceCompleted(3);
        assertEquals(1.0, torrentFile.getProgress());
    }

    @Test
    void shouldHandleInvalidPieceIndices() {
        // Invalid indices should be ignored for marking
        torrentFile.markPieceCompleted(-1);
        torrentFile.markPieceCompleted(4);
        assertEquals(0, torrentFile.getCompletedPieceCount());

        // Invalid indices should return false for completion check
        assertFalse(torrentFile.isPieceCompleted(-1));
        assertFalse(torrentFile.isPieceCompleted(4));

        // Valid operations should still work
        torrentFile.markPieceCompleted(0);
        assertTrue(torrentFile.isPieceCompleted(0));
        assertEquals(1, torrentFile.getCompletedPieceCount());
    }

    @Test
    void shouldProvideImmutablePieceList() {
        assertThrows(UnsupportedOperationException.class, () -> {
            torrentFile.getPieces().add(new Piece(4, PIECE_SIZE));
        });
    }
}