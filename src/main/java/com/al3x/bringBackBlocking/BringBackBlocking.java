package com.al3x.bringBackBlocking;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BringBackBlocking extends JavaPlugin implements Listener {

    FileConfiguration config = this.getConfig();
    List<Player> playersEating = new ArrayList<>();

    @Override
    public void onEnable() {

        config.addDefault("reduce-percentage", 50);
        config.addDefault("reduce-only-on-entity-damage", true);
        config.options().copyDefaults(true);
        saveConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("giveblockablesword").setExecutor(new GiveBlockableSword());

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        // Listen for the block dig packet (for some reason happens when a player stops eating)
        manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (playersEating.contains(player)) {
                    playersEating.remove(player);
                }
            }
        });

        if (!Bukkit.getPluginManager().isPluginEnabled("OldCombatMechanics")) {
            Bukkit.getLogger().warning("[BringBackBlocking] OldCombatMechanics was not found! Swords can be blocked but won't you wont have the old combat mechanics besides that!");
        }

        Bukkit.getLogger().info("[BringBackBlocking] BBB Enabled!");
    }

    // The player is hit, check if they are holding a blocked sword
    // if so reduce the damage
    @EventHandler
    public void onHit(EntityDamageEvent e) {
        if (config.getBoolean("reduce-only-on-entity-damage") && e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        // Victim is a player
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();

        // They are indeed holding an item
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        ItemStack item = player.getInventory().getItemInMainHand();

        // Item has the consumable tag
        if (!isBlockableSword(item)) return;

        // Item is being "consumed"
        if (playersEating.contains(player)) {
            e.setDamage(config.getInt("reduce-percentage") * e.getDamage() / 100);
        }
    }

    // Get when the player starts eating and adds them to the eating list
    @EventHandler
    public void onBlock(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;

        // If the player is holding a consumable item
        if (isBlockableSword(item)) {
            playersEating.add(player);
        }
    }

    // Is the item a sword
    private boolean isSword(ItemStack item) {
        return item.getType() == Material.NETHERITE_SWORD || item.getType() == Material.DIAMOND_SWORD || item.getType() == Material.IRON_SWORD || item.getType() == Material.GOLDEN_SWORD || item.getType() == Material.STONE_SWORD || item.getType() == Material.WOODEN_SWORD;
    }

    // Is the item a sword and does it have the consumable tag
    private boolean isBlockableSword(ItemStack item) {
        if (!isSword(item)) return false;
        // i have no flipping clue what a AtomicBoolean is but i dont care it works
        AtomicBoolean result = new AtomicBoolean(false);
        NBT.getComponents(item, nbt -> {
            if (nbt.hasTag("minecraft:consumable")) {
                result.set(true);
            }
        });
        return result.get();
    }



}
