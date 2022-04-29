package com.roleplay.npc;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.mojang.authlib.GameProfile;
import com.roleplay.extended.LocationDatei;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Created 29.04.2022
 * @Author Nihar
 * @Description
 * This class is used to store or load a NPC from the file.
 * It also contains methods to get the NPC by an ID.
 *
 * This class has been created with the support of the
 * GitHub CoPilot.
 *
 */
public class NPCContext extends Objekt
{
    //  Attributes:
    Map<Integer, NPC> npcs = new HashMap<>();

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        // Load the NPCs.
        File directory = new File(Sys.of_getMainFilePath() + "//NPCs//");
        File[] files = directory.listFiles();

        if(files != null && files.length > 0)
        {
            for(File file : files)
            {
                if(file != null)
                {
                    int rc = of_loadNPCByFile(file);

                    //  If an error occurred, stop the loading process.
                    if(rc != 1)
                    {
                        of_sendErrorMessage(null, "NPCContext.of_load();", "There was an error while loading the NPC from the following file: " + file.getName());
                        return -1;
                    }
                }
            }

            return 1;
        }

        return -1;
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This function is used to load a NPC from a file.
     * @param file The file to load the NPC from.
     * @return 1 = Ok, -1 = Error. -2 = Some data in the given file is missing. -3 = The EntityPlayer could not be created with the given information.
     */
    private int of_loadNPCByFile(File file)
    {
        LocationDatei datei = new LocationDatei(file);

        if(datei.of_fileExists())
        {
            //  Get all important information.
            Location location = datei.of_getLocationByKey("Location");
            String[] commandSet = datei.of_getStringArrayByKey("CommandSet");
            String displayName = datei.of_getString("DisplayName");
            String skinName = datei.of_getString("SkinName");

            if(location != null && commandSet != null && displayName != null)
            {
                //  Set the SkinName to NULL if it is set to 'None'.
                if(skinName.equalsIgnoreCase("None"))
                {
                    skinName = null;
                }

                //  Load the NPC. The SkinName can be null.
                NPC npc = new NPC(location, displayName, skinName);
                npc.of_setCommandSet(commandSet);

                // Check if the NPC is valid.
                String errorMessage = npc.of_validate();

                // If no error occurred, add the NPC to the list.
                if(errorMessage == null)
                {
                    EntityPlayer entityPlayer = of_createEntityPlayer2World(npc);
                    npc.of_setEntityNPC(entityPlayer);

                    //  Add the NPC to the list.
                    npcs.put(npc.of_getObjectId(), npc);
                    return 1;
                }
                // If an error occurred, stop the loading.
                else
                {
                    of_sendErrorMessage(null, "NPCContext.of_loadNPCByFile();", errorMessage);
                    return -1;
                }
            }

            return -2;
        }

        return -1;
    }

    /**
     * This function is used to create a EntityPlayer from a NPC.
     * @return The EntityPlayer.
     */
    private EntityPlayer of_createEntityPlayer2World(NPC npc)
    {
        //  Create an NPC.
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        Location loc = npc.of_getLocation();
        WorldServer world = ((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle();

        //  Set the GameProfile and the EntityPlayer.
        GameProfile gameProfile = main.NPCSERVICE.of_createGameProfile(npc.of_getSkinName());

        return new EntityPlayer(server, world, gameProfile);
    }

    /**
     * This function is used to load create a file for the given NPC.
     * @param npc The NPC to store into a file.
     * @return 1 = Ok, -1 = Error, -2 = NPC already exists.
     */
    public int of_saveNPC2File(NPC npc)
    {
        String fileName = npc.of_getDisplayName();
        fileName = Sys.of_getNormalizedString(fileName).toLowerCase();
        fileName = "npc_" + fileName + ".yml";

        // Create the file.
        LocationDatei datei = new LocationDatei(new File(Sys.of_getMainFilePath() + "//NPCs//" + fileName));

        if(!datei.of_fileExists())
        {
            //  Save the NPC.
            datei.of_set("DisplayName", npc.of_getDisplayName());

            if(npc.of_getSkinName() == null)
            {
                npc.of_setSkinName("None");
            }

            datei.of_set("SkinName", npc.of_getSkinName());
            datei.of_getSetStringArray("CommandSet", npc.of_getCommandSet());
            datei.of_setLocation("Location", npc.of_getLocation());

            // Save the file.
            return datei.of_save("NPCContext.of_saveNPC2File();");
        }

        return -2;
    }

    /* ************************************* */
    /* DEBUG CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        // Send the debug information.
        Sys.of_sendMessage("Loaded NPCs: " + of_getLoadedNPCsSize());
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public int of_getLoadedNPCsSize()
    {
        return npcs.size();
    }
}
