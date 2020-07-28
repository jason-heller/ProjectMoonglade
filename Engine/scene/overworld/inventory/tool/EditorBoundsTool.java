package scene.overworld.inventory.tool;

import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import dev.Console;
import dev.tracers.LineRender;
import io.Input;

public class EditorBoundsTool {
	public static Vector3f p1 = new Vector3f();
	public static Vector3f p2 = new Vector3f();
	
	private static Vector3f p1r = new Vector3f(), p2r = new Vector3f();
	
	public static void interact(Vector3f selectionPt, boolean lmb, boolean rmb, int facingIndex) {
		
		
		if (Input.isDown(Keyboard.KEY_LCONTROL)) {
			if (rmb) {
				if (p1.y > p2.y) p1.y++;
				else p2.y++;
				
				p1r.y = p1.y;
				p2r.y = p2.y;
			}
			if (lmb) {
				if (p1.y > p2.y) p1.y--;
				else p2.y--;
				
				p1r.y = p1.y;
				p2r.y = p2.y;
			}
		} else {
			if (lmb) {
				p1.set(selectionPt);
				p1r.set(p1);
				Console.log("p1 set",p1);
				if (facingIndex == 1)
					p1r.x++;
			}
			
			if (rmb) {
				p2.set(selectionPt);
				p2r.set(p2);
				Console.log("p2 set",p2);
				if (facingIndex == 1)
					p2r.x++;
			}
		}
		
		LineRender.clearPoints();
		// Ew ew ew ew
		LineRender.addPoints(new Vector3f(p1r.x, p1r.y, p1r.z), new Vector3f(p2r.x, p1r.y, p1r.z));
		LineRender.addPoints(new Vector3f(p1r.x, p1r.y, p1r.z), new Vector3f(p1r.x, p2r.y, p1r.z));
		LineRender.addPoints(new Vector3f(p1r.x, p1r.y, p1r.z), new Vector3f(p1r.x, p1r.y, p2r.z));
		LineRender.addPoints(new Vector3f(p1r.x, p2r.y, p2r.z), new Vector3f(p2r.x, p2r.y, p2r.z));
		LineRender.addPoints(new Vector3f(p2r.x, p1r.y, p2r.z), new Vector3f(p2r.x, p2r.y, p2r.z));
		LineRender.addPoints(new Vector3f(p2r.x, p2r.y, p1r.z), new Vector3f(p2r.x, p2r.y, p2r.z));
		LineRender.addPoints(new Vector3f(p2r.x, p2r.y, p1r.z), new Vector3f(p2r.x, p1r.y, p1r.z));
		LineRender.addPoints(new Vector3f(p2r.x, p2r.y, p1r.z), new Vector3f(p1r.x, p2r.y, p1r.z));
		LineRender.addPoints(new Vector3f(p2r.x, p2r.y, p1r.z), new Vector3f(p1r.x, p2r.y, p1r.z));
		LineRender.addPoints(new Vector3f(p1r.x, p2r.y, p2r.z), new Vector3f(p1r.x, p1r.y, p2r.z));
		LineRender.addPoints(new Vector3f(p2r.x, p1r.y, p2r.z), new Vector3f(p1r.x, p1r.y, p2r.z));
		LineRender.addPoints(new Vector3f(p1r.x, p2r.y, p2r.z), new Vector3f(p1r.x, p2r.y, p1r.z)); // ew
	}
}
