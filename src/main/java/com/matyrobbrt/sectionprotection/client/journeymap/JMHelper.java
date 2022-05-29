package com.matyrobbrt.sectionprotection.client.journeymap;

import com.matyrobbrt.sectionprotection.util.Constants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;

public interface JMHelper {

    static boolean journeyMapLoaded() {
        return ModList.get().isLoaded(Constants.JOURNEYMAP);
    }

    @Nonnull
    static JMHelper getInstance() {
        return Store.instance;
    }

    void setClaimed(ChunkPos pos, ResourceKey<Level> dim, DyeColor bannerColour);

    class Store {
        static JMHelper instance = new JMHelper() {
            @Override
            public void setClaimed(ChunkPos pos, ResourceKey<Level> dim, DyeColor bannerColour) {

            }
        };
    }
}
