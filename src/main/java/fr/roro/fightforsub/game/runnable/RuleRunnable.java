package fr.roro.fightforsub.game.runnable;

import fr.roro.fightforsub.FightForSub;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class RuleRunnable {

    private final Runnable callback;

    public RuleRunnable(Runnable callback) {
        this.callback = callback;
        this.sendStartMessage();
    }

    private void sendStartMessage() {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(
                "§6§lLa partie va bientôt commencer ! Toute déconnexion à partir de maintenant est considérée comme définitive.");

        for (Player player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 50.0F, 1.0F);

        this.sendTeleportMessage();
    }

    private void sendTeleportMessage() {
        Bukkit.getScheduler().runTaskLater(FightForSub.getInstance(), () -> {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§6§lVous allez être téléporté en cercle dans une arène prévue à cet effet.");

            for (Player player : Bukkit.getOnlinePlayers())
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 50.0F, 1.0F);

            this.sendInvincibilityMessage();
        }, 20L);
    }

    private void sendInvincibilityMessage() {
        Bukkit.getScheduler().runTaskLater(FightForSub.getInstance(), () -> {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§6§lAu démarrage de la partie, vous aurez §a§l30 secondes §6§ld'invincibilité.");

            for (Player player : Bukkit.getOnlinePlayers())
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 50.0F, 1.0F);

            this.start();
        }, 20L);
    }

    private void start() {
        Bukkit.getScheduler().runTaskLater(FightForSub.getInstance(), this.callback, 20L);
    }
}
