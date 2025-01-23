package model;

public class PeerStatus {
    private long uploadedBytes;
    private long downloadedBytes;
    private double uploadSpeed;
    private double downloadSpeed;
    private long lastUpdateTime;

    public PeerStatus() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void updateTransfer(long uploadedBytes, long downloadedBytes) {
        this.uploadedBytes += uploadedBytes;
        this.downloadedBytes += downloadedBytes;
        updateSpeeds();
    }

    private void updateSpeeds() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastUpdateTime;
        if (timeDiff > 0) {
            uploadSpeed = (uploadedBytes * 1000.0) / timeDiff;
            downloadSpeed = (downloadedBytes * 1000.0) / timeDiff;
            // Reset counters
            uploadedBytes = 0;
            downloadedBytes = 0;
            lastUpdateTime = currentTime;
        }
    }

    public double getUploadSpeed() { return uploadSpeed; }
    public double getDownloadSpeed() { return downloadSpeed; }
}