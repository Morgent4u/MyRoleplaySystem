package com.roleplay.ifield;

import com.basis.ancestor.Objekt;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * @Created 22.05.2022
 * @Author Nihar
 * @Description
 * An IField-Object is used to create defined areas/fields
 * in which defined CommandSets will be executed.
 * The long-name for IField is "InteractionField".
 */
public class IField extends Objekt
{
    //  Attributes:
    private String[] commandSet;
    private Material material;
    private Location loc;
    private double range;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public IField(Material material, String[] commandSet, Location loc, double range)
    {
        this.material = material;
        this.commandSet = commandSet;
        this.loc = loc;
        this.range = range;
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

    public double of_getRange()
    {
        return range;
    }
}
