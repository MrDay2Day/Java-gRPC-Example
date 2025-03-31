# Java gRPC Server and Client Example

This project demonstrates a basic Java gRPC server and client setup using **Maven** for build management. It showcases several key gRPC features, including reflection, metadata handling, and error handling. This example aims to provide a practical guide for understanding how microservices can leverage gRPC for efficient communication.


## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/grpc/file/
│   │   |   |   ├── Main.java
│   │   │   ├── examples/
│   │   │   │   ├── GRPCClient.java
│   │   │   │   ├── GRPCServer.java
│   │   ├── proto/
│   │   │   ├── greeter.proto
│   │   │   ├── calculator.proto
├── target/
├── pom.xml
└── README.md
```

## Protocol Buffer Definitions

The project uses two protocol buffer definitions: `greeter.proto` and `calculator.proto`.

### `greeter.proto`

```protobuf
syntax = "proto3";

option java_multiple_files = true;
option java_package = "grpc.file.proto";
option java_outer_classname = "GreeterProto";

package greeter;

service Greeter {
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}

service Farewell {
  rpc SayGoodbye (ByeRequest) returns (ByeReply) {}
}

message HelloRequest {
  string name = 1;
}

message HelloReply {
  string message = 1;
}

message ByeRequest {
  string name = 1;
}

message ByeReply {
  string message = 1;
}
```
