package model;

import java.util.HashSet;
import java.util.Set;

public class SwarmInfo {
    private final String infoHash;
    private int numSeeders;
    private int numLeechers;
    private final Set<String> activePeers;
    private long totalTransferred;

    public SwarmInfo(String infoHash) {
        this.infoHash = infoHash;
        this.numSeeders = 0;
        this.numLeechers = 0;
        this.activePeers = new HashSet<>();
        this.totalTransferred = 0;
    }

    public void addPeer(String peerId, boolean isSeeder) {
        activePeers.add(peerId);
        if (isSeeder) {
            numSeeders++;
        } else {
            numLeechers++;
        }
    }

    public void removePeer(String peerId, boolean wasSeeder) {
        activePeers.remove(peerId);
        if (wasSeeder) {
            numSeeders--;
        } else {
            numLeechers--;
        }
    }

    public void updateTransferred(long bytes) {
        totalTransferred += bytes;
    }

    // Getters
    public int getNumSeeders() { return numSeeders; }
    public int getNumLeechers() { return numLeechers; }
    public Set<String> getActivePeers() { return new HashSet<>(activePeers); }
    public long getTotalTransferred() { return totalTransferred; }
}