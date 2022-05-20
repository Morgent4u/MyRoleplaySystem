package com.roleplay.npc;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.mojang.authlib.GameProfile;
import com.roleplay.extended.LocationDatei;
import com.roleplay.spieler.Spieler;
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
            String skinName = datei.of_getString("SkinName");

            if(location != null && commandSet != null)
            {
                //  Set the SkinName to NULL if it is set to 'None'.
                skinName = skinName.equalsIgnoreCase("None") ? null : skinName;

                //  Load the NPC. The SkinName can be null.
                NPC npc = new NPC(location, skinName);
                npc.of_setCommandSet(commandSet);
                npc.of_setInfo(datei.of_getFileName());

                // Check if the NPC is valid.
                String errorMessage = npc.of_validate();

                // If no error occurred, add the NPC to the list.
                if(errorMessage == null)
                {
                    EntityPlayer entityPlayer = of_createEntityPlayer2World(npc);

                    // This function also sets the ObjectId (EntityId)!
                    npc.of_setEntityNPC(entityPlayer);

                    //  Teleport the EntityPlayer to the given location.
                    Location loc = npc.of_getLocation();
                    entityPlayer.b(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

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
        WorldServer world = ((CraftWorld) Objects.requireNonNull(npc.of_getLocation().getWorld())).getHandle();

        //  Set the GameProfile and the EntityPlayer.
        GameProfile gameProfile = main.NPCSERVICE.of_createGameProfile(npc.of_getSkinName());

        return new EntityPlayer(server, world, gameProfile);
    }

    /**
     * This function is used to load create a file for the given NPC.
     * @param npc The NPC to store into a file.
     * @return 1 = Ok, -1 = Error, -2 = NPC already exists.
     */
    public int of_saveNPC2File(NPC npc, Spieler ps)
    {
        //  The info-attribute contains the given name (which has been defined by the player).
        //  Normalize the fileName.
        String fileName = npc.of_getInfo();
        fileName = Sys.of_getNormalizedString(fileName).toLowerCase();
        fileName = "npc_" + fileName + ".yml";

        // Create the file.
        LocationDatei datei = new LocationDatei(new File(Sys.of_getMainFilePath() + "//NPCs//" + fileName));

        if(!datei.of_fileExists())
        {
            Location loc = npc.of_getLocation();

            //  Save the NPC.
            if(npc.of_getSkinName() == null)
            {
                npc.of_setSkinName("None");
            }

            datei.of_set("SkinName", npc.of_getSkinName());
            datei.of_getSetStringArray("CommandSet", npc.of_getCommandSet());
            datei.of_setLocation("Location", loc);

            // Save the NPC-Stuff into the file.
            int rc = datei.of_save("NPCContext.of_saveNPC2File();");

            if(rc == 1)
            {
                // After saving the NPC successfully, we load it from the file and show the created NPC, to all online players.
                rc = of_loadNPCByFile(datei.of_getFile().getAbsoluteFile());

                if(rc == 1)
                {
                    //  Show the NPC for all online players.
                    // Send the remove packets...
                    main.NPCSERVICE.of_removeAllNPCsFromAllOnlinePlayers();

                    //  Send the create packets...
                    main.NPCSERVICE.of_showAllNPCs2AllOnlinePlayers();

                    //  Perform a command to create the hologram. This is used to avoid the problem with the name of the npc.
                    if(ps != null)
                    {
                        ps.of_getPlayer().performCommand("hd create " + fileName + " &8[&cNPC: &f"+npc.of_getInfo()+"&8]");
                    }

                    return 1;
                }
                //  If the NPC could not be loaded from the file.
                else
                {
                    of_sendErrorMessage(null, "NPCContext.of_saveNPC2File();", "The NPC could not be loaded from the file.");
                }
            }
            //  If file could not be saved...
            else
            {
                of_sendErrorMessage(null, "NPCContext.of_saveNPC2File();", "File could not be saved.");
            }

            return -1;
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

    public NPC of_getNPCByEntityId(int entityId)
    {
        return npcs.get(entityId);
    }

    public int of_getLoadedNPCsSize()
    {
        return npcs.size();
    }

    public NPC[] of_getLoadedNPCs()
    {
        return npcs.values().toArray(new NPC[0]);
    }
}
