package me.RareHyperIon.AntiTrample;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class Main extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private Material parsedFarmland;

    @Override
    public void onLoad() {
        this.parsedFarmland = Material.FARMLAND;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);

        final var command = this.getCommand("antitrample");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
        String sound = getConfig().getString("Sound");

        if (sound != null && !sound.trim().isEmpty()) {
            var key = org.bukkit.NamespacedKey.minecraft(sound.toLowerCase());
            if (org.bukkit.Registry.SOUNDS.get(key) == null) {
                getLogger().warning("Invalid sound in config: " + sound);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;


        final var block = event.getClickedBlock();
        if (block == null || block.getType() != this.parsedFarmland) return;
        final FileConfiguration config = this.getConfig();

        final String mode = config.getString("PermissionMode", "BYPASS").toUpperCase();
        final Player player = event.getPlayer();

        if("BYPASS".equals(mode) && player.hasPermission("antitrample.ignored") ||
            "WHITELIST".equals(mode) && !player.hasPermission("antitrample.use")) {
            return;
        }

        final String message = config.getString("Message");
        final String sound = config.getString("Sound");

        if(message != null && !message.trim().isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

        if (sound != null && !sound.trim().isEmpty()) {
            var key = org.bukkit.NamespacedKey.minecraft(sound.toLowerCase());
            var soundEvent = org.bukkit.Registry.SOUNDS.get(key);

            if (soundEvent != null) {
                player.playSound(player.getLocation(), soundEvent, 1, 1);
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTrample(org.bukkit.event.entity.EntityChangeBlockEvent event) {
        if (event.getBlock().getType() != this.parsedFarmland) return;

        final FileConfiguration config = this.getConfig();
        final String mode = config.getString("PermissionMode", "BYPASS").toUpperCase();

        if (event.getEntity() instanceof Player player) {

            if ("BYPASS".equals(mode) && player.hasPermission("antitrample.ignored")) {
                return;
            }

            if ("WHITELIST".equals(mode) && !player.hasPermission("antitrample.use")) {
                return;
            }

        } else {
            if (!config.getBoolean("PreventMobs")) return;
        }

        event.setCancelled(true);
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            @NotNull String[] args) {

        if (cmd.getName().equalsIgnoreCase("antitrample")) {
            if (!sender.hasPermission("antitrample.reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission to run this command."));
                return true;
            }

            if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: /antitrample reload"));
                return true;
            }

            this.reloadConfig();

            String sound = getConfig().getString("Sound");
            if (sound != null && !sound.trim().isEmpty()) {
                var key = org.bukkit.NamespacedKey.minecraft(sound.toLowerCase());
                if (org.bukkit.Registry.SOUNDS.get(key) == null) {
                    getLogger().warning("Invalid sound in config: " + sound);
                }
            }

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully reloaded."));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {

        return Collections.singletonList("reload");
    }

}
