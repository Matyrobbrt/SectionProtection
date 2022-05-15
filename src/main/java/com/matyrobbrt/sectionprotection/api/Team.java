package com.matyrobbrt.sectionprotection.api;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.matyrobbrt.sectionprotection.api.Member.Permission;
import com.matyrobbrt.sectionprotection.util.codec.Codecs;
import com.matyrobbrt.sectionprotection.world.TeamRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.chunk.LevelChunk;

public class Team {

    public static final Codec<Team> CODEC = RecordCodecBuilder
        .create(
            in -> in
                .group(
                    Codecs.mutableList(Member.CODEC).fieldOf("members")
                        .forGetter(t -> List.copyOf(t.getMembers().values())),
                    Codecs.mutableList(ChunkData.CODEC)
                        .fieldOf("chunks").forGetter(t -> t.claimedChunks),
                    Codecs.enumSet(Codecs.forEnum(Permission.class)).fieldOf("default_permission")
                        .forGetter(Team::getDefaultPermissions))
                .apply(in, Team::new));

    private final Map<UUID, Member> members;

    private final List<ChunkData> claimedChunks;
    private final EnumSet<Permission> defaultPermissions;

    private Team(List<Member> members, List<ChunkData> claimedChunks, EnumSet<Permission> defaultPermissions) {
        this.members = members.stream().collect(Collectors.toMap(Member::getUUID, Function.identity()));
        this.claimedChunks = claimedChunks;
        this.defaultPermissions = defaultPermissions;
    }

    public Team(UUID owner) {
        this(Lists.newArrayList(new Member(EnumSet.allOf(Permission.class), owner)), new ArrayList<>(),
            EnumSet.of(Permission.INTERACT));
    }

    public Map<UUID, Member> getMembers() {
        return members;
    }

    public void addMember(Member member) {
        this.members.put(member.getUUID(), member);
    }

    public void removeMember(Member member) {
        this.members.remove(member.getUUID());
    }

    public EnumSet<Permission> getDefaultPermissions() {
        return defaultPermissions;
    }

    @Nullable
    public Member getMember(UUID id) {
        return members.get(id);
    }

    public static void claim(String teamId, LevelChunk chunk) {
        final var reg = TeamRegistry.get(chunk.getLevel().getServer());
        final var team = reg.getTeam(teamId);
        team.claimedChunks.add(new ChunkData(chunk));
        chunk.getCapability(ClaimedChunk.CAPABILITY).ifPresent(claimed -> claimed.setOwnerTeam(teamId));
        reg.setDirty(true);
    }
}
