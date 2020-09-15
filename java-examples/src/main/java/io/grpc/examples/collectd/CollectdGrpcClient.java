package io.grpc.examples.collectd;

import collectd.CollectdGrpc;
import collectd.CollectdOuterClass;
import collectd.types.Types;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectdGrpcClient {

    public static void main(String[] args) {
        CollectdGrpcClient collectdGrpcClient = new CollectdGrpcClient("10.206.84.154", 50052);
        collectdGrpcClient.putMetrics();
    }

    private final ManagedChannel managedChannel;

    private final CollectdGrpc.CollectdStub collectdStub;

    public CollectdGrpcClient(final String hostname, final int port) {
        managedChannel = ManagedChannelBuilder
                .forAddress(hostname, port)
                .usePlaintext()
                .build();

        collectdStub = CollectdGrpc.newStub(managedChannel);
        managedChannel.notifyWhenStateChanged(this.managedChannel.getState(true),
                () -> log.info("Connectivity change to [{}] with server [{}:{}]",
                        this.managedChannel.getState(true), hostname, port));
    }

    public void putMetrics() {

        final long startTimestamp = System.currentTimeMillis();

        StreamObserver<CollectdOuterClass.PutValuesResponse> responseObserver = new StreamObserver<CollectdOuterClass.PutValuesResponse>() {

            public void onNext(CollectdOuterClass.PutValuesResponse putValuesResponse) {
                log.info("onNext: " + putValuesResponse.toString());
            }

            public void onError(Throwable throwable) {
                log.error("onError: ", throwable);
            }

            public void onCompleted() {
                log.info("Finished!");
            }
        };

        StreamObserver<CollectdOuterClass.PutValuesRequest> reqObserver = collectdStub.putValues(responseObserver);

        // Identifier
        Types.Identifier identifier = Types.Identifier.newBuilder()
                .setHost("localhost")
                .setPlugin("grpc")
                .setPluginInstance("grpc-test")
                .setType("counter")
                .setTypeInstance("metric_test").build();

        try {
            int num = 5;
            for (int i = 0; i < num; i++) {
                Types.Value value = Types.Value.newBuilder().setCounter(i).build();
                Types.ValueList valueList = Types.ValueList.newBuilder().addDsNames("DataSource" + i)
                        .addValues(value).setIdentifier(identifier).setTime(Timestamp.newBuilder().setSeconds(startTimestamp).build()).build();
                CollectdOuterClass.PutValuesRequest request = CollectdOuterClass.PutValuesRequest.newBuilder().setValueList(valueList).build();
                reqObserver.onNext(request);
            }

            Thread.sleep(10000);

        } catch (Throwable t) {

            reqObserver.onError(t);
        }
        reqObserver.onCompleted();
    }
}
