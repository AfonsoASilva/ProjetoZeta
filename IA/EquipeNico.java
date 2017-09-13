package ProjetoZeta.IA;
import robocode.*;
import java.awt.Color;

import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import java.util.Random;

public class EquipeNico extends AdvancedRobot{

	private int turnCounter;
	
	private int wallMargin = 30;
	private double aproximationRate = 15;

	private int movementAmount = 200;
	private int movementAmountPace = 10;	
	private boolean movementAmountLastIncrease = false;

	private double moveFat = 10;
	private double moveFatPace = 5;
	private boolean moveFatLastIncrease = false;
		
	private double maxFire = 2;
	private double maxFirePace = 1;
	private boolean maxFireDistanceLastIncrease = false;
	
	private double minFire = 1;
	private double minFirePace = 1;
	
	private double maxFireDistance = 110;
	private double maxFireDistancePace = 20;
	
	private boolean fireBulletLastIncrease = false;

	private double keepDistance = 110;
	private double keepDistancePace = 20;
	private boolean keepDistanceLastIncrease = false;
	
	private int moveBreaker = 60;
	private int moveBreakerPace = 1;
	private boolean moveBreakerLastIncrease = false;

	private boolean tooCloseToWall = false;
	
	public void run() {
		setColors(Color.blue, Color.red, Color.white, Color.blue, Color.white);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		addCustomEvent(
			new Condition("too_close_to_walls") {
				public boolean test() {
					boolean tooClose = getX() <= wallMargin ||
					getX() >= getBattleFieldWidth() - wallMargin ||
					getY() <= wallMargin ||
					getY() >= getBattleFieldHeight() - wallMargin;
					if (!tooClose && tooCloseToWall) {
						tooCloseToWall = false;
					}
					tooCloseToWall = tooClose;
					return tooClose;	
				}
			}
		);
		
		turnCounter = 0;

		while (true) {
			if (getRadarTurnRemaining() == 0) {
				setTurnRadarRight(Double.POSITIVE_INFINITY);
			}
			moveRandom();
			turnCounter++;
			execute();
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		double absolutePosition = getHeading() + e.getBearing();
		double distance = e.getDistance();
		pointRadar(absolutePosition);

		if (distance > keepDistance) {
			moveCloser(distance, e.getBearing());
		}else {
			moveAway(distance, e.getBearing());
		}	
		
		double strength = smartFire(distance);
		
		pointGun(
			e.getBearingRadians(), 
			e.getHeadingRadians(), 
			e.getVelocity(), 
			Rules.getBulletSpeed(strength)
	    );
		
		fBullet(strength);
	}

	private void pointRadar(double absolutePosition) {
		double radarTurn = Utils.normalRelativeAngleDegrees(absolutePosition - getRadarHeading());
		setTurnRadarRight(radarTurn);
	}
		
	private void fBullet(double strength) {
       decrementFireBullet();
	   decrementMaxFireDistance();
	   setFire(strength);
	   decrementKeepDistance();
	}
	
	private void turnSideways (double bearing) {
		double turnSide = 90 + bearing;
		setTurnRight(Utils.normalRelativeAngleDegrees(turnSide));
	}
	
	private void pointGun(double bearing, double heading, double velocity, double bulletSpeed) {
	    double absoluteBearing = getHeadingRadians() + bearing;
		double future = velocity * Math.sin(heading - absoluteBearing) / bulletSpeed;
		double gunTurn;
		if (getOthers() == 1){
			if (turnCounter % 2 == 0) {
				gunTurn = absoluteBearing - getGunHeadingRadians() + future;
			}else {
				gunTurn = absoluteBearing - getGunHeadingRadians();
			}
		}else{
			gunTurn = absoluteBearing - getGunHeadingRadians() + future;
		}
	    			
	    setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn)) ;
	}

	public void onBulletHitBullet(BulletHitBulletEvent event) {
	   incrementFireBullet();
	   incrementMaxFireDistance();
	   incrementMoveBreaker();
	}

	
	private double smartFire(double distance) {
		double strength;
	//	double energy = getEnergy();
		if (distance < maxFireDistance + keepDistance) {
			strength = maxFire;
	    }else {
			strength = minFire;
		}
					
		return strength;
	}
	
	private void moveRandom() {
		Random random = new Random();
		double move = moveFat + random.nextInt(movementAmount);
		if (turnCounter % moveBreaker == 0) {
			setAhead(move);	
		}
		if (turnCounter % moveBreaker == 32) {
			setAhead(-move);
		}
	}
	
	private void moveCloser(double distance, double bearing) {
		Random random = new Random();
		double move = moveFat + random.nextInt(movementAmount);
		if (turnCounter % moveBreaker == 0) {
			turnSideways(bearing - aproximationRate);
			setAhead(move);
		}
		if (turnCounter % moveBreaker == 32) {
			turnSideways(bearing + aproximationRate);
			setAhead(-move);
		}
	}
	
	private void moveAway (double distance, double bearing) {
		Random random = new Random();
		double move = moveFat + random.nextInt(movementAmount);
		if (turnCounter % moveBreaker == 0) {
			turnSideways(bearing + aproximationRate);
			setAhead(move);
		}
		if (turnCounter % moveBreaker == 32) {
			turnSideways(bearing - aproximationRate);
			setAhead(-move);
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		double absolutePosition = getHeading() + e.getBearing();
		pointRadar(absolutePosition);
		turnSideways(e.getBearing());
		incrementMovementAmount();
		incrementMoveFat();
		incrementKeepDistance();
		incrementMoveBreaker();
	}

	public void OnHitRobot(HitRobotEvent e) {
		double absolutePosition = getHeading() + e.getBearing();
		pointRadar(absolutePosition);
		turnSideways(e.getBearing());
		decrementMovementAmount();
		decrementMoveFat();
		incrementKeepDistance();
		decrementMoveBreaker();
	}
	
	public void onHitWall(HitWallEvent e) {
		decrementKeepDistance();
		setAhead(-movementAmount * (getVelocity() > 0 ? 1 : -1));
		decrementMoveBreaker();
	}
	public void onCustomEvent(CustomEvent e) {
		if (e.getCondition().getName().equals("too_close_to_walls")) {
			if (tooCloseToWall) {
				return;
			}
			tooCloseToWall = true;
			setAhead(-movementAmount * (getVelocity() > 0 ? 1 : -1));
		}
	}
	
	public void incrementKeepDistance() {
		double newKeepDistance = keepDistance + keepDistancePace;
		keepDistance = Math.min(newKeepDistance, 650);
		
		if (!keepDistanceLastIncrease) {
			double newKeepDistancePace = keepDistancePace / 2;
			keepDistancePace = Math.max(newKeepDistancePace, 20);
		}else {
			double newKeepDistancePace = keepDistancePace * 2;
			keepDistancePace = Math.min(newKeepDistancePace, 100);
		}
		keepDistanceLastIncrease = true;
	}
	
	public void decrementKeepDistance() {
		double newKeepDistance = keepDistance - keepDistancePace;
		keepDistance = Math.max(newKeepDistance, 60);
		
		if (keepDistanceLastIncrease) {
			double newKeepDistancePace = keepDistancePace / 2;
			keepDistancePace = Math.max(newKeepDistancePace, 20);
		}else {
			double newKeepDistancePace = keepDistancePace * 2;
			keepDistancePace = Math.min(newKeepDistancePace, 100);
		}
		keepDistanceLastIncrease = false;
	}
	
	public void incrementMaxFireDistance() {
		double newMaxFireDistance = maxFireDistance + maxFireDistancePace;
		maxFireDistance = Math.min(newMaxFireDistance, 600);
		if (maxFireDistanceLastIncrease) {
			double newMaxFireDistancePace = maxFireDistancePace / 2;
			maxFireDistancePace = Math.max(newMaxFireDistancePace, 15);
		}else {
			double newMaxFireDistancePace = maxFireDistancePace * 2;
			maxFireDistancePace = Math.min(newMaxFireDistancePace, 100);
		}
		maxFireDistanceLastIncrease = true;
	}
	
	public void decrementMaxFireDistance() {
		double newMaxFireDistance = maxFireDistance - 20;
		maxFireDistance = Math.max(newMaxFireDistance, 300);
		
		if (maxFireDistanceLastIncrease) {
			double newMaxFireDistancePace = maxFireDistancePace / 2;
			maxFireDistancePace = Math.max(newMaxFireDistancePace, 15);
		}else {
			double newMaxFireDistancePace = maxFireDistancePace * 2;
			maxFireDistancePace = Math.max(newMaxFireDistancePace, 100);
		}
		maxFireDistanceLastIncrease = false;
	}
	
	public void incrementMoveBreaker() {
		int newMoveBreaker = moveBreaker + moveBreakerPace;
		moveBreaker = Math.min(newMoveBreaker, 66);
		
		if (!moveBreakerLastIncrease) {
			int newMoveBreakerPace = moveBreakerPace / 2;
			moveBreakerPace = Math.max(newMoveBreakerPace, 1);
		}else {
			int newMoveBreakerPace = moveBreakerPace * 2;
			moveBreakerPace = Math.min(newMoveBreakerPace, 2);
		}
		moveBreakerLastIncrease = true;
	}
	
	public void decrementMoveBreaker() {
		int newMoveBreaker = moveBreaker - moveBreakerPace;
		moveBreaker = Math.max(newMoveBreaker, 56);
		
		if (moveBreakerLastIncrease) {
			int newMoveBreakerPace = moveBreakerPace / 2;
			moveBreakerPace = Math.max(newMoveBreakerPace, 1);
		}else {
			int newMoveBreakerPace = moveBreakerPace * 2;
			moveBreakerPace = Math.min(newMoveBreakerPace, 2);
		}
		moveBreakerLastIncrease = false;
	}

	public void incrementFireBullet() {
        double newMaxFire = maxFire + maxFirePace;
		maxFire = Math.max(newMaxFire, 2.7);
	    double newMinFire = minFire + minFirePace;
	    minFire = Math.min(newMinFire, 1.5);
		
		if (!fireBulletLastIncrease) {
			double newMaxFirePace = maxFirePace / 2;
			maxFirePace = Math.max(newMaxFirePace, 0.5);
			double newMinFirePace = minFirePace / 2;
			minFirePace = Math.max(newMinFirePace, 0.5);
		}else {
			double newMaxFirePace = maxFirePace * 2;
			maxFirePace = Math.min(newMaxFirePace, 2);
			double newMinFirePace = minFirePace * 2;
			minFirePace = Math.min(newMinFirePace, 2);
		}
		fireBulletLastIncrease = true;
	}
	
	public void decrementFireBullet() {
		double newMaxFire = maxFire - maxFirePace;
	    maxFire = Math.max(newMaxFire, 1.8);
	    double newMinFire = minFire - minFirePace;
	    minFire = Math.max(newMinFire, 0.4);
		
		if (fireBulletLastIncrease) {
			double newMaxFirePace = maxFirePace / 2;
			maxFirePace = Math.max(newMaxFirePace, 0.5);
			double newMinFirePace = minFirePace / 2;
			minFirePace = Math.max(newMinFirePace, 0.5);
		}else {
			double newMaxFirePace = maxFirePace * 2;
			maxFirePace = Math.min(newMaxFirePace, 2);
			double newMinFirePace = minFirePace * 2;
			minFirePace = Math.min(newMinFirePace, 2);
		}
		fireBulletLastIncrease = false;
	}
	
	public void incrementMoveFat() {
		double newMoveFat = moveFat + moveFatPace;
		moveFat = Math.min(newMoveFat, 200);
		
		if (!moveFatLastIncrease) {
			double newMoveFatPace = moveFatPace / 2;
			moveFatPace = Math.max(newMoveFatPace, 30);
		}else {
			double newMoveFatPace = moveFatPace * 2;
			moveFatPace = Math.min(newMoveFatPace, 100);
		}
		moveFatLastIncrease = true;
	}
	
	public void decrementMoveFat() {
		double newMoveFat = moveFat - moveFatPace;
		moveFat = Math.max(newMoveFat, 10);
		
		if (moveFatLastIncrease) {
			double newMoveFatPace = moveFatPace / 2;
			moveFatPace = Math.max(newMoveFatPace, 30);
		}else {
			double newMoveFatPace = moveFatPace * 2;
			moveFatPace = Math.min(newMoveFatPace, 100);
		}
		moveFatLastIncrease = false;
	}
	
	public void incrementMovementAmount() {
		int newMovementAmount = movementAmount + movementAmountPace;
		movementAmount = Math.min(newMovementAmount, 600);
		
		if (!movementAmountLastIncrease) {
			int newMovementAmountPace = movementAmountPace / 2;
			movementAmountPace = Math.max(newMovementAmountPace, 30);
		}else {
			int newMovementAmountPace = movementAmountPace * 2;
			movementAmountPace = Math.min(newMovementAmountPace, 140);
		}
		movementAmountLastIncrease = true;
	}
	
	public void decrementMovementAmount() {
		int newMovementAmount = movementAmount - movementAmountPace;
		movementAmount = Math.max(newMovementAmount, 100);
		
		if (movementAmountLastIncrease) {
			int newMovementAmountPace = movementAmountPace / 2;
			movementAmountPace = Math.max(newMovementAmountPace, 30);
		}else {
			int newMovementAmountPace = movementAmountPace * 2;
			movementAmountPace = Math.min(newMovementAmountPace, 140);
		}
		movementAmountLastIncrease = false;
	}


}
