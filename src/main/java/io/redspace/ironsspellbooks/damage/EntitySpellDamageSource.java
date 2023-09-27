package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;

public class EntitySpellDamageSource extends EntityDamageSource implements ISpellDamageSource {
    public EntitySpellDamageSource(AbstractSpell spell, Entity entity) {
        super(spell.getDeathMessageId(), entity);
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
