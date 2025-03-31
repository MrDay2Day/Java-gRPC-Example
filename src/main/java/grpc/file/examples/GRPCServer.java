package grpc.file.examples;

import io.grpc.*;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;

import grpc.file.proto.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GRPCServer {
    private Server server;

    private void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .addService(new CalculatorImpl())
                .addService(ProtoReflectionService.newInstance())
                .intercept(new MetadataInterceptor())
                .build()
                .start();
        System.out.println("Server with reflection started, listening on " + port);
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            String resString = "Hello " + req.getName();
            HelloReply reply = HelloReply.newBuilder()
                    .setMessage(resString)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    private class CalculatorImpl extends CalculatorGrpc.CalculatorImplBase {
        @Override
        public void add(AddRequest req, StreamObserver<AddResponse> responseObserver) {
            // Handle the addition request
            if(req.getA() < 0) {
                responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Invalid Value")));
                return;
            }
            int sum = req.getA() + req.getB();
            AddResponse reply = AddResponse.newBuilder()
                    .setResult(sum)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void multiply(MultiplyRequest req, StreamObserver<MultiplyResponse> responseObserver) {
            // Handle the addition request
            int result = (req.getA() + req.getB()) * req.getC();
            MultiplyResponse reply = MultiplyResponse.newBuilder()
                    .setResult(result)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final GRPCServer server = new GRPCServer();
        server.start();

        // Wait for shutdown
        server.server.awaitTermination();
    }
}

class MetadataInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTH_TOKEN_KEY =
            Metadata.Key.of("auth-token", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> USER_ID_KEY =
            Metadata.Key.of("user-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String userId = headers.get(USER_ID_KEY);
        String token = headers.get(AUTH_TOKEN_KEY);

        /*


            DO AUTHENTICATION LOGIC HERE


         */

        if(userId == null || token == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing user ID or auth token"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        System.out.println("Received UserId: " + userId);
        System.out.println("Received Token: " + token);

        return next.startCall(call, headers);
    }
}