package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class IndirectSpellDamageSource extends IndirectEntityDamageSource implements ISpellDamageSource {
    public IndirectSpellDamageSource(AbstractSpell spell, Entity pSource, @Nullable Entity pIndirectEntity) {
        super(spell.getDeathMessageId(), pSource, pIndirectEntity);
        this.schoolType = spell.getSchoolType();
    }

    float lifesteal;
    SchoolType schoolType;

    @Override
    public float getLifesteal() {
        return lifesteal;
    }

    @Override
    public DamageSource setLifesteal(float lifesteal) {
        this.lifesteal = lifesteal;
        return this;
    }

    @Override
    public SchoolType getSchool() {
        return schoolType;
    }

    @Override
    public DamageSource cast() {
        return this;
    }
}
