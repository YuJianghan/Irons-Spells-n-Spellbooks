package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.damagesource.DamageSource;

public class SpellDamageSource extends DamageSource implements ISpellDamageSource {
    public SpellDamageSource(AbstractSpell spell) {
        super(spell.getDeathMessageId());
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
