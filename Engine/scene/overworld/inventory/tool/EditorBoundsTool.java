package scene.overworld.inventory.tool;

import org.joml.Vector3f;

import dev.Console;
import scene.entity.EntityHandler;
import scene.entity.utility.MarkerEntity;

public class EditorBoundsTool {
	public static Vector3f p1 = new Vector3f();
	public static Vector3f p2 = new Vector3f();
	private static MarkerEntity m1, m2;
	
	public static void interact(Vector3f selectionPt, boolean lmb, boolean rmb) {
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
}
