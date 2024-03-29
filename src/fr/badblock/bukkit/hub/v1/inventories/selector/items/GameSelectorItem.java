package fr.badblock.bukkit.hub.v1.inventories.selector.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.badblock.bukkit.hub.v1.inventories.abstracts.items.CustomItem;
import fr.badblock.bukkit.hub.v1.inventories.market.cosmetics.boosters.inventories.RealTimeBoosterManager;
import fr.badblock.bukkit.hub.v1.objects.HubPlayer;
import fr.badblock.bukkit.hub.v1.rabbitmq.listeners.SEntryInfosListener;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.data.boosters.PlayerBooster;
import fr.badblock.gameapi.run.BadblockGame;
import fr.badblock.gameapi.utils.general.TimeUnit;
import fr.badblock.gameapi.utils.i18n.Locale;
import fr.badblock.gameapi.utils.itemstack.ItemStackUtils;
import fr.badblock.gameapi.utils.threading.TaskManager;
import fr.badblock.sentry.FullSEntry;

public abstract class GameSelectorItem extends CustomItem {

	public static HashMap<String, Integer> inGamePlayers = new HashMap<>();
	public static HashMap<String, Integer> waitingLinePlayers = new HashMap<>();

	private long lastRefresh;

	public GameSelectorItem(String name, Material material) {
		this(name, material, (byte) 0, 1, "");
	}

	public GameSelectorItem(String name, Material material, byte data, int amount, String lore) {
		super(name, material, data, amount, lore);
		GameSelectorItem item = this;
		TaskManager.scheduleSyncRepeatingTask("gameselector_" + name, new Runnable() {
			@Override
			public void run() {
				long timestamp = System.currentTimeMillis();
				int tempWaitingLinePlayers = 0;
				int tempInGamePlayers = 0;
				for (String game : getGames()) {
					FullSEntry fullSEntry = SEntryInfosListener.sentries.get(game);
					if (fullSEntry == null) {
						continue;
					}
					tempWaitingLinePlayers += fullSEntry.getWaitinglinePlayers();
					tempInGamePlayers += fullSEntry.getIngamePLayers();
				}
				if (!(waitingLinePlayers.containsKey(getGamePrefix())))
				{
					waitingLinePlayers.put(getGamePrefix(), tempWaitingLinePlayers);
				}
				if (!(inGamePlayers.containsKey(getGamePrefix())))
				{
					inGamePlayers.put(getGamePrefix(), tempInGamePlayers);
				}
				int waitingLinePlayersInt = waitingLinePlayers.get(getGamePrefix());
				int inGamePlayersInt = inGamePlayers.get(getGamePrefix());
				if (waitingLinePlayersInt == tempWaitingLinePlayers && inGamePlayersInt == tempInGamePlayers) {
					if (!RealTimeBoosterManager.stockage.containsKey(getBoosterPrefix()) || (RealTimeBoosterManager.stockage.get(getBoosterPrefix()) != null && (!RealTimeBoosterManager.stockage.get(getBoosterPrefix()).isValid() || !RealTimeBoosterManager.stockage.get(getBoosterPrefix()).isEnabled()))) {
						return;
					}
				}
				if (inGamePlayersInt < 50)
				{
					return;
				}
				if (lastRefresh < timestamp)
				{
					if (waitingLinePlayersInt > tempWaitingLinePlayers) waitingLinePlayersInt--;
					else if (waitingLinePlayersInt < tempWaitingLinePlayers) waitingLinePlayersInt++;
					if (inGamePlayersInt > tempInGamePlayers) inGamePlayersInt--;
					else if (inGamePlayersInt < tempInGamePlayers) inGamePlayersInt++;
					waitingLinePlayers.put(getGamePrefix(), waitingLinePlayersInt);
					inGamePlayers.put(getGamePrefix(), inGamePlayersInt);
					lastRefresh = timestamp + new Random().nextInt(1600) + 300;
				}
				Map<Locale, ItemStack> staticItems = new HashMap<>();
				for (Entry<Locale, ItemStack> entry : staticItem.entrySet())
					staticItems.put(entry.getKey(), rebuildLore(entry.getValue(), entry.getKey()));
				setStaticItem(staticItems);
				for (Player p : Bukkit.getOnlinePlayers()) {
					BadblockPlayer player = (BadblockPlayer) p;
					HubPlayer hubPlayer = HubPlayer.get(player);
					if (hubPlayer.getCurrentInventory() == null)
						continue;
					if (player.getOpenInventory() == null)
						continue;
					if (hubPlayer.getCurrentInventory().getLines() * 9 != player.getOpenInventory().getTopInventory().getSize())
						continue;
					if (!player.getTranslatedMessage(hubPlayer.getCurrentInventory().getName())[0]
							.equals(player.getOpenInventory().getTopInventory().getName()))
						continue;
					if (hubPlayer.getCurrentInventory().getItems().containsValue(item)) {
						getKeysByValue(hubPlayer.getCurrentInventory().getItems(), item).stream().forEach(
								slot -> player.getOpenInventory().getTopInventory().setItem(slot, toItemStack(player)));
					}
				}
			}
		}, 20 * 30, 20 * 30);
	}

	static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map)
	{
		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());

		Collections.sort(sortedEntries, 
				new Comparator<Entry<K,V>>() {
			@Override
			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		return sortedEntries;
	}

	private static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
		return map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

	public GameSelectorItem(String name, Material material, byte data, String lore) {
		this(name, material, data, 1, lore);
	}

	public GameSelectorItem(String name, Material material, int amount, String lore) {
		this(name, material, (byte) 0, amount, lore);
	}

	public GameSelectorItem(String name, Material material, String lore) {
		this(name, material, (byte) 0, 1, lore);
	}

	public abstract boolean isMiniGame();

	public abstract List<String> getGames();

	public abstract String getGamePrefix();
	
	public String getBoosterPrefix()
	{
		return getGamePrefix();
	}

	public abstract BadblockGame getGame();

	public ItemStack rebuildLore(ItemStack itemStack, Locale locale) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (this.isFakeEnchantment()) {
			itemStack = ItemStackUtils.fakeEnchant(itemStack);
		}
		if (this.getLore() != null && !this.getLore().isEmpty()) {
			String boosterLore = GameAPI.i18n().get(locale, "hub.items.booster.nobooster")[0]/*"§cAucun booster activé, on en a pas parlé avant."*/;
			if (RealTimeBoosterManager.stockage.containsKey(this.getBoosterPrefix())) {
				PlayerBooster playerBooster = RealTimeBoosterManager.stockage.get(this.getBoosterPrefix());
				if (playerBooster.isEnabled() && playerBooster.isValid()) {
					boosterLore = GameAPI.i18n().get(locale, "hub.items.booster.boost", playerBooster.getUsername(), (int) ((playerBooster.getBooster().getCoinsMultiplier() - 1) * 100), (int) ((playerBooster.getBooster().getXpMultiplier() - 1) * 100), TimeUnit.SECOND.toShort((playerBooster.getExpire() / 1000L) - (System.currentTimeMillis() / 1000L)))[0]; 
				}
			}
			itemMeta.setLore(Arrays.asList(GameAPI.i18n().get(locale, this.getLore(), inGamePlayers.get(getGamePrefix()), waitingLinePlayers.get(getGamePrefix()),
					(this.getGame() != null ? this.getGame().getDeveloper() : ""), boosterLore)));
		}
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	@Override
	public ItemStack toItemStack(Locale locale) {
		ItemStack itemStack = new ItemStack(this.getMaterial(), this.getAmount(), this.getData());
		if (this.isFakeEnchantment()) {
			itemStack = ItemStackUtils.fakeEnchant(itemStack);
		}
		ItemMeta itemMeta = itemStack.getItemMeta();
		
		String addedString = "";
		
		List<Entry<String, Integer>> list = entriesSortedByValues(inGamePlayers);
		Iterator<Entry<String, Integer>> iterator = list.iterator();
		
		for (int i = 0; i < 3; i++)
		{
			if (!iterator.hasNext())
			{
				break;
			}
			
			Entry<String, Integer> entry = iterator.next();
			if (entry.getKey().equalsIgnoreCase(this.getGamePrefix()))
			{
				addedString = GameAPI.i18n().get(locale, "hub.items.populargame")[0];
				break;
			}
		}
		
		itemMeta.setDisplayName(GameAPI.i18n().get(locale, this.getName(), addedString)[0]);
		
		if (this.getLore() != null && !this.getLore().isEmpty()) {
			String boosterLore = GameAPI.i18n().get(locale, "hub.items.booster.nobooster")[0]/*"§cAucun booster activé, on en a pas parlé avant."*/;
			if (RealTimeBoosterManager.stockage.containsKey(this.getGamePrefix())) {
				PlayerBooster playerBooster = RealTimeBoosterManager.stockage.get(this.getGamePrefix());
				if (playerBooster.isEnabled() && playerBooster.isValid()) {
					boosterLore = GameAPI.i18n().get(locale, "hub.items.booster.boost", playerBooster.getUsername(), (int) ((playerBooster.getBooster().getCoinsMultiplier() - 1) * 100), (int) ((playerBooster.getBooster().getXpMultiplier() - 1) * 100), TimeUnit.SECOND.toShort((playerBooster.getExpire() / 1000L) - (System.currentTimeMillis() / 1000L)))[0]; 
				}
			}
			itemMeta.setLore(Arrays.asList(GameAPI.i18n().get(locale, this.getLore(), inGamePlayers.get(getGamePrefix()), waitingLinePlayers.get(getGamePrefix()),
					(this.getGame() != null ? this.getGame().getDeveloper() : ""), boosterLore)));
		}
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	@Override
	public ItemStack toItemStack(BadblockPlayer player) {
		return toItemStack(player.getPlayerData().getLocale());
	}

}
