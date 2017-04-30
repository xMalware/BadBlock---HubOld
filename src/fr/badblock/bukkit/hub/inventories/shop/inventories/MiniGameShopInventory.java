package fr.badblock.bukkit.hub.inventories.shop.inventories;

import fr.badblock.bukkit.hub.inventories.abstracts.inventories.CustomInventory;
import fr.badblock.bukkit.hub.inventories.settings.items.SkullSettingsItem;
import fr.badblock.bukkit.hub.inventories.settings.settings.LightBlueStainedGlassPaneItem;
import fr.badblock.bukkit.hub.inventories.shop.QuitShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.BoosterMiniGamesShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.Chest1MiniGamesShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.Chest2MiniGamesShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.Chest3MiniGamesShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.DiamondMiniGamesShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.EmeraldMiniGamesShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.FactionGameShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.FreeBuildGameShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.GoldMiniGamesShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.GrayStainedGlassPaneShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.MiniGamesShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.ObsidianMiniGamesShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.PvPBoxGameShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.RedStainedGlassPaneShopItem;
import fr.badblock.bukkit.hub.inventories.shop.items.SkyBlockGameShopItem;

public class MiniGameShopInventory extends CustomInventory {

	public MiniGameShopInventory() {
		super("hub.items.minigameshopinventory", 5);
		LightBlueStainedGlassPaneItem blueStainedGlassPaneItem = new LightBlueStainedGlassPaneItem();
		this.setItem(blueStainedGlassPaneItem, 0, 8, 9, 17, 18, 26, 27, 35, 36);
		this.setItem(1, new SkullSettingsItem());
		this.setItem(2, new MiniGamesShopItem());
		this.setItem(3, new FactionGameShopItem());
		this.setItem(4, new SkyBlockGameShopItem());
		this.setItem(5, new FreeBuildGameShopItem());
		this.setItem(6, new PvPBoxGameShopItem());
		this.setItem(10, new GrayStainedGlassPaneShopItem());
		this.setItem(11, new RedStainedGlassPaneShopItem());
		this.setItem(12, new GrayStainedGlassPaneShopItem());
		this.setItem(13, new GrayStainedGlassPaneShopItem());
		this.setItem(14, new GrayStainedGlassPaneShopItem());
		this.setItem(15, new GrayStainedGlassPaneShopItem());
		this.setItem(16, new GrayStainedGlassPaneShopItem());
		this.setItem(19, new GoldMiniGamesShopItem());
		this.setItem(20, new DiamondMiniGamesShopItem());
		this.setItem(21, new EmeraldMiniGamesShopItem());
		this.setItem(22, new ObsidianMiniGamesShopItem());
		this.setItem(28, new Chest1MiniGamesShopItem());
		this.setItem(29, new Chest2MiniGamesShopItem());
		this.setItem(30, new Chest3MiniGamesShopItem());
		this.setItem(37, new BoosterMiniGamesShopItem());
		this.setAsLastItem(new QuitShopItem());
	}

}