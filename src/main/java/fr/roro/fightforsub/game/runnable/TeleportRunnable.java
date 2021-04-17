package fr.roro.fightforsub.game.runnable;

import com.google.common.base.Preconditions;
import fr.roro.fightforsub.FightForSub;
import fr.roro.fightforsub.game.GamePlayer;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class TeleportRunnable implements Runnable {

    private final Queue<Location>   locations;
    private final Queue<GamePlayer> players;
    private final Runnable          callback;
    private final BukkitTask        task;
    private final int               totalPlayers;

    public TeleportRunnable(List<Location> locations, List<GamePlayer> players, Runnable callback) {
        Preconditions.checkArgument(locations.size() == players.size());

        Collections.shuffle(locations);
        Collections.shuffle(players);

        this.locations = new ArrayDeque<>(locations);
        this.players = new ArrayDeque<>(players);
        this.callback = callback;
        this.totalPlayers = players.size();

        this.task = Bukkit.getScheduler().runTaskTimer(FightForSub.getInstance(), this, 4L, 4L);
    }

    @Override
    public void run() {
        if (this.players.isEmpty()) {
            this.task.cancel();
            this.callback.run();
            return;
        }

        GamePlayer player = this.players.remove();
        Location location = this.locations.remove();

        FightForSub.getInstance().getAllPlayers().stream()
                .filter(GamePlayer::isOnline)
                .forEach(gamePlayer -> gamePlayer.getPlayer().getHandle().playerConnection.sendPacket(new PacketPlayOutChat(new ChatComponentText("§7Téléportation de §e" + player.getName() + " §7(§c" + (this.totalPlayers - this.players.size()) + "§7/§c" + this.totalPlayers + "§7)"), (byte) 2)));

        if (!player.isOnline())
            return;

        player.teleport(location);
        player.getPlayer().setWalkSpeed(0.0F);
        player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 666666, 250, true, false), true);
        player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 666666, 250, true, false), true);
        player.getPlayer().setFoodLevel(1);
    }
}
