package com.roleplay.position;

import com.basis.ancestor.Objekt;
import org.bukkit.Location;

/**
 * @Created 19.05.2022
 * @Author Nihar
 * @Description
 * An IField-Object is used to create defined areas/fields
 * in which defined CommandSets will be executed.
 * The long-name for IField is "InteractionField".
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
