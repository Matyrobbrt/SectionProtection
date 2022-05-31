package com.matyrobbrt.sectionprotection.api.banner;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;

/**
 * A manager for banner patterns and their team.
 */
@ParametersAreNonnullByDefault
public interface BannerManager {

    /**
     * Gets the members of a team.
     * @param banner the team's banner
     * @return a possibly-null (if the team doesn't exist) list of the member's team
     */
    @Nullable
    List<UUID> getMembers(Banner banner);

    /**
     * Creates a team.
     * @param banner the team's banner
     * @param owner the owner of the team
     */
    void createTeam(Banner banner, UUID owner);

}
