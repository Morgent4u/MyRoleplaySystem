package com.basis.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.basis.sys.Sys;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @Created 11.10.2021
 * @Author Nihar
 * @Description
 * This object is used to create or edit
 * fast '.YML'-Files.
 */
public class SimpleFile
{
    private File file;
    public YamlConfiguration cfg;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    /**
     * Constructor
     */
    public SimpleFile() { }

    /**
     * Constructor
     * @param absolutePath Absolute file path for example: 'plugins\\Plugin\\others\\settings.yml'
     */
    public SimpleFile(String absolutePath)
    {
        //	Korrektur, falls beim Absoluten FilePath etwas falsch angegeben wird!
        absolutePath = absolutePath.replace("\\", "//");

        //	Wenn die SimpleFile nicht auf .yml endet, wird dies hinzugef�gt.
        if(!absolutePath.contains(".yml"))
        {
            absolutePath += ".yml";
        }

        this.file = new File(absolutePath);
        this.cfg = new YamlConfiguration();

        //  Initialize the configuration.
        of_initializeData("constructor(File);");
    }

    /**
     * Constructor
     * @param file File object.
     */
    public SimpleFile(@NotNull File file)
    {
        this.file = file;
        this.cfg = new YamlConfiguration();

        //  Initialize the configuration.
        of_initializeData("constructor(File);");
    }

    private void of_initializeData(String invokerName)
    {
        try
        {
            this.cfg.load(file);
            return;
        }
        catch (Exception ignored) { }

        if(of_fileExists())
        {
            //  Create the backup file...
            if(of_createBackupFromCurrentFile() == 1)
            {
                Sys.of_sendErrorMessage(null, "SimpleFile", invokerName, "Error while reading the following file: " + file.getAbsolutePath() + " - Backup file has been created. :)! We try to fix the current file...");
            }
            else
            {
                Sys.of_sendErrorMessage(null, "SimpleFile", invokerName, "Error while creating the backup file for the following file: " + file.getAbsolutePath() + " - Backup file could not be created. :(");
            }
        }
    }

    /* ************************************* */
    /* UNLOADER */
    /* ************************************* */

    /**
     * This function unloads this object.
     * If the file is empty the file will be destroyed.
     */
    public void of_unload()
    {
        if(file != null)
        {
            //  Check the length of the file.
            //  Delete the file if it's empty we don't want to save empty files.
            if(file.length() == 0)
            {
                file.delete();
                Sys.of_debug("File has been deleted because it was empty. File: " + file.getAbsolutePath());
            }
        }
    }

    /* ************************************* */
    /* GET-SET-Method */
    /* ************************************* */

    /**
     * This function checks if the current configKey is already set.
     * If the configKey is not set the defaultValue will be set and also returned.
     * If the configKey is already set, the value will be returned.
     * @param configKey Section in the .YML-File.
     * @param defaultValue Default value which will be used as initialize value for the given section.
     * @return The value in the configKey or the defaultValue if the configKey is not given.
     */
    public String of_getSetString(String configKey, String defaultValue)
    {
        String tmpValue = null;

        if(cfg.isSet(configKey))
        {
            tmpValue = cfg.getString(configKey);

            if(tmpValue == null)
            {
                tmpValue = defaultValue;
            }
        }
        else
        {
            cfg.set(configKey, defaultValue);
            tmpValue = defaultValue;
        }

        return tmpValue;
    }

    /**
     * This function checks if the current configKey is already set.
     * If the configKey is not set the defaultValue will be set and also returned.
     * If the configKey is already set, the value will be returned.
     * @param configKey Section in the .YML-File.
     * @param defaultValue Default value which will be used as initialize value for the given section.
     * @return The value in the configKey or the defaultValue if the configKey is not given.
     */
    public int of_getSetInt(String configKey, int defaultValue)
    {
        int tmpValue = -1;

        if(cfg.isSet(configKey))
        {
            tmpValue = Sys.of_getString2Int(cfg.getString(configKey));

            if(tmpValue == -1)
            {
                tmpValue = defaultValue;
            }
        }
        else
        {
            cfg.set(configKey, defaultValue);
            tmpValue = defaultValue;
        }

        return tmpValue;
    }

    /**
     * This function checks if the current configKey is already set.
     * If the configKey is not set the defaultValue will be set and also returned.
     * If the configKey is already set, the value will be returned.
     * @param configKey Section in the .YML-File.
     * @param defaultValue Default value which will be used as initialize value for the given section.
     * @return The value in the configKey or the defaultValue if the configKey is not given.
     */
    public double of_getSetDouble(String configKey, double defaultValue)
    {
        double tmpValue = -1;

        if(cfg.isSet(configKey))
        {
            try
            {
                tmpValue = cfg.getDouble(configKey);
            }
            catch (Exception ignored) { }

            if(tmpValue == -1)
            {
                tmpValue = defaultValue;
            }
        }
        else
        {
            cfg.set(configKey, defaultValue);
            tmpValue = defaultValue;
        }

        return tmpValue;
    }

    /**
     * This function checks if the current configKey is already set.
     * If the configKey is not set the defaultValue will be set and also returned.
     * If the configKey is already set, the value will be returned.
     * @param configKey Section in the .YML-File.
     * @param defaultBool Default value which will be used as initialize value for the given section.
     * @return The value in the configKey or the defaultValue if the configKey is not given.
     */
    public boolean of_getSetBoolean(String configKey, boolean defaultBool)
    {
        boolean tmpValue = false;

        if(cfg.isSet(configKey))
        {
            tmpValue = cfg.getBoolean(configKey);
        }
        else
        {
            cfg.set(configKey, defaultBool);
            tmpValue = defaultBool;
        }

        return tmpValue;
    }

    /**
     * This function checks if the current configKey is already set.
     * If the configKey is not set the defaultValue will be set and also returned.
     * If the the configKey is already set, the value will be returned.
     * @param configKey Section in the .YML-File.
     * @param arrayList Default value which will be used as initialize value for the given section.
     * @return The value in the configKey or the defaultValue if the configKey is not given.
     */
    public String[] of_getSetStringArrayList(String configKey, ArrayList<String> arrayList)
    {
        String[] tmp = null;

        //  Does the path already exist?
        if(cfg.isSet(configKey))
        {
            //  Load existing one...
            tmp = of_getStringArrayByKey(configKey);
        }
        //  Create new...
        else
        {
            cfg.set(configKey, arrayList);
        }

        //  Error while getting the value? We ignore this...
        if(tmp == null)
        {
            //  We send back the default....
            return arrayList.toArray(new String[0]);
        }

        return tmp;
    }

    /**
     * This function checks if the current configKey is already set.
     * If the configKey is not set the defaultValue will be set and also returned.
     * If the the configKey is already set, the value will be returned.
     * @param configKey Section in the .YML-File.
     * @param array Default value which will be used as initialize value for the given section.
     * @return The value in the configKey or the defaultValue if the configKey is not given.
     */
    public String[] of_getSetStringArray(String configKey, String[] array)
    {
        //	�berladung von of_getSetStringArray...
        ArrayList<String> tmpList = new ArrayList<>();

        if(array != null && array.length > 0)
        {
            //	String-Array zu einer ArrayList konvertieren.
            Collections.addAll(tmpList, array);

            return of_getSetStringArrayList(configKey, tmpList);
        }

        return null;
    }

    /* ************************************* */
    /* OBJECT-METHODS */
    /* ************************************* */

    /**
     * This function deletes a whole directory or specific file.
     * @param directory Directory or file.
     */
    public void of_deleteRecursive(File directory)
    {
        String[] fileNames = directory.list();

        if(fileNames != null)
        {
            for(String fileName : fileNames)
            {
                of_deleteRecursive(new File(directory.getPath(), fileName));
            }
        }

        directory.delete();
    }

    /**
     * This function deletes the file of this object.
     */
    public void of_delete()
    {
        if(file != null)
        {
            file.delete();
        }
    }

    /* ************************************* */
    /* SAVE-STUFF */
    /* ************************************* */

    /**
     * This function is used to save the current file.
     * @param invoker Invoker name or system area which calls this function.
     * @return 1 if the file was saved successfully, otherwise -1.
     */
    public int of_save(String invoker)
    {
        try
        {
            cfg.save(file);
            return 1;
        }
        catch (Exception e)
        {
            Sys.of_sendErrorMessage(e, "SimpleFile", "of_save(String)", "Error while saving the file!");
        }

        return -1;
    }

    /**
     * This function is used to create a backup. It will be called if the load-process of
     * the file failed. This has been coded in this constructor.
     * @return 1 if the backup was created successfully, otherwise -1.
     */
    private int of_createBackupFromCurrentFile()
    {
        if(file != null)
        {
            if(file.length() > 0)
            {
                //  We want to create a folder with the current date, after this we create the file with the time-stamp.
                String currentTimeStamp = Sys.of_getTimeStamp(true, "yyy_MM_dd", "hh_mm_ss").replace(" ",  "//");
                File backupFile = new File(Sys.of_getMainFilePath() + "Backups//", currentTimeStamp + file.getName());

                //  Delete the backup-file if it already exists.
                if(backupFile.exists())
                {
                    backupFile.delete();
                }

                try
                {
                    FileUtils.copyFile(file, backupFile);
                    return 1;
                }
                catch (IOException e)
                {
                    Sys.of_sendErrorMessage(e, "SimpleFile", "of_createBackupFromCurrentFile()", "Error while creating the backup file!");
                }
            }
            //  If the file is empty...
            else
            {
                Sys.of_debug("The file is empty! No backup will be created!");
            }
        }

        return -1;
    }

    /* ************************************* */
    /* SETTER // ADDER // REMOVER */
    /* ************************************* */

    /**
     * This function sets the given value to the configKey-section.
     * @param configKey ConfigKey Section in the .YML
     * @param object Object which will be set.
     */
    public void of_set(String configKey, Object object)
    {
        if(cfg != null)
        {
            if(object == null)
            {
                Sys.of_sendErrorMessage(null, "SimpleFile", "of_set(String, Object);", "The config-section-path is not valid! "+configKey);
                return;
            }

            cfg.set(configKey, object);
        }
    }

    public void of_setFile(File file)
    {
        this.file = file;
    }

    public void of_setConfig(YamlConfiguration config)
    {
        this.cfg = config;
    }

    public void of_removeKeySection(String key)
    {
        cfg.set(key, "");
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This function returns a string from a configKey which contains
     * multiple lines of values.
     * @param configKey ConfigSection to the multiple lines.
     * @return String array with the multiple lines.
     */
    public String[] of_getStringArrayByKey(String configKey)
    {
        if(cfg != null)
        {
            try
            {
                List<String> values = cfg.getStringList(configKey);

                //	Sicherstellen, dass die Liste nicht leer ist.
                if(!values.isEmpty())
                {
                    return values.toArray(new String[0]);
                }
            }
            catch (Exception ignored) { }
        }

        return null;
    }

    /**
     * This function returns all configKeys for one specified section.
     * @param configKey Specified section from which the keys should come from.
     * @return An array which contains the specified configKeys.
     */
    public String[] of_getKeySectionsByKey(String configKey)
    {
        String[] keys = null;

        if(cfg != null)
        {
            try
            {
                keys = cfg.getConfigurationSection(configKey).getKeys(false).toArray(new String[0]);
            }
            catch (Exception ignored) { }
        }

        return keys;
    }

    /**
     * This function is used to get a string value from
     * a configKey in the YML-file.
     * @param configKey Section in the .YML-File.
     * @return The value in the configKey.
     */
    public String of_getString(String configKey)
    {
        return cfg.getString(configKey);
    }

    public int of_getIntByKey(String configKey)
    {
        int value = -1;

        if(cfg != null)
        {
            try
            {
                value = cfg.getInt(configKey);
            }
            catch (Exception ignored) { }
        }

        return value;
    }

    public long of_getLongByKey(String configKey)
    {
        long value = -1;

        if(cfg != null)
        {
            try
            {
                value = cfg.getLong(configKey);
            }
            catch (Exception ignored) { }
        }

        return value;
    }

    public double of_getDoubleByKey(String configKey)
    {
        double value = -1;

        if(cfg != null)
        {
            try
            {
                value = cfg.getDouble(configKey);
            }
            catch (Exception ignored) { }
        }

        return value;
    }

    public boolean of_getBooleanByKey(String configKey)
    {
        boolean value = false;

        if(cfg != null)
        {
            try
            {
                value = cfg.getBoolean(configKey);
            }
            catch (Exception ignored) { }
        }

        return value;
    }

    /**
     * This function is used to generate an automatic index for this file.
     * @return Current keyCount or index value.
     */
    public int of_getNextKey()
    {
        int key = of_getSetInt("KeyCount", 0);
        key++;

        of_save("SimpleFile.of_getNextKey();");

        return key;
    }

    public File of_getFile()
    {
        return file;
    }

    public File[] of_getFiles()
    {
        return file.listFiles();
    }

    public YamlConfiguration of_getConfig()
    {
        return cfg;
    }

    public String of_getFileName()
    {
        if(file != null)
        {
            return file.getName();
        }

        return "NoFileName";
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    public boolean of_fileExists()
    {
        if(file != null)
        {
            if(file.length() == 0)
            {
                file.delete();
                return false;
            }

            return file.exists();
        }

        return false;
    }
}