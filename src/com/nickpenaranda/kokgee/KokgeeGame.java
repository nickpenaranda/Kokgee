package com.nickpenaranda.kokgee;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

public class KokgeeGame extends BasicGame {
  public static final int BOARD_WIDTH = 10;
  public static final int BOARD_HEIGHT = 20;
  public static final int PIECE_SIZE = 16;

  private static final int BOARD_PADDING_V = 40;
  
  /*
   * Timing-related settings
   * ROW_KILL_INTERVAL        Time (ms) to pause between cleared rows disappearing
   * MOVEMENT_INTERVAL        Time (ms) to pause between piece movement impulses
   * START_LEVEL              Starting difficulty
   * BOOST_INTERVAL_INDEX     When boost is active, what interval mLevel should it use (default: max)
   * HALT_PAUSE               After setting a piece to rest, how long to pause (ms)
   */
  private static final int ROW_KILL_INTERVAL = 100;
  private static final int MOVEMENT_INTERVAL = 80;
  private static final int START_LEVEL = 0;
  private static final int BOOST_INTERVAL_INDEX = 9;
  private static final int HALT_PAUSE = 250;
  
  /*
   * PHYSICS_INTERVALS        Array of times (ms) between drop impulses for each
   *                          mLevel of difficulty
   *                          
   * The function below (and its output for x=1,2,3,...,20) were used to create
   * values for exponential decay of the interval                         
   * f(x) = exp(-x/8 + 1/8)
   * 750 662 584 515 455 401 354 313 276 243 215 190 167 148 130 115 102  90  79  70
  */
//  public static final int[] PHYSICS_INTERVALS = 
//    {750, 662, 584, 515, 455,
//     401, 354, 313, 276, 243,
//     215, 190, 167, 148, 130,
//     115, 102, 90, 79, 70};
  
  /*
   * These are the official timings
   */
  public static final int[] PHYSICS_INTERVALS =
    {500, 450, 400, 350, 300,
     250, 200, 150, 100, 50};
  
  private static final int LINES_PER_LEVEL = 10;
  
  /*
   * Mutually exclusive movement input states
   */
  private enum Movement {
    NONE,LEFT,RIGHT;
  }

  private static Random R = new Random();

  private static ArrayList<Shape> mShapes;
  
  private Piece mPiece;
  private Shape mNextShape;
  private int mBoard[][];
  
  private Stack<Integer> mRowsToKill;
  
  private Movement movement;
  
  /*
   * Game-state variables
   * mLevel               Current level
   * mPoints              Player points
   * mGameTicks           Total time elapsed (ms)
   * mNextPhysicsTick     Time (relative to mGameTicks) for next physics update
   * mNextMovementTick    As above, but for piece movement update
   * mNextRowKillTick     As above, for animating row clearing.  Note that the
   *                      row kill code in update() blocks physics/movement updates
   * mLinesCompleted      Total lines completed
   * mLinesToNextLevel    # of lines needed before level increase
   * bBoost               Is boost (aka fast drop) currently active
   * bEnableBoost         Can boost be activated?                      
   */
  
  private int mLevel;
  private int mPoints;
  private int mGameTicks;
  private int mNextPhysicsTick;
  private int mNextMovementTick;
  private int mNextRowKillTick;
  private int mLinesCompleted;
  private int mLinesToNextLevel;
  
  private boolean bBoost;
  private boolean bEnableBoost;
  private boolean bSounds;
  private boolean bForcePhysics; // @HACK

  /*
   * Sounds
   * mSoundGameover       Upon lose condition
   * mSoundSetPiece       Upon setting a piece down
   * mSoundClear1-4       Upon clear <1,2,3 or 4> lines at once
   */
  private Sound mSoundGameover;
  private Sound mSoundSetPiece;
  private Sound mSoundClear1;
  private Sound mSoundClear2;
  private Sound mSoundClear3;
  private Sound mSoundClear4;
  
  /*
   * Rendering colors, see Shape.initColors()
   */
  private Color[] mColors;
  
  /*
   * Barebones constructor simply calls super constructor to set process name
   */
  public KokgeeGame() {
    super("Kokgee");
  }
  
  @Override
  public void init(GameContainer arg0) throws SlickException {
    mBoard = new int[BOARD_WIDTH][BOARD_HEIGHT];
    for(int x=0;x<BOARD_WIDTH;++x)
      for(int y=0;y<BOARD_HEIGHT;++y)
        mBoard[x][y] = 0;
    
    mShapes = Shape.initShapes();
    mColors = Shape.initColors();
    
    mGameTicks = 0;
    mNextPhysicsTick = 0;
    mNextRowKillTick = 0;

    mRowsToKill = new Stack<Integer>();

    mLevel = START_LEVEL;
    mPoints = 0;
    mLinesCompleted = 0;
    mLinesToNextLevel = LINES_PER_LEVEL;
    
    bEnableBoost = true;
    bBoost = false;
    bForcePhysics = false;
    movement = Movement.NONE;
    
    bSounds = true;
    try {
      mSoundGameover = new Sound("res/you_lose.wav");
      mSoundSetPiece = new Sound("res/block_set.wav");
      mSoundClear1 =   new Sound("res/clear_x1.wav");
      mSoundClear2 =   new Sound("res/clear_x2.wav");
      mSoundClear3 =   new Sound("res/clear_x3.wav");
      mSoundClear4 =   new Sound("res/clear_x4.wav");
    } catch(SlickException e) {
      e.printStackTrace();
      bSounds = false;
      // @TODO Do the right thing!!!
    }
    
    genRandom();
    spawnNext();
  }

  @Override
  public void render(GameContainer c, Graphics g) throws SlickException {
    /*
     * Calculate bottom left corner of board
     */
    int bx = (c.getWidth() / 2) - (PIECE_SIZE * BOARD_WIDTH / 2);
    int by = (PIECE_SIZE * BOARD_HEIGHT) + BOARD_PADDING_V;
    
    int prex = (c.getWidth() / 2) + (PIECE_SIZE * BOARD_WIDTH / 2) + PIECE_SIZE;
    int prey = BOARD_PADDING_V + (PIECE_SIZE * 4) + PIECE_SIZE;
    /*
     * Render text information
     */
    g.setColor(Color.cyan);
    g.drawString("Level " + mLevel, 16, 8);
    g.drawString(mLinesToNextLevel - mLinesCompleted + " lines until next mLevel!", 16, 24);
    g.drawString("Points: " + mPoints, 16, 40);
    
    /*
     * Render field background with border
     */
    g.setColor(Color.darkGray);
    g.fillRect(bx, by + PIECE_SIZE, PIECE_SIZE * BOARD_WIDTH, -1 * PIECE_SIZE * BOARD_HEIGHT);
    g.setColor(Color.white);
    g.drawRect(bx, by + PIECE_SIZE, PIECE_SIZE * BOARD_WIDTH, -1 * PIECE_SIZE * BOARD_HEIGHT);
    
    /*
     * Render board contents
     */
    for(int x=0;x<BOARD_WIDTH;++x)
      for(int y=0;y<BOARD_HEIGHT;++y)
        if(mBoard[x][y] != 0) {
          g.setColor(mColors[mBoard[x][y]]);
          g.fillRect(bx + (x * PIECE_SIZE), by - (y * PIECE_SIZE), PIECE_SIZE, PIECE_SIZE);
          g.setColor(Color.white);
          g.drawRect(bx + (x * PIECE_SIZE), by - (y * PIECE_SIZE), PIECE_SIZE, PIECE_SIZE);
        }
    
    /*
     * Render next piece
     */
    g.drawString("Next piece", prex, prey - (PIECE_SIZE * 4));
    
    for(int k : mNextShape.getParts(0)) {
      int px = (k - 1) % 4;
      int py = (k - 1) / 4;
      g.setColor(mColors[mNextShape.getColor()]);
      g.fillRect(prex + (px * PIECE_SIZE), prey - (py * PIECE_SIZE), PIECE_SIZE, PIECE_SIZE);
      g.setColor(Color.white);
      g.drawRect(prex + (px * PIECE_SIZE), prey - (py * PIECE_SIZE), PIECE_SIZE, PIECE_SIZE);
    }
    
    /*
     * Render active piece
     */
    if(mPiece != null) {
      int ox = bx + mPiece.getX() * PIECE_SIZE;
      int oy = by - (mPiece.getY() * PIECE_SIZE);
      int opx = mPiece.getX();
      int opy = mPiece.getY();
      
      for(int k : mPiece.getParts()) {
        int px = (k - 1) % 4;
        int py = (k - 1) / 4;
        if(opx + px < 0 || opx + px >= BOARD_WIDTH || opy + py < 0 || opy + py >= BOARD_HEIGHT)
          continue;
        g.setColor(mColors[mPiece.getColor()]);
        g.fillRect(ox + (px * PIECE_SIZE), oy - (py * PIECE_SIZE), PIECE_SIZE, PIECE_SIZE);
        
        g.setColor(Color.white);
        g.drawRect(ox + (px * PIECE_SIZE), oy - (py * PIECE_SIZE), PIECE_SIZE, PIECE_SIZE);
      }
    } 
  }
  
  @Override
  /*
   * Each call of the update method first checks if there are any outstanding rows
   * to kill.  If so, the game will animate those at the exclusion of any physics/movement.
   * Afterward, normal movement and physics ticks resume.  Both counts are independent but blocked
   * by the row killing
   * (non-Javadoc)
   * @see org.newdawn.slick.BasicGame#update(org.newdawn.slick.GameContainer, int)
   */
  public void update(GameContainer c, int delta) throws SlickException {
    mGameTicks += delta;
    
    /*
     *  Outstanding rows in kill stack
     */
    if(!mRowsToKill.isEmpty() && mGameTicks > mNextRowKillTick) {
      int row = mRowsToKill.pop().intValue();
      for(int r=row;r<BOARD_HEIGHT-2;++r) // Shift all values down one row
        for(int x=0;x<BOARD_WIDTH;++x)
          mBoard[x][r] = mBoard[x][r+1];
      
      for(int x=0;x<BOARD_WIDTH;++x) // Empty top of board
        mBoard[x][BOARD_HEIGHT-1] = 0;
      
      // Increment line counts/thresholds and level as appropriate
      mLinesCompleted++;
      if(mLinesCompleted >= mLinesToNextLevel) {
        mLevel++;
        mLinesToNextLevel += LINES_PER_LEVEL;
      }
      mNextRowKillTick = mGameTicks + ROW_KILL_INTERVAL;
    }
    else { // IFF no rows to be killed, process movement ticks first then physics ticks
      if(mGameTicks > mNextMovementTick) {
        switch(movement) {
          case LEFT:
            doMoveLeft();
            break;
          case RIGHT:
            doMoveRight();
            break;
        }
        mNextMovementTick = mGameTicks + MOVEMENT_INTERVAL;
      }
      
      // Note the bForcePhysics flag which causes piece boosting to immediately
      // take effect. @HACK
      if(mGameTicks > mNextPhysicsTick || bForcePhysics) {
        bForcePhysics = false;
        if(bBoost)
          mNextPhysicsTick = mGameTicks + PHYSICS_INTERVALS[BOOST_INTERVAL_INDEX];
        else
          mNextPhysicsTick = mGameTicks + PHYSICS_INTERVALS[mLevel];
        doPhysics();
      }
    }
  }

  @Override
  public void keyPressed(int key, char c) {
    switch(key) {
      case Input.KEY_LEFT:
        movement = Movement.LEFT;
        break;
      case Input.KEY_RIGHT:
        movement = Movement.RIGHT;
        break;
      case Input.KEY_DOWN:
        if(bEnableBoost)
          bForcePhysics = true;
          bBoost = true;
        break;
      case Input.KEY_SPACE:
        doDrop();
        break;
      case Input.KEY_F:
        doRotateCW();
        break;
      case Input.KEY_D:
        doRotateCCW();
        break;
      case Input.KEY_F1:
        if(mLevel > 0) mLevel--;
        break;
      case Input.KEY_F2:
        if(mLevel < PHYSICS_INTERVALS.length - 1) mLevel++;
        break;
      case Input.KEY_ESCAPE:
        System.exit(-1);
        break;
    }
  }

  @Override
  public void keyReleased(int key, char c) {
    switch(key) {
      case Input.KEY_LEFT:
        movement = Movement.NONE;
        mNextMovementTick = mGameTicks;
        break;
      case Input.KEY_RIGHT:
        movement = Movement.NONE;
        mNextMovementTick = mGameTicks;
        break;
        
      case Input.KEY_DOWN:
        if(bEnableBoost)
          bBoost = false;
        break;
    }
  }

  public static void main(String[] args) {
    try {
      AppGameContainer appGameContainer = new AppGameContainer(new KokgeeGame());
      appGameContainer.setDisplayMode(640, 480, false);
      appGameContainer.setMinimumLogicUpdateInterval(10);
      appGameContainer.setTargetFrameRate(100);
      appGameContainer.setShowFPS(false);
      appGameContainer.start();
    } catch (SlickException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }  
  }

  
  /*
   * Creates a new piece at the prescribed position and orientation
   */
  private void spawnNext() {
    mPiece = new Piece(mNextShape,
                       0,
                       3,BOARD_HEIGHT - 2); // places position 11 at (6,20)
    genRandom();
  }
  
  /*
   * Selects a next piece
   */
  private void genRandom() {
    int numShapes = mShapes.size();
    int shapeIndex = R.nextInt(numShapes);
    mNextShape = mShapes.get(shapeIndex);
  }
  
  /*
   * After a piece has collided on a physics tick, this method destroys the piece
   * and updates the board, then calls checkLines to check for completions
   */
  private void haltPiece() {
    int ox = mPiece.getX();
    int oy = mPiece.getY();
    
    if(bSounds)
      mSoundSetPiece.play();
    
    for(int k : mPiece.getParts()) {
      int px = ox + ((k - 1) % 4);
      int py = oy + ((k - 1) / 4);
      if(px >= 0 && px < BOARD_WIDTH && py >= 0 && py < BOARD_HEIGHT)
        mBoard[px][py] = mPiece.getColor();
      else {
        // @ENDGAME
        if(bSounds)
          mSoundGameover.play();
        //System.exit(-1);
      }
        
    }
    mPiece = null;
    checkLines();
    mNextPhysicsTick += HALT_PAUSE;
    bBoost = false;
  }
  
  /*
   * Iterates through all rows, bottom to top, and checks to see if they are complete.
   * If so, adds them to the row kill list.  Also plays the appropriate sound when
   * one or more lines were completed
   */
  private void checkLines() {
    int numComplete = 0;
    for(int r=0;r<BOARD_HEIGHT;++r) {
      boolean rowComplete = true;
      for(int c=0;c<BOARD_WIDTH && rowComplete;++c) {
        if(mBoard[c][r] == 0) rowComplete = false;
      }
      if(rowComplete) {
        numComplete++;
        mRowsToKill.push(new Integer(r));
      }
    }
    
    switch(numComplete) {
      case 1:
        mPoints += 40 * (mLevel + 1);
        break;
      case 2:
        mPoints += 100 * (mLevel + 1);
        break;
      case 3:
        mPoints += 300 * (mLevel + 1);
        break;
      case 4:
        mPoints += 1200 * (mLevel + 1);
        break;
    }
    
    if(bSounds) {
      switch(numComplete) {
        case 1:
          mSoundClear1.play();
          break;
        case 2:
          mSoundClear2.play();
          break;
        case 3:
          mSoundClear3.play();
          break;
        case 4:
          mSoundClear4.play();
          break;
      }
    }
  }

  /*
   * Checks all parts of a piece with origin (ox,oy) for collisions against
   * board boundaries and occupied board positions
   * @param   ox    x-origin, relative to board, to check parts against
   * @param   oy    y-origin, relative to board, to check parts against
   * @return        true if a collision was detected
   */
  private boolean pieceCollides(int ox,int oy, int[] parts) {
    boolean bNoGo = false;
    for(int k : parts) {
      int px = ox + ((k - 1) % 4);
      int py = oy + ((k - 1) / 4);
      if(py < 0 // position would fall off the bottom
         || px < 0 // position too far left
         || px >= BOARD_WIDTH // position too far right
         || py >= BOARD_HEIGHT // position too high
         || mBoard[px][py] != 0) { // within board height and collides
         
        bNoGo = true;
        break;
      }
    }
    return(bNoGo);
  }
  
  /*
   * Called on every physics tick, this basically moves the piece down or
   * sets it in place if it would collide if moved down.  Also spawns a new
   * piece if no pieces are active
   */
  private void doPhysics() {
    if(mPiece == null)
      spawnNext();

    if(pieceCollides(mPiece.getX(),mPiece.getY() - 1,mPiece.getParts())) haltPiece();
    else mPiece.moveDown();
  }
  
  /*
   * Drops the piece directly down and sets it
   */
  private void doDrop() {
    if(mPiece != null) {
      while(!pieceCollides(mPiece.getX(),mPiece.getY() - 1,mPiece.getParts()))
        mPiece.moveDown();
      haltPiece();
    }
  }
  
  /*
   * doMoveLeft/doMoveRight are called when movement is LEFT or RIGHT, respectively.
   * bound and piece collision checking are performed to deny movement
   */
  private void doMoveLeft() {
    if(mPiece != null) {
      if(!pieceCollides(mPiece.getX() - 1,mPiece.getY(),mPiece.getParts())) mPiece.moveLeft();
    }
  }
  
  private void doMoveRight() {
    if(mPiece != null) {
      if(!pieceCollides(mPiece.getX() + 1,mPiece.getY(),mPiece.getParts())) mPiece.moveRight();
    }
  }
  
  /* 
   * Similar to movement do* methods above, but performs an extra Y transform to maintain
   * lowest extent of block.  This logic is extended in the actual rotate* methods of the 
   * piece class
   * UPDATE: The extra Y transform logic above has been scrapped
   */
  private void doRotateCW() {
    if(mPiece != null) {
      if(!pieceCollides(mPiece.getX(),mPiece.getY(),mPiece.peekCW()))
        mPiece.rotateCW();
    }
  }

  private void doRotateCCW() {
    if(mPiece != null) {
      if(!pieceCollides(mPiece.getX(),mPiece.getY(),mPiece.peekCW()))
        mPiece.rotateCCW();
    }
  }
}
