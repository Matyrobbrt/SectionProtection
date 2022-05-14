package com.matyrobbrt.sectionprotection.api;

import java.util.EnumSet;
import java.util.UUID;

import com.matyrobbrt.sectionprotection.util.EnumSetCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Member {

    public static final Codec<Member> CODEC = RecordCodecBuilder.create(in -> in
        .group(
            new EnumSetCodec<>(Codec.INT.xmap(i -> Permission.values()[i], Permission::ordinal)).fieldOf("permissions")
                .forGetter(Member::getPermissions),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(Member::getUUID))
        .apply(in, Member::new));

    private final EnumSet<Permission> permissions;
    private final UUID uuid;

    public Member(EnumSet<Permission> permissions, UUID uuid) {
        this.permissions = permissions;
        this.uuid = uuid;
    }

    public EnumSet<Permission> getPermissions() {
        return permissions;
    }

    public UUID getUUID() {
        return uuid;
    }

    public enum Permission {

        OWNER, PLACE, BREAK, CLAIM, INTERACT;

    }

}
