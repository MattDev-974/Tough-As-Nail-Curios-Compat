package com.example.tancurios.curios;

import com.example.tancurios.TanCuriosMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Empêche de cumuler l'effet thermique en portant la même pièce d'armure TAN
 * à la fois en slot vanilla et en slot Curios.
 *
 * TANCuriosItem#canEquip bloque déjà le sens "équiper en Curios alors que la
 * pièce équivalente est déjà portée en vanilla". Mais l'inverse n'était pas
 * couvert : rien n'empêchait d'équiper l'armure en vanilla (clic-droit,
 * glisser-déposer dans l'inventaire, etc.) pendant qu'elle était déjà dans le
 * slot Curios, car l'équipement vanilla ne passe pas par ICurio#canEquip.
 *
 * Ce handler écoute donc les changements d'équipement vanilla et éjecte
 * automatiquement la pièce Curios correspondante si elle est du même type et
 * vient aussi de Tough As Nails, pour qu'il soit impossible d'avoir les deux
 * en même temps.
 */
@EventBusSubscriber(modid = TanCuriosMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TANEquipmentDuplicateHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide) return;

        EquipmentSlot slot = event.getSlot();
        if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST
                && slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) {
            return;
        }

        ItemStack newVanillaStack = event.getTo();
        if (newVanillaStack.isEmpty() || !(newVanillaStack.getItem() instanceof ArmorItem newArmor)) return;
        if (!isFromTAN(newVanillaStack)) return;

        String curiosSlotId = switch (newArmor.getType()) {
            case HELMET     -> "tan_helmet";
            case CHESTPLATE -> "tan_chestplate";
            case LEGGINGS   -> "tan_leggings";
            case BOOTS      -> "tan_boots";
            default         -> null;
        };
        if (curiosSlotId == null) return;

        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
            handler.getStacksHandler(curiosSlotId).ifPresent(stacksHandler -> {
                var stackHandler = stacksHandler.getStacks();
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack curioStack = stackHandler.getStackInSlot(i);
                    if (curioStack.isEmpty()
                            || !(curioStack.getItem() instanceof ArmorItem curioArmor)
                            || curioArmor.getType() != newArmor.getType()
                            || !isFromTAN(curioStack)) {
                        continue;
                    }

                    TanCuriosMod.LOGGER.debug(
                        "[TAN-Curios] Armure {} équipée en vanilla : éjection du doublon Curios pour éviter le cumul de température.",
                        BuiltInRegistries.ITEM.getKey(newArmor.asItem()));

                    // Retire la pièce du slot Curios et la rend au joueur
                    // (dans l'inventaire, ou par terre si l'inventaire est plein)
                    // plutôt que de la faire simplement disparaître.
                    ItemStack removed = stackHandler.extractItem(i, curioStack.getCount(), false);
                    if (!removed.isEmpty() && !player.getInventory().add(removed)) {
                        player.drop(removed, false);
                    }
                }
            })
        );
    }

    private static boolean isFromTAN(ItemStack stack) {
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null && key.getNamespace().equals("toughasnails");
    }
}
