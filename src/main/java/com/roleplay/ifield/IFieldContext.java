package com.roleplay.ifield;

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
 * This class-object is used to load/save/delete a IField object
 * into a file.
 */
public class IFieldContext extends Objekt
{
    //  Attributes:
    private Map<Integer, IField> iFields = new HashMap<>();
    private String mainFolder;

    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    /**
     * Is used to initialize the object.
     * @param mainFolder The main-folder which contains all iField files.
     */
    public IFieldContext(String mainFolder)
    {
        this.mainFolder = mainFolder;
    }

    /**
     * This function is used to load all iFields from the main-folder.
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
                    int rc = of_loadIFieldByFile(file);

                    //  If an error occurred, stop the loading process.
                    if(rc != 1)
                    {
                        of_sendErrorMessage(null, "IFieldContext.of_load();", "There was an error while loading the iField from the following file: " + file.getName());
                        return -1;
                    }
                }
            }

            return 1;
        }

        return -1;
    }

    /**
     * This function is used to load a iField from a file.
     * @param file The file to load the iField from.
     * @return 1 if the iField was loaded successfully, -1 if an error occurred.
     */
    public int of_loadIFieldByFile(File file)
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
                IField iField = new IField(material, commandSet, location);
                iField.of_setInfo(datei.of_getFileName().replace(".yml", ""));
                iField.of_setObjectId(iFields.size() + 1);
                iFields.put(iField.of_getObjectId(), iField);
                return 1;
            }
        }

        return -1;
    }

    /* ************************************* */
    /* OBJECT-METHODS */
    /* ************************************* */

    /**
     * This method is used to store the given iField into a file.
     * @param ifield The iField to store.
     * @return 1 = OK, -1 = Error. -2 = The given iField (file-name) already exist.
     */
    public int of_saveIField2File(IField ifield)
    {
        if(ifield != null)
        {
            LocationDatei datei = new LocationDatei(new File(of_getMainFolder() + ifield.of_getInfo() + ".yml"));

            if(!datei.of_fileExists())
            {
                datei.of_getSetStringArray("CommandSet", ifield.of_getCommandSet());
                datei.of_set("Material", ifield.of_getMaterial().toString());
                datei.of_setLocation("Location", ifield.of_getLocation());

                //  Add the current iField to the current memory...
                ifield.of_setObjectId(iFields.size() + 1);
                iFields.put(ifield.of_getObjectId(), ifield);

                return datei.of_save("IFieldContext.of_saveIField2File();");
            }
            //  If the iField already exist.
            else
            {
                return -2;
            }
        }

        return -1;
    }

    /**
     * This method is used to delete the given iField.
     * @param ifield The iField to delete.
     * @return 1 = OK, -1 = Error.
     */
    public int of_deleteIField(IField ifield)
    {
        if(ifield != null)
        {
            LocationDatei datei = new LocationDatei(new File(of_getMainFolder() + ifield.of_getInfo() + ".yml"));

            if(datei.of_fileExists())
            {
                datei.of_delete();

                //  Remove the IField from the list.
                iFields.remove(ifield.of_getObjectId());
                return 1;
            }
        }

        return -1;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public IField[] of_getAllIFields()
    {
        return iFields.values().toArray(new IField[0]);
    }

    public String of_getMainFolder()
    {
        return mainFolder;
    }

    public int of_getLoadedIFields()
    {
        return iFields.size();
    }
}
