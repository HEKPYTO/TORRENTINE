package model;

public class Piece {
    private final int index;
    private final int size;
    private boolean downloaded;
    private boolean verified;

    public Piece(int index, int size) {
        this.index = index;
        this.size = size;
        this.downloaded = false;
        this.verified = false;
    }

    public int getIndex() { return index; }
    public int getSize() { return size; }
    public boolean isDownloaded() { return downloaded; }
    public boolean isVerified() { return verified; }

    public void setDownloaded(boolean status) {
        this.downloaded = status;
    }

    public void setVerified(boolean status) {
        this.verified = status;
    }
}
