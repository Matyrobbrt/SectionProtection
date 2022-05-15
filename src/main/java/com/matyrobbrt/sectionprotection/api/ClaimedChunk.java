package com.matyrobbrt.sectionprotection.api;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public interface ClaimedChunk extends INBTSerializable<CompoundTag> {

    Capability<ClaimedChunk> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    @Nullable
    Banner getOwningBanner();

    void setOwningBanner(Banner banner);

    class Impl implements ClaimedChunk {

        private Banner banner;

        public Impl(Banner owner) {
            this.banner = owner;
        }

        public Impl() {
            this(null);
        }

        @Override
        public CompoundTag serializeNBT() {
            final var nbt = new CompoundTag();
            if (banner != null) {
                nbt.put("banner", banner.serialize());
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt.contains("banner")) {
                banner = new Banner(nbt.getList("banner", Tag.TAG_COMPOUND));
            }
        }

        @Override
        public Banner getOwningBanner() {
            return banner;
        }

        @Override
        public void setOwningBanner(Banner banner) {
            this.banner = banner;
        }

    }
}
