package cc.towerdefence.minestom.command.agones.subs;

import cc.towerdefence.minestom.MinestomServer;
import cc.towerdefence.minestom.command.agones.AgonesCommand;
import dev.agones.sdk.alpha.AlphaAgonesSDKProto;
import dev.agones.sdk.alpha.SDKGrpc;
import io.grpc.stub.StreamObserver;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;

import java.util.UUID;

public class AlphaSubs {
    private final SDKGrpc.SDKStub sdk = MinestomServer.getAgonesManager().getAlphaSdk();

    public void executeSetCapacity(CommandSender sender, CommandContext context) {
        int capacity = context.get("value");
        this.sdk.setPlayerCapacity(AlphaAgonesSDKProto.Count.newBuilder().setCount(capacity).build(), new StreamObserver<AlphaAgonesSDKProto.Empty>() {
            @Override
            public void onNext(AlphaAgonesSDKProto.Empty value) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "SetPlayerCapacity", AgonesCommand.RequestStatus.NEXT,
                        "Set player capacity to " + capacity
                );

                sender.sendMessage(response);
            }

            @Override
            public void onError(Throwable t) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "SetPlayerCapacity", AgonesCommand.RequestStatus.ERROR,
                        t.getMessage()
                );

                sender.sendMessage(response);
            }

            @Override
            public void onCompleted() {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "SetPlayerCapacity", AgonesCommand.RequestStatus.COMPLETED,
                        ""
                );

                sender.sendMessage(response);
            }
        });
    }

    public void executeGetConnectedPlayers(CommandSender sender, CommandContext context) {
        this.sdk.getConnectedPlayers(AlphaAgonesSDKProto.Empty.getDefaultInstance(), new StreamObserver<>() {
            @Override
            public void onNext(AlphaAgonesSDKProto.PlayerIDList value) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "GetConnectedPlayers", AgonesCommand.RequestStatus.NEXT,
                        "(%s): %s".formatted(value.getListCount(), value.getListList())
                );

                sender.sendMessage(response);
            }

            @Override
            public void onError(Throwable t) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "GetConnectedPlayers", AgonesCommand.RequestStatus.ERROR,
                        t.getMessage()
                );

                sender.sendMessage(response);
            }

            @Override
            public void onCompleted() {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "GetConnectedPlayers", AgonesCommand.RequestStatus.COMPLETED,
                        ""
                );

                sender.sendMessage(response);
            }
        });
    }

    public void executeIsPlayerConnected(CommandSender sender, CommandContext context) {
        UUID playerId = context.get("playerId");
        this.sdk.isPlayerConnected(AlphaAgonesSDKProto.PlayerID.newBuilder().setPlayerID(playerId.toString()).build(), new StreamObserver<>() {
            @Override
            public void onNext(AlphaAgonesSDKProto.Bool value) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "IsPlayerConnected", AgonesCommand.RequestStatus.NEXT,
                        "Player %s is %sconnected".formatted(playerId, value.getBool() ? "" : "not ")
                );

                sender.sendMessage(response);
            }

            @Override
            public void onError(Throwable t) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "IsPlayerConnected", AgonesCommand.RequestStatus.ERROR,
                        t.getMessage()
                );

                sender.sendMessage(response);
            }

            @Override
            public void onCompleted() {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "IsPlayerConnected", AgonesCommand.RequestStatus.COMPLETED,
                        ""
                );

                sender.sendMessage(response);
            }
        });
    }

    public void executePlayerConnect(CommandSender sender, CommandContext context) {
        UUID playerId = context.get("playerId");
        this.sdk.playerConnect(AlphaAgonesSDKProto.PlayerID.newBuilder().setPlayerID(playerId.toString()).build(), new StreamObserver<>() {
            @Override
            public void onNext(AlphaAgonesSDKProto.Bool value) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "PlayerConnect", AgonesCommand.RequestStatus.NEXT,
                        value.getBool() ? "Player %s connected".formatted(playerId) : "Player %s already marked connected???? List unchanged".formatted(playerId)
                );

                sender.sendMessage(response);
            }

            @Override
            public void onError(Throwable t) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "PlayerConnect", AgonesCommand.RequestStatus.ERROR,
                        t.getMessage()
                );

                sender.sendMessage(response);
            }

            @Override
            public void onCompleted() {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "PlayerConnect", AgonesCommand.RequestStatus.COMPLETED,
                        ""
                );

                sender.sendMessage(response);
            }
        });
    }

    public void executePlayerDisconnect(CommandSender sender, CommandContext context) {
        UUID playerId = context.get("playerId");
        this.sdk.playerDisconnect(AlphaAgonesSDKProto.PlayerID.newBuilder().setPlayerID(playerId.toString()).build(), new StreamObserver<>() {
            @Override
            public void onNext(AlphaAgonesSDKProto.Bool value) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "PlayerDisconnect", AgonesCommand.RequestStatus.NEXT,
                        value.getBool() ? "Player %s marked disconnected".formatted(playerId) : "Player %s is not marked as connected??? List unchanged".formatted(playerId)
                );

                sender.sendMessage(response);
            }

            @Override
            public void onError(Throwable t) {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "PlayerDisconnect", AgonesCommand.RequestStatus.ERROR,
                        t.getMessage()
                );

                sender.sendMessage(response);
            }

            @Override
            public void onCompleted() {
                Component response = AgonesCommand.generateMessage(
                        "Alpha", "PlayerDisconnect", AgonesCommand.RequestStatus.COMPLETED,
                        ""
                );

                sender.sendMessage(response);
            }
        });
    }
}
