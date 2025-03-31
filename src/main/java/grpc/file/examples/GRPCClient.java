package grpc.file.examples;

import io.grpc.*;

import grpc.file.proto.*;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.TimeUnit;

public class GRPCClient {
    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final CalculatorGrpc.CalculatorBlockingStub calculatorStub;

    // Define metadata keys
    private static final Metadata.Key<String> AUTH_TOKEN_KEY = Metadata.Key.of("auth-token", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> USER_ID_KEY = Metadata.Key.of("user-id", Metadata.ASCII_STRING_MARSHALLER);


    // Constructor without metadata
    public GRPCClient(Channel channel) {
        blockingStub = GreeterGrpc.newBlockingStub(channel);
        calculatorStub = CalculatorGrpc.newBlockingStub(channel);
    }

    // Constructor with metadata
    public GRPCClient(Channel channel, Metadata metadata) {
        // Create a channel with the metadata attached
        Channel interceptedChannel = ClientInterceptors.intercept(channel, MetadataUtils.newAttachHeadersInterceptor(metadata));

        blockingStub = GreeterGrpc.newBlockingStub(interceptedChannel);
        calculatorStub = CalculatorGrpc.newBlockingStub(interceptedChannel);
    }

    public void greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response = blockingStub.sayHello(request);
        System.out.println("Greeting: " + response.getMessage());
    }

    public void calculateAdd(int num1, int num2) {
        AddRequest addReq = AddRequest
                .newBuilder()
                .setA(num1).setB(num2)
                .build();
        AddResponse response = calculatorStub.add(addReq);
        System.out.println("The sum of " + num1 + " and " + num2 + " is: " + response.getResult());

    }

    public static void main(String[] args) throws Exception {
        String target = "localhost:50051";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext() // Disable TLS for simplicity
                .build();

        try {
            // Create metadata
            Metadata metadata = new Metadata();
            metadata.put(AUTH_TOKEN_KEY, "your_auth_token");
            metadata.put(USER_ID_KEY, "user123");

            GRPCClient client = new GRPCClient(channel, metadata);
            client.greet("World");
            client.calculateAdd(3,5);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}