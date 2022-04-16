package com.roleplay.board;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;
import com.basis.utils.Datei;

import java.util.HashMap;
import java.util.Map;

/**
 * @Created 16.04.2022
 * @Author Nihar
 * @Description
 * This class is used to handle permissions of the user.
 * The permissions can be edited in the permissions.yml file.
 */
public class PermissionBoard extends Objekt
{
    //  Attributes:
    //  PermissionsKey - Permissions
    Map<String, String> permissions = new HashMap<>();
    Datei datei;

    /* ************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************* */

    public PermissionBoard()
    {
        datei = new Datei(Sys.of_getMainFilePath() + "//Others//permissions.yml");
    }

    /**
     * This method is used to load all predefined permissions from the permissions.yml file.
     * If the file does not exist it will be created.
     * @return 1 if the file was loaded successfully, -1 if the file could not be saved.
     */
    @Override
    public int of_load()
    {
        //  Load general Permissions:
        of_addPermissions2Board("General.Admin.Permission", "mrs.general.admin");
        of_addPermissions2Board("General.Default.Permission", "mrs.general.default");

        //  Load specific Permissions (Role play):
        of_addPermissions2Board("Command.Permission.Test", "mrs.command.test");
        of_addPermissions2Board("Command.Permission.Showinfo", "mrs.command.showinfo");

        return datei.of_save("PermissionBoard.of_load();");
    }

    /* **************************** */
    /* ADDER // SETTER // REMOVER */
    /* *************************** */

    private void of_addPermissions2Board(String permKey, String permission)
    {
        permission = datei.of_getSetString(permKey, permission);
        permissions.put(permKey, permission);
    }

    /* **************************** */
    /* DEBUG CENTER */
    /* *************************** */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("Loaded permissions: " + permissions.size());
    }

    /* **************************** */
    /* GETTER */
    /* *************************** */

    public String of_getPermissionByKey(String permKey)
    {
        String permission = permissions.get(permKey);

        //  If the permission is not found, return the admin permission.
        if(permission == null)
        {
            return permissions.get("General.Admin.Permission");
        }

        return permission;
    }
}