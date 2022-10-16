package cc.towerdefence.minestom.agones;

import cc.towerdefence.minestom.MinestomServer;
import dev.agones.sdk.SDKGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.jetbrains.annotations.NotNull;

public final class AgonesManager {
    private static final int AGONES_GRPC_PORT = MinestomServer.DEV_ENVIRONMENT ? 9357 : Integer.parseInt(System.getenv("AGONES_SDK_GRPC_PORT"));
    private static final boolean ENABLED = !MinestomServer.DEV_ENVIRONMENT || System.getenv("ENABLE_AGONES_DEV") != null;

    private final @NotNull SDKGrpc.SDKStub sdk;
    private final @NotNull dev.agones.sdk.beta.SDKGrpc.SDKStub betaSdk;
    private final @NotNull dev.agones.sdk.alpha.SDKGrpc.SDKStub alphaSdk;

    public AgonesManager() {
        if (!ENABLED) throw new IllegalStateException("Initialisation of AgonesManager is disabled");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", AGONES_GRPC_PORT).usePlaintext().build();
        this.sdk = SDKGrpc.newStub(channel);
        this.betaSdk = dev.agones.sdk.beta.SDKGrpc.newStub(channel);
        this.alphaSdk = dev.agones.sdk.alpha.SDKGrpc.newStub(channel);
    }

    public static boolean isEnabled() {
        return ENABLED;
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
