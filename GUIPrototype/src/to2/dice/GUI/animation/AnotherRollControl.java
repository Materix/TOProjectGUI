package to2.dice.GUI.animation;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

public class AnotherRollControl extends AbstractControl {

	private int number;
	private int diceName;
	private boolean startRoll;
	private Quaternion targetRotate;
	private Vector3f[] controlPoints = { new Vector3f(0, 1, 0), // 1
			new Vector3f(1, 0, 0), // 2
			new Vector3f(0, 0, 1), // 3
			new Vector3f(0, 0, -1), // 4
			new Vector3f(-1, 0, 0), // 5
			new Vector3f(0, -1, 0) // 6
	};

	private int step;

	public AnotherRollControl(int diceName) {
		super();
		super.setEnabled(false);
		this.diceName = diceName;
	}

	@Override
	protected void controlRender(RenderManager arg0, ViewPort arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void controlUpdate(float arg0) {
		if (startRoll) {
			RigidBodyControl diceControl = this.spatial.getControl(RigidBodyControl.class);
			diceControl.setEnabled(true);
			switch (number) {
			case 1:
				targetRotate = new Quaternion().fromAngleAxis(FastMath.HALF_PI, new Vector3f(1, 0, 0));
				break;
			case 2:
				targetRotate = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, new Vector3f(0, 1, 0));
				break;
			case 3:
				targetRotate = new Quaternion().fromAngleAxis(FastMath.ZERO_TOLERANCE, new Vector3f(1, 0, 0));
				break;
			case 4:
				targetRotate = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(1, 0, 0));
				break;
			case 5:
				targetRotate = new Quaternion().fromAngleAxis(FastMath.HALF_PI, new Vector3f(0, 1, 0));
				break;
			case 6:
				targetRotate = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, new Vector3f(1, 0, 0));
				break;
			}
			diceControl.setPhysicsLocation(Util.randomLocation());
			diceControl.setPhysicsRotation(targetRotate);
			Node n = ((Node)spatial);
			((Geometry) n.getChild("Cube1")).getMaterial().setColor("Diffuse", ColorRGBA.White);
			startRoll = false;
			this.setEnabled(false);
		}
	}

	public void setNumberToRoll(int number) {
		synchronized (this.spatial) {
			this.number = number;
			setEnabled(true);
			startRoll = true;
			step = 170;
		}
	}

}
