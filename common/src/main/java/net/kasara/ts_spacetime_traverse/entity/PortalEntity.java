package net.kasara.ts_spacetime_traverse.entity;

import net.kasara.ts_spacetime_traverse.block.ModBlocksCommon;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.kasara.ts_spacetime_traverse.server.PortalManager;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * ワープ用ポータルエンティティ
 */
public class PortalEntity extends Entity {

    // ポータルの生存時間（tick）
    public static final int LIFETIME_TICKS = 20 * 60;  // (20t/s)

    // 出現・消滅アニメーション時間（tick）
    public static final int ANIMATION_TICKS = (int) (20 * 1.2f);

    // 所有者情報
    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);

    // ウェイポイント情報
    private static final EntityDataAccessor<String> WAYPOINT_UUID = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> WAYPOINT_NAME = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);

    // 転送先情報
    private static final EntityDataAccessor<String> TARGET_DIMENSION_NAME = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TARGET_X = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_Y = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_Z = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_YAW = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.INT);

    // アニメーション制御
    private static final EntityDataAccessor<Long> SPAWN_TICK = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> VANISH_START_TICK = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> VANISH_START_SCALE = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.FLOAT);

    // リンクポータル(返ってこれるポータル)
    private static final EntityDataAccessor<String> LINKED_PORTAL_UUID = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_PLACE_PORTAL = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.BOOLEAN);

    // チャンクロード維持用
    private long chunkTicketExpiryTicks = 0L;

    public PortalEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;  // 物理衝突を無効化
    }

    /**
     * DataTracker初期化
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(OWNER_UUID, "");
        entityData.define(WAYPOINT_UUID, "");
        entityData.define(TARGET_DIMENSION_NAME, "minecraft:overworld");
        entityData.define(TARGET_X, 0);
        entityData.define(TARGET_Y, 0);
        entityData.define(TARGET_Z, 0);
        entityData.define(TARGET_YAW, 0);

        entityData.define(OWNER_NAME, "");
        entityData.define(WAYPOINT_NAME, "");

        entityData.define(SPAWN_TICK, 0L);
        entityData.define(VANISH_START_TICK, -1L);
        entityData.define(VANISH_START_SCALE, 1.0f);

        entityData.define(LINKED_PORTAL_UUID, "");
        entityData.define(IS_PLACE_PORTAL, true);
    }

    /**
     * 永続化(NBT)
     */
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        entityData.set(OWNER_UUID, input.getString("OwnerUUID").orElse(""));
        entityData.set(WAYPOINT_UUID, input.getString("WaypointUUID").orElse(""));
        entityData.set(TARGET_DIMENSION_NAME, input.getString("TargetDimension").orElse("minecraft:overworld"));
        entityData.set(TARGET_X, input.getInt("TargetX").orElse(0));
        entityData.set(TARGET_Y, input.getInt("TargetY").orElse(0));
        entityData.set(TARGET_Z, input.getInt("TargetZ").orElse(0));
        entityData.set(TARGET_YAW, input.getInt("TargetYaw").orElse(0));

        entityData.set(OWNER_NAME, input.getString("OwnerName").orElse(""));
        entityData.set(WAYPOINT_NAME, input.getString("WaypointName").orElse(""));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putString("OwnerUUID", entityData.get(OWNER_UUID));
        output.putString("WaypointUUID", entityData.get(WAYPOINT_UUID));
        output.putString("TargetDimension", entityData.get(TARGET_DIMENSION_NAME));
        output.putInt("TargetX", entityData.get(TARGET_X));
        output.putInt("TargetY", entityData.get(TARGET_Y));
        output.putInt("TargetZ", entityData.get(TARGET_Z));
        output.putInt("TargetYaw", entityData.get(TARGET_YAW));

        output.putString("OwnerName", entityData.get(OWNER_NAME));
        output.putString("WaypointName", entityData.get(WAYPOINT_NAME));
    }

    @Override
    public void tick() {
        super.tick();

        if (!(level() instanceof ServerLevel serverLevel)) return;

        long worldTime = serverLevel.getGameTime();

        // 初回 tick で spawnTick を確定
        if (entityData.get(SPAWN_TICK) == 0L) {
            entityData.set(SPAWN_TICK, worldTime);
        }

        // ポータル内に侵入したエンティティを検出して転送
        String dimension = entityData.get(TARGET_DIMENSION_NAME);
        if (!dimension.isEmpty()) {
            ServerLevel targetLevel = level().getServer()
                    .getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimension)));

            if (targetLevel != null) {
                AABB portalBox = this.getBoundingBox();

                for (Entity entity : level().getEntities(this, portalBox.inflate(3))) {
                    if (!(entity instanceof PortalEntity) && shouldTeleport(entity, portalBox)) {
                        teleport(entity, targetLevel);
                    }
                }
            }
        }

        // チャンクのアンロード防止
        if (--chunkTicketExpiryTicks <= 0L) {
            serverLevel.getChunkSource().addTicketWithRadius(
                    TicketType.ENDER_PEARL,
                    chunkPosition(),
                    3
            );
            chunkTicketExpiryTicks = TicketType.ENDER_PEARL.timeout();
        }

        // 生存時間終了前に消滅アニメーション開始
        if (!isVanishing() && worldTime - getSpawnTick() >= LIFETIME_TICKS - ANIMATION_TICKS) {
            startVanish(worldTime);
        }

        // 消滅完了判定
        if (isVanishing()) {
            long elapsed = worldTime - getVanishStartTick();
            if (elapsed >= ANIMATION_TICKS * getVanishStartScale()) {
                discard();
            }
        }
    }

    /**
     * 消滅処開始
     *
     * @param worldTime 消滅を始めたワールド時間
     */
    public void startVanish(long worldTime) {
        if (isVanishing()) return;

        float currentScale;
        long spawnTick = getSpawnTick();
        currentScale = spawnTick <= 0 ? 0f : Math.min((worldTime - spawnTick) / (float) ANIMATION_TICKS, 1f);

        entityData.set(VANISH_START_SCALE, currentScale);
        entityData.set(VANISH_START_TICK, worldTime);

        // リンク先ポータルも同時に消滅
        PortalEntity linkedPortal = getLinkedPortal();
        if (linkedPortal != null && !linkedPortal.isVanishing()) {
            linkedPortal.startVanish(worldTime);
        }

        // サーバー管理リストから除外
        PortalManager.removeActivePlacePortals(getOwnerUuid());
    }

    public boolean isVanishing() {
        return entityData.get(VANISH_START_TICK) >= 0;
    }

    /**
     * ポータルに入るかの判定
     */
    private boolean shouldTeleport(Entity entity, AABB portalBox) {
        // 接触判定
        if (entity.getBoundingBox().intersects(portalBox)) {
            return true;
        }

        // 高速通過判定
        Vec3 prevPos = new Vec3(entity.xOld, entity.yOld, entity.zOld);
        Vec3 currentPos = entity.position();

        return portalBox.clip(prevPos, currentPos).isPresent();
    }

    /**
     * ワープ処理
     *
     * @param entity 入ったEntity
     * @param targetLevel 行き先のサーバーワールド
     */
    private void teleport(Entity entity, ServerLevel targetLevel) {
        // ポータル自身は転送しない
        if (entity instanceof PortalEntity) return;

        BlockPos targetBlockPos = getTargetBlockPos();

        double x = targetBlockPos.getX() + 0.5;
        double y = targetBlockPos.getY();
        double z = targetBlockPos.getZ() + 0.5;
        float yaw = getTargetYaw();
        float pitch = entity.getXRot();
        Vec3 motion = entity.getDeltaMovement().yRot((float) Math.toRadians(yaw - entity.getYRot()));

        // 足場用の VoidBlock を配置
        if (entity instanceof LivingEntity le && !le.isFallFlying()) {
            tryPlaceVoidBlock(targetLevel, targetBlockPos);
            motion = new Vec3(0, 0, 0);
        }

        // プレイヤーは同期
        if(entity instanceof ServerPlayer player) {
            player.connection.teleport(x, y, z, yaw, pitch);
            player.hurtMarked = true;
        }

        TeleportTransition transition = new TeleportTransition(
                targetLevel,
                new Vec3(x, y, z),
                motion,
                yaw,
                pitch,
                false,
                false,
                Set.of(),
                TeleportTransition.PLACE_PORTAL_TICKET
        );

        entity.teleport(transition);
        entity.fallDistance = 0.0f;

        // サーバー側フック処理
        PortalHandler.handlePortalEntry(entity, this);
    }

    /**
     * 転送先直下にVoidBlockを配置する
     * 水や空気の場合のみ設置
     */
    private void tryPlaceVoidBlock(ServerLevel level, BlockPos tpTargetPos) {
        BlockPos placePos = tpTargetPos.below(1);

        for (int i = 1; i < 4; i++) {
            BlockPos checkPos = tpTargetPos.below(i);
            var state = level.getBlockState(checkPos);

            if (!state.isAir() && state.getFluidState().isEmpty()) return;

            if (!state.getFluidState().isEmpty()) {
                level.setBlock(placePos, ModBlocksCommon.VOID_BLOCK.defaultBlockState(), 3);
                return;
            }
        }
        level.setBlock(placePos, ModBlocksCommon.VOID_BLOCK.defaultBlockState(), 3);
    }

    public void setOwner(ServerPlayer player) {
        entityData.set(OWNER_UUID, player.getStringUUID());
        entityData.set(OWNER_NAME, player.getName().getString());
    }

    public void setOwner(UUID ownerUuid, String ownerName) {
        entityData.set(OWNER_UUID, ownerUuid.toString());
        entityData.set(OWNER_NAME, ownerName);
    }

    public void setWaypoint(WaypointData waypoint) {
        entityData.set(WAYPOINT_UUID, waypoint.uuid().toString());
        entityData.set(WAYPOINT_NAME, waypoint.name());
        entityData.set(TARGET_DIMENSION_NAME, waypoint.dimension().identifier().toString());
        entityData.set(TARGET_X, waypoint.blockPos().getX());
        entityData.set(TARGET_Y, waypoint.blockPos().getY());
        entityData.set(TARGET_Z, waypoint.blockPos().getZ());
        entityData.set(TARGET_YAW, waypoint.yaw());
    }

    public void setLinkPortal(PortalEntity other, boolean isPlace) {
        entityData.set(LINKED_PORTAL_UUID, other.getStringUUID());
        entityData.set(IS_PLACE_PORTAL, isPlace);
    }

    public UUID getOwnerUuid() {
        return UUID.fromString(entityData.get(OWNER_UUID));
    }

    public UUID getWaypointUuid() {
        return UUID.fromString(entityData.get(WAYPOINT_UUID));
    }

    public ResourceKey<Level> getTargetDimension() {
        return ResourceKey.create(Registries.DIMENSION, Identifier.parse(entityData.get(TARGET_DIMENSION_NAME)));
    }

    public BlockPos getTargetBlockPos() {
        return new BlockPos(entityData.get(TARGET_X), entityData.get(TARGET_Y), entityData.get(TARGET_Z));
    }

    public int getTargetYaw() {
        return entityData.get(TARGET_YAW);
    }

    public String getOwnerName() {
        return entityData.get(OWNER_NAME);
    }

    public String getWaypointName() {
        return entityData.get(WAYPOINT_NAME);
    }

    public String getTargetPosText() {
        return String.format("XYZ: %s / %s / %s", entityData.get(TARGET_X), entityData.get(TARGET_Y), entityData.get(TARGET_Z));
    }

    public Long getSpawnTick() {
        return entityData.get(SPAWN_TICK);
    }

    public Long getVanishStartTick() {
        return entityData.get(VANISH_START_TICK);
    }

    public float getVanishStartScale() {
        return entityData.get(VANISH_START_SCALE);
    }

    public @Nullable PortalEntity getLinkedPortal() {
        String uuidStr = entityData.get(LINKED_PORTAL_UUID);
        if (uuidStr.isEmpty()) return null;
        return (PortalEntity) level().getServer().getLevel(getTargetDimension()).getEntity(UUID.fromString(uuidStr));
    }

    public float getAnimationDuration() {
        return ANIMATION_TICKS;
    }

    public boolean getIsPlacePortal() {
        return entityData.get(IS_PLACE_PORTAL);
    }

    /**
     * ダメージを受けない
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    /**
     * 指定したエンティティ座標におけるポータルの当たり判定
     * 設置可否の判定でも同じ形を使うため、ここに集約する
     */
    public static AABB createHitbox(Vec3 pos) {
        double width = 0.5;
        double height = 0.5;

        return new AABB(
                pos.x - width / 2, pos.y + 1 + height / 2, pos.z - width / 2,
                pos.x + width / 2, pos.y + 1.5 + height / 2, pos.z + width / 2
        );
    }

    /**
     * 当たり判定
     */
    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        return createHitbox(pos);
    }
}