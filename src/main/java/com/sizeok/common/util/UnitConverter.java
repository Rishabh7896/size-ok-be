package com.sizeok.common.util;

import com.sizeok.common.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public final class UnitConverter {

    private static final BigDecimal CM_PER_INCH = new BigDecimal("2.54");

    private static final Map<String, double[]> ALLOWED_CM_RANGES = Map.ofEntries(
            Map.entry("CHEST", new double[]{50.0, 180.0}),
            Map.entry("BUST", new double[]{50.0, 180.0}),
            Map.entry("SHOULDER", new double[]{25.0, 75.0}),
            Map.entry("NECK", new double[]{25.0, 65.0}),
            Map.entry("ARM_LENGTH", new double[]{35.0, 100.0}),
            Map.entry("SLEEVE", new double[]{10.0, 105.0}),
            Map.entry("UPPER_ARM", new double[]{15.0, 65.0}),
            Map.entry("WAIST", new double[]{45.0, 180.0}),
            Map.entry("HIP", new double[]{55.0, 190.0}),
            Map.entry("HIPS", new double[]{55.0, 190.0}),
            Map.entry("INSEAM", new double[]{40.0, 110.0}),
            Map.entry("OUTSEAM", new double[]{60.0, 135.0}),
            Map.entry("THIGH", new double[]{30.0, 100.0}),
            Map.entry("KNEE", new double[]{20.0, 70.0}),
            Map.entry("CALF", new double[]{20.0, 70.0}),
            Map.entry("RISE", new double[]{15.0, 50.0}),
            Map.entry("LENGTH", new double[]{35.0, 160.0}),
            Map.entry("HEIGHT", new double[]{100.0, 240.0}),
            Map.entry("WEIGHT", new double[]{25.0, 250.0})
    );

    private UnitConverter() {
    }

    public static String normalizeDimensionCode(String dimension) {
        if (dimension == null || dimension.isBlank()) {
            throw new BadRequestException("INVALID_DIMENSION", "Dimension code cannot be blank");
        }
        String upper = dimension.trim().toUpperCase().replace(' ', '_');
        if ("HIPS".equals(upper)) {
            return "HIP";
        }
        if ("BUST".equals(upper)) {
            return "CHEST";
        }
        if ("SLEEVE_LENGTH".equals(upper)) {
            return "SLEEVE";
        }
        return upper;
    }

    public static String normalizeUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return "CM";
        }
        String upper = unit.trim().toUpperCase();
        if ("CM".equals(upper) || "CENTIMETERS".equals(upper) || "CENTIMETRES".equals(upper)) {
            return "CM";
        }
        if ("IN".equals(upper) || "INCH".equals(upper) || "INCHES".equals(upper)) {
            return "IN";
        }
        throw new BadRequestException("UNSUPPORTED_UNIT", "Unsupported measurement unit: " + unit + ". Allowed: CM, IN");
    }

    public static BigDecimal toCentimeters(BigDecimal value, String unit) {
        if (value == null) {
            throw new BadRequestException("INVALID_VALUE", "Measurement value cannot be null");
        }
        String normUnit = normalizeUnit(unit);
        if ("IN".equals(normUnit)) {
            return value.multiply(CM_PER_INCH).setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal fromCentimeters(BigDecimal valueCm, String targetUnit) {
        if (valueCm == null) {
            return null;
        }
        String normUnit = normalizeUnit(targetUnit);
        if ("IN".equals(normUnit)) {
            return valueCm.divide(CM_PER_INCH, 2, RoundingMode.HALF_UP);
        }
        return valueCm.setScale(2, RoundingMode.HALF_UP);
    }

    public static void validateDimensionRangeCm(String dimensionCode, BigDecimal valueCm) {
        String code = normalizeDimensionCode(dimensionCode);
        double[] bounds = ALLOWED_CM_RANGES.get(code);
        if (bounds == null) {
            throw new BadRequestException("UNSUPPORTED_DIMENSION", "Unsupported measurement dimension: " + dimensionCode);
        }
        double val = valueCm.doubleValue();
        if (val < bounds[0] || val > bounds[1]) {
            throw new BadRequestException(
                    "MEASUREMENT_OUT_OF_RANGE",
                    String.format("Dimension %s value %.2f cm is outside allowed range [%.1f, %.1f] cm",
                            code, val, bounds[0], bounds[1])
            );
        }
    }
}
