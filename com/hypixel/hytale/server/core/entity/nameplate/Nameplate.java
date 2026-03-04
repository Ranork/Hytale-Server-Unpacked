package com.hypixel.hytale.server.core.entity.nameplate;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class Nameplate implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<Nameplate> CODEC = BuilderCodec.builder(Nameplate.class, Nameplate::new)
      .append(new KeyedCodec<>("Text", Codec.STRING), (nameplate, s) -> nameplate.text = s, nameplate -> nameplate.text)
      .documentation("The contents to display as the nameplate text.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   @Nonnull
   private String text = "";
   private boolean isNetworkOutdated = true;

   @Nonnull
   public static ComponentType<EntityStore, Nameplate> getComponentType() {
      return EntityModule.get().getNameplateComponentType();
   }

   public Nameplate() {
   }

   public Nameplate(@Nonnull String text) {
      this.text = text;
   }

   @Nonnull
   public String getText() {
      return this.text;
   }

   public void setText(@Nonnull String text) {
      if (!this.text.equals(text)) {
         this.text = text;
         this.isNetworkOutdated = true;
      }
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      Nameplate nameplate = new Nameplate();
      nameplate.text = this.text;
      return nameplate;
   }
}
