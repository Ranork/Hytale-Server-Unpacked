package com.hypixel.hytale.server.core.universe.world.chunk;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.store.StoredCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public class ChunkColumn implements Component<ChunkStore> {
   public static final BuilderCodec<ChunkColumn> CODEC = BuilderCodec.builder(ChunkColumn.class, ChunkColumn::new)
      .append(
         new KeyedCodec<>("Sections", new ArrayCodec<>(new StoredCodec<>(ChunkStore.HOLDER_CODEC_KEY), Holder[]::new)),
         (chunk, holders) -> chunk.sectionHolders = holders,
         chunk -> {
            int length = chunk.sections.length;
            if (chunk.sectionHolders != null) {
               length = Math.max(chunk.sectionHolders.length, chunk.sections.length);
            }

            Holder<ChunkStore>[] array = new Holder[length];
            if (chunk.sectionHolders != null) {
               System.arraycopy(chunk.sectionHolders, 0, array, 0, chunk.sectionHolders.length);
            }

            for (int i = 0; i < chunk.sections.length; i++) {
               Ref<ChunkStore> section = chunk.sections[i];
               if (section == null) {
                  break;
               }

               Store<ChunkStore> store = section.getStore();
               array[i] = store.copySerializableEntity(section);
            }

            return array;
         }
      )
      .add()
      .build();
   private final Ref<ChunkStore>[] sections = new Ref[10];
   @Nullable
   private Holder<ChunkStore>[] sectionHolders;

   public static ComponentType<ChunkStore, ChunkColumn> getComponentType() {
      return LegacyModule.get().getChunkColumnComponentType();
   }

   public ChunkColumn() {
   }

   public ChunkColumn(Holder<ChunkStore>[] sectionHolders) {
      this.sectionHolders = sectionHolders;
   }

   @Nullable
   public Ref<ChunkStore> getSection(int section) {
      return section >= 0 && section < this.sections.length ? this.sections[section] : null;
   }

   @Nonnull
   public Ref<ChunkStore>[] getSections() {
      return this.sections;
   }

   @Nullable
   public Holder<ChunkStore>[] getSectionHolders() {
      return this.sectionHolders;
   }

   @Nullable
   public Holder<ChunkStore>[] takeSectionHolders() {
      Holder<ChunkStore>[] temp = this.sectionHolders;
      this.sectionHolders = null;
      return temp;
   }

   public void putSectionHolders(Holder<ChunkStore>[] holders) {
      this.sectionHolders = holders;
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      ChunkColumn newChunk = new ChunkColumn();
      int length = this.sections.length;
      if (this.sectionHolders != null) {
         length = Math.max(this.sectionHolders.length, this.sections.length);
      }

      Holder<ChunkStore>[] holders = new Holder[length];
      if (this.sectionHolders != null) {
         for (int i = 0; i < this.sectionHolders.length; i++) {
            Holder<ChunkStore> sectionHolder = this.sectionHolders[i];
            if (sectionHolder != null) {
               holders[i] = sectionHolder.clone();
            }
         }
      }

      for (int ix = 0; ix < this.sections.length; ix++) {
         Ref<ChunkStore> section = this.sections[ix];
         if (section != null) {
            holders[ix] = section.getStore().copyEntity(section);
         }
      }

      newChunk.sectionHolders = holders;
      return newChunk;
   }

   @Nonnull
   @Override
   public Component<ChunkStore> cloneSerializable() {
      ChunkColumn newChunk = new ChunkColumn();
      int length = this.sections.length;
      if (this.sectionHolders != null) {
         length = Math.max(this.sectionHolders.length, this.sections.length);
      }

      Holder<ChunkStore>[] holders = new Holder[length];
      if (this.sectionHolders != null) {
         for (int i = 0; i < this.sectionHolders.length; i++) {
            Holder<ChunkStore> sectionHolder = this.sectionHolders[i];
            if (sectionHolder != null) {
               holders[i] = sectionHolder.clone();
            }
         }
      }

      for (int ix = 0; ix < this.sections.length; ix++) {
         Ref<ChunkStore> section = this.sections[ix];
         if (section != null) {
            holders[ix] = section.getStore().copySerializableEntity(section);
         }
      }

      newChunk.sectionHolders = holders;
      return newChunk;
   }
}
