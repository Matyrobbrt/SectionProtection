package com.matyrobbrt.sectionprotection.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.BlockSnapshot;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public final class ActionType<T> {

    /**
     * The action type for block interactions. Called when a player interacts with a block.
     */
    public static final ActionType<InteractionContext<BlockState>> BLOCK_INTERACTION = new ActionType<>("interaction", cast(InteractionContext.class), "allow_interaction");
    /**
     * The breaking action type. Called when a player starts breaking a block in a claimed chunk.
     */
    public static final ActionType<BreakContext> BREAKING = new ActionType<>("breaking", BreakContext.class, "allow_breaking");
    /**
     * The placing action type. Called when a player attempts to place a block in a claimed chunk.
     */
    public static final ActionType<PlaceContext> PLACING = new ActionType<>("placing", PlaceContext.class, "allow_placing");

    private final String name;
    private final Class<T> contextClass;
    private final ResourceLocation tag;
    private final Map<ResourceKey<?>, TagKey<?>> tags = new HashMap<>();
    private ActionType(String name, Class<T> contextClass, String tag) {
        this.name = name;
        this.contextClass = contextClass;
        this.tag = new ResourceLocation(SectionProtectionAPI.MOD_ID, tag);
    }

    @Override
    public String toString() {
        return name;
    }

    public Class<T> contextClass() {
        return contextClass;
    }

    @SuppressWarnings("unchecked")
    public <Z> TagKey<Z> getTag(ResourceKey<? extends Registry<Z>> registry) {
        return (TagKey<Z>) tags.computeIfAbsent(registry, k -> TagKey.create(registry, tag));
    }

    public interface InteractionContext<T> {
        Result canInteract(ServerPlayer player, InteractionType interactionType, InteractionHand hand, Level level, BlockPos pos, T object);

        enum InteractionType {
            RIGHT_CLICK,
            LEFT_CLICK
        }
    }

    public interface BreakContext {
        Result canBreak(ServerPlayer player, Level world, BlockPos pos, BlockState state);
    }

    public interface PlaceContext {
        Result canPlace(ServerPlayer player, @Nonnull BlockSnapshot blockSnapshot, @Nonnull BlockState placedAgainst);

        static BlockState getPlacedBlock(BlockSnapshot snapshot) {
            return snapshot.getCurrentBlock();
        }
    }

    public enum Result {
        /**
         * Allows the action
         */
        ALLOW,
        /**
         * Denies the action
         */
        DENY,
        /**
         * Passes this action to other listeners. This is returned by listeners
         * which do not want to modify the result of an action.
         */
        CONTINUE
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> cast(Class<?> clazz) {
        return (Class<T>) clazz;
    }
}
