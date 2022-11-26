package games.rednblack.ar.playground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;

import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

public class PBRShadowCatcherShaderProvider extends PBRShaderProvider {
    private final DefaultShaderProvider shadowCatcherShaderProvider =
            new DefaultShaderProvider(null, Gdx.files.internal("shadowCatcher.glsl").readString());

    public PBRShadowCatcherShaderProvider(PBRShaderConfig config) {
        super(config);
    }

    @Override
    protected PBRShader createShader(Renderable renderable, PBRShaderConfig config, String prefix) {
        if(renderable.environment.has(SphericalHarmonicsAttribute.Coefficients)){
            prefix += "#define sphericalHarmonicsFlag\n";
        }
        return new PBRCustomShader(renderable, config, prefix);
    }

    @Override
    public Shader getShader(Renderable renderable) {
        if (renderable.userData instanceof ShaderType) {
            ShaderType type = (ShaderType) renderable.userData;
            switch (type) {
                case PBR:
                    return super.getShader(renderable);
                case SHADOW_CATCHER:
                    return shadowCatcherShaderProvider.getShader(renderable);
            }
        }
        return super.getShader(renderable);
    }

    public enum ShaderType {
        SHADOW_CATCHER,
        PBR
    }
}
