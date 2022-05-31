package com.roleplay.board;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.spieler.Spieler;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @Created 18.05.2022
 * @Author Nihar
 * @Description
 * With this object-class it is possible
 * to create or load a defined scoreBoard to
 * the player.
 */
public class ScoreBoard extends Objekt
{
    //  Attributes:
    String[] scoreBoardLines;

    /* ************************* */
    /* CONSTRUCTOR */
    /* ************************* */

    /**
     * Define a scoreBoard which will be displayed to the player.
     * @param lines Lines which should be displayed. The first line is the
     *              title of the scoreBoard.
     */
    public ScoreBoard(String[] lines)
    {
        this.scoreBoardLines = lines;
    }

    /**
     * Load the scoreBoard for all online-players.
     */
    public void of_loadScoreboard2AllPlayers()
    {
        if(main.SPIELERSERVICE != null)
        {
            Collection<Spieler> players = main.SPIELERSERVICE._CONTEXT.of_getAllPlayers();

            for(Spieler ps : players)
            {
                of_sendScoreboard2Player(ps);
            }
        }
    }

    /* **************************** */
    /* OBJECT METHODS */
    /* *************************** */

    /**
     * This function is used to create a player defined scoreBoard.
     * @param ps Player instance.
     */
    public void of_sendScoreboard2Player(Spieler ps)
    {
        if(ps != null)
        {
            if(scoreBoardLines.length > 0)
            {
                Player p = ps.of_getPlayer();

                //  Create a 1.18-ScoreBoard!
                Scoreboard board = new Scoreboard();
                String title = main.MESSAGEBOARD.of_translateMessageWithPlayerStats(scoreBoardLines[0], ps);

                //  The first argument is used to set the title.
                ScoreboardObjective objective = board.a(Sys.of_getVersion(), IScoreboardCriteria.b, (IChatBaseComponent) new ChatMessage(title), IScoreboardCriteria.EnumScoreboardHealthDisplay.a);

                //  Send default packets... (remove, create, DisplaySideboard)
                of_sendPacket(p, new PacketPlayOutScoreboardObjective(objective, 1));
                of_sendPacket(p,  new PacketPlayOutScoreboardObjective(objective, 0));
                of_sendPacket(p, new PacketPlayOutScoreboardDisplayObjective(1, objective));

                //  Remove the title from the array to avoid an order problem by reversing it!
                String[] lines = Sys.of_removeArrayValueByIndex(scoreBoardLines, 0);

                //  Get the size of the elements in the array.
                int objScore = lines.length;
                int placeholderCounter = 0;

                //  Reverse the array to get the right order for the scoreboard.
                try
                {
                    Collections.reverse(Arrays.asList(lines));
                }
                catch (Exception ignored)
                {
                    of_sendErrorMessage(null, "ScoreBoard.of_sendScoreboard2Player();", "Error while reversing the given array! This should not happen... we continue anyway!");
                }

                //	Iterate all lines...
                for(int i = 0; i < lines.length; i++)
                {
                    objScore--;

                    //	tmpValue is the text which will be displayed.
                    String tmpValue = lines[i];
                    tmpValue = tmpValue.replace("&", "§");

                    //  If a placeholder has been found add a placement to avoid a problem by using same color codes.
                    boolean lb_addPlacement = tmpValue.contains("%") || tmpValue.equals("");

                    //  Replace current line with player stats...
                    tmpValue = main.MESSAGEBOARD.of_translateMessageWithPlayerStats(tmpValue, ps);

                    if(lb_addPlacement)
                    {
                        placeholderCounter++;
                        StringBuilder addPlacement = new StringBuilder("§");

                        //  Add the placement.
                        for(int k = 0; k < placeholderCounter; k++)
                        {
                            addPlacement.append("§");
                        }

                        tmpValue = tmpValue + addPlacement;
                    }

                    //  Add the current defined line to the scoreboard and send it to the player.
                    of_sendPacket(p, new PacketPlayOutScoreboardScore(ScoreboardServer.Action.a, Sys.of_getVersion(), tmpValue, (i + 1)));
                }
            }
        }
    }

    /**
     * This function is used to send a packet to the player.
     * @param p Player instance.
     * @param packet The packet which should be sent.
     */
    private void of_sendPacket(Player p, Packet<?> packet)
    {
        if(packet != null)
        {
            //  Create the connection and the packet.
            try
            {
                (((CraftPlayer) p).getHandle().b).a(packet);
            }
            catch (Exception ignored) { }
        }
    }
}
