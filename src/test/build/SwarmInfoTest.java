package test.build;

import model.SwarmInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class SwarmInfoTest {
    private SwarmInfo swarmInfo;

    @BeforeEach
    void setUp() {
        swarmInfo = new SwarmInfo("hash123");
    }

    @Test
    void constructorShouldInitializeCorrectly() {
        assertEquals(0, swarmInfo.getNumSeeders());
        assertEquals(0, swarmInfo.getNumLeechers());
        assertTrue(swarmInfo.getActivePeers().isEmpty());
        assertEquals(0L, swarmInfo.getTotalTransferred());
    }

    @Test
    void shouldManageSeeders() {
        swarmInfo.addPeer("peer1", true);
        assertEquals(1, swarmInfo.getNumSeeders());
        assertEquals(0, swarmInfo.getNumLeechers());

        swarmInfo.removePeer("peer1", true);
        assertEquals(0, swarmInfo.getNumSeeders());
    }

    @Test
    void shouldManageLeechers() {
        swarmInfo.addPeer("peer1", false);
        assertEquals(0, swarmInfo.getNumSeeders());
        assertEquals(1, swarmInfo.getNumLeechers());

        swarmInfo.removePeer("peer1", false);
        assertEquals(0, swarmInfo.getNumLeechers());
    }

    @Test
    void shouldTrackActivePeers() {
        swarmInfo.addPeer("peer1", true);
        swarmInfo.addPeer("peer2", false);

        assertEquals(2, swarmInfo.getActivePeers().size());
        assertTrue(swarmInfo.getActivePeers().contains("peer1"));
        assertTrue(swarmInfo.getActivePeers().contains("peer2"));
    }

    @Test
    void shouldUpdateTransferredBytes() {
        swarmInfo.updateTransferred(1000L);
        assertEquals(1000L, swarmInfo.getTotalTransferred());

        swarmInfo.updateTransferred(500L);
        assertEquals(1500L, swarmInfo.getTotalTransferred());
    }

    @Test
    void shouldPreventDuplicatePeers() {
        swarmInfo.addPeer("peer1", true);
        swarmInfo.addPeer("peer1", true);

        assertEquals(1, swarmInfo.getActivePeers().size());
        assertEquals(2, swarmInfo.getNumSeeders()); // Counter still increments
    }
}