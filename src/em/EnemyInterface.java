package em;

/**
 * @version 1.2
 * @since 7/3/2020
 * @author Eduardo Morgado (up201706894)
 * @author Ângelo Gomes (up201703990)
 * 
 * @see http://mark.random-article.com/robocode/improved_targeting.html
 * @see http://mark.random-article.com/robocode/enemy_bot.html
 * @see http://mark.random-article.com/robocode/adv_enemy_bot.html
 */
public interface EnemyInterface {
	public Boolean exists();
	public double getX();
	public double getY();
	public double getBearing();	//Returns the bearing to the robot you scanned, relative to your robot's heading, in degrees
	public double getDistance();//Returns the distance to the robot
	public double getHeading();	//Returns the heading of the robot, in degrees 
	public double getVelocity();
	
	/**
	 * This method calculates the future x position of enemy
	 * We calculate the distance that the bot will be from us (velocity * time), this will be the hypotenuse
	 * Given the bot's heading (the degrees) we can calculate its distance by considering
	 * 		sin(O) = opposite/hypotenuse, therefore, the distance the bot travels in the x axis will
	 * 		be opp = sin(o) * hyp == sin(heading) * velocity * time 
	 * 
	 * @param time: the interval of time between two positions
	 * @return double: the future x coordinate
	 */	
	public double getFutureX(long when);
	
	/**
	 * This method calculates the future y position of enemy
	 * We calculate the distance that the bot will be from us (velocity * time), this will be the hypotenuse
	 * Given the bot's heading (the degrees) we can calculate its distance by considering
	 * 		cos(O) = adjacent/hypotenuse, therefore, the distance the bot travels in the x axis will
	 * 		be adj = cos(o) * hyp == cos(heading) * velocity * time
	 * 
	 * @param time: the interval of time between two positions
	 * @return double: the future y coordinate
	 */
	public double getFutureY(long when);
}

