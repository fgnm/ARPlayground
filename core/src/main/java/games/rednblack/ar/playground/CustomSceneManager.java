package games.rednblack.ar.playground;

import com.badlogic.gdx.graphics.g3d.environment.SphericalHarmonics;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

import net.mgsx.gltf.scene3d.scene.SceneManager;

public class CustomSceneManager extends SceneManager {

    public static final float[] sphericalHarmonicFactors = {
            0.282095f,
            -0.325735f,
            0.325735f,
            -0.325735f,
            0.273137f,
            -0.273137f,
            0.078848f,
            -0.273137f,
            0.136569f,
    };

    private final float[] sphericalHarmonicsCoefficients = new float[9 * 3];

    public CustomSceneManager() {
    }

    public CustomSceneManager(int maxBones) {
        super(maxBones);
    }

    public CustomSceneManager(ShaderProvider shaderProvider, DepthShaderProvider depthShaderProvider) {
        super(shaderProvider, depthShaderProvider);
    }

    public CustomSceneManager(ShaderProvider shaderProvider, DepthShaderProvider depthShaderProvider, RenderableSorter renderableSorter) {
        super(shaderProvider, depthShaderProvider, renderableSorter);
    }

    public void setAmbientLight(SphericalHarmonics sphericalHarmonics) {
        SphericalHarmonicsAttribute attribute = environment.get(SphericalHarmonicsAttribute.class, SphericalHarmonicsAttribute.Coefficients);
        if (attribute != null) {
            float[] coefficients = sphericalHarmonics.data;
            for (int i = 0; i < 9 * 3; ++i) {
                sphericalHarmonicsCoefficients[i] = coefficients[i] * sphericalHarmonicFactors[i / 3];
            }
            attribute.sphericalHarmonics.set(sphericalHarmonicsCoefficients);
        }
    }
}
