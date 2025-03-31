package grpc.file;

import grpc.file.examples.GRPCServer;

public class Main {
    public static void main(String[] args) {
        System.out.println("gRPC Server");

        try{
            GRPCServer.main(args);
        } catch (Exception e) {
            System.err.println("Error starting gRPC server: " + e.getMessage());
        }
    }
}