package me.despawningbone.module;

public enum FARM_TIER {
	COMMON(1.0),
	UNCOMMON(1.5),
	RARE(2.0),
	MYTHICAL(3.0),
	LEGENDARY(6.0),
	GODLY(15.0);
	
	private final double effectivenessMulti;
	private FARM_TIER(double effectivenessMulti)
	{
		this.effectivenessMulti = effectivenessMulti;
	}
	
	public double getEffectivenessMulti()
	{
		return this.effectivenessMulti;
	}
}
