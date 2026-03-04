package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PersistentModel implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<PersistentModel> CODEC = BuilderCodec.builder(PersistentModel.class, PersistentModel::new)
      .append(new KeyedCodec<>("Model", Model.ModelReference.CODEC), (entity, model) -> entity.modelReference = model, entity -> entity.modelReference)
      .add()
      .build();
   private Model.ModelReference modelReference;

   @Nonnull
   public static ComponentType<EntityStore, PersistentModel> getComponentType() {
      return EntityModule.get().getPersistentModelComponentType();
   }

   private PersistentModel() {
   }

   public PersistentModel(@Nonnull Model.ModelReference modelReference) {
      this.modelReference = modelReference;
   }

   @Nonnull
   public Model.ModelReference getModelReference() {
      return this.modelReference;
   }

   public void setModelReference(@Nonnull Model.ModelReference modelReference) {
      this.modelReference = modelReference;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      PersistentModel modelComponent = new PersistentModel();
      modelComponent.modelReference = this.modelReference;
      return modelComponent;
   }
}
