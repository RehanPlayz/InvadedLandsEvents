package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class InvadedEvent implements Listener {
    protected EventsMain plugin;
    protected Logger log;
    private String name;
    protected boolean started;
    private boolean enabled;

    protected ConfigurationSection eventConfig;
    protected List<Player> players;
    protected List<Player> spectators;
    protected BukkitRunnable playerCheck;

    protected ItemStack star;

    public InvadedEvent(String name, EventsMain plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.name = name;
        this.eventConfig = plugin.getConfig().getConfigurationSection("events." + name.toLowerCase().replace(" ", ""));
        this.enabled = eventConfig.getBoolean("enabled");
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();

        this.playerCheck = new BukkitRunnable() {
            @Override
            public void run() {
                if (players.size() < 2) {
                    playerWon(players.get(0));
                }
            }
        };

        this.star = new ItemStack(Material.NETHER_STAR);
        ItemMeta im = star.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&l&3Leave Event"));
        this.star.setItemMeta(im);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (enabled)
            init(plugin);
        else
            log.info(name + " not enabled!");
    }

    public abstract void init(EventsMain plugin);
    public abstract void start();
    public abstract void stop();

    public String getName() {
        return name;
    }

    public boolean isStarted() {
        return started;
    }
    public boolean isEnabled() {
        return enabled;
    }

    public boolean containsPlayer(Player player) {
        return players.contains(player) || spectators.contains(player);
    }

    public void joinEvent(Player player) {
        players.add(player);
        player.teleport(ConfigUtils.locFromConfig(eventConfig.getConfigurationSection("spec-location")));
        player.getInventory().setItem(8, star);
        //add to team and scoreboard
    }

    public void leaveEvent(Player player) {
        players.remove(player);
        spectators.remove(player);
        player.getInventory().clear();
        player.teleport(ConfigUtils.getSpawnLoc());
        //remove from team and scoreboard
    }

    public void specEvent(Player player) {
        spectators.add(player);
        player.teleport(ConfigUtils.locFromConfig(eventConfig.getConfigurationSection("spec-location")));
        player.getInventory().setItem(8, star);
    }

    protected void loseEvent(Player player) {
        players.remove(player);
        specEvent(player);
    }

    protected void playerWon(Player player) {
        for (int i = 0; i < 4; i++) {
            Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " won the " + name + ChatColor.YELLOW + " event!");
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                stop();
            }
        }, 100);
    }

    protected void spawnTpPlayers() {
        for (Player player : players) {
            player.getInventory().clear();
            player.teleport(ConfigUtils.getSpawnLoc());
        }

        for (Player player : spectators) {
            player.getInventory().clear();
            player.teleport(ConfigUtils.getSpawnLoc());
        }
    }

    /*
    TODO:
        - Scoreboards/Teams
     */
}