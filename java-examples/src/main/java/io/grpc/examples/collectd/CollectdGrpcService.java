package io.grpc.examples.collectd;

import collectd.CollectdGrpc;
import collectd.CollectdOuterClass;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectdGrpcService extends CollectdGrpc.CollectdImplBase {

    @Override
    public StreamObserver<CollectdOuterClass.PutValuesRequest> putValues(StreamObserver<CollectdOuterClass.PutValuesResponse> responseObserver) {
        log.info("putValues!");
        return new CollectdStreamReqObserver(responseObserver);
    }

    private static final class CollectdStreamReqObserver implements StreamObserver<CollectdOuterClass.PutValuesRequest> {

        private final StreamObserver<CollectdOuterClass.PutValuesResponse> responseObserver;

        public CollectdStreamReqObserver(StreamObserver<CollectdOuterClass.PutValuesResponse> responseObserver) {
            this.responseObserver = responseObserver;
        }

        public void onNext(CollectdOuterClass.PutValuesRequest request) {
            try {
                log.info("PutValuesRequest: " + request.toString());
                responseObserver.onNext(CollectdOuterClass.PutValuesResponse.newBuilder().build());
            } catch (Throwable t) {
                this.responseObserver.onError(t);
            }
        }

        public void onError(Throwable t) {
            log.info("ERROR: " + t.getMessage());
            responseObserver.onCompleted();
        }

        public void onCompleted() {
            responseObserver.onCompleted();
        }
    }
}