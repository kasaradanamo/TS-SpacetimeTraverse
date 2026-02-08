package net.kasara.ts_spacetime_traverse.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.client.render.entity.state.PortalRenderState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * PortalEntity の描画を担当するクラス
 */
@Environment(EnvType.CLIENT)
public class PortalRenderer extends EntityRenderer<PortalEntity, PortalRenderState>{

    private static final Identifier TEXTURE = Identifier.of(TokorotenSlimeAPI.getModId(), "textures/entity/portal.png");

    private static final float WIDTH = 3.0f;             // ポータルの幅
    private static final float HEIGHT = 3.0f;            // ポータルの高さ
    private static final float SPIN_SPEED = 0.05f;       // 自転速度
    private static final double NAME_TAG_DISTANCE = 15.0; // ラベル表示距離の最大

    public PortalRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public PortalRenderState createRenderState() {
        // 各エンティティの描画状態を管理するPortalRenderStateを生成
        return new PortalRenderState();
    }

    @Override
    public void render(PortalRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(renderState, matrices, queue, cameraState);

        matrices.push();

        // 常にカメラ正面を向いて回転
        Quaternionf rot = new Quaternionf(cameraState.orientation);
        rot.x = 0;
        rot.z = 0;
        rot.normalize();
        matrices.multiply(rot);

        // スケール適応
        float scale = renderState.scale;
        matrices.translate(0, HEIGHT / 2, 0);
        matrices.scale(scale, scale, scale);
        matrices.translate(0, -HEIGHT / 2, 0);

        // 中心を軸に自転
        matrices.translate(0, HEIGHT / 2, 0);
        matrices.multiply(new Quaternionf().rotateZ(renderState.spin));
        matrices.translate(0, -HEIGHT / 2, 0);

        // 描画
        drawPortal(matrices, queue, 1.0f);

        matrices.pop();

        // ラベル描画(ウェイポイント名、座標、オーナー)
        drawText(renderState, matrices, queue, cameraState, renderState.waypointName, 0.6f);
        drawText(renderState, matrices, queue, cameraState, renderState.posText, 0.3f);
        drawText(renderState, matrices, queue, cameraState, "Owner: " + renderState.ownerName, 0f);
    }

    /**
     * ポータル本体の四角形を描画する
     */
    private void drawPortal(MatrixStack matrices, OrderedRenderCommandQueue queue, float alpha) {
        queue.submitCustom(
            matrices,
            RenderLayer.getEntityTranslucent(TEXTURE),
            (entry, vc) -> {
                Matrix4f mat = entry.getPositionMatrix();
                int light = 0xF000F0;   // 最大光源
                int overlay = OverlayTexture.DEFAULT_UV;

                // 頂点を指定して四角形を描画
                vc.vertex(mat, -WIDTH / 2, 0, 0).color(255, 255, 255, (int)(alpha * 255)).texture(0, 1).overlay(overlay).light(light).normal(0, 0, 1);
                vc.vertex(mat, WIDTH / 2, 0, 0).color(255, 255, 255, (int)(alpha * 255)).texture(1, 1).overlay(overlay).light(light).normal(0, 0, 1);
                vc.vertex(mat, WIDTH / 2, HEIGHT, 0).color(255, 255, 255, (int)(alpha * 255)).texture(1, 0).overlay(overlay).light(light).normal(0, 0, 1);
                vc.vertex(mat, -WIDTH / 2, HEIGHT, 0).color(255, 255, 255, (int)(alpha * 255)).texture(0, 0).overlay(overlay).light(light).normal(0, 0, 1);
            }
        );
    }

    /**
     * ポータル上にテキストラベルを描画
     *
     * @param state 描画状態
     * @param matrices MatrixStack
     * @param queue RenderCommandQueue
     * @param cameraState カメラ状態
     * @param text 表示する文字列
     * @param yOffset 高さオフセット
     */
    private void drawText(PortalRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, String text, float yOffset) {
        if (text == null || text.isEmpty()) return;

        Vec3d labelPos = state.entityPos.add(0, HEIGHT + yOffset - 0.3f, 0);
        double distSq = cameraState.pos.squaredDistanceTo(labelPos);

        // 表示距離外なら描画しない
        if (distSq > NAME_TAG_DISTANCE * NAME_TAG_DISTANCE) return;

        queue.submitLabel(
                matrices,
                new Vec3d(0, HEIGHT + yOffset - 0.3f, 0),
                0,
                Text.literal(text),
                true,
                state.light,
                distSq,
                cameraState
        );
    }

    @Override
    public void updateRenderState(PortalEntity entity, PortalRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);

        long worldTime = entity.getEntityWorld().getTime();
        long spawnTick = entity.getSpawnTick();

        // スポーン前の初期化
        if (spawnTick <= 0) {
            state.scale = 0.0f;
            state.spin = 0.0f;
            state.entityPos = entity.getLerpedPos(tickProgress);

            state.ownerName = entity.getOwnerName();
            state.waypointName = entity.getWaypointName();
            state.posText = entity.getTargetPosText();
            return;
        }

        float now = worldTime + tickProgress;
        float scale;

        // 消失アニメーション中
        if (entity.isVanishing()) {
            float elapsed = now - entity.getVanishStartTick();

            float startScale = entity.getVanishStartScale();
            float duration = entity.getAnimationDuration() * startScale;

            float t = duration > 0 ? Math.min(elapsed / duration, 1.0f) : 1.0f;
            scale = startScale * (1.0f - t);
        }
        // 通常スケールアップアニメーション
        else {
            float elapsed = now - spawnTick;
            scale = Math.min(elapsed / entity.getAnimationDuration(), 1.0f);
        }
        state.scale = scale;
        state.spin = now * SPIN_SPEED;
        state.entityPos = entity.getLerpedPos(tickProgress);

        state.ownerName = entity.getOwnerName();
        state.waypointName = entity.getWaypointName();
        state.posText = entity.getTargetPosText();
    }
}
