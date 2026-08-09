package net.kasara.ts_spacetime_traverse.item;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.server.PositionSwapModeManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * プレイヤーが使用すると特定の進捗を付与し、アイテムを消費するアイテム
 */
public class SpacetimeEyeItem extends Item {
    public SpacetimeEyeItem(Properties pros) {
        super(pros);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 右クリック時に手に持ってるアイテムスタックを取得
        ItemStack stack = player.getItemInHand(hand);

        // 使用した手をMinecraftに通知(アニメーションなどで必要)
        player.startUsingItem(hand);

        // サーバー側かつプレイヤーがサーバープレイヤーエンティティの場合
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {

            // プレイヤーの進捗トラッカー取得
            PlayerAdvancements tracker = serverPlayer.getAdvancements();

            // JSONで作った子進捗を取得
            Advancement adv = serverPlayer.getServer().getAdvancements()
                    .getAdvancement(new ResourceLocation(TokorotenSlimeAPI.getModId(), "use_spacetime_eye"));

            // 進捗が存在し、まだ達成していない場合
            if (adv != null && !tracker.getOrStartProgress(adv).isDone()) {
                // 進捗を達成させる
                tracker.award(adv, "use_spacetime_eye");
                // モード変更
                PositionSwapModeManager.toggle(player);

                // アイテム消費（クリエイティブは減らさない）
                if (!serverPlayer.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                // 使用統計を更新
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
        return InteractionResultHolder.success(stack);
    }
}
