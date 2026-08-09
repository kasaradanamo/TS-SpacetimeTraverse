package net.kasara.ts_spacetime_traverse.network.packet.s2c;

import net.kasara.ts_spacetime_traverse.client.WaypointClientManager;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * プレイヤーの全ウェイポイント情報をクライアントに送信する
 */
public record WaypointInfoS2CPacket(List<WaypointData> waypoints, @Nullable UUID quick) {

    public static void encode(WaypointInfoS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeCollection(packet.waypoints(), (b, data) -> WaypointData.encode(data, (FriendlyByteBuf) b));

        // quickはnull許容のためOptional相当のフラグ付きで書き込む
        buf.writeBoolean(packet.quick() != null);
        if (packet.quick() != null) {
            buf.writeUUID(packet.quick());
        }
    }

    public static WaypointInfoS2CPacket decode(FriendlyByteBuf buf) {
        List<WaypointData> waypoints = buf.readCollection(ArrayList::new, b -> WaypointData.decode((FriendlyByteBuf) b));
        UUID quick = buf.readBoolean() ? buf.readUUID() : null;
        return new WaypointInfoS2CPacket(waypoints, quick);
    }

    public static void send(ServerPlayer player, Collection<WaypointData> waypoints, @Nullable UUID quick) {
        ModPacketsCommon.SEND_TO_PLAYER.accept(player, new WaypointInfoS2CPacket(new ArrayList<>(waypoints), quick));
    }

    // クライアント側のみで処理するため受信コンテキストは不要
    public static void handle(WaypointInfoS2CPacket packet) {
        WaypointClientManager.waypointInfo(packet.waypoints(), packet.quick());
    }
}
