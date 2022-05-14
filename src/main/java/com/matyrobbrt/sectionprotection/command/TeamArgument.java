package com.matyrobbrt.sectionprotection.command;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.misc.Pair;

import com.matyrobbrt.sectionprotection.api.Member.Permission;
import com.matyrobbrt.sectionprotection.api.Team;
import com.matyrobbrt.sectionprotection.world.TeamRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class TeamArgument implements ArgumentType<String> {

    public static TeamArgument team() {
        return new TeamArgument();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof CommandSourceStack s) {
            if (s.getEntity() instanceof ServerPlayer player) {
                final boolean isOp = s.getServer().getPlayerList().isOp(player.getGameProfile());
                final TeamRegistry reg = TeamRegistry.get(s.getServer());
                final var in = builder.getInput();
                return CompletableFuture.supplyAsync(() -> {
                    final Collection<String> teams = isOp ? reg.getAllTeams().keySet() : reg.getTeams(player.getUUID());
                    final List<Suggestion> sug = teams.stream().filter(t -> t.startsWith(in))
                        .filter(
                            t -> isOp || reg.getTeam(t).getMember(player.getUUID()).getPermissions()
                                .contains(Permission.OWNER))
                        .map(ss -> ss.substring(in.length()))
                        .map(actual -> new Suggestion(StringRange.at(in.length()), actual))
                        .toList();
                    return new Suggestions(StringRange.at(in.length()), sug);
                });
            }
        }
        return ArgumentType.super.listSuggestions(context, builder);
    }

    @Nullable
    public static Pair<String, Team> getTeam(CommandContext<CommandSourceStack> pContext, String pName) {
        final var arg = pContext.getArgument(pName, String.class);
        final TeamRegistry reg = TeamRegistry.get(pContext.getSource().getServer());
        final var team = reg.getTeam(arg);
        if (team == null) {
            return null;
        } else {
            return new Pair<>(arg, team);
        }
    }

}
