package ProjetoZeta.IA;
import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;

public class RoboCop extends AdvancedRobot {
    
    /*----------INICIALIZAÇÃO DAS VARIÁVEIS-----------------*/
    // HashMap para armazenar as informacoes dos adversarios atingidos
    HashMap<String,Adversario> adversarios;
    // uso do static para persistir entre as rodadas
    static HashMap<String,int[][][][]> estadosArmazenados = new HashMap<String,int[][][][]>();
    Adversario alvo;
    
    // Direcao usada no guessfactor.
    int direcao = 1;
    double direcaoPerpendicular = 1;
    
    int acertoTiro;
    
    //  posicoes do robô
    Point2D.Double posAtual, posAnterior, posProxima;
    
    
    /*----------STARTANDO O ROBO-----------------*/
    public void run() {
        colorir();
        
        adversarios = new HashMap<String,Adversario>();
        alvo = null;
        // Definicao do campo de batalha para o robo. O numero 50 , para previnir o choque na parede
        Rectangle2D campo = new Rectangle2D.Double(50, 50, getBattleFieldWidth() - 100, getBattleFieldHeight() - 100);
        posProxima = null;
        acertoTiro = 0;
        
        while(true) {
            posAtual = new Point2D.Double(getX(), getY());
            
            // se n tiver alvo, gira o radar para procurar um alvo.
            if(alvo == null) {
                turnRadarRight(360);
            } else {
                // Senao, gira o radar para o alvo.
                double anguloRadar = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(calcularAngulo(posAtual, alvo.posicao)) - getRadarHeading());
                alvo = null;
                turnRadarRight(anguloRadar);
                // Se o alvo se moveu , adquire um novo alvo
                if(alvo == null) {
                    turnRadarRight(anguloRadar < 0 ? -360 - anguloRadar : 360 - anguloRadar);
                }
            }
            
            if(alvo != null) {
                // se tiver mais do que um adversario no campo de batalha
                if(getOthers() > 1) {
                    if(posProxima == null) {
                        posProxima = posAnterior = posAtual;
                    }
                    // Gera 100 possibilidades de posicoes de risco, depois seleciona a posicao de menor risco
                    for(int i = 0; i < 100; i++){
                        double novaDistancia = (100 * Math.random()) + 100;
                        Point2D.Double novaPosicao = calcularPonto(posAtual, Math.toRadians(Math.random() * 360), novaDistancia);
                        if(campo.contains(novaPosicao) && calcularRisco(novaPosicao) < calcularRisco(posProxima)){
                            posProxima = novaPosicao;
                        }
                    }
                } else {
                    // Calculo para que o robo permaneca na posicao perpendicular
                    double d = (Math.random() * 100) + 300;
                    if(!campo.contains(calcularPonto(posAtual, calcularAngulo(posAtual, alvo.posicao) + Math.PI / 3 * direcaoPerpendicular, d)) || ((Math.random() * (acertoTiro % 5) > 0.6))) {
                        direcaoPerpendicular = -direcaoPerpendicular;
                    }
                    // Procura um angulo perpendicular ao inimigo, senao procura o angulo valido mais proximo
                    double angulo = calcularAngulo(posAtual, alvo.posicao) + (Math.PI / 2) * direcaoPerpendicular;
                    while(!campo.contains(calcularPonto(posAtual, angulo, d))) {
                        angulo -= direcaoPerpendicular * 0.1;
                    }
                    posProxima = calcularPonto(posAtual, angulo, d);
                }
                // Calcula a distancia absoluta e o angulo para aa posucao, atualiza a posicao anterior
                double distancia = posAtual.distance(posProxima);
                double anguloDestino = robocode.util.Utils.normalRelativeAngleDegrees(Math.toDegrees(calcularAngulo(posAtual, posProxima)) - getHeading());
                posAnterior = posAtual;
                
                // Calcula o valor da menor volta possivel e movimenta o robo para este ponto.
                if(Math.abs(anguloDestino) > 90) {
                    anguloDestino = robocode.util.Utils.normalRelativeAngleDegrees(anguloDestino + 180);
                    distancia = -distancia;
                }
                turnRight(anguloDestino);
                ahead(distancia);
            }
        }
    }
    
	// Colore o robo, a cor da bala, do radar..
    public void colorir(){
        setBodyColor(Color.cyan);
        setGunColor(Color.black);
        setRadarColor(Color.lightGray);
        setBulletColor(Color.black);
        setScanColor(Color.red);
    }
    
	//Scaneia os robos adversarios
    public void onScannedRobot(ScannedRobotEvent e) {
        // Adiciona ou atualiza dados do adversario
        String nome = e.getName();
        Adversario inimigoScan;
        if(adversarios.get(nome) == null) {
            inimigoScan = new Adversario(nome, calcularPonto(posAtual, Math.toRadians(getHeading() + e.getBearing()), e.getDistance()), e.getEnergy(), e.getBearing(), e.getHeading(), new Vector<BulletWave>());
        } else {
            inimigoScan = new Adversario(nome, calcularPonto(posAtual, Math.toRadians(getHeading() + e.getBearing()), e.getDistance()), e.getEnergy(), e.getBearing(), e.getHeading(), adversarios.get(nome).waves);
        }
        adversarios.put(nome, inimigoScan);
        
        if((alvo == null) || (alvo.nome.equals(inimigoScan.nome)) || (e.getDistance() < alvo.posicao.distance(posAtual))) {
            alvo = inimigoScan;
        }
        
        // Armazena os estados dos adversarios.
        int[][][][] adversariosEstados = estadosArmazenados.get(nome.split(" ")[0]);
        if(adversariosEstados == null) {
            adversariosEstados = new int[2][9][13][31];
            estadosArmazenados.put(nome.split(" ")[0], adversariosEstados);
        }
        
        // Quanto mais distante o inimigo, menor o poder da bala. Quanto menor o poder da bala, maior a velocidade e a acuracia.
        double poderTiro = getOthers() > 1 ? 3 : Math.min(3, Math.max(600 / e.getDistance(), 1));
        double absoluteBearing = Math.toRadians(getHeading() + inimigoScan.bearing);
        
        if(e.getVelocity() != 0) {
            if(Math.sin(Math.toRadians(inimigoScan.heading) - absoluteBearing) * e.getVelocity() < 0) {
                direcao = -1;
            } else {
                direcao = 1;
            }
        }
        
        int[] estadosAtuais = adversariosEstados[getOthers() > 1 ? 0 : 1][(int) (e.getVelocity() == 0 ? 8 : Math.abs(Math.sin(Math.toRadians(inimigoScan.heading) - absoluteBearing) * e.getVelocity() / 3))][(int) (e.getDistance() / 100)];
        
        // Cria e adiciona novas ondas
        BulletWave newWave = new BulletWave(posAtual, inimigoScan.posicao, absoluteBearing, poderTiro, getTime(), direcao, estadosAtuais, getTime() - 1);
        inimigoScan.waves.add(newWave);
        for(int i = 0; i < inimigoScan.waves.size(); i++) {
            BulletWave currentWave = inimigoScan.waves.get(i);
            if(currentWave.waveHit(inimigoScan.posicao, getTime())) {
                inimigoScan.waves.remove(currentWave);
                i--;
            }
        }
        
        
        if((inimigoScan == alvo) && (poderTiro < getEnergy())) {
            int melhorIndice = 15;
            for(int i = 0; i < 31; i++) {
                if(estadosAtuais[melhorIndice] < estadosAtuais[i]) {
                    melhorIndice = i;
                }
            }
            
            double guessFactor = (double)(melhorIndice - (estadosAtuais.length - 1) / 2) / ((estadosAtuais.length - 1) / 2);
            double angleOffset = direcao * guessFactor * newWave.maxEscapeAngle();
            double gunAdjust = Math.toDegrees(robocode.util.Utils.normalRelativeAngle(absoluteBearing - Math.toRadians(getGunHeading()) + angleOffset));
            turnGunRight(gunAdjust);
            setFire(poderTiro);
        }
    }
    
	// Usado apenas quando a batalha esta 1v1, para indicar se o robo esta acertando o tiro
    public void onHitByBullet(HitByBulletEvent e) {
        if(getOthers() == 1) {
            acertoTiro++;
        }
    }
    
	// se o robo tiver sido eliminado, ele sai da Hashtable
    public void roboEliminado(RobotDeathEvent e) {
        adversarios.remove(e.getName());
        if((alvo != null) && (alvo.nome.equals(e.getName()))) {
            alvo = null;
        }
    }
    
//
    public void onWin(WinEvent e) {
        risadinha();
    }
    
    public void risadinha()
    {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
    
    public double calcularRisco(Point2D destino){
        double risco = 0;
        Iterator<Adversario> adversariosIt = adversarios.values().iterator();
        while(adversariosIt.hasNext()){
            Adversario inimigoIt = adversariosIt.next();
            double riscoInimigo = Math.min(inimigoIt.energia/getEnergy(), 2);
            double perp = Math.abs(Math.cos(calcularAngulo(posAtual, destino) - calcularAngulo(inimigoIt.posicao, destino)));
            risco += riscoInimigo*((1+perp)/destino.distanceSq(inimigoIt.posicao));
        }
        risco += 0.1 / destino.distanceSq(posAnterior);
        risco += 0.1 / destino.distanceSq(posAtual);
        
        return risco;
    }
    
	// Calcula a distância entre dois pontos:
    static double calcularAngulo(Point2D posOrigem, Point2D posAlvo) {
        return Math.atan2(posAlvo.getX() - posOrigem.getX(), posAlvo.getY() - posOrigem.getY());
    }
    
    // Calcula um ponto, dado um ângulo e uma distância de um ponto de origem:
    static Point2D.Double calcularPonto(Point2D posOrigem, double angulo, double distancia) {
        return new Point2D.Double(posOrigem.getX() + Math.sin(angulo) * distancia, posOrigem.getY() + Math.cos(angulo) * distancia);
    }
    
    
    public class Adversario {
        public String nome;
        public Point2D.Double posicao;
        public double energia, bearing, heading;
        public Vector<BulletWave> waves;	

        public Adversario(String nome, Point2D.Double posAdversario, double energia, double bearing, double heading, Vector<BulletWave> waves) {
            this.nome = nome;
            this.posicao = posAdversario;
            this.energia = energia;
            this.bearing = bearing;
            this.heading = heading;
            this.waves = waves;
        }
    }
    
    
    public class BulletWave {
        private Point2D.Double origin, lastKnown;
        private double bearing, poderTiro;
        private long fireTime;
        private int direcao;
        private int[] returnSegment;
        private long lastTime;
        
        public BulletWave(Point2D.Double location, Point2D.Double eneposAtual, double bearing, double poderTiro, long fireTime, int direcao, int[] segment, long time) {
            this.origin = location;
            this.lastKnown = eneposAtual;
            this.bearing = bearing;
            this.poderTiro = poderTiro;
            this.fireTime = fireTime;
            this.direcao = direcao;
            this.returnSegment = segment;
            lastTime = time;
        }
        
        public double getBulletSpeed() {
            return 20 - poderTiro * 3;
        }
        
        public double maxEscapeAngle() {
            return Math.asin(8 / getBulletSpeed()); 
        }
        
        public boolean waveHit(Point2D.Double posInimigo, long time) {
            long dt = time - lastTime;
            double dx = (posInimigo.getX() - lastKnown.getX()) / dt;
            double dy = (posInimigo.getY() - lastKnown.getY()) / dt;
            
            while(lastTime < time) {
                if(origin.distance(posInimigo) <= (lastTime - fireTime) * getBulletSpeed()) {
                    double desiredDirection = Math.atan2(posInimigo.getX() - origin.getX(), posInimigo.getY() - origin.getY());
                    double angleOffset = robocode.util.Utils.normalRelativeAngle(desiredDirection - bearing);
                    double guessFactor = Math.max(-1, Math.min(1, angleOffset / maxEscapeAngle())) * direcao;
                    int index = (int) Math.round((returnSegment.length - 1) / 2 * (guessFactor + 1));
                    returnSegment[index]++;
                    return true;
                }
                lastTime++;
                lastKnown = new Point2D.Double(lastKnown.getX() + dx, lastKnown.getY() + dy);
            }
            return false;
        }
    }
    
}
