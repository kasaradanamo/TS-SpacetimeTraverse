package net.kasara.ts_spacetime_traverse.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

public class PortalRenderState extends EntityRenderState {
    public Vec3 entityPos;

    public float scale;
    public float spin;

    public String ownerName;
    public String waypointName;
    public String posText;
}
