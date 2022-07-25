package com.roleplay.events;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.roleplay.npc.NPC;
import com.roleplay.spieler.Spieler;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @Created 31.05.2022
 * @Author Nihar
 * @Description
 * This event-class is used to listen to the events which are used for the npc-system.
 * In this case we check the player-movement. If the player is near a npc, we change the head-rotation of the npc.
 */
public class ue_npc implements Listener
{
    /**
     * This event is used to react to the player-movement-event.
     * @param e The event which is used to react to the player-movement-event.
     */
    @EventHandler
    public void ue_playerMove4MRS(PlayerMoveEvent e)
    {
        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

        if(ps != null)
        {
            //  Check if the NPCService is valid and some NPCs are registered.
            if(main.NPCSERVICE != null && main.NPCSERVICE._CONTEXT.of_getLoadedObjects() > 0)
            {
                Location pLoc = ps.of_getPlayer().getLocation();

                //  Iterate through all NPCs...
                Objekt[] objects = main.NPCSERVICE._CONTEXT.of_getAllObjects();

                if(objects != null && objects.length > 0)
                {
                    for(Objekt objekt : objects)
                    {
                        if(objekt instanceof NPC)
                        {
                            NPC npc = (NPC) objekt;
                            Location npcLoc = npc.of_getLocation();

                            //  Check for the same world!
                            if(pLoc.getWorld() == npcLoc.getWorld())
                            {
                                //  Check for the distance between the player and the NPC.
                                if(pLoc.distance(npcLoc) > 5.0D)
                                    continue;

                                //  Get the npc-location to fix the head-rotation.
                                npcLoc.setDirection(e.getPlayer().getLocation().subtract(npcLoc).toVector());
                                float yaw = npcLoc.getYaw();
                                float pitch = npcLoc.getPitch();

                                //  Send the player some packets to update the npc-head rotation.
                                PlayerConnection connection = (((CraftPlayer)e.getPlayer()).getHandle()).b;
                                connection.a(new PacketPlayOutEntity.PacketPlayOutEntityLook(npc.of_getEntityNPC().getBukkitEntity().getEntityId(), (byte) (int) (yaw % 360.0F * 256.0F / 360.0F), (byte) (int) (pitch % 360.0F * 256.0F / 360.0F), false));
                                connection.a(new PacketPlayOutEntityHeadRotation(npc.of_getEntityNPC(), (byte)(int)(yaw % 360.0F * 256.0F / 360.0F)));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This event is used to listen to the player
     * while changing the world.
     * @param e The event instance.
     */
    @EventHandler
    public void ue_playerChangeWorld4MRS(PlayerChangedWorldEvent e)
    {
        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

        if(ps != null)
        {
            //  Check if the NPCService is valid and some NPCs are registered.
            if(main.NPCSERVICE != null && main.NPCSERVICE._CONTEXT.of_getLoadedObjects() > 0)
            {
                //  Refresh the NPCs for the player.
                main.NPCSERVICE.of_removeAllNPCsFromPlayer(ps);
                main.NPCSERVICE.of_showAllNPCs2Player(ps);
            }
        }
    }
}
