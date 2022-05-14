package com.matyrobbrt.sectionprotection.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.misc.Pair;

import com.matyrobbrt.sectionprotection.api.Team;
import com.matyrobbrt.sectionprotection.network.RequestTeamsPacket;
import com.matyrobbrt.sectionprotection.network.SPNetwork;
import com.matyrobbrt.sectionprotection.world.TeamRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;

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
        if (context.getSource() instanceof ClientSuggestionProvider s) {
            final var in = builder.getInput();
            return CompletableFuture.supplyAsync(() -> {
                final var received = new AtomicReference<Suggestions>();
                SPNetwork.CHANNEL.sendToServer(RequestTeamsPacket.INSTANCE);
                RequestTeamsPacket.TEAMS.addListener(teams -> {
                    final List<Suggestion> sug = teams.stream().filter(t -> t.startsWith(in))
                        .map(ss -> ss.substring(in.length()))
                        .map(actual -> new Suggestion(StringRange.at(in.length()), actual))
                        .toList();
                    received.set(new Suggestions(StringRange.at(in.length()), sug));
                });
                while (received.get() == null) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                return received.get();
            });
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
