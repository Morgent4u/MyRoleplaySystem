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

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public IField(Material material, String[] commandSet, Location loc)
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
