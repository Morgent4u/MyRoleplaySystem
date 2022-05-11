package com.roleplay.extern;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.roleplay.npc.NPC;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @Created 16.04.2022
 * @Author Nihar
 * @Description
 * This class is used to represent protocolLib-objects.
 */
public class ProtocolLib extends Objekt
{
    public ProtocolManager PROTOCOLMANAGER;

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        try
        {
            PROTOCOLMANAGER = ProtocolLibrary.getProtocolManager();
            return 1;
        }
        catch (Exception e)
        {
            of_sendErrorMessage(e, "of_load();", "Error while registering the ProtocolLib-service to this plugin.");
        }

        return -1;
    }

    /**
     * This function is used to load the protocolLib-listeners.
     */
    public void ue_addSpecificPacketListeners2ProtocolLibManager()
    {
        if(PROTOCOLMANAGER != null)
        {
            // Only enable this packet listener if NPCs has been created.
            if(main.NPCSERVICE._CONTEXT.of_getLoadedNPCsSize() > 0)
            {
                //  @PacketListener | This PacketListener is used to listen between Packets of the player and the NPC.
                PROTOCOLMANAGER.addPacketListener(new PacketAdapter(main.PLUGIN, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY)
                {
                    @Override
                    public void onPacketReceiving(PacketEvent e)
                    {
                        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

                        if(ps != null)
                        {
                            PacketContainer packet = e.getPacket();

                            //  get Entity ID
                            int entityId = packet.getIntegers().read(0);

                            // Get the NPC by using the EntityId.
                            NPC npc = main.NPCSERVICE._CONTEXT.of_getNPCByEntityId(entityId);

                            if(npc != null)
                            {
                                //  Only let the player interact when he is near the npc.
                                if(npc.of_getLocation().distance(ps.of_getPlayer().getLocation()) <= 1.5)
                                {
                                    new BukkitRunnable()
                                    {

                                        @Override
                                        public void run()
                                        {
                                            //  Execute all given Commands for this specific NPC.
                                            new CommandSet(npc.of_getCommandSet(), ps).of_executeAllCommands();
                                        }

                                    }.runTask(main.PLUGIN);
                                }
                            }
                        }
                    }
                });
            }
        }
    }
}
