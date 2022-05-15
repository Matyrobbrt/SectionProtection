package com.matyrobbrt.sectionprotection.api;

import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class WithPlacerState extends BlockState {

    private final UUID placerUUID;

    public WithPlacerState(Block p_61042_, ImmutableMap<Property<?>, Comparable<?>> p_61043_,
        MapCodec<BlockState> p_61044_, UUID placerUUID) {
        super(p_61042_, p_61043_, p_61044_);
        this.placerUUID = placerUUID;
    }

    public UUID getPlacerUUID() {
        return placerUUID;
    }

}
