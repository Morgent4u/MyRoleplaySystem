package com.roleplay.plot;

import com.basis.ancestor.Objekt;
import com.roleplay.manager.LabelManager;
import org.bukkit.Location;

/**
 * @Created 24.07.2022
 * @Author Nihar
 * @Description
 * This java-class is used to represent
 * a mrs-plot object. This object will be
 * used when a player wants to add a new
 * plot to the mrs system.
 */
public class Plot extends Objekt
{
    //  Attributes:
    Location loc;
    String worldGuardRegion;
    double price = -1;
    int labelEnum;

    /* ************************************* */
    /* VALIDATION */
    /* ************************************* */

    /**
     * We need to validate every value because
     * the PlotContext does not make it!
     * @return ErrorMessage otherwise NULL => OK.
     */
    @Override
    public String of_validate()
    {
        if(loc == null)
        {
            return "You need to define a Spawn-Point (Location)!";
        }

        if(worldGuardRegion == null)
        {
            return "You need to define a world-guard region!";
        }

        if(price == -1)
        {
            return "You need to define a price!";
        }

        if(!LabelManager.of_getInstance().of_check4LabelEnumExist(of_getLabelEnum()))
        {
            return "The given Label-Category does not exist or is not valid!";
        }

        //  All seems OK.
        return null;
    }

    /* ************************************* */
    /* SETTER // ADDER // REMOVER */
    /* ************************************* */

    public void of_setLocation(Location loc)
    {
        this.loc = loc;
    }

    public void of_setWorldGuardRegion(String worldGuardRegion)
    {
        this.worldGuardRegion = worldGuardRegion;
    }

    public void of_setPrice(double price)
    {
        this.price = price;
    }

    public void of_setLabelEnum(int labelEnum)
    {
        this.labelEnum = labelEnum;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public Location of_getLocation()
    {
        return loc;
    }

    public String of_getWorldGuardRegion()
    {
        return worldGuardRegion;
    }

    public double of_getPrice()
    {
        return price;
    }

    public int of_getLabelEnum()
    {
        return labelEnum;
    }
}
