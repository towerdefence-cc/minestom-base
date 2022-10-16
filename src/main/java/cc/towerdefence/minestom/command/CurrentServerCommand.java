package cc.towerdefence.minestom.command;

import cc.towerdefence.minestom.MinestomServer;
import cc.towerdefence.minestom.playertracker.PlayerTrackerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class CurrentServerCommand extends Command {
    private final PlayerTrackerManager playerTrackerManager;
    private static final String MESSAGE = """
            <gold>Current Proxy: <yellow><proxy_id>
            <gold>Current Server: <yellow><server_id>""";
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public CurrentServerCommand() {
        super("currentserver");

        this.playerTrackerManager = MinestomServer.getPlayerTrackerManager();

        this.setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            this.playerTrackerManager.retrievePlayerServer(player.getUuid(), onlineServer -> {
                sender.sendMessage(MINI_MESSAGE.deserialize(MESSAGE, TagResolver.builder()
                        .tag("server_id", Tag.inserting(Component.text(onlineServer.getServerId())))
                        .tag("proxy_id", Tag.inserting(Component.text(onlineServer.getProxyId())))
                        .build()));
            });
        });
    }
}
