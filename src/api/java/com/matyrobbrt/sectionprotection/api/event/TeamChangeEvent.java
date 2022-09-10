package com.matyrobbrt.sectionprotection.api.event;

import com.matyrobbrt.sectionprotection.api.banner.Banner;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;
import java.util.UUID;

/**
 * Fired when a team is created or an existing one changes its member. <br>
 * Use the side-specific subclasses.
 */
public sealed class TeamChangeEvent extends Event {
    private final Banner banner;
    private final List<UUID> members;

    private TeamChangeEvent(Banner banner, List<UUID> members) {
        this.banner = banner;
        this.members = members;
    }

    public Banner getBanner() {
        return banner;
    }

    /**
     * Gets the new members of the team. <br>
     * Modifying this list has <strong>no effect</strong> over the team's original contents.<br>
     * Use {@linkplain com.matyrobbrt.sectionprotection.api.banner.BannerManager#getMembers(Banner)} (if necessary with a packet) in order to get a modifyable list.
     */
    public List<UUID> getMembers() {
        return members;
    }

    public static final class Server extends TeamChangeEvent {
        public Server(Banner banner, List<UUID> members) {
            super(banner, members);
        }
    }

    public static final class Client extends TeamChangeEvent {
        public Client(Banner banner, List<UUID> members) {
            super(banner, members);
        }
    }
}
