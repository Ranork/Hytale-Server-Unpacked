package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionBeacon;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionBeacon extends ActionBase {
   protected final String message;
   protected final double range;
   protected final int[] targetGroups;
   protected final int targetToSendSlot;
   protected final double expirationTime;
   protected final int sendCount;
   @Nullable
   protected final List<Ref<EntityStore>> sendList;

   public ActionBeacon(@Nonnull BuilderActionBeacon builderActionBeacon, @Nonnull BuilderSupport support) {
      super(builderActionBeacon);
      this.message = builderActionBeacon.getMessage(support);
      this.range = builderActionBeacon.getRange();
      this.targetGroups = builderActionBeacon.getTargetGroups(support);
      this.targetToSendSlot = builderActionBeacon.getTargetToSendSlot(support);
      this.expirationTime = builderActionBeacon.getExpirationTime();
      this.sendCount = builderActionBeacon.getSendCount();
      this.sendList = this.sendCount > 0 ? new ObjectArrayList(this.sendCount) : null;
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      role.getPositionCache().requireEntityDistanceUnsorted(this.range);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return !super.canExecute(ref, role, sensorInfo, dt, store)
         ? false
         : this.targetToSendSlot == Integer.MIN_VALUE || role.getMarkedEntitySupport().hasMarkedEntityInSlot(this.targetToSendSlot);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      Ref<EntityStore> target = this.targetToSendSlot >= 0 ? role.getMarkedEntitySupport().getMarkedEntityRef(this.targetToSendSlot) : ref;
      PositionCache positionCache = role.getPositionCache();
      if (this.sendCount <= 0) {
         positionCache.forEachNPCUnordered(
            this.range,
            ActionBeacon::filterNPCs,
            (_ref, _this, _target, _self) -> _this.sendNPCMessage(_self, _ref, _target, _self.getStore()),
            this,
            role,
            target,
            ref,
            store
         );
         return true;
      } else {
         positionCache.forEachNPCUnordered(
            this.range,
            ActionBeacon::filterNPCs,
            (npcEntity, _this, _sendList, _self) -> RandomExtra.reservoirSample(npcEntity, _this.sendCount, _sendList),
            this,
            role,
            this.sendList,
            ref,
            store
         );

         for (int i = 0; i < this.sendList.size(); i++) {
            this.sendNPCMessage(ref, this.sendList.get(i), target, store);
         }

         this.sendList.clear();
         return true;
      }
   }

   protected static boolean filterNPCs(
      @Nonnull Ref<EntityStore> ref, @Nonnull ActionBeacon _this, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return ref.getStore().getComponent(ref, BeaconSupport.getComponentType()) != null
         && WorldSupport.isGroupMember(role.getRoleIndex(), ref, _this.targetGroups, componentAccessor);
   }

   protected void sendNPCMessage(
      @Nonnull Ref<EntityStore> self,
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull Ref<EntityStore> target,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      NPCEntity npcComponent = componentAccessor.getComponent(self, NPCEntity.getComponentType());

      assert npcComponent != null;

      Role role = npcComponent.getRole();
      if (role.getDebugSupport().isDebugFlagSet(RoleDebugFlags.BeaconMessages)) {
         ((HytaleLogger.Api)NPCPlugin.get().getLogger().atInfo())
            .log("ID %d sent message '%s' with target ID %d to ID %d", self.getIndex(), this.message, target.getIndex(), targetRef.getIndex());
         ThreadLocalRandom random = ThreadLocalRandom.current();
         Vector3f color = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
         Matrix4d matrix = new Matrix4d();
         matrix.identity();
         Matrix4d tmp = new Matrix4d();
         TransformComponent transformComponent = componentAccessor.getComponent(self, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d pos = transformComponent.getPosition();
         ModelComponent modelComponent = componentAccessor.getComponent(self, ModelComponent.getComponentType());

         assert modelComponent != null;

         Model model = modelComponent.getModel();
         double x = pos.x;
         double y = pos.y + (model != null ? model.getEyeHeight(self, componentAccessor) : 0.0F);
         double z = pos.z;
         matrix.translate(x, y + random.nextFloat() * 0.5 - 0.25, z);
         TransformComponent targetTransformComponent = componentAccessor.getComponent(targetRef, TransformComponent.getComponentType());

         assert targetTransformComponent != null;

         Vector3d targetPos = targetTransformComponent.getPosition();
         ModelComponent targetModelComponent = componentAccessor.getComponent(targetRef, ModelComponent.getComponentType());
         float targetEyeHeight = targetModelComponent != null ? targetModelComponent.getModel().getEyeHeight(targetRef, componentAccessor) : 0.0F;
         x -= targetPos.getX();
         y -= targetPos.getY() + targetEyeHeight;
         z -= targetPos.getZ();
         double angleY = Math.atan2(-z, -x);
         matrix.rotateAxis(angleY + (float) (Math.PI / 2), 0.0, 1.0, 0.0, tmp);
         double angleX = Math.atan2(Math.sqrt(x * x + z * z), -y);
         matrix.rotateAxis(angleX, 1.0, 0.0, 0.0, tmp);
         DebugUtils.addArrow(componentAccessor.getExternalData().getWorld(), matrix, color, pos.distanceTo(targetPos), 5.0F, true);
      }

      BeaconSupport beaconSupportComponent = componentAccessor.getComponent(targetRef, BeaconSupport.getComponentType());
      if (beaconSupportComponent != null) {
         beaconSupportComponent.postMessage(this.message, target, this.expirationTime);
      }
   }
}
