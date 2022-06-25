package com.roleplay.iblock;

import com.basis.ancestor.Objekt;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * @Created 22.05.2022
 * @Author Nihar
 * @Description
 * An IBlock-Object is used to define for a specific block
 * an CommandSet which should be executed when the player clicks
 * on the given block.
 */
public class IBlock extends Objekt
{
    //  Attributes:
    private String[] commandSet;
    private Material material;
    private Location loc;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public IBlock(Material material, String[] commandSet, Location loc)
    {
        this.material = material;
        this.commandSet = commandSet;
        this.loc = loc;
    }

    /* ************************************* */
    /* SETTER */
    /* ************************************* */

    public void of_setMaterial(Material material)
    {
        this.material = material;
    }

    public void of_setLocation(Location loc)
    {
        this.loc = loc;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public Material of_getMaterial()
    {
        return material;
    }

    public String[] of_getCommandSet()
    {
        return commandSet;
    }

    public Location of_getLocation()
    {
        return loc;
    }
}
