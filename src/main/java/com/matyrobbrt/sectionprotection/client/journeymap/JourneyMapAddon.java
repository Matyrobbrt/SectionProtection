package com.matyrobbrt.sectionprotection.client.journeymap;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.SectionProtectionAPI;
import com.matyrobbrt.sectionprotection.api.banner.Banner;
import com.matyrobbrt.sectionprotection.client.SPClient;
import com.matyrobbrt.sectionprotection.network.SPFeatures;
import com.matyrobbrt.sectionprotection.network.packet.claiming.UnclaimChunkPacket;
import com.matyrobbrt.sectionprotection.util.Utils;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.ModPopupMenu;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.util.UIState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

@ClientPlugin
public class JourneyMapAddon implements IClientPlugin, JMHelper {
    public static final String GROUP_NAME = "ClaimedChunks";

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
    public void setClaimed(ChunkPos pos, ResourceKey<Level> dim, DyeColor bannerColour, BlockPos bannerPos, Banner owningTeam, boolean isSelfMember) {
        final var id = "sp_claimed_" + pos.toString();

        final var overlay = new PolygonOverlay(
                SectionProtection.MOD_ID,
                id,
                dim,
                new ShapeProperties()
                        .setFillColor(bannerColour.getTextColor())
                        .setFillOpacity(0.2f),
                new MapPolygon(getChunkCorners(pos))
        ).setOverlayGroupName(GROUP_NAME);

        if (isSelfMember) {
            overlay.setTitle("Chunk claimed by you");
            overlay.setOverlayListener(new SimpleOverlayListener() {
                @Override
                @ParametersAreNonnullByDefault
                public void onOverlayMenuPopup(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition, ModPopupMenu modPopupMenu) {
                    final var subList = modPopupMenu.createSubItemList("Unclaim chunk");
                    subList.addMenuItem("Unclaim", b -> unclaim(false));
                    subList.addMenuItem("Unclaim and remove banner", b -> unclaim(true));
                }

                private void unclaim(boolean remove) {
                    if (SPClient.serverCanReceivePacket(SPFeatures.CLAIM_PACKETS)) {
                        SPClient.sendToServer(SPFeatures.CLAIM_PACKETS, new UnclaimChunkPacket(
                                dim, pos, remove
                        ));
                    }
                }
            });
        } else {
            Utils.getOwnerName(null, ClientJMEventListeners.getMembers(owningTeam))
                .ifPresent(owner -> overlay.setTitle("Chunk claimed by " + owner));
        }

        api.remove(overlay);
        try {
            api.show(overlay);
        } catch (Exception e) {
            SectionProtection.LOGGER.error("Exception trying to render JourneyMap overlay: ", e);
        }

        if (false) { // TODO implement showing the banner on the map
            final var banner = new Waypoint(SectionProtection.MOD_ID, "banner_" + pos, "ProtectingBanner", dim, bannerPos)
                    .setEditable(false)
                    .setColor(bannerColour.getTextColor());
            banner.setDisplayed(dim.location().toString(), false);
            api.remove(banner);
            try {
                api.show(banner);
            } catch (Exception e) {
                SectionProtection.LOGGER.error("Exception trying to add JourneyMap banner waypoint: ", e);
            }
        }
    }

    @Override
    public void unclaim(ChunkPos pos, ResourceKey<Level> dim) {
        api.remove(new PolygonOverlay(
                SectionProtection.MOD_ID,
                "sp_claimed_" + pos.toString(),
                dim,
                new ShapeProperties(),
                new MapPolygon(getChunkCorners(pos))
        ));
    }

    @Override
    public void removeAllMarkers() {
        api.removeAll(SectionProtectionAPI.MOD_ID);
    }

    private static List<BlockPos> getChunkCorners(ChunkPos pos) {
        final var corners = new ArrayList<BlockPos>();
        corners.add(new BlockPos(pos.getMinBlockX(), 256, pos.getMinBlockZ()));
        corners.add(new BlockPos(pos.getMinBlockX(), 256, pos.getMaxBlockZ() + 1));
        corners.add(new BlockPos(pos.getMaxBlockX() + 1, 256, pos.getMaxBlockZ() + 1));
        corners.add(new BlockPos(pos.getMaxBlockX() + 1, 256, pos.getMinBlockZ()));
        return corners;
    }
}
