package scene.overworld.inventory.tool;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import dev.Console;
import dev.tracers.LineRender;
import io.Input;
import scene.entity.EntityHandler;
import scene.entity.utility.MarkerEntity;

public class EditorBoundsTool {
	public static Vector3f p1 = new Vector3f();
	public static Vector3f p2 = new Vector3f();
	private static MarkerEntity m1, m2;
	
	public static void init() {
		
	}
	
	public static void interact(Vector3f selectionPt, boolean lmb, boolean rmb) {
		
		
		if (Input.isDown(Keyboard.KEY_LCONTROL)) {
			if (rmb) {
				if (p1.y > p2.y) p1.y++;
				else p2.y++;
			}
			if (lmb) {
				if (p1.y > p2.y) p1.y--;
				else p2.y--;
			}
		} else {
			if (lmb) {
				p1.set(selectionPt);
				Console.log("p1 set",p1);
				if (m1 != null) {
					m1.destroy();
				}
				m1 = new MarkerEntity(p1);
				EntityHandler.addEntity(m1);
			}
			
			if (rmb) {
				p2.set(selectionPt);
				Console.log("p2 set",p2);
				if (m2 != null) {
					m2.destroy();
				}
				m2 = new MarkerEntity(p2);
				EntityHandler.addEntity(m2);
			}
		}
		
		LineRender.clearPoints();
		LineRender.addPoints(new Vector3f(p1.x, p1.y, p1.z), new Vector3f(p2.x, p1.y, p1.z));
		LineRender.addPoints(new Vector3f(p1.x, p1.y, p1.z), new Vector3f(p1.x, p2.y, p1.z));
		LineRender.addPoints(new Vector3f(p1.x, p1.y, p1.z), new Vector3f(p1.x, p1.y, p2.z));
		LineRender.addPoints(new Vector3f(p1.x, p2.y, p2.z), new Vector3f(p2.x, p2.y, p2.z));
		LineRender.addPoints(new Vector3f(p2.x, p1.y, p2.z), new Vector3f(p2.x, p2.y, p2.z));
		LineRender.addPoints(new Vector3f(p2.x, p2.y, p1.z), new Vector3f(p2.x, p2.y, p2.z));
		
		LineRender.addPoints(new Vector3f(p2.x, p2.y, p1.z), new Vector3f(p2.x, p1.y, p1.z));
		LineRender.addPoints(new Vector3f(p2.x, p2.y, p1.z), new Vector3f(p1.x, p2.y, p1.z));
		
		LineRender.addPoints(new Vector3f(p2.x, p2.y, p1.z), new Vector3f(p1.x, p2.y, p1.z));
		
		LineRender.addPoints(new Vector3f(p1.x, p2.y, p2.z), new Vector3f(p1.x, p1.y, p2.z));
		LineRender.addPoints(new Vector3f(p2.x, p1.y, p2.z), new Vector3f(p1.x, p1.y, p2.z));
		
		LineRender.addPoints(new Vector3f(p1.x, p2.y, p2.z), new Vector3f(p1.x, p2.y, p1.z));
	}
}
