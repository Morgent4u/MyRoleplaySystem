package com.roleplay.position;

import com.basis.ancestor.Objekt;
import com.basis.ancestor.ObjektContext;
import com.basis.sys.Sys;
import com.basis.utils.SimpleFile;
import com.roleplay.extended.ExtendedFile;
import org.bukkit.Location;
import java.io.File;

/**
 * @Created 19.06.2022
 * @Author Nihar
 * @Description
 * This context is used to load or store a defined Position into
 * a file.
 */
public class PositionContext extends ObjektContext
{
    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    /**
     * Is used to initialize the object.
     * @param instanceName The name of the instance.
     * @param mainFolder   The main-folder which contains all Object files.
     */
    public PositionContext(String instanceName, String mainFolder)
    {
        super(instanceName, mainFolder);
    }

    @Override
    public int of_loadObjectByFile(File file)
    {
        ExtendedFile datei = new ExtendedFile(file);

        if(datei.of_fileExists())
        {
            String positionName = datei.of_getSetString("PositionName", "").replace("&", "§");
            String[] commandSet = datei.of_getStringArrayByKey("CommandSet");
            Location location = datei.of_getLocationByKey("Location");
            int range = datei.of_getSetInt("Range", 3);

            //  Validate all data...
            if(positionName.length() > 0 && location != null)
            {
                //  Create the position object and add it to the position-list.
                //  We also need to set the object-info attribute with the file-name.
                //  This is used to identify the CommandSet for the POS-System.
                //  Example: POS=test or POS=test.yml or POSITION=test or POSITION=test.yml
                Position pos = new Position(commandSet, positionName, location, range);
                pos.of_setInfo(file.getName().replace(".yml", "").toLowerCase());
                return of_addObject2ContextList(pos);
            }

            Sys.of_debug("PositionContext.of_load(File); - Error while loading following file, data is corrupted: " + file.getAbsolutePath());
            return -2;
        }

        return -1;
    }

    /* ************************************* */
    /* OBJECT-METHODS */
    /* ************************************* */

    @Override
    public int of_saveObject2File(Objekt object)
    {
        if(object != null)
        {
            Position pos = (Position) object;
            String fileName = Sys.of_getNormalizedString(pos.of_getPositionName());
            ExtendedFile datei = new ExtendedFile(new File(of_getMainFolder() + fileName + ".yml"));

            if(!datei.of_fileExists())
            {
                //  Store the position-attributes into the file.
                datei.of_set("PositionName", pos.of_getPositionName().replace("§", "&"));
                datei.of_getSetStringArray("CommandSet", pos.of_getCommandSet());
                datei.of_set("Range", pos.of_getRange());
                datei.of_setLocation("Location", pos.of_getLocation());

                //  Add the current Position to the current memory...
                pos.of_setInfo(fileName.replace(".yml", "").toLowerCase());

                if(of_addObject2ContextList(pos) == 1)
                {
                    return datei.of_save("PositionContext.of_savePosition2File();");
                }
            }
            //  If the Position already exist.
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
        if(object != null)
        {
            //  Get the position-object and file.
            Position pos = (Position) object;
            SimpleFile datei = new SimpleFile(new File(of_getMainFolder() + pos.of_getInfo() + ".yml"));

            if(datei.of_fileExists())
            {
                //  Delete the file.
                datei.of_delete();
                return of_removeObjectFromContextList(pos);
            }
            //  If the file does not exist.
            else
            {
                return -2;
            }
        }

        return -1;
    }
}
