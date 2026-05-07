package com.main.heatrun.domain.heatmap.dto;

import com.main.heatrun.domain.entity.HeatmapTile;

public record HeatmapTileResponse(
        Long id,
        Integer tileX,
        Integer tileY,
        Integer zoomLevel,
        Integer visitCount,
        Double intensity,
        Boolean isRunning
) {
    public static HeatmapTileResponse from(HeatmapTile tile) {
        return new HeatmapTileResponse(
                tile.getId(),
                tile.getTileX(),
                tile.getTileY(),
                tile.getZoomLevel(),
                tile.getVisitCount(),
                tile.getIntensity(),
                tile.getIsRunning()
        );
    }
}
