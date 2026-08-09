package net.kasara.ts_spacetime_traverse.fabric.mixin.client;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * ClientAdvancements#progress(private)に安全にアクセスするためのアクセサ。
 */
@Mixin(ClientAdvancements.class)
public interface ClientAdvancementManagerAccessor {

    @Accessor("progress")
    Map<Advancement, AdvancementProgress> getProgress();
}
