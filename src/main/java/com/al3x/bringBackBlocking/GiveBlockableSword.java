package com.al3x.bringBackBlocking;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GiveBlockableSword implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!commandSender.hasPermission("bbb.givesword")) {
            commandSender.sendMessage("You do not have permission to use this command!");
            return true;
        }

        if (strings.length != 2) {
            commandSender.sendMessage("Usage: /givesword <player> <sword>");
            return true;
        }

        Player player = Bukkit.getPlayer(strings[0]);
        if (player == null) {
            commandSender.sendMessage("Player not found!");
            return true;
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:give " + player.getName() + " " + strings[1] + "[consumable={animation:'block',consume_seconds:72000}]");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> completions = new ArrayList<>();

        if (!commandSender.hasPermission("bbb.givesword")) {
            return completions;
        }

        if (strings.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }

        if (strings.length == 2) {
            completions.add("netherite_sword");
            completions.add("diamond_sword");
            completions.add("golden_sword");
            completions.add("iron_sword");
            completions.add("stone_sword");
            completions.add("wooden_sword");
        }

        return completions;
    }
}
