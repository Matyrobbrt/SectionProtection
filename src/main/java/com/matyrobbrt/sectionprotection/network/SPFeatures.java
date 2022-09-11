package com.matyrobbrt.sectionprotection.network;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.network.packet.SPPacket;
import com.matyrobbrt.sectionprotection.network.packet.SyncChunkPacket;
import com.matyrobbrt.sectionprotection.network.packet.SyncChunksPacket;
import com.matyrobbrt.sectionprotection.network.packet.SyncTeamPacket;
import com.matyrobbrt.sectionprotection.network.packet.claiming.UnclaimChunkPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.annotation.Nullable;
import java.util.function.Function;

public enum SPFeatures {
    CLAIM_SYNC("claim_sync", "1.0.0", new PacketData<>(
            SyncChunksPacket.class, SyncChunksPacket::decode, NetworkDirection.PLAY_TO_CLIENT
    ), new PacketData<>(
            SyncChunkPacket.class, SyncChunkPacket::decode, NetworkDirection.PLAY_TO_CLIENT
    )),

    TEAM_SYNC("team_sync", "1.0.0", new PacketData<>(
            SyncTeamPacket.class, SyncTeamPacket::decode, NetworkDirection.PLAY_TO_CLIENT
    )),

    CLAIM_PACKETS("claim_packets", "1.0.0", new PacketData<>(
            UnclaimChunkPacket.class, UnclaimChunkPacket::decode, NetworkDirection.PLAY_TO_SERVER
    ));

    private final String featureName;
    private final ResourceLocation channelName;
    private final ArtifactVersion currentVersion;
    private final PacketData<?>[] packets;

    @SafeVarargs
    SPFeatures(String featureName, String currentVersion, PacketData<? extends SPPacket>... packets) {
        this.featureName = featureName;
        this.channelName = new ResourceLocation(SectionProtection.MOD_ID, featureName);
        this.currentVersion = new DefaultArtifactVersion(currentVersion);
        this.packets = packets;
    }

    public String featureName() {
        return featureName;
    }

    public ResourceLocation channelName() {
        return channelName;
    }

    public ArtifactVersion currentVersion() {
        return currentVersion;
    }

    public PacketData<? extends SPPacket>[] getPackets() {
        return packets;
    }

    public void sendToClient(SPPacket packet, @Nullable ServerPlayer player) {
        if (clientCanReceive(player)) {
            //noinspection ConstantConditions
            SPNetwork.getChannel(this).sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public boolean clientCanReceive(@Nullable ServerPlayer player) {
        if (SPNetwork.isModPresent(player)) {
            final var targetVersion = SPNetwork.getFeatureVersion(player.connection.getConnection(), this);
            return currentVersion.compareTo(targetVersion) <= 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + featureName + " feature, current version " + currentVersion + ")";
    }

    record PacketData<T extends SPPacket>(Class<T> clazz, Function<FriendlyByteBuf, T> decoder, @Nullable NetworkDirection direction) {}
}
