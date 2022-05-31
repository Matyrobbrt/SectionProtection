package com.matyrobbrt.sectionprotection.api.chunk;

import com.matyrobbrt.sectionprotection.api.banner.Banner;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A manager tracking the claimed status of a chunk.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ChunkManager {

    /**
     * Gets the data associated with a chunk.
     * @param pos the chunk's pos
     * @return possibly-null data associated with that chunk
     */
    @Nullable
    ChunkData getOwner(ChunkPos pos);

    /**
     * Sets the data of a chunk.
     * @param pos the chunk's pos
     * @param data the chunk's data
     */
    void setData(ChunkPos pos, ChunkData data);

    /**
     * Sets the owner of a chunk.
     * @param pos the chunk's pos
     * @param banner the new owner
     * @param bannerPos the position of the banner protecting that chunk
     */
    default void setOwner(ChunkPos pos, Banner banner, @Nullable BlockPos bannerPos) {
        setData(pos, new ChunkData(banner, bannerPos));
    }

    /**
     * Removes the data associated with a chunk.
     * @param pos the chunk's pos.
     * @return the chunk's old data, if present
     */
    @Nullable
    ChunkData removeData(ChunkPos pos);
}
