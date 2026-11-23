package com.example.myapplication.map

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

object MapTileSources {

    val satelliteHybrid2026 = object : OnlineTileSourceBase(
        "GoogleHybridSatellite2026",
        0,
        19,
        256,
        ".jpg",
        arrayOf(
            "https://mt0.google.com/vt/lyrs=y&hl=en",
            "https://mt1.google.com/vt/lyrs=y&hl=en",
            "https://mt2.google.com/vt/lyrs=y&hl=en",
            "https://mt3.google.com/vt/lyrs=y&hl=en"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val z = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "$baseUrl&x=$x&y=$y&z=$z"
        }
    }

    val terrain2026 = object : OnlineTileSourceBase(
        "GoogleTerrain2026",
        0,
        19,
        256,
        ".png",
        arrayOf(
            "https://mt0.google.com/vt/lyrs=p&hl=en",
            "https://mt1.google.com/vt/lyrs=p&hl=en"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val z = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "$baseUrl&x=$x&y=$y&z=$z"
        }
    }
}