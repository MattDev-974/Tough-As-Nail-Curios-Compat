package com.example.tancurios.curios;

import com.example.tancurios.TanCuriosMod;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * ICurio implementation pour les armures Tough As Nails.
 * Compatible avec Curios API 9.5.x (NeoForge 1.21.1)
 */
public class TANCuriosItem implements ICurio {

    private final ItemStack stack;

    public TANCuriosItem(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Applique les modificateurs d'attributs de l'armure TAN dans le slot Curios.
     * Curios 9.5+ utilise Holder<Attribute> et ResourceLocation au lieu de UUID.
     */
    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();

        if (!(stack.getItem() instanceof ArmorItem armorItem)) return modifiers;

        // Défense (valeur d'armure)
        int defense = armorItem.getDefense();
        if (defense > 0) {
            modifiers.put(
                Attributes.ARMOR,
                new AttributeModifier(
                    id,
                    defense,
                    AttributeModifier.Operation.ADD_VALUE
                )
            );
        }

        // Solidité de l'armure (toughness)
        float toughness = armorItem.getToughness();
        if (toughness > 0) {
            modifiers.put(
                Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_toughness"),
                    toughness,
                    AttributeModifier.Operation.ADD_VALUE
                )
            );
        }

        // Résistance aux knockbacks
        float knockback = armorItem.getMaterial().value().knockbackResistance();
        if (knockback > 0) {
            modifiers.put(
                Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_knockback"),
                    knockback,
                    AttributeModifier.Operation.ADD_VALUE
                )
            );
        }

        TanCuriosMod.LOGGER.debug("[TAN-Curios] Attributs appliqués pour {} dans slot {}",
                BuiltInRegistries.ITEM.getKey(stack.getItem()), slotContext.identifier());

        return modifiers;
    }

    @Override
    public boolean canEquip(SlotContext slotContext) {
    if (!(stack.getItem() instanceof ArmorItem armorItem)) return false;
    return switch (armorItem.getType()) {
        case HELMET     -> slotContext.identifier().equals("tan_helmet");
        case CHESTPLATE -> slotContext.identifier().equals("tan_chestplate");
        case LEGGINGS   -> slotContext.identifier().equals("tan_leggings");
        case BOOTS      -> slotContext.identifier().equals("tan_boots");
        default         -> false;
        };
    }

    /**
     * Son lors de l'équipement - Curios 9.5 utilise Holder<SoundEvent>
     */
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            // equipSound() retourne un Holder<SoundEvent> en 1.21.1
            var soundHolder = armorItem.getMaterial().value().equipSound();
            return new ICurio.SoundInfo(soundHolder.value(), 1.0f, 1.0f);
        }
        // Pas de constante EMPTY en 9.5, on retourne null pour le son par défaut
        return null;
    }

    @Override
    public boolean canSync(SlotContext slotContext) {
        return true;
    }
}
