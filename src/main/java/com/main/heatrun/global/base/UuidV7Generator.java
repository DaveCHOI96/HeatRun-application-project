package com.main.heatrun.global.base;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public final class UuidV7Generator {

    private UuidV7Generator() {
        // 인스턴스 생성 금지 — 유틸 클래스
    }

    //명시적으로 UUID가 필요할때 사용
//    public static UUID generate() {
//        return UuidCreator.getTimeOrderedEpoch();
//    }

    public static UUID generateIfAbsent(UUID current) {
        return current != null ? current : UuidCreator.getTimeOrderedEpoch();
    }
}
