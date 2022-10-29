package cc.towerdefence.minestom.playertracker;

import cc.towerdefence.api.agonessdk.EmptyStreamObserver;
import cc.towerdefence.api.service.PlayerTrackerGrpc;
import cc.towerdefence.api.service.PlayerTrackerProto;
import cc.towerdefence.minestom.MinestomServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Consumer;

public class PlayerTrackerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerTrackerManager.class);

    private final PlayerTrackerGrpc.PlayerTrackerStub stub;

    public PlayerTrackerManager() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("player-tracker.towerdefence.svc", 9090)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();

        this.stub = PlayerTrackerGrpc.newStub(channel);
    }

    public void ready() {
        MinestomServer.getEventNode().addListener(PlayerLoginEvent.class, this::onPlayerJoin);
    }

    public void retrievePlayerServer(UUID uuid, Consumer<PlayerTrackerProto.OnlineServer> responseConsumer) {
        this.stub.getPlayerServer(PlayerTrackerProto.GetPlayerServerRequest.newBuilder()
                .setPlayerId(uuid.toString())
                .build(), new StreamObserver<>() {
            @Override
            public void onNext(PlayerTrackerProto.GetPlayerServerResponse value) {
                responseConsumer.accept(value.getServer());
            }

            @Override
            public void onError(Throwable t) {
                LOGGER.error("Error while getting player server", t);
            }

            @Override
            public void onCompleted() {
                // ignored
            }
        });
    }

    private void onPlayerJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        this.stub.serverPlayerLogin(PlayerTrackerProto.PlayerLoginRequest.newBuilder()
                        .setPlayerId(player.getUuid().toString())
                        .setPlayerName(player.getUsername())
                        .setServerId(MinestomServer.SERVER_ID).build()
                , new EmptyStreamObserver<>());
    }
}
