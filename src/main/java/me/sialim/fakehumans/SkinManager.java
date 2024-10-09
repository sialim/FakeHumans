package me.sialim.fakehumans;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sialim.fakehumans.traits.FHHomunculusTrait;
import me.sialim.fakehumans.traits.FHOwnerTrait;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SkinManager {
    private final FakeHumans plugin;

    public SkinManager(FakeHumans plugin) {
        this.plugin = plugin;
    }

    public void setSkinFromUsername(NPC npc, String username) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String uuidUrl = "https://api.mojang.com/users/profiles/minecraft/" + username;
                HttpURLConnection uuidConnection = (HttpURLConnection) new URL(uuidUrl).openConnection();
                uuidConnection.setRequestMethod("GET");

                if (uuidConnection.getResponseCode() != 200) {
                    throw new RuntimeException("Failed to get UUID for username: " + username);
                }

                // Read UUID response correctly
                BufferedReader uuidReader = new BufferedReader(new InputStreamReader(uuidConnection.getInputStream()));
                StringBuilder uuidResponseBuilder = new StringBuilder();
                String line;
                while ((line = uuidReader.readLine()) != null) {
                    uuidResponseBuilder.append(line);
                }
                String uuidResponse = uuidResponseBuilder.toString();

                JsonObject uuidJson = JsonParser.parseString(uuidResponse).getAsJsonObject();
                String uuid = uuidJson.get("id").getAsString();

                String skinUrl = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
                HttpURLConnection skinConnection = (HttpURLConnection) new URL(skinUrl).openConnection();
                skinConnection.setRequestMethod("GET");

                if (skinConnection.getResponseCode() != 200) {
                    throw new RuntimeException("Failed to get skin information for UUID: " + uuid);
                }

                // Read skin response correctly
                BufferedReader skinReader = new BufferedReader(new InputStreamReader(skinConnection.getInputStream()));
                StringBuilder skinResponseBuilder = new StringBuilder();
                while ((line = skinReader.readLine()) != null) {
                    skinResponseBuilder.append(line);
                }
                String skinResponse = skinResponseBuilder.toString();

                JsonObject skinJson = JsonParser.parseString(skinResponse).getAsJsonObject();
                JsonObject properties = skinJson.getAsJsonArray("properties").get(0).getAsJsonObject();

                String texture = properties.get("value").getAsString();
                String signature = properties.get("signature").getAsString();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                    skinTrait.setTexture(texture, signature);
                    skinTrait.setSkinPersistent(username, skinTrait.getSignature(), skinTrait.getTexture());
                    if (npc.hasTrait(FHHomunculusTrait.class)) {
                        plugin.setNPCSize(npc, 0.65f);
                    }
                });
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to set skin for " + username + ": " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                    skinTrait.setSkinPersistent(Bukkit.getPlayer(npc.getOrAddTrait(FHOwnerTrait.class).getOwner()));
                });
            }
        });
    }
}
