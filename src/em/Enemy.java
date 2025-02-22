package em;

import robocode.ScannedRobotEvent;
import robocode.Robot;

public class Enemy implements EnemyInterface {
	double bearing, distance, heading, velocity, x, y;
	String name;
	
	public Enemy() {
		this.name = null;
		this.x = this.y = 0;
		this.bearing = this.distance = this.velocity = 0.0;
	}
	
	public Enemy(ScannedRobotEvent e, Robot robot) {
		this.bearing = e.getBearing();
		this.distance = e.getDistance();
		this.heading = e.getHeading();
		this.velocity = e.getVelocity();
		this.name = e.getName();
		//To the bot's position in degrees add its bearing
		double abs_bearing_deg= (robot.getHeading() + e.getBearing());
		//normalize
		if (abs_bearing_deg <0) abs_bearing_deg += 360;
		
		/**
		 * Consider:  We wish to calculate the new x and y positions of the enemy robot
		 * 	e****robot   e.getDistance will give us the hypotenuse, we have the bearing angle
		 * 	 *    *      the new position of the robot will be the old + the distance e-robot
		 *    *   *		 to calculate it we can consider sin(O) = opposite/hy so 
		 *     * us      distance = sin(bearing) * e.getDistance
		 */
		double val =  Math.sin(Math.toRadians(abs_bearing_deg)) * e.getDistance();
		
		this.x = robot.getX() + val;
		this.y = robot.getY() + val;
	}
	
	public Boolean exists() 	{return this.name == null || this.name == "";}
	public double getX() 		{return this.x;}
	public double getY() 		{return this.y;}
	public double getBearing()	{return this.bearing;}
	public double getDistance()	{return this.distance;}
	public double getHeading()	{return this.heading;}
	public double getVelocity()	{return this.velocity;}
	public String getName()		{return this.name;}
	public double getFutureX(long when) {
		return this.x + Math.sin(Math.toRadians(this.getHeading())) * this.getVelocity() * when;
	}
	public double getFutureY(long when) {
		return this.y + Math.cos(Math.toRadians(this.getHeading())) * this.getVelocity() * when;
	}

}