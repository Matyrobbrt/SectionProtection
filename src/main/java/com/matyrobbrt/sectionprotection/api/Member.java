package com.matyrobbrt.sectionprotection.api;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Member {

    public static final Codec<Member> CODEC = RecordCodecBuilder.create(in -> in
        .group(
            Codec.list(Codec.INT.xmap(i -> Permission.values()[i], Permission::ordinal)).fieldOf("permissions")
                .forGetter(Member::getPermissions),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(Member::getUUID))
        .apply(in, Member::new));

    private final List<Permission> permissions;
    private final UUID uuid;

    public Member(List<Permission> permissions, UUID uuid) {
        this.permissions = permissions;
        this.uuid = uuid;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public UUID getUUID() {
        return uuid;
    }

    public enum Permission {

        OWNER, PLACE, BREAK, CLAIM, INTERACT;

        public static final List<Permission> ALL = ImmutableList.copyOf(Permission.values());
        public static final List<Permission> DEFAULT = List.of(Permission.INTERACT);

    }

}
