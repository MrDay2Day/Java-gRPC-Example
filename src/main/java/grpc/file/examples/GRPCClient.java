package grpc.file.examples;

import io.grpc.*;

import grpc.file.proto.*;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.Executor;
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
        Metadata metadata = new Metadata();
        metadata.put(AUTH_TOKEN_KEY, "your_auth_token_on_service");
        metadata.put(USER_ID_KEY, "user123Service");

        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response = blockingStub
                .withCallCredentials(new MetadataCredentials(metadata))
                .sayHello(request);
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

            try{
                GRPCClient client_1 = new GRPCClient(channel);
                client_1.greet("World"); // With metadata
                client_1.calculateAdd(3,5); // Without metadata
            }catch (Exception e){
                System.out.println("Individual Metadata Error: " + e.getMessage());
            }


            try{
                // Create metadata
                Metadata metadata = new Metadata();
                metadata.put(AUTH_TOKEN_KEY, "your_auth_token");
                metadata.put(USER_ID_KEY, "user123");

                GRPCClient client_2 = new GRPCClient(channel, metadata); // Global metadata
                client_2.greet("World");
                client_2.calculateAdd(3,5);
            }catch(Exception e){
                System.out.println("Global Metadata Error: " + e.getMessage());
            }


        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}


// Custom CallCredentials implementation
class MetadataCredentials extends CallCredentials {
    private final Metadata metadata;

    public MetadataCredentials(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        appExecutor.execute(() -> {
            try {
                applier.apply(metadata);
            } catch (Throwable e) {
                applier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });
    }

    @Override
    public void thisUsesUnstableApi() {
        // This method is required by the CallCredentials interface but doesn't need implementation
    }
}

