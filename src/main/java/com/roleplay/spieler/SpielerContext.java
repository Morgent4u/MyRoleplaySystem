package com.roleplay.spieler;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Datei;
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
    private HashMap<String, Spieler> players = new HashMap<>();
    private final String userdataPath = Sys.of_getMainFilePath() + "Userdata//";

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
        //  Check if the player is already registered.
        if(!players.containsKey(p.getName()))
        {
            //  Create a new player instance.
            Spieler ps = new Spieler(p);
            String uuid = p.getUniqueId().toString();
            int dbId = players.size() + 1;
            int rangId = -1;
            int jobId = -1;
            double moneyATM = -1;
            double moneyCash = -1;

            if(main.SETTINGS.of_isUsingMySQL())
            {
                // Check the player count.
                if(players.size() == 0 && !main.SQL.of_isConnected())
                {
                    //  Connect to the database.
                    main.SQL.of_createConnection();
                }

                //	Does the player already exist?
                String sqlStatement = "SELECT user FROM mrs_user WHERE uuid = '"+uuid+"';";
                dbId = Sys.of_getString2Int(main.SQL.of_getRowValue_suppress(sqlStatement, "user"));

                if(dbId == -1)
                {
                    //  Create new database user.
                    dbId = of_createNewPlayerEntry2Database(p);
                    ps.of_setHasPlayedBefore(false);
                }

                //  Load player data via. database request.
                if(dbId != -1)
                {
                    String sqlSelect = "SELECT * FROM mrs_user WHERE user = "+dbId+";";
                    ResultSet result = main.SQL.of_getResultSet_suppress(sqlSelect, true);

                    try
                    {
                        //  Set data from the ResultSet to the player instance.
                        rangId = result.getInt("rang");
                        jobId = result.getInt("job");
                        moneyATM = result.getDouble("money");
                        moneyCash = result.getDouble("atm");
                    }
                    catch (Exception e)
                    {
                        of_sendErrorMessage(e, "SpielerContext.of_loadPlayer(Player);", "Error while loading the player data from the database.");
                    }
                }
                //  Switch to the file-system.
                else
                {
                    //  Switch to the fileSystem because of a database error.
                    of_swap2FileSystem();

                    //  File-System...
                    of_loadPlayer(p);
                    return;
                }
            }
            else
            {
                //  File-System...
                Datei user = new Datei(userdataPath + uuid + ".yml");

                //  Set playerHasPlayedBefore state.
                if(!user.of_fileExists())
                {
                    ps.of_setHasPlayedBefore(false);
                }

                //  Default-Stuff
                String sectionKey = "System";
                rangId = user.of_getSetInt(sectionKey + ".RangId", 999);
                jobId = user.of_getSetInt(sectionKey + ".JobId", 999);

                //  Player-Stuff
                sectionKey = "Player";
                user.of_set(sectionKey + ".Name", p.getName());
                user.of_set(sectionKey + ".FirstConnection", Sys.of_getTimeStamp(true));
                user.of_set(sectionKey + ".LastConnection", Sys.of_getTimeStamp(true));
                moneyATM = user.of_getSetDouble(sectionKey + ".Money.ATM", main.SETTINGS.of_getDefaultMoneyATM());
                moneyCash = user.of_getSetDouble(sectionKey + ".Money.Cash", main.SETTINGS.of_getDefaultMoneyCash());

                int rc = user.of_save("SpielerContext.of_loadPlayer(Player);");

                if(rc != 1)
                {
                    ps.of_sendErrorMessage(null, "SpielerContext.of_loadPlayer(Player);", "Error while saving the player data to the file-system.");
                    return;
                }
            }

            // Check if the player is new and the vault-economy-system is enabled.
            if(!ps.of_hasPlayedBefore() && main.SETTINGS.of_isUsingVaultMoneySystem())
            {
                //  Set the money to the player instance.
                main.SPIELERSERVICE.of_editPlayerMoney(ps, "atm", "add", moneyATM);
                main.SPIELERSERVICE.of_editPlayerMoney(ps, "cash", "add", moneyCash);
            }

            //  Set player data to the player instance.
            ps.of_setRangId(rangId);
            ps.of_setJobId(jobId);

            // If the server is not using the vault-economy-system, set the money to the player instance.
            if(!main.SETTINGS.of_isUsingVaultMoneySystem())
            {
                ps.of_setMoneyATM(moneyATM);
            }

            ps.of_setMoneyCash(moneyCash);

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

            //  Check player count.
            if(players.size() == 0 && main.SETTINGS.of_isUsingMySQL())
            {
                //  Disconnect from the database.
                main.SQL.of_closeConnection();
            }
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
            String sqlInsert = "INSERT INTO mrs_user (user, name, uuid, atm, money, firstConnection, lastConnection) VALUES ("+dbId+", '"+p.getName()+"', '"+p.getUniqueId()+"', "+main.SETTINGS.of_getDefaultMoneyATM()+", "+main.SETTINGS.of_getDefaultMoneyCash()+", NOW(), NOW());";
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
            if(main.SETTINGS.of_isUsingMySQL())
            {
                //	Update-Statement:
                String sqlUpdate = "UPDATE mrs_user SET name = '"+ps.of_getName()+"'" +
                        ", rang = "+ ps.of_getRangId() +
                        ", job = "+ ps.of_getJobId() +
                        ", atm = "+ ps.of_getMoneyATM() +
                        ", money = "+ ps.of_getMoneyCash() +
                        ", lastConnection = NOW()" +
                        ", WHERE mrs_user.user = " + ps.of_getObjectId() + ";";

                // Error handling in the run_update function.
                boolean bool = main.SQL.of_run_update(sqlUpdate);

                if(!bool)
                {
                    //  Switch to the fileSystem because of a database error.
                    of_swap2FileSystem();
                    return of_savePlayer(ps);
                }

                return 1;
            }
            //  Store player data into the file-system.
            else
            {
                //  File-System...
                Datei user = new Datei(userdataPath + ps.of_getUUID() + ".yml");

                //  Default-Stuff
                String sectionKey = "System";
                user.of_set(sectionKey + ".RangId", ps.of_getRangId());
                user.of_set(sectionKey + ".JobId", ps.of_getJobId());

                //  Player-Stuff
                sectionKey = "Player";
                //  We set the name again cause sometimes player change their name.
                user.of_set(sectionKey + ".Name", ps.of_getName());

                //  Set last connection date and the money-stuff.
                user.of_set(sectionKey + ".LastConnection", Sys.of_getTimeStamp(true));
                user.of_set(sectionKey + ".Money.ATM", ps.of_getMoneyATM());
                user.of_set(sectionKey + ".Money.Cash", ps.of_getMoneyCash());

                return user.of_save("SpielerContext.of_loadPlayer(Player);");
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

    /**
     * This function is used to switch to the file-system.
     */
    private void of_swap2FileSystem()
    {
        // Disable the MySQL connection and switch to the file-system.
        main.SETTINGS.of_setUseMySQL(false);

        if(main.SQL != null && main.SQL.of_isConnected())
        {
            main.SQL.of_closeConnection();
            main.SQL = null;
        }
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