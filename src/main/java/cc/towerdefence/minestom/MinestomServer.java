package cc.towerdefence.minestom;

import cc.towerdefence.api.agonessdk.EmptyStreamObserver;
import cc.towerdefence.minestom.agones.AgonesManager;
import cc.towerdefence.minestom.command.CurrentServerCommand;
import cc.towerdefence.minestom.kubernetes.KubernetesManager;
import cc.towerdefence.minestom.playertracker.PlayerTrackerManager;
import dev.agones.sdk.alpha.AlphaAgonesSDKProto;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.extras.velocity.VelocityProxy;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinestomServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinestomServer.class);

    public static final boolean DEV_ENVIRONMENT = System.getenv("AGONES_SDK_GRPC_PORT") == null;
    public static final int MAX_PLAYERS = System.getenv("MAX_PLAYERS") == null ? 100 : Integer.parseInt(System.getenv("MAX_PLAYERS"));
    public static final String SERVER_ID = DEV_ENVIRONMENT ? "local" : System.getenv("HOSTNAME");


    // Will be null if Agones is not enabled (AgonesManager#isEnabled() == false)
    private static @UnknownNullability EventNode<Event> EVENT_NODE;
    private static @UnknownNullability KubernetesManager kubernetesManager;
    private static @UnknownNullability AgonesManager agonesManager;
    private static @UnknownNullability PlayerTrackerManager playerTrackerManager;

    static {
        if (KubernetesManager.isEnabled()) kubernetesManager = new KubernetesManager();
        if (KubernetesManager.isEnabled()) agonesManager = new AgonesManager();
        if (KubernetesManager.isEnabled()) playerTrackerManager = new PlayerTrackerManager();
    }

    public MinestomServer(String address, int port) {
        MinecraftServer server = MinecraftServer.init();

        this.tryEnableVelocity();

        LOGGER.info("Starting server at {}:{}", address, port);
        server.start(address, port);

        EVENT_NODE = EventNode.all("minestom-base");
        MinecraftServer.getGlobalEventHandler().addChild(EVENT_NODE);

        if (KubernetesManager.isEnabled()) {
            LOGGER.info("Marking server as READY for Agones with a capacity of {} players", MAX_PLAYERS);
            agonesManager.ready();
            agonesManager.getAlphaSdk().setPlayerCapacity(AlphaAgonesSDKProto.Count.newBuilder().setCount(MAX_PLAYERS).build(), new EmptyStreamObserver<>());
            playerTrackerManager.ready();

            MinecraftServer.getCommandManager().register(new CurrentServerCommand());
        } else {
            LOGGER.warn("""
                    Kubernetes is not enabled, this server will not be able to connect to Agones
                    Other features such as [player-tracking] will also be disabled
                    """);
        }
    }

    private void tryEnableVelocity() {
        String forwardingSecret = Entrypoint.getValue("minestom.velocity-forwarding-secret", new String[0], null);
        if (forwardingSecret == null) return;

        LOGGER.info("Enabling Velocity forwarding");

        VelocityProxy.enable(forwardingSecret);
    }

    public static EventNode<Event> getEventNode() {
        return EVENT_NODE;
    }

    public static KubernetesManager getKubernetesManager() {
        return kubernetesManager;
    }

    public static AgonesManager getAgonesManager() {
        return agonesManager;
    }

    public static PlayerTrackerManager getPlayerTrackerManager() {
        return playerTrackerManager;
    }
}
