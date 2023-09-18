package games.rednblack.ar.playground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseAnimationController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.Pools;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.CascadeShadowMap;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;
import net.mgsx.gltf.scene3d.utils.ShaderParser;

import games.rednblack.gdxar.GdxAnchor;
import games.rednblack.gdxar.GdxArApplicationListener;
import games.rednblack.gdxar.GdxFrame;
import games.rednblack.gdxar.GdxLightEstimationMode;
import games.rednblack.gdxar.GdxPlaneType;
import games.rednblack.gdxar.GdxPose;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ARPlayground extends GdxArApplicationListener {
	private CustomSceneManager sceneManager;
	private CustomDirectionalShadowLight directionalLight;
	private Scene modelScene, groundFloor;
	private SceneAsset modelAsset;
	private CascadeShadowMap cascadeShadowMap;

	private final Quaternion targetDir = new Quaternion();
	private final Vector3 targetPos = new Vector3();
	private final Vector3 targetScale = new Vector3(1, 1, 1);

	private final ModelBuilder builder = new ModelBuilder();
	private final BaseAnimationController.Transform transform = new BaseAnimationController.Transform();
	private final LongMap<ModelInstance> modelInstances = new LongMap<>();

	@Override
	public void create() {
		//Setup some configs
		getArAPI().setPowerSaveMode(false);
		getArAPI().setAutofocus(true);
		getArAPI().enableSurfaceGeometry(true);

		//Setup glTF rendering environment
		PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
		config.numBones = 40;
		config.manualSRGB = PBRShaderConfig.SRGB.NONE;
		config.fragmentShader = ShaderParser.parse(Gdx.files.internal("pbr/pbr.fs.glsl"));;
		config.vertexShader = ShaderParser.parse(Gdx.files.internal("pbr/pbr.vs.glsl"));
		DepthShader.Config depthConfig = PBRShaderProvider.createDefaultDepthConfig();
		depthConfig.numBones = 40;

		sceneManager = new CustomSceneManager(new PBRShadowCatcherShaderProvider(config), new PBRDepthShaderProvider(depthConfig));
		directionalLight = new CustomDirectionalShadowLight(4096, 4096);
		directionalLight.direction.set(1, -3, 1).nor();
		directionalLight.color.set(Color.WHITE);
		sceneManager.environment.add(directionalLight);

		cascadeShadowMap = new CascadeShadowMap(2);
		//sceneManager.setCascadeShadowMap(cascadeShadowMap);

		IBLBuilder iblBuilder = IBLBuilder.createOutdoor(directionalLight);
		Cubemap diffuseCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(),
				"diffuse/diffuse_", "_0.jpg", EnvironmentUtil.FACE_NAMES_NEG_POS);
		//Cubemap diffuseCubemap = iblBuilder.buildIrradianceMap(256);
		Cubemap specularCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(),
				"specular/specular_", "_", ".jpg", 10, EnvironmentUtil.FACE_NAMES_NEG_POS);
		//Cubemap specularCubemap = iblBuilder.buildRadianceMap(10);
		iblBuilder.dispose();

		Texture brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

		sceneManager.setAmbientLight(1);
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1f / 255f));
		if (getArAPI().getLightEstimationMode() == GdxLightEstimationMode.ENVIRONMENTAL_HDR)
			sceneManager.environment.set(new SphericalHarmonicsAttribute(SphericalHarmonicsAttribute.Coefficients));

		//Create a plane model for the virtual ground floor, need to show shadows
		builder.begin();
		Material groundMaterial = new Material(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
		MeshPartBuilder meshPartBuilder = builder.part("ground", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, groundMaterial);
		float size = 3;
		meshPartBuilder.rect(-size / 2f, 0, -size / 2f, -size / 2f, 0, size / 2f, size / 2f, 0, size / 2f, size / 2f, 0, -size / 2f, 0, 0, 0);
		ModelInstance ground = new ModelInstance(builder.end());
		ground.userData = PBRShadowCatcherShaderProvider.ShaderType.SHADOW_CATCHER;
		groundFloor = new Scene(ground);

		//Load AR Model
		modelAsset = new GLBLoader().load(Gdx.files.internal("BrainStem.glb"));

		//Setup glTF -> AR Bind
		getArAPI().getARCamera().far = 10f;
		sceneManager.setCamera(getArAPI().getARCamera());
		directionalLight.setViewport(6, 6, sceneManager.camera.near, sceneManager.camera.far);

		//cascadeShadowMap.setCascades(sceneManager.camera, directionalLight, 10f, 2f);
		/*directionalLight.setViewport(4, 4, sceneManager.camera.near, sceneManager.camera.far);
		DirectionalShadowLight shadowLight = sceneManager.getFirstDirectionalShadowLight();
		shadowLight.setCenter(sceneManager.camera.position);
		cascadeShadowMap.setCascades(sceneManager.camera, shadowLight, 1000f, 4f);*/

		//Start AR!
		getArAPI().setRenderAR(true);
	}

	@Override
	public void renderARModels(GdxFrame frame) {
		//Update environment light based on AR frame
		if (frame.lightEstimationMode == GdxLightEstimationMode.ENVIRONMENTAL_HDR) {
			sceneManager.setAmbientLight(frame.sphericalHarmonics);

			directionalLight.direction.set(frame.lightDirection.x, frame.lightDirection.y, -frame.lightDirection.z);
			directionalLight.lightIntensity.set(frame.lightIntensity);
		} else if (frame.lightEstimationMode == GdxLightEstimationMode.AMBIENT_INTENSITY) {
			sceneManager.setAmbientLight(frame.ambientIntensity);

			directionalLight.intensity = 1f;
			directionalLight.baseColor.set(frame.lightColor);
		} else {
			sceneManager.setAmbientLight(1);
			directionalLight.baseColor.set(Color.WHITE);
		}

		//Force update models based on the tracking anchor calculated by the framework
		for (GdxAnchor anchor : frame.getAnchors()) {
			ModelInstance model = modelInstances.get(anchor.id);
			if (model != null) {
				model.transform.set(anchor.gdxPose.getPosition(), anchor.gdxPose.getRotation());
			}
		}

		transform.lerp(targetPos, targetDir, targetScale, 0.1f);

		if (modelScene != null) {
			modelScene.modelInstance.transform.set(transform.translation, transform.rotation);
			groundFloor.modelInstance.transform.set(transform.translation, transform.rotation);
			modelScene.modelInstance.transform.scale(0.5f, 0.5f, 0.5f);
		}

		sceneManager.update(Gdx.graphics.getDeltaTime());
		sceneManager.render();
	}

	@Override
	public void render() {
		if (Gdx.input.isTouched()) {
			handleTouch(Gdx.input.getX(), Gdx.input.getY());
		}
	}

	public void handleTouch(float x, float y) {
		if (modelScene == null) {
			GdxAnchor newAnchor = getArAPI().requestHitPlaneAnchor(x, y, GdxPlaneType.ANY);
			if (newAnchor != null) {
				modelScene = new Scene(modelAsset.scene);
				modelScene.animations.playAll();
				sceneManager.addScene(modelScene);

				GdxPose p = newAnchor.gdxPose;
				modelScene.modelInstance.transform.translate(p.getPosition());
				modelScene.modelInstance.transform.set(p.getRotation());
				targetDir.set(p.getRotation());
				targetPos.set(p.getPosition());
				transform.set(targetPos, targetDir, targetScale);

				modelInstances.put(newAnchor.id, modelScene.modelInstance);

				Pools.free(newAnchor);

				sceneManager.addScene(groundFloor);
			}
		} else {
			GdxPose p = getArAPI().requestHitPlanePose(x, y, GdxPlaneType.ANY);
			if (p != null) {
				targetDir.set(p.getRotation());
				targetPos.set(p.getPosition());

				Pools.free(p);
			}
		}
	}
}