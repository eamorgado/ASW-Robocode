package em;

import robocode.*;
import robocode.AdvancedRobot;
import java.awt.geom.Point2D;
import java.awt.Color;

public class Primus extends AdvancedRobot implements BotInterface{

	private Enemy enemy = new Enemy();
	private byte move_dir= 1;
	
	//Used to indicate event handling, preventing loop of event
	private int too_close = 0;
	
	//arbitrary size of bounding box
	private int wall_margin = 60;

	public void assemblyLine() {
		setColors(Color.black, Color.red, Color.black);
		//radar and gun turn independent
		setAdjustRadarForGunTurn(true);
		//gun and robot turn independent
		setAdjustGunForRobotTurn(true);
		addWallCollision();
	}	
	
	public void addWallCollision() {
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
		setTurnRight(normalizeBearing(enemy.getBearing() + 90));

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
	
	public void shootGun() {
		if (enemy.exists()) return; //No enemy, gun not required
		
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
		
		//Find the angle of rotation for the gun to hit future point
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
	
	public double normalizeBearing(double angle) {
		//Use while statements for when angle is either too big or too small
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}	
	
	public double calculateAngleRotation(double a_x, double a_y, double b_x, double b_y) {
		double bc,h; //h is the hypotenuse BA
		bc = b_x - a_x;
		h = Point2D.distance(a_x,a_y,b_x,b_y);
		return Math.toDegrees(Math.asin(bc/h));
	}	
	
	public void ordersCycle() {
		turnRadar();
		makeMove();
		shootGun();
		execute();
	}	
	
	public void onScannedRobot(ScannedRobotEvent e) {enemy = new Enemy(e, this);}
	public void onRobotDeath(RobotDeathEvent e) {
		out.println("Omae wa mo shinderu");
		setTurnRight(90);
		setTurnLeft(90);
		setMaxVelocity(0);
	}
	
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
	public void onHitRobot(HitRobotEvent e) {
		out.println("Hit Robot");
		too_close = 0;
	}
	
	public void run() {
		assemblyLine();
		while (true) ordersCycle();
	}
}