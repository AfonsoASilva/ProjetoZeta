package ProjetoZeta.IA;

import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.*;
import java.awt.Color;
import java.util.ArrayList;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * FranElayne - a robot by (your name here)
 */
public class Cabecao extends AdvancedRobot
{
	double distanciaInimigo;
	ArrayList<Double> quantidade = new ArrayList <Double>();
	ArrayList<Double> arrayAcertos = new ArrayList <Double>();
	ArrayList<Double> energiaInimiga = new ArrayList<Double>();
	int qAcertosAcima150 = 0;
	int qErrosAcima150 = 0;
	int qAcertosAbaixo150 = 0;
	int qErrosAbaixo150 = 0;
	int percentErrosAcima150;
	int percentAcertosAcima150;
	int percentErrosAbaixo150;
	int percentAcertosAbaixo150;
	int totalAbaixo150 = 0;
	int totalAcima150 =0;
	int movimentar =1;
	double valor;
	int Acertos = 0;
	int erros = 0;
	double velocidadeIdeal=0;
	double energia_A, energia_B, energia_C;
	/**
	 * PaintingRobot's run method - Seesaw
	 */
	public void run() {
		
		//Atribuindo cores
		setBodyColor(new Color(255, 20, 147));
		setGunColor(new Color(0, 150, 50));
		setRadarColor(new Color(148, 0, 211));
		setScanColor(Color.BLACK);
		setAdjustGunForRobotTurn(true); //desacoplando a arma do robozin
		turnRadarRightRadians(Double.POSITIVE_INFINITY); // fica girando o radar sem parar para a direita
  
		
	}

	/**
	 * Fire when we see a robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		distanciaInimigo = e.getDistance();
		//out.println("Distância do inimigo: "+distanciaInimigo);
		// Criando uma variavel double que pega a posição absoluta do inimigo detectado (focado)
		double distanciaNRxRI=e.getBearingRadians()+getHeadingRadians(); 
		// Variável que guarda o valor da velocidade do inimigo baseado na distância e ângulo formado
		double velocidadeInimigo=e.getVelocity() * Math.sin(e.getHeadingRadians() -distanciaNRxRI);
		// criando uma variavel que recebera a posicao para o disparo
		double ajustaMira;
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		//calcular o total de acerto
		
		
		
		if(Math.random()>.9){
			valor = 12*Math.random()+12;
			out.println("Valor qq: "+valor);
			setMaxVelocity(valor);
		}
		/*
		 * Abaixo o codigo para medir a distancia do inimigo para o disparo 
		 */
		if((qAcertosAcima150 + qErrosAcima150) > 10){
			totalAcima150 = qAcertosAcima150 + qErrosAcima150;
			percentAcertosAcima150 = qAcertosAcima150/totalAcima150;
			percentErrosAcima150 = qErrosAcima150/totalAcima150;
			totalAbaixo150 = qAcertosAbaixo150 + qErrosAbaixo150;
			percentAcertosAcima150 = qAcertosAbaixo150/totalAbaixo150;
			percentErrosAcima150 = qErrosAbaixo150/totalAbaixo150;
			out.println("Percentual de acerto acima de 150: "+percentAcertosAcima150);
			out.println("Percentual de erro acima de 150: "+percentErrosAcima150);
			double PAcertos = Acertos / (erros+Acertos);
			//P(Acertar|>150) = P(>150|Acertar) * P(Acertar) / P(>150)
			double probFinal1 = percentAcertosAcima150 * PAcertos;
			//P(¬Acertar|>150) = P(>150|¬Acertar) * P(¬Acertar) / P(>150)
			double probFinal2 = percentErrosAcima150 * PAcertos;	
			//¢ = P(Acertar|>150) + P(¬Acertar|>150
			double constanteNorm = 	probFinal1 + probFinal2;
			double fim1 = probFinal1/constanteNorm;
			double fim2 = probFinal2/constanteNorm;
			//se a prob de acertar for maior que a de errar para >150
			if(fim1>fim2){
				 // esse else e para quando a distancia for menor que 99
				// ajusta a mira com a formula da distancia do nosso robo menos a distancia do rovo inimigo mais a 
				// velociadade do robo inimigo, tudo isso dividido por 22
				ajustaMira = robocode.util.Utils.normalRelativeAngle(distanciaNRxRI- getGunHeadingRadians()+velocidadeInimigo/15);
				// ajusta o canhao para o desparo
				setTurnGunRightRadians(ajustaMira);
				setTurnLeft(-90-e.getBearing()); 
				// anda de atras do robo inimigo baseado na formula que 
				setAhead((e.getDistance() - 140)*movimentar);
				// atira com o nivel 3 de potencia
				setFire(3); 
			}else{
				// ajusta a mira com a formula da distancia do nosso robo menos a distancia do rovo inimigo mais a 
				// velociadade do robo inimigo, tudo isso dividido por 22
				ajustaMira = robocode.util.Utils.normalRelativeAngle(distanciaNRxRI- getGunHeadingRadians()+velocidadeInimigo/22);
				// ajusta o canhao para o desparo
				setTurnGunRightRadians(ajustaMira); 
				setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(distanciaNRxRI-getHeadingRadians()+velocidadeInimigo/getVelocity()));
				// anda de atras do robo inimigo baseado na formula que 
				setAhead((e.getDistance() - 140)*movimentar);
				// atira com o nivel 2 de potencia
				setFire(2); 
			}
		
	
		}else{
			if (e.getDistance()>150) { // se a distancia do inimigo for menor que 250 e maior que 100
				// ajusta a mira com a formula da distancia do nosso robo menos a distancia do rovo inimigo mais a 
				// velociadade do robo inimigo, tudo isso dividido por 22
				ajustaMira = robocode.util.Utils.normalRelativeAngle(distanciaNRxRI- getGunHeadingRadians()+velocidadeInimigo/22);
				// ajusta o canhao para o desparo
				setTurnGunRightRadians(ajustaMira); 
				setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(distanciaNRxRI-getHeadingRadians()+velocidadeInimigo/getVelocity()));
				// anda de atras do robo inimigo baseado na formula que 
				setAhead((e.getDistance() - 140)*movimentar);
				// atira com o nivel 2 de potencia
				setFire(2); 
			}
			else{ // esse else e para quando a distancia for menor que 99
				// ajusta a mira com a formula da distancia do nosso robo menos a distancia do rovo inimigo mais a 
				// velociadade do robo inimigo, tudo isso dividido por 22
				ajustaMira = robocode.util.Utils.normalRelativeAngle(distanciaNRxRI- getGunHeadingRadians()+velocidadeInimigo/15);
				// ajusta o canhao para o desparo
				setTurnGunRightRadians(ajustaMira);
				setTurnLeft(-90-e.getBearing()); 
				// anda de atras do robo inimigo baseado na formula que 
				setAhead((e.getDistance() - 140)*movimentar);
				// atira com o nivel 3 de potencia
				setFire(3); 
			}
		}	
	}

	public void onHitRobot(HitRobotEvent e) {
		if(velocidadeIdeal>0){
			setMaxVelocity(velocidadeIdeal);
		} else{
				if (e.getBearing() > -90 && e.getBearing() <= 90)
       				back(100);
     			else
       				ahead(100);
				}
		}
	
	

	public void onHitByBullet(HitByBulletEvent e) {
		turnLeft(90 - e.getBearing());
		ahead(20);
	}
	
	public void onHitWall(HitWallEvent e) {
			if(velocidadeIdeal>0){
			setMaxVelocity(velocidadeIdeal);
		} else{
				if (e.getBearing() > -90 && e.getBearing() <= 90)
       				back(100);
     			else
       				ahead(100);
				}
		
	}
	 public void guardaValorAcertivo() {
   		Double total=1.0;
		        for(int i=0; i < arrayAcertos.size(); i++){
		            double x = arrayAcertos.get(i);
		            for(int j = i+1; j < arrayAcertos.size() ; j++){
		                double y = arrayAcertos.get(j);
		                if(x == y){
		                    arrayAcertos.remove(j);
		                    j--;
		                    total++;
		                }
		            } 
		            quantidade.add(total);
		            total = 1.0;
		        } 
			//	double qAcertos = calculaTotalAcertos(); 
				double maior = 0;
				int z=0;
				for(int b = 0; b <arrayAcertos.size(); b ++){
					out.println("Valor: "+arrayAcertos.get(b)+": "+quantidade.get(b)+" vezes");
					if(quantidade.get(b) > maior){
						maior = quantidade.get(b);
						z = b;
					}
				}
				velocidadeIdeal =  arrayAcertos.get(z);
				out.println("VAlor que teve mais vezes: "+velocidadeIdeal);
	}
	
/*	public double calculaTotalAcertos(){
		double total=0;
		for(int i = 0; i<quantidade.size(); i++){
			total += quantidade.get(i);
		}return total;
	}*/

	
	public void onRoundEnded(RoundEndedEvent event) {
       guardaValorAcertivo();
	   out.println("tanto de quantidade: "+quantidade.size());
	   out.println("tanto de acertos: "+arrayAcertos.size());
	   out.println("BAIXA ENERGIA: "+energiaInimiga.size());
   }
 


	//quando ele acertar a bala no adversário
	public void onBulletHit(BulletHitEvent e) {
		Acertos++;
		double x = Math.round(valor);
		out.println("Valor ACERTADO: "+x);
		arrayAcertos.add(x);
		//out.println(e.getBullet().getPower());
		//	out.println(e.getEnergy());
		double energiaAtual =e.getEnergy();
		if(energiaAtual<40){
			energiaInimiga.add(energiaAtual);
		}
		double distanciaAcertada = distanciaInimigo;
		//out.println("distância acerta"+distanciaAcertada);
		if(distanciaAcertada > 150){
			qAcertosAcima150++;
		}else{
			qAcertosAbaixo150++;
		}
	}
	
	public void	onBulletMissed(BulletMissedEvent e) {
		erros++;
		double distanciaErradaAcima150 = distanciaInimigo;
		out.println("distância q errou: "+distanciaErradaAcima150);
		if(distanciaErradaAcima150 > 150){
			qErrosAcima150++;
		}else{
			qErrosAbaixo150++;
		}
	}
	/**out.println("Inicio aeee!");
	 * Paint a red circle around our PaintingRobot
	 */

}
//////
