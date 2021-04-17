package fr.roro.fightforsub.game;

import fr.roro.fightforsub.FightForSub;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class GameLoop implements Runnable {

    private int size = 261;
    private long tick;

    @Override
    public void run() {
        this.tick++;

        if (this.tick % 10 == 0)
            this.tick();

        List<GamePlayer> alivePlayers = FightForSub.getInstance().getAlivePlayers();

        if (this.tick % 5 == 0)
            alivePlayers.stream()
                    .filter(player -> player.getPlayer() != null)
                    .forEach(player -> {
                        Location location = player.getPlayer().getLocation();
                        double borderSize = location.getWorld().getWorldBorder().getSize() / 2.0D;

                        if (location.getX() < -borderSize - 2 || location.getX() > borderSize + 2 || location.getZ() < -borderSize - 2 || location.getZ() > borderSize + 2)
                            player.borderTeleport();
                        else if (location.getX() < -borderSize + 0.1 || location.getX() > borderSize + 0.1 || location.getZ() < -borderSize + 0.1 || location.getZ() > borderSize + 0.1)
                            player.incrementBorderTimer(location);
                    });
    }

    private void tick() {
        if (this.tick % 20 != 0)
            return;

        int timeElapsed = FightForSub.getInstance().increaseTimeElapsed();
        World world = Bukkit.getWorlds().get(0);

        FightForSub.getInstance().getAllPlayers().forEach(gamePlayer -> gamePlayer.tick(timeElapsed));

        if (timeElapsed % 60 == 0)
            FightForSub.getInstance().dropLootChest();

        if (timeElapsed >= 120 && timeElapsed % 10 == 0)
            this.shrinkBorder(2);

        if (timeElapsed % 2 == 0 && world.getWorldBorder().getSize() > this.size) {
            world.getWorldBorder().setSize(world.getWorldBorder().getSize() - 2, 1);

            for (Player player : Bukkit.getOnlinePlayers())
                player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1000.0F, 1.0F);
        }

        /*List<GamePlayer> alivePlayers = FightForSub.getInstance().getAlivePlayers();

        for (int i = 0; i < alivePlayers.size(); i++) {
            for (int j = 0; j < i; j++) {
                GamePlayer playerOne = alivePlayers.get(i);
                GamePlayer playerTwo = alivePlayers.get(j);

                if (playerOne.getPlayer().getLocation().distanceSquared(playerTwo.getPlayer().getLocation()) > 100.0D) {
                    playerOne.hideName(playerTwo.getName());
                    playerTwo.hideName(playerOne.getName());
                } else {
                    playerOne.showName(playerTwo.getName());
                    playerTwo.showName(playerOne.getName());
                }
            }
        }*/
    }

    public void shrinkBorder(int amount) {
        this.size = Math.max(this.size - amount, 5);
    }

    public void setBorder(int newSize) {
        this.size = newSize;
    }

    public void reset() {
        this.tick = 0;
    }
}
