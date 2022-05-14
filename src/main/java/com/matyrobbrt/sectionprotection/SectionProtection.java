package com.matyrobbrt.sectionprotection;

import org.slf4j.Logger;

import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.api.OneCapProvider;
import com.matyrobbrt.sectionprotection.api.Team;
import com.matyrobbrt.sectionprotection.world.TeamRegistry;
import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.LevelChunk;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SectionProtection.MOD_ID)
public class SectionProtection {

    public static final String MOD_ID = "sectionprotection";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SectionProtection() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SectionProtection::registerCaps);

        MinecraftForge.EVENT_BUS.register(SectionProtection.class);
    }

    @SubscribeEvent
    static void onItemDropped(final ItemTossEvent event) {
        if (event.getPlayer().level.isClientSide)
            return;
        if (event.getEntityItem().getItem().getItem() == Items.STICK) {
            TeamRegistry.get(event.getPlayer().level.getServer()).addTeam(
                "test_" + event.getPlayer().getRandom().nextInt(129021),
                new Team(event.getPlayer().getUUID()));
        } else if (event.getEntityItem().getItem().getItem() == Items.APPLE) {
            MutableComponent component = new TextComponent("Your teams: ");
            final var registry = TeamRegistry.get(event.getPlayer().level.getServer());
            final var teams = registry.getTeams(event.getPlayer().getUUID());
            for (int j = 0; j < teams.size(); j++) {
                final var team = registry.getTeam(teams.get(j));
                final var member = team.getMember(event.getPlayer().getUUID());
                component = component.append(
                    new TextComponent(teams.get(j)).withStyle(s -> s.applyFormat(ChatFormatting.AQUA)).append("("));
                for (final var it = member.getPermissions().iterator(); it.hasNext();) {
                    final var i = it.next();
                    component = component.append(i.toString());
                    if (it.hasNext()) {
                        component = component.append(" ");
                    }
                }
                component = component.append(")");
                if (j != teams.size() - 1) {
                    component = component.append(", ");
                }
            }
            event.getPlayer().sendMessage(component, Util.NIL_UUID);
        }
    }

    private static final ResourceLocation CAP_ID = new ResourceLocation(MOD_ID, "claimed");

    @SubscribeEvent
    static void attachChunkCaps(final AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(CAP_ID, new OneCapProvider<>(ClaimedChunk.CAPABILITY, ClaimedChunk.Impl::new));
    }

    static void registerCaps(final RegisterCapabilitiesEvent event) {
        event.register(ClaimedChunk.class);
    }
}
