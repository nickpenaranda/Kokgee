package com.nickpenaranda.kokgee;

 
public class Piece {
  private Shape mShape;
  private int mConfigIndex;
  private int mPosX, mPosY;
  
  public Piece(Shape shape, int config, int x, int y) {
    mShape = shape;
    mConfigIndex = config % mShape.getNumConfigs();
    mPosX = x;
    mPosY = y;
  }
  
  public int getX() { return(mPosX); }
  public int getY() { return(mPosY); }
  
  public void moveDown() { mPosY--; }
  public void moveLeft() { mPosX--; }
  public void moveRight() { mPosX++; }
  
  public int[] peekCW() {
    return(mShape.getParts((mConfigIndex + 1) % mShape.getNumConfigs()));
  }
  
  public int peekCWLR() {
    return(mShape.getLowestRow((mConfigIndex + 1) % mShape.getNumConfigs()));
  }
  
  public int[] peekCCW() {
    return(mShape.getParts((mConfigIndex > 0) ? mConfigIndex - 1 : mShape.getNumConfigs() - 1));
  }
    
  public int peekCCWLR() {
    return(mShape.getLowestRow((mConfigIndex > 0) ? mConfigIndex - 1 : mShape.getNumConfigs() - 1));
  }
  
//  public void rotateCW() {
//    int oldLowRow = getLowestRow();
//    mConfigIndex = (mConfigIndex + 1) % mShape.getNumConfigs();
//    
//    int newLowRow = getLowestRow();
//    int dLowRow = newLowRow - oldLowRow;
//    
//    mPosY -= dLowRow;
//    
//  }
  public void rotateCW() {
    mConfigIndex = (mConfigIndex + 1) % mShape.getNumConfigs();
  }
  
//  public void rotateCCW() { 
//    int oldLowRow = getLowestRow();
//    mConfigIndex = (mConfigIndex > 0) ? mConfigIndex - 1 : mShape.getNumConfigs() - 1;
//
//    int newLowRow = getLowestRow();
//    int dLowRow = newLowRow - oldLowRow;
//    
//    mPosY -= dLowRow;
//  }
  public void rotateCCW() {
    mConfigIndex = (mConfigIndex > 0) ? mConfigIndex - 1 : mShape.getNumConfigs() - 1;
  }
  
  public int getLowestRow() { return(mShape.getLowestRow(mConfigIndex)); }
  public int[] getParts() { return(mShape.getParts(mConfigIndex)); }
  public int getColor() { return(mShape.getColor()); }

  public String toString() {
    return(String.format("<Shape %s/%d @ (%d,%d)",mShape.getLabel(),mConfigIndex,mPosX,mPosY));
  }

}