package me.despawningbone.module;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.registry.WorldData;

import org.inventivetalent.particle.ParticleEffect;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.events.IslandChangeOwnerEvent;
import com.wasteofplastic.askyblock.events.IslandEnterEvent;
import com.wasteofplastic.askyblock.events.IslandExitEvent;
import com.wasteofplastic.askyblock.events.IslandNewEvent;
import com.wasteofplastic.askyblock.events.IslandPreDeleteEvent;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.EventPriority;

import me.despawningbone.module.OreGenerator.UPGRADE;
import me.despawningbone.modules.Platform;
import me.despawningbone.modules.api.GUI;
import me.despawningbone.modules.api.Module;
import me.despawningbone.modules.api.ReflectionUtils;
import me.despawningbone.modules.api.Variables;
import me.despawningbone.modules.api.GUI.Button;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_12_R1.BlockPosition;

public class Totems extends Module {
	
	private SplittableRandom ran = new SplittableRandom();  //avoid object instantiation every time
	private double xOffset = 0.5;
	private double yOffset = 0.5;
	private double zOffset = 0.5;
	
	private Economy econ;
	
	private static final String prefix = "§8[§3§l!§8] §7";
	
	private final DecimalFormat df = new DecimalFormat("0.0");
	
	//legacy HashMap<UUID, Set<UUID>> playersOnIsland = new HashMap<UUID, Set<UUID>>();
	HashMap<UUID, Long> oreCountMsg = new HashMap<UUID, Long>();
	HashMap<UUID, OreGenerator> inEditorMode = new HashMap<UUID, OreGenerator>();
		
	ASkyBlockAPI ASkyAPI = ASkyBlockAPI.getInstance();
	
	/*
	 * TODO Long checks for ore block physical block material desync
	 */
	
	@Override
	public void cleanup() {
		for(UUID uuid : ASkyAPI.getOwnedIslands().keySet()) {
			Object list = Variables.get("Island", uuid.toString(), "Totems");
			if(list instanceof List<?>) {
				Variables.set( "Island", uuid.toString(), "Totems", serialize((List<OreGenerator>) list) );
			}
		}
		super.cleanup();
	}
	
	public Totems(Platform platform) {
		super(platform, "sbcore", "sbsessionvar");
		
		for(UUID uuid : ASkyAPI.getOwnedIslands().keySet()) {
			Object list = Variables.get("Island", uuid.toString(), "Totems");
			if(list instanceof String) {
				byte[] data = Base64.getDecoder().decode((String) list);
				try {
					Variables.set("Island", uuid.toString(), "Totems", deserialize(data));
				} catch (ClassNotFoundException e) {
					System.out.println("Could not load Island Owner: " + Bukkit.getPlayer(uuid).getName() + "'s Totems! SKIPPING...");
					e.printStackTrace();
				}
			}
		}
	
		for (int i = 0; i < 1; i ++) {
			delayRun(() -> {
				List<OreGenerator> gens = new ArrayList<OreGenerator>();
				for(UUID uuid : ASkyAPI.getOwnedIslands().keySet()) {
					Object list = Variables.get("Island", uuid.toString(), "Totems");
					if(list instanceof List<?>) {
						gens.addAll((List<OreGenerator>) Variables.get("Island", uuid.toString(), "Totems"));
					}
				}
				final List<OreGenerator> allGens = gens;
				delayRun(() -> {
					boolean loaded = false;
					while(loaded != true) {
						if(Bukkit.getWorld("Skyblock") != null) {
							delayRun(() -> {
								for (OreGenerator gen : allGens) {
									updateHologram(gen.getHologramLocation(), gen.getHologramLines());
									gen.setEditorMode(false);
								}
							},0,false);
							loaded = true;
						}
					}
				},1,true);	
			},1,false);
		}
		
		
		
		new OreBlock(this);
		
		econ = platform.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
		
		startRepeatingTask(() -> {
			for(Entry<UUID, Set<UUID>> entry : sessionVar().playersOnIsland.entrySet()) {
				List<OreGenerator> gens = getGeneratorsOnly(entry.getKey());
				if(gens != null) {
					for(OreGenerator gen : gens) {
						delayRun(() -> gen.generateOres(false),0, false);
					}	
				}
			}
		},10,10,true);
		
		startRepeatingTask(() -> {
			for(OreGenerator gen : inEditorMode.values()) {
				for (OreBlock ore : gen.getOreBlocks()) {
					Location oreLoc = ore.getLocation();
					oreLoc.getWorld().spawnParticle(Particle.BLOCK_CRACK, oreLoc.clone().add(0.5,0.75,0.5), 110, new MaterialData(ore.getCurrentMaterial()));
				}
			}
		},50,50,false);
		
		startRepeatingTask(() -> {
			oreCountMsg.clear();
		},36000,36000,false);
		
		registerCommand("totem", new BukkitCommand("totem", "Totem system for Skyblock", "[subcmd]", Arrays.asList("totem","totems")) {
			@Override
			public boolean execute(CommandSender sender, String label, String[] args) {
				Boolean canUseCMD = false;
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(player.hasPermission("totems.admin")) canUseCMD = true;
				}else if (sender instanceof ConsoleCommandSender) {
					canUseCMD = true;
				}
				if(canUseCMD) {
					if(args.length == 0) {
						sender.sendMessage("Totems");
						sender.sendMessage("");
						sender.sendMessage("/totems give <player> <regenblocks> <Lootlvl> <Stacklvl> <Speedlvl> <amt>");
						sender.sendMessage("");
						return true;
					}
					if(args.length == 1) {
						if (args[0].equalsIgnoreCase("reset")) {
						Player player = (Player) sender;
						UUID ownerUUID = player.getUniqueId();
						String owner = ownerUUID.toString();
						Variables.set("Island", owner, "Totems", new ArrayList<List<OreGenerator>>());
					
						}
						else if (args[0].equalsIgnoreCase("pasteschem")) {
							pasteSchematic("TotemStructure", ((Player) sender).getLocation(), false);
						}
					}
					if(args.length == 7) {
						if (args[0].equalsIgnoreCase("give")) {
							int regenblocks,lootLvl,stackLvl,speedLvl,amt;
							OfflinePlayer player = Bukkit.getPlayer(args[1]);
							try {
								regenblocks = Integer.parseInt(args[2]);
								lootLvl= Integer.parseInt(args[3]);
								stackLvl = Integer.parseInt(args[4]);
								speedLvl = Integer.parseInt(args[5]);
								amt = Integer.parseInt(args[6]);
							}catch (NumberFormatException e) {
								sender.sendMessage("Invalid args!");
								return true;
							}
							if(regenblocks > 2 && regenblocks <= 24) {
								ItemStack totem = getTotemItem(regenblocks, lootLvl, stackLvl, speedLvl);
								if(Bukkit.getPlayer(player.getName()) != null) {
									Player online = Bukkit.getPlayer(player.getName());
									core().giveItem(online, totem, true);
								}else {
									core().giveItem(player, totem);
								}
							}else {
								sender.sendMessage("Invalid tier! amount of regen blocks! can only be 3-12");
							}
							
						}
					}
				}
				
				return true;
			}
		});
		
	}
	
	public void delayRunEmulator(Runnable run, long delay, boolean async, boolean runOnReload) {
		delayRun(run, delay, async, runOnReload);
	}
	
	private SBCore core() {
    	return ((SBCore) platform.getModule("sbcore"));
    }
	
	private SBSessionVar sessionVar() {
    	return ((SBSessionVar) platform.getModule("sbsessionvar"));
    }
	
	public ItemStack getTotemItem(int regenblocks, int lootLvl, int stackLvl, int speedLvl) {
		ItemStack item = new ItemStack(Material.SEA_LANTERN, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§b§lORE §3§lTOTEM §7(Place Down)");
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(" §7This totem will populate selected blocks");
		lore.add(" §7with ores tiered to the totem's upgrades.");
		lore.add("");
		lore.add("§8§l[ §3§lStatistics");
		lore.add(" §b§l* §3§lRegen Blocks§7: §f§l"+regenblocks+"x");
		lore.add(" §b§l* §3§lUpgrades§7:");
		lore.add("§7 §7 - §bOre Stack §7(Lvl " + stackLvl + ")");
		lore.add("§7 §7 - §bLoot Level §7(Lvl " + lootLvl + ")");
		lore.add("§7 §7 - §bSpeed §7(Lvl " + speedLvl + ")");
		lore.add("");
		lore.add("§7 (( §7§oPlace §b§oTotem §7§odown to begin. §7))");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		item.addUnsafeEnchantment(Enchantment.DURABILITY, -1);
		item = ReflectionUtils.setNBTTag(item, "setString", "Statistics", OreGenerator.statsToString(regenblocks, lootLvl, stackLvl, speedLvl));
		
		return item;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTotemPlace(BlockPlaceEvent event) {
		if(event.getItemInHand().getType() != Material.SEA_LANTERN) return;
		if(!event.getItemInHand().hasItemMeta() || !event.getItemInHand().getItemMeta().hasLore()) return;
		if(event.getItemInHand().getItemMeta().getDisplayName().equals("§b§lORE §3§lTOTEM §7(Place Down)")) {
		if(!event.getBlock().getWorld().getName().equalsIgnoreCase("Skyblock")) {
			event.getPlayer().sendMessage(prefix + "Totems can only be placed in the Overworld!");
			event.setCancelled(true);
			return;
		}
		if(!areaClearForTotem(event.getBlock())) {
			event.getPlayer().sendMessage(prefix + "There is insufficient space to place down this Totem.");
			event.setCancelled(true);
			return;
		}
		if(isATotemBlock(event.getBlock(), getGeneratorsOnly(ASkyAPI.getOwner(event.getBlock().getLocation())))) {
			event.setCancelled(true);
			return;
		}
			Map.Entry<UUID, List<OreGenerator>> entry = getGenerators(event.getPlayer(), event.getBlockPlaced().getLocation());
			if(entry == null) {
				event.setCancelled(true);
				return;
			}
			
			
			
			String stats = ReflectionUtils.getNBTTag(event.getItemInHand(), "getString", "Statistics");
			OreGenerator totem = new OreGenerator(entry.getKey(), event.getBlockPlaced());
			pasteSchematic("TotemStructure", event.getBlockPlaced().getLocation(), false);
			totem.statsFromString(stats);
			UUID owner = entry.getKey();
			List<OreGenerator> totems = entry.getValue();
			if(totems == null) {
				totems = new ArrayList<OreGenerator>();
			}
			totems.add(totem);
			totem.setLootLevel(totem.getLootLevel());
			createHologram(totem.getHologramLocation(), totem.getHologramLines());
			saveToVar(owner, totems);
			
			/*ItemStack stones = new ItemStack(Material.STONE, 24);
			ItemMeta meta = stones.getItemMeta();
			meta.setDisplayName("§b§lOre §3§lTotem §8[§7Tutorial Blocks§8]");
			List<String> lore = new ArrayList<String>(Arrays.asList(
					"§7",
					"§8§l[ §f§lHOW TO SETUP",
					"§f1. §7Place down 3x Stone blocks on the ground.",
					"§f2. §7Right-click totem, turn §a§lon Editor's Mode",
					"§f3. §7Exit out of the totem GUI",
					"§f4. §7Individually select the 3 stone blocks",
					"§7 §7 §7 §7 you've placed down.",
					"§7 §7 §7 §7 Make sure! You see this message§8:",
					"§7 §7 §7 §7 §8\"§7Assigned this block... Ore Gen §8[§33/3§8]\"",
					"§5. §7Open Totem GUI, turn §c§loff Editor's Mode",
					"§7",
					"§7Need help? ask around!",
					"§7More info of our features in /help!"
					
					));
			meta.setLore(lore);
			stones.setItemMeta(meta);
			core().giveItem(event.getPlayer(), stones, false);*/
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		UUID owner = ASkyAPI.getOwner(event.getBlock().getLocation());
		if(owner != null) {
			List<OreGenerator> totems = getGeneratorsOnly(owner);
			if(totems != null) {
				for (OreGenerator totem : totems) {
					if(totem.isInLocation(event.getBlock().getLocation())) {
						event.setCancelled(true);
						return;
					}
				}		
			}
		}
	}
	
	@EventHandler
	public void onInteractSponge(PlayerInteractEvent event) {
		if(event.getClickedBlock().getType() == Material.SPONGE) {
			if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
				Player player = event.getPlayer();
				ItemStack item = player.getItemInHand();
				if(item.hasItemMeta() && item.getItemMeta().hasLore()) {
					if(getLevel(item.getItemMeta().getLore(), "Insta Sponge") > 0) {
						Block block = event.getClickedBlock();
						if(OreGenerator.getPossibleGenBlock().contains(block.getType()) || OreBlock.getPossibleOreTypes().contains(block.getType())) {
							Map.Entry<UUID, List<OreGenerator>> entry = getGenerators(player, block.getLocation());
							if(entry == null) return;
							
							if(inEditorMode.containsKey(event.getPlayer().getUniqueId())) {
								player.sendMessage(prefix + "Turned §c§lOFF §7Editor mode for this generator.");
								inEditorMode.remove(event.getPlayer().getUniqueId());
								event.setCancelled(true);
								return;
							}
							
							if(isATotemBlock(event.getClickedBlock(),entry.getValue())) {
								Location loc = event.getClickedBlock().getLocation();
								((CraftPlayer) player).getHandle().playerInteractManager.breakBlock(new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
								
							}
						}
						

					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if(!event.getBlock().getWorld().getName().equalsIgnoreCase("Skyblock")) {
			return;
		}
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if(OreGenerator.getPossibleGenBlock().contains(block.getType()) || OreBlock.getPossibleOreTypes().contains(block.getType())) {
			Map.Entry<UUID, List<OreGenerator>> entry = getGenerators(player, block.getLocation());
			if(entry == null) return;
			
			if(inEditorMode.containsKey(event.getPlayer().getUniqueId())) {
				player.sendMessage(prefix + "Turned §c§lOFF §7Editor mode for this generator.");
				inEditorMode.remove(event.getPlayer().getUniqueId());
				event.setCancelled(true);
				return;
			}
			
			if(isATotemBlock(event.getBlock(),entry.getValue())) {
				if(OreGenerator.getPossibleGenBlock().contains(block.getType())) {
					event.setCancelled(true);
					return;
				}else {
					for(OreGenerator gen : entry.getValue()) {
						for(OreBlock ore : gen.getOreBlocks()) {
							if(ore.getLocation().equals(block.getLocation())) {
								if(ore.getOreStack() == 0) {
									event.setCancelled(true);
									return;
								}
								int mobcoinLevel = 0;
								ItemStack tool = player.getItemInHand();
								if(tool.getType() == Material.DIAMOND_PICKAXE) {
									if(tool.hasItemMeta() && tool.getItemMeta().hasLore()) {
										mobcoinLevel = Totems.getLevel(tool.getItemMeta().getLore(), "Coins");
										if(ran.nextDouble() <= mobcoinLevel*0.00008 + (0.00000087 * gen.getLootLevel())) {
											addMobCoin(player, 1);
											player.sendMessage("§a§l+1 Mob Coin");
										}
									}
								}
								
								HashMap<ItemStack, Integer> leftOver = gen.processOreBreak(player, ore, event);
								if(leftOver != null) {
									
									for(Entry<ItemStack,Integer> items : leftOver.entrySet()) {
										int totalDrop = items.getValue();
										ItemStack item = items.getKey().clone();
										for (int i = 0; i < Math.ceil(totalDrop/64.0); i++) {
											if(totalDrop <= 64) {
												item.setAmount(totalDrop);
												core().giveItem(player, item);
											}
											else {
												totalDrop -= 64;
												item.setAmount(64);
												core().giveItem(player, item);
											}
										}
									}
								}
								break;
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(!event.getPlayer().getWorld().getName().equalsIgnoreCase("Skyblock")) {
			return;
		}
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			Player player = event.getPlayer();
			
			//Seeing how many blocks are left
			if(isTotemMaterial(block.getType())) {
				Map.Entry<UUID, List<OreGenerator>> entry = getGenerators(player, block.getLocation());
				if(entry == null || entry.getValue() == null) return;
				
				for (OreGenerator gen : entry.getValue()) {
					if(gen.isInLocation(block.getLocation())) {
						if(inEditorMode.containsKey(event.getPlayer().getUniqueId())) {
							if(!inEditorMode.get(event.getPlayer().getUniqueId()).getLocation().equals(gen.getLocation())) {
								event.getPlayer().sendMessage(prefix + "Please turn off Editor mode to access this totem");
								return;
							}
						}
						openTotemGUI(player, gen);
						return;
					}
					for (OreBlock oreBlock : gen.getOreBlocks()) {
						if(oreBlock.getLocation().equals(block.getLocation())) {
							sendOreCountMessage(player, gen, oreBlock);
							return;
						}
					}		
					
				}
			}
			
		}
		
		else if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			Player player = event.getPlayer();
			if(inEditorMode.containsKey(event.getPlayer().getUniqueId())) {
				Map.Entry<UUID, List<OreGenerator>> entry = getGenerators(player, block.getLocation());
				if(entry == null) return;
				
				OreGenerator cachedGen = inEditorMode.get(event.getPlayer().getUniqueId());
				OreGenerator gen = null;
				for(OreGenerator foundGen : entry.getValue()) {
					if(foundGen.getLocation().equals(cachedGen.getLocation())) {
						gen = foundGen;
						break;
					}
				}
				if(gen == null) return;
				
				/*if(gen.isInLocation(block.getLocation()) && OreGenerator.getPossibleGenBlock().contains(block.getType())) {
					gen.setEditorMode(false);
					inEditorMode.remove(player.getUniqueId());
					player.sendMessage(prefix + "Turned §c§lOFF §7Editor mode for this generator.");
					return;
				}*/
				
				if(!isATotemBlock(block, entry.getValue())) {
					if(block.getType() == Material.STONE) {
						if(ASkyAPI.getOwner(block.getLocation()).equals(entry.getKey())) {
							if(gen.assignOreGenBlock(block)) {
								block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5,0.75,0.5), 160, new MaterialData(block.getType(), block.getData()));
								player.sendMessage(prefix + "Assigned this block as an Ore Generator §8[§7"+gen.getOreBlocks().size()+"§8/§7"+gen.getMaxOreBlocks()+"§8]");
								inEditorMode.put(event.getPlayer().getUniqueId(), gen);
								saveToVar(entry.getKey(), entry.getValue());
							}	
						}
						
						
					}else {
						event.getPlayer().sendMessage(prefix + "Invalid type of block! You can only assign Stone blocks as Ore Generators");
					}
				}else {
					for (OreBlock oreBlock : gen.getOreBlocks()) {
						if(oreBlock.getLocation().equals(block.getLocation())) {
							gen.unAssignOreGenBlock(oreBlock);
							inEditorMode.put(event.getPlayer().getUniqueId(), gen);
							player.sendMessage(prefix + "Unassigned this block as an Ore Generator §8[§7"+gen.getOreBlocks().size()+"§8/§7"+gen.getMaxOreBlocks()+"§8]");
							break;
						}
					}
				}
			}/*else {
				if(event.getPlayer().getItemInHand().getType().equals(Material.DIAMOND_PICKAXE)) return;
				if(OreGenerator.getPossibleGenBlock().contains(block.getType())) {
					Map.Entry<UUID, List<OreGenerator>> entry = getGenerators(player, block.getLocation());
					if(entry == null) {
						return;
					}
					for (OreGenerator gen : entry.getValue()) {
						if(gen.isInLocation(block.getLocation())) {
							gen.setEditorMode(true);
							inEditorMode.put(player.getUniqueId(), gen);
							player.sendMessage(prefix + "Turned §a§lON §7Editor Mode for this generator.");
							break;
						}
					}
				}
			}*/
		}
	}
	
	public void sendOreCountMessage(Player player, OreGenerator gen, OreBlock ore) {
		if(oreCountMsg.containsKey(player.getUniqueId())) {
			if(oreCountMsg.get(player.getUniqueId()) < System.currentTimeMillis()) {
				oreCountMsg.remove(player.getUniqueId());
			}
		}
		if(!oreCountMsg.containsKey(player.getUniqueId())) {
			oreCountMsg.put(player.getUniqueId(), System.currentTimeMillis()+400);
			player.sendMessage("");
			player.sendMessage("§b§l Ore §3§lBlock");
			player.sendMessage("§7 There are currently §b§n"+ore.getOreStack()+"§7 Blocks Stored in here.");
			player.sendMessage("");
			player.sendMessage("§b§l * §7Next Generation in§f"+formatTime( (int) (gen.nextGenerationTime-System.currentTimeMillis())/1000 ));
			player.sendMessage("");
		}
	}
	
	public void openTotemGUI(Player player, OreGenerator gen) {
		GUI gui = new GUI(3, "Totem");
		
		gui.assignSlotButton(0, 27, new Button(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7)));
		
		gui.assignSlotButton(26, 1, new Button(new ItemStack(Material.BARRIER, 1), (s,p,c) ->{
			List<OreGenerator> fromVar = getGeneratorsOnly(gen.getIslandOwner());
			OreGenerator toRemove = null;
			for(OreGenerator updatedGen : fromVar) {
				if(updatedGen.getLocation().equals(gen.getLocation())) {
					
					player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.0f, 2.0f);
					player.closeInventory();
					destroyPhysicalBlocks(updatedGen);
					toRemove = updatedGen;
					
					break;
				}
			}
			if(toRemove != null) {
				core().giveItem(player, getTotemItem(toRemove.getMaxOreBlocks(), toRemove.getLootLevel(), toRemove.getOreStackLevel(), toRemove.getSpeedLevel()), true);
				fromVar.remove(toRemove);
				saveToVar(gen.getIslandOwner(), fromVar);
			}
		})
				.setName("&c&lRemove Totem")
				.setLore("","&4&l * &7All upgrades purchased on this", "&c will be &7be carried into Ore Totem Item!", "",
						"&7 (( &7&oLeft-Click to &7&o&nremove&e&o Ore Totem &7))"));
		
		gui.assignSlotButton(18, 1, new Button(new ItemStack(Material.STAINED_CLAY, 1, (short) (gen.getEditorMode() == true ? 5:14)), (s,p,c) -> {
			List<OreGenerator> fromVar = getGeneratorsOnly(gen.getIslandOwner());
			for(OreGenerator updatedGen : fromVar) {
				if(updatedGen.getLocation().equals(gen.getLocation())) {
					if(updatedGen.getEditorMode() == true) {
						boolean found = false;
						for (Entry<UUID, OreGenerator> entry : inEditorMode.entrySet()) {
							if(entry.getValue().getLocation().equals(updatedGen.getLocation())) {
								found = true;
								break;
							}
						}
						if(found == false) {
							updatedGen.setEditorMode(false);
						}
					}
					if(updatedGen.getEditorMode() == false) {
						updatedGen.setEditorMode(true);
						inEditorMode.put(player.getUniqueId(), updatedGen);
						saveToVar(gen.getIslandOwner(), fromVar);
						openTotemGUI(player, updatedGen);
						player.sendMessage(prefix + "Turned §a§lON §7Editor Mode for this generator.");
					}
					else if (updatedGen.getEditorMode() == true) {
						if(inEditorMode.containsKey(player.getUniqueId())) {
							if(inEditorMode.get(player.getUniqueId()).getLocation().equals(updatedGen.getLocation())) {
								inEditorMode.remove(player.getUniqueId());
								updatedGen.setEditorMode(false);
								saveToVar(gen.getIslandOwner(), fromVar);
								openTotemGUI(player, updatedGen);
								player.sendMessage(prefix + "Turned §c§lOFF §7Editor mode for this generator.");
							}
							else {
								player.sendMessage(prefix + "Someone else is editting this totem currently!");
							}
						}
						else {
							player.sendMessage(prefix + "Someone else is editting this totem currently!");
						}
					}

					break;
				}
			}
		})
				.setName("&7&l* &b&lEditor &8["+(gen.getEditorMode() == true ? "&a&lENABLED":"&c&lDISABLED")+"&8]")
				.setLore("","&8&l[ &3&lASSIGN ORE BLOCKS", "&7 When in &bEditor's Mode&7, Left-Click on",
						"&7 Stone blocks to &b&nAssign&7 them as &bOre Gens&7.", "", "&b These &7will be the blocks that Ores",
						"&7 will &bgenerate on&7!","","&7 (( &7&oLeft-Click to "+(gen.getEditorMode() == true ? "&c&o&ndisable":"&a&o&nenable")+"&7&o Editor's Mode &7))"));
		
		if(gen.getEditorMode() == false) {
			gui.assignSlotButton(15, 1, new Button(new ItemStack(Material.FEATHER, 1),(s,p,c) -> {
				openTotemUpgradesGUI(player, gen, UPGRADE.SPEED);
			})
					.setName("&6&l * &e&lSPEED UPGRADE &7(Upgrade) &6&l*")
					.setLore("", "&8&l[ &6&lINFO","&e Speed Upgrades &7reduces the &6&ntime taken",
							"&7 for the next ore to &6generate&7!", "", " &8&l* &6&lCURRENT LEVEL&7: &f&l"+
							gen.getLevelOf(UPGRADE.SPEED), " &8&l* &e&lUPGRADE COST&7: &f$"+numberFormatShort(OreGenerator.getCostOf(UPGRADE.SPEED, gen.getLevelOf(UPGRADE.SPEED)+1)),
							"", "&7(( &7&oLeft-Click to access Upgrade menu &7))"));
			
			gui.assignSlotButton(13, 1, new Button(new ItemStack(Material.EMERALD_ORE, 1),(s,p,c) -> {
				openTotemUpgradesGUI(player, gen, UPGRADE.LOOT);
			})
					.setName("&2&l * &a&lLOOT UPGRADE &7(Upgrade) &2&l*")
					.setLore("", "&8&l[ &2&lINFO","&a Loot Upgrades &7increases&7 the &a&nchances of",
							" &7better &7ores to &agenerate!", "", " &8&l* &2&lCURRENT LEVEL&7: &f&l"+
							gen.getLevelOf(UPGRADE.LOOT), " &8&l* &a&lUPGRADE COST&7: &f$"+numberFormatShort(OreGenerator.getCostOf(UPGRADE.LOOT, gen.getLevelOf(UPGRADE.LOOT)+1)),
							"", "&7(( &7&oLeft-Click to access Upgrade menu &7))"));
			
			gui.assignSlotButton(11, 1, new Button(new ItemStack(Material.ENDER_PORTAL_FRAME, 1),(s,p,c) -> {
				openTotemUpgradesGUI(player, gen, UPGRADE.ORE_STACK);
			})
					.setName("&3&l * &b&lORE STACK UPGRADE &7(Upgrade) &3&l*")
					.setLore("", "&8&l[ &3&lINFO","&b Ore Stack Upgrades &7increases&7 the &b&nmax number",
							" &7of ores that can be &bstored&7 in an Ore Block!", "", " &8&l* &3&lCURRENT LEVEL&7: &f&l"+
							gen.getLevelOf(UPGRADE.ORE_STACK), " &8&l* &b&lUPGRADE COST&7: &f$"+numberFormatShort(OreGenerator.getCostOf(UPGRADE.ORE_STACK, gen.getLevelOf(UPGRADE.ORE_STACK)+1)),
							"", "&7(( &7&oLeft-Click to access Upgrade menu &7))"));
				
		}else if (inEditorMode.containsKey(player.getUniqueId()) && inEditorMode.get(player.getUniqueId()).getLocation().equals(gen.getLocation())){
			gui.assignSlotButton(13, 1, new Button(new ItemStack(Material.STONE), (s,p,c) -> {
				List<OreGenerator> fromVar = getGeneratorsOnly(gen.getIslandOwner());
				for(OreGenerator updatedGen : fromVar) {
					if(updatedGen.getLocation().equals(gen.getLocation())) {
						openBlocksManagerGUI(player, updatedGen);
						break;
					}
				}
			})
					.setName("&7&l * &f&lManage Ore Blocks &7&l* &7(Click)")
					.setLore("", "&7 Manage &fOre Blocks &7currently set", "&7 on your Island.",
							" ", " &8&l* &f&lOre Blocks Manageable&7: "+gen.getOreBlocks().size() + " &8/&f"+gen.getMaxOreBlocks() + "&7 Blocks",
							" &7(( &7&oLeft-click to manage &f&oOre Blocks &7))"));
		}
		
		gui.open(platform, player);
	}
	
	public void openBlocksManagerGUI(Player player, OreGenerator gen) {
		GUI gui = new GUI(3, "Totem Upgrade");
		gui.assignSlotButton(0, 27, new Button(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7)));
		
		int slot = 0;
		for (OreBlock block : gen.getOreBlocks()) {
			Location bLoc = block.getLocation();
			gui.assignSlotButton(slot, 1, new Button(new ItemStack(block.getCurrentMaterial(),1), (s,p,c) -> {
				List<OreGenerator> fromVar = getGeneratorsOnly(gen.getIslandOwner());
				for(OreGenerator updatedGen : fromVar) {
					if(updatedGen.getLocation().equals(gen.getLocation())) {
						OreBlock toRemove = null;
						for (OreBlock updatedBlock : updatedGen.getOreBlocks()) {
							if(updatedBlock.getLocation().equals(block.getLocation())) {
								toRemove = updatedBlock;
								break;
							}
						}
						if(toRemove != null) {
							updatedGen.unAssignOreGenBlock(toRemove);
							saveToVar(gen.getIslandOwner(), fromVar);
							openBlocksManagerGUI(player, updatedGen);
						}
						break;
					}
				}
			})
					.setName("&3&lOre &b&lBlock")
					.setLore("","&3&L * &b&lLOCATION&7: &fX:" + bLoc.getBlockX() + ", Y:" + bLoc.getBlockY() + ", Z:" + bLoc.getBlockZ(),
							"&3&l * &b&lSTACK SIZE&7: &f&l" + block.getOreStack(), "",
							"&7 (( &7&oLeft-click to &7&n&oremove&7&o Ore Block &7))"));
			
			slot++;
		}
		if(slot == 0) {
			gui.assignSlotButton(13, 1, new Button(new ItemStack(Material.BARRIER))
					.setName("&c&lNothing to see here!")
					.setLore("&7Start assigning Ore Generator blocks",
							"&7by Left-Clicking stone blocks in your",
							"&7own island!"));
		}
		gui.open(platform, player);
	}
	
	public void openTotemUpgradesGUI(Player player, OreGenerator gen, OreGenerator.UPGRADE upgrade) {
		GUI gui = new GUI(5, "Totem Upgrade");
		gui.assignSlotButton(0, 45, new Button(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7)).setName("§7 "));
		
		int page = 1;
		if(gen.getLevelOf(upgrade) >= 21) page = (int) Math.ceil((gen.getLevelOf(upgrade)+1)/21.0);
		int upgradeLevel = gen.getLevelOf(upgrade);
		int upgradeIndex = (page-1) * 21;
		

		
		String name = "";
		Material m;
		List<String> info = null;
		
		switch(upgrade) {
		case LOOT:
			m = Material.EMERALD_ORE;
			name = "§2§l * §a§lLOOT UPGRADE {level} §7(Upgrade) §2§l*";
			info = new ArrayList<String>(Arrays.asList("", "§8§l[ §2§lINFO","§a Loot Upgrades §7increases§7 the §a§nchances of",
					" §7better §7ores to §agenerate!"));
			break;
		case ORE_STACK:
			m = Material.ENDER_PORTAL_FRAME;
			name = "§3§l * §b§lORE STACK UPGRADE {level} §7(Upgrade) §3§l*";
			info = new ArrayList<String>(Arrays.asList("", "§8§l[ §3§lINFO","§b Ore Stack Upgrades §7increases§7 the §b§nmax number",
					" §7of ores that can be §bstored§7 in an Ore Block!"));
			break;
		case SPEED:
			m = Material.FEATHER;
			name = "§6§l * §e§lSPEED UPGRADE {level} §7(Upgrade) §6§l*";
			info = new ArrayList<String>(Arrays.asList("", "§8§l[ §6§lINFO","§e Speed Upgrades §7reduces the §6§ntime taken",
						"§7 for the next ore to §6generate§7!"));
			break;
			
		default: return;
		}
		
		int slot = 9;
		for (int i = 1; i <= 21; i++) {
			slot++;
			if( (slot+1) % 9 == 0 ) slot += 2;
			upgradeIndex++;
			
			ItemStack item = new ItemStack(m, 1);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name.replace("{level}", String.valueOf(upgradeIndex)));
			
			List<String> lore = new ArrayList<String>();
			lore.add("");
			lore.add("§8§l[ §a§l"+(upgradeIndex == 1 ? "DEFAULT":  (upgradeIndex == gen.getLevelOf(upgrade) ? "CURRENT" : "UPGRADE") ));
			switch(upgrade) {
			case LOOT:
				OreSpawnTable thisLevel = OreGenerator.getSpawnableMaterials(upgradeIndex);
				OreSpawnTable prevLevel = OreGenerator.getSpawnableMaterials(upgradeIndex-1);
				List<String> fullLore = new ArrayList<String>();
				for (Material material : OreBlock.getPossibleOreTypes()) {
					if(thisLevel.getDistributionOf(material) > 0 &&
							(prevLevel.getDistributionOf(material) >= 0 || upgradeIndex-1 == 0)) {
						String line = "§7"+prettifyMaterial(material.name())+": §a"+ df.format(thisLevel.getChanceOf(material)) + "%";
						if(thisLevel.getDistributionOf(material) != prevLevel.getDistributionOf(material)) {
							if(upgradeIndex-1 != 0 && upgradeLevel+1 == upgradeIndex)
								line += "§8 (§cfrom " + df.format(prevLevel.getChanceOf(material)) + "%§8)";
						}
						fullLore.add(line);
						//fullLore.add( §8(§cfrom " + df.format(prevLevel.getChanceOf(material)) + "%§8)" );
					}
				}
				double additionalCooldown = 1000;
				if(upgradeIndex > 1) {
					additionalCooldown = ( (double) (OreGenerator.getCooldown(upgradeIndex, 1) - (double) OreGenerator.getCooldown(upgradeIndex-1, 1))/1000.0);
					String cd = "§7Cooldown: §c+"+ df.format(additionalCooldown) + "s/gen";
					if(upgradeLevel + 1 == upgradeIndex || upgradeLevel == upgradeIndex)
						cd += "§8 (§7Total:§c"+formatTime((int) (OreGenerator.getCooldown(upgradeIndex, gen.getSpeedLevel())/1000)) +"/gen§8)";
					fullLore.add(cd);
				}
				lore.addAll(fullLore);
				break;
			case SPEED:
				lore.add("§7 * §a-"+df.format(OreGenerator.getSpeedIncrease(upgradeIndex)) + "s Cooldown" + (upgradeIndex == gen.getLevelOf(upgrade) ? "" : " §7from §c-" + df.format(OreGenerator.getSpeedIncrease((upgradeIndex > 1 ? upgradeIndex-1 : upgradeIndex))) + "s Cooldown") );
				if(upgradeLevel + 1 == upgradeIndex || upgradeLevel == upgradeIndex) 
					lore.add("§7 * §7Ore Gen Cooldown:§a" + formatTime((int) (OreGenerator.getCooldown(gen.getLootLevel(), upgradeIndex)/1000)) + "/gen" );
				break;
			case ORE_STACK:
				lore.add("§7 * §a"+OreGenerator.getMaxOreStack(upgradeIndex) + " Max Stack §7from §c" + OreGenerator.getMaxOreStack((upgradeIndex > 1 ? upgradeIndex-1 : upgradeIndex)) + " Max Stack" );
				break;
			}
			lore.add("");
			meta.setLore(lore);
			
			if(upgradeLevel >= upgradeIndex) {
				item.setType(Material.STAINED_GLASS_PANE);
				lore.add("§7 (( §7§oUpgrade already §a§n§ounlocked§7§o! §7))");
				meta.setLore(lore);
				item.setItemMeta(meta);
				item.setDurability((short) 5);
				gui.assignSlotButton(slot, 1, new Button(item));
			}else if (upgradeLevel+1 == upgradeIndex) {
				lore.addAll(0,info);
				lore.add("§7 (( §7§oUpgrade costs: §a$"+numberFormatShort(OreGenerator.getCostOf(upgrade, upgradeIndex))+" §7))");
				lore.add("§7 (( §7§oLeft-click to §7§n§oupgrade§7§o to §aLevel "+upgradeIndex+". §7))");
				meta.setLore(lore);
				item.setItemMeta(meta);
				final int level = upgradeIndex;
				gui.assignSlotButton(slot, 1, new Button(item, (s,p,c) -> {
					List<OreGenerator> fromVar = getGeneratorsOnly(gen.getIslandOwner());
					for(OreGenerator updatedGen : fromVar) {
						if(updatedGen.getLocation().equals(gen.getLocation())) {
							if(updatedGen.getLevelOf(upgrade)+1 == level) {
								if(level <= OreGenerator.getMaxLevelOf(upgrade)) {
									if(econ.getBalance(player) >= OreGenerator.getCostOf(upgrade, level)) {
										boolean allowed = true;
										if(upgrade == UPGRADE.SPEED) {
											if(updatedGen.getLevelOf(upgrade) >= updatedGen.getLevelOf(UPGRADE.LOOT)) {
												allowed = false;
												player.sendMessage(prefix + "You cannot upgrade Speed past the totem's Loot Upgrade's level!");
											}
										}
										if(allowed) {
											econ.withdrawPlayer(player, OreGenerator.getCostOf(upgrade, level));
											updatedGen.setLevelOf(upgrade, level);
											player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
											
											updateHologram(updatedGen.getHologramLocation(),updatedGen.getHologramLines());
											
											openTotemUpgradesGUI(player, updatedGen, upgrade);
											saveToVar(gen.getIslandOwner(), fromVar);	
										}
									}
									else {
										player.sendMessage(prefix + "Insufficient funds!");
										player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1.0f, 0.3f);
									}
								
									break;	
								}else {
									player.sendMessage(prefix + "Such operations will exceed the maximum level.");
								}
								
							}
							else {
								player.closeInventory();
								player.sendMessage(prefix + "Desync'd, reopen the menu again.");
							}
						}
					}
				}));
			}else {
				if(upgradeIndex <= OreGenerator.getMaxLevelOf(upgrade)) {
					item.setType(Material.STAINED_GLASS_PANE);
					lore.add("§7 (( §7§oUpgrade costs: §a§o$"+numberFormatShort(OreGenerator.getCostOf(upgrade, upgradeIndex))+" §7))");
					lore.add("§7 (( §7§oUpgrade the previous level first! §7))");
					meta.setLore(lore);
					item.setItemMeta(meta);
					item.setDurability((short) 14);
					gui.assignSlotButton(slot, 1, new Button(item));	
				}else {
					gui.assignSlotButton(slot, 1, new Button(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7)).setName("§7 "));
				}
			}
		}
		gui.open(platform, player);
	}
	
	
	public Boolean isTotemMaterial(Material m) {
		if(OreGenerator.getPossibleGenBlock().contains(m) ||
			OreBlock.getPossibleOreTypes().contains(m))
			return true;
		return false;
	}
	
	/**
	 * Whether this block is a totem ore or not
	 * @return true if it is
	 */
	public Boolean isATotemBlock(Block block, List<OreGenerator> entry) {
		if(entry != null) {
			for(OreGenerator gen : entry) {
				if(gen.isInLocation(block.getLocation())) {
					return true;
				}
				for(OreBlock ore : gen.getOreBlocks()) {
					if(ore.getLocation().equals(block.getLocation())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Whether this block is a totem block or not
	 * @return true if it is
	 */
	public Boolean isATotemBlock(List<Block> blocks, List<OreGenerator> entry) {
		if(entry != null) {
			for(OreGenerator gen : entry) {
				for (Block blockLoc : blocks) {
					if(gen.isInLocation(blockLoc.getLocation())) {
						return true;
					}	
				}
				for(OreBlock ore : gen.getOreBlocks()) {
					for (Block blockLoc : blocks) {
						if(ore.getLocation().equals(blockLoc.getLocation())) {
							return true;
						}	
					}
				}
			}
		}
		return false;
	}
	
	public void destroyPhysicalBlocks(OreGenerator gen) {
		List<Block> blocks = new ArrayList<Block>(45);
		Block totemPlace = gen.getLocation().clone().add(0,-2,0).getBlock();
		for(int x = -1; x <= 1; x++) {
			for(int y = 0; y<=5; y++) {
				for(int z = -1; z <= 1; z++) {
					blocks.add(totemPlace.getRelative(x,y,z));
				}
			}
		}
		Collections.shuffle(blocks);
		int timer = 0;
		for (Block block : blocks) {
			timer++;
			delayRun(() -> {
				if(block.getChunk().isLoaded()) {
					if(block.getType() != Material.AIR) {
						block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, (float) ran.nextDouble(0.5,1.5));
						block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5,0.75,0.5), 70, new MaterialData(block.getType()));
						block.setType(Material.AIR);	
					}
				}
			},timer,false);
		}
		delayRun(() -> removeHologram(gen.getHologramLocation()),timer+1,false);
	}
	
	@EventHandler
	public void onIslandEnter(IslandEnterEvent event) {
		Set<UUID> onIsland = sessionVar().playersOnIsland.getOrDefault(event.getIslandOwner(), new HashSet<UUID>());
		onIsland.add(event.getPlayer());
		sessionVar().playersOnIsland.put(event.getIslandOwner(), onIsland);
	}
	
	@EventHandler
	public void onIslandExit(IslandExitEvent event) {
		Set<UUID> onIsland = sessionVar().playersOnIsland.getOrDefault(event.getIslandOwner(), new HashSet<UUID>());
		onIsland.remove(event.getPlayer());
		if(onIsland.size() == 0)
			sessionVar().playersOnIsland.remove(event.getIslandOwner());
		else
			sessionVar().playersOnIsland.put(event.getIslandOwner(), onIsland); 
	}
	
	@EventHandler
	public void islandReset(IslandPreDeleteEvent event) {
		UUID ownerUUID = ASkyAPI.getTeamLeader(event.getPlayerUUID());
		String owner = ownerUUID.toString();
		Variables.set("Island", owner, "Totems", null);
	}
	
	@EventHandler
	public void islandCreate(IslandNewEvent event) {
		UUID ownerUUID = event.getPlayer().getUniqueId();
		String owner = ownerUUID.toString();
		Variables.set("Island", owner, "Totems", new ArrayList<List<OreGenerator>>());
	}
	
	@EventHandler
	public void islandChangeOwner(IslandChangeOwnerEvent event) {
		Variables.set("Island", event.getOldOwner().toString(), "Totems", null);
		Variables.set("Island", event.getNewOwner().toString(), "Totems", new ArrayList<List<OreGenerator>>());
	}
	
	/**
	 * Gets the list of generators of that island from Cache.
	 * 
	 * @param player -> Player that is requesting the List of Generators
	 * @param loc -> Location at which the action has occured
	 * @return -> null if any condition is not met (Player not part of island, no island found) Or List of gens of the island if conditions are met
	 */
	public Entry<UUID, List<OreGenerator>> getGenerators(Player player, Location loc) {
		UUID owner = ASkyAPI.getOwner(loc);
		if(owner == null) return null;
		Boolean inTeam = false;
		if(owner.toString().equals(player.getUniqueId().toString())) {
			inTeam = true;
		}else {
			for (UUID uuid : ASkyAPI.getTeamMembers(owner)) {
				if(uuid.toString().equals(player.getUniqueId().toString())) {
					inTeam = true;
					break;
				}
			}	
		}
		if(!inTeam) return null;
		List<OreGenerator> islandTotems = new ArrayList<OreGenerator>();
		try {
			islandTotems = (List<OreGenerator>) Variables.get("Island", owner.toString(), "Totems");
		}catch (ClassCastException e) {
			if (Variables.get("Island", owner.toString(), "Totems") instanceof String) {
				byte[] data = Base64.getDecoder().decode((String) Variables.get("Island", owner.toString(), "Totems"));
				try {
					Variables.set("Island", owner.toString(), "Totems", deserialize(data));
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		Map.Entry<UUID, List<OreGenerator>> entry = Pair.of(owner, islandTotems);
		return entry;
	}
	
	public List<OreGenerator> getGeneratorsOnly(UUID owner) {
		if(owner == null) return null;
		List<OreGenerator> islandTotems = new ArrayList<OreGenerator>();
		try {
			islandTotems = (List<OreGenerator>) Variables.get("Island", owner.toString(), "Totems");
		}catch (ClassCastException e) {
			if (Variables.get("Island", owner.toString(), "Totems") instanceof String) {
				byte[] data = Base64.getDecoder().decode((String) Variables.get("Island", owner.toString(), "Totems"));
				try {
					Variables.set("Island", owner.toString(), "Totems", deserialize(data));
					islandTotems = (List<OreGenerator>) Variables.get("Island", owner.toString(), "Totems");
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return islandTotems;
	}
	
	public Boolean saveToVar(UUID owner, List<OreGenerator> newGens) {
		Variables.set("Island", owner.toString(), "Totems", newGens);	
		return true;
	}
	
	/**
	 * Control block movement events
	 */
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onExplode(BlockExplodeEvent event) {
		if(!event.getBlock().getWorld().getName().equalsIgnoreCase("Skyblock")) {
			return;
		}
		Location eventLoc = event.getBlock().getLocation();
		UUID owner = ASkyAPI.getOwner(eventLoc);
		List<OreGenerator> gens = getGeneratorsOnly(owner);
		
		if(isATotemBlock(event.getBlock(), gens)) {
			event.setCancelled(true);
		}
	}

	//side note: can improve performance if causing issues by:
	//creating a piston cache for "banned pistons" that alr know they're
	//tryna push ore blocks. Island based HashMap<UUID, List<Blocks>>
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonPush(BlockPistonExtendEvent event) {
		if(!event.getBlock().getWorld().getName().equalsIgnoreCase("Skyblock")) {
			return;
		}
		List<Material> toLookFor = OreGenerator.getPossibleGenBlock();
		toLookFor.addAll(OreBlock.getPossibleOreTypes());
		Boolean proceed = false;
		for (Block block : event.getBlocks()) {
			if(toLookFor.contains(block.getType())) {
				proceed = true;
				break;
			}
		}
		
		if(!proceed) return;
		
		Location eventLoc = event.getBlock().getLocation();
		UUID owner = ASkyAPI.getOwner(eventLoc);
		List<OreGenerator> gens = getGeneratorsOnly(owner);
		
		if(isATotemBlock(event.getBlocks(), gens)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonPull(BlockPistonRetractEvent event) {
		if(!event.getBlock().getWorld().getName().equalsIgnoreCase("Skyblock")) {
			return;
		}
		
		List<Material> toLookFor = OreGenerator.getPossibleGenBlock();
		toLookFor.addAll(OreBlock.getPossibleOreTypes());
		Boolean proceed = false;
		for (Block block : event.getBlocks()) {
			if(toLookFor.contains(block.getType())) {
				proceed = true;
				break;
			}
		}
		
		if(!proceed) return;
		
		Location eventLoc = event.getBlock().getLocation();
		UUID owner = ASkyAPI.getOwner(eventLoc);
		List<OreGenerator> gens = getGeneratorsOnly(owner);
		
		if(isATotemBlock(event.getBlocks(), gens)) {
			event.setCancelled(true);
		}
	}
	
	
	
	/**
	 * Hologram functions
	 */
	
    public void createHologram(Location location, List<String> lines) {
    	createHologramAt(fixLocation(location), lines);
    }
    
    private void createHologramAt(Location location, List<String> lines) {
        Hologram hologram = HologramsAPI.createHologram(platform, location);
		for (String line : lines) {
		    hologram.appendTextLine(line);
		}
	}
    
    public void updateHologram(Location location, List<String> lines) {
        location = fixLocation(location);
        for (Hologram hologram : HologramsAPI.getHolograms(platform)) {
            if (hologram.getX() != location.getX()
                    || hologram.getY() != location.getY()
                    || hologram.getZ() != location.getZ()) continue;
            // only update if there is a change to the text
            boolean isChanged = lines.size() != hologram.size();
            if(!isChanged) {
                // double-check the lines
                for(int i = 0; !isChanged && i < lines.size(); ++i) {
                    isChanged = !hologram.getLine(i).toString().equals("CraftTextLine [text=" + lines.get(i) + "]");
                }
            }
            if(isChanged) {
                hologram.clearLines();
                for (String line : lines) {
                    hologram.appendTextLine(line);
                }
            }
            return;
        }
        createHologramAt(location, lines);
    }
    
    public void removeHologram(Location location) {
        location = fixLocation(location);
        for (Hologram hologram : HologramsAPI.getHolograms(platform)) {
            if (hologram.getX() != location.getX()
                    || hologram.getY() != location.getY()
                    || hologram.getZ() != location.getZ()) continue;
            hologram.delete();
        }
    }
    
    public final Location fixLocation(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return location.clone().add((x - (int) x) + xOffset, (y - (int) y) + yOffset, (z - (int) z) + zOffset);
    }
	
	/**
	 * Serialization & Deserialization functions
	 */
	
	private static String serialize(Object object) {
		try (ByteArrayOutputStream bs = new ByteArrayOutputStream(); ObjectOutputStream os = new ObjectOutputStream(bs)) {
			os.writeObject(object);
	        return Base64.getEncoder().encodeToString(bs.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Object deserialize(byte[] data) throws ClassNotFoundException{
        try (ObjectInputStream os = new ObjectInputStream(new ByteArrayInputStream(data))) {
        	Object o = os.readObject();
            os.close();
            return o;
        } catch (IOException e) {
        	if(e instanceof StreamCorruptedException || e instanceof EOFException) throw new IllegalArgumentException("Invalid serialization!");
        	e.printStackTrace();
        }
        return null;
	}
	
	private String prettifyMaterial(String entityName) {
    	entityName = entityName.toLowerCase().replaceAll("_", " ");
    	String[] arr = entityName.split(" ");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                .append(arr[i].substring(1)).append(" ");
        }          
        return sb.toString().trim();
    }
	
	public Boolean areaClearForTotem(Block totemPlace) {
		for(int x = -1; x <= 1; x++) {
			for(int y = 0; y<=5; y++) {
				for(int z = -1; z <= 1; z++) {
					if(x == 0 && y == 0 && z == 0) continue;
					if(totemPlace.getRelative(x,y,z).getType() != Material.AIR) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * This is STANDARD WORLD EDIT Schematic pasting
	 * Spent way too much time searching CBA to fix it with FAWE system
	 * if for bigger proj this is req, find it urself kthxbye;D
	 * solu: probably follow a standard tut but make sure
	 * your FAWE hook is the one in the plugin folder cause yes
	 * but i didnt test it yes cause yes, this works so im done ok 
	 * aoidawjdadawjdoaidjsojoaijwd
	 * 
	 */
	public boolean pasteSchematic(String schematicName, Location loc, boolean noAir) {
        
        Vector to = new Vector(loc.getBlockX()+1, loc.getBlockY()-1, loc.getBlockZ()+1); // Where you want to paste

        com.sk89q.worldedit.world.World weWorld = new BukkitWorld(loc.getWorld());
        WorldData worldData = weWorld.getWorldData();
        Clipboard clipboard = null;
		try {
			clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream("plugins/WorldEdit/schematics/"+schematicName+".schematic")).read(worldData);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(clipboard == null) return false;
        //Region region = clipboard.getRegion();

        EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);
        AffineTransform transform = new AffineTransform();

        ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), extent, to);
        if (!transform.isIdentity()) copy.setTransform(transform);
        if (noAir) {
            copy.setSourceMask(new ExistingBlockMask(clipboard));
        }
        try {
			Operations.completeLegacy(copy);
	        extent.flushQueue();
		} catch (MaxChangedBlocksException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
        return true;
    }
	
	public static String formatTime(int secs) {
		if(secs < 1) {
			return " 0s";
		}
		int remainder = secs % 86400;

		int days 	= secs / 86400;
		int hours 	= remainder / 3600;
		int minutes	= (remainder / 60) - (hours * 60);
		int seconds	= (remainder % 3600) - (minutes * 60);

		String fDays 	= (days > 0 	? " " + days + "d" : "");
		String fHours 	= (hours > 0 	? " " + hours + "h" : "");
		String fMinutes = (minutes > 0 	? " " + minutes + "m" : "");
		String fSeconds = (seconds > 0 	? " " + seconds + "s" : "");
		
		return new StringBuilder().append(fDays).append(fHours)
				.append(fMinutes).append(fSeconds).toString();
	}
	
	public String numberFormat(double amt) {
		String formatted = " ";
		if(amt > 1000000000000000.0) {
			formatted = df.format(amt / 1000000000000000.0) + " Quadrillion";
		}else if(amt > 1000000000000.0) {
			formatted = df.format(amt / 1000000000000.0) + " Trillion";
		}else if(amt > 1000000000.0) {
			formatted = df.format(amt / 1000000000.0) + " Billion";
		}else if(amt > 1000000.0) {
			formatted = df.format(amt / 1000000.0) + " Million";
		}else if(amt > 1000.0) {
			formatted = df.format(amt / 1000.0) + " Thousand";
		}else {
			formatted = df.format(amt);
		}
		return formatted;
	}
	
	public String numberFormatShort(double amt) {
		String formatted = " ";
		if(amt >= 1000000000000000.0) {
			formatted = df.format(amt / 1000000000000000.0) + "Q";
		}else if(amt >= 1000000000000.0) {
			formatted = df.format(amt / 1000000000000.0) + "T";
		}else if(amt >= 1000000000.0) {
			formatted =  df.format(amt / 1000000000.0) + "B";
		}else if(amt >= 1000000.0) {
			formatted = df.format(amt / 1000000.0) + "M";
		}else if(amt >= 1000.0) {
			formatted = df.format(amt / 1000.0) + "K";
		}else {
			formatted = df.format(amt);
		}
		return formatted;
	}
	
	public static Integer getLevel(List<String> lore, String ench) {
		for (int i = 0; i < lore.size(); i++) {
			if(lore.get(i).contains("❘ §f"+ench)) {
				try {
					return Integer.parseInt(ChatColor.stripColor(lore.get(i).split(ench)[1]).trim());
				}catch (NumberFormatException e) {
					return -1;
				}
			}
		}
		return 0;
	}
	
	public void addMobCoin(OfflinePlayer player, int amt) {
		if(player != null) {
			if(Variables.getPlayer(player, "MobCoins") == null) {
				Variables.setPlayer(player, "MobCoins", 0);
			}
			Variables.setPlayer(player, "MobCoins", (int) Variables.getPlayer(player, "MobCoins")+amt);	
		}
	}
	
	public void setMobCoin(OfflinePlayer player, int amt) {
		Variables.setPlayer(player, "MobCoins", amt);
	}
	
	public int getMobCoinsBalance(OfflinePlayer player) {
		if(Variables.getPlayer(player, "MobCoins") == null) {
			Variables.setPlayer(player, "MobCoins", 0);
		}
		return (int) Variables.getPlayer(player, "MobCoins");
	}
	
	public Boolean subtractMobCoin(OfflinePlayer player, int amt) {
		if(Variables.getPlayer(player, "MobCoins") == null) {
			Variables.setPlayer(player, "MobCoins", 0);
		}
		
		int currentBal = (int) Variables.getPlayer(player, "MobCoins");
		if(currentBal >= amt) {
			currentBal -= amt;
			Variables.setPlayer(player, "MobCoins", currentBal);
			return true;
		}
		
		return false;
	}
	
	 
	
}