
package fr.badblock.bukkit.hub.inventories.shop.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import fr.badblock.bukkit.hub.inventories.abstracts.actions.ItemAction;
import fr.badblock.bukkit.hub.inventories.abstracts.inventories.CustomInventory;
import fr.badblock.bukkit.hub.inventories.abstracts.items.CustomItem;
import fr.badblock.bukkit.hub.inventories.shop.inventories.SkyBlockShopInventory;
import fr.badblock.bukkit.hub.objects.HubPlayer;
import fr.badblock.gameapi.players.BadblockPlayer;

public class SkyBlockGameShopItem extends CustomItem {

	public SkyBlockGameShopItem() {
		super("hub.items.skyblockgameshopitem", Material.DIRT);
	}

	@Override
	public List<ItemAction> getActions() {
		return Arrays.asList(ItemAction.INVENTORY_DROP, ItemAction.INVENTORY_LEFT_CLICK,ItemAction.INVENTORY_RIGHT_CLICK, ItemAction.INVENTORY_WHEEL_CLICK);
	}

	@Override
	public void onClick(BadblockPlayer player, ItemAction itemAction, Block clickedBlock) {
		HubPlayer hubPlayer = HubPlayer.get(player);
		CustomInventory customInventory = CustomInventory.get(SkyBlockShopInventory.class);
		hubPlayer.setShopInventory(customInventory);
		customInventory.open(player);
	}

}