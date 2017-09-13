package ProjetoZeta.IA;

import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.util.Random;
import java.util.ArrayList;
import robocode.*;
import java.io.Writer;

public class Zeta extends AdvancedRobot {
	
	ArrayList< ScannedRobotEvent > listaRobosAchados = new ArrayList< ScannedRobotEvent >();
	ScannedRobotEvent roboMenorDistancia;
	ScannedRobotEvent roboAtual;	
	boolean direcao = true;
	
	public void run() {
		
		while (true) { // enquanto nao achar alguem

			movimento(90);
			if (getRadarTurnRemaining() == 0.0) {
				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
			
			execute();
			
		}
	}
	// quando achar um robo adversario
	public void onScannedRobot(ScannedRobotEvent e) {
		
		if(roboAtual == null){
			roboAtual = e;
			listaRobosAchados.add(e);
		}else{

			if(!listaRobosAchados.contains(e)){
				listaRobosAchados.add(e);
			}

			if(roboAtual.getName().equals(e.getName()) && 
				((compararRobos(roboAtual, e) != 0)))
			{
				roboAtual = e;
			}else{
				for(ScannedRobotEvent robo  : listaRobosAchados){									
				  if (compararRobos(roboAtual, robo) == 1){
					  roboAtual = robo;					  
				  }
				}
			}

			
		}	
		//System.out.println(roboAtual.getDistance());
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());		
		execute();		
		atirar(roboAtual);		
		
	}
	
	public int compararRobos(ScannedRobotEvent r1, ScannedRobotEvent r2){
		// System.out.println(r1.getName()+" - "+((r1.getDistance()/ 2) + r1.getEnergy()));
		// System.out.println(r2.getName()+" - "+((r2.getDistance()/ 2) + r2.getEnergy()));
		if (((r1.getDistance()) + r1.getEnergy()) > ((r2.getDistance()) + r2.getEnergy())){
			return 1;
		}else if(((r1.getDistance()/ 2) + r1.getEnergy()) < ((r2.getDistance()/ 2) + r2.getEnergy()))
		{
			return 2;
		}else{
			if(r1.getEnergy() > r2.getEnergy()){
				return 1;
			}else if(r1.getEnergy() < r2.getEnergy()){
				return 2;
			}else{
				return 0;
			}			

		}
	}

	public void atirar(ScannedRobotEvent e) {		

		
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());		
		
		if (Math.abs(bearingFromGun) <= 3) {
			turnGunRight(bearingFromGun);
			
			if (getGunHeat() == 0) {
				fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
			}
		} 		
		else {
			turnGunRight(bearingFromGun);
		}
		
		if (bearingFromGun == 0) {
			scan();
		}	
			
	}

	public void onHitByBullet(HitByBulletEvent e){
		direcao = !direcao;
		movimento(180);
	}

	public void OnHitWall()
	{
		direcao = !direcao;
		movimento(90);	
	}

	public void movimento(int valor){

		if(direcao){
			setAhead(Math.random()*1000);			 
			
		}else{
			setBack(Math.random()*1000);			
		}		

		int randomNum = 1 + (int)(Math.random() * (2 - 1));
		if(randomNum == 1)
		{
			setTurnLeft(Math.random()*valor);
		}else{
			setTurnRight(Math.random()*valor);
		}
		execute();		
	}

	public void onHitRobot(HitRobotEvent e) {
		direcao = !direcao;
		movimento(90);
	}

	public void onWin(WinEvent e) {
		// Victory dance
		turnRight(36000);
	}

}
