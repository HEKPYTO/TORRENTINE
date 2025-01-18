package test.build;

import model.PeerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class PeerStatusTest {
    private PeerStatus status;

    @BeforeEach
    void setUp() {
        status = new PeerStatus();
    }

    @Test
    void constructorShouldInitializeWithZeroSpeeds() {
        assertEquals(0.0, status.getUploadSpeed());
        assertEquals(0.0, status.getDownloadSpeed());
    }

    @Test
    void shouldCalculateSpeedsAfterTransfer() {
        // Add some transfer data
        status.updateTransfer(1048576L, 524288L); // 1MB upload, 512KB download

        // Speeds should be updated
        assertTrue(status.getUploadSpeed() > 0.0);
        assertTrue(status.getDownloadSpeed() > 0.0);
    }

    @Test
    void shouldHandleZeroTransfer() {
        status.updateTransfer(0L, 0L);
        assertEquals(0.0, status.getUploadSpeed());
        assertEquals(0.0, status.getDownloadSpeed());
    }

    @Test
    void shouldUpdateSpeedsOnMultipleTransfers() {
        status.updateTransfer(1048576L, 524288L); // First transfer
        double firstUploadSpeed = status.getUploadSpeed();

        // Sleep briefly to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        status.updateTransfer(1048576L, 524288L); // Second transfer
        double secondUploadSpeed = status.getUploadSpeed();

        // Speeds should be different due to time difference
        assertNotEquals(firstUploadSpeed, secondUploadSpeed);
    }
}
