package com.lulan.shincolle.client.model.legacy.compat;

/**STATE getter/setter
 */
public interface IShipFlags
{

	/**
	 * minor states like level, kills, ammo...
	 */
	public int getStateMinor(int id);

	/**
	 * state flags
	 */
	public boolean getStateFlag(int flag);
	public void setStateFlag(int id, boolean flag);

	/**
	 * update flag
	 */


}
