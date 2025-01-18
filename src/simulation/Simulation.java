package simulation;

import base.torrent.*;
import model.*;
import java.util.*;

public class Simulation {
    private static final int PIECE_SIZE = 262144; // 256KB
    private static final long FILE_SIZE = 10_485_760L; // 10MB
    private static final int SIMULATION_CYCLES = 20;
    private static final long SLEEP_TIME = 1000; // 1 second between cycles

    private final TorrentTracker tracker;
    private final TorrentClient initialSeeder;  // Changed to TorrentClient
    private final List<TorrentClient> peers;
    private final TorrentFile torrentFile;
    private final Random random;

    public Simulation() {
        this.random = new Random();
        this.tracker = createTracker();
        this.initialSeeder = createInitialSeeder();  // Changed method name
        this.peers = createPeers(3); // Start with 3 peers
        this.torrentFile = createTorrentFile();
        initializeNetwork();
    }

    private TorrentTracker createTracker() {
        return new TorrentTracker("TRK001", "10.0.0.1", "NYC",
                1000, 100.0, 100.0, Long.MAX_VALUE);
    }

    private TorrentClient createInitialSeeder() {
        TorrentClient seeder = new TorrentClient("SEED001", "10.0.0.2", "LAX",
                500, 50.0, 50.0, Long.MAX_VALUE);
        return seeder;
    }

    private List<TorrentClient> createPeers(int count) {
        List<TorrentClient> newPeers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TorrentClient peer = new TorrentClient(
                    String.format("PEER%03d", i + 1),
                    String.format("10.0.1.%d", i + 1),
                    "LOCATION" + (i + 1),
                    100,
                    10.0,
                    20.0,
                    FILE_SIZE * 2
            );
            newPeers.add(peer);
        }
        return newPeers;
    }

    private TorrentFile createTorrentFile() {
        return new TorrentFile("HASH001", "sample.data", FILE_SIZE, PIECE_SIZE);
    }

    private void initializeNetwork() {
        tracker.trackFile(torrentFile);

        initialSeeder.initializeDownload(torrentFile);
        for (int i = 0; i < torrentFile.getPieceCount(); i++) {
            initialSeeder.getDownloadingFile(torrentFile.getInfoHash()).markPieceCompleted(i);
        }
        tracker.announce(torrentFile.getInfoHash(), initialSeeder.getDeviceID(), "completed");

        for (TorrentClient peer : peers) {
            peer.initializeDownload(torrentFile);
            tracker.announce(torrentFile.getInfoHash(), peer.getDeviceID(), "started");
        }
    }

    private void simulationCycle() {
        List<TorrentClient> activePeers = new ArrayList<>(peers);
        Collections.shuffle(activePeers, random);

        for (TorrentClient peer : activePeers) {
            processDownloads(peer);
        }
    }

    private void processDownloads(TorrentClient peer) {
        for (int i = 0; i < 3; i++) {
            int neededPiece = peer.getNextNeededPiece(torrentFile.getInfoHash());
            if (neededPiece == -1) break;

            TorrentClient sourcePeer = findPeerWithPiece(neededPiece, peer);
            if (sourcePeer != null) {
                boolean success = peer.requestPiece(torrentFile.getInfoHash(), neededPiece, sourcePeer);
                if (success) {
                    System.out.printf("Peer %s downloaded piece %d from %s%n",
                            peer.getDeviceID(), neededPiece, sourcePeer.getDeviceID());
                }
            }
        }
    }

    private TorrentClient findPeerWithPiece(int pieceIndex, TorrentClient requester) {
        List<TorrentClient> candidates = new ArrayList<>(peers);
        candidates.add(initialSeeder);
        Collections.shuffle(candidates, random);

        for (TorrentClient candidate : candidates) {
            if (candidate != requester &&
                    candidate.getDownloadingFile(torrentFile.getInfoHash()) != null &&
                    candidate.getDownloadingFile(torrentFile.getInfoHash()).isPieceCompleted(pieceIndex)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isDownloadComplete() {
        for (TorrentClient peer : peers) {
            if (!peer.isDownloadComplete(torrentFile.getInfoHash())) {
                return false;
            }
        }
        return true;
    }

    public void runSimulation() {
        System.out.println("Starting BitTorrent Simulation");
        System.out.println("File size: " + (FILE_SIZE / 1048576) + "MB");
        System.out.println("Piece size: " + (PIECE_SIZE / 1024) + "KB");
        System.out.println("Number of pieces: " + torrentFile.getPieceCount());
        System.out.println("Number of peers: " + peers.size());
        System.out.println("\nSimulation running...\n");

        for (int cycle = 1; cycle <= SIMULATION_CYCLES; cycle++) {
            System.out.println("=== Cycle " + cycle + " ===");
            simulationCycle();
            printNetworkStatus();

            if (isDownloadComplete()) {
                System.out.println("\nAll peers completed download!");
                break;
            }

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        printFinalStatistics();
    }

    private void printNetworkStatus() {
        System.out.println("\nNetwork Status:");
        System.out.printf("Seeder (%s): 100.0%% complete%n", initialSeeder.getDeviceID());
        for (TorrentClient peer : peers) {
            System.out.printf("%s: %.1f%% complete (Speed: ↑%.2f MB/s, ↓%.2f MB/s)%n",
                    peer.getDeviceID(),
                    peer.getDownloadProgress(torrentFile.getInfoHash()) * 100,
                    peer.getRealUploadSpeed() / 1048576.0,
                    peer.getRealDownloadSpeed() / 1048576.0);
        }
        System.out.println();
    }

    private void printFinalStatistics() {
        System.out.println("\nFinal Statistics:");
        for (TorrentClient peer : peers) {
            System.out.printf("%s:%n", peer.getDeviceID());
            System.out.printf("  - Final Progress: %.1f%%%n",
                    peer.getDownloadProgress(torrentFile.getInfoHash()) * 100);
            System.out.printf("  - Completed Pieces: %d/%d%n",
                    peer.getCompletedPieceCount(torrentFile.getInfoHash()),
                    peer.getTotalPieceCount(torrentFile.getInfoHash()));
        }
    }

    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        simulation.runSimulation();
    }
}