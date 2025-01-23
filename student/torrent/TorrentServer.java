package torrent;

import base.Computer;
import model.TorrentFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TorrentServer extends Computer {
    private final Map<String, Set<String>> activePeers;

    public TorrentServer(String deviceID, String ipAddress, String location,
                         int bandwidth, double maxUploadSpeed, double maxDownloadSpeed,
                         long storageCapacity) {
        super(deviceID, ipAddress, location, bandwidth, maxUploadSpeed, maxDownloadSpeed, storageCapacity);
        this.activePeers = new HashMap<>();
    }

    public void hostFile(TorrentFile file) {
        if (file == null) {
            return;
        }
        String infoHash = file.getInfoHash();
        if (infoHash != null && !activePeers.containsKey(infoHash)) {
            activePeers.put(infoHash, new HashSet<>());
        }
    }

    public void registerPeer(String infoHash, String peerId) {
        if (infoHash == null || peerId == null) {
            return;
        }
        activePeers.computeIfPresent(infoHash, (hash, peers) -> {
            peers.add(peerId);
            return peers;
        });
    }

    public Set<String> getActivePeers(String infoHash) {
        if (infoHash == null) {
            return new HashSet<>();
        }
        return new HashSet<>(activePeers.getOrDefault(infoHash, new HashSet<>()));
    }
}