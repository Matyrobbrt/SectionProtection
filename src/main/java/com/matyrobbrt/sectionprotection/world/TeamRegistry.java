package com.matyrobbrt.sectionprotection.world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.Team;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class TeamRegistry extends SavedData {

    public static final int CURRENT_VERSION = 0;

    private final Map<String, Team> teams = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag tag) {
        final var teamsNbt = new CompoundTag();
        teams.forEach((id, team) -> teamsNbt.put(id, Team.CODEC.encodeStart(NbtOps.INSTANCE, team).get().orThrow()));
        tag.put("teams", teamsNbt);
        tag.putInt("version", CURRENT_VERSION);
        return tag;
    }

    public void addTeam(String id, Team team) {
        this.teams.put(id, team);
        setDirty(true);
    }

    @Nullable
    public Team getTeam(String id) {
        return teams.get(id);
    }

    //@formatter:off
    public List<String> getTeams(UUID member) {
        return teams.entrySet()
            .stream()
            .filter(e -> e.getValue().getMembers().containsKey(member))
            .map(Map.Entry::getKey)
            .toList();
    }
    
    public Map<String, Team> getAllTeams() {
        return teams;
    }

    public static TeamRegistry load(CompoundTag nbt) {
        final var reg = new TeamRegistry();
        final var teams = nbt.getCompound("teams");
        teams.getAllKeys().forEach(
            id -> reg.addTeam(id, Team.CODEC.decode(NbtOps.INSTANCE, teams.get(id)).get().orThrow().getFirst()));
        return reg;
    }

    public static TeamRegistry get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TeamRegistry::load, TeamRegistry::new,
            SectionProtection.MOD_ID + "_teams");
    }

}
