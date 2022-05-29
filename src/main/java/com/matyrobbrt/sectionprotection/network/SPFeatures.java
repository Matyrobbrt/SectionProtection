package com.matyrobbrt.sectionprotection.network;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.network.packet.SPPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.annotation.Nullable;

public enum SPFeatures {
    CLAIMED_SYNC("claimed_sync", "1.0.0");

    private final String featureName;
    private final ResourceLocation channelName;
    private final ArtifactVersion currentVersion;
    private final Class<? extends SPPacket>[] packets;

    @SafeVarargs
    SPFeatures(String featureName, String currentVersion, Class<? extends SPPacket>... packets) {
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

    public Class<? extends SPPacket>[] getPackets() {
        return packets;
    }

    public void sendToClient(SPPacket packet, @Nullable ServerPlayer player) {
        if (SPNetwork.isModPresent(player)) {
            SPNetwork.getChannel(this).sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    @Override
    public String toString() {
        return "(" + featureName + " feature, current version " + currentVersion + ")";
    }
}
