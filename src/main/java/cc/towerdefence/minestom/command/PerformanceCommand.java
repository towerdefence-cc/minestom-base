package cc.towerdefence.minestom.command;

import cc.towerdefence.minestom.MinestomServer;
import cc.towerdefence.minestom.utils.DurationFormatter;
import cc.towerdefence.minestom.utils.ProgressBar;
import com.sun.management.GcInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.monitoring.TickMonitor;
import org.jetbrains.annotations.NotNull;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PerformanceCommand extends Command {
    private static final Set<Pattern> EXCLUDED_MEMORY_SPACES = Stream.of("Metaspace", "Compressed Class Space", "^CodeHeap")
            .map(Pattern::compile).collect(Collectors.toUnmodifiableSet());

    private final AtomicReference<TickMonitor> lastTick = new AtomicReference<>();

    public PerformanceCommand() {
        super("performance");

        MinestomServer.getEventNode().addListener(ServerTickMonitorEvent.class, event -> this.lastTick.set(event.getTickMonitor()));

        this.addSyntax(this::onExecute);
    }

    private void onExecute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        this.getGcInfo();

        long totalMem = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long freeMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long ramUsage = totalMem - freeMem;
        float ramPercent = (float) ramUsage / (float) totalMem;

        TickMonitor monitor = this.lastTick.get();
        double tickMs = monitor.getTickTime();
        double tps = Math.min(MinecraftServer.TICK_PER_SECOND, Math.floor(1000 / tickMs));

        sender.sendMessage(
                Component.text()
                        .append(Component.newline())
                        .append(Component.text("RAM Usage: ", NamedTextColor.GRAY))
                        .append(ProgressBar.create(ramPercent, 30, "┃", NamedTextColor.GREEN, TextColor.color(0, 123, 0)))
                        .append(Component.text(String.format(" %sMB / %sMB\n", ramUsage, totalMem), NamedTextColor.GRAY))

                        .append(Component.newline())
                        .append(this.createGcComponent())
                        .append(Component.newline())

                        .append(Component.text("\nTPS: ", NamedTextColor.GRAY))
                        .append(Component.text(tps, NamedTextColor.GREEN))

                        .append(Component.text(" | ", NamedTextColor.DARK_GRAY))

                        .append(Component.text("MSPT: ", NamedTextColor.GRAY))
                        .append(Component.text(String.format("%sms\n", Math.floor(tickMs * 100) / 100), NamedTextColor.GREEN))
        );
    }

    private Component createGcComponent() {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("GC Info:", NamedTextColor.GRAY));

        for (Map.Entry<String, GcInfo> entry : this.getGcInfo().entrySet()) {
            TextComponent.Builder entryBuilder = Component.text();
            String lastRunText;
            if (entry.getValue() == null) {
                lastRunText = "never";
            } else {
                long millisSinceRun = this.getUptime() - entry.getValue().getEndTime();
                lastRunText = entry.getValue() == null ? "never" : DurationFormatter.ofGreatestUnit(Duration.ofMillis(millisSinceRun)) + " ago";
            }

            entryBuilder.append(Component.text("\n  " + entry.getKey() + ":", NamedTextColor.GRAY))
                    .append(Component.text("\n    Last Run: ", NamedTextColor.GRAY))
                    .append(Component.text(lastRunText, NamedTextColor.GOLD));

            if (entry.getValue() != null)
                entryBuilder.hoverEvent(HoverEvent.showText(this.createGcHover(entry.getKey(), entry.getValue())));

            builder.append(entryBuilder);
        }

        return builder.asComponent();
    }

    private Component createGcHover(String name, GcInfo info) {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("Name: ", NamedTextColor.GOLD))
                .append(Component.text(name, NamedTextColor.GRAY))
                .append(Component.newline())

                .append(Component.text("Duration: ", NamedTextColor.GOLD))
                .append(Component.text(info.getDuration() + "ms", NamedTextColor.GRAY))
                .append(Component.newline(), Component.newline())

                .append(Component.text("Memory After:", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(this.createMemoryUsagePeriod(info.getMemoryUsageAfterGc()))
                .append(Component.newline(), Component.newline())

                .append(Component.text("Memory Before:", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(this.createMemoryUsagePeriod(info.getMemoryUsageBeforeGc()));
        return builder.build();
    }

    private Component createMemoryUsagePeriod(Map<String, MemoryUsage> memoryUsageMap) {
        List<Component> lines = new ArrayList<>();

        for (Map.Entry<String, MemoryUsage> entry : memoryUsageMap.entrySet()) {
            if (EXCLUDED_MEMORY_SPACES.stream().anyMatch(pattern -> pattern.matcher(entry.getKey()).find()))
                continue;

            lines.add(Component.text().append(Component.text("  " + entry.getKey() + ": ", NamedTextColor.GOLD))
                    .append(Component.text(entry.getValue().getUsed() / 1024 / 1024 + "MB", NamedTextColor.GRAY))
                    .build());
        }

        return Component.join(JoinConfiguration.newlines(), lines);
    }

    private Map<String, GcInfo> getGcInfo() {
        Map<String, GcInfo> gcInfo = new HashMap<>();

        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            com.sun.management.GarbageCollectorMXBean bean = (com.sun.management.GarbageCollectorMXBean) garbageCollectorMXBean;

            gcInfo.put(bean.getName(), bean.getLastGcInfo());
        }
        return gcInfo;
    }

    private long getUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }
}