package com.matyrobbrt.sectionprotection.network;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.network.packet.SPPacket;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class SPNetwork {

    public static final ResourceLocation EXISTENCE_CHANNEL_NAME = new ResourceLocation(SectionProtection.MOD_ID, "exists");
    public static final EventNetworkChannel EXISTENCE_CHANNEL = NetworkRegistry.ChannelBuilder
            .named(EXISTENCE_CHANNEL_NAME)
            .networkProtocolVersion(() -> "yes")
            .clientAcceptedVersions(str -> true)
            .serverAcceptedVersions(str -> true)
            .eventNetworkChannel();

    private static final Map<SPFeatures, SimpleChannel> FEATURE_CHANNELS = new EnumMap<>(SPFeatures.class);

    public static void register() {
        for (final var feature : SPFeatures.values()) {
            int index = 0;
            final var channel = NetworkRegistry.ChannelBuilder
                    .named(feature.channelName())
                    .networkProtocolVersion(feature.currentVersion()::toString)
                    .clientAcceptedVersions(str -> true)
                    .serverAcceptedVersions(str -> true)
                    .simpleChannel();

            for (final var pkt : feature.getPackets()) {
                SPPacket.register(channel, index++, pkt);
            }

            FEATURE_CHANNELS.put(feature, channel);
        }
    }

    public static SimpleChannel getChannel(SPFeatures feature) {
        @Nullable final SimpleChannel channel = FEATURE_CHANNELS.get(feature);
        return Objects.requireNonNull(channel);
    }

    public static boolean isModPresent(@Nullable ServerPlayer client) {
        return client != null && isModPresent(client.connection.getConnection());
    }

    public static boolean isModPresent(Connection connection) {
        return EXISTENCE_CHANNEL.isRemotePresent(connection);
    }

    public static ArtifactVersion getFeatureVersion(Connection connection, SPFeatures feature) {
        @Nullable final ArtifactVersion featureVersion = getFeatureVersionIfExists(connection, feature);
        return featureVersion != null ? featureVersion : new DefaultArtifactVersion("0.0.0");
    }

    @Nullable
    public static ArtifactVersion getFeatureVersionIfExists(Connection connection, SPFeatures feature) {
        @Nullable final String channelVersion = getChannelVersion(connection, feature.channelName());
        return channelVersion != null ? new DefaultArtifactVersion(channelVersion) : null;
    }

    @Nullable
    public static String getChannelVersion(Connection connection, ResourceLocation channelName) {
        @Nullable final var connectionData = NetworkHooks.getConnectionData(connection);
        if (connectionData == null) return null;
        return connectionData.getChannels().get(channelName);
    }
}
