package com.roleplay.extended;

import com.basis.sys.Sys;
import com.basis.utils.Datei;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;

/**
 * @Created 29.04.2022
 * @Author Nihar
 * @Description
 * This class is used to read a file which contains a location.
 *
 * This class has been created with the support of the
 * GitHub-CoPilot project.
 *
 */
public class LocationDatei extends Datei
{
    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public LocationDatei(File file)
    {
        super(file);
    }

    /* ************************************* */
    /* SETTER */
    /* ************************************* */

    /**
     * This method is used to store a location in a file.
     * @param key The ConfigKey of the location.
     * @param location The location.
     */
    public void of_setLocation(String key, Location location)
    {
        if(location != null)
        {
            cfg.set(key + ".World", location.getWorld().getName());
            cfg.set(key + ".X", location.getX());
            cfg.set(key + ".Y", location.getY());
            cfg.set(key + ".Z", location.getZ());
            cfg.set(key + ".Yaw", location.getYaw());
            cfg.set(key + ".Pitch", location.getPitch());
        }
        //  If an error occurs send a message to the console.
        else
        {
            Sys.of_sendErrorMessage(null, "LocationDatei", ".of_setLocation();", "The given location is null.");
        }
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This method is used to load a location from a file.
     * @param key The ConfigKey of the location.
     * @return The location.
     */
    public Location of_getLocationByKey(String key)
    {
        String worldName = cfg.getString(key + ".World");

        // Get the information of the world.
        double x = cfg.getDouble(key + ".X");
        double y = cfg.getDouble(key + ".Y");
        double z = cfg.getDouble(key + ".Z");
        float yaw = cfg.getInt(key + ".Yaw");
        float pitch = cfg.getInt(key + ".Pitch");

        //  Check if the given world exists...
        World world = null;

        // Check if the worldName is valid!
        if(worldName != null && !worldName.isEmpty())
        {
            try
            {
                world = Bukkit.getWorld(worldName);
            }
            catch(Exception ignored) { }
        }

        if(world != null)
        {
            // ...if it exists return the location.
            return new Location(world, x, y, z, yaw, pitch);
        }
        //  If an error occurs send a message to the console.
        else
        {
            Sys.of_sendErrorMessage(null, "LocationDatei", ".of_getLocationByKey();", "The given world does not exist.");
        }

        return null;
    }
}
