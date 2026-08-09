package net.kasara.ts_spacetime_traverse.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * PortalEntityの描画クラス
 */
public class PortalRenderer extends EntityRenderer<PortalEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TokorotenSlimeAPI.getModId(), "textures/entity/portal.png");

    private static final float WIDTH = 3.0f;              // ポータルの幅
    private static final float HEIGHT = 3.0f;             // ポータルの高さ
    private static final float SPIN_SPEED = 0.05f;        // 自転速度
    private static final double NAME_TAG_DISTANCE = 15.0; // ラベル表示距離の最大

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PortalEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        long spawnTick = entity.getSpawnTick();
        float now = entity.level().getGameTime() + partialTicks;

        // スポーン前はスケール0で非表示
        float scale = spawnTick <= 0 ? 0.0f : getScale(entity, now, spawnTick);
        float spin = spawnTick <= 0 ? 0.0f : now * SPIN_SPEED;

        poseStack.pushPose();

        // 常にカメラ正面を向いて回転(Y軸のみ追従)
        Quaternionf rot = new Quaternionf(this.entityRenderDispatcher.cameraOrientation());
        rot.x = 0;
        rot.z = 0;
        rot.normalize();
        poseStack.mulPose(rot);

        // スケール適応
        poseStack.translate(0, HEIGHT / 2, 0);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, -HEIGHT / 2, 0);

        // 中心を軸に自転
        poseStack.translate(0, HEIGHT / 2, 0);
        poseStack.mulPose(new Quaternionf().rotateZ(spin));
        poseStack.translate(0, -HEIGHT / 2, 0);

        // 描画
        drawPortal(poseStack, buffer, 1.0f);

        poseStack.popPose();

        // ラベル描画(ウェイポイント名、座標、オーナー)
        drawText(entity, poseStack, buffer, entity.getWaypointName(), 0.6f, packedLight);
        drawText(entity, poseStack, buffer, entity.getTargetPosText(), 0.3f, packedLight);
        drawText(entity, poseStack, buffer, "Owner: " + entity.getOwnerName(), 0f, packedLight);
    }

    /**
     * ポータル本体の四角形を描画する
     */
    private void drawPortal(PoseStack poseStack, MultiBufferSource buffer, float alpha) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        Matrix3f normal = pose.normal();
        int light = 0xF000F0;   // 最大光源
        int overlay = OverlayTexture.NO_OVERLAY;
        int a = (int) (alpha * 255);

        // 頂点を指定して四角形を描画
        vc.vertex(mat, -WIDTH / 2, 0, 0).color(255, 255, 255, a).uv(0, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
        vc.vertex(mat, WIDTH / 2, 0, 0).color(255, 255, 255, a).uv(1, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
        vc.vertex(mat, WIDTH / 2, HEIGHT, 0).color(255, 255, 255, a).uv(1, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
        vc.vertex(mat, -WIDTH / 2, HEIGHT, 0).color(255, 255, 255, a).uv(0, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 0, 1).endVertex();
    }

    /**
     * ポータル上にテキストラベルを描画
     */
    private void drawText(PortalEntity entity, PoseStack poseStack, MultiBufferSource buffer, String text, float yOffset, int packedLight) {
        if (text == null || text.isEmpty()) return;

        // 表示距離外なら描画しない
        Vec3 labelPos = entity.position().add(0, HEIGHT + yOffset + 0.3f, 0);
        double distSq = this.entityRenderDispatcher.camera.getPosition().distanceToSqr(labelPos);
        if (distSq > NAME_TAG_DISTANCE * NAME_TAG_DISTANCE) return;

        // 底上げ分を+0.3に反転してポータル上端から浮かせる
        poseStack.pushPose();
        poseStack.translate(0, HEIGHT + yOffset + 0.3f, 0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        float bgOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int bgColor = (int) (bgOpacity * 255.0F) << 24;

        Font font = getFont();
        Component component = Component.literal(text);
        float x = -font.width(component) / 2.0f;

        font.drawInBatch(component, x, 0, 0x20FFFFFF, false, matrix, buffer, Font.DisplayMode.SEE_THROUGH, bgColor, packedLight);
        font.drawInBatch(component, x, 0, -1, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
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

    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return TEXTURE;
    }
}
