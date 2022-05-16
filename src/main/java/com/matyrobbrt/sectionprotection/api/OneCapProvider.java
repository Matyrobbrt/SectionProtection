package com.matyrobbrt.sectionprotection.api;

import java.util.function.Supplier;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class OneCapProvider<N extends Tag, T extends INBTSerializable<N>>
    implements ICapabilityProvider, INBTSerializable<N> {

    private final Capability<T> type;
    private final LazyOptional<T> cap;

    public OneCapProvider(Capability<T> type, @Nonnull Supplier<? extends T> cap) {
        this.type = type;
        this.cap = LazyOptional.of(cap::get);
    }

    @Override
    public N serializeNBT() {
        return cap.orElseThrow(RuntimeException::new).serializeNBT();
    }

    @Override
    public void deserializeNBT(N nbt) {
        cap.ifPresent(c -> c.deserializeNBT(nbt));
    }

    @Nonnull
    @Override
    public <Z> LazyOptional<Z> getCapability(@Nonnull Capability<Z> cap, Direction side) {
        if (cap == type) {
            return this.cap.cast();
        }
        return LazyOptional.empty();
    }

}
