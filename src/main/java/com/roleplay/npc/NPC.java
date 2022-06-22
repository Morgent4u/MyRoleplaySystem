package com.roleplay.npc;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Location;

/**
 * @Created 17.04.2022
 * @Author Nihar
 * @Description
 * This class is used to represent an NPC.
 *
 * This class has been created with the support of the
 * GitHub CoPilot.
 *
 */
public class NPC extends Objekt
{
    //  Attributes:
    EntityPlayer entityNpc;
    Location loc;

    String[] cmds;
    String skinName;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public NPC(Location spawnLoc, String skinName)
    {
        //  Set the location.
        loc = spawnLoc;

        //  Set the skin name.
        this.skinName = skinName;
    }

    /* ************************************* */
    /* VALIDATE PROCESS */
    /* ************************************* */

    @Override
    public String of_validate()
    {
        if(loc == null)
        {
            return "The location is null.";
        }

        if(cmds == null || cmds.length == 0)
        {
            return "The command set is null or empty.";
        }

        return null;
    }

    /* ************************************* */
    /* DEBUG - CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        //  Send the information.
        Sys.of_sendMessage("EntityId: " + ( ( entityNpc != null ) ? entityNpc.getBukkitEntity().getEntityId() : "-1" ));
        Sys.of_sendMessage("Location: " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
        Sys.of_sendMessage("Skin: " + skinName);

        if(cmds != null && cmds.length > 0)
        {
            Sys.of_sendMessage("Commands:");

            for(String cmd : cmds)
            {
                Sys.of_sendMessage("- " + cmd);
            }
        }
        // If no commands are set, send a message.
        else
        {
            Sys.of_sendMessage("Commands: not defined");
        }
    }

    /* ************************************* */
    /* SETTER // ADDER // REMOVE */
    /* ************************************* */

    /**
     * If the entityNPC will be updated to the new one
     * we need to update the objectId. If the entityNPC
     * is null, we need to set the objectId to -1.
     * @param entityNpc The new entityNPC.
     */
    public void of_setEntityNPC(EntityPlayer entityNpc)
    {
        if(entityNpc != null)
        {
            this.entityNpc = entityNpc;

            //  Update the EntityId to this object (ObjectId).
            of_setObjectId(entityNpc.getBukkitEntity().getEntityId());
        }
        //  Else, set the EntityId to -1.
        else
        {
            of_setObjectId(-1);
        }
    }

    public void of_setCommandSet(String[] cmdSet)
    {
        this.cmds = cmdSet;
    }

    public void of_setSkinName(String skinName)
    {
        this.skinName = skinName;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public EntityPlayer of_getEntityNPC()
    {
        return entityNpc;
    }

    public Location of_getLocation()
    {
        return loc;
    }

    public String of_getSkinName()
    {
        return skinName;
    }

    public String[] of_getCommandSet()
    {
        return cmds;
    }
}
