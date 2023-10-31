package me.despawningbone.module;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.FaweQueue;

import me.despawningbone.modules.api.SimpleLocation;

public class SuperFarm implements Serializable {
	private static final long serialVersionUID = 7288076894806525177L;
	private transient DecimalFormat df = new DecimalFormat("0.0");

	final FARM_TYPE farmType;
	final FARM_TIER farmTier;
	
	Long fuelEndTime;
	
	final UUID islandOwner;
	final SimpleLocation simpleLoc; 
	transient Location loc = null;
	
	double growthRateMultiplier;
	int farmLevel;
	
	Long nextGenerationTime;
	int nextGenerationAmount;
	
	int manualHarvestCount;
	boolean autoHarvest;
	
	List<String> hologramLines;
	
	public SuperFarm(FARM_TYPE farmType, FARM_TIER farmTier, UUID islandOwner, Block block)
	{
		this.farmType = farmType;
		this.farmTier = farmTier;
		this.islandOwner = islandOwner;
		this.simpleLoc = new SimpleLocation(block.getLocation().clone().add(0,2,0));
		this.loc = simpleLoc.getTrueLocation();
		this.growthRateMultiplier = 1.0;
		this.farmLevel = 0;
		this.manualHarvestCount = 0;
		this.autoHarvest = false;
	}
	
	public Boolean isInLocation(Location loc) {
		if(this.loc == null) {
			this.loc = this.simpleLoc.getTrueLocation();
		}
		int minX = this.loc.getBlockX() - 2;
		int minZ = this.loc.getBlockZ() - 2;
		int minY = this.loc.getBlockY() - 2;
		if(minX <= loc.getBlockX() &&
			minX+5 > loc.getBlockX() &&
			minZ <= loc.getBlockZ() &&
			minZ+5 > loc.getBlockZ() &&
			minY <= loc.getBlockY() &&
			minY+3 > loc.getBlockY()) {
				return true;
		}
		return false;
	}
	
	public boolean generateCrop(boolean immediately)
	{
		if(System.currentTimeMillis() > this.nextGenerationTime || immediately) {
			this.nextGenerationTime = System.currentTimeMillis() + 1000;
			
			if(farmType == FARM_TYPE.CACTUS)
			{
				int cactus = (int) getGrowthRate();
				World world = simpleLoc.getWorld();
				FaweQueue queue = FaweAPI.createQueue(FaweAPI.getWorld(world.getName()), false);
		        queue.getRelighter().clear();
		        
		        Block freeBlock = world.getBlockAt(simpleLoc.getTrueLocation());
		        int y = simpleLoc.getBlockY()+2;
		        for(; y < 254; y++)
		        {
		        	if(freeBlock.getType() == Material.AIR)
		        		break;
		        	freeBlock = freeBlock.getRelative(0, 1, 0);
		        }
		        
		        for(int i = 0; i < cactus; ++i)
		        {
		        	queue.setBlock(simpleLoc.getBlockX(), y+i, simpleLoc.getBlockZ(), Material.CACTUS.getId(), 0);
		        }
		        queue.optimize();
		        queue.getRelighter().clear();
		        
				queue.flush();
				
			}
			return true;
		}
		return false;
	}
	
	public void setGrowthMultiplier(double multi)
	{
		if(multi < 0.01) throw new IllegalArgumentException("Super Farm Growth Multiplier value set to less than 1%.");
		this.growthRateMultiplier = multi;
	}
	
	public void setFarmLevel(int farmLevel)
	{
		if(farmLevel < 1) throw new IllegalArgumentException("Trying to set Farm level less than 1");
		this.farmLevel = farmLevel;
	}
	
	public void addFuelTime(int seconds)
	{
		if(fuelEndTime < System.currentTimeMillis())
			fuelEndTime = System.currentTimeMillis();
		fuelEndTime += seconds * 1000;
	}
	
	public double getGrowthMultiplier() { return this.growthRateMultiplier; }
	public int getFarmLevel() { return this.farmLevel; }
	public int getFuelTimeRemaining()
	{
		if(fuelEndTime < System.currentTimeMillis()) return 0;
		else
		{
			return (int) Math.round(fuelEndTime - System.currentTimeMillis() * 0.001);
		}
	}
	
	private void updateHologramLines() {
		List<String> holo = new ArrayList<String>();
		holo.add("§e§lSUPER§6§lFARM §8(§7"+prettifyMaterial(farmType.toString() + "§7§8)"));
		holo.add("§7Farm Tier: " + SuperFarm.tierDisplay(farmTier));
		holo.add("§7Growth Rate: §f" + df.format(getGrowthRate()) + " / Second");
		holo.add("§7Auto Harvest: " + (autoHarvest ? "§a§lON":"§c§lOFF"));
		
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
	
	public double getGrowthRate()
	{
		return 1.0 * farmTier.getEffectivenessMulti() * this.growthRateMultiplier;
	}
	
	public FARM_TYPE getFarmType()
	{
		return farmType;
	}
	
	public static String tierDisplay(FARM_TIER tier)
	{
		switch(tier)
		{
		case COMMON:
			return "§f§lCOMMON";
		case UNCOMMON:
			return "§a§lUNCOMMON";
		case RARE:
			return "§d§lRARE";
		case MYTHICAL:
			return "§5§lMYTHICAL";
		case LEGENDARY:
			return "§6§lLEGENDARY";
		case GODLY:
			return "§c§lGODLY";
		default:
			return "§cUndefined Tier";
		}
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
}
