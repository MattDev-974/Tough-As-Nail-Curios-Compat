package com.example.tancurios.temperature;

import com.example.tancurios.TanCuriosMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import toughasnails.api.temperature.IPlayerTemperatureModifier;
import toughasnails.api.temperature.TemperatureLevel;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Modifie la température du joueur en tenant compte des armures TAN
 * équipées dans les slots Curios.
 */
public class TANTemperatureModifier implements IPlayerTemperatureModifier {

    @Override
    public TemperatureLevel modify(Player player, TemperatureLevel current) {
        // Récupérer les armures TAN dans les slots Curios
        var curiosHandler = CuriosApi.getCuriosInventory(player);
        if (curiosHandler.isEmpty()) return current;

        int warmingPieces = 0;  // armures qui réchauffent (wool, fur, hide)
        int coolingPieces = 0;  // armures qui refroidissent (leaf, cooling)

        var stacks = curiosHandler.get().findCurios(stack ->
            stack.getItem() instanceof ArmorItem &&
            isFromTAN(stack)
        );

        for (var slotResult : stacks) {
            ItemStack stack = slotResult.stack();
            String itemId = getItemId(stack);

            // Évite le double comptage si la même pièce d'armure (par type)
            // est aussi équipée dans le slot vanilla correspondant : TAN
            // applique déjà son propre effet thermique pour celle-ci.
            if (stack.getItem() instanceof ArmorItem curiosArmor && isAlsoWornVanilla(player, curiosArmor)) {
                continue;
            }

            if (itemId.contains("wool") || itemId.contains("fur") || itemId.contains("hide") || itemId.contains("thermal")) {
                warmingPieces++;
            } else if (itemId.contains("leaf") || itemId.contains("cooling") || itemId.contains("straw")) {
                coolingPieces++;
            }
        }

        if (warmingPieces == 0 && coolingPieces == 0) return current;

        // Sécurité : on ne compte jamais plus que le nombre de slots d'armure
        // logiques (casque/plastron/jambières/bottes = 4).
        final int MAX_PIECES = 4;
        warmingPieces = Math.min(warmingPieces, MAX_PIECES);
        coolingPieces = Math.min(coolingPieces, MAX_PIECES);

        TanCuriosMod.LOGGER.debug("[TAN-Curios] Modificateur température: +chaud={} +froid={} base={}",
                warmingPieces, coolingPieces, current);

        // On détecte le sens réel de l'échelle en comparant les noms des
        // constantes ("ICY"/"COLD" vs "HOT"/"SCORCHING"), pour ne jamais se
        // tromper de direction quel que soit l'ordre de déclaration côté TAN.
        TemperatureLevel[] values = TemperatureLevel.values();
        int coldEnd = findColdEndIndex(values);
        int hotEnd = (coldEnd == 0) ? values.length - 1 : 0;
        int directionTowardHot = (hotEnd > coldEnd) ? 1 : -1;

        // IMPORTANT : on ne décale plus d'un niveau ENTIER de TemperatureLevel
        // par pièce d'armure. Un seul niveau de TemperatureLevel représente
        // déjà une portion énorme de l'échelle totale (ex: 1/5e ou 1/7e), donc
        // sauter un niveau complet par pièce rendait l'effet beaucoup trop
        // fort (un seul casque en laine surchauffait, une seule pièce en
        // feuille gelait). À la place, l'intensité augmente par PALIERS selon
        // le nombre net de pièces, et est plafonnée à 2 niveaux maximum même
        // avec 4 pièces du même type :
        //   1-2 pièces nettes -> décalage de 1 niveau
        //   3-4 pièces nettes -> décalage de 2 niveaux (effet plus marqué,
        //                         mais jamais démesuré)
        int netWarmth = warmingPieces - coolingPieces;
        int magnitude = Math.abs(netWarmth);
        int shift;
        if (magnitude == 0) {
            shift = 0;
        } else if (magnitude <= 2) {
            shift = 1;
        } else {
            shift = 2;
        }
        shift *= Integer.signum(netWarmth);

        int ordinal = current.ordinal();
        ordinal += directionTowardHot * shift;

        // Clamp entre les valeurs valides de TemperatureLevel
        ordinal = Math.max(0, Math.min(values.length - 1, ordinal));

        return values[ordinal];
    }

    /**
     * Retourne l'index, dans TemperatureLevel.values(), du niveau le plus
     * froid, en se basant sur le nom de la constante plutôt que sur sa
     * position. On ne fait aucune hypothèse sur l'ordre de déclaration côté
     * Tough As Nails, qui peut différer d'une version à l'autre.
     */
    private static int findColdEndIndex(TemperatureLevel[] values) {
        for (int i = 0; i < values.length; i++) {
            String name = values[i].name().toUpperCase();
            if (name.contains("ICY") || name.contains("COLD") || name.contains("FREEZ")) {
                return i;
            }
        }
        // Si aucun nom ne correspond à un repère froid connu, on suppose
        // (comme avant) que l'index 0 est le plus froid.
        return 0;
    }

    private boolean isAlsoWornVanilla(Player player, ArmorItem curiosArmor) {
        var equipmentSlot = switch (curiosArmor.getType()) {
            case HELMET     -> net.minecraft.world.entity.EquipmentSlot.HEAD;
            case CHESTPLATE -> net.minecraft.world.entity.EquipmentSlot.CHEST;
            case LEGGINGS   -> net.minecraft.world.entity.EquipmentSlot.LEGS;
            case BOOTS      -> net.minecraft.world.entity.EquipmentSlot.FEET;
            default         -> null;
        };
        if (equipmentSlot == null) return false;

        ItemStack vanillaStack = player.getItemBySlot(equipmentSlot);
        if (vanillaStack.isEmpty() || !(vanillaStack.getItem() instanceof ArmorItem vanillaArmor)) return false;
        if (vanillaArmor.getType() != curiosArmor.getType()) return false;

        return isFromTAN(vanillaStack);
    }

    private boolean isFromTAN(ItemStack stack) {
        var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null && key.getNamespace().equals("toughasnails");
    }

    private String getItemId(ItemStack stack) {
        var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null ? key.getPath() : "";
    }
}
