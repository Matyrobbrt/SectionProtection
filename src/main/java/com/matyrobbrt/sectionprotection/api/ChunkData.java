package com.matyrobbrt.sectionprotection.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public record ChunkData(ChunkPos pos, ResourceKey<Level> dimension) {

    public static final Codec<ChunkData> CODEC = RecordCodecBuilder.create(in -> in
        .group(Codec.LONG.xmap(ChunkPos::new, ChunkPos::toLong).fieldOf("pos").forGetter(ChunkData::pos),
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).fieldOf("dim").forGetter(ChunkData::dimension))
        .apply(in, ChunkData::new));

    public ChunkData(LevelChunk chunk) {
        this(chunk.getPos(), chunk.getLevel().dimension());
    }
}
