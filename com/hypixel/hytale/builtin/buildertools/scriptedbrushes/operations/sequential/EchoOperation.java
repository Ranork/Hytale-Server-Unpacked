package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EchoOperation extends SequenceBrushOperation {
   public static final BuilderCodec<EchoOperation> CODEC = BuilderCodec.builder(EchoOperation.class, EchoOperation::new)
      .append(new KeyedCodec<>("Message", Codec.STRING), (op, val) -> op.messageArg = val, op -> op.messageArg)
      .documentation("A message to print to chat when this operation is ran")
      .add()
      .documentation("Print some text to chat")
      .build();
   private String messageArg = "Default message";

   public EchoOperation() {
      super("Echo to Chat", "Print some text to chat", false);
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      playerRefComponent.sendMessage(Message.raw(this.messageArg));
   }
}
