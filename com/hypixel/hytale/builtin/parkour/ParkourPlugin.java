package com.hypixel.hytale.builtin.parkour;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collections;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ParkourPlugin extends JavaPlugin {
   protected static ParkourPlugin instance;
   public static final String PARKOUR_CHECKPOINT_MODEL_ID = "Objective_Location_Marker";
   private final Object2IntMap<UUID> currentCheckpointByPlayerMap = new Object2IntOpenHashMap();
   private final Object2LongMap<UUID> startTimeByPlayerMap = new Object2LongOpenHashMap();
   private final Int2ObjectMap<UUID> checkpointUUIDMap = new Int2ObjectOpenHashMap();
   private ComponentType<EntityStore, ParkourCheckpoint> parkourCheckpointComponentType;
   private Model parkourCheckpointModel;
   private int lastIndex;

   public static ParkourPlugin get() {
      return instance;
   }

   public ParkourPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   public ComponentType<EntityStore, ParkourCheckpoint> getParkourCheckpointComponentType() {
      return this.parkourCheckpointComponentType;
   }

   public Model getParkourCheckpointModel() {
      return this.parkourCheckpointModel;
   }

   public Object2IntMap<UUID> getCurrentCheckpointByPlayerMap() {
      return this.currentCheckpointByPlayerMap;
   }

   public Object2LongMap<UUID> getStartTimeByPlayerMap() {
      return this.startTimeByPlayerMap;
   }

   public Int2ObjectMap<UUID> getCheckpointUUIDMap() {
      return this.checkpointUUIDMap;
   }

   public int getLastIndex() {
      return this.lastIndex;
   }

   @Override
   protected void setup() {
      instance = this;
      this.parkourCheckpointComponentType = this.getEntityStoreRegistry()
         .registerComponent(ParkourCheckpoint.class, "ParkourCheckpoint", ParkourCheckpoint.CODEC);
      EntityModule entityModule = EntityModule.get();
      ComponentType<EntityStore, Player> playerComponentType = entityModule.getPlayerComponentType();
      ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent = entityModule.getPlayerSpatialResourceType();
      this.getEntityStoreRegistry().registerSystem(new ParkourCheckpointSystems.EnsureNetworkSendable());
      this.getEntityStoreRegistry().registerSystem(new ParkourCheckpointSystems.Init(this.parkourCheckpointComponentType));
      this.getEntityStoreRegistry()
         .registerSystem(new ParkourCheckpointSystems.Ticking(this.parkourCheckpointComponentType, playerComponentType, playerSpatialComponent));
      this.getCommandRegistry().registerCommand(new ParkourCommand());
   }

   @Override
   protected void start() {
      ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Objective_Location_Marker");
      if (modelAsset == null) {
         throw new IllegalStateException(String.format("Default parkour checkpoint model '%s' not found", "Objective_Location_Marker"));
      } else {
         this.parkourCheckpointModel = Model.createUnitScaleModel(modelAsset);
      }
   }

   public void updateLastIndex(int index) {
      if (index > this.lastIndex) {
         this.lastIndex = index;
      }
   }

   public void updateLastIndex() {
      this.lastIndex = Collections.<Integer>max(this.checkpointUUIDMap.keySet());
   }

   public void resetPlayer(UUID playerUuid) {
      this.currentCheckpointByPlayerMap.replace(playerUuid, -1);
   }
}
