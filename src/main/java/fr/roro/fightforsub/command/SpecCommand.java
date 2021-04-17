package fr.roro.fightforsub.command;

import fr.roro.fightforsub.FightForSub;
import fr.roro.fightforsub.game.GamePlayer;
import fr.roro.fightforsub.game.GameStatus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class SpecCommand extends Command {

    public SpecCommand() {
        super("spec");
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
            sender.sendMessage("§cVous ne pouvez pas changer votre mode de jeu durant une partie.");
            return false;
        }

        GamePlayer gamePlayer = FightForSub.getInstance().getPlayer((Player) sender);

        if (gamePlayer.isSpectator())
            sender.sendMessage("§eVous serez compté comme joueur durant la partie.");
        else
            sender.sendMessage("§aVous serez compté comme spectateur durant la partie.");

        gamePlayer.setSpectator(!gamePlayer.isSpectator());
        return true;
    }
}
