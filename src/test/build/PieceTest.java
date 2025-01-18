package test.build;

import model.Piece;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class PieceTest {
    private Piece piece;

    @BeforeEach
    void setUp() {
        piece = new Piece(1, 262144); // 256KB piece size
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals(1, piece.getIndex());
        assertEquals(262144, piece.getSize());
        assertFalse(piece.isDownloaded());
        assertFalse(piece.isVerified());
    }

    @Test
    void shouldUpdateDownloadStatus() {
        piece.setDownloaded(true);
        assertTrue(piece.isDownloaded());

        piece.setDownloaded(false);
        assertFalse(piece.isDownloaded());
    }

    @Test
    void shouldUpdateVerificationStatus() {
        piece.setVerified(true);
        assertTrue(piece.isVerified());

        piece.setVerified(false);
        assertFalse(piece.isVerified());
    }

    @Test
    void statusUpdatesShouldBeIndependent() {
        piece.setDownloaded(true);
        assertFalse(piece.isVerified());

        piece.setVerified(true);
        assertTrue(piece.isDownloaded());
        assertTrue(piece.isVerified());
    }
}