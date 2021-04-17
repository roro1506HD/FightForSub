package fr.roro.fightforsub.command;

import fr.roro.fightforsub.FightForSub;
import fr.roro.fightforsub.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class MalusCommand extends Command {

    public MalusCommand() {
        super("malus");
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

        if (args.length == 0) {
            sender.sendMessage("§cUtilisation : /malus <joueur>");
            return false;
        }

        Player player = Bukkit.getPlayerExact(args[0]);
        if (player == null) {
            sender.sendMessage("§cLe joueur spécifié est introuvable !");
            return false;
        }

        GamePlayer gamePlayer = FightForSub.getInstance().getPlayer(player);

        if (gamePlayer.getMinLives() == 1) {
            sender.sendMessage("§aLe joueur §2" + player.getName() + " §ajouera désormais sans le malus.");
            gamePlayer.setMinLives(0);
        } else {
            sender.sendMessage("§eLe joueur §6" + player.getName() + " §ejouera désormais avec le malus.");
            gamePlayer.setMinLives(1);
        }
        return true;
    }
}
