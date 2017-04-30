package fr.badblock.bukkit.hub.inventories.selector.submenus.items.rush;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import fr.badblock.bukkit.hub.inventories.abstracts.actions.ItemAction;
import fr.badblock.bukkit.hub.inventories.abstracts.items.CustomItem;
import fr.badblock.gameapi.players.BadblockPlayer;

public class RushNBPaperItem extends CustomItem {

	public RushNBPaperItem() {
		super("hub.items.rushselectoritem.paper_nb_displayname", Material.PAPER, "hub.items.rushselectoritem.paper_nb_lore");
	}

	@Override
	public List<ItemAction> getActions() {
		return Arrays.asList(ItemAction.INVENTORY_DROP, ItemAction.INVENTORY_LEFT_CLICK, ItemAction.INVENTORY_RIGHT_CLICK, ItemAction.INVENTORY_WHEEL_CLICK);
	}

	@Override
	public void onClick(BadblockPlayer player, ItemAction itemAction, Block clickedBlock) {

	}

}