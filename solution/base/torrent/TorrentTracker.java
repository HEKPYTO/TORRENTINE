package base.torrent;

import base.Computer;
import model.TorrentFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TorrentTracker extends Computer {
    private final Map<String, Set<String>> peers;
    private final Map<String, TorrentFile> trackedFiles;

    public TorrentTracker(String deviceID, String ipAddress, String location,
                          int bandwidth, double maxUploadSpeed, double maxDownloadSpeed,
                          long storageCapacity) {
        super(deviceID, ipAddress, location, bandwidth, maxUploadSpeed, maxDownloadSpeed, storageCapacity);
        this.peers = new HashMap<>();
        this.trackedFiles = new HashMap<>();
    }

    public void trackFile(TorrentFile file) {
        if (file == null) {
            return;
        }
        String infoHash = file.getInfoHash();
        if (infoHash != null && !peers.containsKey(infoHash)) {
            peers.put(infoHash, new HashSet<>());
            trackedFiles.put(infoHash, file);
        }
    }

    public void announce(String infoHash, String peerId, String event) {
        if (infoHash == null || peerId == null) {
            return;
        }
        peers.computeIfPresent(infoHash, (hash, peerSet) -> {
            peerSet.add(peerId);
            return peerSet;
        });
    }

    public Set<String> getPeers(String infoHash) {
        if (infoHash == null) {
            return new HashSet<>();
        }
        return new HashSet<>(peers.getOrDefault(infoHash, new HashSet<>()));
    }

    public TorrentFile getTrackedFile(String infoHash) {
        if (infoHash == null) {
            return null;
        }
        return trackedFiles.get(infoHash);
    }

    public boolean isTracked(String infoHash) {
        if (infoHash == null) {
            return false;
        }
        return trackedFiles.containsKey(infoHash);
    }
}