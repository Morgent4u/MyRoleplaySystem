package com.roleplay.spieler;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import org.bukkit.entity.Player;

import java.util.Objects;

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
    String textBlockAttribute;
    String input4Commands = "";

    int rangId;
    int jobId;

    //  Money-Attribute:
    double moneyCash;
    double moneyATM;
    double moneyDiff;

    //  1 = ATM, 0 = CASH
    int moneyType;
    int dbIdOtherPlayer;
    int invId;
    int npcInteractionCounter;
    int positionId;

    boolean ib_playedBefore = true;
    boolean ib_newbie;
    boolean ib_blockedMoving;
    boolean ib_wait4Input;

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

    public void of_setMoneyCash(double moneyCash)
    {
        this.moneyCash = moneyCash;
    }

    public void of_setMoneyATM(double moneyATM)
    {
        this.moneyATM = moneyATM;
    }

    public void of_setMoneyDiff(double moneyDiff)
    {
        this.moneyDiff = moneyDiff;
    }

    public void of_setMoneyType(int moneyType)
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

    public void of_setHasPlayedBefore(boolean playedBefore)
    {
        this.ib_playedBefore = playedBefore;
    }

    public void of_setIsNewbie(boolean newbie)
    {
        this.ib_newbie = newbie;
    }

    public void of_setInvId(int invId)
    {
        this.invId = invId;
    }

    public void of_setTextBlockAttribute(String textBlockName)
    {
        this.textBlockAttribute = textBlockName;
    }

    public void of_setBlockedMoving(boolean bool)
    {
        this.ib_blockedMoving = bool;
    }

    public void of_setInput4Commands(String input)
    {
        input4Commands = input;
    }

    public void of_setWaiting4Input(boolean bool)
    {
        ib_wait4Input = bool;
    }

    public void of_setNPCInteractionCounter(int value)
    {
        npcInteractionCounter = value;
    }

    public void of_setPositionId(int positionId)
    {
        this.positionId = positionId;
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

    public double of_getMoneyCash()
    {
        return moneyCash;
    }

    public double of_getMoneyATM()
    {
        if(main.SETTINGS.of_isUsingVaultMoneySystem())
        {
            return main.VAULT.ECONOMY.getBalance(p);
        }

        return moneyATM;
    }

    public double of_getMoneyDiff()
    {
        return moneyDiff;
    }

    public int of_getMoneyType()
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

    public int of_getInvId()
    {
        return invId;
    }

    public String of_getTextBlockAttribute()
    {
        return textBlockAttribute;
    }

    public String of_getPlayerIPAsString()
    {
        return Objects.requireNonNull(p.getAddress()).getAddress().toString().replace("/", "");
    }

    public String of_getInput4Commands()
    {
        return input4Commands;
    }

    public int of_getNPCInteractionCounter()
    {
        return npcInteractionCounter;
    }

    public int of_getPositionId()
    {
        return positionId;
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    public boolean of_hasPlayedBefore()
    {
        return ib_playedBefore;
    }

    public boolean of_isNewbie()
    {
        return ib_newbie;
    }

    public boolean of_isBlockedMovingEnabled()
    {
        return ib_blockedMoving;
    }

    public boolean of_isWaiting4Input()
    {
        return ib_wait4Input;
    }
}