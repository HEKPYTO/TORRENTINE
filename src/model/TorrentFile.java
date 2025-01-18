package model;

import java.util.*;

public class TorrentFile {
    private final String infoHash;
    private final String fileName;
    private final long fileSize;
    private final int pieceSize;
    private List<Piece> pieces;
    private BitSet completedPieces;

    public TorrentFile(String infoHash, String fileName, long fileSize, int pieceSize) {
        this.infoHash = infoHash;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        initializePieces();
    }

    private void initializePieces() {
        int pieceCount = (int) Math.ceil((double) fileSize / pieceSize);
        pieces = new ArrayList<>(pieceCount);
        completedPieces = new BitSet(pieceCount);

        for (int i = 0; i < pieceCount; i++) {
            int actualSize = (i == pieceCount - 1) ?
                    (int) (fileSize % pieceSize) : pieceSize;
            if (actualSize == 0) {
                actualSize = pieceSize;  // Handle case where fileSize is exactly divisible by pieceSize
            }
            pieces.add(new Piece(i, actualSize));
        }
    }

    public String getInfoHash() { return infoHash; }
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public int getPieceSize() { return pieceSize; }
    public List<Piece> getPieces() { return Collections.unmodifiableList(pieces); }
    public int getPieceCount() { return pieces.size(); }

    public void markPieceCompleted(int index) {
        if (!isValidPieceIndex(index)) {
            return;
        }
        completedPieces.set(index);
        pieces.get(index).setDownloaded(true);
    }

    public boolean isPieceCompleted(int index) {
        if (!isValidPieceIndex(index)) {
            return false;
        }
        return completedPieces.get(index);
    }

    private boolean isValidPieceIndex(int index) {
        return index >= 0 && index < pieces.size();
    }

    public int getCompletedPieceCount() {
        return completedPieces.cardinality();
    }

    public double getProgress() {
        if (getPieceCount() == 0) {
            return 0.0;
        }
        return (double) getCompletedPieceCount() / getPieceCount();
    }
}