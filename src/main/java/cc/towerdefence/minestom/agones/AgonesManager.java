package cc.towerdefence.minestom.agones;

import cc.towerdefence.api.agonessdk.AgonesUtils;
import cc.towerdefence.api.agonessdk.EmptyStreamObserver;
import cc.towerdefence.minestom.MinestomServer;
import cc.towerdefence.minestom.kubernetes.KubernetesManager;
import dev.agones.sdk.AgonesSDKProto;
import dev.agones.sdk.SDKGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class AgonesManager {
    private static final int AGONES_GRPC_PORT = MinestomServer.DEV_ENVIRONMENT ? 9357 : Integer.parseInt(System.getenv("AGONES_SDK_GRPC_PORT"));

    private static final Logger LOGGER = LoggerFactory.getLogger(AgonesManager.class);

    private final @NotNull SDKGrpc.SDKStub sdk;
    private final @NotNull dev.agones.sdk.beta.SDKGrpc.SDKStub betaSdk;
    private final @NotNull dev.agones.sdk.alpha.SDKGrpc.SDKStub alphaSdk;

    public AgonesManager() {
        if (!KubernetesManager.isEnabled())
            throw new IllegalStateException("Initialisation of AgonesManager is disabled");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", AGONES_GRPC_PORT).usePlaintext().build();
        this.sdk = SDKGrpc.newStub(channel);
        this.betaSdk = dev.agones.sdk.beta.SDKGrpc.newStub(channel);
        this.alphaSdk = dev.agones.sdk.alpha.SDKGrpc.newStub(channel);

        LOGGER.info("Starting Agones Health task");
    }

    /**
     * Should be called when the server is ready to accept connections
     */
    public void ready() {
        AgonesUtils.startHealthTask(this.sdk, 5, TimeUnit.SECONDS);
        this.sdk.ready(AgonesSDKProto.Empty.getDefaultInstance(), new EmptyStreamObserver<>());
    }

    public @NotNull SDKGrpc.SDKStub getSdk() {
        return sdk;
    }

    public @NotNull dev.agones.sdk.beta.SDKGrpc.SDKStub getBetaSdk() {
        return betaSdk;
    }

    public @NotNull dev.agones.sdk.alpha.SDKGrpc.SDKStub getAlphaSdk() {
        return alphaSdk;
    }
}
