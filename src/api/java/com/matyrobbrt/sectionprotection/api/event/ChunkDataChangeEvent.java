package com.matyrobbrt.sectionprotection.api.event;

import com.matyrobbrt.sectionprotection.api.banner.Banner;
import com.matyrobbrt.sectionprotection.api.chunk.ChunkData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

public sealed class ChunkDataChangeEvent extends Event {
    public final ResourceKey<Level> dimension;
    public final ChunkPos pos;
    @Nullable
    public final ChunkData oldData;
    @Nullable
    public final ChunkData newData;

    protected ChunkDataChangeEvent(ResourceKey<Level> dimension, ChunkPos pos, @Nullable ChunkData newData, @Nullable ChunkData oldData) {
        this.dimension = dimension;
        this.pos = pos;
        this.oldData = oldData;
        this.newData = newData;
    }

    @Nullable
    public Banner getOldOwner() {
        return oldData == null ? null : oldData.banner();
    }
    public Banner getNewOwner() {
        return newData == null ? null : newData.banner();
    }

    public static final class Server extends ChunkDataChangeEvent {

        @ApiStatus.Internal
        public Server(ResourceKey<Level> dimension, ChunkPos pos, @org.jetbrains.annotations.Nullable ChunkData newData, @org.jetbrains.annotations.Nullable ChunkData oldData) {
            super(dimension, pos, newData, oldData);
        }
    }
    public static final class Client extends ChunkDataChangeEvent {

        @ApiStatus.Internal
        public Client(ResourceKey<Level> dimension, ChunkPos pos, @org.jetbrains.annotations.Nullable ChunkData newData, @org.jetbrains.annotations.Nullable ChunkData oldData) {
            super(dimension, pos, newData, oldData);
        }
    }
}
