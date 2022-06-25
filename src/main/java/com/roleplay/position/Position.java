package com.roleplay.position;

import com.basis.ancestor.Objekt;
import org.bukkit.Location;

/**
 * @Created 19.05.2022
 * @Author Nihar
 * @Description
 * The Position-Object is used to represent
 * locations/warp-points which can be
 * used in an CommandSet.
 *
 * This is a model.
 *
 */
public class Position extends Objekt
{
    //  Attributes:
    private String[] commandSet;
    private String positionName;
    private Location loc;
    private int range;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public Position(String[] commandSet, String positionName, Location loc, int range)
    {
        this.commandSet = commandSet;
        this.positionName = positionName;
        this.loc = loc;
        this.range = range;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public String[] of_getCommandSet()
    {
        return commandSet;
    }

    public String of_getPositionName()
    {
        return positionName;
    }

    public Location of_getLocation()
    {
        return loc;
    }

    public int of_getRange()
    {
        return range;
    }
}
