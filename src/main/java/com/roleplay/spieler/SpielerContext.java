package com.roleplay.spieler;

import java.util.Collection;
import java.util.HashMap;

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
public class SpielerContext
{
    //	Attribute:
    private HashMap<String, Spieler> players = new HashMap<String, Spieler>();

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public SpielerContext() { }

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    /**
     * This function registers a player to this plugin.
     * The player data will be load by using the database or the
     * file-system.
     * @param p Player instance.
     */
    public void of_loadPlayer(Player p)
    {
        if(!players.containsKey(p.getName()))
        {
            Spieler ps = new Spieler(p);
            String uuid = p.getUniqueId().toString();
            int dbId = -1;

            if(main.SETTINGS.of_isUsingMySQL())
            {
                //	Gibt es den Spieler bereits?
                String sqlStatement = "SELECT user FROM mrs_user WHERE uuid = '"+uuid+"';";
                dbId = Sys.of_getString2Int(main.SQL.of_getRowValue_suppress(sqlStatement, "user"));

                if(dbId == -1)
                {
                    //	Neuen Nutzer anlegen...
                    dbId = of_createNewPlayerEntry2Database(p);
                }

                //	Spieler-Inhalte laden...
                if(dbId != -1)
                {
                    String sqlSelect = "SELECT defaultLanguage FROM mlc_user WHERE user = "+dbId+";";
                    // defaultLanguage = main.SQL.of_getRowValue_suppress(sqlSelect, "defaultLanguage");
                }
            }

            ps.of_setTargetId(dbId);
            players.put(p.getName(), ps);
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
            String sqlInsert = "INSERT INTO mrs_user (user, uuid) VALUES ("+dbId+", '"+p.getUniqueId().toString()+"');";
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
            //	Datenbank oder FileSystem?
            if(main.SETTINGS.of_isUsingMySQL())
            {
                /*
                //	Update-Statement:
                String sqlUpdate = "UPDATE mlc_user SET name = '"+ps.of_getName()+"', defaultLanguage = '"+ps.of_getDefaultLanguage()+"' WHERE mlc_user.user = " + ps.of_getTargetId() + ";";
                boolean bool = main.SQL.of_run_update_suppress(sqlUpdate);

                //	Bei einem Fehler, wechsel zum FileSystem!
                if(!bool)
                {
                    //	Verbindung beenden...
                    main.SETTINGS.of_setUseMySQL(false);
                    main.SQL.of_closeConnection();

                    //	Switch zum FileSystem
                    return of_savePlayer(ps);
                }
                 */

                return 1;
            }
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