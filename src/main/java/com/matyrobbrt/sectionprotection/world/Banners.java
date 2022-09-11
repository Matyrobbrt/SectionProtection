package com.matyrobbrt.sectionprotection.world;

import com.google.common.collect.Lists;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.banner.Banner;
import com.matyrobbrt.sectionprotection.api.banner.BannerManager;
import com.matyrobbrt.sectionprotection.api.event.TeamChangeEvent;
import eu.mihosoft.vcollections.VList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class Banners extends SavedData implements BannerManager {

    /**
     * This represents the current version of the SavedData, allowing us to convert data between
     * an older version and a new one.
     */
    public static final int CURRENT_VERSION = 1;

    private final Map<Banner, List<UUID>> banners;

    public Banners() {
        this.banners = new HashMap<>();
    }

    public Map<Banner, List<UUID>> getBanners() {
        return banners;
    }

    @Nullable
    @Override
    public List<UUID> getMembers(Banner banner) {
        return banners.get(banner);
    }

    public boolean isMember(Banner banner, UUID member) {
        final var team = getMembers(banner);
        return team != null && team.contains(member);
    }

    @Override
    public void createTeam(Banner banner, UUID owner) {
        banners.put(banner, observe(banner, Lists.newArrayList(owner)));
        setDirty(true);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag pCompoundTag) {
        final var datas = new ListTag();
        banners.forEach((b, ids) -> {
            final var idList = new ListTag();
            ids.forEach(id -> idList.add(NbtUtils.createUUID(id)));
            final var data = new CompoundTag();
            data.put("banner", b.serialize());
            data.put("members", idList);
            datas.add(data);
        });
        pCompoundTag.put("data", datas);
        pCompoundTag.putInt("dataVersion", CURRENT_VERSION);
        return pCompoundTag;
    }

    public static Banners load(CompoundTag nbt) {
        final var banners = new Banners();
        nbt.getList("data", Tag.TAG_COMPOUND).forEach(tag -> {
            final var cTag = (CompoundTag) tag;
            final var ids = new ArrayList<>(
                cTag.getList("members", Tag.TAG_INT_ARRAY).stream().map(NbtUtils::loadUUID).toList());
            final var banner = new Banner(cTag.getList("banner", Tag.TAG_COMPOUND));
            banners.banners.put(banner, observe(banner, ids));
        });
        return banners;
    }

    private static List<UUID> observe(Banner teamBanner, List<UUID> list) {
        final var vList = VList.newInstance(list);
        vList.addChangeListener(evt -> MinecraftForge.EVENT_BUS.post(new TeamChangeEvent.Server(teamBanner, List.copyOf(list))));
        return vList;
    }

    public static Banners get(MinecraftServer server) {
        return Objects.requireNonNull(server.getLevel(Level.OVERWORLD)).getDataStorage().computeIfAbsent(Banners::load, Banners::new,
            SectionProtection.MOD_ID + "_banners");
    }

}
