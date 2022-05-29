package com.matyrobbrt.sectionprotection.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.BlockSnapshot;

import javax.annotation.Nonnull;

public final class ActionType<T> {

    /**
     * The action type for interactions. Due to how breaking works, if you want to listen for breaking
     * you need to make sure the interaction will not be denied.
     */
    public static final ActionType<InteractionContext> INTERACTION = new ActionType<>("interaction", InteractionContext.class, "allow_interaction");
    public static final ActionType<BreakContext> BREAKING = new ActionType<>("breaking", BreakContext.class, "allow_breaking");
    public static final ActionType<PlaceContext> PLACING = new ActionType<>("placing", PlaceContext.class, "allow_placing");

    private final String name;
    private final Class<T> contextClass;
    private final TagKey<Block> tag;
    private ActionType(String name, Class<T> contextClass, String tag) {
        this.name = name;
        this.contextClass = contextClass;
        this.tag = BlockTags.create(new ResourceLocation(SectionProtectionAPI.MOD_ID, tag));
    }

    @Override
    public String toString() {
        return name;
    }

    public Class<T> contextClass() {
        return contextClass;
    }

    public TagKey<Block> getTag() {
        return tag;
    }

    public interface InteractionContext {
        Result canInteract(ServerPlayer player, InteractionType interactionType, InteractionHand hand, Level level, BlockPos pos);

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
}
