package com.matyrobbrt.sectionprotection.api.chunk;

import com.matyrobbrt.sectionprotection.api.Banner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public interface ChunkManager {

    @Nullable
    ChunkData getOwner(ChunkPos pos);

    void setOwner(ChunkPos pos, Banner banner, @Nullable BlockPos bannerPos);

    void removeOwner(ChunkPos pos);
}
