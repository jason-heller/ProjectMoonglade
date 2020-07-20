package scene.menu.pause;

import org.lwjgl.opengl.DisplayMode;

import core.Application;
import gl.Camera;
import gl.Window;
import gl.particle.ParticleHandler;
import gl.shadow.ShadowBox;
import gl.shadow.ShadowRender;
import map.Chunk;
import map.Enviroment;
import map.Terrain;
import scene.entity.EntityHandler;
import scene.overworld.Overworld;
import ui.menu.GuiDropdown;
import ui.menu.GuiLabel;
import ui.menu.GuiPanel;
import ui.menu.GuiSlider;
import ui.menu.GuiSpinner;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.MenuListener;
import ui.menu.listener.SliderListener;

public class GraphicsPanel extends GuiPanel {
	private final GuiDropdown resolution;
	private final GuiSlider fov, fps, particleCount, chunkRender, entityRender, shadowDistance;
	private final GuiSpinner fullscreen, bordered, shadowQuality;

	private final DisplayMode[] resolutions;
	private final String[] resMenuOptions;

	public GraphicsPanel(GuiPanel parent, int x, int y, int width, int height) {
		super(parent, x, y, width, height);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582 / 2, 392);

		add(new GuiLabel(x, y, "#SGeneral"));
		
		resolutions = Window.getDisplayModes();
		int i = 0;
		resMenuOptions = new String[resolutions.length];
		for (final DisplayMode mode : resolutions) {
			resMenuOptions[i++] = mode.getWidth() + "x" + mode.getHeight();
		}

		fov = new GuiSlider(x, y, "fov", 60, 105, Camera.fov, 1);
		fov.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Camera.fov = (int) value;
				Application.scene.getCamera().updateProjection();
				ShadowRender.updateParams();
			}

		});

		fps = new GuiSlider(x, y, "Framerate", 30, 120, Window.maxFramerate, 1);
		fps.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Window.maxFramerate = (int) value;
			}

		});
		add(fps);
		add(fov);

		fullscreen = new GuiSpinner(x, y, "Windowing", Window.fullscreen ? 1 : 0, "Windowed", "Fullscreen");
		fullscreen.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				if (index == 1) {
					Window.fullscreen = true;
					Window.setDisplayMode(Window.getWidth(), Window.getHeight(), true);
				} else {
					Window.fullscreen = false;
					Window.setDisplayMode(Window.getWidth(), Window.getHeight(), false);
				}
			}
		});
		add(fullscreen);
		
		bordered = new GuiSpinner(x, y, "Border", Window.hasBorder ? 1 : 0, "Bordered", "No Border");
		bordered.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				if (index == 1) {
					Window.hasBorder = true;
					Window.setBorder(true);
				} else {
					Window.hasBorder = false;
					Window.setBorder(false);
				}
			}
		});
		add(bordered);

		resolution = new GuiDropdown(0, 0, "resolution", resMenuOptions);

		resolution.addListener(new MenuListener() {

			@Override
			public void onClick(String option, int index) {
				Window.setDisplayMode(resolutions[index]);
			}

		});

		addWithoutLayout(resolution);
		resolution.setPosition(x + 324, fullscreen.y - 4);
		addSeparator();

		particleCount = new GuiSlider(x, y, "max particles", 0, 300, ParticleHandler.maxParticles, 1);
		particleCount.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				ParticleHandler.maxParticles = (int) value;
			}

		});
		add(particleCount);

		chunkRender = new GuiSlider(x, y, "Render distance", 3, 33, Terrain.size, 2);
		chunkRender.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				int oldSize = Terrain.size;
				Terrain.size = (int) value;

				if (Application.scene instanceof Overworld) {
					Enviroment enviroment = ((Overworld) Application.scene).getEnviroment();
					//enviroment.x += (oldSize - Terrain.size) / 2;
					//enviroment.z += (oldSize - Terrain.size) / 2;
					enviroment.getTerrain().reload();
				}
			}

		});
		add(chunkRender);
		
		entityRender = new GuiSlider(x, y, "Entity Range", 3, 17, EntityHandler.entityRadius, 2);
		entityRender.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				EntityHandler.entityRadius = (int) value;
				EntityHandler.entityRadius = Math.max(EntityHandler.entityRadius, 3);
				
				/*if (Application.scene instanceof GameScene) {
					((GameScene) Application.scene).getWorld().resize(Globals.chunkRenderDist);
				}*/
			}

		});
		add(entityRender);
		
		addSeparator();
		add(new GuiLabel(x, y, "#SShadows"));
		shadowDistance = new GuiSlider(x, y, "Shadow Distance", 16, 64, ShadowBox.shadowDistance, 1);
		shadowDistance.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				int dist = (int) value;
				if (ShadowRender.shadowMapSize != 0) {
					ShadowBox.shadowDistance = dist;
					ShadowRender.shadowMapSize = 1024 * ((dist/16) + 1); 
					ShadowRender.updateParams();
				}
			}

		});
		add(shadowDistance);

		shadowQuality = new GuiSpinner(x + 32, y, "Quality", ShadowRender.shadowQuality, "No Shadows", "Very Low", "Low", "Medium", "High");
		shadowQuality.addListener(new MenuListener() {
			@Override
			public void onClick(String option, int index) {
				ShadowRender.shadowQuality = index;
				switch(index) {
				case 0:
					ShadowRender.shadowMapSize = 0;
					break;
				case 1:
					ShadowRender.pcfCount = 0;
					break;
				case 2:
					ShadowRender.pcfCount = 1;
					break;
				case 3:
					ShadowRender.pcfCount = 2;
					break;
				case 4:
					ShadowRender.pcfCount = 3;
					break;
				}
				
				if (index != 0) {
					ShadowRender.shadowMapSize = 1024 * ((((int)ShadowBox.shadowDistance)/16) + 1); 
					ShadowRender.updateParams();
				}
			}
		});
		add(shadowQuality);
	}
}
