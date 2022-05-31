package com.matyrobbrt.sectionprotection;

import com.google.auto.service.AutoService;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.matyrobbrt.sectionprotection.api.ActionType;
import com.matyrobbrt.sectionprotection.api.SectionProtectionAPI;
import com.matyrobbrt.sectionprotection.api.banner.BannerManager;
import com.matyrobbrt.sectionprotection.api.chunk.ChunkManager;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@AutoService(SectionProtectionAPI.class)
public class APIImpl implements SectionProtectionAPI {

    private static final Multimap<ActionType<?>, Object> ACTION_TO_PREDICATE = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

    public static <T> Collection<Object> getPredicates(ActionType<T> type) {
        return ACTION_TO_PREDICATE.get(type);
    }

    @Override
    public ChunkManager getChunkManager(ServerLevel level) {
        return ClaimedChunks.get(level);
    }

    @Override
    public BannerManager getBannerManager(MinecraftServer server) {
        return Banners.get(server);
    }

    @Override
    public <T> void registerPredicate(ActionType<T> actionType, T predicate) {
        ACTION_TO_PREDICATE.put(actionType, predicate);
    }

}
