package fr.badblock.bukkit.hub.v1.objects;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.badblock.bukkit.hub.v1.BadBlockHub;
import fr.badblock.bukkit.hub.v1.effectlib.Effect;
import fr.badblock.bukkit.hub.v1.inventories.LinkedInventoryEntity;
import fr.badblock.bukkit.hub.v1.inventories.abstracts.inventories.CustomInventory;
import fr.badblock.bukkit.hub.v1.inventories.connect.ConnectInventory;
import fr.badblock.bukkit.hub.v1.inventories.market.cosmetics.chests.objects.ChestLoader;
import fr.badblock.bukkit.hub.v1.inventories.market.cosmetics.chests.objects.ChestOpener;
import fr.badblock.bukkit.hub.v1.inventories.market.cosmetics.chests.objects.CustomChest;
import fr.badblock.bukkit.hub.v1.inventories.market.cosmetics.chests.objects.CustomChestType;
import fr.badblock.bukkit.hub.v1.inventories.market.cosmetics.disguises.parent.MetamorphosisItem;
import fr.badblock.bukkit.hub.v1.inventories.market.cosmetics.mounts.defaults.MountItem;
import fr.badblock.bukkit.hub.v1.inventories.market.cosmetics.particles.defaults.ParticleItem;
import fr.badblock.bukkit.hub.v1.inventories.market.cosmetics.particles.utils.Wings;
import fr.badblock.bukkit.hub.v1.inventories.market.ownitems.OwnableItem;
import fr.badblock.bukkit.hub.v1.listeners.players.DoubleJumpListener;
import fr.badblock.bukkit.hub.v1.rabbitmq.listeners.ReconnectionInvitationsListener;
import fr.badblock.game.core18R3.players.GameBadblockPlayer;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.databases.SQLRequestType;
import fr.badblock.gameapi.fakeentities.FakeEntity;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.data.InGameData;
import fr.badblock.gameapi.players.data.PlayerData;
import fr.badblock.gameapi.players.data.boosters.PlayerBooster;
import fr.badblock.gameapi.players.scoreboard.BadblockScoreboardGenerator;
import fr.badblock.gameapi.utils.ConfigUtils;
import fr.badblock.gameapi.utils.entities.CustomCreature;
import fr.badblock.gameapi.utils.entities.CustomCreature.CreatureBehaviour;
import fr.badblock.gameapi.utils.general.Callback;
import fr.badblock.gameapi.utils.i18n.TranslatableString;
import fr.badblock.gameapi.utils.threading.TaskManager;
import fr.badblock.gameapi.utils.threading.TempScheduler;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@Getter
@Setter
public class HubPlayer implements InGameData {

	public static int a;
	static {
		TaskManager.scheduleSyncRepeatingTask("ladder_updateplayers", new Runnable() {
			@Override
			public void run() {
				GameAPI.getAPI().getLadderDatabase().sendPing(new String[] { "*" }, new Callback<Integer>() {
					@Override
					public void done(Integer arg0, Throwable arg1) {
						a = arg0;
					}
				});
			}
		}, 0, 2);
	}

	public static HubPlayer get(BadblockPlayer player) {
		return player.inGameData(HubPlayer.class);
	}

	public static HubPlayer get(Player player) {
		return get((BadblockPlayer) player);
	}

	// AntiUseSpam
	private long antiSpamClicked;

	private long bigAntiSpamClicked;
	public MountItem clickedMountItem;
	// Inventories
	public CustomInventory currentInventory;
	// Disguise
	public MetamorphosisItem disguise;

	public DyeColor dyeColor;
	private List<String> friends;

	public CustomCreature lastCreature;

	public long lastMount;

	public long lastVipCuboid;
	public long lastSendVipMessage;
	public long lastGetAwaySendMessage;

	public MountItem mounted;

	private BadblockScoreboardGenerator scoreboard;

	// Mount
	public LivingEntity mountEntity;

	public Map<String, Team> teams = new HashMap<>();

	public long teleportMount;

	// Particles
	public List<Effect> particles = new ArrayList<>();
	public ParticleItem clickedParticleItem;

	// Basic informations
	private UUID uuid;

	// data en vrac
	private ChestOpener chestOpener;
	private boolean		chestFreeze;
	private Location	chestFreezeLocation;

	private boolean velocity = false;

	private long timeBetweenEachVelocityUsage;

	public FakeEntity<?> fakeEntity;

	public OwnableItem buyItem;

	public long lastChat;

	public String message;

	public long lastMove = System.currentTimeMillis() + 300_000L;

	public PlayerBooster lastBooster;

	public HubPlayer() {
		this.setFriends(new ArrayList<>());
	}

	public boolean hasSpam(BadblockPlayer player) {
		long time = System.currentTimeMillis();
		if (this.getBigAntiSpamClicked() >= time)
			return true;
		if (this.getAntiSpamClicked() >= time) {
			player.sendTranslatedMessage("hub.spam.waitbetweeneachinteraction");
			this.setBigAntiSpamClicked(time + 200);
			return true;
		}
		use();
		return false;
	}

	public void updateScoreboard() {
		this.getScoreboard().generate();
	}

	public static String getRealName(BadblockPlayer player)
	{
		GameBadblockPlayer gbp = (GameBadblockPlayer) player;
		if (gbp.getRealName() != null) return gbp.getRealName();
		return gbp.getName();
	}

	public void lodad(BadblockPlayer player) {
		PlayerData playerData = player.getPlayerData();
		if (!playerData.isTempAccess())
		{
			if (!player.getMainGroup().equals("default"))
			{
				playerData.setTempAccess(true);
				player.saveGameData();
			}
		}

		// Reconnection
		if (ReconnectionInvitationsListener.getReconnections().containsKey(player.getName().toLowerCase()))
		{
			String serverName = ReconnectionInvitationsListener.getReconnections().get(player.getName());

			TextComponent message = new TextComponent("");
			StringBuilder stringBuilder = new StringBuilder();
			Iterator<String> iterator = Arrays
					.asList(new TranslatableString("hub.reconnectserver.info", player.getName(), serverName).get(player))
					.iterator();
			while (iterator.hasNext()) {
				String msg = iterator.next();
				stringBuilder.append(msg + (iterator.hasNext() ? System.lineSeparator() : ""));
			}
			message.setText(stringBuilder.toString());
			message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reconnect"));
			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new ComponentBuilder(new TranslatableString("hub.reconnectserver.info_hover",
							player.getGroupPrefix().getAsLine(player) + player.getName()).getAsLine(player))
					.create()));
			player.spigot().sendMessage(message);
		}

		BadBlockHub hub = BadBlockHub.getInstance();
		final TempScheduler tempScheduler0 = new TempScheduler();
		tempScheduler0.task = TaskManager.scheduleSyncRepeatingTask("hub_" + player.getName() + "_" + player.getEntityId(), new Runnable() {
			@Override
			public void run() {
				new Thread()
				{
					@Override
					public void run()
					{
						if (player == null || !player.isOnline()) {
							TaskManager.taskList.remove("hub_" + player.getName() + "_" + player.getEntityId());
							tempScheduler0.task.cancel();
							return;
						}

						// Vérification du dernier coffre gratuit
						for (CustomChestType chestType : ChestLoader.getInstance().getChests()) {
							if (chestType.getGiveEachSeconds() <= -1) continue;
							HubStoredPlayer hubStoredPlayer = HubStoredPlayer.get(player);
							boolean has = false;
							for (CustomChest customChest : hubStoredPlayer.getChests())
								if (customChest.getTypeId() == chestType.getId() && !customChest.isOpened()) 
									has = true;
							if (has) continue;
							if (!hubStoredPlayer.getLastGivenChests().containsKey(chestType.getId()) || (hubStoredPlayer.getLastGivenChests().containsKey(chestType.getId()) && hubStoredPlayer.getLastGivenChests().get(chestType.getId()) + (chestType.getGiveEachSeconds() * 1000L) < System.currentTimeMillis())) {
								hubStoredPlayer.getLastGivenChests().put(chestType.getId(), System.currentTimeMillis());
								hubStoredPlayer.getChests().add(new CustomChest(chestType.getId(), false));
								player.sendTranslatedMessage("hub.chests.youhavereceivednewchest", player.getTranslatedMessage("hub.chests." + chestType.getId() + ".name")[0]);
								player.saveGameData();
							}
						}
						GameAPI.getAPI().getSqlDatabase().call("SELECT id, xp, badcoins FROM debts WHERE playerName = '" + HubPlayer.getRealName(player) + "'", SQLRequestType.QUERY, new Callback<ResultSet>() {

							@Override
							public void done(ResultSet result, Throwable error) {
								try {
									boolean a = false;
									while (result.next()) {
										GameAPI.getAPI().getSqlDatabase().createStatement().executeUpdate("DELETE FROM debts WHERE id = '" + result.getLong("id") + "'");	
										if (result.getLong("badcoins") > 0) {
											player.getPlayerData().addBadcoins(result.getInt("badcoins"), false);
										}
										if (result.getLong("xp") > 0) {
											player.getPlayerData().addXp(result.getInt("xp"), false);
											a = true;
										}
									}
									if (a) {
										player.saveGameData();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

						});
					}
				}.start();
			}
		}, 1, 5 * 300);
		HubStoredPlayer hubStoredPlayer = HubStoredPlayer.get(player);
		if (hubStoredPlayer.getMountConfigs() == null)
			hubStoredPlayer.setMountConfigs(new TreeMap<>());
		if (hubStoredPlayer.getParticleConfigs() == null)
			hubStoredPlayer.setParticleConfigs(new TreeMap<>());
		TempScheduler tempScheduler2 = new TempScheduler();
		tempScheduler2.task = TaskManager.scheduleSyncRepeatingTask("hub_show_" + player.getName() + "_" + player.getEntityId(), new Runnable() {
			/*int length = 0;
			long time = 0;
			int id = 0;
			boolean iv;*/
			@Override
			public void run() {
				if (player == null || !player.isOnline()) {
					TaskManager.taskList.remove("hub_show_" + player.getName() + "_" + player.getEntityId());
					tempScheduler2.task.cancel();
					return;
				}
				// Floating texts
				for (Entry<Location, TranslatableString> entry : LinkedInventoryEntity.getFloatingTexts().entrySet())
				{
					player.showFloatingText(entry.getValue().getAsLine(player), entry.getKey(), 20 * 15, 0);
				}

				BadblockPlayer bbPlayer = player;
				if (scoreboard != null)
				{
					updateScoreboard();
				}
				/*List<String> list = Arrays.asList(GameAPI.i18n().get(player.getPlayerData().getLocale(), "hub.actionbar"));
				 * 
				if (id > list.size() - 1) id = 0
				String actionBar = list.get(id).substring(2, list.get(id).length() - 1);
				if (length <= 0) {
					id++;
					length = 0;
					iv = false;
				}else if (length == actionBar.length() / 2) {
					iv = true;
					time = System.currentTimeMillis() + 1500;
				}
				System.out.println(length + "/" + (actionBar.length() / 2));
				if (time < System.currentTimeMillis() && iv) {
					length--;
					iv = true;
				}else length++;
				String character = list.get(id).substring(0, 2) + actionBar.substring(actionBar.length() / 2 - length, actionBar.length() / 2 + (length / 2));
				bbPlayer.sendActionBar(character);*/
				String finalString = "";
				Iterator<String> iterator = Arrays
						.asList(GameAPI.i18n().get(bbPlayer.getPlayerData().getLocale(), "hub.tablist.header"))
						.iterator();
				while (iterator.hasNext()) {
					finalString += iterator.next() + (iterator.hasNext() ? System.lineSeparator() : "");
				}
				String finalString2 = "";
				iterator = Arrays.asList(GameAPI.i18n().get(bbPlayer.getPlayerData().getLocale(), "hub.tablist.footer"))
						.iterator();
				while (iterator.hasNext()) {
					finalString2 += iterator.next() + (iterator.hasNext() ? System.lineSeparator() : "");
				}
				bbPlayer.sendTabHeader(finalString.replace("%0", Integer.toString(a)).replace("%1", player.getName()),
						finalString2.replace("%0", Integer.toString(a)).replace("%1", player.getName()));
				//String finalString3 = "";
				/*iterator = Arrays.asList(player.getTranslatedMessage("hub.bossbar")).iterator();
				while (iterator.hasNext()) {
					finalString3 += iterator.next() + (iterator.hasNext() ? System.lineSeparator() : "");
				}
				bbPlayer.sendBossBar(finalString3.replace("%0", Integer.toString(a)).replace("%1", player.getName()));*/
			}
		}, 0, 20 * 10);
		TaskManager.runAsyncTaskLater(new Runnable() {
			@Override
			public void run() {
				while (!player.isDataFetch()) {
					if (!player.isOnline()) return;
				}
				GameAPI.getAPI().getLadderDatabase().getPlayerData(player, new Callback<JsonObject>() {

					@Override
					public void done(JsonObject result, Throwable error) {
						if (result.has("leaves")) {
							JsonArray leaves = result.get("leaves").getAsJsonArray();
							List<Long> leavess = GameAPI.getGson().fromJson(leaves, GameBadblockPlayer.collectType);
							player.setLeaves(leavess);
						}
					}

				});
			}
		}, 5);
		TempScheduler tempScheduler3 = new TempScheduler();
		tempScheduler3.task = TaskManager.scheduleSyncRepeatingTask(player.getName() + "_funmode", new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				if (player == null || !player.isOnline()) {
					TaskManager.taskList.remove(player.getName() + "_funmode");
					tempScheduler3.task.cancel();
					return;
				}
				if (mountEntity != null && mountEntity.isValid()) {
					HubPlayer hubPlayer = HubPlayer.get(player);
					MountItem mountItem = hubPlayer.getClickedMountItem();
					if (mountItem == null)
						return;
					HubStoredPlayer hubStoredPlayer = HubStoredPlayer.get(player);
					if (hubPlayer.getFakeEntity() != null) {
						if (hubPlayer.getFakeEntity().getLocation().distanceSquared(player.getLocation()) >= 15) {
							hubPlayer.getFakeEntity().teleport(player.getLocation());
							hubPlayer.getFakeEntity().move(player.getLocation().clone().add(0, 1, 0));
						}
					}
					if (hubPlayer.getLastCreature() != null) {
						if (hubStoredPlayer.getMountConfigs().containsKey(mountItem.getName())) {
							if (!hubStoredPlayer.getMountConfigs().get(mountItem.getName()).isBaby()) {
								if (player.getVehicle() == null
										|| (player.getVehicle() != null && !player.getVehicle().isValid())) {
									mountEntity.remove();
								}
								if (hubStoredPlayer.getMountConfigs().get(mountItem.getName()).isPegasus()
										&& mountItem.hasPegasusMode()) {
									if (!mountEntity.isOnGround())
										Wings.SpawnWings2(mountEntity, hubPlayer, player);
									hubPlayer.getLastCreature().setCreatureBehaviour(CreatureBehaviour.FLYING);
								} else
									hubPlayer.getLastCreature().setCreatureBehaviour(CreatureBehaviour.NORMAL);
								if (hubStoredPlayer.getMountConfigs().get(mountItem.getName()).isReverse()) {
									mountEntity.setCustomName("Dinnerbone");
									mountEntity.setCustomNameVisible(false);
								} else {
									mountEntity.setCustomName(player.getName());
									mountEntity.setCustomNameVisible(false);
								}
							} else {
								hubPlayer.getLastCreature().setCreatureBehaviour(CreatureBehaviour.NORMAL);
							}
							if (mountEntity.isOnGround()) {
								if (mountItem.hasFunMode()) {
									if (hubStoredPlayer.getMountConfigs().containsKey(mountItem.getName())) {
										if (hubStoredPlayer.getMountConfigs().get(mountItem.getName()).isFunMode()) {
											Location location = mountEntity.getLocation().clone();
											SecureRandom secureRandom = new SecureRandom();
											for (int x = -2; x < 2; x++) {
												for (int z = -2; z < 2; z++) {
													Block block = location.clone().add(x, -1, z).getBlock();
													if (block.getType().equals(Material.AIR))
														continue;
													if (block.getType().name().contains("SLAB")
															|| block.getType().name().contains("STAIR")
															|| block.getType().name().contains("STEP"))
														continue;
													for (Player p : Bukkit.getOnlinePlayers())
														p.sendBlockChange(location.clone().add(x, -1, z),
																Material.STAINED_CLAY, (byte) secureRandom.nextInt(16));
													final int finalX = x;
													final int finalZ = z;
													TaskManager.runTaskLater("removefunmode_" + UUID.randomUUID().toString(),
															new Runnable() {
														@Override
														public void run() {
															for (Player p : Bukkit.getOnlinePlayers())
																p.sendBlockChange(
																		location.clone().add(finalX, -1,
																				finalZ),
																		block.getType(), block.getData());
														}
													}, 40);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}, 1, 20);

		TempScheduler tempScheduler4 = new TempScheduler();
		tempScheduler4.task = TaskManager.scheduleSyncRepeatingTask(player.getName() + "_connect", new Runnable() {
			@Override
			public void run() {
				if (player == null || !player.isOnline()) {
					TaskManager.taskList.remove(player.getName() + "_connect");
					tempScheduler4.task.cancel();
					return;
				}

				HubStoredPlayer hsp = HubStoredPlayer.get(player);

				if (hsp.connectInventory)
				{
					TaskManager.taskList.remove(player.getName() + "_connect");
					tempScheduler4.task.cancel();
					return;
				}

				if (getCurrentInventory() != null && !getCurrentInventory().getName().equalsIgnoreCase("hub.items.connectinventory.name"))
				{
					return;
				}

				CustomInventory.get(ConnectInventory.class).open(player);
			}
		}, 1, 20 * 1);

		Bukkit.getScheduler().runTaskLater(BadBlockHub.getInstance(), new Runnable()
		{
			@Override
			public void run()
			{
				if (!player.isOnline())
				{
					return;
				}
				
				if (!player.hasPermission("hub.fly"))
				{
					player.playSound(player.getLocation(), Sound.HORSE_DEATH);
					player.sendMessage(" ");
					player.sendMessage(" §cVous ne pouvez pas Fly au hub.");
					player.sendMessage(" §cVous devez acquérir un grade §6§lLegend §cpour fly au Hub.");
					player.sendMessage(" ");
					return;
				}

				DoubleJumpListener.fly.add(player.getName().toLowerCase());
				
				player.playSound(player.getLocation(), Sound.HORSE_GALLOP);
				player.setAllowFlight(true);
				player.sendMessage(" ");
				player.sendMessage(" §aVotre mode SuperMaaan! a été activé.");
				player.sendMessage(" §eL'élite, c'est les gens qui ont le grade §6§lLegend§e.");
				player.sendMessage(" ");
				player.sendMessage(" §dMentionnez @BadBlockGame sur Twitter pour demander un follow");
				player.sendMessage(" §fID de votre demande = BDC-" + player.getName());
				player.sendMessage(" ");
			}
		}, 20);
	}

	public void use() {
		long time = System.currentTimeMillis();
		this.setAntiSpamClicked(time + 500);
		this.setBigAntiSpamClicked(time + 200);
	}

	public void justGetVelocity() {
		velocity = true;
	}

	public boolean canChat(boolean hasPermission) {
		if (hasPermission) return hasPermission;
		boolean can = lastChat < System.currentTimeMillis();
		if (can) lastChat = System.currentTimeMillis() + ConfigUtils.getInt(BadBlockHub.getInstance(), "timebetweenmessage");
		return can;
	}

}
