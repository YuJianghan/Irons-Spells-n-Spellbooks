package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISpellDamageSource {

    float getLifesteal();

    DamageSource setLifesteal(float lifesteal);

    SchoolType getSchool();

    DamageSource cast();

    static ISpellDamageSource source(@NotNull AbstractSpell spell, @Nullable Entity causingEntity, @Nullable Entity directEntity) {
        if (directEntity != null) {
            return new IndirectSpellDamageSource(spell, causingEntity, directEntity);
        } else if (causingEntity != null) {
            return new EntitySpellDamageSource(spell, causingEntity);
        } else {
            return new SpellDamageSource(spell);
        }
    }

}
