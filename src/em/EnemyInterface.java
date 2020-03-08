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
	public double getBearing();
	public double getDistance();
	public double getHeading();
	public double getVelocity();
	
	public double getFutureX(long when);
	public double getFutureY(long when);
}

