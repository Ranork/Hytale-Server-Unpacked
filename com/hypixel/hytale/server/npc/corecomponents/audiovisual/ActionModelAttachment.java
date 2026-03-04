package com.hypixel.hytale.server.npc.corecomponents.audiovisual;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionModelAttachment;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionModelAttachment extends ActionBase {
   @Nonnull
   protected final String slot;
   @Nonnull
   protected final String attachment;

   public ActionModelAttachment(@Nonnull BuilderActionModelAttachment builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.slot = builder.getSlot(support);
      this.attachment = builder.getAttachment(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      setModelAttachment(ref, this.slot, this.attachment, store);
      return true;
   }

   private static void setModelAttachment(
      @Nonnull Ref<EntityStore> ref, @Nonnull String slot, @Nullable String attachment, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (slot.isEmpty()) {
         throw new IllegalArgumentException("Slot must be specified!");
      } else {
         ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());

         assert modelComponent != null;

         NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         Model model = modelComponent.getModel();
         float scale = model.getScale();
         ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(model.getModelAssetId());
         Map<String, String> randomAttachments = model.getRandomAttachmentIds() != null ? new HashMap<>(model.getRandomAttachmentIds()) : new HashMap<>();
         if (attachment != null && !attachment.isEmpty()) {
            randomAttachments.put(slot, attachment);
         } else {
            randomAttachments.remove(slot);
         }

         model = Model.createScaledModel(modelAsset, scale, randomAttachments);
         componentAccessor.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
         Role role = npcComponent.getRole();
         if (role != null) {
            role.updateMotionControllers(ref, model, model.getBoundingBox(), componentAccessor);
         }
      }
   }
}
