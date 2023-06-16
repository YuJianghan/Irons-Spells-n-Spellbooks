package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.network.ClientboundUpdateCastingState;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Collection;

public class CastCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = dispatcher.register(Commands.literal("cast")
                .requires((p) -> p.hasPermission(2))
                .then(Commands.argument("casters", EntityArgument.entities())
                        .then(Commands.argument("spell", EnumArgument.enumArgument(SpellType.class))
                                .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", SpellType.class)))
                                .then(Commands.literal("level")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                                .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", SpellType.class), IntegerArgumentType.getInteger(context, "value"))))
                                        .then(Commands.argument("function value", FunctionArgument.functions())
                                                .executes((context) -> castSpell(context.getSource(), EntityArgument.getEntities(context, "casters"), context.getArgument("spell", SpellType.class), FunctionArgument.getFunctions(context, "function value"))))
                                )


                        ))

        );
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, SpellType spellType, Collection<CommandFunction> functions) {
        int i = 0;

        for (CommandFunction commandfunction : functions) {
            i += source.getServer().getFunctions().execute(commandfunction, source.withSuppressedOutput().withMaximumPermission(2));
        }
        return castSpell(source, targets, spellType, i);
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, SpellType spellType) {
        return castSpell(source, targets, spellType, 1);
    }

    private static int castSpell(CommandSourceStack source, Collection<? extends Entity> targets, SpellType spellType, int level) {
        AbstractSpell spell = spellType.getSpellForType(level);
        for (Entity target : targets) {
            if (target instanceof ServerPlayer serverPlayer) {
                spell.attemptInitiateCast(ItemStack.EMPTY, source.getLevel(), serverPlayer, CastSource.NONE, false);
            } else if (target instanceof AbstractSpellCastingMob castingMob) {
                castingMob.initiateCastSpell(spellType, level);
            }else  if (target instanceof LivingEntity livingEntity){

                var playerMagicData = PlayerMagicData.getPlayerMagicData(livingEntity);
                if (!spell.checkPreCastConditions(source.getLevel(), livingEntity, playerMagicData)) {
                    return 0;
                }

                if (spell.getCastType() == CastType.INSTANT) {
                    spell.onCast(source.getLevel(),livingEntity,playerMagicData);
                        spell.onServerCastComplete(source.getLevel(), livingEntity, playerMagicData, false);
                } else {
                    int effectiveCastTime = spell.getEffectiveCastTime(livingEntity);
                    playerMagicData.initiateCast(spellType.getValue(), spell.getRawLevel(), effectiveCastTime, CastSource.MOB);
                    spell.onServerPreCast(source.getLevel(), livingEntity, playerMagicData);
                }
            }
        }
        return 1;
    }

}
