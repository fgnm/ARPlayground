package games.rednblack.ar.playground;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;

import net.mgsx.gltf.scene3d.shaders.PBRShader;

public class PBRCustomShader extends PBRShader {

    private int u_sphericalHarmonics;

    public PBRCustomShader(Renderable renderable, Config config, String prefix) {
        super(renderable, config, prefix);
    }

    @Override
    public void init() {
        super.init();
        u_sphericalHarmonics = program.fetchUniformLocation("u_sphericalHarmonics", false);
    }

    @Override
    protected void bindLights(Renderable renderable, Attributes attributes) {
        super.bindLights(renderable, attributes);

        SphericalHarmonicsAttribute coefficients = attributes.get(SphericalHarmonicsAttribute.class, SphericalHarmonicsAttribute.Coefficients);
        if (coefficients != null) {
            float[] data = coefficients.sphericalHarmonics.data;
            program.setUniform3fv(u_sphericalHarmonics, data, 0, data.length / 3);
        }
    }
}
