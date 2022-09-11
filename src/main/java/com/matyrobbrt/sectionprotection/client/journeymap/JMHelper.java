package com.matyrobbrt.sectionprotection.client.journeymap;

import com.matyrobbrt.sectionprotection.api.banner.Banner;
import com.matyrobbrt.sectionprotection.util.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import java.util.List;

public interface JMHelper {

    static boolean journeyMapLoaded() {
        return ModList.get().isLoaded(Constants.JOURNEYMAP);
    }

    @Nonnull
    static JMHelper getInstance() {
        return Store.instance;
    }

    void setClaimed(ChunkPos pos, ResourceKey<Level> dim, DyeColor bannerColour, BlockPos bannerPos, Banner owningTeam, boolean isSelfMember);
    void unclaim(ChunkPos pos, ResourceKey<Level> dim);
    void removeAllMarkers();

    class Store {
        static JMHelper instance = new JMHelper() {
            @Override
            public void setClaimed(ChunkPos pos, ResourceKey<Level> dim, DyeColor bannerColour, BlockPos bannerPos, Banner owningTeam, boolean isSelfMember) {

            }

            @Override
            public void unclaim(ChunkPos pos, ResourceKey<Level> dim) {

            }

            @Override
            public void removeAllMarkers() {

            }
        };
    }
}
