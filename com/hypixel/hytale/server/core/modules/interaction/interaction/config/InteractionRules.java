package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionRules implements NetworkSerializable<com.hypixel.hytale.protocol.InteractionRules> {
   @Nonnull
   public static final BuilderCodec<InteractionRules> CODEC = BuilderCodec.builder(InteractionRules.class, InteractionRules::new)
      .appendInherited(
         new KeyedCodec<>("BlockedBy", InteractionModule.INTERACTION_TYPE_SET_CODEC),
         (o, i) -> o.blockedBy = i,
         o -> o.blockedBy,
         (o, p) -> o.blockedBy = p.blockedBy
      )
      .documentation(
         "A collection of interaction types that should block this interaction from starting. If not set then a set of default rules will be applied based on the interaction type that theinteraction is fired with.\nThis is only effective when used on the root interaction of a chain."
      )
      .add()
      .<Set<InteractionType>>appendInherited(
         new KeyedCodec<>("Blocking", InteractionModule.INTERACTION_TYPE_SET_CODEC),
         (o, i) -> o.blocking = i,
         o -> o.blocking,
         (o, p) -> o.blocking = p.blocking
      )
      .documentation(
         "A collection of interaction types that this interaction blocks from starting whilst running.\nDefaults to an empty set (blocking nothing)."
      )
      .addValidator(Validators.nonNull())
      .add()
      .<Set<InteractionType>>appendInherited(
         new KeyedCodec<>("InterruptedBy", InteractionModule.INTERACTION_TYPE_SET_CODEC),
         (o, i) -> o.interruptedBy = i,
         o -> o.interruptedBy,
         (o, p) -> o.interruptedBy = p.interruptedBy
      )
      .documentation(
         "A collection of interaction types that should stop this interaction while it's running.\nThis is only effective when used on the root interaction of a chain."
      )
      .add()
      .<Set<InteractionType>>appendInherited(
         new KeyedCodec<>("Interrupting", InteractionModule.INTERACTION_TYPE_SET_CODEC),
         (o, i) -> o.interrupting = i,
         o -> o.interrupting,
         (o, p) -> o.interrupting = p.interrupting
      )
      .documentation("A collection of interaction types that this interaction should stop when it starts.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockedByBypass", Codec.STRING),
         (o, i) -> o.blockedByBypass = i,
         o -> o.blockedByBypass,
         (o, p) -> o.blockedByBypass = p.blockedByBypass
      )
      .documentation("A tag that if matched will bypass the `BlockedBy` rules.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("BlockingBypass", Codec.STRING), (o, i) -> o.blockingBypass = i, o -> o.blockingBypass, (o, p) -> o.blockingBypass = p.blockingBypass
      )
      .documentation("A tag that if matched will bypass the `Blocking` rules.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("InterruptedByBypass", Codec.STRING),
         (o, i) -> o.interruptedByBypass = i,
         o -> o.interruptedByBypass,
         (o, p) -> o.interruptedByBypass = p.interruptedByBypass
      )
      .documentation("A tag that if matched will bypass the `InterruptedBy` rules.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("InterruptingBypass", Codec.STRING),
         (o, i) -> o.interruptingBypass = i,
         o -> o.interruptingBypass,
         (o, p) -> o.interruptingBypass = p.interruptingBypass
      )
      .documentation("A tag that if matched will bypass the `Interrupting` rules.")
      .add()
      .afterDecode(i -> {
         if (i.blockedByBypass != null) {
            i.blockedByBypassIndex = AssetRegistry.getOrCreateTagIndex(i.blockedByBypass);
         }

         if (i.blockingBypass != null) {
            i.blockingBypassIndex = AssetRegistry.getOrCreateTagIndex(i.blockingBypass);
         }

         if (i.interruptedByBypass != null) {
            i.interruptedByBypassIndex = AssetRegistry.getOrCreateTagIndex(i.interruptedByBypass);
         }

         if (i.interruptingBypass != null) {
            i.interruptingBypassIndex = AssetRegistry.getOrCreateTagIndex(i.interruptingBypass);
         }
      })
      .build();
   @Nonnull
   public static InteractionRules DEFAULT_RULES = new InteractionRules();
   @Nullable
   protected Set<InteractionType> blockedBy;
   @Nonnull
   protected Set<InteractionType> blocking = Collections.emptySet();
   @Nullable
   protected Set<InteractionType> interruptedBy;
   @Nullable
   protected Set<InteractionType> interrupting;
   @Nullable
   protected String blockedByBypass;
   protected int blockedByBypassIndex = Integer.MIN_VALUE;
   @Nullable
   protected String blockingBypass;
   protected int blockingBypassIndex = Integer.MIN_VALUE;
   @Nullable
   protected String interruptedByBypass;
   protected int interruptedByBypassIndex = Integer.MIN_VALUE;
   @Nullable
   protected String interruptingBypass;
   protected int interruptingBypassIndex = Integer.MIN_VALUE;

   public boolean validateInterrupts(
      @Nonnull InteractionType type,
      @Nonnull Int2ObjectMap<IntSet> selfTags,
      @Nonnull InteractionType otherType,
      @Nonnull Int2ObjectMap<IntSet> otherTags,
      @Nonnull InteractionRules otherRules
   ) {
      return otherRules.interruptedBy == null
            || !otherRules.interruptedBy.contains(type)
            || otherRules.interruptedByBypassIndex != Integer.MIN_VALUE && selfTags.containsKey(otherRules.interruptedByBypassIndex)
         ? this.interrupting != null
            && this.interrupting.contains(otherType)
            && (this.interruptingBypassIndex == Integer.MIN_VALUE || !otherTags.containsKey(this.interruptingBypassIndex))
         : true;
   }

   public boolean validateBlocked(
      @Nonnull InteractionType type,
      @Nonnull Int2ObjectMap<IntSet> selfTags,
      @Nonnull InteractionType otherType,
      @Nonnull Int2ObjectMap<IntSet> otherTags,
      @Nonnull InteractionRules otherRules
   ) {
      Set<InteractionType> blockedBy = this.blockedBy != null ? this.blockedBy : InteractionTypeUtils.DEFAULT_INTERACTION_BLOCKED_BY.get(type);
      return !blockedBy.contains(otherType) || this.blockedByBypassIndex != Integer.MIN_VALUE && otherTags.containsKey(this.blockedByBypassIndex)
         ? otherRules.blocking.contains(type) && (otherRules.blockingBypassIndex == Integer.MIN_VALUE || !selfTags.containsKey(otherRules.blockingBypassIndex))
         : true;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.InteractionRules toPacket() {
      com.hypixel.hytale.protocol.InteractionRules packet = new com.hypixel.hytale.protocol.InteractionRules();
      packet.blockedBy = this.blockedBy == null ? null : this.blockedBy.toArray(InteractionType[]::new);
      packet.blocking = this.blocking.isEmpty() ? null : this.blocking.toArray(InteractionType[]::new);
      packet.interruptedBy = this.interruptedBy == null ? null : this.interruptedBy.toArray(InteractionType[]::new);
      packet.interrupting = this.interrupting == null ? null : this.interrupting.toArray(InteractionType[]::new);
      packet.blockedByBypassIndex = this.blockedByBypassIndex;
      packet.blockingBypassIndex = this.blockingBypassIndex;
      packet.interruptedByBypassIndex = this.interruptedByBypassIndex;
      packet.interruptingBypassIndex = this.interruptingBypassIndex;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "InteractionRules{blockedBy="
         + this.blockedBy
         + ", blocking="
         + this.blocking
         + ", interruptedBy="
         + this.interruptedBy
         + ", interrupting="
         + this.interrupting
         + ", blockedByBypass='"
         + this.blockedByBypass
         + "', blockedByBypassIndex="
         + this.blockedByBypassIndex
         + ", blockingBypass='"
         + this.blockingBypass
         + "', blockingBypassIndex="
         + this.blockingBypassIndex
         + ", interruptedByBypass='"
         + this.interruptedByBypass
         + "', interruptedByBypassIndex="
         + this.interruptedByBypassIndex
         + ", interruptingBypass='"
         + this.interruptingBypass
         + "', interruptingBypassIndex="
         + this.interruptedByBypassIndex
         + "}";
   }
}
