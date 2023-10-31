package me.despawningbone.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.SplittableRandom;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import me.despawningbone.modules.api.SimpleLocation;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class OreGenerator implements Serializable {
	
	private static final long serialVersionUID = 7098976005951462816L;
	private static final SplittableRandom ran = new SplittableRandom();  //avoid object instantiation every time

	static String prefix = "§8[§3§l!§8] §7";
	static int oreStack_MAXLVL = 50;
	static int loot_MAXLVL = 35;
	static int speed_MAXLVL = 35;
	
	final UUID islandOwner;
	final SimpleLocation simpleLoc; 
	transient Location loc = null;
	Long lastGeneration;
	Long nextGenerationTime;
	int maxOreBlocks;
	int rangeLvl, oreStackLvl, lootLvl, speedLvl;
	List<OreBlock> oreBlocks;
	List<String> hologramLines;
	OreSpawnTable oreTable;
	
	boolean editorMode;
	
	enum UPGRADE {
		ORE_STACK,
		LOOT,
		SPEED
	}
	
	public static List<Material> getPossibleGenBlock() {
		return new ArrayList<Material>(Arrays.asList(
				Material.SEA_LANTERN,
				Material.COBBLE_WALL,
				Material.SMOOTH_BRICK,
				Material.SMOOTH_STAIRS));
	}
	
	public static OreSpawnTable getSpawnableMaterials(int lootLvl) {
		OreSpawnTable table = new OreSpawnTable();
		
		/**
		 * Modify each Loot Level's spawn chances here
		 */
		switch(lootLvl) {
		case 1:
			table.addMaterial(Material.COBBLESTONE, 55);
			table.addMaterial(Material.COAL_ORE, 15);
			table.addMaterial(Material.IRON_ORE, 10);
			table.addMaterial(Material.LAPIS_ORE, 7.5);
			table.addMaterial(Material.REDSTONE_ORE, 7.0);
			table.addMaterial(Material.GOLD_ORE, 5);
			table.addMaterial(Material.DIAMOND_ORE, 0.5);
			break;
		case 2:
			table.addMaterial(Material.COBBLESTONE, 50);
			table.addMaterial(Material.COAL_ORE, 12.5);
			table.addMaterial(Material.IRON_ORE, 12.5);
			table.addMaterial(Material.LAPIS_ORE, 8.50);
			table.addMaterial(Material.REDSTONE_ORE, 8.25);
			table.addMaterial(Material.GOLD_ORE, 7.50);
			table.addMaterial(Material.DIAMOND_ORE, 0.75);
			break;
		case 3:
			table.addMaterial(Material.COBBLESTONE, 43.5);
			table.addMaterial(Material.COAL_ORE, 12.5);
			table.addMaterial(Material.IRON_ORE, 12.5);
			table.addMaterial(Material.LAPIS_ORE, 10);
			table.addMaterial(Material.REDSTONE_ORE, 10);
			table.addMaterial(Material.GOLD_ORE, 10);
			table.addMaterial(Material.DIAMOND_ORE, 1);
			table.addMaterial(Material.EMERALD_ORE, 0.5);
			break;
		case 4:
			table.addMaterial(Material.COBBLESTONE, 40);
			table.addMaterial(Material.COAL_ORE, 10);
			table.addMaterial(Material.IRON_ORE, 10);
			table.addMaterial(Material.LAPIS_ORE, 10);
			table.addMaterial(Material.REDSTONE_ORE, 10);
			table.addMaterial(Material.GOLD_ORE, 15);
			table.addMaterial(Material.DIAMOND_ORE, 1.5);
			table.addMaterial(Material.EMERALD_ORE, 1);
			table.addMaterial(Material.COAL_BLOCK, 2.5);
			
			break;
		case 5:
			table.addMaterial(Material.COBBLESTONE, 35);
			table.addMaterial(Material.COAL_ORE, 7.5);
			table.addMaterial(Material.IRON_ORE, 9);
			table.addMaterial(Material.LAPIS_ORE, 10);
			table.addMaterial(Material.REDSTONE_ORE, 10);
			table.addMaterial(Material.GOLD_ORE, 15);
			table.addMaterial(Material.DIAMOND_ORE, 5);
			table.addMaterial(Material.EMERALD_ORE, 2.5);
			table.addMaterial(Material.COAL_BLOCK, 5);
			table.addMaterial(Material.IRON_BLOCK, 1);
			break;
		case 6:
			table.addMaterial(Material.COBBLESTONE, 30);
			table.addMaterial(Material.COAL_ORE, 5);
			table.addMaterial(Material.IRON_ORE, 8);
			table.addMaterial(Material.LAPIS_ORE, 9);
			table.addMaterial(Material.REDSTONE_ORE, 9);
			table.addMaterial(Material.GOLD_ORE, 15);
			table.addMaterial(Material.DIAMOND_ORE, 10);
			table.addMaterial(Material.EMERALD_ORE, 5);
			table.addMaterial(Material.COAL_BLOCK, 6.5);
			table.addMaterial(Material.IRON_BLOCK, 2.5);
			break;
		case 7:
			table.addMaterial(Material.COBBLESTONE, 25);
			table.addMaterial(Material.COAL_ORE, 3.5);
			table.addMaterial(Material.IRON_ORE, 7);
			table.addMaterial(Material.LAPIS_ORE, 7.5);
			table.addMaterial(Material.REDSTONE_ORE, 7.5);
			table.addMaterial(Material.GOLD_ORE, 12);
			table.addMaterial(Material.DIAMOND_ORE, 15);
			table.addMaterial(Material.EMERALD_ORE, 7.5);
			table.addMaterial(Material.COAL_BLOCK, 8);
			table.addMaterial(Material.IRON_BLOCK, 3.5);
			table.addMaterial(Material.LAPIS_BLOCK, 2);
			table.addMaterial(Material.REDSTONE_BLOCK, 1.5);
			break;
		case 8:
			table.addMaterial(Material.COBBLESTONE, 25);
			table.addMaterial(Material.IRON_ORE, 5);
			table.addMaterial(Material.LAPIS_ORE, 7.5);
			table.addMaterial(Material.REDSTONE_ORE, 7.5);
			table.addMaterial(Material.GOLD_ORE, 10);
			table.addMaterial(Material.DIAMOND_ORE, 20);
			table.addMaterial(Material.EMERALD_ORE, 10);
			table.addMaterial(Material.COAL_BLOCK, 8);
			table.addMaterial(Material.IRON_BLOCK, 3.5);
			table.addMaterial(Material.LAPIS_BLOCK, 2);
			table.addMaterial(Material.REDSTONE_BLOCK, 1.5);
			break;
		case 9:
			table.addMaterial(Material.COBBLESTONE, 22.5);
			table.addMaterial(Material.IRON_ORE, 5);
			table.addMaterial(Material.LAPIS_ORE, 5);
			table.addMaterial(Material.REDSTONE_ORE, 5);
			table.addMaterial(Material.GOLD_ORE, 5);
			table.addMaterial(Material.DIAMOND_ORE, 22.5);
			table.addMaterial(Material.EMERALD_ORE, 15);
			table.addMaterial(Material.COAL_BLOCK, 8);
			table.addMaterial(Material.IRON_BLOCK, 5);
			table.addMaterial(Material.LAPIS_BLOCK, 3.5);
			table.addMaterial(Material.REDSTONE_BLOCK, 2);
			table.addMaterial(Material.GOLD_BLOCK, 1.5);
			break;
		case 10:
			table.addMaterial(Material.COBBLESTONE, 20);
			table.addMaterial(Material.IRON_ORE, 2.25);
			table.addMaterial(Material.LAPIS_ORE, 5);
			table.addMaterial(Material.REDSTONE_ORE, 5);
			table.addMaterial(Material.GOLD_ORE, 5);
			table.addMaterial(Material.DIAMOND_ORE, 22.5);
			table.addMaterial(Material.EMERALD_ORE, 16.5);
			table.addMaterial(Material.COAL_BLOCK, 8);
			table.addMaterial(Material.IRON_BLOCK, 6.5);
			table.addMaterial(Material.LAPIS_BLOCK, 4);
			table.addMaterial(Material.REDSTONE_BLOCK, 3);
			table.addMaterial(Material.GOLD_BLOCK, 2.25);
			break;
		case 11:
			table.addMaterial(Material.COBBLESTONE, 17.5);
			table.addMaterial(Material.LAPIS_ORE, 4);
			table.addMaterial(Material.REDSTONE_ORE, 4);
			table.addMaterial(Material.GOLD_ORE, 5);
			table.addMaterial(Material.DIAMOND_ORE, 23.5);
			table.addMaterial(Material.EMERALD_ORE, 18.5);
			table.addMaterial(Material.COAL_BLOCK, 8);
			table.addMaterial(Material.IRON_BLOCK, 6.5);
			table.addMaterial(Material.LAPIS_BLOCK, 4);
			table.addMaterial(Material.REDSTONE_BLOCK, 4);
			table.addMaterial(Material.GOLD_BLOCK, 3.5);
			table.addMaterial(Material.DIAMOND_BLOCK, 1.5);
			//table.addMaterial(Material.EMERALD_BLOCK, 0.5);
			break;
		case 12:
			table.addMaterial(Material.COBBLESTONE, 15);
			table.addMaterial(Material.LAPIS_ORE, 3);
			table.addMaterial(Material.REDSTONE_ORE, 3);
			table.addMaterial(Material.GOLD_ORE, 4);
			table.addMaterial(Material.DIAMOND_ORE, 20);
			table.addMaterial(Material.EMERALD_ORE, 16.5);
			table.addMaterial(Material.COAL_BLOCK, 10);
			table.addMaterial(Material.IRON_BLOCK, 8.5);
			table.addMaterial(Material.LAPIS_BLOCK, 6);
			table.addMaterial(Material.REDSTONE_BLOCK, 6);
			table.addMaterial(Material.GOLD_BLOCK, 5);
			table.addMaterial(Material.DIAMOND_BLOCK, 2.5);
			table.addMaterial(Material.EMERALD_BLOCK, 0.5);
			break;
		case 13:
			table.addMaterial(Material.COBBLESTONE, 12.5);
			table.addMaterial(Material.LAPIS_ORE, 2);
			table.addMaterial(Material.REDSTONE_ORE, 2);
			table.addMaterial(Material.GOLD_ORE, 3);
			table.addMaterial(Material.DIAMOND_ORE, 20);
			table.addMaterial(Material.EMERALD_ORE, 16.5);
			table.addMaterial(Material.COAL_BLOCK, 10);
			table.addMaterial(Material.IRON_BLOCK, 8.5);
			table.addMaterial(Material.LAPIS_BLOCK, 7.25);
			table.addMaterial(Material.REDSTONE_BLOCK, 7.25);
			table.addMaterial(Material.GOLD_BLOCK, 6.5);
			table.addMaterial(Material.DIAMOND_BLOCK, 3.5);
			table.addMaterial(Material.EMERALD_BLOCK, 1);
			break;
		case 14:
			table.addMaterial(Material.COBBLESTONE, 10);
			table.addMaterial(Material.GOLD_ORE, 2);
			table.addMaterial(Material.DIAMOND_ORE, 19);
			table.addMaterial(Material.EMERALD_ORE, 15);
			table.addMaterial(Material.COAL_BLOCK, 10);
			table.addMaterial(Material.IRON_BLOCK, 10);
			table.addMaterial(Material.LAPIS_BLOCK, 9.5);
			table.addMaterial(Material.REDSTONE_BLOCK, 9.5);
			table.addMaterial(Material.GOLD_BLOCK, 8);
			table.addMaterial(Material.DIAMOND_BLOCK, 4.5);
			table.addMaterial(Material.EMERALD_BLOCK, 2.5);
			break;
		case 15:
			table.addMaterial(Material.COBBLESTONE, 8);
			table.addMaterial(Material.DIAMOND_ORE, 17);
			table.addMaterial(Material.EMERALD_ORE, 15);
			table.addMaterial(Material.COAL_BLOCK, 10);
			table.addMaterial(Material.IRON_BLOCK, 10);
			table.addMaterial(Material.LAPIS_BLOCK, 10);
			table.addMaterial(Material.REDSTONE_BLOCK, 10);
			table.addMaterial(Material.GOLD_BLOCK, 10);
			table.addMaterial(Material.DIAMOND_BLOCK, 6.5);
			table.addMaterial(Material.EMERALD_BLOCK, 3);
			table.addMaterial(Material.SPONGE, 0.5);
			break;
		case 16:
			table.addMaterial(Material.COBBLESTONE, 5);
			table.addMaterial(Material.DIAMOND_ORE, 15);
			table.addMaterial(Material.EMERALD_ORE, 15);
			table.addMaterial(Material.COAL_BLOCK, 9.5);
			table.addMaterial(Material.IRON_BLOCK, 9.5);
			table.addMaterial(Material.LAPIS_BLOCK, 10);
			table.addMaterial(Material.REDSTONE_BLOCK, 10);
			table.addMaterial(Material.GOLD_BLOCK, 12);
			table.addMaterial(Material.DIAMOND_BLOCK, 8.5);
			table.addMaterial(Material.EMERALD_BLOCK, 4.75);
			table.addMaterial(Material.SPONGE, 0.75);
			break;
		case 17:
			table.addMaterial(Material.COBBLESTONE, 2.5);
			table.addMaterial(Material.DIAMOND_ORE, 12.5);
			table.addMaterial(Material.EMERALD_ORE, 15);
			table.addMaterial(Material.COAL_BLOCK, 9);
			table.addMaterial(Material.IRON_BLOCK, 9);
			table.addMaterial(Material.LAPIS_BLOCK, 11);
			table.addMaterial(Material.REDSTONE_BLOCK, 11);
			table.addMaterial(Material.GOLD_BLOCK, 14.5);
			table.addMaterial(Material.DIAMOND_BLOCK, 10);
			table.addMaterial(Material.EMERALD_BLOCK, 5.5);
			table.addMaterial(Material.SPONGE, 1);
			break;
		case 18:
			table.addMaterial(Material.DIAMOND_ORE, 12.5);
			table.addMaterial(Material.EMERALD_ORE, 15);
			table.addMaterial(Material.COAL_BLOCK, 8);
			table.addMaterial(Material.IRON_BLOCK, 8.25);
			table.addMaterial(Material.LAPIS_BLOCK, 10);
			table.addMaterial(Material.REDSTONE_BLOCK, 10);
			table.addMaterial(Material.GOLD_BLOCK, 16);
			table.addMaterial(Material.DIAMOND_BLOCK, 12.5);
			table.addMaterial(Material.EMERALD_BLOCK, 6.5);
			table.addMaterial(Material.SPONGE, 1.25);
			break;
		case 19:
			table.addMaterial(Material.DIAMOND_ORE, 10.5);
			table.addMaterial(Material.EMERALD_ORE, 12.5);
			table.addMaterial(Material.COAL_BLOCK, 7.5);
			table.addMaterial(Material.IRON_BLOCK, 8);
			table.addMaterial(Material.LAPIS_BLOCK, 10);
			table.addMaterial(Material.REDSTONE_BLOCK, 10);
			table.addMaterial(Material.GOLD_BLOCK, 17);
			table.addMaterial(Material.DIAMOND_BLOCK, 14.5);
			table.addMaterial(Material.EMERALD_BLOCK, 8.5);
			table.addMaterial(Material.SPONGE, 1.50);
			break;
		case 20:
			table.addMaterial(Material.DIAMOND_ORE, 7.5);
			table.addMaterial(Material.EMERALD_ORE, 10.5);
			table.addMaterial(Material.COAL_BLOCK, 7);
			table.addMaterial(Material.IRON_BLOCK, 7.75);
			table.addMaterial(Material.LAPIS_BLOCK, 10);
			table.addMaterial(Material.REDSTONE_BLOCK, 10);
			table.addMaterial(Material.GOLD_BLOCK, 18.5);
			table.addMaterial(Material.DIAMOND_BLOCK, 16.5);
			table.addMaterial(Material.EMERALD_BLOCK, 10.5);
			table.addMaterial(Material.SPONGE, 1.75);
			break;
		case 21:
			table.addMaterial(Material.DIAMOND_ORE, 7.0);
			table.addMaterial(Material.EMERALD_ORE, 10);
			table.addMaterial(Material.COAL_BLOCK, 5);
			table.addMaterial(Material.IRON_BLOCK, 6.75);
			table.addMaterial(Material.LAPIS_BLOCK, 9.5);
			table.addMaterial(Material.REDSTONE_BLOCK, 9.5);
			table.addMaterial(Material.GOLD_BLOCK, 19.25);
			table.addMaterial(Material.DIAMOND_BLOCK, 18.5);
			table.addMaterial(Material.EMERALD_BLOCK, 12.5);
			table.addMaterial(Material.SPONGE, 2.0);
			break;
		case 22:
			table.addMaterial(Material.DIAMOND_ORE, 5.0);
			table.addMaterial(Material.EMERALD_ORE, 8);
			table.addMaterial(Material.COAL_BLOCK, 4.5);
			table.addMaterial(Material.IRON_BLOCK, 6.5);
			table.addMaterial(Material.LAPIS_BLOCK, 9.5);
			table.addMaterial(Material.REDSTONE_BLOCK, 9.5);
			table.addMaterial(Material.GOLD_BLOCK, 20.75);
			table.addMaterial(Material.DIAMOND_BLOCK, 20.5);
			table.addMaterial(Material.EMERALD_BLOCK, 14.5);
			table.addMaterial(Material.SPONGE, 2.25);
			break;
		case 23:
			table.addMaterial(Material.DIAMOND_ORE, 3.25);
			table.addMaterial(Material.EMERALD_ORE, 5);
			table.addMaterial(Material.COAL_BLOCK, 3.5);
			table.addMaterial(Material.IRON_BLOCK, 6);
			table.addMaterial(Material.LAPIS_BLOCK, 9);
			table.addMaterial(Material.REDSTONE_BLOCK, 9);
			table.addMaterial(Material.GOLD_BLOCK, 21.75);
			table.addMaterial(Material.DIAMOND_BLOCK, 22.5);
			table.addMaterial(Material.EMERALD_BLOCK, 17.5);
			table.addMaterial(Material.SPONGE, 2.5);
			break;
		case 24:
			table.addMaterial(Material.DIAMOND_ORE, 2.5);
			table.addMaterial(Material.EMERALD_ORE, 3.5);
			table.addMaterial(Material.COAL_BLOCK, 3);
			table.addMaterial(Material.IRON_BLOCK, 6);
			table.addMaterial(Material.LAPIS_BLOCK, 8);
			table.addMaterial(Material.REDSTONE_BLOCK, 8);
			table.addMaterial(Material.GOLD_BLOCK, 21.25);
			table.addMaterial(Material.DIAMOND_BLOCK, 25.5);
			table.addMaterial(Material.EMERALD_BLOCK, 19.5);
			table.addMaterial(Material.SPONGE, 2.75);
			break;
		case 25:
			table.addMaterial(Material.EMERALD_ORE, 1.5);
			table.addMaterial(Material.COAL_BLOCK, 2.5);
			table.addMaterial(Material.IRON_BLOCK, 5);
			table.addMaterial(Material.LAPIS_BLOCK, 8);
			table.addMaterial(Material.REDSTONE_BLOCK, 8);
			table.addMaterial(Material.GOLD_BLOCK, 20.25);
			table.addMaterial(Material.DIAMOND_BLOCK, 25.5);
			table.addMaterial(Material.EMERALD_BLOCK, 19.5);
			table.addMaterial(Material.SPONGE, 3);
			break;
		case 26:
			table.addMaterial(Material.COAL_BLOCK, 2.5);
			table.addMaterial(Material.IRON_BLOCK, 5);
			table.addMaterial(Material.LAPIS_BLOCK, 7);
			table.addMaterial(Material.REDSTONE_BLOCK, 7);
			table.addMaterial(Material.GOLD_BLOCK, 19.5);
			table.addMaterial(Material.DIAMOND_BLOCK, 27.5);
			table.addMaterial(Material.EMERALD_BLOCK, 21.5);
			table.addMaterial(Material.SPONGE, 3.25);
			break;
		case 27:
			table.addMaterial(Material.COAL_BLOCK, 2.5);
			table.addMaterial(Material.IRON_BLOCK, 5);
			table.addMaterial(Material.LAPIS_BLOCK, 6);
			table.addMaterial(Material.REDSTONE_BLOCK, 6);
			table.addMaterial(Material.GOLD_BLOCK, 18.5);
			table.addMaterial(Material.DIAMOND_BLOCK, 28.5);
			table.addMaterial(Material.EMERALD_BLOCK, 22.5);
			table.addMaterial(Material.SPONGE, 3.5);
			break;
		case 28:
			table.addMaterial(Material.COAL_BLOCK, 1.5);
			table.addMaterial(Material.IRON_BLOCK, 4.5);
			table.addMaterial(Material.LAPIS_BLOCK, 5.5);
			table.addMaterial(Material.REDSTONE_BLOCK, 5.5);
			table.addMaterial(Material.GOLD_BLOCK, 20);
			table.addMaterial(Material.DIAMOND_BLOCK, 30);
			table.addMaterial(Material.EMERALD_BLOCK, 25);
			table.addMaterial(Material.SPONGE, 3.75);
			break;
		case 29:
			table.addMaterial(Material.COAL_BLOCK, 0.5);
			table.addMaterial(Material.IRON_BLOCK, 2.5);
			table.addMaterial(Material.LAPIS_BLOCK, 3.5);
			table.addMaterial(Material.REDSTONE_BLOCK, 3.5);
			table.addMaterial(Material.GOLD_BLOCK, 15);
			table.addMaterial(Material.DIAMOND_BLOCK, 35);
			table.addMaterial(Material.EMERALD_BLOCK, 30);
			table.addMaterial(Material.SPONGE, 4);
			break;
		case 30:
			table.addMaterial(Material.COAL_BLOCK, 0.5);
			table.addMaterial(Material.IRON_BLOCK, 2.5);
			table.addMaterial(Material.LAPIS_BLOCK, 3.5);
			table.addMaterial(Material.REDSTONE_BLOCK, 3.5);
			table.addMaterial(Material.GOLD_BLOCK, 15);
			table.addMaterial(Material.DIAMOND_BLOCK, 35);
			table.addMaterial(Material.EMERALD_BLOCK, 30);
			table.addMaterial(Material.SPONGE, 4.25);
			break;
		case 31:
			table.addMaterial(Material.IRON_BLOCK, 1.5);
			table.addMaterial(Material.LAPIS_BLOCK, 2.5);
			table.addMaterial(Material.REDSTONE_BLOCK, 2.5);
			table.addMaterial(Material.GOLD_BLOCK, 12.5);
			table.addMaterial(Material.DIAMOND_BLOCK, 40);
			table.addMaterial(Material.EMERALD_BLOCK, 35);
			table.addMaterial(Material.SPONGE, 4.5);
			break;
		case 32:
			table.addMaterial(Material.IRON_BLOCK, 1);
			table.addMaterial(Material.LAPIS_BLOCK, 1.5);
			table.addMaterial(Material.REDSTONE_BLOCK, 1.5);
			table.addMaterial(Material.GOLD_BLOCK, 10.5);
			table.addMaterial(Material.DIAMOND_BLOCK, 42.5);
			table.addMaterial(Material.EMERALD_BLOCK, 38.75);
			table.addMaterial(Material.SPONGE, 4.75);
			break;
		case 33:
			table.addMaterial(Material.GOLD_BLOCK, 5);
			table.addMaterial(Material.DIAMOND_BLOCK, 50);
			table.addMaterial(Material.EMERALD_BLOCK, 40);
			table.addMaterial(Material.SPONGE, 5);
			break;
		case 34:
			table.addMaterial(Material.DIAMOND_BLOCK, 52.25);
			table.addMaterial(Material.EMERALD_BLOCK, 42.5);
			table.addMaterial(Material.SPONGE, 5.25);
			break;
		case 35:
			table.addMaterial(Material.DIAMOND_BLOCK, 49.25);
			table.addMaterial(Material.EMERALD_BLOCK, 45);
			table.addMaterial(Material.SPONGE, 5.75);
			break;
		}
		return table;
	}
	
	public static int getMaxOreStack(int oreStackLvl) {
		
		/**
		 * Modify each Ore Stack Level's max ore stack here
		 */
		if(oreStackLvl > 0 && oreStackLvl <= 50) {
			return 4+oreStackLvl;
		}
		else if (oreStackLvl > 50 && oreStackLvl <= 100) {
			return 54 + (oreStackLvl-50);
		}
		else {
			throw new IllegalArgumentException("Ore Stack level Out of Range");
		}
	}
	
	public static double getSpeedIncrease(int speedLvl) {
		if(speedLvl < 1) throw new IllegalArgumentException("Speed level Out of Range");
		return Math.pow(speedLvl-1, 1.55);
	}
	
	public static double getCostOf(UPGRADE type, int level) {
		if(level < 1) throw new IllegalArgumentException("Getting level cost of Level that is < 0!");
		switch(type) {
		case ORE_STACK: return 250 * level * level;
		case LOOT: return 2500 * level * level;
		case SPEED: return 2000 * level * level;
		default: return 0;
		}
	}
	
	public static Long getCooldown(int lootLevel, int speedLevel) {
		if(lootLevel <= 0 || speedLevel <= 0) throw new IllegalArgumentException("Totems loot level & speed level cannot be less or equal to 0!");
		Long timeInMS = (long) (1000 * ( Math.pow(lootLevel, 1.6) - getSpeedIncrease(speedLevel) ));
		if(timeInMS < 0) {
			timeInMS = 0L;
		}
		//return 0L;
		return timeInMS;
	}
	
	public static double getChanceOfSpawn(Material m, int lootLvl) {
		OreSpawnTable table = getSpawnableMaterials(lootLvl);
		return table.getChanceOf(m);
	}
	
	public static String statsToString(int regenBlocks, int lootLvl, int stackLvl, int speedLvl) {
		return regenBlocks + ";" + lootLvl + ";" + stackLvl + ";" + speedLvl;
	}
	
	public OreGenerator(UUID islandOwner, Block block) {
		this.islandOwner = islandOwner;
		this.simpleLoc = new SimpleLocation(block.getLocation().clone().add(0,2,0));
		this.loc = simpleLoc.getTrueLocation();
		this.maxOreBlocks = 3;
		this.oreBlocks = new ArrayList<OreBlock>();
		this.lastGeneration = System.currentTimeMillis();
		this.rangeLvl = 1;
		this.oreStackLvl = 1;
		this.lootLvl = 1;
		this.speedLvl = 1;
		this.editorMode = false;
		this.oreTable = getSpawnableMaterials(this.lootLvl);
		this.nextGenerationTime = System.currentTimeMillis()+getCooldown(this.lootLvl,this.speedLvl);
		this.updateHologramLines();
	}
	
	public OreGenerator(UUID islandOwner, Block block, int maxOreBlocks, int rangeLvl, int oreStackLvl, int lootLvl, int speedLvl) {
		this.islandOwner = islandOwner;
		this.simpleLoc = new SimpleLocation(block.getLocation().clone().add(0,2,0));
		this.loc = simpleLoc.getTrueLocation();
		this.maxOreBlocks = maxOreBlocks;
		this.oreBlocks = new ArrayList<OreBlock>();
		this.lastGeneration = System.currentTimeMillis();
		this.rangeLvl = rangeLvl;
		this.oreStackLvl = oreStackLvl;
		this.lootLvl = lootLvl;
		this.speedLvl = speedLvl;
		this.editorMode = false;
		this.oreTable = getSpawnableMaterials(this.lootLvl);
		this.nextGenerationTime = System.currentTimeMillis()+getCooldown(this.lootLvl,this.speedLvl);
		this.updateHologramLines();
	}
	
	public OreGenerator(List<String> fromString) {
		this.simpleLoc = new SimpleLocation(new SimpleLocation(fromString.get(0)).getTrueLocation().add(0,2,0));
		this.loc = this.simpleLoc.getTrueLocation();
		String split[] = fromString.get(1).split(";");
		this.oreBlocks = new ArrayList<OreBlock>();
		this.maxOreBlocks = Integer.parseInt(split[0]);
		this.lastGeneration = Long.parseLong(split[1]);
		this.rangeLvl = Integer.parseInt(split[2]);
		this.oreStackLvl = Integer.parseInt(split[3]);
		this.lootLvl = Integer.parseInt(split[4]);
		this.speedLvl = Integer.parseInt(split[5]);
		this.islandOwner = UUID.fromString(split[6]);
		this.editorMode = false;
		this.oreTable = getSpawnableMaterials(this.lootLvl);
		this.nextGenerationTime = (System.currentTimeMillis()+getCooldown(this.lootLvl,this.speedLvl));
		this.updateHologramLines();
		if(fromString.size() < 4) return;
		for (int i = 3; i < fromString.size(); i++) {
			oreBlocks.add(new OreBlock(fromString.get(i)));
		}
	}
	
	/**
	 * Do not use this method to turn the generator into a serialized string. Use toSerializedObject()
	 */
	public String toString() {
		throw new IllegalArgumentException("Ore Generator should not be stored using toString but toSerializedObject!");
	}
	
	public List<String> toSerializedObject() {
		List<String> string = new ArrayList<String>();
		string.add(this.simpleLoc.getSerializedString());
		string.add(this.maxOreBlocks+";"+this.lastGeneration+";"+this.rangeLvl+";"+this.oreStackLvl+";"+this.lootLvl+";"+this.speedLvl+";"+islandOwner.toString());
		string.add(""); //reserved for Settings;
		for (OreBlock block : oreBlocks) {
			string.add(block.toString());
		}
		return string;
	}
	
	public UUID getIslandOwner() {
		return this.islandOwner;
	}
	
	public Boolean isInLocation(Location loc) {
		if(this.loc == null) {
			this.loc = this.simpleLoc.getTrueLocation();
		}
		int minX = this.loc.getBlockX() - 1;
		int minZ = this.loc.getBlockZ() - 1;
		int minY = this.loc.getBlockY() - 2;
		if(minX <= loc.getBlockX() &&
			minX+3 > loc.getBlockX() &&
			minZ <= loc.getBlockZ() &&
			minZ+3 > loc.getBlockZ() &&
			minY <= loc.getBlockY() &&
			minY+5 > loc.getBlockY()) {
				return true;
		}
		return false;
	}
	
	public HashMap<ItemStack, Integer> processOreBreak(Player player, OreBlock ore, BlockBreakEvent event) {
		if(event.isCancelled()) return null;
		if(oreBlocks.contains(ore)) {
			if(ore.getOreStack() == 0) {
				return null;
			}
			event.setDropItems(false);
			
			// item drop handling
			World world = event.getBlock().getWorld();
			Location loc = event.getBlock().getLocation();
			int fortuneLevel = 0;
			int experienceLevel = 0;
			int itemabsorberLevel = 0;
			int totalBlocks = 0;
			ItemStack tool = player.getItemInHand();
			if(tool.getType() == Material.DIAMOND_PICKAXE) {
				if(tool.hasItemMeta() && tool.getItemMeta().hasLore()) {
					fortuneLevel = Totems.getLevel(tool.getItemMeta().getLore(), "Fortune");
					experienceLevel = Totems.getLevel(tool.getItemMeta().getLore(), "Experience");
					itemabsorberLevel = Totems.getLevel(tool.getItemMeta().getLore(), "Item Absorber");
				}
			}
			HashMap<ItemStack, Integer> allItems = getBlockDrops(player, ore, fortuneLevel);
			for(Entry<ItemStack,Integer> items : allItems.entrySet()) {
				totalBlocks += items.getValue();
				int totalDrop = items.getValue();
				ItemStack item = items.getKey().clone();
				
				if(itemabsorberLevel == 0) {
					for (int i = 0; i < Math.ceil(totalDrop/64.0); i++) {
						if(totalDrop <= 64) {
							item.setAmount(totalDrop);
							world.dropItem(loc.clone().add((ran.nextInt(0,1) == 0 ? -0.5 : 0.5), 0.5, (ran.nextInt(0,1) == 0 ? -0.5 : 0.5)), item);	
						}
						else {
							totalDrop -= 64;
							item.setAmount(64);
							world.dropItem(loc.clone().add(0, 0.5, 0), item);
						}
					}
				}

				String name = prettifyMaterial(items.getKey().getType().name());
				if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
					name = "§8'"+item.getItemMeta().getDisplayName()+"§'";
				}
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
						"§f" +  name + " §a+" + items.getValue()
						));
				
			}
			
			if(totalBlocks > 0) {
				setTotalExperience(player, (int)(getTotalExperience(player)+totalBlocks*10*(1+0.25*experienceLevel)));
			}
			
			
			ore.removeFromOreStack(1);
			ore.regenBlock(oreTable.generateMaterial());
			if(itemabsorberLevel > 0) 
				return allItems;
			else {
				return null;
			}
		}
		return null;
	}
	
	private HashMap<ItemStack, Integer> getBlockDrops(Player player, OreBlock ore, int fortuneLevel) {
		HashMap<ItemStack, Integer> items = new HashMap<ItemStack, Integer>();
		
		//Custom tools reading of custom fortune
	
		
		int totalDrop = 0;
		
		switch(ore.getCurrentBlockType()) {
		case DEFAULT:
			switch(ore.getCurrentMaterial()) {
			case STONE:
			case COBBLESTONE:
			case COAL_BLOCK:
			case IRON_BLOCK:
			case GOLD_BLOCK:
			case REDSTONE_BLOCK:
			case LAPIS_BLOCK:
			case DIAMOND_BLOCK:
			case EMERALD_BLOCK:
			case SPONGE:
			{
				totalDrop = ran.nextInt(1,2+fortuneLevel);
				ItemStack stack = new ItemStack(ore.getCurrentMaterial(),1);
				items.put(stack, items.getOrDefault(items, 0)+totalDrop);
				break;	
			}
			
			//Unsmelted ores
			case IRON_ORE:
			case GOLD_ORE:
			{
				totalDrop = ran.nextInt(1,2+fortuneLevel);
				ItemStack stack = new ItemStack(ore.getCurrentMaterial(),1);
				items.put(stack, items.getOrDefault(items, 0)+totalDrop);
				break;	
			}
				
			case COAL_ORE:
			case DIAMOND_ORE:
			case EMERALD_ORE:
			{
				totalDrop = ran.nextInt(1,2+fortuneLevel);
				
				Material m;
				switch(ore.getCurrentMaterial()) {
				case COAL_ORE: m = Material.COAL; break;
				case DIAMOND_ORE: m = Material.DIAMOND; break;
				case EMERALD_ORE: m = Material.EMERALD; break;
				default: throw new IllegalArgumentException();
				
				}
				
				ItemStack stack = new ItemStack(m,1);
				items.put(stack, items.getOrDefault(items, 0)+totalDrop);
				break;	
			}
				
			case REDSTONE_ORE:
			case GLOWING_REDSTONE_ORE:
			case LAPIS_ORE:
			{
				totalDrop = ran.nextInt(2,5+fortuneLevel*2);
				Material m;
				switch(ore.getCurrentMaterial()) {
				case REDSTONE_ORE:
				case GLOWING_REDSTONE_ORE:
					m = Material.REDSTONE; 
					break;
				case LAPIS_ORE:
					m = Material.INK_SACK;
					break;
				default: throw new IllegalArgumentException();
				}
			
				ItemStack stack = new ItemStack(m,1 , (short) (m == Material.INK_SACK ? 4 : 0) );
				items.put(stack, items.getOrDefault(items, 0)+totalDrop);
				break;	
			}
			
			default:
			}
			
		default:
			
		}
		
		return items;
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
	
	public void setLootLevel(int level) {
		if(level < 1) throw new IllegalArgumentException("Totem upgrade level cannot be set to " + level + "!");
		this.lootLvl = level;
		this.oreTable = getSpawnableMaterials(this.lootLvl);
		this.updateHologramLines();
	}
	
	public void setOreStackLevel(int level) {
		if(level < 1) throw new IllegalArgumentException("Totem upgrade level cannot be set to " + level + "!");
		this.oreStackLvl = level;
		this.updateHologramLines();
	}
	
	public void setSpeedLevel(int level) {
		if(level < 1) throw new IllegalArgumentException("Totem upgrade level cannot be set to " + level + "!");
		this.speedLvl = level;
		this.updateHologramLines();
	}
	
	public void setLevelOf(UPGRADE upgrade, int level) {
		if(level < 1) throw new IllegalArgumentException("Totem upgrade level cannot be set to " + level + "!");
		switch(upgrade) {
		case LOOT: this.lootLvl = level; this.oreTable = getSpawnableMaterials(this.lootLvl); break;
		case ORE_STACK: this.oreStackLvl = level; break;
		case SPEED: this.speedLvl = level; break;
		default: throw new IllegalArgumentException("Invalid upgrade type!");
		}
		this.updateHologramLines();
	}
	
	public static int getMaxLevelOf(UPGRADE upgrade) {
		switch(upgrade) {
		case LOOT: return loot_MAXLVL;
		case ORE_STACK: return oreStack_MAXLVL;
		case SPEED: return speed_MAXLVL;
		default: return 0;
		}
	}
	
	public Boolean upgradeLootLevel(int amt) {
		if(amt < 0) throw new IllegalArgumentException("Cant upgrade totems with negative numbers");
		if(lootLvl + amt <= loot_MAXLVL) {
			this.lootLvl += amt;
			this.oreTable = getSpawnableMaterials(this.lootLvl);
			this.updateHologramLines();
			return true;
		}
		return false;
	}
	
	public Boolean upgradeOreStackLevel(int amt) {
		if(amt < 0) throw new IllegalArgumentException("Cant upgrade totems with negative numbers");
		if(oreStackLvl + amt <= oreStack_MAXLVL) {
			this.oreStackLvl += amt;
			this.updateHologramLines();
			return true;
		}
		return false;
	}
	
	public Boolean upgradeSpeedLevel(int amt) {
		if(amt < 0) throw new IllegalArgumentException("Cant upgrade totems with negative numbers");
		if(speedLvl + amt <= speed_MAXLVL) {
			this.speedLvl += amt;
			this.updateHologramLines();
			return true;
		}
		return false;
	}
	
	public int getSpeedLevel() {
		return this.speedLvl;
	}
	
	public int getOreStackLevel() {
		return this.oreStackLvl;
	}
	
	public int getLootLevel() {
		return this.lootLvl;
	}
	
	public int getLevelOf(UPGRADE type) {
		switch(type) {
		case SPEED: return this.speedLvl;
		case ORE_STACK: return this.oreStackLvl;
		case LOOT: return this.lootLvl;
		default: return 1;
		}
	}
	
	
	
	/**
	 * Tries to generate ores in the OreStacks of OreBlocks.
	 * @param immediately -> True only if you want ores to generate immediately without any regards to cooldown time/
	 * @return Returns true if successfully generated more ores, returns false if generation is still under cooldown
	 */
	public Boolean generateOres(Boolean immediately) {
		if(System.currentTimeMillis() > this.nextGenerationTime || immediately) {
			this.nextGenerationTime = System.currentTimeMillis()+getCooldown(this.lootLvl,this.speedLvl);
			for (OreBlock ore : oreBlocks) {
				if(ore.getOreStack() < getMaxOreStack(this.oreStackLvl)) {
					ore.addToOreStack(1);
					if(ore.getOreStack() == 1) {
						ore.regenBlock(oreTable.generateMaterial());
					}
				}
			}
			return true;
		}
		return false;
	}
	
	public Location getLocation() {
		if(this.loc == null) {
			this.loc = simpleLoc.getTrueLocation();
		}
		return this.loc;
	}
	
	public List<OreBlock> getOreBlocks() {
		return oreBlocks;
	}
	
	public int getMaxOreBlocks() {
		return this.maxOreBlocks;
	}
	
	public void setEditorMode(boolean bool) {
		this.editorMode = bool;
	}
	
	public Boolean getEditorMode() {
		return this.editorMode;
	}
	
	public Boolean isAssociatedOreGenBlock(Block block) {
		for (OreBlock oreB : oreBlocks) {
			if(oreB.isAtLocation(block.getLocation())) {
				return true;
			}
		}
		return false;
	}
	
	public Boolean assignOreGenBlock(Block block) {
		if(!editorMode) return false;
		if(oreBlocks.size() >= maxOreBlocks) {
			return false;
		}
		for (OreBlock oreB : oreBlocks) {
			if(oreB.isAtLocation(block.getLocation())) {
				return false;
			}
		}
		oreBlocks.add(new OreBlock(block.getLocation()));
		return true;
	}
	
	public Boolean unAssignOreGenBlock(Block block) {
		if(!editorMode) return false;
		OreBlock toRemove = null;
		for (OreBlock oreB : oreBlocks) {
			if(oreB.isAtLocation(block.getLocation())) {
				toRemove = oreB;
				break;
			}
		}
		if(toRemove != null) {
			oreBlocks.remove(toRemove);
			block.setType(Material.AIR);
			return true;
		}
		return false;
	}
	
	public Boolean unAssignOreGenBlock(OreBlock block) {
		if(oreBlocks.remove(block)) {
			block.processRemove();
			return true;	
		}
		return false;
	}
	
	public void statsFromString(String stats) {
		String split[] = stats.split(";");
		this.maxOreBlocks = Integer.parseInt(split[0]);
		this.lootLvl = Integer.parseInt(split[1]);
		this.oreStackLvl = Integer.parseInt(split[2]);
		this.speedLvl = Integer.parseInt(split[3]);
		updateHologramLines();
	}
	
	private void updateHologramLines() {
		List<String> holo = new ArrayList<String>();
		holo.add("§b§lOre §3§lTotem §8(§f§l"+this.maxOreBlocks+"x Ores§8)");
		holo.add("§7§l - §bOre Stack §7(Lvl " + this.oreStackLvl + ")");
		holo.add("§7§l - §bLoot §7(Lvl " + this.lootLvl + ")");
		holo.add("§7§l - §bSpeed §7(Lvl " + this.speedLvl + ")");
		holo.add("");
		holo.add("§7(( §7§oRight-click to manage totem §7))");
		this.hologramLines = holo;
	}
	
	public Location getHologramLocation() {
		if(this.loc == null) {
			this.loc = simpleLoc.getTrueLocation();
		}
		return this.loc.clone().add(0,4.5,0);
	}
	
	public List<String> getHologramLines() {
		return this.hologramLines;
	}
	
	private void addHologramLine(String str) {
		this.hologramLines.add(str);
	}
	
	private void removeHologrameLine(int index) {
		if(!this.hologramLines.isEmpty()) {
			if(hologramLines.size()-1 >= index) {
				hologramLines.remove(index);
			}
		}
	}
	
	private void setHologramLines(List<String> strs) {
		this.hologramLines = strs;
	}
	
	 public int getTotalExperience(int level) {
	       int xp = 0;

	       if (level >= 0 && level <= 15) {
	           xp = (int) Math.round(Math.pow(level, 2) + 6 * level);
	       } else if (level > 15 && level <= 30) {
	           xp = (int) Math.round((2.5 * Math.pow(level, 2) - 40.5 * level + 360));
	       } else if (level > 30) {
	           xp = (int) Math.round(((4.5 * Math.pow(level, 2) - 162.5 * level + 2220)));
	       }
	       return xp;
	   }

	   public  int getTotalExperience(Player player) {
	       return Math.round(player.getExp() * player.getExpToLevel()) + getTotalExperience(player.getLevel());
	   }

	   public void setTotalExperience(Player player, int amount) {
	       int level = 0;
	       int xp = 0;
	       float a = 0;
	       float b = 0;
	       float c = -amount;

	       if (amount > getTotalExperience(0) && amount <= getTotalExperience(15)) {
	           a = 1;
	           b = 6;
	       } else if (amount > getTotalExperience(15) && amount <= getTotalExperience(30)) {
	           a = 2.5f;
	           b = -40.5f;
	           c += 360;
	       } else if (amount > getTotalExperience(30)) {
	           a = 4.5f;
	           b = -162.5f;
	           c += 2220;
	       }
	       level = (int) Math.floor((-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a));
	       xp = amount - getTotalExperience(level);
	       player.setLevel(level);
	       player.setExp(0);
	       player.giveExp(xp);
	   }
}
