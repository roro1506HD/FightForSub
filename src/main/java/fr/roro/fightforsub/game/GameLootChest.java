package fr.roro.fightforsub.game;

import fr.roro.fightforsub.FightForSub;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class GameLootChest implements Runnable {

    private static final Random      RANDOM = new Random();
    private static final ItemStack[] ITEMS;

    private final Location      location;
    private final Chicken       vehicle;
    private final ArmorStand    chest;
    private final Slime         leash;
    private final List<Chicken> parachute;
    private final BukkitTask    task;
    private       double        theta;
    private       Block         chestBlock;

    public GameLootChest(Location location) {
        this.location = location.clone();
        location.setY(90);

        this.vehicle = location.getWorld().spawn(location, Chicken.class);
        this.chest = location.getWorld().spawn(location, ArmorStand.class);
        this.leash = location.getWorld().spawn(location, Slime.class);

        this.leash.setSize(1);
        this.chest.setHelmet(new ItemStack(Material.CHEST));
        this.vehicle.setPassenger(this.chest);
        this.chest.setPassenger(this.leash);

        this.leash.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 666666, 255, true, false), true);
        this.vehicle.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 666666, 255, true, false), true);
        this.chest.setVisible(false);

        this.parachute = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Chicken chicken = location.getWorld()
                    .spawn(location.clone().add(randomDouble(), 5, randomDouble()), Chicken.class);
            chicken.setLeashHolder(this.leash);
            this.parachute.add(chicken);
        }

        for (Player player : Bukkit.getOnlinePlayers())
            player.playSound(location, Sound.FIREWORK_BLAST, 1000.0F, 0.5F);

        this.task = Bukkit.getScheduler().runTaskTimer(FightForSub.getInstance(), this, 1L, 1L);
    }

    public void reset() {
        try {
            if (this.chestBlock != null) {
                ((Chest) this.chestBlock.getState()).getBlockInventory().clear();
                this.chestBlock.setType(Material.AIR);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.task.cancel();
        this.leash.remove();
        this.vehicle.remove();
        this.chest.remove();
        this.parachute.forEach(Entity::remove);
    }

    @Override
    public void run() {
        if (this.vehicle.isOnGround()) {
            this.task.cancel();
            Location chestLocation = this.vehicle.getLocation();
            this.leash.remove();
            this.vehicle.remove();
            this.chest.remove();
            this.parachute.forEach(Entity::remove);

            if (chestLocation.getBlock().getType().isSolid())
                chestLocation.add(0, 1, 0);

            this.chestBlock = chestLocation.getBlock();
            this.chestBlock.setType(Material.CHEST);
            ((Chest) this.chestBlock.getState()).getBlockInventory().setItem(13, ITEMS[RANDOM.nextInt(7)]);

            for (int i = 0; i < 40; i++) {
                double angle = 2 * Math.PI * i / 40;
                double x = Math.cos(angle) * 1.5;
                double z = Math.sin(angle) * 1.5;
                Location particleLocation = chestLocation.clone().add(x, 0, z);

                sendParticle(particleLocation, new Vector(0, 1, 0));
                sendParticle(particleLocation, particleLocation.clone().subtract(this.location).toVector().normalize());
            }
            return;
        }

        for (double d = 0.0D; d <= 2 * Math.PI; d += Math.PI / 2) {
            Location particleLocation = this.location.clone()
                    .add(Math.cos(this.theta + d) * 0.5, 0.1, Math.sin(this.theta + d) * 0.5);
            Location reversedParticleLocation = this.location.clone()
                    .add(Math.cos(-this.theta + d) * 0.5, 0.1, Math.sin(-this.theta + d) * 0.5);

            sendParticle(particleLocation, particleLocation.clone().subtract(this.location).toVector().normalize());
            sendParticle(reversedParticleLocation,
                    reversedParticleLocation.clone().subtract(this.location).toVector().normalize());
        }

        this.theta += Math.PI / 40.0D;
    }

    private double randomDouble() {
        return Math.random() < 0.5D ? ((1.0D - Math.random()) * 3.0D - 1.5D) : (Math.random() * 3.0D - 1.5D);
    }

    private void sendParticle(Location location, Vector vector) {
        location.getWorld().getNearbyEntities(location, 64.0D, 64.0D, 64.0D).stream()
                .filter(Player.class::isInstance)
                .map(CraftPlayer.class::cast)
                .forEach(player -> player.getHandle().playerConnection.sendPacket(
                        new PacketPlayOutWorldParticles(EnumParticle.FLAME, true, (float) location.getX(),
                                (float) location.getY(), (float) location.getZ(), (float) vector.getX(),
                                (float) vector.getY(), (float) vector.getZ(), 0.5F, 0)));
    }

    static {
        ITEMS = new ItemStack[7];

        ITEMS[0] = new ItemStack(Material.SAND, 16);
        ITEMS[1] = new ItemStack(Material.POTION, 1, (short) 16388);
        ITEMS[2] = new ItemStack(Material.ROTTEN_FLESH);
        ITEMS[3] = new ItemStack(Material.POTION, 1, (short) 8235);

        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("§c§lBON TOUTOU ! :)");
        item.setItemMeta(itemMeta);

        ITEMS[4] = item;

        item = new ItemStack(Material.POTION, 1, (short) 8194);
        itemMeta = item.getItemMeta();
        ((PotionMeta) itemMeta).addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 1), true);
        item.setItemMeta(itemMeta);

        ITEMS[5] = item;

        item = new ItemStack(Material.POTION, 1, (short) 8227);
        itemMeta = item.getItemMeta();
        ((PotionMeta) itemMeta).clearCustomEffects();
        ((PotionMeta) itemMeta).addCustomEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0), true);
        itemMeta.setLore(Collections.singletonList("§eCette potion vous donne §62 coeurs d'absorption"));
        item.setItemMeta(itemMeta);

        ITEMS[6] = item;
    }
}
