package com.matyrobbrt.sectionprotection.commands;

import com.matyrobbrt.sectionprotection.Constants;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;

import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.ERROR_NOT_LOADED;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.ERROR_OUT_OF_WORLD;

import java.util.Optional;

public class SPCommands {

    //@formatter:off
    public static void register(final RegisterCommandsEvent event) {
        final var cmd = literal(SectionProtection.MOD_ID)
            .then(literal("chunk")
                    .then(literal("owner")
                        .then(argument("pos", BlockPosArgument.blockPos())
                            .executes(ctx -> getChunkOwner(ctx.getSource().getLevel(), ctx))
                                .then(argument("dimension", DimensionArgument.dimension())
                                    .executes(ctx -> getChunkOwner(DimensionArgument.getDimension(ctx, "dimension"), ctx))))
                    )
            );

        event.getDispatcher().register(cmd);
    }
    //@formatter:on

    private static int getChunkOwner(ServerLevel dimension, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var blockpos = context.getArgument("pos", Coordinates.class).getBlockPos(context.getSource());
        if (!dimension.hasChunkAt(blockpos)) {
            throw ERROR_NOT_LOADED.create();
        } else if (!dimension.isInWorldBounds(blockpos)) {
            throw ERROR_OUT_OF_WORLD.create();
        }
        final var chunk = dimension.getChunkAt(blockpos);
        chunk.getCapability(ClaimedChunk.CAPABILITY).resolve().flatMap(c -> Optional.ofNullable(c.getOwningBanner()))
            .map(ban -> Banners.get(context.getSource().getServer()).getMembers(ban))
            .ifPresentOrElse(team -> {
                final var members = team.stream()
                    .flatMap(uuid -> Utils.getPlayerName(context.getSource().getServer(), uuid).stream())
                    .map(name -> new TextComponent(name).withStyle(Constants.WITH_PLAYER_NAME))
                    .toList();
                MutableComponent text = new TextComponent("That chunk is owned by: ");
                for (var i = 0; i < members.size(); i++) {
                    text = text.append(members.get(i));
                    if (i == members.size() - 2) {
                        text = text.append(" and ");
                    } else if (i < members.size() - 1) {
                        text = text.append(", ");
                    }
                }
                context.getSource().sendSuccess(text, true);
            }, () -> context.getSource().sendSuccess(new TextComponent("No team owns the chunk at ")
                    .append(new TextComponent(blockpos.toShortString()).withStyle(ChatFormatting.AQUA)), true));
        return Command.SINGLE_SUCCESS;
    }
}
