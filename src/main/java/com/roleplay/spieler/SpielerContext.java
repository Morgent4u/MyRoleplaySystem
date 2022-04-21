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

            //  File-System...
            Datei user = of_getPlayerFile(ps);

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
                //  Continue because the default values are used.
                ps.of_sendErrorMessage(null, "SpielerContext.of_loadPlayer(Player);", "Error while saving the player data to the file-system.");
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
            //  We use our own system instead...
            if(!main.SETTINGS.of_isUsingVaultMoneySystem())
            {
                ps.of_setMoneyATM(moneyATM);
            }

            ps.of_setMoneyCash(moneyCash);

            //  Add the player instance to the player list.
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
            Datei user = of_getPlayerFile(ps);

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
            user.of_set(sectionKey + ".Money.ATM", ps.of_getMoneyATM());
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

    public Datei of_getPlayerFile(Spieler ps)
    {
        return new Datei(userdataPath + ps.of_getUUID() + ".yml");
    }

    public Datei of_getPlayerFileByUUID(String uuid)
    {
        return new Datei(userdataPath + uuid + ".yml");
    }

    public Datei of_getPlayerIPFile(Spieler ps)
    {
        return new Datei(userdataPath + "IpAddresses//" + ps.of_getPlayerIPAsString() + ".yml");
    }
}