package em;

import robocode.*;
import robocode.AdvancedRobot;
import java.awt.geom.Point2D;
import java.awt.Color;


/**
 * This robot is implemented by following Mark Whitley's class on the subject (IT 218 - Robocode Project):
 * 		@see http://mark.random-article.com/robocode/
 * 
 * For Scanning:
 * 		@see http://mark.random-article.com/robocode/basic_scanning.html
 * 		@see http://mark.random-article.com/robocode/improved_scanning.html
 * 
 * For Targeting:
 * 		@see http://mark.random-article.com/robocode/basic_targeting.html
 * 		@see http://mark.random-article.com/robocode/improved_targeting.html
 * 
 * For Movement:
 * 		@see http://mark.random-article.com/robocode/basic_movement.html
 * 		@see http://mark.random-article.com/robocode/improved_movement.html -- Wal avoidance
 * 
 * For custom enemy classes: consult the files Enemy.java and AdvancedEnemy.java
 */

/** 
 * @version 1.2
 * @since 7/3/2020
 * @author Eduardo Morgado (up201706894)
 * @author Ângelo Gomes (up)
 */
public class Primus extends AdvancedRobot {

	private AdvancedEnemyBot enemy = new AdvancedEnemyBot();
	private byte move_dir= 1;
	//Used to indicate event handling, preventing loop of event
	private int too_close = 0;
	//arbitrary size of bounding box
	private int wall_margin = 60;

	void assemblyLine() {
		setColors(Color.black, Color.red, Color.black);
		//radar and gun turn independent
		setAdjustRadarForGunTurn(true);
		//gun and robot turn independent
		setAdjustGunForRobotTurn(true);
		addWallCollision();
	}
	
	void addWallCollision() {
		//add a custom event that checks if we are too close to walls
		addCustomEvent(new Condition("too_close_to_walls") { //set event name
			public boolean test() {
				boolean top,right,bot,left;
				//check if we are close to top margin
				top = getX() <= wall_margin;
				//check if we are close to right margin
				right = getX() >= getBattleFieldWidth() - wall_margin;
				//check if we are close to bottom margin
				bot = getY() <= wall_margin;
				//check if we are close to l margin
				left = getY() >= getBattleFieldHeight() - wall_margin;
				return (top || right || bot || left);
			}
		});
	}
	
	public void turnRadar() {setTurnRadarRight(360);}
	
	public void makeMove() {
		// always square off our enemy, turning slightly toward him
		setTurnRight(normalizeBearing(enemy.getBearing()+ 90 - (15 * move_dir)));

		// if we're close to the wall, eventually, we'll move away
		if (too_close > 0) too_close--; //decrease danger

		// switch directions if we've stopped
		// (also handles moving away from the wall if too close)
		if (getVelocity() == 0) {
			setMaxVelocity(8); //max velocity is 8 px/turn
			move_dir *= -1; //invert direction
			setAhead(Double.MAX_VALUE * move_dir);
		}
	}

	void shootGun() {
		if (enemy.none()) return; //No enemy, gun not required
		
		/**
		 * Fire power calculation formula, as the enemy distance increases, 
		 * 	the fire power decreases
		 * 
		 * @see http://mark.random-article.com/weber/java/robocode/lesson4.html
		 */
		double fire_power = Math.min(500 / enemy.getDistance(), 3);
		
		/**
		 * Bullet speed
		 * @see http://robowiki.net/wiki/Robocode/Game_Physics
		 */
		double bullet_speed = 20 - 3*fire_power;
		
		//calculate time for bullet to travel distance => v = distance/time
		long time = (long)(enemy.getDistance() / bullet_speed);
		
		//get enemy future x and y coordinates based on bullet travel time
		double future_x = enemy.getFutureX(time);
		double future_y = enemy.getFutureY(time);
		
		//Find placement of gun to hit enemy
		double turn_deg = calculateAngleRotation(getX(), getY(), future_x, future_y);
		setTurnGunRight(normalizeBearing(turn_deg - getGunHeading()));
		
		/**
		 * Avoid premature shooting => firing before turning gun toward the target
		 * + Fires only when gun is cooled
		 * Note: getGunTurnRemaining => how far the gun is from target
		 * @see http://mark.random-article.com/weber/java/robocode/lesson4.html
		 */
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) 
			setFire(fire_power);
		
	}
	
	/**
	 * This method computes the normalized bearing of a given angle
	 * A normalized bearing is an angle between -180 and +180
	 * @param angle - a possible non-normalized angle
	 * @return double - normalized bearing
	 * @see http://mark.random-article.com/weber/java/robocode/lesson4.html
	 */
	double normalizeBearing(double angle) {
		//Use while statements for when angle is either too big or too small
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}

	
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
	double calculateAngleRotation(double a_x, double a_y, double b_x, double b_y) {
		double bc,h; //h is the hypotenuse BA
		bc = b_x - a_x;
		h = Point2D.distance(a_x,a_y,b_x,b_y);
		return Math.toDegrees(Math.asin(bc/h));
	}
	
	private void ordersCycle() {
		turnRadar();
		makeMove();
		shootGun();
		//setTurnGunRight();
		execute();
	}
	
	/**
	 * This method handles a scanned event => radar found other bot
	 * @see http://mark.random-article.com/weber/java/robocode/lesson4.html
	 */
	public void onScannedRobot(ScannedRobotEvent e) {enemy.update(e, this);}

	public void onRobotDeath(RobotDeathEvent e) {
		out.println("Omae wa mo shinderu");
		setTurnRight(90);
		setTurnLeft(90);
		setMaxVelocity(0);
	}   

	/**
	 * This method is a custom event handler
	 * 
	 * @param e - A custom event reference, we will then need to check for the event type aka, its name
	 * @return void
	 */
	public void onCustomEvent(CustomEvent e) {
		//Choose the action based on the event's name
		switch(e.getCondition().getName()) {
			case "too_close_to_walls":
				if (too_close <= 0) {//We need to change action move is critical
					out.println("To close to wall, stop");
					too_close += wall_margin;
					setMaxVelocity(0);
				}
			break;
		}
	}

	public void onHitWall(HitWallEvent e){out.println("Nani");}

	/**
	 * Event handler for collision with enemy bot => move away
	 * @parm e - the collision event
	 */
	public void onHitRobot(HitRobotEvent e) {
		out.println("Hit Robot");
		//Avoid object
		too_close = 0;
	}
	
	public void run() {
		assemblyLine();
		while (true) ordersCycle();
	}
}