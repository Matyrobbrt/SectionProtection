package com.matyrobbrt.sectionprotection.api;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.matyrobbrt.sectionprotection.api.Member.Permission;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Team {

    public static final Codec<Team> CODEC = RecordCodecBuilder.create(
        in -> in.group(Codec.list(Member.CODEC).fieldOf("members").forGetter(Team::getMembers)).apply(in, Team::new));

    private final List<Member> members;
    private final List<Member> membersView;

    public Team(List<Member> members) {
        this.members = members;
        membersView = Collections.unmodifiableList(this.members);
    }

    public Team(UUID owner) {
        this(Lists.newArrayList(new Member(Lists.newArrayList(Permission.values()), owner)));
    }

    public List<Member> getMembers() {
        return membersView;
    }

    public void addMember(Member member) {
        this.members.add(member);
    }

    public void removeMember(Member member) {
        this.members.remove(member);
    }

    @Nullable
    public Member getMember(UUID id) {
        for (final var m : members) {
            if (m.getUUID().equals(id)) {
                return m;
            }
        }
        return null;
    }
}
