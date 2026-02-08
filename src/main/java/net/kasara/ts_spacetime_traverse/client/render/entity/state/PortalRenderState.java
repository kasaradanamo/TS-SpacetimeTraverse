package net.kasara.ts_spacetime_traverse.client.render.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class PortalRenderState extends EntityRenderState {
    public Vec3d entityPos;

    public float scale;
    public float spin;

    public String ownerName;
    public String waypointName;
    public String posText;
}
