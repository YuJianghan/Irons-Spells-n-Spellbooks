package io.redspace.ironsspellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class SonicBoomSpell extends AbstractEldritchSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "sonic_boom");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(25)
            .build();

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(spellLevel, caster), 1))
        );
    }

    public SonicBoomSpell() {
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 8;
        this.castTime = 30;
        this.baseManaCost = 150;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.WARDEN_SONIC_CHARGE);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.SONIC_BOOM.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        CameraShakeManager.addCameraShake(new CameraShakeData(10, entity.position(), 20));
        var hitResult = Utils.raycastForEntity(level, entity, getRange(spellLevel, entity), false, .25f);
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            if (target instanceof LivingEntity) {
                if (DamageSources.applyDamage(target, getDamage(spellLevel, entity), getDamageSource(entity))) {
                    //TODO: some kind of on hit effect?
                    //deafen warden or something would be cool
                }
            }
        }
        float distance = (float) hitResult.distanceTo(entity);
        Vec3 vec3 = entity.getLookAngle().normalize();
        for (int i = 0; i < distance; i++) {
            var vec32 = vec3.scale(i).add(entity.getEyePosition());
            MagicManager.spawnParticles(level, ParticleTypes.SONIC_BOOM, vec32.x, vec32.y, vec32.z, 1, 0, 0, 0, 0, false);
        }
        super.onCast(level, spellLevel, entity, playerMagicData);
    }

    @Override
    protected void playSound(Optional<SoundEvent> sound, Entity entity, boolean playDefaultSound) {
        if (sound == getCastFinishSound()) {
            entity.playSound(sound.get(), 3.5f, .9f + entity.level.random.nextFloat() * .2f);
        } else {
            super.playSound(sound, entity, playDefaultSound);
        }

    }

    public static float getRange(int level, LivingEntity caster) {
        return 15 + 5 * level;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_SPIT_ANIMATION;
    }
}
