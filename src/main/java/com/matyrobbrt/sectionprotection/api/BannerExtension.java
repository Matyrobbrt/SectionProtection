package com.matyrobbrt.sectionprotection.api;

public interface BannerExtension {

    boolean isProtectionBanner();
    void setProtectionBanner(boolean isProtectionBanner);

    void setSectionProtectionIsUnloaded(boolean isUnloaded);
    boolean getSectionProtectionIsUnloaded();
}
