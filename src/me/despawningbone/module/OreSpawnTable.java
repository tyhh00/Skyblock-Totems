package me.despawningbone.module;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class OreSpawnTable implements Serializable {
	
	private static final long serialVersionUID = 2425420992027697710L;
	
	private Map<Material, Double> distribution;
    private double distSum;

    public OreSpawnTable() {
        distribution = new HashMap<>();
        distSum = 0;
    }

    public void addMaterial(Material m, double distribution) {
        if (this.distribution.get(m) != null) {
            distSum -= this.distribution.get(m);
        }
        this.distribution.put(m, distribution);
        distSum += distribution;
    }
    
    public double getSumOfEntryChances() {
    	return distSum;
    }
    
    public double getChanceOf(Material m) {
    	return distribution.getOrDefault(m, 0.0)/distSum * 100;
    }
    
    public double getDistributionOf(Material m) {
    	return distribution.getOrDefault(m, 0.0);
    }
    
    public void clearTable() {
    	distribution.clear();
    	distSum = 0.0;
    }

    public Material generateMaterial() {
    	if(distSum == 0 || distribution.isEmpty()) return null;
        double rand = Math.random();
        double ratio = 1.0f / distSum;
        double tempDist = 0;
        for (Material i : distribution.keySet()) {
            tempDist += distribution.get(i);
            if (rand / ratio <= tempDist) {
                return i;
            }
        }
        return null;
    }

}