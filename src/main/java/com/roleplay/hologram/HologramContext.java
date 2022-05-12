package com.roleplay.hologram;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.extended.LocationDatei;
import org.bukkit.Location;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Created 07.05.2022
 * @Author Nihar
 * @Description
 * This class is used to load holograms from a file or
 * save it into the file.
 */
public class HologramContext extends Objekt
{
    //  Attributes:
    private Map<Integer, Hologram> holograms = new HashMap<>();

    /* ************************************* */
    /* LOADER // UNLOADER */
    /* ************************************* */

    /**
     * Get all hologram files in the holograms folder and load each.
     * @return 1 = OK, -1 Error
     */
    @Override
    public int of_load()
    {
        //  Load holograms from files.
        File directory = new File(Sys.of_getMainFilePath() + "//Holograms//");
        File[] files = directory.listFiles();

        if(files != null && files.length > 0)
        {
            for(File file : files)
            {
                if(file != null)
                {
                    int rc = of_loadHologramFromFile(new LocationDatei(file));

                    //  If an error occurred, stop the loading process.
                    if(rc != 1)
                    {
                        of_sendErrorMessage(null, "HologramContext.of_load();", "There was an error while loading the hologram from the following file: " + file.getName());
                        return -1;
                    }
                }
            }

            return 1;
        }

        return -1;
    }

    /**
     * This function is used to load a hologram from a file.
     * @param datei The file to load the hologram from.
     * @return 1 if successful, -1 if an error occured.
     */
    public int of_loadHologramFromFile(LocationDatei datei)
    {
        //  File exists, load it.
        if(datei.of_fileExists())
        {
            //  Get file attributes
            String[] holoLines = datei.of_getStringArrayByKey("Hologram.Titles");
            Location loc = datei.of_getLocationByKey("Hologram.Location");
            double height = datei.of_getDoubleByKey("Hologram.Height");

            if(loc != null)
            {
                //  Create the hologram and add it to the list.
                Hologram hologram = new Hologram(loc, height);

                if(holoLines != null && holoLines.length > 0)
                {
                    holoLines = Sys.of_getReplacedArrayString(holoLines, "&", "§");

                    //  Iterate all lines and add them to the hologram. The add function also creates the hologram.
                    for(String line : holoLines)
                    {
                        hologram = main.HOLOGRAMSERVICE.of_addLine2Hologram(hologram, line);
                    }

                    //  Store the filepath, this is used to delete the hologram file.
                    hologram.of_setFilePath(datei.of_getFile().getAbsolutePath());
                    of_addHologram2Context(hologram);
                    return 1;
                }
                else
                {
                    of_sendErrorMessage(null, "HologramContext.of_loadHologramFromFile();", "The hologram file " + datei.of_getFile().getName() + " does not contain any hologram titles.");
                }
            }
            else
            {
                of_sendErrorMessage(null, "HologramContext.of_loadHologramFromFile();", "The hologram file " + datei.of_getFile().getName() + " does not contain a location.");
            }
        }

        return -1;
    }

    /**
     * This function is used to unload all loaded holograms.
     * It only calls the of_unload() function of the hologram-object.
     */
    @Override
    public void of_unload()
    {
        for(Hologram hologram : holograms.values())
        {
            hologram.of_unload();
        }
    }

    /* ************************************* */
    /* OBJECT-METHODS */
    /* ************************************* */

    /**
     * This function is used to save a hologram to a file.
     * @param fileName The hologram title.
     * @param hologram The hologram to save.
     * @return 1 if successful, -1 if an error occurred, -2 if file already exists.
     */
    public int of_saveHologram2File(String fileName, Hologram hologram)
    {
        //  Normalize filename.
        if(!fileName.endsWith(".yml"))
        {
            fileName = fileName + ".yml";
        }
        fileName = Sys.of_getNormalizedString(fileName);
        fileName = fileName.toLowerCase();

        //  Create the file...
        LocationDatei datei = new LocationDatei(new File(Sys.of_getMainFilePath() + "//Holograms//" + fileName));

        if (!datei.of_fileExists())
        {
            //  When the file does not exist, create it.
            ArrayList<String> lines = hologram.of_getHologramTitles();

            if(lines != null && lines.size() > 0)
            {
                // Replace '§' with '&'
                lines = Sys.of_getReplacedArrayList(lines, "§", "&");

                //  Write into the file.
                datei.of_set("Hologram.Height", hologram.of_getHeight());
                datei.of_getSetStringArrayList("Hologram.Titles", lines);
                datei.of_setLocation("Hologram.Location", hologram.of_getSpawnLocation());

                //  Store the file-path, this is used to load the hologram from file in the npc-context!
                hologram.of_setFilePath(datei.of_getFile().getAbsolutePath());

                // Save the file.
                return datei.of_save("HologramContext.of_saveHologram2File();");
            }
            else
            {
                of_sendErrorMessage(null, "HologramContext.of_saveHologram2File();", "No hologram titles found.");
            }

            return -1;
        }

        return -2;
    }

    /**
     * This function is used to update the hologram-file with the new hologram-titles.
     * @param holo The hologram to update.
     * @return 1 if successful, -1 if an error occurred., -2 If the file does not exist.
     */
    public int of_updateHologram2File(Hologram holo)
    {
        //  Get the hologram-file.
        LocationDatei datei = new LocationDatei(new File(holo.of_getFilePath()));

        if(datei.of_fileExists())
        {
            //  Get the hologram-titles.
            ArrayList<String> lines = holo.of_getHologramTitles();
            lines = Sys.of_getReplacedArrayList(lines, "§", "&");
            datei.of_set("Hologram.Titles", lines);

            return datei.of_save("HologramContext.of_updateHologram2File();");
        }

        //  Only existing holograms can be updated.
        return -2;
    }

    /**
     * This function is used to delete a hologram file.
     * @param holo The hologram to delete.
     * @return 1 if successful, -1 if an error occurred.
     */
    public int of_deleteHologram(Hologram holo)
    {
        // Call the unload function of the hologram object to delete all armor-stand entities.
        holo.of_unload();

        //  Now we can delete the file.
        File file = new File(holo.of_getFilePath());

        if(file.length() > 0)
        {
            if(file.delete())
            {
                holograms.remove(holo.of_getObjectId());
                return 1;
            }
        }

        return -1;
    }

    /* ************************************* */
    /* DEBUG-CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("Loaded holograms: " + of_getHologramSize());
    }

    /* ************************************* */
    /* SETTER // ADDER // REMOVER */
    /* ************************************* */

    /**
     * This function adds the given hologram to the context-list.
     * @param hologram The hologram which should be added to the list.
     */
    public void of_addHologram2Context(Hologram hologram)
    {
        hologram.of_setObjectId(holograms.size() + 1);
        holograms.put(hologram.of_getObjectId(), hologram);
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public int of_getHologramSize()
    {
        return holograms.size();
    }

    public Hologram of_getHologramById(int id)
    {
        return holograms.get(id);
    }

    public Hologram[] of_getAllHolograms()
    {
        return holograms.values().toArray(new Hologram[0]);
    }

}
