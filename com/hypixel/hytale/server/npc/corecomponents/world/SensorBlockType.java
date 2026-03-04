package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorBlockType;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorBlockType extends SensorBase implements IAnnotatedComponentCollection {
   protected final Sensor sensor;
   protected final int blockSet;

   public SensorBlockType(@Nonnull BuilderSensorBlockType builder, @Nonnull BuilderSupport support, Sensor sensor) {
      super(builder);
      this.sensor = sensor;
      this.blockSet = builder.getBlockSet(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (super.matches(ref, role, dt, store) && this.sensor.matches(ref, role, dt, store)) {
         InfoProvider sensorInfo = this.sensor.getSensorInfo();
         if (sensorInfo == null) {
            return false;
         } else {
            IPositionProvider positionProvider = sensorInfo.getPositionProvider();
            int x = MathUtil.floor(positionProvider.getX());
            int y = MathUtil.floor(positionProvider.getY());
            int z = MathUtil.floor(positionProvider.getZ());
            World world = store.getExternalData().getWorld();
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
            if (chunk == null) {
               positionProvider.clear();
               return false;
            } else {
               int blockId = chunk.getBlock(x, y, z);
               if (!BlockSetModule.getInstance().blockInSet(this.blockSet, blockId)) {
                  positionProvider.clear();
                  return false;
               } else {
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.sensor.getSensorInfo();
   }

   @Override
   public void registerWithSupport(Role role) {
      this.sensor.registerWithSupport(role);
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.sensor.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
   }

   @Override
   public void loaded(Role role) {
      this.sensor.loaded(role);
   }

   @Override
   public void spawned(Role role) {
      this.sensor.spawned(role);
   }

   @Override
   public void unloaded(Role role) {
      this.sensor.unloaded(role);
   }

   @Override
   public void removed(Role role) {
      this.sensor.removed(role);
   }

   @Override
   public void teleported(Role role, World from, World to) {
      this.sensor.teleported(role, from, to);
   }

   @Override
   public void done() {
      this.sensor.done();
   }

   @Override
   public int componentCount() {
      return 1;
   }

   @Override
   public IAnnotatedComponent getComponent(int index) {
      if (index >= this.componentCount()) {
         throw new IndexOutOfBoundsException();
      } else {
         return this.sensor;
      }
   }

   @Override
   public void setContext(IAnnotatedComponent parent, int index) {
      super.setContext(parent, index);
      this.sensor.setContext(this, index);
   }
}
