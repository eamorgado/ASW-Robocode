package em;

import robocode.Robot;
import robocode.ScannedRobotEvent;

/**
 * @see http://mark.random-article.com/robocode/improved_targeting.html
 * @see http://mark.random-article.com/robocode/adv_enemy_bot.html
 */

public class AdvancedEnemyBot extends EnemyBot{
	private double x, y;
	
	public AdvancedEnemyBot(){reset();}
	
	public double getX(){return x;}
	public double getY(){return y;}
	public void reset(){
		super.reset();
		x = y = 0;
	}
	
	public void update(ScannedRobotEvent e, Robot robot){
		super.update(e);
		double abs_bearing_deg= (robot.getHeading() + e.getBearing());
		if (abs_bearing_deg <0) abs_bearing_deg += 360;
		
		double val =  Math.sin(Math.toRadians(abs_bearing_deg)) * e.getDistance();
		x = robot.getX() + val;
		y = robot.getY() + val;
		
	}
	
	public double getFutureX(long when){
		return x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
	}
	
	public double getFutureY(long when ){
		return y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
	}
}