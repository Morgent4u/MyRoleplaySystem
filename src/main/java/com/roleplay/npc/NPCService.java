package com.roleplay.npc;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.roleplay.spieler.Spieler;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * @Created 17.04.2022
 * @Author Nihar
 * @Description
 * In this class are several functions/methods which are used
 * to handle NPC-Stuff.
 *
 */
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
                //  Create the connection to the player-client to send the packets.
                EntityPlayer entityPlayer = npc.of_getEntityNPC();

                //  Create the Team which is used to hide the NameTag and the DataWatcher to build the full skin of the NPC.
                ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard()).getHandle(), p.getName());
                DataWatcher watcher = new DataWatcher(entityPlayer);

                //  Disallow the NameTagVisibility of the team.
                team.a(ScoreboardTeamBase.EnumNameTagVisibility.b);
                //  DataWatcher is needed to build the full skin of the NPC.
                watcher.a(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte) 127);

                //  This packet adds the NPC to the player.
                connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, entityPlayer));
                //  This packet shows the NPC to the player.
                connection.a(new PacketPlayOutNamedEntitySpawn((EntityHuman) entityPlayer));
                //  This packet corrects the NPC-head rotation.
                connection.a(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (npc.of_getLocation().getYaw() * 256 / 360)));
                //  This packet is used to build the full skin of the NPC.
                connection.a(new PacketPlayOutEntityMetadata(entityPlayer.ae(), watcher, false));

                //  Remove the NameTag of the NPC.
                PacketPlayOutScoreboardTeam teamPacket1 = PacketPlayOutScoreboardTeam.a(team);
                PacketPlayOutScoreboardTeam teamPacket2 = PacketPlayOutScoreboardTeam.a(team, true);
                PacketPlayOutScoreboardTeam teamPacket3 = PacketPlayOutScoreboardTeam.a(team, entityPlayer.getBukkitEntity().getName(), PacketPlayOutScoreboardTeam.a.a);

                //  Send the packets which hide the NameTag of the NPC.
                connection.a(teamPacket1);
                connection.a(teamPacket2);
                connection.a(teamPacket3);

                //  Remove the NPC-Name from the tab-list.
                new BukkitRunnable()
                {

                    @Override
                    public void run()
                    {
                        //  This packet must be sent a little later is used to remove then npc-name from the tab-list.
                        connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, entityPlayer));
                    }

                }.runTaskLater(main.PLUGIN, 20L);
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
     * This function is used to get the texture and signature for the
     * GameProfile.
     * @param skinName SkinName of the player to get the texture and signature for.
     * @return Returns an Array, [0] => Texture and [1] => Signature.
     */
    public String[] of_getTextureAndSignatureBySkinName(String skinName)
    {
        //  Create new GameProfile.
        String texture = "";
        String signature = "";

        try
        {
            //  Get the UUID by using the SkinName aka. PlayerName.
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + skinName);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            String uuid = (new JsonParser()).parse(reader).getAsJsonObject().get("id").getAsString();

            //  Open second url for the properties
            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = (new JsonParser()).parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();

            //  Get the properties...
            texture = property.get("value").getAsString();
            signature = property.get("signature").getAsString();
        }
        catch (Exception ignored) { }

        return new String[] {texture, signature};
    }
}
