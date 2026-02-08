package net.kasara.ts_spacetime_traverse.item;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * プレイヤーが使用すると特定の進捗を付与し、アイテムを消費するカスタムアイテム。
 */
public class SpacetimeEyeItem extends Item {
    public SpacetimeEyeItem(Settings settings) {
        super(settings);
    }

    /**
     * プレイヤーがアイテムを右クリックしたときに呼ばれるメソッド。
     *
     * @param world ワールド
     * @param user 使ったプレイヤー
     * @param hand 使った手
     * @return ActionResult.SUCCESS 常に成功として返す
     */
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // 使用した手をMinecraftに通知(アニメーションなどで必要)
        user.setCurrentHand(hand);

        // サーバー側かつプレイヤーがサーバープレイヤーエンティティの場合
        if (world instanceof ServerWorld && user instanceof ServerPlayerEntity serverPlayer) {

            // 右クリック時に手に持ってるアイテムスタックを取得
            ItemStack stack = user.getStackInHand(hand);

            // プレイヤーの進捗トラッカー取得
            PlayerAdvancementTracker tracker = serverPlayer.getAdvancementTracker();

            // JSONで作った子進捗を取得
            AdvancementEntry adv = Objects.requireNonNull(serverPlayer.getEntityWorld().getServer())
                    .getAdvancementLoader()
                    .get(Identifier.of(TokorotenSlimeAPI.getModId(), "use_spacetime_eye"));

            // 進捗が存在し、まだ達成していない場合
            if (adv != null && !tracker.getProgress(adv).isDone()) {
                // 進捗を達成させる
                tracker.grantCriterion(adv, "use_spacetime_eye");

                // アイテム消費（クリエイティブは減らさない）
                stack.decrementUnlessCreative(1, user);

                // 使用統計を更新
                user.incrementStat(Stats.USED.getOrCreateStat(this));
            }
        }
        return ActionResult.SUCCESS;
    }
}
