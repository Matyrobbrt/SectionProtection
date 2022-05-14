package com.matyrobbrt.sectionprotection.api;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public interface ClaimedChunk extends INBTSerializable<CompoundTag> {

    Capability<ClaimedChunk> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    @Nullable
    String getOwnerTeam();

    void setOwnerTeam(String team);

    class Impl implements ClaimedChunk {

        private String owner;

        public Impl(String owner) {
            this.owner = owner;
        }

        public Impl() {
            this(null);
        }

        @Override
        public CompoundTag serializeNBT() {
            final var nbt = new CompoundTag();
            if (owner != null) {
                nbt.putString("owner", owner);
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt.contains("owner")) {
                owner = nbt.getString("owner");
            }
        }

        @Override
        public String getOwnerTeam() {
            return owner;
        }

        @Override
        public void setOwnerTeam(String team) {
            this.owner = team;
        }

    }
}
