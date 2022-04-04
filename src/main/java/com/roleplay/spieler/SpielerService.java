package com.roleplay.spieler;

import com.basis.ancestor.Objekt;
import com.roleplay.spieler.Spieler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collection;

/**
 * @Created 21.03.2022
 * @Author Nihar
 * @Description
 * This class contains useful methods or functions
 * in reference to the object: Spieler.
 *
 */
public class SpielerService extends Objekt
{
    //	Attribute
    public SpielerContext _CONTEXT;

    /* ************************************* */
    /* LOADER // UNLOADER */
    /* ************************************* */

    /**
     * This function creates for each online player an object instance of the class Spieler.
     * @return 1 if successful, 0 if not.
     */
    @Override
    public int of_load()
    {
        _CONTEXT = new SpielerContext();

        for(Player p : Bukkit.getOnlinePlayers())
        {
            _CONTEXT.of_loadPlayer(p);
        }

        return 1;
    }

    /**
     * Is used to save player data in the database before
     * the server is reloading/stopping.
     */
    @Override
    public void of_unload()
    {
        if(_CONTEXT != null)
        {
            Collection<Spieler> players = _CONTEXT.of_getAllSpieler();

            for(Spieler ps : players)
            {
                _CONTEXT.of_savePlayer(ps);
            }
        }
    }

    /* ************************************* */
    /* OBJEKT-ANWEISUNGEN */
    /* ************************************* */

    /**
     * This function sends an interactive-chat message to the player.
     * @param ps Player instance.
     * @param chatText The text which will be displayed in the chat.
     * @param hoverText The text which will be displayed if the cursor hovers over it.
     * @param command The command which will be executed by the player if the user clicks on the message.
     */
    public void of_sendInteractiveMessage(Spieler ps, String chatText, String hoverText, String command)
    {
        //	Interaktive Chat-Nachricht!
        TextComponent tc = new TextComponent();
        tc.setText(chatText);
        tc.setBold(true);
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        ps.of_getPlayer().spigot().sendMessage(tc);
    }
}