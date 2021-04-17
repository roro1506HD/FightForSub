package fr.roro.fightforsub.listener;

import fr.roro.fightforsub.FightForSub;
import fr.roro.fightforsub.game.GamePlayer;
import fr.roro.fightforsub.game.GameStatus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class PlayerListener implements Listener {

    public static boolean chat = true;

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (Bukkit.getOnlinePlayers().size() >= 200 &&
                Bukkit.getWhitelistedPlayers().stream().map(OfflinePlayer::getUniqueId)
                        .noneMatch(uuid -> uuid.equals(event.getUniqueId())))
            event.disallow(Result.KICK_WHITELIST, "Le serveur est plein, merci de réessayer ultérieurement.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FightForSub.getInstance().registerPlayer(event.getPlayer());
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FightForSub.getInstance().unregisterPlayer(event.getPlayer());
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().getName().equals("ZeratoR") || event.getPlayer().getName().equals("Libe_")) {
            event.setFormat("§c§l%s: %s");
            return;
        }

        if (!chat) {
            event.setCancelled(true);
            return;
        }

        event.setFormat("§7%s: %s");
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();

        if (message.startsWith("/me ") || message.startsWith("/minecraft:me ") ||
                message.startsWith("/minecraft:tell ") || message.startsWith("/tell "))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getBlock().hasMetadata("placed")) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);

        FightForSub.getInstance().getPlacedBlocks().remove(event.getBlock());
        event.getBlock().removeMetadata("placed", FightForSub.getInstance());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.SKULL) {
            event.setCancelled(true);
            return;
        }

        FightForSub.getInstance().getPlacedBlocks().add(event.getBlock());
        event.getBlock().setMetadata("placed", new FixedMetadataValue(FightForSub.getInstance(), true));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        GamePlayer gamePlayer = FightForSub.getInstance().getPlayer((Player) event.getEntity());

        if (FightForSub.getInstance().getStatus() != GameStatus.IN_GAME || gamePlayer.isInvulnerable() ||
                !gamePlayer.isAlive())
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player damager = null;
        if (event.getDamager() instanceof Player)
            damager = (Player) event.getDamager();
        else if (event.getDamager() instanceof Projectile &&
                ((Projectile) event.getDamager()).getShooter() instanceof Player)
            damager = (Player) ((Projectile) event.getDamager()).getShooter();

        if (damager != null && FightForSub.getInstance().getPlayer(damager).isInvulnerable())
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType() == SlotType.ARMOR)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.POTION) {
            int slot = event.getPlayer().getInventory().getHeldItemSlot();
            Bukkit.getScheduler()
                    .runTask(FightForSub.getInstance(), () -> event.getPlayer().getInventory().setItem(slot, null));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo().getY() < 100 && (FightForSub.getInstance().getStatus() == GameStatus.WAITING || !FightForSub.getInstance().getPlayer(event.getPlayer()).isAlive()))
            event.getPlayer().teleport(FightForSub.SPAWN);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GamePlayer gamePlayer = FightForSub.getInstance().getPlayer(player);

        event.setDeathMessage(null);

        if (player.getKiller() != null && player.getKiller() != player) {
            TextComponent textComponent = new TextComponent(player.getName());
            textComponent.setColor(ChatColor.RED);
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Cliquez pour vous téléporter sur §c" + player.getName()).create()));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getName()));

            TextComponent partTwo = new TextComponent(" a été tué par ");
            partTwo.setColor(ChatColor.GRAY);
            partTwo.setHoverEvent(null);
            partTwo.setClickEvent(null);

            TextComponent partThree = new TextComponent(player.getKiller().getName());
            partThree.setColor(ChatColor.GREEN);
            partThree.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Cliquez pour vous téléporter sur §a" + player.getKiller().getName()).create()));
            partThree.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getKiller().getName()));


            textComponent.addExtra(partTwo);
            textComponent.addExtra(partThree);

            FightForSub.getInstance().getAllPlayers().stream()
                    .filter(GamePlayer::isOnline)
                    .forEach(tempPlayer -> {
                        if (tempPlayer.isSpectator())
                            tempPlayer.getPlayer().spigot().sendMessage(textComponent);
                        else
                            tempPlayer.getPlayer().sendMessage(textComponent.toLegacyText());
                    });

            FightForSub.getInstance().getPlayer(player.getKiller()).incrementKills();
            player.getKiller().setHealth(Math.min(20.0D, player.getKiller().getHealth() + 6.0D));
        } else {
            TextComponent textComponent = new TextComponent(player.getName());
            textComponent.setColor(ChatColor.RED);
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Cliquez pour vous téléporter sur §c" + player.getName()).create()));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getName()));

            TextComponent partTwo = new TextComponent(" est mort.");
            partTwo.setColor(ChatColor.GRAY);
            partTwo.setHoverEvent(null);
            partTwo.setClickEvent(null);

            textComponent.addExtra(partTwo);

            FightForSub.getInstance().getAllPlayers().stream()
                    .filter(GamePlayer::isOnline)
                    .forEach(tempPlayer -> {
                        if (tempPlayer.isSpectator())
                            tempPlayer.getPlayer().spigot().sendMessage(textComponent);
                        else
                            tempPlayer.getPlayer().sendMessage(textComponent.toLegacyText());
                    });
        }

        player.setHealth(20.0D);
        gamePlayer.setInvulnerable(10);

        boolean gaveSand = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack != null && itemStack.getType() != Material.STONE_SWORD &&
                    itemStack.getType() != Material.BOW && itemStack.getType() != Material.ARROW) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                if (itemStack.getType() == Material.SAND && !gaveSand) {
                    gaveSand = true;
                    player.getInventory().setItem(i, new ItemStack(Material.SAND, 32));
                } else
                    player.getInventory().setItem(i, null);
            }
        }

        if (!gaveSand)
            player.getInventory().addItem(new ItemStack(Material.SAND, 32));

        Bukkit.getScheduler().runTask(FightForSub.getInstance(), () -> {
            if (!gamePlayer.decrementLives())
                return;

            gamePlayer.getPlayer().sendMessage("§cVous êtes mort, vous avez été téléporté dans un autre emplacement de l'arène. Vous possédez une période d'invincibilité de §610 secondes§c.");
            gamePlayer.randomTeleport();
            gamePlayer.giveArmor();
            gamePlayer.getPlayer().getActivePotionEffects().stream()
                    .map(PotionEffect::getType)
                    .forEach(gamePlayer.getPlayer()::removePotionEffect);
        });
    }
}
