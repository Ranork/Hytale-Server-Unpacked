package com.hypixel.hytale.server.npc.corecomponents.audiovisual;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ModelParticle;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.entities.SpawnModelParticles;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionSpawnParticles;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import javax.annotation.Nonnull;

public class ActionSpawnParticles extends ActionBase {
   protected final String particleSystem;
   protected final double range;
   protected final Vector3d offset;
   protected final ModelParticle[] modelParticlesProtocol;

   public ActionSpawnParticles(@Nonnull BuilderActionSpawnParticles builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.particleSystem = builder.getParticleSystem(support);
      this.offset = builder.getOffset(support);
      this.range = builder.getRange(support);
      com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle particle = new com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle();
      particle.setSystemId(this.particleSystem);
      particle.setPositionOffset(new Vector3f((float)this.offset.x, (float)this.offset.y, (float)this.offset.z));
      particle.setTargetNodeName(builder.getTargetNodeName(support));
      particle.setDetachedFromModel(builder.isDetachedFromModel(support));
      this.modelParticlesProtocol = new ModelParticle[]{particle.toPacket()};
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = new Vector3d(this.offset).rotateY(transformComponent.getRotation().getYaw()).add(transformComponent.getPosition());
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      playerSpatialResource.getSpatialStructure().collect(position, this.range, results);
      NetworkId networkIdComponent = store.getComponent(ref, NetworkId.getComponentType());
      if (networkIdComponent == null) {
         return true;
      } else {
         SpawnModelParticles packet = new SpawnModelParticles(networkIdComponent.getId(), this.modelParticlesProtocol);
         ObjectListIterator var13 = results.iterator();

         while (var13.hasNext()) {
            Ref<EntityStore> playerRef = (Ref<EntityStore>)var13.next();
            PlayerRef playerRefComponent = store.getComponent(playerRef, PlayerRef.getComponentType());
            if (playerRefComponent != null) {
               playerRefComponent.getPacketHandler().write(packet);
            }
         }

         return true;
      }
   }
}
