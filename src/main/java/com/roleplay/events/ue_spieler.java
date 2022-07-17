package com.roleplay.events;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Settings;
import com.roleplay.board.MessageBoard;
import com.roleplay.manager.TablistManager;
import com.roleplay.objects.CommandSet;
import com.roleplay.objects.TextBlock;
import com.roleplay.spieler.Spieler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

/**
 * @Created 04.04.2022
 * @Author Nihar
 * @Description
 * This event-class listens to player specific casual events.
 */
public class ue_spieler implements Listener
{
    /**
     * This event is used to interact with the player while joining the server.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerJoin4MRS(PlayerJoinEvent e)
    {
        //  Load all player specific data from database / file-system and store the instance in the player-context.
        main.SPIELERSERVICE._CONTEXT.of_loadPlayer(e.getPlayer());

        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

        if(ps != null)
        {
            //  Check for the NPCs...
            if(main.NPCSERVICE != null && main.NPCSERVICE._CONTEXT.of_getLoadedObjects() > 0)
            {
                main.NPCSERVICE.of_showAllNPCs2Player(ps);
            }

            //  When we are using the tab-list manager we need to update/create the tab-list for the player :)!
            if(Settings.of_getInstance().of_isUsingTablist())
            {
                TablistManager.of_getInstance().of_createOrUpdateTablist4AllPlayers();
            }

            //  If we use the joinQuitMessage, we send them into the chat.
            if(Settings.of_getInstance().of_isUsingJoinAndQuitMessage())
            {
                e.setJoinMessage(MessageBoard.of_getInstance().of_translateMessageWithPlayerStats(Settings.of_getInstance().of_getJoinMessage(), ps));
            }
        }
    }

    /**
     * This event is used to unload the player and remove the player from the player-context.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerQuit4MRS(PlayerQuitEvent e)
    {
        //  Unload all player specific data from the player-context.
        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

        if(ps != null)
        {
            //  If we use the joinQuitMessage, we send them into the chat.
            if(Settings.of_getInstance().of_isUsingJoinAndQuitMessage())
            {
                e.setQuitMessage(MessageBoard.of_getInstance().of_translateMessageWithPlayerStats(Settings.of_getInstance().of_getQuitMessage(), ps));
            }

            //  Remove the player from the IBlock-Setup.
            if(Settings.of_getInstance().of_isUsingIBlock())
            {
                main.IBLOCKSERVICE.of_removePlayerFromSetup(ps);
            }

            //  Check for the NPCs...
            /*
            if(main.NPCSERVICE != null && main.NPCSERVICE._CONTEXT.of_getLoadedNPCsSize() > 0)
            {
                main.NPCSERVICE.of_removeAllNPCsFromPlayer(ps);
            }
            */

            main.SPIELERSERVICE._CONTEXT.of_unloadPlayer(ps);
        }
    }

    /**
     * This Event is used to listen to the player when
     * he is trying to connect to the server. The event-call
     * is earlier than the player-join-event.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerLogin4MRS(PlayerLoginEvent e)
    {
        if(Settings.of_getInstance().of_isUsingMaintenanceMode())
        {
            //  Create a text-block and add a placeholder to it.
            TextBlock text = new TextBlock("txt_maintenance_mode");
            text.of_addPlaceholder2TextBlock("%p%", e.getPlayer().getName());

            //  Build the kick-message.
            String[] messages = text.of_getTranslatedTextBlockLines();
            StringBuilder msgBuilder = new StringBuilder();

            for(String message : messages)
            {
                msgBuilder.append(message).append("\n");
            }

            //  The result-parameter needs to be NULL.
            e.disallow(null, msgBuilder.toString());
        }
    }

    /**
     * This Event is used to interact with the player when he switches the item in his hands.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerSwapHandItem4MRS(PlayerSwapHandItemsEvent e)
    {
        if(Settings.of_getInstance().of_isUsingMenuOnSwap())
        {
            Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

            if(ps != null)
            {
                main.SPIELERSERVICE.of_openInvById(ps, 1);
            }
        }
    }

    /**
     * This event is used to interact with the player while he moves.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerMove4MRS(PlayerMoveEvent e)
    {
        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

        if(ps != null)
        {
            e.setCancelled(ps.of_isBlockedMovingEnabled());
        }
    }

    /**
     * This event is used to interact with the player while
     * he is chatting.
     * @param e The event instance.
     */
    @EventHandler
    public void ue_asyncPlayerChat4MRS(AsyncPlayerChatEvent e)
    {
        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

        //  Check if the player is valid, and we need to get the player-input.
        if(ps != null && ps.of_isWaiting4Input())
        {
            //  Get the player input and execute the rest of the defined Commands.
            String[] cmds = null;
            ps.of_setWaiting4Input(false);

            try
            {
                cmds = (String[]) ps.of_getPowerObject();
            }
            catch(Exception ignored) { }

            //  Validate the rest of the CommandSet.
            if(cmds != null && cmds.length > 0)
            {
                //  In the first array-value should be the placeholder.
                String placeholder = cmds[0];

                if(placeholder.startsWith("%"))
                {
                    cmds = Sys.of_removeArrayValue(cmds, placeholder);

                    if(cmds.length > 0)
                    {
                        //  Create the new command-set with the rest of the commands.
                        CommandSet cmdSet = new CommandSet(cmds, ps);
                        cmdSet.of_addReplaceValue2CommandSet(cmds[0], e.getMessage());
                        cmdSet.of_replaceAllCommandsWithDefinedReplacements();
                        cmdSet.of_executeAllCommands();
                    }
                }
            }
        }
    }
}
