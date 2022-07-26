package org.mpdev.projects.tsreports.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.mpdev.projects.tsreports.TSReports;
import org.mpdev.projects.tsreports.inventory.InventoryDrawer;
import org.mpdev.projects.tsreports.inventory.inventories.LangSelector;
import org.mpdev.projects.tsreports.inventory.inventories.ReportPanel;
import org.mpdev.projects.tsreports.inventory.inventories.StatusPanel;
import org.mpdev.projects.tsreports.objects.OfflinePlayer;
import org.mpdev.projects.tsreports.utils.PluginHelp;
import org.mpdev.projects.tsreports.utils.Utils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ReportCommand extends Command implements TabExecutor {

    private final TSReports plugin;
    public static int cooldown;
    public static final Map<UUID, Integer> timer = new HashMap<>();

    public ReportCommand(TSReports plugin, String command, String permission, String... aliases) {
        super(command, permission, aliases);
        this.plugin = plugin;
        cooldown = plugin.getConfig().getInt("reportCooldown", 60);

        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (timer.isEmpty())return;
            for (UUID uuid : timer.keySet()) {
                int timeleft = timer.get(uuid);

                if (timeleft <= 0) {
                    timer.remove(uuid);
                } else {
                    timer.put(uuid, timeleft - 1);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof ProxiedPlayer)) {
            Utils.sendText(sender, "onlyPlayer");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 1 && args[0].equalsIgnoreCase("language")) {

            if (!TSReports.getInstance().getConfig().getBoolean("gui.languageselector")) {
                Utils.sendText(sender, "commandDisabled");
                return;
            }

            if (!plugin.getCommands().get("language")) {
                Utils.sendText(sender, "commandDisabled");
                return;
            }

            InventoryDrawer.open(new LangSelector(null, player));

        } else if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("status")) {

            if (!TSReports.getInstance().getConfig().getBoolean("gui.statuspanel")) {
                Utils.sendText(sender, "commandDisabled");
                return;
            }

            if (!plugin.getCommands().get("status-player")) {
                Utils.sendText(sender, "commandDisabled");
                return;
            }

            if (args.length == 1) {
                InventoryDrawer.open(new StatusPanel(null, player, 1));
                return;
            }

            if (!Utils.isInteger(args[1])) {
                Utils.sendText(player, "mustBeNumber");
                return;
            }

            int page = Integer.parseInt(args[1]);
            InventoryDrawer.open(new StatusPanel(null, player, page));

        } else if (args.length >= 1) {

            if (!plugin.getCommands().get("report")) {
                Utils.sendText(sender, "commandDisabled");
                return;
            }

            if (timer.containsKey(player.getUniqueId())) {
                Utils.sendText(player, "reportCooldown", message -> message.replace("%time%", String.valueOf(timer.get(player.getUniqueId()))));
                return;
            }

            if (plugin.getConfigManager().getBannedPlayers().contains(player.getName()) || plugin.getConfigManager().getBannedPlayers().contains(player.getUniqueId().toString())) {
                Utils.sendText(sender, "blacklisted");
                return;
            }

            String uuidOrName = args[0];

            if ((uuidOrName.equalsIgnoreCase(player.getName()) || uuidOrName.equalsIgnoreCase(player.getUniqueId().toString())) && !player.getName().equalsIgnoreCase("LaurinVL")) {
                Utils.sendText(player, "reportYourself");
                return;
            }

            if (Utils.isOnline(uuidOrName)) {

                ProxiedPlayer target = uuidOrName.length() == 36
                        ? plugin.getProxy().getPlayer(UUID.fromString(uuidOrName))
                        : plugin.getProxy().getPlayer(uuidOrName);

                InventoryDrawer.open(new ReportPanel(null, player, target));

            } else if (Utils.isOffline(uuidOrName)) {

                OfflinePlayer target = TSReports.getInstance().getOfflinePlayers().get(uuidOrName);

                InventoryDrawer.open(new ReportPanel(null, player, target));

            } else {

                Utils.sendText(player, "playerNotFound");

            }

        } else {

            PluginHelp.reportHelp(player);

        }

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> autoComplete = new ArrayList<>();

        if (args.length >= 1) {
            autoComplete.addAll(plugin.getOfflinePlayers().keySet());
        }

        return autoComplete;
    }

}
