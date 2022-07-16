package com.basis.ancestor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @Created 09.07.2022
 * @Author Nihar
 * @Description
 * This is the ancestor-class for every ObjektContext.
 * It's used to load or store a defined Object into a file.
 */
public abstract class ObjektContext extends Objekt
{
    //  Attributes
    private Map<Integer, Objekt> objects = new HashMap<>();
    private String mainFolder;

    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    /**
     * Is used to initialize the object.
     * @param instanceName The name of the instance.
     * @param mainFolder The main-folder which contains all Object files.
     */
    public ObjektContext(String instanceName, String mainFolder)
    {
        this.mainFolder = mainFolder;
        of_setInfo(instanceName);
    }

    /**
     * This function is used to load all Objects from the main-folder.
     * @return 1 = OK, 0 = No files to load, -1 = ERROR
     */
    @Override
    public int of_load()
    {
        //  Load the files.
        File directory = new File(of_getMainFolder());
        File[] files = directory.listFiles();

        //  Validate the current files.
        if(files != null && files.length > 0)
        {
            for(File file : files)
            {
                if(file != null)
                {
                    int rc = of_loadObjectByFile(file);

                    //  If an error occurred, stop the loading process.
                    if(rc != 1)
                    {
                        of_sendErrorMessage(null, of_getInfo() + ".of_load();", "There was an error while loading the Object from the following file: " + file.getName());
                        return -1;
                    }
                }
            }

            return 1;
        }

        return 0;
    }

    /**
     * This function is used to load a defined Object from a file.
     * @param file The file which contains the structure of the Object.
     * @return 1 = OK, -1 = ERROR
     */
    public int of_loadObjectByFile(File file)
    {
        of_sendErrorMessage(null, of_getInfo() + ".of_loadObjectByFile();", "This function needs to be override from the child-class.");
        return -1;
    }

    /**
     * This function is used to unload all loaded objects.
     * It only calls the of_unload() function of each object.
     */
    @Override
    public void of_unload()
    {
        for(Objekt objects : of_getAllObjects())
        {
            objects.of_unload();
        }
    }

    /* ************************************* */
    /* OBJECT-METHODS */
    /* ************************************* */

    /**
     * This function is used to store a defined Object into a file.
     * @param object The Object to store.
     * @return 1 = OK, -1 = ERROR
     */
    public int of_saveObject2File(Objekt object)
    {
        of_sendErrorMessage(null, of_getInfo() + ".of_saveObject2File();", "This function needs to be override from the child-class.");
        return -1;
    }

    /**
     * This function is used to delete the file of the given object.
     * @param object The Object to delete the file from .
     * @return 1 = OK, -1 = ERROR
     */
    public int of_deleteObjectFromFile(Objekt object)
    {
        of_sendErrorMessage(null, of_getInfo() + ".of_deleteObjectFromFile();", "This function needs to be override from the child-class.");
        return -1;
    }

    /* ************************************* */
    /* SETTER- // ADDER- // REMOVER-METHODS */
    /* ************************************* */

    /**
     * This function is used to add a defined Object to the internal list.
     * @param object The Object to add.
     * @return 1 = OK, -1 = ERROR
     */
    protected int of_addObject2ContextList(Objekt object)
    {
        if(object != null)
        {
            object.of_setObjectId(of_getLoadedObjects() + 1);
            objects.put(object.of_getObjectId(), object);
            return 1;
        }

        return -1;
    }

    /**
     * This function is used to remove the given Object from the internal list.
     * @param object The Object to remove.
     * @return 1 = OK, -1 = ERROR
     */
    protected int of_removeObjectFromContextList(Objekt object)
    {
        if(object != null)
        {
            objects.remove(object.of_getObjectId());
            return 1;
        }

        return -1;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public Objekt[] of_getAllObjects()
    {
        return objects.values().toArray(new Objekt[0]);
    }

    public Objekt of_getObjectById(int objectId)
    {
        return objects.get(objectId);
    }

    public String of_getMainFolder()
    {
        return mainFolder;
    }

    public int of_getLoadedObjects()
    {
        return objects.size();
    }
}
