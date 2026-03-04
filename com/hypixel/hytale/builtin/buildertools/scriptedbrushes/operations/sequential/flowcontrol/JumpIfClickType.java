package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class JumpIfClickType extends SequenceBrushOperation {
   public static final BuilderCodec<JumpIfClickType> CODEC = BuilderCodec.builder(JumpIfClickType.class, JumpIfClickType::new)
      .append(new KeyedCodec<>("StoredIndexName", Codec.STRING), (op, val) -> op.indexVariableNameArg = val, op -> op.indexVariableNameArg)
      .documentation("The labeled index to jump to, previous or future")
      .add()
      .<JumpIfClickType.ClickType>append(
         new KeyedCodec<>("ClickType", new EnumCodec<>(JumpIfClickType.ClickType.class)), (op, val) -> op.clickTypeArg = val, op -> op.clickTypeArg
      )
      .documentation("The click type (left or right) to compare with to jump")
      .add()
      .documentation("Jump the execution of the stack based on the click type")
      .build();
   @Nonnull
   public String indexVariableNameArg = "Undefined";
   @Nonnull
   public JumpIfClickType.ClickType clickTypeArg = JumpIfClickType.ClickType.Left;

   public JumpIfClickType() {
      super("Jump If Click Type", "Jump the execution of the stack based on the click type", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.clickTypeArg.equals(JumpIfClickType.ClickType.Left) && brushConfig.getInteractionType().equals(InteractionType.Primary)) {
         brushConfigCommandExecutor.loadOperatingIndex(this.indexVariableNameArg);
      } else if (this.clickTypeArg.equals(JumpIfClickType.ClickType.Right) && brushConfig.getInteractionType().equals(InteractionType.Secondary)) {
         brushConfigCommandExecutor.loadOperatingIndex(this.indexVariableNameArg);
      }
   }

   public static enum ClickType {
      Left,
      Right;
   }
}
