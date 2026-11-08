package com.sizeok.common.util;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {
    }

    public static String newId(String prefix) {
        String compact = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return prefix + "_" + compact;
    }
}
