package com.roleplay.spieler;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import org.bukkit.entity.Player;

/**
 * @Created 21.03.2022
 * @Author Nihar
 * @Description
 * This class creates for every player an
 * object-instance from type Spieler.
 * Further this class is used to save player data into
 * the database or the file-system.
 */
public class SpielerContext extends Objekt
{
    //	Attribute:
    private HashMap<String, Spieler> players = new HashMap<String, Spieler>();

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    /**
     * This function registers a player to this plugin.
     * The player data will be load by using the database.
     * @param p Player instance.
     */
    public void of_loadPlayer(Player p)
    {
        if(!players.containsKey(p.getName()))
        {
            Spieler ps = new Spieler(p);
            String uuid = p.getUniqueId().toString();
            int dbId = -1;

            //	Gibt es den Spieler bereits?
            String sqlStatement = "SELECT user FROM mrs_user WHERE uuid = '"+uuid+"';";
            dbId = Sys.of_getString2Int(main.SQL.of_getRowValue_suppress(sqlStatement, "user"));

            if(dbId == -1)
            {
                //  Create new database user.
                dbId = of_createNewPlayerEntry2Database(p);
            }

            //  Load player data via. database request.
            if(dbId != -1)
            {
                String sqlSelect = "SELECT * FROM mrs_user WHERE user = "+dbId+";";
                ResultSet result = main.SQL.of_getResultSet_suppress(sqlSelect, true);

                try
                {
                    //  Set data from the ResultSet to the player instance.
                    ps.of_setRangId(result.getInt("rang"));
                    ps.of_setJobId(result.getInt("job"));
                    ps.of_setMoneyATM(result.getInt("money"));
                    ps.of_setMoneyCash(result.getInt("atm"));
                }
                catch (Exception e)
                {
                    of_sendErrorMessage(e, "SpielerContext.of_loadPlayer(Player);", "Error while loading the player data from the database.");
                }
            }

            ps.of_setObjectId(dbId);
            players.put(p.getName(), ps);
        }
    }

    /* ************************************* */
    /* UNLOADER */
    /* ************************************* */

    /**
     * This function saves the player instance into the database and removes the player form the
     * player-list.
     * @param ps Player instance.
     */
    public void of_unloadPlayer(Spieler ps)
    {
        if(ps != null)
        {
            of_savePlayer(ps);
            players.remove(ps.of_getName());
        }
    }

    /* ************************************* */
    /* OBJEKT-ANWEISUNGEN */
    /* ************************************* */

    /**
     * This function is used to add a new player to the database.
     * @param p Player instance.
     * @return N = SUCCESS, -1 = ERROR
     */
    private int of_createNewPlayerEntry2Database(Player p)
    {
        int dbId = main.SQL.of_updateKey("user");

        if(dbId > 0)
        {
            String sqlInsert = "INSERT INTO mrs_user (user, name, uuid, firstConnection, lastConnection) VALUES ("+dbId+", '"+p.getName()+"', '"+p.getUniqueId().toString()+"', NOW(), NOW());";
            boolean bool = main.SQL.of_run_update(sqlInsert);

            if(bool)
            {
                return dbId;
            }
        }

        return -1;
    }

    /**
     * This function stores the player data into the database or
     * the file-system. It also swaps to the file-system if no
     * database connection is given.
     * @param ps Own instance of the player (Spieler).
     * @return 1 = SUCCESS, -1 = ERROR
     */
    public int of_savePlayer(Spieler ps)
    {
        if(ps != null)
        {
            //	Update-Statement:
            String sqlUpdate = "UPDATE mrs_user SET name = '"+ps.of_getName()+"'" +
                    ", rang = "+ ps.of_getRangId() +
                    ", job = "+ ps.of_getJobId() +
                    ", atm = "+ ps.of_getMoneyATM() +
                    ", money = "+ ps.of_getMoneyCash() +
                    ", lastConnection = NOW()" +
                    ", WHERE mrs_user.user = " + ps.of_getObjectId() + ";";

            main.SQL.of_run_update_suppress(sqlUpdate);
            return 1;
        }

        return -1;
    }

    /**
     * Overload of the function of_savePlayer(Spieler).
     * @param playerName Playername
     * @return 1 = SUCCESS, -1 = ERROR
     */
    public int of_savePlayer(String playerName)
    {
        return of_savePlayer(players.get(playerName));
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public Collection<Spieler> of_getAllSpieler()
    {
        return players.values();
    }

    public Spieler of_getSpieler(String playerName)
    {
        return players.get(playerName);
    }
}