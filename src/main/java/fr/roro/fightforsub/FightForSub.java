package fr.roro.fightforsub;

import fr.roro.fightforsub.command.BorderCommand;
import fr.roro.fightforsub.command.ChatOffCommand;
import fr.roro.fightforsub.command.ChatOnCommand;
import fr.roro.fightforsub.command.LootCommand;
import fr.roro.fightforsub.command.MalusCommand;
import fr.roro.fightforsub.command.SpecCommand;
import fr.roro.fightforsub.command.StartCommand;
import fr.roro.fightforsub.game.GameLoop;
import fr.roro.fightforsub.game.GameLootChest;
import fr.roro.fightforsub.game.GamePlayer;
import fr.roro.fightforsub.game.GameStatus;
import fr.roro.fightforsub.game.runnable.RuleRunnable;
import fr.roro.fightforsub.game.runnable.TeleportRunnable;
import fr.roro.fightforsub.listener.EntityListener;
import fr.roro.fightforsub.listener.PlayerListener;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.CraftingManager;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class FightForSub extends JavaPlugin {

    private static final Random      RANDOM = new Random();
    public static        Location    CENTER;
    public static        Location    SPAWN;
    private static       FightForSub instance;

    private final Map<UUID, GamePlayer> playersByUuid = new HashMap<>();
    private final GameLoop              gameLoop      = new GameLoop();
    private final List<GameLootChest>   lootChests    = new ArrayList<>();
    private final List<Block>           placedBlocks  = new ArrayList<>();
    private       BukkitTask            gameLoopTask;
    private       int                   timeElapsed;
    private       int                   round         = 1;
    private       GameStatus            status        = GameStatus.WAITING;
    private       boolean               printedGame;

    @Override
    public void onDisable() {
        if (this.gameLoopTask != null)
            this.gameLoopTask.cancel();

        this.lootChests.forEach(GameLootChest::reset);
        this.lootChests.clear();

        this.placedBlocks.forEach(block -> {
            block.setType(Material.AIR, false);
            block.removeMetadata("placed", this);
        });
        this.placedBlocks.clear();

        new ArrayList<>(Bukkit.getWorlds().get(0).getEntities()).stream()
                .filter(Entity::isValid)
                .filter(((Predicate<Entity>) Player.class::isInstance).negate())
                .filter(((Predicate<Entity>) ItemFrame.class::isInstance).negate())
                .forEach(Entity::remove);

        this.status = GameStatus.WAITING;

        this.playersByUuid.values().stream()
                .filter(gamePlayer -> gamePlayer.getPlayer() != null)
                .forEach(GamePlayer::onDisconnect);
        this.playersByUuid.clear();
    }

    @Override
    public void onEnable() {
        instance = this;
        CENTER = new Location(Bukkit.getWorlds().get(0), 0.0D, 33.0D, 0.0D);
        SPAWN = new Location(Bukkit.getWorlds().get(0), 540.5D, 161.5D, -27.5D, 180.0F, 0.0F);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(), this);
        ((CraftServer) getServer()).getCommandMap().register(getDescription().getName(), new StartCommand());
        ((CraftServer) getServer()).getCommandMap().register(getDescription().getName(), new SpecCommand());
        ((CraftServer) getServer()).getCommandMap().register(getDescription().getName(), new LootCommand());
        ((CraftServer) getServer()).getCommandMap().register(getDescription().getName(), new BorderCommand());
        ((CraftServer) getServer()).getCommandMap().register(getDescription().getName(), new MalusCommand());
        // ((CraftServer) getServer()).getCommandMap().register(getDescription().getName(), new SaveStatsCommand());
        ((CraftServer) getServer()).getCommandMap().register(getDescription().getName(), new ChatOnCommand());
        ((CraftServer) getServer()).getCommandMap().register(getDescription().getName(), new ChatOffCommand());

        Bukkit.getOnlinePlayers().forEach(this::registerPlayer);

        World world = Bukkit.getWorlds().get(0);
        world.setDifficulty(Difficulty.HARD);
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setGameRuleValue("keepInventory", "true");
        world.getWorldBorder().setCenter(0.5D, 0.5D);
        world.getWorldBorder().setSize(5001.0D);
        world.getWorldBorder().setDamageAmount(0.0D);
        world.getWorldBorder().setDamageBuffer(1000.0D);

        CraftingManager.getInstance().getRecipes().removeIf(recipe -> {
            ItemStack itemStack = recipe.b();

            if (itemStack == null)
                return false;

            return Item.getId(itemStack.getItem()) == Material.SANDSTONE.getId();
        });
    }

    public void registerPlayer(Player player) {
        this.playersByUuid.computeIfAbsent(player.getUniqueId(), uuid -> new GamePlayer(player)).onConnect(player);
    }

    public void unregisterPlayer(Player player) {
        this.playersByUuid.get(player.getUniqueId()).onDisconnect();
    }

    public GamePlayer getPlayer(Player player) {
        return this.playersByUuid.get(player.getUniqueId());
    }

    public List<GamePlayer> getAlivePlayers() {
        return this.playersByUuid.values().stream()
                .filter(((Predicate<GamePlayer>) GamePlayer::isSpectator).negate())
                .filter(GamePlayer::isAlive)
                .collect(Collectors.toList());
    }

    private List<GamePlayer> getSpectators() {
        return this.playersByUuid.values().stream()
                .filter(GamePlayer::isOnline)
                .filter(GamePlayer::isSpectator)
                .collect(Collectors.toList());
    }

    public List<GamePlayer> getAllPlayers() {
        return new ArrayList<>(this.playersByUuid.values());
    }

    public void startRound() {
        World world = Bukkit.getWorlds().get(0);
        this.status = GameStatus.TELEPORTING;

        List<GamePlayer> gamePlayers = new ArrayList<>(this.playersByUuid.values());
        List<Location> locations = new ArrayList<>();

        gamePlayers.removeIf(GamePlayer::isSpectator);

        this.getSpectators().forEach(player -> {
            player.getPlayer().setGameMode(GameMode.SPECTATOR);
            player.getPlayer().teleport(CENTER);
            //player.getScoreboard().setLine(4, "Manche : §3" + this.round);
        });

        gamePlayers.forEach(player -> {
            if (!player.isOnline())
                player.setAlive(false);
            else {
                player.getPlayer().getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(10, 999999, 0));
                player.getPlayer().getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, new ChatComponentText("§6Téléportation en cours...")));
                player.getPlayer().getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, new ChatComponentText("§ePensez à lire les règles dans le chat")));
                //player.getScoreboard().setLine(4, "Manche : §3" + this.round);
                player.getScoreboard().setLine(2, "Kills (§emanche§r) : §e0");
            }
        });

        int playerSize = gamePlayers.size();
        for (int i = 0; i < playerSize; ++i) {
            final double angle = Math.toRadians(360.0 / playerSize * i);
            final double x = Math.cos(angle) * 110.0;
            double y = 80.0;
            final double z = Math.sin(angle) * 110.0;
            final Location location = FightForSub.CENTER.clone().add(x, 0.0, z);
            while (!world.getBlockAt((int)x, (int)y, (int)z).getType().isSolid() && y > 0.0) {
                --y;
            }
            location.setY(y + 1.0);
            location.setDirection(FightForSub.CENTER.clone().subtract(location).toVector().normalize());
            locations.add(location);
        }

        CompletableFuture<Void> teleportFuture = new CompletableFuture<>();
        new TeleportRunnable(locations, gamePlayers, () -> teleportFuture.complete(null));

        CompletableFuture<Void> rulesFuture = new CompletableFuture<>();
        new RuleRunnable(() -> rulesFuture.complete(null));

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(teleportFuture, rulesFuture);

        allFuture.whenComplete((aVoid, throwable) -> {
            gamePlayers.stream()
                    .filter(GamePlayer::isAlive)
                    .forEach(player -> {
                        player.teleport(null);
                        player.giveStuff();
                        player.giveArmor();
                        player.getPlayer().setHealth(20.0D);
                        player.getPlayer().setGameMode(GameMode.SURVIVAL);
                        player.setInvulnerable(30);

                        player.getPlayer().setWalkSpeed(0.2F);
                        player.getPlayer().setFoodLevel(20);
                        player.getPlayer().removePotionEffect(PotionEffectType.JUMP);
                        player.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
                        player.getPlayer().getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(0, 20, 20));
                        player.getPlayer().getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.RESET, null));
                    });

            Bukkit.getWorlds().get(0).getWorldBorder().setSize(261.0D);
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§c§lQue le combat commence !");

            for (Player player : Bukkit.getOnlinePlayers())
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 50.0F, 1.0F);

            this.status = GameStatus.IN_GAME;
            this.gameLoopTask = Bukkit.getScheduler().runTaskTimer(this, this.gameLoop, 1L, 1L);
        });
    }

    public void endRound() {
        this.status = GameStatus.FINISHED;
        Bukkit.getWorlds().get(0).getWorldBorder().setSize(5000.0D);
        this.gameLoopTask.cancel();
        this.gameLoop.setBorder(261);
        this.gameLoop.reset();
        this.timeElapsed = 0;

        this.lootChests.forEach(GameLootChest::reset);
        this.lootChests.clear();

        this.placedBlocks.forEach(block -> {
            block.setType(Material.AIR, false);
            block.removeMetadata("placed", this);
        });
        this.placedBlocks.clear();

        if (!this.printedGame)
            this.writeRoundFile();

        GamePlayer winner = getAlivePlayers().get(0);

        winner.setRank(1);
        winner.getPlayer().getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(10, 80, 10));
        winner.getPlayer().getHandle().playerConnection
                .sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, new ChatComponentText("§6Victoire !")));
        Bukkit.broadcastMessage(
                "§a" + winner.getPlayer().getName() + " §7vient de remporter la manche §6" + this.round + " §7avec §b" +
                        winner.getKills() + " §7kills !");
        winner.getPlayer().sendMessage("§aVous avez remporté cette manche avec §6" + winner.getKills() + " §akills !");

        new ArrayList<>(Bukkit.getWorlds().get(0).getEntities()).stream()
                .filter(Entity::isValid)
                .filter(((Predicate<Entity>) Player.class::isInstance).negate())
                .filter(((Predicate<Entity>) ItemFrame.class::isInstance).negate())
                .forEach(Entity::remove);

        winner.getPlayer().setGameMode(GameMode.ADVENTURE);

        new BukkitRunnable() {
            int timer = 10;

            @Override
            public void run() {
                if (this.timer-- == 0 || winner.getPlayer() == null) {
                    cancel();
                    FightForSub.this.round++;
                    FightForSub.this.playersByUuid.values().forEach(GamePlayer::reset);
                    FightForSub.this.status = GameStatus.WAITING;
                    return;
                }

                Firework firework = winner.getPlayer().getWorld()
                        .spawn(winner.getPlayer().getLocation(), Firework.class);
                FireworkEffect effect = FireworkEffect.builder()
                        .with(Type.values()[RANDOM.nextInt(Type.values().length)])
                        .withColor(Color.fromRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256)))
                        .flicker(RANDOM.nextBoolean()).trail(RANDOM.nextBoolean()).build();
                FireworkMeta meta = firework.getFireworkMeta();

                meta.addEffect(effect);
                firework.setFireworkMeta(meta);
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    public void dropLootChest() {
        World world = Bukkit.getWorlds().get(0);
        double size = world.getWorldBorder().getSize() / 2 - 1;

        Location location;
        do {
            int x = -(int) size + RANDOM.nextInt((int) size * 2);
            int y = 200;
            int z = -(int) size + RANDOM.nextInt((int) size * 2);

            Material blockType;
            while ((!(blockType = world.getBlockAt(x, y, z).getType()).isSolid() || blockType == Material.BARRIER) &&
                    y > 0)
                y--;

            location = new Location(world, x + 0.5, y + 1, z + 0.5);
        } while (location.distance(CENTER) > 1000 || location.getY() > 150);

        this.lootChests.add(new GameLootChest(location));
    }

    private void writeRoundFile() {
        new Thread(() -> {
            try {
                PrintWriter writer = new PrintWriter(new File("round-" + this.round + ".csv"));

                this.playersByUuid.values().stream()
                        .filter(((Predicate<GamePlayer>) GamePlayer::isSpectator).negate())
                        .forEach(player -> writer
                                .println(player.getName() + ";" + player.getRank() + ";" + player.getKills()));

                writer.flush();
                writer.close();
            } catch (Exception ex) {
                Bukkit.getScheduler().runTask(this,
                        () -> Bukkit.broadcastMessage(
                                "Une erreur est survenue lors de la création du fichier CSV \"round\"."));
                ex.printStackTrace();
            }
        }).start();
    }

    public void writeGameFile() {
        if (this.printedGame)
            return;

        this.printedGame = true;
        new Thread(() -> {
            try {
                File file = new File("game.csv");

                if (file.exists())
                    file.delete();

                PrintWriter writer = new PrintWriter(file);

                this.playersByUuid.values().stream()
                        .filter(((Predicate<GamePlayer>) GamePlayer::isSpectator).negate())
                        .forEach(player -> writer.println(player.getName() + ";" + player.getTotalKills()));

                writer.flush();
                writer.close();
            } catch (Exception ex) {
                Bukkit.getScheduler().runTask(this,
                        () -> Bukkit.broadcastMessage(
                                "Une erreur est survenue lors de la création du fichier CSV \"game\"."));
                ex.printStackTrace();
            }
        }).start();
    }

    public int increaseTimeElapsed() {
        return ++this.timeElapsed;
    }

    public int getTimeElapsed() {
        return this.timeElapsed;
    }

    public GameStatus getStatus() {
        return this.status;
    }

    public int getRound() {
        return this.round;
    }

    public GameLoop getGameLoop() {
        return this.gameLoop;
    }

    public List<Block> getPlacedBlocks() {
        return this.placedBlocks;
    }

    public static FightForSub getInstance() {
        return instance;
    }
}
