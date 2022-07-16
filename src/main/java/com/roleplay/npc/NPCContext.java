package com.roleplay.npc;

import com.basis.ancestor.Objekt;
import com.basis.ancestor.ObjektContext;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.roleplay.extended.ExtendedFile;
import com.roleplay.spieler.Spieler;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import java.io.File;
import java.util.Objects;
import java.util.UUID;

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
public class NPCContext extends ObjektContext
{
    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    /**
     * Is used to initialize the object.
     * @param instanceName The name of the instance.
     * @param mainFolder   The main-folder which contains all Object files.
     */
    public NPCContext(String instanceName, String mainFolder)
    {
        super(instanceName, mainFolder);
    }

    @Override
    public int of_loadObjectByFile(File file)
    {
        ExtendedFile datei = new ExtendedFile(file);

        if(datei.of_fileExists())
        {
            //  Get all important information.
            Location location = datei.of_getLocationByKey("Location");
            String[] commandSet = datei.of_getStringArrayByKey("CommandSet");
            String skinName = datei.of_getString("SkinName");
            String skinTexture = datei.of_getString("SkinTexture");
            String skinSignature = datei.of_getString("SkinSignature");

            if(location != null && commandSet != null && skinTexture != null && skinSignature != null)
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
                    EntityPlayer entityPlayer = of_createEntityPlayer2World(npc, skinTexture, skinSignature);

                    // This function also sets the ObjectId (EntityId)!
                    npc.of_setEntityNPC(entityPlayer);

                    //  Teleport the EntityPlayer to the given location.
                    Location loc = npc.of_getLocation();
                    entityPlayer.b(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

                    //  Add the NPC to the list.
                    return of_addObject2ContextList(npc);
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

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This function is used to create a EntityPlayer from a NPC.
     * @return The EntityPlayer.
     */
    private EntityPlayer of_createEntityPlayer2World(NPC npc, String skinTexture, String skinSignature)
    {
        //  Create an NPC.
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) Objects.requireNonNull(npc.of_getLocation().getWorld())).getHandle();

        //  Set the GameProfile and the EntityPlayer.
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
        gameProfile.getProperties().put("textures", new Property("textures", skinTexture, skinSignature));

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
        ExtendedFile datei = new ExtendedFile(new File(Sys.of_getMainFilePath() + "//NPCs//" + fileName));

        if(!datei.of_fileExists())
        {
            Location loc = npc.of_getLocation();

            //  Save the NPC.
            if(npc.of_getSkinName() == null)
            {
                npc.of_setSkinName("None");
            }

            //  Store the NPC data into the npc-file.
            datei.of_set("SkinName", npc.of_getSkinName());

            //  Save the Texture and Signature of the skin, this is used to create the GameProfile.
            String[] textureSignature = main.NPCSERVICE.of_getTextureAndSignatureBySkinName(npc.of_getSkinName());

            if(textureSignature != null && textureSignature.length > 0)
            {
                datei.of_set("SkinTexture", textureSignature[0]);
                datei.of_set("SkinSignature", textureSignature[1]);
            }

            //  Store the given CommandSet and the location into the file.
            datei.of_getSetStringArray("CommandSet", npc.of_getCommandSet());
            datei.of_setLocation("Location", loc);

            // Save the NPC-Stuff into the file.
            int rc = datei.of_save("NPCContext.of_saveNPC2File();");

            if(rc == 1)
            {
                // After saving the NPC successfully, we load it from the file and show the created NPC, to all online players.
                rc = of_loadObjectByFile(datei.of_getFile().getAbsoluteFile());

                if(rc == 1)
                {
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
        Sys.of_sendMessage("Loaded NPCs: " + of_getLoadedObjects());
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    @Override
    public Objekt of_getObjectById(int objectId)
    {
        Objekt[] objects = main.NPCSERVICE._CONTEXT.of_getAllObjects();

        if(objects != null && objects.length > 0)
        {
            for(Objekt objekt : objects)
            {
                if(objekt instanceof NPC)
                {
                    NPC npc = (NPC) objekt;

                    if(npc.of_getEntityId() == objectId)
                    {
                        return npc;
                    }
                }
            }
        }

        return null;
    }
}
