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
 * 
 * 
 */
public class Primus extends AdvancedRobot {

	private AdvancedEnemyBot enemy = new AdvancedEnemyBot();
	private byte move_dir= 1;
	//Used to indicate event handling, preventing loop of event
	private int too_close = 0;
	//arbitrary size of bounding box
	private int wall_margin = 60;

	
void turnRadar() {setTurnRadarRight(360);}
	
	public void makeMove() {
		// always square off our enemy, turning slightly toward him
		setTurnRight(normalizeBearing(enemy.getBearing() + 90 - (15 * move_dir)));

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
		if (enemy.none()) return;

		double fire_power = Math.min(400 / enemy.getDistance(), 3);
		/**
		 * Bullet speed
		 * @see http://robowiki.net/wiki/Robocode/Game_Physics
		 */
		double bullet_speed = 20 - 3*fire_power;
		
		//calculate time for bullet to travel distance
		long time = (long)(enemy.getDistance() / bullet_speed); //v = d/dt
		
		//get enemy future x and y coordinates based on bullet travel time
		double future_x = enemy.getFutureX(time);
		double future_y = enemy.getFutureY(time);
		
		double absDeg = absoluteBearing(getX(), getY(), future_x, future_y);
		setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));
		
		//if gun is cooled and we can fire in this tick(gun rotates 20º per turn) then fire
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) 
			setFire(fire_power);
		
	}
	
	/**
	 * This method computes the normalized bearing of a given angle
	 * A normalized bearing is an angle between -180 and +180
	 * 
	 * @param angle - a possible non-normalized angle
	 * @return double - normalized bearing
	 *	
	 * @see http://mark.random-article.com/weber/java/robocode/lesson4.html
	 */
	double normalizeBearing(double angle) {
		//Use while statements for when angle is either too big or too small
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}

	/** computes the absolute bearing between two points
	 * 
	 * @see http://mark.random-article.com/weber/java/robocode/lesson4.html
	 */
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		
		return (xo > 0 && yo > 0)? arcSin // both pos: lower-Left
				: (xo < 0 && yo > 0)? 360 + arcSin // x neg, y pos: lower-right
				: (xo > 0 && yo < 0)? 180 - arcSin // x pos, y neg: upper-left
				: (xo < 0 && yo < 0)? 180 - arcSin // both neg: upper-right
				: 0; 
	}
	
	
	public void run() {
		setColors(Color.black, Color.red, Color.black);
		//radar and gun turn independent
		setAdjustRadarForGunTurn(true);
		//gun and robot turn independent
		setAdjustGunForRobotTurn(true);

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

		while (true) {
			turnRadar();
			makeMove();
			shootGun();
			execute();
		}
	}
	
	/**** Robocode events Methods ****/
	public void onScannedRobot(ScannedRobotEvent e) {
		// track if we have no enemy, the one we found is significantly
		// closer, or we scanned the one we've been tracking.
		boolean have_enemy, enemy_closing_in, same_scanned;
		//check if we have no enemy to track
		have_enemy = enemy.none();
		//check if enemy is at a distance that is dangerous for us => avoid
		enemy_closing_in = e.getDistance() < enemy.getDistance() - 120;
		//check if scanned is the same
		same_scanned = e.getName().equals(enemy.getName());
		
		if (have_enemy || enemy_closing_in || same_scanned) enemy.update(e, this);
	}

	public void onRobotDeath(RobotDeathEvent e) {
		// see if the robot we were tracking died
		if (e.getName().equals(enemy.getName())) enemy.reset();
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

	public void onHitWall(HitWallEvent e){out.println("Hit Wall");}

	/**
	 * @parm e - Event that marks colision with enemy bot => move away
	 */
	public void onHitRobot(HitRobotEvent e) {
		out.println("Hit Robot");
		too_close = 0;
	}
}

