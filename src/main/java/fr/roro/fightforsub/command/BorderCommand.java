package fr.roro.fightforsub.command;

import fr.roro.fightforsub.FightForSub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class BorderCommand extends Command {

    public BorderCommand() {
        super("border");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is restricted to physical players.");
            return false;
        }

        FightForSub.getInstance().getGameLoop().shrinkBorder(20);
        return true;
    }
}
