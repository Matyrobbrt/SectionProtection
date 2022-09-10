package com.matyrobbrt.sectionprotection.client;

import com.google.auto.service.AutoService;
import com.matyrobbrt.sectionprotection.api.client.SectionProtectionClientAPI;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@AutoService(SectionProtectionClientAPI.class)
public class ClientApiImpl implements SectionProtectionClientAPI {
    static boolean claimSync;
    static boolean teamSync;
    @Override
    public void enableClaimSync() {
        claimSync = true;
    }

    @Override
    public void enableTeamSync() {
        teamSync = true;
    }
}
