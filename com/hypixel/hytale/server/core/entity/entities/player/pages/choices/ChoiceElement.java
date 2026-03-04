package com.hypixel.hytale.server.core.entity.entities.player.pages.choices;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import javax.annotation.Nonnull;

public abstract class ChoiceElement {
   public static final CodecMapCodec<ChoiceElement> CODEC = new CodecMapCodec<>("Type");
   public static final BuilderCodec<ChoiceElement> BASE_CODEC = BuilderCodec.abstractBuilder(ChoiceElement.class)
      .append(
         new KeyedCodec<>("DisplayNameKey", Codec.STRING),
         (choiceElement, s) -> choiceElement.displayNameKey = s,
         choiceElement -> choiceElement.displayNameKey
      )
      .addValidator(Validators.nonEmptyString())
      .add()
      .<String>append(
         new KeyedCodec<>("DescriptionKey", Codec.STRING),
         (choiceElement, s) -> choiceElement.descriptionKey = s,
         choiceElement -> choiceElement.descriptionKey
      )
      .addValidator(Validators.nonEmptyString())
      .add()
      .<ChoiceInteraction[]>append(
         new KeyedCodec<>("Interactions", new ArrayCodec<>(ChoiceInteraction.CODEC, ChoiceInteraction[]::new)),
         (choiceElement, choiceInteractions) -> choiceElement.interactions = choiceInteractions,
         choiceElement -> choiceElement.interactions
      )
      .addValidator(Validators.nonEmptyArray())
      .add()
      .append(
         new KeyedCodec<>("Requirements", new ArrayCodec<>(ChoiceRequirement.CODEC, ChoiceRequirement[]::new)),
         (choiceElement, choiceRequirements) -> choiceElement.requirements = choiceRequirements,
         choiceElement -> choiceElement.requirements
      )
      .add()
      .build();
   protected String displayNameKey;
   protected String descriptionKey;
   protected ChoiceInteraction[] interactions;
   protected ChoiceRequirement[] requirements;

   public ChoiceElement(String displayNameKey, String descriptionKey, ChoiceInteraction[] interactions, ChoiceRequirement[] requirements) {
      this.displayNameKey = displayNameKey;
      this.descriptionKey = descriptionKey;
      this.interactions = interactions;
      this.requirements = requirements;
   }

   protected ChoiceElement() {
   }

   public String getDisplayNameKey() {
      return this.displayNameKey;
   }

   public String getDescriptionKey() {
      return this.descriptionKey;
   }

   public ChoiceInteraction[] getInteractions() {
      return this.interactions;
   }

   public ChoiceRequirement[] getRequirements() {
      return this.requirements;
   }

   public abstract void addButton(UICommandBuilder var1, UIEventBuilder var2, String var3, PlayerRef var4);

   public boolean canFulfillRequirements(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef) {
      if (this.requirements == null) {
         return true;
      } else {
         for (ChoiceRequirement requirement : this.requirements) {
            if (!requirement.canFulfillRequirement(store, ref, playerRef)) {
               return false;
            }
         }

         return true;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChoiceElement{displayNameKey='"
         + this.displayNameKey
         + "', descriptionKey='"
         + this.descriptionKey
         + "', interactions="
         + Arrays.toString((Object[])this.interactions)
         + ", requirements="
         + Arrays.toString((Object[])this.requirements)
         + "}";
   }
}
