package scene.menu.pause;

import org.lwjgl.opengl.DisplayMode;

import core.Application;
import gl.Camera;
import gl.Window;
import gl.particle.ParticleHandler;
import map.Terrain;
import scene.entity.EntityHandler;
import ui.menu.GuiDropdown;
import ui.menu.GuiPanel;
import ui.menu.GuiSlider;
import ui.menu.GuiSpinner;
import ui.menu.layout.GuiFlowLayout;
import ui.menu.listener.MenuListener;
import ui.menu.listener.SliderListener;

public class GraphicsPanel extends GuiPanel {
	private final GuiDropdown resolution;
	private final GuiSlider fov, fps, particleCount, chunkRender, entityRender;
	private final GuiSpinner fullscreen, bordered;

	private final DisplayMode[] resolutions;
	private final String[] resMenuOptions;

	public GraphicsPanel(GuiPanel parent, int x, int y) {
		super(parent, x, y);
		setLayout(new GuiFlowLayout(GuiFlowLayout.VERTICAL), x, y, 582 / 2, 392);

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

		chunkRender = new GuiSlider(x, y, "Render distance", 3, 17, Terrain.size, 2);
		chunkRender.addListener(new SliderListener() {

			@Override
			public void onClick(float value) {
			}

			@Override
			public void onRelease(float value) {
				Terrain.size = (int) value;

				/*if (Application.scene instanceof GameScene) {
					((GameScene) Application.scene).getWorld().resize(Globals.chunkRenderDist);
				}*/
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
				EntityHandler.entityRadius = (int) value / 2;

				/*if (Application.scene instanceof GameScene) {
					((GameScene) Application.scene).getWorld().resize(Globals.chunkRenderDist);
				}*/
			}

		});
		add(entityRender);

	}
}
