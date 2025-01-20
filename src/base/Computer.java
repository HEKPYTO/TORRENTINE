package base;

public class Computer extends Router {
    protected final double maxUploadSpeed;
    protected final double maxDownloadSpeed;
    protected long storageCapacity;
    protected long usedStorage;

    public Computer(String deviceID, String ipAddress, String location,
                    int bandwidth, double maxUploadSpeed, double maxDownloadSpeed, long storageCapacity) {
        super(deviceID, ipAddress, location, bandwidth);
        this.maxUploadSpeed = Math.max(maxUploadSpeed, 0);
        this.maxDownloadSpeed = Math.max(maxDownloadSpeed, 0);
        this.storageCapacity = Math.max(storageCapacity, 0);
        this.usedStorage = 0;
    }

    public double getMaxUploadSpeed() {
        return maxUploadSpeed;
    }

    public double getMaxDownloadSpeed() {
        return maxDownloadSpeed;
    }

    public long getStorageCapacity() {
        return storageCapacity;
    }

    public long getUsedStorage() {
        return usedStorage;
    }

    public void setStorageCapacity(long storageCapacity) {
        this.storageCapacity = Math.max(storageCapacity, 0);
    }

    public void setUsedStorage(long usedStorage) {
        this.usedStorage = Math.min(Math.max(usedStorage, 0), storageCapacity);
    }

    public boolean hasStorageSpace(long requiredSpace) {
        return (usedStorage + requiredSpace) <= storageCapacity;
    }
}