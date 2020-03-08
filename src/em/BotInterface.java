package em;

import robocode.CustomEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;


/**
 * This robot is implemented by following Mark Whitley's class on the subject (IT 218 - Robocode Project):
 * 		@see http://mark.random-article.com/robocode/
 * 
 * For Targeting:
 * 		@see http://mark.random-article.com/robocode/basic_targeting.html
 * 		@see http://mark.random-article.com/robocode/improved_targeting.html
 * 
 * For Movement:
 * 		@see http://mark.random-article.com/robocode/basic_movement.html
 * 		@see http://mark.random-article.com/robocode/improved_movement.html -- Wal avoidance
 * 
 * @version 1.2
 * @since 7/3/2020
 * @author Eduardo Morgado (up201706894)
 * @author Ângelo Gomes (up201703990)
 */
public interface BotInterface {
	public void assemblyLine();		//set colors and independent movement
	public void addWallCollision(); //add a custom event that checks if we are too close to walls
	public void turnRadar();		//turn radar 360
	public void makeMove();			//make move based on enemy position and handle wall event
	public void shootGun();			//Calculate enemy future postion to fire at
	
	/**
	 * This method computes the normalized bearing of a given angle
	 * A normalized bearing is an angle between -180 and +180
	 * @param angle - a possible non-normalized angle
	 * @return double - normalized bearing
	 * @see http://mark.random-article.com/weber/java/robocode/lesson4.html
	 */
	public double normalizeBearing(double angle);
	
	/**
	 * This method calculates the angle of rotation from the line in point A to B
	 * 
	 * Given two points A(x,y) and B(x,y) we wish to find the angle of rotation
	 * 		from the position A to B.
	 * Considering that the two points are never parallel (at which point the angle of
	 * 		rotation is 0) we can consider a third point C, the three points together
	 * 		will always form a square triangle ABC with square angle on point C, no
	 * 		matter how close, as long as A and B are never parallel.
	 * 
	 * - Consider:
	 * 	B*****C   The angle of rotation between B and A is the acute angle BAC, to
	 * 	 *    *		calculate its value we must first calculate BC as the opposing
	 *    *   *		side and the hypotenuse BA
	 *      * A   At which point, we can calculate said angle (consider it as O) by
	 * 	developing this formula: sin(O) = opposite/hypotenuse, sin(O) = BC/BA
	 * 	which, to obtain O can be developt as: O = sin-1(BC/BA) or,
	 * 	O = arcsin(BC/BA)
	 * 
	 * @param A(x,y) - coordinates of point A (the origin of rotation)
	 * @param B(x,y) - coordinates of point B (the destiny of rotation)
	 * @return the angle of rotation BAC
	 * @see https://www.mathsisfun.com/algebra/trig-finding-angle-right-triangle.html
	 */
	public double calculateAngleRotation(double a_x, double a_y, double b_x, double b_y);
	
	
	public void ordersCycle();		//action to take in robot turn
	
	/**
	 * This method handles a scanned event => radar found other bot
	 * @see http://mark.random-article.com/weber/java/robocode/lesson4.html
	 */
	public void onScannedRobot(ScannedRobotEvent e);
	public void onRobotDeath(RobotDeathEvent e);
	
	/**
	 * This method is a custom event handler
	 * 
	 * @param e - A custom event reference, we will then need to check for the event type aka, its name
	 * @return void
	 */
	public void onCustomEvent(CustomEvent e);	//Custom event handler
	public void onHitWall(HitWallEvent e);
	
	/**
	 * Event handler for collision with enemy bot => move away
	 * @parm e - the collision event
	 */
	public void onHitRobot(HitRobotEvent e);
	public void run();
}

