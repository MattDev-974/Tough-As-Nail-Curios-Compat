package com.example.tancurios.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class TANCuriosRenderer implements ICurioRenderer {

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource multiBufferSource,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        if (!(stack.getItem() instanceof ArmorItem armorItem)) return;
        if (!(slotContext.entity() instanceof LivingEntity entity)) return;

        EquipmentSlot vanillaSlot = armorItem.getEquipmentSlot();

        // Ne rendre que si le slot vanilla est vide
        if (!entity.getItemBySlot(vanillaSlot).isEmpty()) return;

        renderArmorPiece(stack, vanillaSlot, entity, poseStack, multiBufferSource, light,
                limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void renderArmorPiece(
            ItemStack stack,
            EquipmentSlot slot,
            LivingEntity entity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        entity.setItemSlot(slot, stack);
        try {
            var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            if (!(renderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer lr)) return;

            for (Object layer : lr.layers) {
                if (layer instanceof HumanoidArmorLayer armorLayer) {
                    armorLayer.render(poseStack, buffer, light, entity,
                            limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
                    break;
                }
            }
        } finally {
            entity.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}
