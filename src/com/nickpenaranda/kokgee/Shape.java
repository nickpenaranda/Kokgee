package com.nickpenaranda.kokgee;

import java.util.ArrayList;

import org.newdawn.slick.Color;

public class Shape {
  private final ArrayList<Shape.ShapeConfig> mConfigs;
  private String mLabel;

  private int mNumConfigs;
  private int mColor;
  
  public Shape(String label, int color, Shape.ShapeConfig[] configs) {
    mLabel = label;
    mColor = color;
    mConfigs = new ArrayList<Shape.ShapeConfig>();
    for(Shape.ShapeConfig config : configs) {
      mConfigs.add(config);
    }
    mNumConfigs = mConfigs.size();
  }
  
  public static Color[] initColors() {
    return(new Color[] {Color.black,Color.magenta,Color.blue,Color.green,Color.red,Color.cyan,Color.yellow,Color.pink});
  }
  
  public static ArrayList<Shape> initShapes() {
    ArrayList<Shape> shapes = new ArrayList<Shape>();
    
    shapes.add(new Shape("I",1,new ShapeConfig[] {ShapeConfig.I1, ShapeConfig.I2}));
    shapes.add(new Shape("J",2,new ShapeConfig[] {ShapeConfig.J1, ShapeConfig.J2, ShapeConfig.J3, ShapeConfig.J4}));
    shapes.add(new Shape("L",3,new ShapeConfig[] {ShapeConfig.L1, ShapeConfig.L2, ShapeConfig.L3, ShapeConfig.L4}));
    shapes.add(new Shape("O",4,new ShapeConfig[] {ShapeConfig.O1}));
    shapes.add(new Shape("S",5,new ShapeConfig[] {ShapeConfig.S1, ShapeConfig.S2}));
    shapes.add(new Shape("T",6,new ShapeConfig[] {ShapeConfig.T1, ShapeConfig.T2, ShapeConfig.T3, ShapeConfig.T4}));
    shapes.add(new Shape("Z",7,new ShapeConfig[] {ShapeConfig.Z1, ShapeConfig.Z2}));

    return(shapes);
  }
    
  public String getLabel() { return(mLabel); }
  
  public int getLowestRow(int index) { return(mConfigs.get(index).bottom); }
  
  public int[] getParts(int index) { return(mConfigs.get(index).parts); }
  
  public int getNumConfigs() { return(mNumConfigs); }
  
  public int getColor() { return(mColor); }
    
  enum ShapeConfig {
    I1(2,new int[] {9,10,11,12}),
    I2(0,new int[] {3,7,11,15}),
    J1(1,new int[] {8,10,11,12}),
    J2(1,new int[] {6,7,11,15}),
    J3(2,new int[] {10,11,12,14}),
    J4(1,new int[] {7,11,15,16}),
    L1(1,new int[] {6,10,11,12}),
    L2(1,new int[] {7,11,14,15}),
    L3(2,new int[] {10,11,12,16}),
    L4(1,new int[] {7,8,11,15}),
    O1(1,new int[] {6,7,10,11}),
    S1(1,new int[] {6,7,11,12}),
    S2(1,new int[] {8,11,12,15}),
    T1(1,new int[] {7,10,11,12}),
    T2(1,new int[] {7,10,11,15}),
    T3(2,new int[] {10,11,12,15}),
    T4(1,new int[] {7,11,12,15}),
    Z1(1,new int[] {7,8,10,11}),
    Z2(1,new int[] {7,11,12,16});
        
    private final int bottom;
    private final int[] parts;
    
    ShapeConfig(int bottom, int[] parts) {
      this.bottom = bottom;
      this.parts = parts;
    }
    
    public int getBottom() { return bottom; }
    public int[] getPieces() { return parts; }
    
  }
    
}