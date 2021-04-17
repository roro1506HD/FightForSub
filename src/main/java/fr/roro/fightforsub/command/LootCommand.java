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
public class LootCommand extends Command {

    public LootCommand() {
        super("loot");
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
            sender.sendMessage("§cUtilisation : /loot <nombre>");
            return false;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("§cLe nombre que vous avez entré n'est pas valide.");
            return false;
        }

        for (int i = 0; i < amount; i++)
            FightForSub.getInstance().dropLootChest();
        return true;
    }
}
