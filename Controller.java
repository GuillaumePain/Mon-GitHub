
import java.util.ArrayList;
import java.util.Random;

import edu.wsu.KheperaSimulator.RobotController;
import edu.wsu.KheperaSimulator.KSGripperStates;


public class Controller extends RobotController{


	public Controller() {
	
	}
	
	// sensors
	public final static int LEFT = 0;
	public final static int ANGLEL = 1;
	public final static int FRONTL = 2;
	public final static int FRONTR = 3;
	public final static int ANGLER = 4;
	public final static int RIGHT = 5;
	public final static int BACKR = 6;
	public final static int BACKL = 7;
	
	// distance to pick a ball or not hit a wall
	private final static int IMPACT_DISTANCE = 800;
	private final static int PASSAGE_DISTANCE = 250;
	
	private Random rand = new Random();
	
	// next move to be processed
	protected Move currentMove = null;
	
	// where it is supposed to go - fill in initialize()
	protected ArrayList<Move> trackPlan = new ArrayList<Move>();
	// history of moves
	protected ArrayList<Move> history = new ArrayList<Move>();
	
	// counts steps covered if moving forward or to be covered when retracing
	protected long forwardCounter = 0;
	// checks if retracing is active
	protected boolean retracingActive = false;
	// whether plan (false) or history (true) is used
	protected boolean returning = false;

	// states for switch
	protected int state = 0;
	
	// check if we're compensating.
	protected boolean slowSpeed = false; 

	public void doWork() throws Exception {

		switch(state) {
		
		case 0:
			if (moveToCrossing()){
				state ++;
			}
			break;
			
		case 1:
			if (moveForwardSteps(13)){
				state ++;
			}
			break;
				
		case 2:
			int way = selectWay();
			if (way == 1){
				turn(90);
			}
			else if (way == -1){
				turn(-90);
			}
			else if (way == 0){
			}
			else {
				turn(180);
			}
			state ++;
			break;
					
//		case 3:
//			if (moveForwardSteps(15)){
//				state ++;
//			}
//			break;					
		
		default:
			state = 0;


		}

	}

	/* (non-Javadoc)
	 * @see edu.wsu.KepheraSimulator.Controller#close()
	 */
	public void close() throws Exception {

	}
	
	private int selectWay(){
		int result = Integer.MIN_VALUE;
		
		if (canMoveLeft() && !canMoveRight()){
			result = -1;
		}
		else if (!canMoveLeft() && canMoveRight()){
			result = 1;
		}
		else if (canMoveLeft() && canMoveRight()){
			/*if (rand.nextDouble() > 0.5){
				result = 1;
			}
			else {
				result = -1;
			}*/
			result = 1;
		}
		
		if (canMoveForward() && (result == Integer.MIN_VALUE)){
			result = 0;
		}		
		
		return result;
	}
	
	/**
	 * Selecting respective states for moves
	 * @param m move to process
	 */
	private void processMove(Move m){
//		System.out.println("Next move: " + m.getType() + ", " + m.getSize());
		
		if (m.getType().equals(Move.FORWARD)){
			currentMove = m;
			state = 7;
		}
		else if (m.getType().equals(Move.TURN)){
			currentMove = m;
			state = 6;
		}
		else {
			System.out.println("error occured");
			state = 8;
		}
	}
	
	/**
	 * Goes until it hits something
	 * Number of steps covered stored in forwardCounter
	 * @return finished or not
	 */
	private boolean moveForward(){
		long dist = goToObstacle();
		if (dist != Long.MIN_VALUE){
			forwardCounter = 0;
			return true;
		}
		
		return false;
	}
			
	private long goToObstacle(){
		boolean done = false;
		
		if (canMoveForward()){
			setMotorSpeeds(3,3);
			forwardCounter ++;
		}
		else {
			setMotorSpeeds(0,0);
			done = true;
		}
		
		if (done){
			return forwardCounter;
		}
		else{
			return Long.MIN_VALUE;
			
		}
	}
	
	private boolean moveToCrossing(){
		long dist = goToPassage();
		if (dist != Long.MIN_VALUE){
			forwardCounter = 0;
			return true;
		}
		
		return false;
	}
	
	private long goToPassage(){
		boolean done = false;
				
		if (canMoveForward()){
			if (canMoveLeft() || canMoveRight()){
				setMotorSpeeds(0,0);
				done = true;
			}
			else {
				setMotorSpeeds(5,5);
				forwardCounter ++;
			}
		}
		else {
			setMotorSpeeds(0,0);
			done = true;
		}
		
		
		if (done){
			return forwardCounter;
		}
		else{
			return Long.MIN_VALUE;
		}
	}
	
	private boolean canMoveForward(){
		if((getDistanceValue(FRONTL) < IMPACT_DISTANCE) && (getDistanceValue(FRONTR) < IMPACT_DISTANCE)) {
	          return true;
	    } else {
	        return false;
	    }
	}
	
//	private boolean canMoveForward(){
//		  int open;
//		  if((getDistanceValue(FRONTL) < IMPACT_DISTANCE) && (getDistanceValue(FRONTR) < IMPACT_DISTANCE)) {
//		           open = 0;
//		     } else {
//		      if (getDistanceValue(FRONTL) < IMPACT_DISTANCE &&  (getDistanceValue(FRONTR) > IMPACT_DISTANCE)){
//		       open = 1;
//		      }
//		      if (getDistanceValue(FRONTL) > IMPACT_DISTANCE &&  (getDistanceValue(FRONTR) < IMPACT_DISTANCE)){
//		          open = 2;
//		      }
//		      else {
//		       open = 3;
//		      }
//		  }
//		  switch(open){
//			  case 0:
//			   return true;
//			  
//			  case 1: //slight turn left
//			   turnLeft(45);
//			   open = 0;
//			   break;
//			   
//			  case 2: // slight turn right
//			   turnRight(45);
//			   open = 0;
//			   break;
//			   
//			  case 3:
//			   return false;
//			   
//			  default:
//		  }
//		  return false;
//		 }
	
	private boolean canMoveRight(){
		if((getDistanceValue(RIGHT) < PASSAGE_DISTANCE) && (getDistanceValue(ANGLER) < PASSAGE_DISTANCE * 0.5) && getDistanceValue(BACKR) < PASSAGE_DISTANCE*0.3) {
	          return true;
	    } else {
	        return false;
	    }
	}
	
	private boolean canMoveLeft(){
		if((getDistanceValue(LEFT) < PASSAGE_DISTANCE) && (getDistanceValue(ANGLEL) < PASSAGE_DISTANCE * 0.5) && getDistanceValue(BACKL) < PASSAGE_DISTANCE*0.3) {
	          return true;
	    } else {
	        return false;
	    }
	}
	
	
	/**
	 * retraces x steps
	 * @param x number of steps to retrace
	 * @return
	 */
	private boolean moveForwardSteps(long x){
		if (!retracingActive){
			forwardCounter = x;
			retracingActive = true;
		}
		
		return retrace();
		
	}
	
//	private boolean retrace(){
//		if (canMoveForward()){
//			if (forwardCounter > 0){			
//				setMotorSpeeds(5,5);
//				forwardCounter --;
//			}
//			else {
//				setMotorSpeeds(0,0);
//				retracingActive = false;
//				return true;
//			}
//		}
//		else {
//			setMotorSpeeds(0,0);
//			retracingActive = false;
//			if (forwardCounter <= 0){			
//				return true;
//			}			
//		}
//		
//		return false;
//	}
	
	private boolean retrace(){
		if (canMoveForward() && (forwardCounter > 0)){			
				setMotorSpeeds(3,3);
				forwardCounter --;
		}
		else {
			setMotorSpeeds(0,0);
			retracingActive = false;
			return true;
		}
		return false;
	}
	
	/**
	 * Creating a plan
	 */
	private void initialize(){
		trackPlan.add(new Move("forward",50));
		trackPlan.add(new Move("turn",90));
		trackPlan.add(new Move("forward",80));
		trackPlan.add(new Move("turn",90));
		trackPlan.add(new Move("forward",100));
//		trackPlan.add(new Move("turn",90));
//		trackPlan.add(new Move("forward",100));
//		trackPlan.add(new Move("turn",90));
//		trackPlan.add(new Move("forward",100));
//		trackPlan.add(new Move("turn",-90));
//		trackPlan.add(new Move("forward",100));
//		trackPlan.add(new Move("turn",-90));
//		trackPlan.add(new Move("forward",50));
		
		// turns at the start when it returns
		history.add(new Move("turn",0));
	}
	
	/**
	 * Opens gripper puts arms down
	 */
	
	private void prepareGripper(){
		if (getGripperState() == KSGripperStates.GRIP_CLOSED){
			setGripperState(KSGripperStates.GRIP_OPEN);
		}
		if (getArmState() == KSGripperStates.ARM_UP){
			setArmState(KSGripperStates.ARM_DOWN);
		}
	}
	
	/**
	 * Turns a robot specified angle
	 * @param angle to turn
	 */
	
	private void turn(long angle){
		angle = angle + 360;
		angle = angle % 360;
		if (angle > 180){
			turnLeft(angle - 360);
		}
		else {
			turnRight(angle);
		}
	}
	
	private void turnLeft(long angle){
		
		// target position
		long target = getLeftWheelPosition() + 3*angle;
				
		while (getLeftWheelPosition() != target) {
				
			// Checks if the robot have overturned and must adjust
			if (((getLeftWheelPosition() < target) && (angle < 0)) ||
					(getLeftWheelPosition() > target) && (angle > 0)) {

				long offset = (getLeftWheelPosition() - target);

				angle = -offset*3;
				target = getLeftWheelPosition() - offset;
				slowSpeed = true;

				//	 System.out.println("TurnDegrees: Wanted to turn to " + target + " but turned to" + getLeftWheelPosition() + "("+offset+") COMPENSATING!");

			}

			int speed;
			if(slowSpeed)
				speed = 1;
			else
				speed = 5;

			// keep on turning
			if(angle > 0) {
				setMotorSpeeds(speed, -speed); // Right turn
			} else {
				setMotorSpeeds(-speed, speed); // Left turn
			}
		}
		// stop
		setMotorSpeeds(0,0);
		
	}
	
	private void turnRight(long angle){
		// target position
		long target = getRightWheelPosition() - 3*angle;
		
//		System.out.println("Position: " + getRightWheelPosition());
//		System.out.println("Target " + target);
				
		while (getRightWheelPosition() != target) {
				
			// Checks if the robot have overturned and must adjust
			if (((getRightWheelPosition() < target) && (angle > 0)) ||
					(getRightWheelPosition() > target) && (angle < 0)) {

				long offset = (getRightWheelPosition() - target);

				angle = -offset*3;
				target = getRightWheelPosition() - offset;
				slowSpeed = true;

//				System.out.println("TurnDegrees: Wanted to turn to " + target + " but turned to" + getRightWheelPosition() + "("+offset+") COMPENSATING!");

			}

			int speed;
			if(slowSpeed)
				speed = 1;
			else
				speed = 5;

			// keep on turning
			if(angle > 0) {
				setMotorSpeeds(speed, -speed); // Right turn
			} else {
				setMotorSpeeds(-speed, speed); // Left turn
			}
		}
		
		setMotorSpeeds(0,0);
		
	}
}
