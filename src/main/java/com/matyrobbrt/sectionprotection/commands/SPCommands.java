package com.matyrobbrt.sectionprotection.commands;

import com.matyrobbrt.sectionprotection.SPTags;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.extensions.BannerExtension;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.util.FakePlayerHolder;
import com.matyrobbrt.sectionprotection.util.ServerConfig;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.ERROR_NOT_LOADED;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.ERROR_OUT_OF_WORLD;

@SuppressWarnings("Duplicates")
public class SPCommands {

    //@formatter:off
    public static void register(final RegisterCommandsEvent event) {
        final var cmd = literal(SectionProtection.MOD_ID)
            .then(literal("chunk")
                    .then(literal("owner")
                        .executes(ctx -> getChunkOwner(new BlockPos(ctx.getSource().getPosition()), ctx.getSource().getLevel(), ctx))
                        .then(argument("pos", BlockPosArgument.blockPos())
                            .executes(ctx -> getChunkOwner(ctx.getArgument("pos", Coordinates.class), ctx.getSource().getLevel(), ctx))
                                .then(argument("dimension", DimensionArgument.dimension())
                                    .executes(ctx -> getChunkOwner(ctx.getArgument("pos", Coordinates.class), DimensionArgument.getDimension(ctx, "dimension"), ctx))))
                    )
                    .then(literal("pos")
                        .executes(ctx -> getChunkPos(new BlockPos(ctx.getSource().getPosition()), ctx))
                        .then(argument("pos", BlockPosArgument.blockPos())
                            .executes(ctx -> getChunkPos(ctx.getArgument("pos", Coordinates.class).getBlockPos(ctx.getSource()), ctx))))
                    .then(literal("unclaim")
                            .executes(ctx -> unclaim(ctx, new BlockPos(ctx.getSource().getPosition()), false))
                            .then(argument("pos", BlockPosArgument.blockPos())
                                    .executes(ctx -> unclaim(ctx, ctx.getArgument("pos", Coordinates.class).getBlockPos(ctx.getSource()), false))
                                    .then(argument("remove_banner", BoolArgumentType.bool())
                                            .executes(ctx -> unclaim(ctx, ctx.getArgument("pos", Coordinates.class).getBlockPos(ctx.getSource()), BoolArgumentType.getBool(ctx, "remove_banner"))))))
                    .then(literal("banner_pos")
                            .executes(ctx -> bannerPos(ctx, new BlockPos(ctx.getSource().getPosition())))
                            .then(argument("pos", BlockPosArgument.blockPos())
                                    .executes(context -> bannerPos(context, context.getArgument("pos", Coordinates.class).getBlockPos(context.getSource())))))
            )
            .then(literal("version")
                    .executes(SPCommands::version))
            .then(literal("guidebook")
                    .executes(SPCommands::guideBook))
            .then(literal("fake_players")
                    .then(literal("list")
                            .executes(SPCommands::listFakePlayers)))
            .then(literal("conversion_items")
                    .then(literal("list")
                            .executes(SPCommands::listConversionItems)));

        event.getDispatcher().register(cmd);
    }
    //@formatter:on

    private static int bannerPos(CommandContext<CommandSourceStack> context, BlockPos pos) throws CommandSyntaxException {
        final var manager = ClaimedChunks.get(context.getSource().getLevel());
        final var chunkData = manager.getOwner(pos);
        if (chunkData == null) {
            context.getSource().sendFailure(new TextComponent("The chunk at ")
                    .append(new TextComponent(pos.toShortString()).withStyle(ChatFormatting.AQUA))
                    .append(" is not claimed!"));
            return Command.SINGLE_SUCCESS;
        }
        if (!context.getSource().hasPermission(Commands.LEVEL_GAMEMASTERS)) {
            final var team = Banners.get(context.getSource().getServer()).getMembers(chunkData.banner());
            if (team != null && !team.contains(context.getSource().getPlayerOrException().getUUID())) {
                context.getSource().sendFailure(new TextComponent("You do not have permission to know the position of the banner protecting that chunk!"));
                return Command.SINGLE_SUCCESS;
            }
        }
        if (chunkData.bannerPos() != null) {
            context.getSource().sendSuccess(new TextComponent("The banner protecting the chunk at ")
                    .append(new TextComponent(pos.toShortString()).withStyle(ChatFormatting.AQUA))
                    .append(" is at ")
                    .append(new TextComponent(chunkData.bannerPos().toShortString()).withStyle(ChatFormatting.GOLD)), true);
        } else {
            context.getSource().sendFailure(new TextComponent("The position of the banner protecting that chunk is unknown! This may be caused by upgrading from an older version of SectionProtection."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int unclaim(CommandContext<CommandSourceStack> context, BlockPos pos, boolean removeBanner) throws CommandSyntaxException {
        final var manager = ClaimedChunks.get(context.getSource().getLevel());
        final var data = manager.getOwner(pos);
        if (data == null) {
            context.getSource().sendFailure(new TextComponent("The chunk at ")
                    .append(new TextComponent(pos.toShortString()).withStyle(ChatFormatting.AQUA))
                    .append(" is not claimed!"));
            return Command.SINGLE_SUCCESS;
        }
        if (!context.getSource().hasPermission(Commands.LEVEL_GAMEMASTERS)) {
            final var team = Banners.get(context.getSource().getServer()).getMembers(data.banner());
            if (team != null && !team.contains(context.getSource().getPlayerOrException().getUUID())) {
                context.getSource().sendFailure(new TextComponent("You cannot unclaim somebody else's chunk!"));
                return Command.SINGLE_SUCCESS;
            }
        }
        manager.removeOwner(pos);
        if (removeBanner && data.bannerPos() != null) {
            final var be = context.getSource().getLevel().getBlockEntity(data.bannerPos());
            if (be instanceof BannerExtension ext) {
                ext.sectionProtectionUnclaim();
            }
            context.getSource().getLevel().destroyBlock(data.bannerPos(), true, context.getSource().getEntity());
        }
        context.getSource().sendSuccess(new TextComponent("Chunk at ").append(new TextComponent(pos.toShortString()).withStyle(ChatFormatting.AQUA))
                .append(" has been unclaimed!"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listConversionItems(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MutableComponent text = new TextComponent("Items usable as conversion items: ");
        final var all = Stream.concat(
            Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).getTag(SPTags.IS_CONVERSION_ITEM).stream(),
            ServerConfig.CONVERSION_ITEMS.get().stream().map(ResourceLocation::new).map(ForgeRegistries.ITEMS::getValue).filter(Objects::nonNull)
        );
        for (final var it = all.iterator(); it.hasNext();) {
            final var stack = new ItemStack(it.next());
            text = text.append(stack.getDisplayName().copy().withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack)))));
            if (it.hasNext()) {
                text = text.append(", ");
            }
        }
        context.getSource().sendSuccess(text, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int listFakePlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MutableComponent text = new TextComponent("FakePlayers present in this server: ");
        final var all = FakePlayerHolder.getAll();
        if (all.isEmpty()) {
            text = new TextComponent("No FakePlayers are present in this server.");
        }
        for (final var it = all.iterator(); it.hasNext();) {
            final var fake = it.next();
            text = text.append(fake.getName().copy()
                    .withStyle(Constants.WITH_PLAYER_NAME)
                    .withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, fake.getGameProfile().getName())))
                    .withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(fake.getUUID().toString())))));
            if (it.hasNext()) {
                text = text.append(", ");
            }
        }
        context.getSource().sendSuccess(text, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int guideBook(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource().getEntity() instanceof Player player) {
            if (player.getInventory().add(Constants.SP_BOOK.get())) {
                context.getSource().sendSuccess(new TextComponent("You have been given a SectionProtection guide book!"), false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int version(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (SectionProtection.VERSION == null) {
            context.getSource().sendFailure(new TextComponent("The SectionProtection version cannot be determined in a development environment!"));
            return 1;
        }
        final var text = new TextComponent("SectionProtection version information:")
            .append("\n")
            .append("Mod Version: ").append(new TextComponent(SectionProtection.VERSION.version()).withStyle(ChatFormatting.AQUA))
            .append("\n")
            .append("Timestamp: ").append(new TextComponent(SectionProtection.VERSION.timestamp()).withStyle(ChatFormatting.AQUA))
            .append("\n")
            .append("Commit ID: ").append(new TextComponent(SectionProtection.VERSION.commitId()).withStyle(ChatFormatting.AQUA));
        context.getSource().sendSuccess(text, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getChunkPos(BlockPos blockpos, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!context.getSource().getLevel().hasChunkAt(blockpos)) {
            throw ERROR_NOT_LOADED.create();
        } else if (!context.getSource().getLevel().isInWorldBounds(blockpos)) {
            throw ERROR_OUT_OF_WORLD.create();
        }
        final var chunkPos = context.getSource().getLevel().getChunkAt(blockpos).getPos();
        final var pos = chunkPos.x + "," + chunkPos.z;
        context.getSource().sendSuccess(new TextComponent("The positions of the chunk containing the block with the position ")
                .append(new TextComponent(blockpos.toShortString()).withStyle(ChatFormatting.AQUA))
                .append(" are ")
                .append(new TextComponent(pos).withStyle(s -> s.withColor(ChatFormatting.GOLD).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, pos)))), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int getChunkOwner(Coordinates coords, ServerLevel dimension, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return getChunkOwner(coords.getBlockPos(context.getSource()), dimension, context);
    }

    private static int getChunkOwner(BlockPos blockpos, ServerLevel dimension, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!dimension.hasChunkAt(blockpos)) {
            throw ERROR_NOT_LOADED.create();
        } else if (!dimension.isInWorldBounds(blockpos)) {
            throw ERROR_OUT_OF_WORLD.create();
        }
        final var chunk = new ChunkPos(blockpos);
        final var manager = ClaimedChunks.get(dimension);
        Optional.ofNullable(manager.getOwner(chunk))
            .map(ban -> Banners.get(context.getSource().getServer()).getMembers(ban.banner()))
            .ifPresentOrElse(team -> {
                final var members = team.stream()
                    .flatMap(uuid -> Utils.getPlayerName(context.getSource().getServer(), uuid).stream())
                    .map(name -> new TextComponent(name).withStyle(Constants.WITH_PLAYER_NAME))
                    .toList();
                MutableComponent text = new TextComponent("The chunk at ")
                        .append(new TextComponent(blockpos.toShortString()).withStyle(ChatFormatting.AQUA))
                        .append(" is owned by: ");
                for (var i = 0; i < members.size(); i++) {
                    text = text.append(members.get(i));
                    if (i == members.size() - 2) {
                        text = text.append(" and ");
                    } else if (i < members.size() - 1) {
                        text = text.append(", ");
                    }
                }
                context.getSource().sendSuccess(text, false);
            }, () -> context.getSource().sendSuccess(new TextComponent("No team owns the chunk at ")
                    .append(new TextComponent(blockpos.toShortString()).withStyle(ChatFormatting.AQUA)), false));
        return Command.SINGLE_SUCCESS;
    }
}
