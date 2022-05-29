package com.matyrobbrt.sectionprotection.api.extensions;

public interface BannerExtension {

    boolean isProtectionBanner();
    void setProtectionBanner(boolean isProtectionBanner);

    void setSectionProtectionIsUnloaded(boolean isUnloaded);
    boolean getSectionProtectionIsUnloaded();

    void sectionProtectionUnclaim();
}
