package com.roleplay.npc;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.roleplay.spieler.Spieler;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class NPCService extends Objekt
{
    //  Attributes:
    public NPCContext _CONTEXT;

    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    public NPCService()
    {
        _CONTEXT = new NPCContext();
    }

    @Override
    public int of_load()
    {
        return _CONTEXT.of_load();
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This function is used to load all NPCs for the given player.
     * @param ps The player to load the NPCs for.
     */
    public void of_showAllNPCs2Player(Spieler ps)
    {
        if(_CONTEXT.of_getLoadedNPCsSize() > 0)
        {
            Player p = ps.of_getPlayer();

            //  Create the connection and the packet.
            PlayerConnection connection = ((CraftPlayer) p).getHandle().b;

            for(NPC npc : _CONTEXT.of_getLoadedNPCs())
            {
                EntityPlayer entityPlayer = npc.of_getEntityNPC();

                //  Send packet...
                connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, entityPlayer));
                connection.a(new PacketPlayOutNamedEntitySpawn(entityPlayer));
                connection.a(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (npc.of_getLocation().getYaw() * 256 / 360)));
            }
        }
    }

    /**
     * This function is used to remove all NPCs for the given player.
     * @param ps The player to remove the NPCs from.
     */
    public void of_removeAllNPCsFromPlayer(Spieler ps)
    {
        if(_CONTEXT.of_getLoadedNPCsSize() > 0)
        {
            Player p = ps.of_getPlayer();

            //  Create the connection and the packet.
            PlayerConnection connection = ((CraftPlayer) p).getHandle().b;

            for(NPC npc : _CONTEXT.of_getLoadedNPCs())
            {
                //  Send the destory-packet.
                connection.a(new PacketPlayOutEntityDestroy(npc.of_getEntityNPC().ae()));
            }
        }
    }

    /**
     * This function is used to load all NPCs for each player.
     */
    public void of_showAllNPCs2AllOnlinePlayers()
    {
        if(_CONTEXT.of_getLoadedNPCsSize() > 0)
        {
            for(Spieler ps : main.SPIELERSERVICE._CONTEXT.of_getAllPlayers())
            {
                of_showAllNPCs2Player(ps);
            }
        }
    }

    /**
     * This function is used to remove all NPCs for each player.
     */
    public void of_removeAllNPCsFromAllOnlinePlayers()
    {
        if(_CONTEXT.of_getLoadedNPCsSize() > 0)
        {
            for(Spieler ps : main.SPIELERSERVICE._CONTEXT.of_getAllPlayers())
            {
                of_removeAllNPCsFromPlayer(ps);
            }
        }
    }

    /**
     * This function is used to create a GameProfile by using the SkinName.
     * If no SkinName is given it only creates the GameProfile.
     * @param skinName The SkinName of the GameProfile. This can be null.
     * @return The GameProfile.
     */
    public GameProfile of_createGameProfile(String skinName)
    {
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");

        if(skinName != null)
        {
            try
            {
                //  Get information via. API for the GameProfile.
                HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", skinName)).openConnection();

                //  Check if the connection is open.
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                {
                    ArrayList<String> lines = new ArrayList<>();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    reader.lines().forEach(lines::add);

                    //  Extract the properties.
                    String reply = String.join(" ",lines);
                    int indexOfValue = reply.indexOf("\"value\": \"");
                    int indexOfSignature = reply.indexOf("\"signature\": \"");
                    String skin = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
                    String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

                    //  Set the skin.
                    gameProfile.getProperties().put("textures", new Property("textures", skin, signature));
                }

            } catch (IOException ignored) { }
        }

        return gameProfile;
    }
}
