package em;

import robocode.ScannedRobotEvent;

/**
 * @see http://mark.random-article.com/robocode/improved_scanning.html
 * @see http://mark.random-article.com/robocode/enemy_bot.html
 */
public class EnemyBot {
	double bearing, distance, energy, heading, velocity;
	String name;
	
	//Constructor
	public EnemyBot(){reset();}
	
	public double getBearing()	{return this.bearing;}
	public double getDistance()	{return this.distance;}
	public double getEnergy()	{return this.energy;}
	public double getHeading()	{return this.heading;}
	public double getVelocity()	{return this.velocity;}
	public String getName()		{return this.name;}
	
	//Update Enemy to new bot
	public void update(ScannedRobotEvent bot){
		this.bearing = bot.getBearing();
		this.distance = bot.getDistance();
		this.energy = bot.getEnergy();
		this.heading = bot.getHeading();
		this.velocity = bot.getVelocity();
		this.name = bot.getName();
	}
	
	//Reset all values
	public void reset(){
		this.bearing = this.distance = this.energy = this.heading = this.velocity = 0.0;
		this.name = null;
	}
	
	/**
	 * Checks if enemy if non existent
	 * @param void
	 * @return boolean - true if enemy does not exist
	 */
	public Boolean none(){return (name == null || name == "");}
}