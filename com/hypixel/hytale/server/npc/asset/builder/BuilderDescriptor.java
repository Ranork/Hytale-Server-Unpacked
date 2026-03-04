package com.hypixel.hytale.server.npc.asset.builder;

import com.hypixel.hytale.server.npc.asset.builder.providerevaluators.ProviderEvaluator;
import com.hypixel.hytale.server.npc.asset.builder.validators.Validator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;

public class BuilderDescriptor {
   private final String name;
   private final String category;
   private final BuilderDescriptorState state;
   private final String shortDescription;
   private final String longDescription;
   private final List<BuilderAttributeDescriptor> attributes = new ObjectArrayList();
   private final List<Validator> validators = new ObjectArrayList();
   private final List<ProviderEvaluator> providerEvaluators = new ObjectArrayList();
   private final Set<String> tags;

   public BuilderDescriptor(String name, String category, String shortDescription, String longDescription, Set<String> tags, BuilderDescriptorState state) {
      this.name = name;
      this.category = category;
      this.shortDescription = shortDescription;
      this.longDescription = longDescription;
      this.state = state;
      this.tags = tags;
   }

   public BuilderAttributeDescriptor addAttribute(BuilderAttributeDescriptor attributeDescriptor) {
      this.attributes.add(attributeDescriptor);
      return attributeDescriptor;
   }

   public BuilderAttributeDescriptor addAttribute(String name, String type, BuilderDescriptorState state, String shortDescription, String longDescription) {
      return this.addAttribute(new BuilderAttributeDescriptor(name, type, state, shortDescription, longDescription));
   }

   public void addValidator(Validator validator) {
      this.validators.add(validator);
   }

   public void addProviderEvaluator(ProviderEvaluator providerEvaluator) {
      this.providerEvaluators.add(providerEvaluator);
   }
}
