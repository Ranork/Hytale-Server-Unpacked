package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SetDensity extends SequenceBrushOperation {
   public static final BuilderCodec<SetDensity> CODEC = BuilderCodec.builder(SetDensity.class, SetDensity::new)
      .append(new KeyedCodec<>("Density", Codec.INTEGER), (op, val) -> op.density = val, op -> op.density)
      .documentation("Changes the likelyhood that a given block will be processed")
      .add()
      .documentation(
         "Sets the random chance that any given block being set will actually get set, otherwise getting cancelled. Ex: a value of 30 is a 30% chance blocks will appear with a set operation."
      )
      .build();
   public Integer density = 100;

   public SetDensity() {
      super(
         "Density",
         "Sets the random chance that any given block being set will actually get set, otherwise getting cancelled. Ex: a value of 30 is a 30% chance blocks will appear with a set operation.",
         false
      );
   }

   @Override
   public void modifyBrushConfig(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull BrushConfig brushConfig,
      @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      brushConfig.setDensity(MathUtil.clamp(this.density, 1, 100));
   }
}
