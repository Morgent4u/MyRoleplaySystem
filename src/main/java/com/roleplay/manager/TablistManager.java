package com.roleplay.manager;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Datei;
import com.roleplay.spieler.Spieler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Created 20.05.2022
 * @Author Nihar
 * @Description
 * This object-class is used to set a tab-list to the player.
 * It also manages the defined player-list.
 */
public class TablistManager extends Objekt
{
    //  Attributes:
    //  IntegerId - Permissions
    private Map<Integer, String[]> teams = new HashMap<>();
    private String topLine;
    private String bottomLine;

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    /**
     * This function is used to load a defined tab-list from the settings.yml file.
     * If no tab-list has been defined it will be added a predefined tab-list-design.
     * @param datei The Datei-object which is used to load the settings.yml file.
     * @param configSection The section which is used to load the tab-list.
     */
    public void of_loadPredefinedTeams(Datei datei, String configSection)
    {
        String[] keys = datei.of_getKeySectionsByKey(configSection);

        topLine = datei.of_getSetString(configSection + ".Top", "&c"+Sys.of_getProgramVersion());
        bottomLine = datei.of_getSetString(configSection + ".Bottom", "&fQuestions? &a/Help");

        //  No entries found... we add two :)
        if(keys == null || keys.length == 1)
        {
            //  Admin:
            datei.of_getSetString(configSection + ".001.Label", "&8[&4&lAdmin&8]&4 %p%");
            datei.of_getSetString(configSection + ".001.Permissions", "mrs.scoreboard.admin");

            //  Default-player:
            datei.of_getSetString(configSection + ".002.Label", "&8[&aPlayer&8]&a %p%");
            datei.of_getSetString(configSection + ".002.Permissions", "mrs.scoreboard.default");
            keys = datei.of_getKeySectionsByKey(configSection);
        }

        //  We remove some array-values because the 'USE,TOP,BOTTOM' are not needed.
        //  Remove the index-value 0 because if remove some entry the order will be updated!
        keys = Sys.of_removeArrayValueByIndex(keys, 0);
        keys = Sys.of_removeArrayValueByIndex(keys, 0);
        keys = Sys.of_removeArrayValueByIndex(keys, 0);

        //  Need to be bigger than 1 because we have the 'USE'-entry!
        if(keys.length > 0)
        {
            for (String key : keys)
            {
                //  The key must be a number!
                int teamId = Sys.of_getString2Int(key);

                if(teamId != -1)
                {
                    String label = datei.of_getString(configSection + "." + key + ".Label");
                    String permissions = datei.of_getString(configSection + "." + key + ".Permissions");

                    //  Validate the strings:
                    if(label != null && permissions != null)
                    {
                        String[] team = new String[2];
                        team[0] = label.replace("&", "§");
                        team[1] = permissions;

                        teams.put(teamId, team);
                    }
                    //  Error while reading the scoreboard-line.
                    else
                    {
                        Sys.of_debug("Error while reading the tab-list-entries-line: "+key+" | The label or the permissions are null. Please check the settings-file.");
                    }
                }
                //  If the given key is not valid!
                else
                {
                    Sys.of_debug("The given key for the tab-list '" + key + "' is not valid! It must be a number!");
                }
            }

            //  Cannot be empty because we add two entries in the beginning.
            if(!teams.isEmpty())
            {
                Sys.of_debug("Tab-list-entries loaded: "+teams.size());
            }
        }
        //  If no entry has been found, we send an error-message.
        else
        {
            of_sendErrorMessage(null, "TablistManager.of_loadPredefinedTeams();", "No tab-list-entries found in the settings-file!");
        }
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This function is used to create the tab-list for each player.
     */
    public void of_createOrUpdateTablist4AllPlayers()
    {
        if(!teams.isEmpty())
        {
            //  Create a scoreboard which will be implemented and then set to the player.
            Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

            //  Register the teams to the given ScoreBoard :)
            of_registerAllTeams2GivenScoreboard(board);

            //  We need to define all players to this player-scoreboard.
            Collection<Spieler> players = main.SPIELERSERVICE._CONTEXT.of_getAllPlayers();

            //  Iterate through all players.
            for(Spieler ds : players)
            {
                //  Iterate through all teams.
                for(Integer key : teams.keySet())
                {
                    String[] attributes = teams.get(key);
                    String label = attributes[0];
                    String permissions = attributes[1];

                    if(ds.of_getPlayer().hasPermission(permissions))
                    {
                        //  Define the team and add the player to it.
                        String teamPattern = "#" + key;
                        Team team = board.getTeam(teamPattern);

                        if(team != null)
                        {
                            Player d = ds.of_getPlayer();
                            d.setPlayerListName(main.MESSAGEBOARD.of_translateMessageWithPlayerStats(label, ds));
                            d.setPlayerListHeader(main.MESSAGEBOARD.of_translateMessageWithPlayerStats(topLine, ds));
                            d.setPlayerListFooter(main.MESSAGEBOARD.of_translateMessageWithPlayerStats(bottomLine, ds));

                            //  Add the player to the team.
                            team.addPlayer(ds.of_getPlayer());
                            break;
                        }
                    }
                }
            }

            //  Set the defined scoreboard to each player.
            for(Spieler ds : players)
            {
                ds.of_getPlayer().setScoreboard(board);
            }
        }
    }

    /**
     * This function is used to register all defined teams to the given
     * scoreboard.
     * @param board The scoreboard which is used to register the teams.
     */
    private void of_registerAllTeams2GivenScoreboard(Scoreboard board)
    {
        //  Iterate through all teams...
        for(Integer key : teams.keySet())
        {
            //  Get the attributes (we need the label) and define the teamPattern.
            String teamPattern = "#" + key;

            if(board.getTeam(teamPattern) == null)
            {
                //  Register the team.
                board.registerNewTeam(teamPattern).setCanSeeFriendlyInvisibles(false);
            }
        }
    }
}
