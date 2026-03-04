package com.hypixel.hytale.server.core.universe.world.worldmap;

import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;

record WorldMapManager$MarkerAndItsStore(UserMapMarker marker, UserMapMarkersStore store) {
}