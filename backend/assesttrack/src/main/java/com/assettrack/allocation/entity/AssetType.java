package com.assettrack.allocation.entity;

/**
 * Enum for Asset Types
 * Represents different categories of assets in the system
 */
public enum AssetType {
    LAPTOP("Laptop"),
    DESKTOP("Desktop"),
    MONITOR("Monitor"),
    KEYBOARD("Keyboard"),
    MOUSE("Mouse"),
    PRINTER("Printer"),
    TABLET("Tablet"),
    MOBILE("Mobile"),
    NETWORK_DEVICE("Network Device"),
    OTHER("Other");

    private final String displayName;

    AssetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AssetType fromString(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        // match enum name (case-insensitive) or displayName (case-insensitive)
        for (AssetType t : AssetType.values()) {
            if (t.name().equalsIgnoreCase(trimmed) || t.displayName.equalsIgnoreCase(trimmed)) {
                return t;
            }
        }
        return null;
    }
}
