package com.roleplay.iblock;

import com.basis.ancestor.Objekt;
import com.basis.ancestor.ObjektContext;
import com.roleplay.extended.ExtendedFile;
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
public class IBlockContext extends ObjektContext
{
    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    /**
     * Is used to initialize the object.
     * @param instanceName The name of the instance.
     * @param mainFolder   The main-folder which contains all Object files.
     */
    public IBlockContext(String instanceName, String mainFolder)
    {
        super(instanceName, mainFolder);
    }

    @Override
    public int of_loadObjectByFile(File file)
    {
        ExtendedFile datei = new ExtendedFile(file);

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
                return of_addObject2ContextList(iblock);
            }
        }

        return -1;
    }

    /* ************************************* */
    /* OBJECT-METHODS */
    /* ************************************* */

    @Override
    public int of_saveObject2File(Objekt object)
    {
        IBlock iblock = (IBlock) object;

        if(iblock != null)
        {
            ExtendedFile datei = new ExtendedFile(new File(of_getMainFolder() + iblock.of_getInfo() + ".yml"));

            if(!datei.of_fileExists())
            {
                datei.of_getSetStringArray("CommandSet", iblock.of_getCommandSet());
                datei.of_set("Material", iblock.of_getMaterial().toString());
                datei.of_setLocation("Location", iblock.of_getLocation());

                //  Add the current IBlock to the current memory...
                if(of_addObject2ContextList(iblock) == 1)
                {
                    return datei.of_save("IBlockContext.of_saveIBlock2File();");
                }
            }
            //  If the IBlock already exist.
            else
            {
                return -2;
            }
        }

        return -1;
    }

    @Override
    public int of_deleteObjectFromFile(Objekt object)
    {
        IBlock iblock = (IBlock) object;

        if(iblock != null)
        {
            ExtendedFile datei = new ExtendedFile(new File(of_getMainFolder() + iblock.of_getInfo() + ".yml"));

            if(datei.of_fileExists())
            {
                //  Remove the IBlock from the list.
                datei.of_delete();
                return of_removeObjectFromContextList(iblock);
            }
        }

        return -1;
    }
}
