package me.despawningbone.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

import me.despawningbone.modules.api.SimpleLocation;

public class OreBlock implements Serializable {
	
	private static final long serialVersionUID = -331403249831197646L;
	private static Totems delegate;
    
    public OreBlock(Totems module) {
        delegate = module;
        this.simpleLoc = null;
        this.loc = null;
        this.noOreMaterial = null;
        this.currentBlock = null;
        this.oreStack = 0;
    }
	
    enum BLOCKTYPE {
    	DEFAULT,
    	DOUBLE_BLOCK,
    	MOBCOIN
    }
    
    final SimpleLocation simpleLoc;
	transient Location loc = null;
	Material noOreMaterial;
	int oreStack;
	Material currentBlock;
	BLOCKTYPE currentType;
	
	public static List<Material> getPossibleOreTypes() {
		return new ArrayList<Material>(Arrays.asList(
				Material.STONE,
				Material.COBBLESTONE,
				
				Material.COAL_ORE,
				Material.IRON_ORE,
				Material.LAPIS_ORE,
				Material.GLOWING_REDSTONE_ORE,
				Material.REDSTONE_ORE,
				Material.GOLD_ORE,
				Material.DIAMOND_ORE,
				Material.EMERALD_ORE,
				
				Material.COAL_BLOCK,
				Material.IRON_BLOCK,
				Material.LAPIS_BLOCK,
				Material.REDSTONE_BLOCK,
				Material.GOLD_BLOCK,
				Material.DIAMOND_BLOCK,
				Material.EMERALD_BLOCK,
				
				Material.SPONGE
				));
	}
	
	public OreBlock(Location loc) {
		this.simpleLoc = new SimpleLocation(loc);
		this.loc = loc;
		this.noOreMaterial = Material.STONE;
		
		this.oreStack = 0;
		this.currentBlock = this.noOreMaterial;
		this.currentType = BLOCKTYPE.DEFAULT;
		this.updatePhysicalBlock();
	}
	
	public OreBlock(Location loc, Material noOreMaterial, int oreStack, Material currentBlock) {
		this.simpleLoc = new SimpleLocation(loc);
		this.loc = loc;
		this.noOreMaterial = noOreMaterial;
		
		this.oreStack = oreStack;
		this.currentBlock = currentBlock;
		this.currentType = BLOCKTYPE.DEFAULT;
		this.updatePhysicalBlock();
	}
	
	public OreBlock(String fromString) {
		this.simpleLoc = new SimpleLocation(loc);
		String split[] = fromString.split(":");
		this.loc = new SimpleLocation(split[0]).getTrueLocation();
		split = split[1].split(";");
		this.noOreMaterial = Material.getMaterial(split[0]);
		this.oreStack = Integer.parseInt(split[1]);
		this.currentBlock = Material.getMaterial(split[2]);
		this.currentType = BLOCKTYPE.DEFAULT;
		this.updatePhysicalBlock();
	}
	
	public String toString() {
		return (new SimpleLocation(loc).toString())+":"+this.noOreMaterial.name()+";"+this.oreStack+";"+this.currentBlock.name();
	}
	
	public void regenBlock(Material newMaterial, BLOCKTYPE type) {
		if(this.oreStack > 0) {
			currentBlock = newMaterial;
			currentType = type;
		}else {
			currentBlock = noOreMaterial;
		}
		updatePhysicalBlock();
	}
	
	public void regenBlock(Material newMaterial) {
		if(this.oreStack > 0) {
			currentBlock = newMaterial;
			currentType = BLOCKTYPE.DEFAULT;
		}else {
			currentBlock = noOreMaterial;
		}
		updatePhysicalBlock();
	}
	
	public void processRemove() {
		this.oreStack = 0;
		this.currentBlock = Material.STONE;
		updatePhysicalBlock();
	}
	
	public Boolean updatePhysicalBlock() {
		if(this.loc == null) this.loc = this.simpleLoc.getTrueLocation();
		if(this.loc.getChunk().isLoaded()) {
			Block b = loc.getBlock();
			Material m = this.currentBlock;
			b.setType(m);
			Runnable run = () -> {b.setType(m);};
			delegate.delayRunEmulator(run, 1, false, false);
			
			return true;
		}
		return false;
	}
	
	public Boolean isAtLocation(Location loc) {
		if(this.loc == null) this.loc = this.simpleLoc.getTrueLocation();
		if(this.loc.equals(loc))
			return true;
		return false;
	}
	
	public Location getLocation() {
		if(this.loc == null) this.loc = this.simpleLoc.getTrueLocation();
		return this.loc;
	}
	
	public int getOreStack() {
		return this.oreStack;
	}
	
	public void setOreStack(int newstack) {
		if(newstack < 0) throw new IllegalArgumentException("Ore Block's stack cannot be set to < 0!");
		this.oreStack = newstack;
	}
	
	public void addToOreStack(int amt) {
		this.oreStack += amt;
	}
	
	public void removeFromOreStack(int amt) {
		this.oreStack -= amt;
		if(this.oreStack < 0) this.oreStack= 0;
	}
	
	public BLOCKTYPE getCurrentBlockType() {
		return this.currentType;
	}
	
	public Material getCurrentMaterial() {
		return this.currentBlock;
	}
	
	public Material getNoOreBlockType() {
		return this.noOreMaterial;
	}
}
