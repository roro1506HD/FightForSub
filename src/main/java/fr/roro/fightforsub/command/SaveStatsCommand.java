package fr.roro.fightforsub.command;

import fr.roro.fightforsub.FightForSub;
import fr.roro.fightforsub.game.GameStatus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class SaveStatsCommand extends Command {

    public SaveStatsCommand() {
        super("savestats");
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

        if (FightForSub.getInstance().getStatus() != GameStatus.WAITING) {
            sender.sendMessage(
                    "§cUne partie est en cours ! Veuillez attendre la fin de celle-ci pour utiliser cette commande.");
            return true;
        }

//        FightForSub.getInstance().writeGameFile();
//        sender.sendMessage("§aVous avez sauvegardé les statistiques de la partie.");
        sender.sendMessage("§cCette commande est actuellement désactivée.");
        return true;
    }
}
