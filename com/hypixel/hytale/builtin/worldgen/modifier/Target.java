package com.hypixel.hytale.builtin.worldgen.modifier;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.common.util.StringUtil;
import javax.annotation.Nonnull;

public class Target {
   public static final BuilderCodec<Target> CODEC = BuilderCodec.builder(Target.class, Target::new)
      .documentation("Configures the world-gen root and content path rules that should be targeted by the WorldGenModifier")
      .<String>append(new KeyedCodec<>("Root", BuilderCodec.STRING), (instance, root) -> instance.root = root, instance -> instance.root)
      .documentation("The name of the world-gen root configuration folder to target")
      .add()
      .<String[]>append(new KeyedCodec<>("Rules", BuilderCodec.STRING_ARRAY), (instance, paths) -> instance.rules = paths, instance -> instance.rules)
      .documentation("A list of glob-matching path rules to match world-gen asset files against")
      .add()
      .build();
   @Nonnull
   private String root = "Default";
   @Nonnull
   private String[] rules = ArrayUtil.EMPTY_STRING_ARRAY;

   public boolean matchesRoot(@Nonnull String name) {
      return this.root.equals(name);
   }

   public boolean matchesRule(@Nonnull String path) {
      for (String rule : this.rules) {
         if (!rule.isEmpty() && StringUtil.isGlobMatching(rule, path)) {
            return true;
         }
      }

      return false;
   }
}
