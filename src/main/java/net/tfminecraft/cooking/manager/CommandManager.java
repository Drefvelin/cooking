package net.tfminecraft.cooking.manager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.tfminecraft.cooking.utils.ItemBuilder;

public class CommandManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§e/cooking builditem <string>");
            return true;
        }

        if (args[0].equalsIgnoreCase("builditem")) {

            if (args.length < 2) {
                player.sendMessage("§cUsage: /cooking builditem <itemString>");
                return true;
            }

            // Combine the full item string (supports spaces)
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) builder.append(" ");
                builder.append(args[i]);
            }
            String itemString = builder.toString();
            ItemBuilder.buildFromString(player, itemString, null);

            player.sendMessage("§aGenerated item(s) from string!");
            return true;
        }

        player.sendMessage("§e/cooking builditem <string>");
        return true;
    }
}
