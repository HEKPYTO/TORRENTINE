package base.torrent;

import base.Computer;
import model.PeerStatus;
import model.Piece;
import model.TorrentFile;

import java.util.*;

public class TorrentClient extends Computer {
    private final Map<String, TorrentFile> downloadingFiles;
    private final Map<String, PeerStatus> peerStatuses;
    private final Random random;

    public TorrentClient(String deviceID, String ipAddress, String location,
                         int bandwidth, double maxUploadSpeed, double maxDownloadSpeed,
                         long storageCapacity) {
        super(deviceID, ipAddress, location, bandwidth, maxUploadSpeed, maxDownloadSpeed, storageCapacity);
        this.downloadingFiles = new HashMap<>();
        this.peerStatuses = new HashMap<>();
        this.random = new Random();
    }

    public void initializeDownload(TorrentFile file) {
        if (hasStorageSpace(file.getFileSize())) {
            // Create a new instance of TorrentFile
            TorrentFile newFile = new TorrentFile(
                    file.getInfoHash(),
                    file.getFileName(),
                    file.getFileSize(),
                    file.getPieceSize()
            );
            downloadingFiles.put(file.getInfoHash(), newFile);
            System.out.println(getDeviceID() + " started for " + file.getInfoHash());
            System.out.println("Started downloading: " + file.getFileName());
        } else {
            System.out.println("Insufficient storage for: " + file.getFileName());
        }
    }

    public boolean requestPiece(String infoHash, int pieceIndex, TorrentClient peer) {
        // Validate inputs
        if (peer == null || infoHash == null) {
            return false;
        }

        // Get files from both sides
        TorrentFile localFile = downloadingFiles.get(infoHash);
        TorrentFile peerFile = peer.getDownloadingFile(infoHash);

        if (localFile == null || peerFile == null) {
            return false;
        }

        // Validate piece index
        if (pieceIndex < 0 || pieceIndex >= localFile.getPieceCount()) {
            return false;
        }

        // Check if we already have the piece
        if (localFile.isPieceCompleted(pieceIndex)) {
            return false;
        }

        // Check if peer has the piece
        if (!peerFile.isPieceCompleted(pieceIndex)) {
            return false;
        }

        // Get the piece and simulate transfer
        Piece piece = localFile.getPieces().get(pieceIndex);
        if (simulateTransfer(piece, peer)) {
            localFile.markPieceCompleted(pieceIndex);
            updatePeerStatus(peer.getDeviceID(), 0, piece.getSize());
            peer.updatePeerStatus(getDeviceID(), piece.getSize(), 0);
            return true;
        }

        return false;
    }

    private boolean simulateTransfer(Piece piece, TorrentClient peer) {
        // Both clients must be online
        if (!isOnline() || !peer.isOnline()) {
            return false;
        }

        // Simple success rate simulation
        double successRate = 1.0; // Always succeed in tests
        return random.nextDouble() < successRate;
    }

    private void updatePeerStatus(String peerId, long uploaded, long downloaded) {
        PeerStatus status = peerStatuses.computeIfAbsent(peerId, k -> new PeerStatus());
        status.updateTransfer(uploaded, downloaded);
    }

    public int getNextNeededPiece(String infoHash) {
        TorrentFile file = downloadingFiles.get(infoHash);
        if (file != null) {
            for (int i = 0; i < file.getPieceCount(); i++) {
                if (!file.isPieceCompleted(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public double getDownloadProgress(String infoHash) {
        TorrentFile file = downloadingFiles.get(infoHash);
        return file != null ? file.getProgress() : 0.0;
    }

    public int getCompletedPieceCount(String infoHash) {
        TorrentFile file = downloadingFiles.get(infoHash);
        return file != null ? file.getCompletedPieceCount() : 0;
    }

    public int getTotalPieceCount(String infoHash) {
        TorrentFile file = downloadingFiles.get(infoHash);
        return file != null ? file.getPieceCount() : 0;
    }

    public double getRealUploadSpeed() {
        return peerStatuses.values().stream()
                .mapToDouble(PeerStatus::getUploadSpeed)
                .sum();
    }

    public double getRealDownloadSpeed() {
        return peerStatuses.values().stream()
                .mapToDouble(PeerStatus::getDownloadSpeed)
                .sum();
    }

    public TorrentFile getDownloadingFile(String infoHash) {
        return downloadingFiles.get(infoHash);
    }

    public boolean isDownloadComplete(String infoHash) {
        TorrentFile file = downloadingFiles.get(infoHash);
        return file != null && file.getProgress() >= 1.0;
    }
}