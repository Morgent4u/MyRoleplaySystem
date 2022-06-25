package com.roleplay.iblock;

import com.basis.ancestor.Objekt;
import com.roleplay.extended.LocationDatei;
import org.bukkit.Location;
import org.bukkit.Material;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @Created 22.05.2022
 * @Author Nihar
 * @Description
 * This class-object is used to load/save/delete an IBlock object
 * into a file.
 */
public class IBlockContext extends Objekt
{
    //  Attributes:
    private Map<Integer, IBlock> iblocks = new HashMap<>();
    private String mainFolder;

    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    /**
     * Is used to initialize the object.
     * @param mainFolder The main-folder which contains all IBlock files.
     */
    public IBlockContext(String mainFolder)
    {
        this.mainFolder = mainFolder;
    }

    /**
     * This function is used to load all IBlocks from the main-folder.
     * @return 1 = OK, -1 = ERROR
     */
    @Override
    public int of_load()
    {
        // Load the IBlocks.
        File directory = new File(of_getMainFolder());
        File[] files = directory.listFiles();

        if(files != null && files.length > 0)
        {
            for(File file : files)
            {
                if(file != null)
                {
                    int rc = of_loadIBlockByFile(file);

                    //  If an error occurred, stop the loading process.
                    if(rc != 1)
                    {
                        of_sendErrorMessage(null, "IBlockContext.of_load();", "There was an error while loading the IBlock from the following file: " + file.getName());
                        return -1;
                    }
                }
            }

            return 1;
        }

        return -1;
    }

    /**
     * This function is used to load an IBlock from a file.
     * @param file The file to load the IBlock from.
     * @return 1 if the IBlock was loaded successfully, -1 if an error occurred.
     */
    public int of_loadIBlockByFile(File file)
    {
        LocationDatei datei = new LocationDatei(file);

        if(datei.of_fileExists())
        {
            Material material = Material.getMaterial(datei.of_getSetString("Material", "STONE").toUpperCase());
            String[] commandSet = datei.of_getStringArrayByKey("CommandSet");
            Location location = datei.of_getLocationByKey("Location");

            //  Validate all data.
            if(material != null && commandSet != null && location != null)
            {
                IBlock iblock = new IBlock(material, commandSet, location);
                iblock.of_setInfo(datei.of_getFileName().replace(".yml", ""));
                iblock.of_setObjectId(iblocks.size() + 1);
                iblocks.put(iblock.of_getObjectId(), iblock);
                return 1;
            }
        }

        return -1;
    }

    /* ************************************* */
    /* OBJECT-METHODS */
    /* ************************************* */

    /**
     * This method is used to store the given IBlock into a file.
     * @param iblock The IBlock to store.
     * @return 1 = OK, -1 = Error. -2 = The given IBlock (file-name) already exist.
     */
    public int of_saveIBlock2File(IBlock iblock)
    {
        if(iblock != null)
        {
            LocationDatei datei = new LocationDatei(new File(of_getMainFolder() + iblock.of_getInfo() + ".yml"));

            if(!datei.of_fileExists())
            {
                datei.of_getSetStringArray("CommandSet", iblock.of_getCommandSet());
                datei.of_set("Material", iblock.of_getMaterial().toString());
                datei.of_setLocation("Location", iblock.of_getLocation());

                //  Add the current IBlock to the current memory...
                iblock.of_setObjectId(iblocks.size() + 1);
                iblocks.put(iblock.of_getObjectId(), iblock);

                return datei.of_save("IBlockContext.of_saveIBlock2File();");
            }
            //  If the IBlock already exist.
            else
            {
                return -2;
            }
        }

        return -1;
    }

    /**
     * This method is used to delete the given IBlock.
     * @param iblock The IBlock to delete.
     * @return 1 = OK, -1 = Error.
     */
    public int of_deleteIBlock(IBlock iblock)
    {
        if(iblock != null)
        {
            LocationDatei datei = new LocationDatei(new File(of_getMainFolder() + iblock.of_getInfo() + ".yml"));

            if(datei.of_fileExists())
            {
                datei.of_delete();

                //  Remove the IBlock from the list.
                iblocks.remove(iblock.of_getObjectId());
                return 1;
            }
        }

        return -1;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public IBlock[] of_getAllIBlocks()
    {
        return iblocks.values().toArray(new IBlock[0]);
    }

    public String of_getMainFolder()
    {
        return mainFolder;
    }

    public int of_getLoadedIBlocks()
    {
        return iblocks.size();
    }
}
