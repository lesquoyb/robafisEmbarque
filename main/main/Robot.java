package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Calendar;
import java.util.Date;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.utility.TextMenu;



public class Robot {

	public static final ReadColor Red = new ReadColor(255, 0, 0);
	public static final ReadColor Yellow = new ReadColor(255, 255, 0);
	public static final ReadColor Green = new ReadColor(0, 255, 0);
	public static final ReadColor Blue = new ReadColor(0, 0, 255);


	private SampleProvider colorSampler;
	private float[] colorSample;
	private EV3ColorSensor colorSensor;
	private void initColorSensor(){
		colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
		colorSampler = colorSensor.getRedMode();
		colorSample = new float[3];
	}

	public ReadColor black = new ReadColor(15, 15, 15), 
			white = new ReadColor(255,255,255),
			objectif = new ReadColor(135, 135, 135);
	private void calibrateBlackAndWhite(){
		white = new ReadColor(0, 0, 0);
		black = new ReadColor(255, 255, 255);
		ReadColor tmp;
		for(int i = 0 ; i < 36 ; i++){
			turn(10);
			tmp = readColor();
			if(tmp.isClearerThan(white)){
				white = tmp;
			}
			else if (tmp.isDarkerThan(black)){
				black = tmp;
			}
		}
		int average = white.getAverage() - black.getAverage();
		objectif = new ReadColor(average, average, average);
	}
	public ReadColor readColor(){
		colorSampler.fetchSample(colorSample, 0);
		return new ReadColor(Math.min( (int) (colorSample[0]*255), 255), 
				Math.min( (int) (colorSample[0]*255), 255),
				Math.min( (int) (colorSample[0]*255), 255)); //TODO: valide seulement avec lecture d'une couleur
	}



	private EV3GyroSensor gyro;
	private SampleProvider gyroSampler;
	private static float[] gyroSample;
	private final NXTRegulatedMotor pivotRotation = Motor.B;
	private final NXTRegulatedMotor motorRotation = Motor.A;
	private final NXTRegulatedMotor motorL = Motor.B;
	private final NXTRegulatedMotor motorR = Motor.A;
	private void  initGyro(){
		gyro = new EV3GyroSensor(LocalEV3.get().getPort("S2"));
		gyroSampler =  gyro.getAngleMode();
		gyroSample = new float[1];
	}
	static final byte position_angle_dans_sample = 0;
	static int nombre_de_degre_par_rotation_moteur = 61;
	static final int rotation_minimale_pour_se_deplacer = 3;//TODO √† r√©gler √† la main avec le robot final
	private void calibrateGyro(){
		gyro.reset();
		motorRotation.rotate(360);
		gyroSampler.fetchSample(gyroSample, 0);
		nombre_de_degre_par_rotation_moteur = (int)gyroSample[position_angle_dans_sample];
	}

	


	
	public enum Modes {Teleguide, FollowLine};

	
	public Modes mode;
	private ServerSocket Server ;
	public Robot() throws Exception{

		//initBluetooth();
		initColorSensor();
		//setMode(Modes.FollowLine);
		//initGyro();
	}


	private void initBluetooth() throws IOException{
		
		BluetoothServer server = new BluetoothServer(this);
		Thread t = new Thread(server);
		t.start();
		
	}

	
	public void close(){
		colorSensor.close();
		gyro.close();
	}

	public void calibrer(){
		//S√©rialiser les valeurs ?
		calibrateBlackAndWhite();
		System.out.println("black: " + black + "\n white: " +white);
		Delay.msDelay(10000);
		calibrateGyro();
	}
	
	
	Thread threadFollowLine = new Thread(new Runnable() {
		
		@Override
		public void run() {
			try {
				System.out.println("follow line");
				followLine(Red);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	});
	public void setMode(Modes m) throws Exception{
		if(m != mode){
			mode = m;

			threadFollowLine.interrupt();
			
			switch (m){
			case FollowLine:
				threadFollowLine.start();
				break;
				
			case Teleguide:
				
				break;
				
				default:
					throw new Exception("mode non gÈrÈ");
			}
		}
	}

	public void choisirScenario() throws Exception{
		String[] menu = new String[]{"Rouge","Vert","Jaune", "Calibrer", "Quitter"};
		TextMenu m = new TextMenu(menu);
		int index = m.select();
		ReadColor selectedColor = null;
		switch(menu[index]){
		case "Rouge":
			selectedColor = Red;
			break;

		case "Vert":
			selectedColor = Green;				
			break;

		case "Jaune":
			selectedColor = Yellow;
			break;
		case "Calibrer":
			m.quit();
			calibrer();
			choisirScenario();
			return ;
		case "Quitter":
			m.quit();
			return;
		default:
			throw new Exception("Index du menu non g√©r√©: " + menu[index]);
		}
		Sound.beep();
		followLine(selectedColor);
	}

	public final ReadColor presqueRouge = new ReadColor(200, 200, 200);
	private boolean colorDetected(ReadColor read, ReadColor goal) throws Exception{
		if(read.isDarkerThan(presqueRouge)){
			//TODO
		}
		return false;
	}


	private long start;
	private int delay = 10;//Taux de rafraichissement pendant le suivit de ligne
	private int maxBaseSpeed = 250;
	private double 	kpBend= .25, 	kpNormal = 1.5,
					kiBend= 0,	 	kiNormal = 0.0,
					kdBend= 0,		kdNormal = 0;
	public void followLine(ReadColor color) throws Exception{

		//TODO: si pas vue noir depuis longtemps, arrÍt et recherche
		
		int baseSpeed = maxBaseSpeed,
				minSpeed = 20;


		double kp ,ki, kd ;

		double virage;


		double error, sum_errors = 0, last_error = 0, delta_error;

		ReadColor read = readColor() ;

		motorL.setSpeed(1);
		motorR.setSpeed(1);
		//motorL.setAcceleration(12000);
		//motorR.setAcceleration(12000);
		
		motorL.backward();
		motorR.backward();
		
		int range = white.getAverage() - black.getAverage();

		colorSensor.setFloodlight(true);
		start = Calendar.getInstance().getTimeInMillis();
		
		baseSpeed = maxBaseSpeed;
		kp = kpBend;
		ki = kiBend;
		kd = kdBend;
		
		int marge = 50;
		long begin = Calendar.getInstance().getTimeInMillis();
		
		while( ! colorDetected(read, color) ){
			
			read = readColor();

			error = objectif.getAverage() - read.getAverage();
			sum_errors += error;
			delta_error = error - last_error;
			last_error = error;

			virage = kp * error + ki * sum_errors + kd * delta_error;

			//TODO: limiter ki * sum_errors
			if( read.getAverage()  >= (white.getAverage() - marge) ){
				if (  Calendar.getInstance().getTimeInMillis() - begin >= 600 ){
					f();
					begin = Calendar.getInstance().getTimeInMillis();
				}
			}
			else{
				begin = Calendar.getInstance().getTimeInMillis();
			}
			
			//System.out.println(virage + " " + kp + " " +  ki + " " + kd);
			motorL.setSpeed((int) Math.min( Math.max( baseSpeed - virage, minSpeed), 720));
			motorR.setSpeed((int) Math.min( Math.max(  baseSpeed +  virage, minSpeed), 720));

			Delay.msDelay(delay);

		}
	
		motorL.stop();
		motorR.stop();

		Sound.beep();
	}
	
	
	private void f(){
		motorL.setSpeed(300);
		motorR.setSpeed(1);
		ReadColor read = readColor();
		do{
			Delay.msDelay(delay);
			read = readColor();
		}while(read.getAverage() >= black.getAverage() + 50);
		
		do{
			read = readColor(); 
			Delay.msDelay(delay);
		}while(read.getAverage() <= black.getAverage() + 50);
			
	}
	
	private long timeFirstBend = 1000;
	
	private double adaptCoef(int currentSpeed,double bendCoef, double normalCoef){
		/*
		if( Calendar.getInstance().getTimeInMillis() - start >= timeFirstBend ){
			return bendCoef;
		}
		else{
			return normalCoef;
		}
		*/
		return bendCoef;
		//return maxCoef - (currentSpeed/(double)maxBaseSpeed) * (maxCoef - minCoef);
	}
	
	private int adaptBaseSpeed(int baseSpeed){
		/*
		if( Calendar.getInstance().getTimeInMillis() - start >= timeFirstBend ){
			return 0;
		}
		else{
			return baseSpeed;
		}
		*/
		return 0;
		
		//TODO: Áa c'est une version qui modifie la vitesse en fonction du temps, ce serait surement mieux sur la version finale d'avoir 
		//		une fonction qui le fasse en fonction du nombre de rotation du moteur
		//      + l‡ c'est assez naze de toute faÁon, une fonction logarithmique serait mieux
		//return (int) (baseSpeed * (1.0- (1 * delay/(double)1000) ));
	}




	public void turn(int angle){

		//TODO: ne pas oublier de changer la rotation si on change de moteur pivot
		gyroSampler.fetchSample(gyroSample, 0);//DegrÈ actuel
		angle += gyroSample[position_angle_dans_sample];
		float mesure;
		int relAngle;
		do{
			gyroSampler.fetchSample(gyroSample, 0);
			mesure = gyroSample[position_angle_dans_sample];
			relAngle =  (int) ( (angle - mesure) * 100 / nombre_de_degre_par_rotation_moteur);
			relAngle = (relAngle > 0 ) ? Math.max(relAngle, rotation_minimale_pour_se_deplacer) : Math.min(relAngle, -rotation_minimale_pour_se_deplacer); //Pour √©viter de rester bloqu√©
			pivotRotation.rotate( - relAngle, false);
			Delay.msDelay(50);
			//TODO: attention on pourrait rester bloqu√©, d√©finir un mouvement minimum
		}while( angle != mesure );

	}




}
