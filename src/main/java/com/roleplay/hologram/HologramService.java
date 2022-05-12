package com.roleplay.hologram;

import com.basis.ancestor.Objekt;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import java.util.Objects;

/**
 * @Created 07.05.2022
 * @Author Nihar
 * @Description
 * This class contains useful methods or functions
 * in reference to the object: Hologram
 *
 */
public class HologramService extends Objekt
{
    //  Attributes:
    public HologramContext _CONTEXT;

    /* ************************************* */
    /* LOADER // UNLOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        //  initialize the hologram context which is used to load
        //  all defined holograms from a file.
        _CONTEXT = new HologramContext();

        return _CONTEXT.of_load();
    }

    @Override
    public void of_unload()
    {
        if(_CONTEXT != null)
        {
            _CONTEXT.of_unload();
        }
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This function edits the hologram which has been defined for the given
     * index-number.
     * @param holo The hologram.
     * @param indexId The index-number of the hologram.
     * @param text The text of the hologram.
     * @return 1 if the hologram was edited successfully, -1 if the index-number is invalid.
     */
    public int of_editHologramLine(Hologram holo, int indexId, String text)
    {
        // Correct the given indexId (is given from the user).
        indexId--;

        if(indexId < holo.of_getArmorStandSize())
        {
            try
            {
                ArmorStand am = holo.of_getArmorStandByIndex(indexId);

                if(am != null)
                {
                    am.setCustomName(text.replace("&", "§"));
                    return 1;
                }
            }
            catch (Exception ignored) { }
        }

        return -1;
    }

    /**
     * This function corrects the alignment of the holograms.
     * @param holo The hologram.
     * @param lb_normalize If true, the holograms will be aligned to the spawn location.
     */
    private void of_normalizeHologramArmorStands(Hologram holo, boolean lb_normalize)
    {
        int armorSize = holo.of_getArmorStandSize();

        if(armorSize > 0)
        {
            //  Get the first element in the list.
            Location loc = holo.of_getArmorStandByIndex(0).getLocation();

            for(int i = 0; i < armorSize; i++)
            {
                if(i != 0)
                {
                    loc.setY(loc.getY() - holo.of_getHeight());
                }
                //  When it's the first line check for normalization.
                else if(lb_normalize)
                {
                    loc.setY(holo.of_getSpawnLocation().getY());
                }

                holo.of_getArmorStandByIndex(i).teleport(loc);
            }
        }
    }

    /* ************************************* */
    /* ADDER // SETTER // REMOVER */
    /* ************************************* */

    /**
     * This function adds a new hologram to the world.
     * Or adds to an existing hologram a new line.
     * @param holo The hologram.
     * @param title The title of the hologram.
     * @return The given hologram.
     */
    public Hologram of_addLine2Hologram(Hologram holo, String title)
    {
        // Get the last location of the hologram.
        Location loc = holo.of_getLastArmorStandLocation();
        title = title.replace("&", "§");

        //  If the last location is null, set the location to the spawn location.
        if(loc == null)
        {
            loc = holo.of_getSpawnLocation();
        }

        //  Create the ArmorStand.
        ArmorStand armorStand = Objects.requireNonNull(loc.getWorld()).spawn(loc, ArmorStand.class);
        armorStand.setCustomName(title);
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setSmall(true);
        armorStand.setBasePlate(false);
        // armorStand.setHeadPose(armorStand.getHeadPose().setY(holo.of_getHeight()));

        //  Add the current ArmorStand to the list.
        holo.of_addArmorStand(armorStand);
        of_normalizeHologramArmorStands(holo, false);

        return holo;
    }

    /**
     * This function removes a hologram from the world by using the defined index-number.
     * @param holo The hologram.
     * @param indexId The index-number of the hologram.
     * @return The hologram if the index-number is valid, null if the index-number is invalid.
     */
    public Hologram of_removeLineFromHologram(Hologram holo, int indexId)
    {
        //  We subtract 1 from the indexId because the input of the player is 1-based.
        indexId--;

        if(indexId < holo.of_getArmorStandSize())
        {
            // If only one armorStand is left, we delete the whole hologram.
            if(holo.of_getArmorStandSize() == 1)
            {
                int rc = _CONTEXT.of_deleteHologram(holo);

                if(rc == 1)
                {
                    return holo;
                }
                //  An error occurred.
                else
                {
                    holo.of_sendErrorMessage(null, "HologramService.of_removeLineFromHologram();", "An error occurred while deleting the hologram. This error can be ignored!");
                }
            }

            holo.of_removeArmorStand(indexId);

            //  Normalize the armorStands.
            of_normalizeHologramArmorStands(holo, indexId == 0);

            return holo;
        }

        return null;
    }
}
