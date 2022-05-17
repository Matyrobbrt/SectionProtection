package com.matyrobbrt.sectionprotection.util;

import java.util.jar.Manifest;

public record SPVersion(String version, String timestamp, String commitId) {

    public static SPVersion from(Manifest manifest) {
        final var attributes = manifest.getMainAttributes();
        return new SPVersion(
            attributes.getValue("Implementation-Version"),
            attributes.getValue("Timestamp"),
            attributes.getValue("Git-Commit")
        );
    }

}
