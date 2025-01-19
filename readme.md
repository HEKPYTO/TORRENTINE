# BitTorrent Network Simulation

## Technical Overview

This project implements a distributed network simulation framework that demonstrates fundamental concepts of peer-to-peer file distribution protocols. The implementation provides programmatic representations of network topology, peer discovery mechanisms, and segmented data transfer operations.

## Architectural Components

### Network Layer
- `NetworkDevice.java`: Abstract network node representation with IP addressing capabilities
- `Router.java`: Network routing implementation with connection management
- `Computer.java`: Extended network node with storage and bandwidth constraints

### Distribution Layer
- `TorrentClient.java`: Peer node implementation for file transfer operations
- `TorrentServer.java`: Seeding node implementation with peer coordination
- `TorrentTracker.java`: Centralized peer discovery and swarm management

### Data Models
- `TorrentFile.java`: File representation with segmentation logic
- `Piece.java`: Atomic transfer unit implementation
- `PeerStatus.java`: Transfer metrics collection and calculation
- `SwarmInfo.java`: Peer group state management

### Network Utilities
- `IPUtils.java`: IPv4 address validation and processing

## Technical Specifications

### Runtime Parameters
```java
public static final int PIECE_SIZE = 262144;    // 256 KiB
public static final long FILE_SIZE = 10485760L; // 10 MiB
public static final int SIMULATION_CYCLES = 20;
public static final long SLEEP_TIME = 1000;     // milliseconds
```

### Network Node Implementation
```java
TorrentClient client = new TorrentClient(
    "CLIENT1",           // Unique identifier
    "192.168.1.10",     // IPv4 address
    "NYC",              // Geographic identifier
    1000,               // Bandwidth capacity (arbitrary units)
    50.0,               // Upload throughput (arbitrary units)
    100.0,              // Download throughput (arbitrary units)
    10000000L           // Storage capacity (bytes)
);
```

### File Transfer Initialization
```java
TorrentFile file = new TorrentFile(
    "hash123",          // Unique file identifier
    "test.mp4",         // File identifier
    1048576L,           // File size (bytes)
    262144              // Segment size (bytes)
);
client.initializeDownload(file);
```

## Technical Documentation

### Peer Discovery Protocol
1. Node registration with tracker instance
2. Peer set maintenance per unique file identifier
3. Peer set distribution to requesting nodes

### Transfer Protocol Implementation
1. File segmentation into fixed-size pieces
2. Piece request operations between peers
3. Transfer simulation with configurable success rate
4. Per-piece progress tracking
5. Throughput calculation based on successful transfers

### Network Architecture
- Unique IPv4 address allocation per node
- Router-based network topology
- Enforced bandwidth and storage constraints
- Simplified routing implementation for simulation purposes

## Implementation Constraints

The current implementation specifically excludes:
- Concrete file I/O operations
- Cryptographic verification
- Network protocol headers
- Actual socket communications
- Request pipelining
- Advanced peer selection algorithms

## Required Dependencies

```markdown
### Runtime Dependencies
- Java Development Kit (JDK) >= 8
  - Minimum version required for the Stream API and lambda expressions
  - Tested with OpenJDK 8, 11, and 17

### Test Dependencies
- JUnit Jupiter >= 5.7.0
  - Required for parameterized tests
  - Required for assertion facilities
  - Required for test lifecycle management

### Build System
- Maven >= 3.6.0
  OR
- Gradle >= 6.0

### Development Environment
- Java IDE with JUnit support
- Git >= 2.0
```

## Execution Instructions

### Simulation Execution
```java
public static void main(String[] args) {
    Simulation simulation = new Simulation();
    simulation.runSimulation();
}
```

## Output Specifications

The simulation generates status output including:
- Per-peer download progress metrics
- Upload and download throughput measurements
- Piece completion statistics
- Network state information
- Aggregate performance metrics

## License and Usage

This software is provided for educational and research purposes. Implementation of this simulation in production environments is not recommended. Users must ensure compliance with all applicable laws and regulations regarding peer-to-peer network implementations.