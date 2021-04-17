package fr.roro.fightforsub.command;

import fr.roro.fightforsub.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class ChatOffCommand extends Command {

    public ChatOffCommand() {
        super("chatoff");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is restricted to physical players.");
            return false;
        }

        if (!sender.isOp()) {
            sender.sendMessage("§cVous n'avez pas accès à cette commande.");
            return false;
        }

        if (!PlayerListener.chat) {
            sender.sendMessage("§cLe chat est déjà retreint");
            return false;
        }

        PlayerListener.chat = false;
        Bukkit.broadcastMessage("§cLe chat est désormais désactivé !");
        return true;
    }
}
