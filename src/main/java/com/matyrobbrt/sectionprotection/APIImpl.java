package com.matyrobbrt.sectionprotection;

import com.google.auto.service.AutoService;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.matyrobbrt.sectionprotection.api.ActionType;
import com.matyrobbrt.sectionprotection.api.SectionProtectionAPI;
import com.matyrobbrt.sectionprotection.api.chunk.ChunkManager;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@AutoService(SectionProtectionAPI.class)
public class APIImpl implements SectionProtectionAPI {

    private static final Multimap<ActionType<?>, Object> actionToPredicate = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

    public static <T> Collection<Object> getPredicates(ActionType<T> type) {
        return actionToPredicate.get(type);
    }

    @Override
    public ChunkManager getManager(ServerLevel level) {
        return ClaimedChunks.get(level);
    }

    @Override
    public <T> void registerPredicate(ActionType<T> actionType, T predicate) {
        actionToPredicate.put(actionType, predicate);
    }

}
