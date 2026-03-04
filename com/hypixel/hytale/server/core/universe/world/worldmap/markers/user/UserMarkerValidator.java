package com.hypixel.hytale.server.core.universe.world.worldmap.markers.user;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.worldmap.CreateUserMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.worldmap.UserMapMarkerConfig;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import java.util.Collection;
import java.util.UUID;

public final class UserMarkerValidator {
   private static final int NAME_LENGTH_LIMIT = 24;

   public static UserMarkerValidator.PlaceResult validatePlacing(Ref<EntityStore> ref, CreateUserMarker packet) {
      boolean shared = packet.shared;
      Store<EntityStore> store = ref.getStore();
      World world = store.getExternalData().getWorld();
      Player player = store.getComponent(ref, Player.getComponentType());
      if (isPlayerTooFarFromMarker(ref, packet.x, packet.z)) {
         return new UserMarkerValidator.Fail("server.worldmap.markers.edit.tooFar");
      } else if (packet.name != null && packet.name.length() > 24) {
         return new UserMarkerValidator.Fail("server.worldmap.markers.create.nameTooLong");
      } else {
         UserMapMarkersStore markersStore = (UserMapMarkersStore)(shared
            ? world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType())
            : player.getPlayerConfigData().getPerWorldData(world.getName()));
         UUID playerUuid = store.getComponent(ref, UUIDComponent.getComponentType()).getUuid();
         Collection<? extends UserMapMarker> markersByPlayer = markersStore.getUserMapMarkers(playerUuid);
         UserMapMarkerConfig markersConfig = world.getGameplayConfig().getWorldMapConfig().getUserMapMarkerConfig();
         if (!markersConfig.isAllowCreatingMarkers()) {
            return new UserMarkerValidator.Fail("server.worldmap.markers.create.creationDisabled");
         } else {
            int limit = shared ? markersConfig.getMaxSharedMarkersPerPlayer() : markersConfig.getMaxPersonalMarkersPerPlayer();
            if (markersByPlayer.size() + 1 >= limit) {
               String msg = shared ? "server.worldmap.markers.create.tooManyShared" : "server.worldmap.markers.create.tooManyPersonal";
               return new UserMarkerValidator.Fail(Message.translation(msg).param("limit", limit));
            } else {
               return new UserMarkerValidator.CanSpawn(player, markersStore);
            }
         }
      }
   }

   public static UserMarkerValidator.RemoveResult validateRemove(Ref<EntityStore> ref, UserMapMarker marker) {
      Store<EntityStore> store = ref.getStore();
      World world = store.getExternalData().getWorld();
      if (isPlayerTooFarFromMarker(ref, marker.getX(), marker.getZ())) {
         return new UserMarkerValidator.Fail("server.worldmap.markers.edit.tooFar");
      } else {
         UserMapMarkerConfig markersConfig = world.getGameplayConfig().getWorldMapConfig().getUserMapMarkerConfig();
         UUID playerUuid = store.getComponent(ref, UUIDComponent.getComponentType()).getUuid();
         UUID createdBy = marker.getCreatedByUuid();
         boolean isOwner = playerUuid.equals(createdBy) || createdBy == null;
         boolean hasPermission = isOwner || markersConfig.isAllowDeleteOtherPlayersSharedMarkers();
         return (UserMarkerValidator.RemoveResult)(!hasPermission
            ? new UserMarkerValidator.Fail("server.worldmap.markers.edit.notOwner")
            : new UserMarkerValidator.CanRemove());
      }
   }

   private static boolean isPlayerTooFarFromMarker(Ref<EntityStore> ref, double markerX, double markerZ) {
      Store<EntityStore> store = ref.getStore();
      Player player = store.getComponent(ref, Player.getComponentType());
      Transform transform = store.getComponent(ref, TransformComponent.getComponentType()).getTransform();
      Vector3d playerPosition = transform.getPosition();
      double distanceToMarker = playerPosition.distanceSquaredTo(markerX, playerPosition.y, markerZ);
      return distanceToMarker > getMaxRemovalDistanceSquared(player);
   }

   private static double getMaxRemovalDistanceSquared(Player player) {
      double maxDistance = player.getViewRadius() * 1.5 * 32.0;
      return maxDistance * maxDistance;
   }

   public record CanRemove() implements UserMarkerValidator.RemoveResult {
   }

   public record CanSpawn(Player player, UserMapMarkersStore markersStore) implements UserMarkerValidator.PlaceResult {
   }

   public record Fail(Message errorMsg) implements UserMarkerValidator.PlaceResult, UserMarkerValidator.RemoveResult {
      public Fail(String messageKey) {
         this(Message.translation(messageKey));
      }
   }

   public sealed interface PlaceResult permits UserMarkerValidator.Fail, UserMarkerValidator.CanSpawn {
   }

   public sealed interface RemoveResult permits UserMarkerValidator.Fail, UserMarkerValidator.CanRemove {
   }
}
