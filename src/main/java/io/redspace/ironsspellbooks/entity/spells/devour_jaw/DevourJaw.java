package io.redspace.ironsspellbooks.entity.spells.devour_jaw;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class DevourJaw extends AoeEntity {
    public DevourJaw(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    LivingEntity target;

    public DevourJaw(Level level, LivingEntity owner, LivingEntity target) {
        this(EntityRegistry.DEVOUR_JAW.get(), level);
        setOwner(owner);
        this.target = target;
    }

    //dont need to serialize, dont need it only client either
    public int vigorLevel;

    @Override
    public void applyEffect(LivingEntity target) {
        if (target == this.target)
            if (DamageSources.applyDamage(target, getDamage(), SpellType.DEVOUR_SPELL.getDamageSource(this, getOwner()), SpellType.DEVOUR_SPELL.getSchoolType()) && getOwner() instanceof LivingEntity livingOwner) {
                livingOwner.heal(getDamage() * .15f);
                if (target.isDeadOrDying()) {
                    var oldVigor = livingOwner.getEffect(MobEffectRegistry.VIGOR.get());
                    var addition = 0;
                    if (oldVigor != null)
                        addition = oldVigor.getAmplifier() + 1;
                    livingOwner.addEffect(new MobEffectInstance(MobEffectRegistry.VIGOR.get(), 20 * 60, vigorLevel + addition));
                    livingOwner.heal((vigorLevel + 1) * 2);
                }
            }

    }

    public final int waitTime = 5;
    public final int warmupTime = waitTime + 8;
    public final int deathTime = warmupTime + 8;

    @Override
    public void tick() {
        if (tickCount < waitTime) {
            if (this.target != null)
                setPos(this.target.position());
        } else if (tickCount == warmupTime) {
            if (level.isClientSide) {
                float y = this.getYRot();
                int countPerSide = 25;
                //These particles were not at all what I intended. But they're cooler. no clue how it works
                for (int i = -countPerSide; i < countPerSide; i++) {
                    Vec3 motion = new Vec3(0, Math.abs(countPerSide) - i, countPerSide * .5f).yRot(y).normalize().multiply(.4f, .8f, .4f);
                    level.addParticle(ParticleHelper.BLOOD, getX(), getY() + .5f, getZ(), motion.x, motion.y, motion.z);
                }
            } else {
                checkHits();
            }
        } else if (tickCount > deathTime)
            discard();
    }

    @Override
    protected float getInflation() {
        return 1f;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void refreshDimensions() {
        return;
    }

    @Override
    public void ambientParticles() {
        return;
    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleHelper.BLOOD;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
