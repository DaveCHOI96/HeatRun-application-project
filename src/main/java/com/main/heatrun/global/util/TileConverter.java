package com.main.heatrun.global.util;

import org.springframework.stereotype.Component;

@Component
public class TileConverter {

    // ── GPS 좌표 → 슬리피맵 타일 변환 ──────────────────────────
    // 슬리피맵(SlippyMap) 표준 공식
    // Google Maps, OpenStreetMap 등 모두 동일한 방식 사용

    // 타일 X 좌표 계산
    public int longitudeToTileX(double longitude, int zoom) {
        return (int) Math.floor(
                (longitude + 180.0) / 360.0 * Math.pow(2, zoom)
        );
    }

    // 타일 Y 좌표 계산
    public int latitudeToTileY(double latitude, int zoom) {
        double latRad = Math.toRadians(latitude);
        return (int) Math.floor(
                (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad))
                      / Math.PI) / 2.0 * Math.pow(2, zoom)
        );
    }

    // 타일 중심 좌표 -> 위도 변환 (역변환)
    public double tileYToLatitude(int tileY, int zoom) {
        double n = Math.PI - 2.0 * Math.PI * tileY / Math.pow(2, zoom);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    // 타일 중심 좌표 -> 경도 변환 (역변환)
    public double tileXToLongitude(int tileX, int zoom) {
        return tileX / Math.pow(2, zoom) * 360.0 - 180.0;
    }

    // GPS 좌표로 타일 범위 계산 (화면에 보이는 타일 목록)
    public int[] getTileRange(double minLat, double maxLat, double minLng, double maxLng, int zoom) {
        int minX = longitudeToTileX(minLng, zoom);
        int maxX = longitudeToTileX(maxLng, zoom);
        int minY = latitudeToTileY(maxLat, zoom);
        int maxY = latitudeToTileY(minLat, zoom);
        return new int[]{minX, maxX, minY, maxY};
    }
}
