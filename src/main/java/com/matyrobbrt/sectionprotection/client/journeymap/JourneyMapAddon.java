package com.matyrobbrt.sectionprotection.client.journeymap;

import com.matyrobbrt.sectionprotection.SectionProtection;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@ClientPlugin
public class JourneyMapAddon implements IClientPlugin, JMHelper {
    private IClientAPI api;

    @Override
    public void initialize(@Nonnull IClientAPI iClientAPI) {
        this.api = iClientAPI;
        Store.instance = this;
        api.removeAll(SectionProtection.MOD_ID);
    }

    @Override
    public String getModId() {
        return SectionProtection.MOD_ID;
    }

    @Override
    public void onEvent(@Nonnull ClientEvent clientEvent) {
    }

    @Override
    public void setClaimed(ChunkPos pos, ResourceKey<Level> dim, DyeColor bannerColour) {
        final var id = "sp_claimed_" + pos;

        final var overlay = new PolygonOverlay(
                SectionProtection.MOD_ID,
                id,
                dim,
                new ShapeProperties(),
                new MapPolygon(getChunkCorners(pos))
        );

        api.remove(overlay);
        try {
            api.show(overlay);
        } catch (Exception e) {
            SectionProtection.LOGGER.error("Exception trying to render JourneyMap overlay: ", e);
        }
    }

    private static List<BlockPos> getChunkCorners(ChunkPos pos) {
        final var corners = new ArrayList<BlockPos>();
        corners.add(new BlockPos(pos.getMinBlockX(), 256, pos.getMinBlockZ()));
        corners.add(new BlockPos(pos.getMinBlockX(), 256, pos.getMaxBlockZ()));
        corners.add(new BlockPos(pos.getMaxBlockX(), 256, pos.getMinBlockZ()));
        corners.add(new BlockPos(pos.getMaxBlockX(), 256, pos.getMaxBlockZ()));
        return corners;
    }
}
