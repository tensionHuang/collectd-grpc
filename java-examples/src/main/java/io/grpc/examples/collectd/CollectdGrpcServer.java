package io.grpc.examples.collectd;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CollectdGrpcServer {

    private Server server;
    private final int port;

    public static void main(String[] args) throws IOException, InterruptedException {
        final CollectdGrpcServer server = new CollectdGrpcServer(50052);
        server.start();
        server.awaitTermination();
    }

    public CollectdGrpcServer(int port){
        this.port = port;
    }
    
    private void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new CollectdGrpcService())
                .build()
                .start();
        log.info("Server is started, listen port: " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }));
    }

    private void shutdown() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void awaitTermination() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
