package base;

public class Computer extends Router {
    protected double maxUploadSpeed;
    protected double maxDownloadSpeed;
    protected long storageCapacity;
    protected long usedStorage;

    public Computer(String deviceID, String ipAddress, String location,
                    int bandwidth, double maxUploadSpeed, double maxDownloadSpeed, long storageCapacity) {
        super(deviceID, ipAddress, location, bandwidth);
        this.maxUploadSpeed = maxUploadSpeed;
        this.maxDownloadSpeed = maxDownloadSpeed;
        this.storageCapacity = storageCapacity;
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

    protected boolean hasStorageSpace(long requiredSpace) {
        return (usedStorage + requiredSpace) <= storageCapacity;
    }
}