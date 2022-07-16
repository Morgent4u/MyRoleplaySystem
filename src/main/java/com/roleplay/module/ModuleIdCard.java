package com.roleplay.module;

import com.roleplay.ancestor.MRSModule;
import org.bukkit.event.Listener;

public class ModuleIdCard extends MRSModule implements Listener
{
    //  Attributes:
    private static final ModuleIdCard instance = new ModuleIdCard();

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    private ModuleIdCard() { }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public static ModuleIdCard of_getInstance()
    {
        return instance;
    }

}
