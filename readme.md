# BitTorrent Network Simulation

A Java-based educational simulation of the BitTorrent protocol, demonstrating peer-to-peer file distribution concepts through practical implementation.

## Overview

This project implements a distributed network simulation that models core BitTorrent concepts including:
- Peer discovery and tracking
- Segmented file transfers
- Bandwidth management
- Network topology
- Progress monitoring

## Project Structure

```
.
├── src/                    # Source code
│   ├── base/              # Core network components
│   │   └── torrent/       # BitTorrent protocol implementation
│   ├── model/             # Data models
│   ├── simulation/        # Simulation runtime
│   ├── test/             # Test suites
│   └── util/             # Utility classes
├── student/               # Student implementation directory
└── solution/             # Reference implementation
```

## Key Components

### Network Layer
- `NetworkDevice.java`: Base class for all network-capable devices
- `Router.java`: Network routing and connection management
- `Computer.java`: Extended network node with storage/bandwidth constraints
- `Hub.java`: Network hub implementation
- `Switch.java`: Network switch implementation

### BitTorrent Implementation
- `TorrentClient.java`: Peer node for file transfer operations
- `TorrentServer.java`: Seeding node with peer coordination
- `TorrentTracker.java`: Centralized peer discovery service

### Data Models
- `TorrentFile.java`: File representation with piece management
- `Piece.java`: Individual file segment unit
- `PeerStatus.java`: Transfer metrics and statistics
- `SwarmInfo.java`: Peer group state tracking

## Technical Specifications

### Runtime Parameters
```java
public static final int PIECE_SIZE = 262144;    // 256 KB
public static final long FILE_SIZE = 10485760L; // 10 MB
public static final int SIMULATION_CYCLES = 20;
public static final long SLEEP_TIME = 1000;     // 1 second
```

### Sample Usage

Creating a torrent client:
```java
TorrentClient client = new TorrentClient(
    "CLIENT1",           // Device ID
    "192.168.1.10",     // IP address
    "NYC",              // Location
    1000,               // Bandwidth (Mbps)
    50.0,               // Max upload speed
    100.0,              // Max download speed
    10000000L           // Storage capacity (bytes)
);
```

Initializing a file transfer:
```java
TorrentFile file = new TorrentFile(
    "hash123",          // Info hash
    "test.mp4",         // Filename
    1048576L,           // File size (bytes)
    262144              // Piece size (bytes)
);
client.initializeDownload(file);
```

Running the simulation:
```java
Simulation simulation = new Simulation();
simulation.runSimulation();
```

## Features

### Network Implementation
- IPv4 address validation and management
- Bandwidth and storage constraints
- Router-based network topology
- Connection tracking
- Device discovery

### BitTorrent Protocol
- Piece-based file segmentation
- Peer discovery and tracking
- Progress monitoring
- Transfer speed calculations
- Swarm management
- Multiple simultaneous transfers

### Testing
Comprehensive test suite covering:
- Network device functionality
- Torrent protocol operations
- Data model integrity
- Edge cases and error conditions

## Getting Started

### Prerequisites
- Java JDK 21 or higher
- JUnit 5.7.0+ for testing

### Building the Project
1. Clone the repository
2. Navigate to the project directory
3. Run the tests: `./gradlew test` or `mvn test`
4. Run the simulation: `./gradlew run` or `mvn exec:java`

## Design Notes

### Limitations
The simulation intentionally excludes:
- Actual file I/O
- Network protocol headers
- Socket communications
- Cryptographic verification
- Request pipelining
- Advanced peer selection

### Educational Focus
This implementation prioritizes:
- Code clarity and readability
- Demonstrable protocol concepts
- Testability
- Modular design
- Clear separation of concerns