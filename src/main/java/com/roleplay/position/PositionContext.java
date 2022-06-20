package com.roleplay.position;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;
import com.basis.utils.Datei;
import com.roleplay.extended.LocationDatei;
import org.bukkit.Location;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @Created 19.06.2022
 * @Author Nihar
 * @Description
 * This context is used to load or store a defined Position into
 * a file.
 */
public class PositionContext extends Objekt
{
    //  Attributes:
    private Map<Integer, Position> positions = new HashMap<>();
    private String mainFolder;

    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    /**
     * Is used to initialize the object.
     * @param mainFolder The main-folder which contains all Position files.
     */
    public PositionContext(String mainFolder)
    {
        this.mainFolder = mainFolder;
    }

    /**
     * This function is used to load all Positions from the main-folder.
     * @return 1 = OK, -1 = ERROR
     */
    @Override
    public int of_load()
    {
        // Load the NPCs.
        File directory = new File(of_getMainFolder());
        File[] files = directory.listFiles();

        if(files != null && files.length > 0)
        {
            for(File file : files)
            {
                if(file != null)
                {
                    int rc = of_loadPositionByFile(file);

                    //  If an error occurred, stop the loading process.
                    if(rc != 1)
                    {
                        of_sendErrorMessage(null, "PositionContext.of_load();", "There was an error while loading the Position from the following file: " + file.getName());
                        return -1;
                    }
                }
            }

            return 1;
        }

        return -1;
    }

    /**
     * This function is used to load a defined Position by the given file.
     * @param file The file to load the Position from.
     * @return 1 = OK, -1 = If an error occurred.
     */
    public int of_loadPositionByFile(File file)
    {
        LocationDatei datei = new LocationDatei(file);

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
                pos.of_setObjectId( positions.size() + 1 );
                positions.put(pos.of_getObjectId(), pos);
                return 1;
            }

            Sys.of_debug("PositionContext.of_load(File); - Error while loading following file, data is corrupted: " + file.getAbsolutePath());
            return -2;
        }

        return -1;
    }

    /* ************************************* */
    /* OBJECT-METHODS */
    /* ************************************* */

    /**
     * This method is used to store the given Position into a file.
     * @param pos The Position-object which should be stored into the file.
     * @return 1 = OK, -1 = Error, -2 = The given Position (file-name) already exist.
     */
    public int of_savePosition2File(Position pos)
    {
        if(pos != null)
        {
            String fileName = Sys.of_getNormalizedString(pos.of_getPositionName());
            LocationDatei datei = new LocationDatei(new File(of_getMainFolder() + fileName + ".yml"));

            if(!datei.of_fileExists())
            {
                //  Store the position-attributes into the file.
                datei.of_set("PositionName", pos.of_getPositionName().replace("§", "&"));
                datei.of_getSetStringArray("CommandSet", pos.of_getCommandSet());
                datei.of_set("Range", pos.of_getRange());
                datei.of_setLocation("Location", pos.of_getLocation());

                //  Add the current Position to the current memory...
                pos.of_setInfo(fileName.replace(".yml", "").toLowerCase());
                pos.of_setObjectId( positions.size() + 1 );
                positions.put(pos.of_getObjectId(), pos);

                return datei.of_save("PositionContext.of_savePosition2File();");
            }
            //  If the Position already exist.
            else
            {
                return -2;
            }
        }

        return -1;
    }

    /**
     * This method is used to delete the given Position.
     * @param pos The Position-object which should be deleted from the file.
     * @return 1 = OK, -1 = Error, -2 = The given Position (file-name) does not exist.
     */
    public int of_deletePosition(Position pos)
    {
        if(pos != null)
        {
            //  Get the position-file.
            Datei datei = new Datei(new File(of_getMainFolder() + pos.of_getInfo() + ".yml"));

            if(datei.of_fileExists())
            {
                //  Delete the file.
                datei.of_delete();

                //  Remove the Position from the list.
                positions.remove(pos.of_getObjectId());
                return 1;
            }
            //  If the file does not exist.
            else
            {
                return -2;
            }
        }

        return -1;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public Position[] of_getAllPositions()
    {
        return positions.values().toArray(new Position[0]);
    }

    public Position of_getPositionByObjectId(int objectId)
    {
        return positions.get(objectId);
    }

    public String of_getMainFolder()
    {
        return mainFolder;
    }

    public int of_getLoadedPositions()
    {
        return positions.size();
    }
}
