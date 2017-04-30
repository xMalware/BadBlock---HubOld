package fr.badblock.bukkit.hub.listeners;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.badblock.bukkit.hub.inventories.abstracts.actions.ItemAction;
import fr.badblock.bukkit.hub.inventories.abstracts.items.CustomItem;
import fr.badblock.bukkit.hub.inventories.market.cosmetics.chests.objects.ChestLoader;
import fr.badblock.bukkit.hub.inventories.market.cosmetics.chests.objects.ChestOpener;
import fr.badblock.bukkit.hub.listeners.vipzone.RaceListener;
import fr.badblock.bukkit.hub.objects.HubPlayer;
import fr.badblock.gameapi.players.BadblockPlayer;

public class PlayerInteractListener extends _HubListener {

	@EventHandler (ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event) {
		BadblockPlayer player = (BadblockPlayer) event.getPlayer();
		Action action = event.getAction();
		HubPlayer lobbyPlayer = HubPlayer.get(player);
		if (lobbyPlayer.isChestFreeze()) {
			event.setCancelled(true);
			return;
		}
		if (!player.hasAdminMode())
			event.setCancelled(true);
		if (event.getClickedBlock() != null) {
			Location location = event.getClickedBlock().getLocation();
			for (ChestOpener chestOpener : ChestLoader.getInstance().getOpeners()) {
				Location opener = chestOpener.getOpenerChestLocation();
				if (opener.getBlockX() == location.getBlockX() && opener.getBlockY() == location.getBlockY() && opener.getBlockZ() == location.getBlockZ() && location.getWorld().getName().equals(opener.getWorld().getName())) {
					ChestLoader.getInstance().open(player, chestOpener);
					return;
				}
			}
		}
		// Right click on a basic item
		if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR))
			return;
		ItemStack itemStack = player.getItemInHand();
		ItemAction itemAction = ItemAction.get(action);
		for (Entry<CustomItem, List<ItemStack>> entry : CustomItem.getItems().entrySet()) {
			if (!(entry.getKey() instanceof CustomItem))
				continue;
			CustomItem customItem = entry.getKey();
			if (itemAction == null)
				continue;
			if (!customItem.getActions().contains(itemAction))
				continue;
			if (!customItem.toItemStack(player).isSimilar(itemStack))
				continue;
			if (lobbyPlayer.hasSpam(player))
				return;
			if (RaceListener.racePlayers.containsKey(player)) {
				player.sendTranslatedMessage("hub.race.youcannotdothatinrace");
				return;
			}
			if (customItem.getNeededPermission() != null && !player.hasPermission(customItem.getNeededPermission())) {
				player.sendMessage(customItem.getErrorNeededPermission());
				return;
			}

			customItem.onClick(player, itemAction, event.getClickedBlock());
		}
	}

}