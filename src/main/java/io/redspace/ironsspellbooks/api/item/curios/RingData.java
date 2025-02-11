package io.redspace.ironsspellbooks.api.item.curios;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class RingData {
    public static final String ISB_ENHANCE = "ISBEnhance";
    String spellId;

    private RingData(String id) {
        this.spellId = id;
    }

    public static RingData getRingData(ItemStack stack) {
        if (hasRingData(stack)) {
            return new RingData(stack.getOrCreateTag().getString(ISB_ENHANCE));
        } else {
            return new RingData(SpellRegistry.none().getSpellId());
        }
    }

    public static void setRingData(ItemStack stack, AbstractSpell spell) {
        var spellTag = stack.getOrCreateTag();
        spellTag.putString(ISB_ENHANCE, spell.getSpellId());
    }

    public static boolean hasRingData(ItemStack itemStack) {
        return itemStack.getTag() != null && itemStack.getTag().contains(ISB_ENHANCE);
    }

    public AbstractSpell getSpell() {
        return SpellRegistry.getSpell(spellId);
    }

    public String getNameForItem(){
        return getSpell() == SpellRegistry.none() ? Component.translatable("tooltip.irons_spellbooks.no_affinity").getString() : getSpell().getSchoolType().getDisplayName().getString();
    }
}
