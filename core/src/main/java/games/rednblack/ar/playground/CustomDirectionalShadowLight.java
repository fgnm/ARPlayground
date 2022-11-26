package games.rednblack.ar.playground;

import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

public class CustomDirectionalShadowLight extends DirectionalShadowLight {
    public CustomDirectionalShadowLight() {
    }

    public CustomDirectionalShadowLight(int shadowMapWidth, int shadowMapHeight) {
        super(shadowMapWidth, shadowMapHeight);
    }

    public CustomDirectionalShadowLight(int shadowMapWidth, int shadowMapHeight, float shadowViewportWidth, float shadowViewportHeight, float shadowNear, float shadowFar) {
        super(shadowMapWidth, shadowMapHeight, shadowViewportWidth, shadowViewportHeight, shadowNear, shadowFar);
    }

    public final Vector3 lightIntensity = new Vector3();

    @Override
    public void updateColor() {
        this.color.r = baseColor.r * lightIntensity.x;
        this.color.g = baseColor.g * lightIntensity.y;
        this.color.b = baseColor.b * lightIntensity.z;
    }
}
