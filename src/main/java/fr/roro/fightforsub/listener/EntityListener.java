package fr.roro.fightforsub.listener;

import fr.roro.fightforsub.FightForSub;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class EntityListener implements Listener {

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(event.toWeatherState());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow)
            event.getEntity().remove();
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Item)
            if (((Item) event.getEntity()).getItemStack().getType() == Material.LEASH)
                event.setCancelled(true);
    }

    @EventHandler
    public void onEntityBlockForm(EntityChangeBlockEvent event) {
        if (event.getTo() == Material.AIR) {
            FightForSub.getInstance().getPlacedBlocks().remove(event.getBlock());
            event.getBlock().removeMetadata("placed", FightForSub.getInstance());
        } else {
            FightForSub.getInstance().getPlacedBlocks().add(event.getBlock());
            event.getBlock().setMetadata("placed", new FixedMetadataValue(FightForSub.getInstance(), true));
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ItemFrame)
            event.setCancelled(true);
    }
}
