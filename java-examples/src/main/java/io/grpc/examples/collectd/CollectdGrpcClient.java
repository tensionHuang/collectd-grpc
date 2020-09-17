package io.grpc.examples.collectd;

import collectd.CollectdGrpc;
import collectd.CollectdOuterClass;
import collectd.types.Types;
import com.google.common.collect.Lists;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectdGrpcClient {

    public static void main(String[] args) {
        CollectdGrpcClient collectdGrpcClient = new CollectdGrpcClient("localhost", 50051);
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

        // Identifier example: SCG/disk-sda/disk_io_time
        Types.Identifier identifier = Types.Identifier.newBuilder()
                // cp pod id
                .setHost("7ba1f40a-3f01-4e51-a755-c96c404733aa")
                .setPlugin("SCG@disk")
                .setPluginInstance("sda")
                .setType("disk_io_time")
                .build();

        try {
            int num = 5;
            for (int i = 0; i < num; i++) {
                Types.Value value1 = Types.Value.newBuilder().setGauge(i).build();
                Types.Value value2 = Types.Value.newBuilder().setGauge(i * 2).build();
                Types.ValueList valueList = Types.ValueList.newBuilder().addAllDsNames(Lists.newArrayList("io_time", "weighted_io_time"))
                        .addValues(value1).addValues(value2).setIdentifier(identifier).setTime(Timestamp.newBuilder().setSeconds(startTimestamp).build()).build();
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
