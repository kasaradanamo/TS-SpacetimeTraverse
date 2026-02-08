package net.kasara.ts_spacetime_traverse.entity;

import net.kasara.ts_spacetime_traverse.block.ModBlocks;
import net.kasara.ts_spacetime_traverse.server.ServerPortalHandler;
import net.kasara.ts_spacetime_traverse.server.ServerPortalManager;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * ワープ用ポータルエンティティ
 *
 * 主な責務：
 * ・Waypoint 情報を DataTracker で同期
 * ・エンティティ侵入検知 → ワープ処理
 * ・一定時間後の消滅アニメーション管理
 * ・リンクポータル（行き/帰り）の管理
 * ・ポータル周辺チャンクのロード維持
 */
public class PortalEntity extends Entity {

    /** ポータルの生存時間（tick） */
    public static final int LIFETIME_TICKS = 20 * 60;  // (20t/s)

    /** 出現・消滅アニメーション時間（tick） */
    public static final int ANIMATION_TICKS = (int) (20 * 1.2f);

    // 所有者情報
    private static final TrackedData<String> OWNER_UUID = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> OWNER_NAME = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.STRING);

    // ウェイポイント情報
    private static final TrackedData<String> WAYPOINT_UUID = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> WAYPOINT_NAME = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.STRING);

    // 転送先情報
    private static final TrackedData<String> TARGET_DIMENSION_NAME = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> TARGET_X = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TARGET_Y = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TARGET_Z = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TARGET_YAW = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // アニメーション制御
    private static final TrackedData<Long> SPAWN_TICK = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.LONG);
    private static final TrackedData<Long> VANISH_START_TICK = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.LONG);
    private static final TrackedData<Float> VANISH_START_SCALE = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.FLOAT);

    // リンクポータル(返ってこれるポータル)
    private static final TrackedData<String> LINKED_PORTAL_UUID = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> IS_PLACE_PORTAL = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    // チャンクロード維持用
    private long chunkTicketExpiryTicks = 0L;

    public PortalEntity(EntityType<?> type, World world) {
        super(type, world);
        noClip = true;  // 物理衝突を無効化
    }

    /**
     * DataTracker初期化
     */
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(OWNER_UUID, "");
        builder.add(WAYPOINT_UUID, "");
        builder.add(TARGET_DIMENSION_NAME, "minecraft:overworld");
        builder.add(TARGET_X, 0);
        builder.add(TARGET_Y, 0);
        builder.add(TARGET_Z, 0);
        builder.add(TARGET_YAW, 0);

        builder.add(OWNER_NAME, "");
        builder.add(WAYPOINT_NAME, "");

        builder.add(SPAWN_TICK, 0L);
        builder.add(VANISH_START_TICK, -1L);
        builder.add(VANISH_START_SCALE, 1.0f);

        builder.add(LINKED_PORTAL_UUID, "");
        builder.add(IS_PLACE_PORTAL, true);
    }

    /**
     * 永続化(NBT)
     */
    @Override
    protected void readCustomData(ReadView view) {
        dataTracker.set(OWNER_UUID, view.getString("OwnerUUID", ""));
        dataTracker.set(WAYPOINT_UUID, view.getString("WaypointUUID", ""));
        dataTracker.set(TARGET_DIMENSION_NAME, view.getString("TargetDimension", "minecraft:overworld"));
        dataTracker.set(TARGET_X, view.getInt("TargetX", 0));
        dataTracker.set(TARGET_Y, view.getInt("TargetY", 0));
        dataTracker.set(TARGET_Z, view.getInt("TargetZ", 0));
        dataTracker.set(TARGET_YAW, view.getInt("TargetYaw", 0));

        dataTracker.set(OWNER_NAME, view.getString("OwnerName", ""));
        dataTracker.set(WAYPOINT_NAME, view.getString("WaypointName", ""));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putString("OwnerUUID", dataTracker.get(OWNER_UUID));
        view.putString("WaypointUUID", dataTracker.get(WAYPOINT_UUID));
        view.putString("TargetDimension", dataTracker.get(TARGET_DIMENSION_NAME));
        view.putInt("TargetX", dataTracker.get(TARGET_X));
        view.putInt("TargetY", dataTracker.get(TARGET_Y));
        view.putInt("TargetZ", dataTracker.get(TARGET_Z));
        view.putInt("TargetYaw", dataTracker.get(TARGET_YAW));

        view.putString("OwnerName", dataTracker.get(OWNER_NAME));
        view.putString("WaypointName", dataTracker.get(WAYPOINT_NAME));
    }

    @Override
    public void tick() {
        super.tick();

        if (!(getEntityWorld() instanceof ServerWorld serverWorld)) return;

        long worldTime = serverWorld.getTime();

        // 初回 tick で spawnTick を確定
        if (dataTracker.get(SPAWN_TICK) == 0L) {
            dataTracker.set(SPAWN_TICK, worldTime);
        }

        // ポータル内に侵入したエンティティを検出して転送
        String dimension = dataTracker.get(TARGET_DIMENSION_NAME);
        if (!dimension.isEmpty()) {
            ServerWorld targetWorld = Objects.requireNonNull(getEntityWorld().getServer())
                    .getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimension)));

            if (targetWorld != null) {
                getEntityWorld().getOtherEntities(this, getBoundingBox())
                        .forEach(entity -> teleport(entity, targetWorld));
            }
        }

        // チャンクのアンロード防止
        if (--chunkTicketExpiryTicks <= 0L) {
            serverWorld.resetIdleTimeout();
            serverWorld.getChunkManager().addTicket(
                    ChunkTicketType.ENDER_PEARL,
                    new ChunkPos(getBlockPos()),
                    2
            );
            chunkTicketExpiryTicks = ChunkTicketType.ENDER_PEARL.expiryTicks();
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

        dataTracker.set(VANISH_START_SCALE, currentScale);
        dataTracker.set(VANISH_START_TICK, worldTime);

        // リンク先ポータルも同時に消滅
        PortalEntity linkedPortal = getLinkedPortal();
        if (linkedPortal != null && !linkedPortal.isVanishing()) {
            linkedPortal.startVanish(worldTime);
        }

        // サーバー管理リストから除外
        ServerPortalManager.removeActivePlacePortals(getOwnerUuid());
    }

    public boolean isVanishing() {
        return dataTracker.get(VANISH_START_TICK) >= 0;
    }

    /**
     * ワープ処理
     *
     * @param entity 入ったEntity
     * @param targetWorld 行き先のサーバーワールド
     */
    private void teleport(Entity entity, ServerWorld targetWorld) {
        // ポータル自身は転送しない
        if (entity instanceof PortalEntity) return;

        BlockPos targetBlockPos = getTargetBlockPos();

        // 足場用の VoidBlock を配置
        tryPlaceVoidBlock(targetWorld, targetBlockPos);

        int x = targetBlockPos.getX();
        int y = targetBlockPos.getY();
        int z = targetBlockPos.getZ();
        float yaw = getTargetYaw();

        Set<PositionFlag> flags = EnumSet.noneOf(PositionFlag.class);
        entity.teleport(targetWorld, x + 0.5, y, z + 0.5, flags, yaw, entity.getPitch(), true);
        entity.fallDistance = 0.0f;

        // サーバー側フック処理
        ServerPortalHandler.handlePortalEntry(entity, this);

        // プレイヤーは明示的に同期
        if(entity instanceof ServerPlayerEntity player) {
            player.networkHandler.requestTeleport(x + 0.5, y, z + 0.5, yaw, entity.getPitch());
        }
    }

    /**
     * 転送先直下にVoidBlockを配置する
     * 水や空気の場合のみ設置
     */
    private void tryPlaceVoidBlock(ServerWorld world, BlockPos tpTargetPos) {
        BlockPos placePos = tpTargetPos.down(1);

        for (int i = 1; i < 4; i++) {
            BlockPos checkPos = tpTargetPos.down(i);
            var state = world.getBlockState(checkPos);

            if (!state.isAir() && state.getFluidState().isEmpty()) return;

            if (!state.getFluidState().isEmpty()) {
                world.setBlockState(placePos, ModBlocks.VOID_BLOCK.getDefaultState());
                return;
            }
        }
        world.setBlockState(placePos, ModBlocks.VOID_BLOCK.getDefaultState());
    }

    public void setOwner(ServerPlayerEntity player) {
        dataTracker.set(OWNER_UUID, player.getUuid().toString());
        dataTracker.set(OWNER_NAME, player.getName().getString());
    }

    public void setOwner(UUID ownerUuid, String ownerName) {
        dataTracker.set(OWNER_UUID, ownerUuid.toString());
        dataTracker.set(OWNER_NAME, ownerName);
    }

    public void setWaypoint(WaypointData waypoint) {
        dataTracker.set(WAYPOINT_UUID, waypoint.uuid().toString());
        dataTracker.set(WAYPOINT_NAME, waypoint.name());
        dataTracker.set(TARGET_DIMENSION_NAME, waypoint.dimension().getValue().toString());
        dataTracker.set(TARGET_X, waypoint.blockPos().getX());
        dataTracker.set(TARGET_Y, waypoint.blockPos().getY());
        dataTracker.set(TARGET_Z, waypoint.blockPos().getZ());
        dataTracker.set(TARGET_YAW, waypoint.yaw());
    }

    public void setLinkPortal(PortalEntity other, boolean isPlace) {
        dataTracker.set(LINKED_PORTAL_UUID, other.getUuid().toString());
        dataTracker.set(IS_PLACE_PORTAL, isPlace);
    }

    public UUID getOwnerUuid() {
        return UUID.fromString(dataTracker.get(OWNER_UUID));
    }

    public UUID getWaypointUuid() {
        return UUID.fromString(dataTracker.get(WAYPOINT_UUID));
    }

    public RegistryKey<World> getTargetDimension() {
        return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dataTracker.get(TARGET_DIMENSION_NAME)));
    }

    public BlockPos getTargetBlockPos() {
        return new BlockPos(dataTracker.get(TARGET_X), dataTracker.get(TARGET_Y), dataTracker.get(TARGET_Z));
    }

    public int getTargetYaw() {
        return dataTracker.get(TARGET_YAW);
    }

    public String getOwnerName() {
        return dataTracker.get(OWNER_NAME);
    }

    public String getWaypointName() {
        return dataTracker.get(WAYPOINT_NAME);
    }

    public String getTargetPosText() {
        return String.format("XYZ: %s / %s / %s", dataTracker.get(TARGET_X), dataTracker.get(TARGET_Y), dataTracker.get(TARGET_Z));
    }

    public Long getSpawnTick() {
        return dataTracker.get(SPAWN_TICK);
    }

    public Long getVanishStartTick() {
        return dataTracker.get(VANISH_START_TICK);
    }

    public float getVanishStartScale() {
        return dataTracker.get(VANISH_START_SCALE);
    }

    public @Nullable PortalEntity getLinkedPortal() {
        String uuidStr = dataTracker.get(LINKED_PORTAL_UUID);
        if (uuidStr.isEmpty()) return null;
        return (PortalEntity) Objects.requireNonNull(Objects.requireNonNull(
                getEntityWorld().getServer()).getWorld(getTargetDimension())).getEntity(UUID.fromString(uuidStr));
    }

    public float getAnimationDuration() {
        return ANIMATION_TICKS;
    }

    public boolean getIsPlacePortal() {
        return dataTracker.get(IS_PLACE_PORTAL);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // ポータルは破壊不可
        return false;
    }

    /**
     * 当たり判定
     */
    @Override
    protected Box calculateDefaultBoundingBox(Vec3d pos) {
        double width = 0.5;
        double height = 0.5;

        return new Box(
                pos.x - width / 2, pos.y + 1 + height / 2, pos.z - width / 2,
                pos.x + width / 2, pos.y + 1.5 + height / 2, pos.z + width / 2
        );
    }
}