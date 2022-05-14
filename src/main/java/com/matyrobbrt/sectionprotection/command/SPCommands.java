package com.matyrobbrt.sectionprotection.command;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.api.Member.Permission;
import com.matyrobbrt.sectionprotection.api.Team;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// TODO translations
@EventBusSubscriber
public class SPCommands {

    //@formatter:off
    @SubscribeEvent
    static void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(literal(SectionProtection.MOD_ID)
            .then(
                literal("claim").then(argument("team", TeamArgument.team()).executes(SPCommands::claim))
            )
        );
    }
    
    // TODO claim limit
    private static int claim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var team = TeamArgument.getTeam(context, "team");
        if (team == null) {
            context.getSource().sendFailure(new TextComponent("Unknown team!"));
        } else {
            if (context.getSource().getEntity() instanceof Player player) {
                final var member = team.b.getMember(player.getUUID());
                final boolean isOp = player.getServer().getPlayerList().isOp(player.getGameProfile());
                if ((member == null || !member.getPermissions().contains(Permission.CLAIM)) && !isOp) {
                    context.getSource().sendFailure(new TextComponent("Cannot claim chunk on behalf of the team " + team.a));
                } else {
                    final var chunk = context.getSource().getLevel().getChunkAt(new BlockPos(context.getSource().getPosition()));
                    final var claimed = chunk.getCapability(ClaimedChunk.CAPABILITY).orElseThrow(RuntimeException::new);
                    if (claimed.getOwnerTeam() == null) {
                        claimed.setOwnerTeam(team.a);
                        context.getSource().sendSuccess(new TextComponent("Successfully claimed chunk!"), true);
                    } else {
                        context.getSource().sendFailure(new TextComponent("That chunk is already owned by ").append(new TextComponent(claimed.getOwnerTeam()).withStyle(ChatFormatting.GOLD)));
                    }
                }
            } else {
                final var chunk = context.getSource().getLevel().getChunkAt(new BlockPos(context.getSource().getPosition()));
                chunk.getCapability(ClaimedChunk.CAPABILITY).ifPresent(c -> {
                    if (c.getOwnerTeam() == null) {
                        Team.claim(team.a, chunk);
                    }
                });
            }
        }
        return Command.SINGLE_SUCCESS;
    }
    
}
