package io.redspace.ironsspellbooks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
//import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

import java.util.stream.Collectors;

public class CreateImbuedSwordCommand {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_imbued_sword.failed"));

    private static final SuggestionProvider<CommandSourceStack> SWORD_SUGGESTIONS = (p_180253_, p_180254_) -> {
        var resources = Registry.ITEM.stream().filter((k) -> k instanceof SwordItem).map(Registry.ITEM::getKey).collect(Collectors.toSet());
        return SharedSuggestionProvider.suggestResource(resources, p_180254_);
    };

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext context) {

        pDispatcher.register(Commands.literal("createImbuedSword").requires((commandSourceStack) -> {
            return commandSourceStack.hasPermission(2);
        }).then(Commands.argument("item", ItemArgument.item(context)).suggests(SWORD_SUGGESTIONS)
                .then(Commands.argument("spell", SpellArgument.spellArgument())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1)).executes((ctx) -> {
                            return createImbuedSword(ctx.getSource(), ctx.getArgument("item", ItemInput.class), ctx.getArgument("spell", String.class), IntegerArgumentType.getInteger(ctx, "level"));
                        })))));
    }

    private static int createImbuedSword(CommandSourceStack source, ItemInput itemInput, String spell, int spellLevel) throws CommandSyntaxException {
        if (!spell.contains(":")) {
            spell = IronsSpellbooks.MODID + ":" + spell;
        }

        var abstractSpell = SpellRegistry.REGISTRY.get().getValue(new ResourceLocation(spell));

        if (spellLevel > abstractSpell.getMaxLevel()) {
            throw new SimpleCommandExceptionType(Component.translatable("commands.irons_spellbooks.create_spell.failed_max_level", abstractSpell.getSpellName(), abstractSpell.getMaxLevel())).create();
        }

        var serverPlayer = source.getPlayer();
        if (serverPlayer != null) {
            ItemStack itemstack = new ItemStack(itemInput.getItem());
            if (itemstack.getItem() instanceof SwordItem) {
                SpellData.setSpellData(itemstack, abstractSpell, spellLevel);
                if (serverPlayer.getInventory().add(itemstack)) {
                    return 1;
                }
            }
        }

        throw ERROR_FAILED.create();
    }
}
