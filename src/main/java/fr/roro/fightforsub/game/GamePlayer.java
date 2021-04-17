package fr.roro.fightforsub.game;

import fr.roro.fightforsub.FightForSub;
import fr.roro.fightforsub.game.team.TeamNameHider;
import fr.roro.fightforsub.util.ScoreboardSign;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class GamePlayer {

    private static final Random        RANDOM     = new Random();
    private static final TeamNameHider NAME_HIDER = new TeamNameHider();

    private final List<String>   hidedPlayers;
    private       String         name;
    private       CraftPlayer    player;
    private       ScoreboardSign scoreboard;
    private       boolean        spectator;
    private       boolean        alive;
    private       int            totalKills;
    private       int            kills;
    private       int            lives;
    private       int            minLives;
    private       int            borderTimer;
    private       int            invulnerabilityTimer;
    private       int            invulnerability;
    private       int            rank;
    private       Location       teleportLocation;

    public GamePlayer(Player player) {
        System.out.println("Registering " + player.getName() + "...");
        this.lives = 3;
        this.hidedPlayers = new ArrayList<>();
    }

    public void onConnect(Player player) {
        System.out.println("Connecting " + player.getName() + "...");
        this.player = (CraftPlayer) player;
        this.scoreboard = new ScoreboardSign(player, "§a§lFIGHT FOR §f§lSUB §b§l#10");
        this.name = player.getName();
        this.createScoreboard();

        this.player.getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(NAME_HIDER, 0));

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setExp(0.0F);
        player.setLevel(0);
        player.setFoodLevel(20);
        player.setHealth(20.0D);
        player.setWalkSpeed(0.2F);

        ((CraftPlayer) player).getHandle().playerConnection
                .sendPacket(new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));

        FightForSub.getInstance().getAllPlayers().stream()
                .filter(GamePlayer::isOnline)
                .forEach(gamePlayer -> gamePlayer.scoreboard
                        .setLine(5, "Joueurs restants : §a" + FightForSub.getInstance().getAlivePlayers().size()));

        player.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);

        if (FightForSub.getInstance().getStatus() == GameStatus.WAITING)
            this.alive = true;

        player.teleport(FightForSub.SPAWN);
        player.setGameMode(GameMode.ADVENTURE);

        FightForSub.getInstance().getAllPlayers().stream()
                .filter(GamePlayer::isOnline)
                .forEach(gamePlayer -> gamePlayer.scoreboard
                        .setLine(5, "Joueurs restants : §a" + FightForSub.getInstance().getAlivePlayers().size()));
    }

    public void onDisconnect() {
        boolean alive = this.alive;

        this.alive = false;
        if (alive && (FightForSub.getInstance().getStatus() == GameStatus.IN_GAME ||
                FightForSub.getInstance().getStatus() == GameStatus.TELEPORTING)) {

            int alivePlayers = FightForSub.getInstance().getAlivePlayers().size();
            this.rank = alivePlayers + 1;

            Bukkit.broadcastMessage(
                    "§c" + this.player.getName() + " §7s'est déconnecté. Il est par conséquent éliminé.");

            if (alivePlayers == 1)
                FightForSub.getInstance().endRound();
        }

        FightForSub.getInstance().getAllPlayers().stream()
                .filter(GamePlayer::isOnline)
                .forEach(gamePlayer -> gamePlayer.scoreboard
                        .setLine(5, "Joueurs restants : §a" + FightForSub.getInstance().getAlivePlayers().size()));

        this.player.getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(NAME_HIDER, 1));

        this.hidedPlayers.clear();
        this.alive = false;
        this.player = null;
        this.scoreboard.destroy();
        this.scoreboard = null;
    }

    public void reset() {
        this.kills = 0;
        this.lives = 3;
        this.rank = 0;
        this.invulnerability = 0;
        this.invulnerabilityTimer = 0;
        new ArrayList<>(this.hidedPlayers).forEach(this::showName);

        if (this.player == null)
            return;

        this.alive = true;
        this.scoreboard.setLine(7, "Durée : §600m00s");

        FightForSub.getInstance().getAllPlayers().stream()
                .filter(GamePlayer::isOnline)
                .forEach(gamePlayer -> gamePlayer.scoreboard
                        .setLine(5, "Joueurs restants : §a" + FightForSub.getInstance().getAlivePlayers().size()));

        //this.scoreboard.setLine(4, "Manche : §3" + FightForSub.getInstance().getRound());

        this.player.getInventory().clear();
        this.player.getInventory().setArmorContents(null);
        this.player.teleport(FightForSub.SPAWN);
        this.player.setGameMode(GameMode.ADVENTURE);
        this.player.setHealth(20.0D);
    }

    public void giveArmor() {
        ItemStack helmet = new ItemStack(this.lives == 3 ? Material.IRON_HELMET : Material.LEATHER_HELMET);
        ItemMeta itemMeta = helmet.getItemMeta();

        if (itemMeta instanceof LeatherArmorMeta)
            ((LeatherArmorMeta) itemMeta).setColor(Color.GREEN);

        itemMeta.spigot().setUnbreakable(true);
        helmet.setItemMeta(itemMeta);

        ItemStack chestplate = new ItemStack(this.lives == 1 ? Material.LEATHER_CHESTPLATE : Material.IRON_CHESTPLATE);
        itemMeta = chestplate.getItemMeta();
        itemMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, 1, true);

        if (itemMeta instanceof LeatherArmorMeta)
            ((LeatherArmorMeta) itemMeta).setColor(Color.GREEN);

        itemMeta.spigot().setUnbreakable(true);
        chestplate.setItemMeta(itemMeta);

        ItemStack leggings = new ItemStack(this.lives == 3 ? Material.IRON_LEGGINGS : Material.LEATHER_LEGGINGS);
        itemMeta = leggings.getItemMeta();
        itemMeta.addEnchant(Enchantment.PROTECTION_PROJECTILE, 1, true);

        if (itemMeta instanceof LeatherArmorMeta)
            ((LeatherArmorMeta) itemMeta).setColor(Color.WHITE);

        itemMeta.spigot().setUnbreakable(true);
        leggings.setItemMeta(itemMeta);

        ItemStack boots = new ItemStack(this.lives == 1 ? Material.LEATHER_BOOTS : Material.IRON_BOOTS);
        itemMeta = boots.getItemMeta();

        if (itemMeta instanceof LeatherArmorMeta)
            ((LeatherArmorMeta) itemMeta).setColor(Color.WHITE);

        itemMeta.spigot().setUnbreakable(true);
        boots.setItemMeta(itemMeta);

        this.player.getInventory().setHelmet(helmet);
        this.player.getInventory().setChestplate(chestplate);
        this.player.getInventory().setLeggings(leggings);
        this.player.getInventory().setBoots(boots);
    }

    public void giveStuff() {
        ItemStack stoneAxe = new ItemStack(Material.STONE_SWORD);
        ItemMeta stoneAxeMeta = stoneAxe.getItemMeta();
        stoneAxeMeta.spigot().setUnbreakable(true);
        stoneAxe.setItemMeta(stoneAxeMeta);

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
        bowMeta.spigot().setUnbreakable(true);
        bow.setItemMeta(bowMeta);

        this.player.getInventory().clear();

        this.player.getInventory().setItem(0, stoneAxe);
        this.player.getInventory().setItem(1, bow);
        this.player.getInventory().setItem(2, new ItemStack(Material.SAND, 32));
        this.player.getInventory().setItem(9, new ItemStack(Material.ARROW));

        this.player.getInventory().setHeldItemSlot(0);
    }

    private void createScoreboard() {
        this.scoreboard.create();
        this.scoreboard.setLine(8, "§a");
        this.scoreboard.setLine(7, "Durée : §600m00s");
        this.scoreboard.setLine(6, "§a");
        this.scoreboard.setLine(5, "Joueurs restants : §a" + FightForSub.getInstance().getAlivePlayers().size());
        //this.scoreboard.setLine(4, "Manche : §3" + FightForSub.getInstance().getRound());
        this.scoreboard.setLine(3, "§a");
        this.scoreboard.setLine(2, /*"Kills (§emanche§r) : §e"*/"Kills : §e" + this.kills);
        //this.scoreboard.setLine(1, "Kills (§ctotal§r) : §c" + this.totalKills);
        this.scoreboard.setLine(0, "§a");
    }

    public void incrementKills() {
        this.totalKills++;
        this.kills++;
        this.scoreboard.setLine(2, /*"Kills (§emanche§r) : §e"*/"Kills : §e" + this.kills);
        //this.scoreboard.setLine(1, "Kills (§ctotal§r) : §c" + this.totalKills);
    }

    public boolean decrementLives() {
        if (--this.lives - this.minLives != 0)
            return true;

        this.alive = false;

        int alivePlayers = FightForSub.getInstance().getAlivePlayers().size();
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        skullMeta.setOwner(this.player.getName());
        skull.setItemMeta(skullMeta);

        //this.player.getWorld().dropItemNaturally(this.player.getLocation(), skull);
        this.player.getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(10, 80, 10));
        this.player.getHandle().playerConnection.sendPacket(
                new PacketPlayOutTitle(EnumTitleAction.TITLE, new ChatComponentText("§cVous êtes éliminé !")));
        this.player.sendMessage(
                "§aVous avez terminé §6#" + (alivePlayers + 1) + " §aavec §b" +
                        this.kills + "§a kill(s) ! Cela vous fait un total de §b" + this.totalKills + " §akill(s) !");

        this.player.setGameMode(GameMode.ADVENTURE);
        this.player.getInventory().clear();
        this.player.getInventory().setArmorContents(null);
        this.player.teleport(FightForSub.SPAWN);

        this.rank = alivePlayers + 1;

        new ArrayList<>(this.hidedPlayers).forEach(this::showName);

        FightForSub.getInstance().getAllPlayers().stream()
                .filter(GamePlayer::isOnline)
                .forEach(gamePlayer -> gamePlayer.scoreboard
                        .setLine(5, "Joueurs restants : §a" + FightForSub.getInstance().getAlivePlayers().size()));

        if (alivePlayers == 1)
            FightForSub.getInstance().endRound();
        return false;
    }

    public void teleport(Location location) {
        if (location == null && this.teleportLocation != null)
            location = this.teleportLocation;
        else if (location != null)
            this.teleportLocation = location;

        this.player.teleport(location);
    }

    public void randomTeleport() {
        World world = Bukkit.getWorlds().get(0);
        double size = world.getWorldBorder().getSize() / 3;

        Location teleportLocation;
        do {
            int x = -(int) size + RANDOM.nextInt((int) size * 2);
            int y = 255;
            int z = -(int) size + RANDOM.nextInt((int) size * 2);

            Material blockType;
            while ((!(blockType = world.getBlockAt(x, y, z).getType()).isSolid() || blockType == Material.BARRIER) &&
                    y > 0)
                y--;

            teleportLocation = new Location(world, x + 0.5, y + 1, z + 0.5);
        } while (teleportLocation.distance(FightForSub.CENTER) > 1500 || teleportLocation.getY() > 150);

        teleportLocation
                .setDirection(FightForSub.CENTER.clone().subtract(teleportLocation.clone()).toVector().normalize());
        this.player.getPlayer().teleport(teleportLocation);
        this.player.getPlayer().setVelocity(new Vector());
    }

    void incrementBorderTimer(Location location) {
        if (++this.borderTimer > 120)
            this.borderTeleport();
        else
            this.player.setVelocity(new Vector(-location.getX(), player.getPlayer().getVelocity().getY(), -location.getZ()).normalize());
    }

    void borderTeleport() {
        this.borderTimer = 0;

        World world = Bukkit.getWorlds().get(0);
        Vector direction = FightForSub.CENTER.clone().subtract(this.player.getLocation()).toVector().normalize();
        Location playerLocation = this.player.getLocation().clone();

        Location teleportLocation = playerLocation.add(direction);
        int x = teleportLocation.getBlockX();
        int y = 255;
        int z = teleportLocation.getBlockZ();

        Material blockType;
        while ((!(blockType = world.getBlockAt(x, y, z).getType()).isSolid() || blockType == Material.BARRIER || blockType == Material.LEAVES || blockType == Material.LEAVES_2 || blockType == Material.LOG || blockType == Material.LOG_2) && y > 0)
            y--;

        teleportLocation.setY(y + 2);

        this.player.getPlayer().teleport(teleportLocation);
    }

    public CraftPlayer getPlayer() {
        return this.player;
    }

    void tick(int timeElapsed) {
        if (this.player == null)
            return;

        String timeFormatted = "";
        int hours = timeElapsed / 60 / 60;
        int minutes = timeElapsed / 60 % 60;
        int seconds = timeElapsed % 60;

        if (hours > 0)
            timeFormatted += String.format("%02dh", hours);

        timeFormatted += String.format("%02dm", minutes);

        if (hours == 0)
            timeFormatted += String.format("%02ds", seconds);

        this.scoreboard.setLine(7, "Durée : §6" + timeFormatted);

        if (this.spectator || !this.alive)
            return;

        if (this.invulnerabilityTimer > 0) {
            this.player.setLevel(--this.invulnerabilityTimer);
            this.player.setExp(this.invulnerabilityTimer * 1.0F / (float) this.invulnerability);

            if (this.invulnerabilityTimer == 0)
                this.player.sendMessage("§cLa période d'invulnérabilité est terminée.");
        }

        this.player.getHandle().playerConnection.sendPacket(new PacketPlayOutChat(new ChatComponentText(
                String.format("%1$s»%2$s»%1$s»%2$s» %3$s %2$s«%1$s«%2$s«%1$s«", timeElapsed % 2 == 0 ? "§4§l" : "§c§l",
                        timeElapsed % 2 == 0 ? "§c§l" : "§4§l",
                        String.format("§6§l%d BARRE%2$s DE VIE RESTANTE%2$s", this.lives - this.minLives,
                                this.lives - this.minLives == 1 ? "" : "S"))),
                (byte) 2));
    }

    void hideName(String name) {
        if (this.hidedPlayers.contains(name))
            return;

        this.hidedPlayers.add(name);

        this.player.getHandle().playerConnection
                .sendPacket(new PacketPlayOutScoreboardTeam(NAME_HIDER, Collections.singletonList(name), 3));
    }

    void showName(String name) {
        if (!this.hidedPlayers.contains(name))
            return;

        this.hidedPlayers.remove(name);

        this.player.getHandle().playerConnection
                .sendPacket(new PacketPlayOutScoreboardTeam(NAME_HIDER, Collections.singletonList(name), 4));
    }

    public boolean isSpectator() {
        return this.spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public boolean isOnline() {
        return this.player != null;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getKills() {
        return this.kills;
    }

    public int getTotalKills() {
        return this.totalKills;
    }

    public boolean isInvulnerable() {
        return this.invulnerabilityTimer > 0;
    }

    public void setInvulnerable(int seconds) {
        this.invulnerabilityTimer = seconds;
        this.invulnerability = seconds;
    }

    public int getMinLives() {
        return this.minLives;
    }

    public void setMinLives(int minLives) {
        this.minLives = minLives;
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return this.name;
    }

    public ScoreboardSign getScoreboard() {
        return this.scoreboard;
    }
}
