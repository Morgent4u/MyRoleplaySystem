package com.roleplay.spieler;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.SimpleFile;
import com.basis.utils.Settings;
import com.roleplay.board.ScoreBoard;
import com.roleplay.objects.CommandSet;
import com.roleplay.objects.TextBlock;
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

            //  Create the database connection if the current
            //  player is the first one which joins the server.
            if(players.isEmpty())
            {
                int rc = -1;

                if(main.SQL != null && Settings.of_getInstance().of_isUsingMySQL())
                {
                    //  If an error occurs we're going to kick the player!
                    rc = main.SQL.of_createConnection();
                }

                if(rc == -1)
                {
                    main.SPIELERSERVICE.of_kickPlayerByUsingTextBlock(ps, new TextBlock("txt_kick_player_no_db_connection", ps), "There was an error while connecting to the database!");
                    Settings.of_getInstance().of_setMaintenanceMode(true);
                    return;
                }
            }

            //  Get the dbId of the player by the uuid.
            int dbId = of_getDbIdByUUID(uuid);

            //  Create a new database entry for the given player.
            if(dbId == -1)
            {
                //  Set the flag, that the player is a newbie :)
                ps.of_setIsNewbie(true);

                //  Generate a database id by the key-control method.
                dbId = main.SQL.of_updateKey("mrs_user");

                //  Kick the player if the id generation fails!
                if(dbId == -1)
                {
                    main.SPIELERSERVICE.of_kickPlayerByDatabaseError(ps, "The key-generation for the user-table failed, we stop here!");
                    return;
                }

                //  Insert the player-information to the table: mrs_user (default user-table).
                String timeStamp = main.SQL.of_getTimeStamp();
                String sqlInsert = "INSERT INTO mrs_user ( user, name, uuid, firstConnection ) VALUES( "+dbId+", '"+p.getName()+"', '"+uuid+"', "+timeStamp+" );";
                boolean lb_sqlOk = main.SQL.of_run_update_suppress(sqlInsert);

                if(lb_sqlOk)
                {
                    //  Create a table entry for the player in the table: mrs_user_data
                    sqlInsert = "INSERT INTO mrs_user_data ( user, money, atm ) VALUES( "+dbId+", "+Settings.of_getInstance().of_getDefaultMoneyCash()+", "+Settings.of_getInstance().of_getDefaultMoneyATM()+" );";
                    lb_sqlOk = main.SQL.of_run_update_suppress(sqlInsert);
                }

                //  Handle an insert-error.
                if(!lb_sqlOk)
                {
                    //  If the player information could not be inserted to the database-table.
                    main.SPIELERSERVICE.of_kickPlayerByDatabaseError(ps, "Could not insert the player-information to the given database-table. Player dbId: " + dbId);
                    return;
                }

                //  If we use the vault-economy-system, we need to add to the current-money-account
                //  the default-money from the mrs-system.
                if(Settings.of_getInstance().of_isUsingVaultMoneySystem())
                {
                    //  Set the money to the player instance.
                    main.SPIELERSERVICE.of_editPlayerMoney(ps, "atm", "add", Settings.of_getInstance().of_getDefaultMoneyATM());
                    main.SPIELERSERVICE.of_editPlayerMoney(ps, "cash", "add", Settings.of_getInstance().of_getDefaultMoneyCash());
                }
            }

            //  Retrieve player-data from the database by using a specified view.
            String sql = "SELECT user, rang, whitelist_yn, job, money, atm, dataProtection_yn, ipAddress FROM mrs_v_user WHERE uuid = '"+uuid+"';";
            ResultSet result = main.SQL.of_getResultSet_suppress(sql, true);

            //  Define some attributes.
            int rangId = -1;
            int jobId = -1;
            double moneyCash = -1;
            double moneyAtm = -1;
            boolean acceptedDataProtection = false;

            if(result != null)
            {
                try
                {
                    //  Get data.
                    dbId = result.getInt("user");

                    //  Get numbers.
                    rangId = result.getInt("rang");
                    jobId = result.getInt("job");
                    moneyCash = result.getDouble("money");
                    moneyAtm = result.getInt("atm");

                    //  Get booleans.
                    acceptedDataProtection = result.getString("dataProtection_yn").equals("Y");
                }
                catch (Exception ignored)
                {
                    main.SPIELERSERVICE.of_kickPlayerByDatabaseError(ps, "There was an error while fetching the data from the view. SpielerContext.of_loadPlayer(); #10");
                    return;
                }
            }

            //  We need to set the dbId to use it for the double-ip-check!
            ps.of_setObjectId(dbId);

            //  Set the defined attributes from the database to the current player-instance.
            ps.of_setRangId(rangId);
            ps.of_setJobId(jobId);

            // If the server is not using the vault-economy-system, set the money to the player instance.
            // We use our own money-system instead...
            if(!Settings.of_getInstance().of_isUsingVaultMoneySystem())
            {
                ps.of_setMoneyATM(moneyAtm);
            }

            ps.of_setMoneyCash(moneyCash);

            //  Add the current player to the player-list.
            players.put(p.getName(), ps);

            //  After this is done, we load the scoreBoard to the player.
            //  Check if scoreBoard is null because in the reload-process it's null!
            if(Settings.of_getInstance().of_isUsingScoreboard() && ScoreBoard.of_getInstance() != null)
            {
                ScoreBoard.of_getInstance().of_sendScoreboard2Player(ps);
            }

            //  When the player does not have accepted the data-protection we disallow moving and send a text-block.
            if(!acceptedDataProtection && Settings.of_getInstance().of_isUsingDataProtection())
            {
                //  Disallow the player to move.
                ps.of_setBlockedMoving(true);

                //  Send the textBlock for the dataProtection agreement.
                new CommandSet(new String[] {"TEXTBLOCK=txt_dataprotection"}, ps).of_executeAllCommands();
            }
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

            //  Check for the online players, if no one is left we close the database connection.
            if(players.isEmpty())
            {
                if(main.SQL != null && Settings.of_getInstance().of_isUsingMySQL() && main.SQL.of_isConnected())
                {
                    main.SQL.of_closeConnection();
                }
            }
        }
    }

    /* ************************************* */
    /* OBJEKT-ANWEISUNGEN */
    /* ************************************* */

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
            //  File-System...
            SimpleFile user = of_getPlayerFile(ps);

            //  System-Stuff
            String sectionKey = "System";
            user.of_set(sectionKey + ".RangId", ps.of_getRangId());
            user.of_set(sectionKey + ".JobId", ps.of_getJobId());

            //  Player-Stuff
            sectionKey = "Player";

            //  We set the name again cause sometimes player change their name.
            user.of_set(sectionKey + ".Name", ps.of_getName());

            //  Set last connection date and the money-attributes.
            user.of_set(sectionKey + ".LastConnection", Sys.of_getTimeStamp(true));

            //  If the server is not reloading and the vault-economy-system is enabled, we save the money.
            if(!main.of_isReloading() && Settings.of_getInstance().of_isUsingVaultMoneySystem())
            {
                user.of_set(sectionKey + ".Money.ATM", ps.of_getMoneyATM());
            }

            user.of_set(sectionKey + ".Money.Cash", ps.of_getMoneyCash());

            return user.of_save("SpielerContext.of_loadPlayer(Player);");
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

    public Collection<Spieler> of_getAllPlayers()
    {
        return players.values();
    }

    public Spieler of_getPlayer(String playerName)
    {
        return players.get(playerName);
    }

    public Spieler of_getPlayerById(int objectId)
    {
        for(Spieler ps : players.values())
        {
            if(ps.of_getObjectId() == objectId)
            {
                return ps;
            }
        }

        return null;
    }

    public SimpleFile of_getPlayerFile(Spieler ps)
    {
        return new SimpleFile(userdataPath + ps.of_getUUID() + ".yml");
    }

    public SimpleFile of_getPlayerFileByUUID(String uuid)
    {
        return new SimpleFile(userdataPath + uuid + ".yml");
    }

    public SimpleFile of_getPlayerIPFile(Spieler ps)
    {
        return new SimpleFile(userdataPath + "IpAddresses//" + ps.of_getPlayerIPAsString() + ".yml");
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    /**
     * This method is used to get the db-id from the given uuid.
     * @param uuid A players uuid.
     * @return N = DbId of the given uuid, -1 = If the uuid has no database-entry!
     */
    private int of_getDbIdByUUID(String uuid)
    {
        return Sys.of_getString2Int(main.SQL.of_getRowValue_suppress("SELECT user FROM mrs_v_user WHERE uuid = '"+uuid+"';", "user"));
    }
}