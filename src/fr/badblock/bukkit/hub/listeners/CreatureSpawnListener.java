package fr.badblock.bukkit.hub.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class CreatureSpawnListener extends _HubListener {

	@EventHandler (ignoreCancelled = false)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getSpawnReason().equals(SpawnReason.CUSTOM))
			return;
		event.setCancelled(true);
	}

}