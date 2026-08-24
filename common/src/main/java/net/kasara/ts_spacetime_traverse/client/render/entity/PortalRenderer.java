package net.kasara.ts_spacetime_traverse.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.render.entity.state.PortalRenderState;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * PortalEntityの描画クラス
 */
public class PortalRenderer extends EntityRenderer<PortalEntity, PortalRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), "textures/entity/portal.png");

    private static final float WIDTH = 3.0f;              // ポータルの幅
    private static final float HEIGHT = 3.0f;             // ポータルの高さ
    private static final float SPIN_SPEED = 0.05f;        // 自転速度
    private static final double NAME_TAG_DISTANCE = 15.0; // ラベル表示距離の最大

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PortalRenderState createRenderState() {
        return new PortalRenderState();
    }

    @Override
    public void submit(PortalRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);

        poseStack.pushPose();

        // 常にカメラ正面を向いて回転
        Quaternionf rot = new Quaternionf(camera.orientation);
        rot.x = 0;
        rot.z = 0;
        rot.normalize();
        poseStack.mulPose(rot);

        // スケール適応
        float scale = state.scale;
        poseStack.translate(0, HEIGHT / 2, 0);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, -HEIGHT / 2, 0);

        // 中心を軸に自転
        poseStack.translate(0, HEIGHT / 2, 0);
        poseStack.mulPose(new Quaternionf().rotateZ(state.spin));
        poseStack.translate(0, -HEIGHT / 2, 0);

        // 描画
        drawPortal(poseStack, submitNodeCollector, 1.0f);

        poseStack.popPose();

        // ラベル描画(ウェイポイント名、座標、オーナー)
        drawText(state, poseStack, submitNodeCollector, camera, state.waypointName, 0.6f);
        drawText(state, poseStack, submitNodeCollector, camera, state.posText, 0.3f);
        drawText(state, poseStack, submitNodeCollector, camera, "Owner: " + state.ownerName, 0f);
    }

    /**
     * ポータル本体の四角形を描画する
     */
    private void drawPortal(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float alpha) {
        submitNodeCollector.submitCustomGeometry(
            poseStack,
            RenderTypes.entityTranslucent(TEXTURE),
            (pose, vc) -> {
                Matrix4f mat = pose.pose();
                int light = 0xF000F0;   // 最大光源
                int overlay = OverlayTexture.NO_OVERLAY;

                // 頂点を指定して四角形を描画
                vc.addVertex(mat, -WIDTH / 2, 0, 0).setColor(255, 255, 255, (int)(alpha * 255)).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                vc.addVertex(mat, WIDTH / 2, 0, 0).setColor(255, 255, 255, (int)(alpha * 255)).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                vc.addVertex(mat, WIDTH / 2, HEIGHT, 0).setColor(255, 255, 255, (int)(alpha * 255)).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                vc.addVertex(mat, -WIDTH / 2, HEIGHT, 0).setColor(255, 255, 255, (int)(alpha * 255)).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
            }
        );
    }

    /**
     * ポータル上にテキストラベルを描画
     */
    private void drawText(PortalRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraState, String text, float yOffset) {
        if (text == null || text.isEmpty()) return;

        Vec3 labelPos = state.entityPos.add(0, HEIGHT + yOffset - 0.3f, 0);
        double distSq = cameraState.pos.distanceToSqr(labelPos);

        // 表示距離外なら描画しない
        if (distSq > NAME_TAG_DISTANCE * NAME_TAG_DISTANCE) return;

        submitNodeCollector.submitNameTag(
                poseStack,
                new Vec3(0, HEIGHT + yOffset - 0.3f, 0),
                0,
                Component.literal(text),
                true,
                state.lightCoords,
                cameraState
        );
    }

    @Override
    public void extractRenderState(PortalEntity entity, PortalRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        long spawnTick = entity.getSpawnTick();
        float now = entity.level().getGameTime() + partialTicks;

        // スポーン前の初期化
        if (spawnTick <= 0) {
            state.scale = 0.0f;
            state.spin = 0.0f;
            state.entityPos = entity.getPosition(partialTicks);

            state.ownerName = entity.getOwnerName();
            state.waypointName = entity.getWaypointName();
            state.posText = entity.getTargetPosText();
            return;
        }

        state.scale = getScale(entity, now, spawnTick);
        state.spin = now * SPIN_SPEED;
        state.entityPos = entity.getPosition(partialTicks);

        state.ownerName = entity.getOwnerName();
        state.waypointName = entity.getWaypointName();
        state.posText = entity.getTargetPosText();
    }

    /**
     * スケール取得
     *
     * @param entity ポータルエンティティ
     * @param now 今の時間
     * @param spawnTick スポーンした時の時間
     * @return スケール
     */
    private float getScale(PortalEntity entity, float now, long spawnTick) {
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
        return scale;
    }
}
