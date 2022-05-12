package com.roleplay.hologram;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import java.util.ArrayList;

/**
 * @Created 01.05.2022
 * @Author Nihar
 * @Description
 * This object is used to create a hologram.
 * A hologram is a visible object that can be placed in the world.
 */
public class Hologram extends Objekt
{
    //  Attributes:
    ArrayList<ArmorStand> armorStands = new ArrayList<>();
    Location spawnLoc;

    //  FilePath is needed to load the Hologram in the NPCContext (while saving process).
    String filePath;

    double height = 0.26;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public Hologram(Location spawnLoc, double height)
    {
        this.spawnLoc = spawnLoc;
        this.height = height;
    }

    /* ************************************* */
    /* UNLOADER */
    /* ************************************* */

    @Override
    public void of_unload()
    {
        //  Iterate all ArmorsStands and remove them from the world.
        for(ArmorStand armorStand : armorStands)
        {
            armorStand.remove();
        }
    }

    /* ************************************* */
    /* SETTER // ADDER // REMOVER */
    /* ************************************* */

    /**
     * This function adds an armorStand to the hologram.
     * @param armorStand The armorStand to add.
     */
    public void of_addArmorStand(ArmorStand armorStand)
    {
        armorStands.add(armorStand);
    }

    /**
     * This function removes an ArmorStand from the list which
     * has been defined in the given indexId.
     * @param indexId List index of the armor-stand which should be removed.
     */
    public void of_removeArmorStand(int indexId)
    {
        armorStands.get(indexId).remove();
        armorStands.remove(indexId);
    }

    public void of_setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This function returns the titles of the holograms.
     * @return The titles of the holograms.
     */
    public ArrayList<String> of_getHologramTitles()
    {
        ArrayList<String> titles = new ArrayList<>();

        for(ArmorStand armorStand : armorStands)
        {
            titles.add(armorStand.getCustomName());
        }

        return titles;
    }

    /**
     * This function returns the last location of the hologram.
     * @return The last location of the hologram.
     */
    public Location of_getLastArmorStandLocation()
    {
        if(armorStands.size() > 0)
        {
            return armorStands.get(armorStands.size() - 1).getLocation();
        }

        return null;
    }

    public ArmorStand of_getArmorStandByIndex(int index)
    {
        return armorStands.get(index);
    }

    /**
     * This function returns the spawn location of the hologram.
     * @return The spawn location of the hologram.
     */
    public Location of_getSpawnLocation()
    {
        return spawnLoc;
    }

    public double of_getHeight()
    {
        return height;
    }

    public int of_getArmorStandSize()
    {
        return armorStands.size();
    }

    public String of_getFilePath()
    {
        return filePath;
    }
}
