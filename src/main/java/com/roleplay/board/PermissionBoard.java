package com.roleplay.board;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;
import com.basis.utils.SimpleFile;
import com.roleplay.spieler.Spieler;

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
    private static final PermissionBoard instance = new PermissionBoard();

    //  PermissionsKey - Permissions
    Map<String, String> permissions = new HashMap<>();
    SimpleFile datei;

    /* ************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************* */

    private PermissionBoard()
    {
        datei = new SimpleFile(Sys.of_getMainFilePath() + "//Others//permissions.yml");
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

        //  Load specific Permissions (Commands):
        of_addPermissions2Board("Command.Permission.MRS", "mrs.command.mrs");
        of_addPermissions2Board("Command.Permission.Test", "mrs.command.test");
        of_addPermissions2Board("Command.Permission.Interaction", "mrs.command.interaction");
        of_addPermissions2Board("Command.Permission.Dataprotection", "mrs.command.dataprotection");
        of_addPermissions2Board("Command.Permission.Showinfo", "mrs.command.showinfo");
        of_addPermissions2Board("Command.Permission.Textblock", "mrs.command.textblock");
        of_addPermissions2Board("Command.Permission.NPC", "mrs.command.npc");
        of_addPermissions2Board("Command.Permission.Hologram", "mrs.command.hologram");

        //  Load specific Permissions (Role play):

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

    public static PermissionBoard of_getInstance()
    {
        return instance;
    }

    /* **************************** */
    /* BOOLS */
    /* *************************** */

    /**
     * This method is used to identify the permissions by the given permissions-key.
     * The defined permissions are loaded from the permissions.yml file.
     * @param ps The player.
     * @param permKey The permissions-key.
     * @return true if the permissions-key is defined, false if not.
     */
    public boolean of_hasPermissions(Spieler ps, String permKey)
    {
        String permission = permissions.get(permKey);

        //  If the permission is not found, use the admin permission.
        if(permission == null)
        {
            permission = permissions.get("General.Admin.Permission");
        }

        return ps.of_getPlayer().hasPermission(permission);
    }

    /**
     * This method is used to check if the player has the given permission.
     * The special-part of this method is that it does not check the current permissions-list
     * which has been defined in the permissions.yml.
     * @param ps The player to check.
     * @param permissions The permissions to check.
     * @return True if the player has the given permission, false if not.
     */
    public boolean of_hasPermissionsByDefault(Spieler ps, String permissions)
    {
        return ps.of_getPlayer().hasPermission(permissions);
    }

    public boolean of_isAdmin(Spieler ps)
    {
        return of_hasPermissions(ps, "General.Admin.Permission");
    }
}
