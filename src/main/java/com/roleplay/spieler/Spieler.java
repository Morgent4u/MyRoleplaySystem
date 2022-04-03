package com.roleplay.spieler;

import com.basis.ancestor.Objekt;
import org.bukkit.entity.Player;

/**
 * @Created 20.03.2022
 * @Author Nihar
 * @Description
 * This class is used to represent a player for
 * this plugin.
 */
public class Spieler extends Objekt
{
    //	Extra-Attribute:
    Player p;
    Object powerObject;

    //	Default-Attribute:
    String name;
    String uuid;
    String moneyType;

    int rangId;
    int jobId;

    //  Money-Attribute:
    int moneyCash;
    int moneyATM;
    int moneyDiff;
    int dbIdOtherPlayer;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    /**
     * Constructor
     * @param p Player instance.
     */
    public Spieler(Player p)
    {
        this.p = p;
        this.name = p.getName();
        this.uuid = p.getUniqueId().toString();
    }

    /* ************************************* */
    /* SETTER // ADDER // REMOVER */
    /* ************************************* */

    public void of_setPowerObject(Object powerObject)
    {
        this.powerObject = powerObject;
    }

    public void of_setName(String name)
    {
        this.name = name;
    }

    public void of_setUUID(String uuid)
    {
        this.uuid = uuid;
    }

    public void of_setMoneyCash(int moneyCash)
    {
        this.moneyCash = moneyCash;
    }

    public void of_setMoneyATM(int moneyATM)
    {
        this.moneyATM = moneyATM;
    }

    public void of_setMoneyDiff(int moneyDiff)
    {
        this.moneyDiff = moneyDiff;
    }

    public void of_setMoneyType(String moneyType)
    {
        this.moneyType = moneyType;
    }

    public void of_setDbIdOtherPlayer(int dbIdOtherPlayer)
    {
        this.dbIdOtherPlayer = dbIdOtherPlayer;
    }

    public void of_setRangId(int rangId)
    {
        this.rangId = rangId;
    }

    public void of_setJobId(int jobId)
    {
        this.jobId = jobId;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public Player of_getPlayer()
    {
        return p;
    }

    public Object of_getPowerObject()
    {
        return powerObject;
    }

    public String of_getName()
    {
        return name;
    }

    public String of_getUUID()
    {
        return uuid;
    }

    public int of_getMoneyCash()
    {
        return moneyCash;
    }

    public int of_getMoneyATM()
    {
        return moneyATM;
    }

    public int of_getMoneyDiff()
    {
        return moneyDiff;
    }

    public String of_getMoneyType()
    {
        return moneyType;
    }

    public int of_getDbIdOtherPlayer()
    {
        return dbIdOtherPlayer;
    }

    public int of_getRangId()
    {
    	return rangId;
    }

    public int of_getJobId()
    {
    	return jobId;
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */
}