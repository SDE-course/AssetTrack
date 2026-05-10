package com.assettrack.allocation.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AssetTypeConverter implements AttributeConverter<AssetType, String> {

    @Override
    public String convertToDatabaseColumn(AssetType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public AssetType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        AssetType t = AssetType.fromString(dbData);
        if (t != null) return t;
        try {
            return AssetType.valueOf(dbData.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
