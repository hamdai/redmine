/**
 * Guitar pickup plotting applet
 * Copyright 1999-2001 J. Donald Tillman, All rights reserved.
 *
 * An applet that draws the frequency response of guitar pickups position
 * along a string.  Details at:
 *   http://www.till.com/articles/PickupResponseDemo
 *
 * Everything here is intentionally written in JDK1.0 to be compatible 
 * with Netscape 4.x browsers on the Macintosh.
 *
 * Draws guitar display:
 *   inch ruler, fret ruler, guitar neck, inlays, nut, bridge, 
 *   mouseable string, mouseable pickups.
 * Dynamically updated frequency response plot:
 *   bar showing string frequencies across the fingerboard
 *   line showing open string frequency.
 * Control panel for guitar physical parameters: 
 *   open string freq, frets, scale length.
 * Pickup control panel heading.
 * Pickup control panels for each pickup:
 *   position, width, level, polarity, remove button.
 */

package com.till.pickupplot;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

public class PickupPlot extends Applet {
    static Color BAD_INPUT_COLOR = new Color(255, 63, 63);
    GuitarModel guitarModel;
    GuitarDisplay guitarDisplay;
    ResponsePlot responsePlot;
    ControlPanel controlPanel;
    Font smallFont;
    public void init() {
	smallFont = new Font(getFont().getName(), Font.PLAIN, 9);
	setLayout(null);
	setBackground(Color.white);
	guitarModel = new GuitarModel();
	guitarDisplay = new GuitarDisplay(this, guitarModel);
	add(guitarDisplay);
	responsePlot = new ResponsePlot();
	add(responsePlot);
	controlPanel = new ControlPanel(this);
	add(controlPanel);
	add(new PickupHeading());
	addPickup();
    }

    /*
     * Redraw the guitar display and replot.
     */
    public void updateDisplay() {
	guitarDisplay.repaint();
	responsePlot.setBar(guitarModel.stringOpenFreq, guitarModel.fretCount);
	responsePlot.setLineCursor(guitarModel.fretFreq());
	generatePlot(); 
    }

    /*
     * Actually draw the plot.
     */
    void generatePlot() {
	responsePlot.clearPlot();
    	int plotPointCount = responsePlot.getPlotPointCount();
    	double freq = responsePlot.fMin;
    	double r = Math.exp(Math.log(responsePlot.fMax / responsePlot.fMin) / (plotPointCount - 1));
    	for (int i = 0; i < plotPointCount; i++) {
	    responsePlot.setPlotPoint(i, freq, guitarModel.responseAt(freq));
	    freq *= r;
    	}
    	responsePlot.repaint();
    }

    void addPickup() {
    	Pickup pickup = guitarModel.addPickup();
    	PickupControlPanel pcp = new PickupControlPanel(this, pickup);
	pcp.setFont(smallFont);
	pickup.controlPanel = pcp;
    	add(pcp);
    	validate();
	pickup.updateControlPanel();
    	updateDisplay();
    }

    void removePickup(Pickup pickup, PickupControlPanel pcp) {
    	guitarModel.removePickup(pickup);
    	remove(pcp);
    	validate();
    	updateDisplay();
	repaint();
    }

    /**
     * Cheezy layout routine.  First place the GuitarDisplay, the 
     * ResponsePlot and the ControlPanel.  Then stack of the 
     * PickupHeading and the various PickupControlPanels.
     */
    public void layout() {
	Dimension d = size();
	int width = d.width;
	int height = d.height;
	Dimension ps;
	int y = 0;
	int x = 5;
	int childCount = countComponents();
	for (int i = 0; i < childCount; i++) {
	    Component c = getComponent(i);
	    if ((c == guitarDisplay) || (c == responsePlot)) {
		ps = c.preferredSize();
		c.reshape(x, y, ps.width, ps.height);
		y += ps.height + 5;
	    } else if (c == controlPanel) {
		ps = c.preferredSize();
		c.reshape(x, y + 10, ps.width, ps.height);
		x += ps.width + 5;
	    } else {
		ps = c.preferredSize();
		c.reshape(x, y, ps.width, ps.height);
		y += ps.height + 2;
	    }
	}
    }

    /**
     * Control panel sets some physical parameters of the guitar model.
     * The scale length, the string open frequency, and has a button to 
     * add new pickups.
     *
     */
    class ControlPanel extends Panel {
	TextField fretCountField;
	TextField scaleLengthField;
	TextField stringOpenFreqField;
	Button addPickupButton;
	
	public ControlPanel(PickupPlot pickupPlot) {
	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    setLayout(gridbag);
		
	    Label fretCountLabel = new Label("Number of frets", Label.LEFT);
	    fretCountLabel.setFont(pickupPlot.smallFont);
	    c.anchor = GridBagConstraints.WEST;
	    gridbag.setConstraints(fretCountLabel, c);
	    add(fretCountLabel);
		
	    fretCountField = new TextField("" + guitarModel.fretCount, 5);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(fretCountField, c);
	    add(fretCountField);
		
	    Label openStringLabel = new Label("Open string frequency (Hz)", Label.LEFT);
	    openStringLabel.setFont(pickupPlot.smallFont);
	    c.gridwidth = 1;
	    gridbag.setConstraints(openStringLabel, c);
	    add(openStringLabel);
		
	    stringOpenFreqField = new TextField("" + guitarModel.stringOpenFreq, 5);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(stringOpenFreqField, c);
	    add(stringOpenFreqField);
		
	    Label scaleLengthLabel = new Label("Scale length (inches)", Label.LEFT);
	    scaleLengthLabel.setFont(pickupPlot.smallFont);
	    c.gridwidth = 1;
	    gridbag.setConstraints(scaleLengthLabel, c);
	    add(scaleLengthLabel);
		
	    scaleLengthField = new TextField("" + guitarModel.scaleLength, 5);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    gridbag.setConstraints(scaleLengthField, c);
	    add(scaleLengthField);
		
	    addPickupButton = new Button("Add pickup");
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.EAST;
	    gridbag.setConstraints(addPickupButton, c);
	    add(addPickupButton);
	}

	public boolean keyUp(Event evt, int key) {
	    Object source = evt.target;
	    if (source == fretCountField) {
		try {
		    // limit frets to 0..60
		    int value = Integer.valueOf(fretCountField.getText()).intValue();
		    if ((0 <= value) && (value <= 60)) {
			fretCountField.setForeground(Color.black);
			guitarModel.fretCount = value;
			guitarDisplay.updateBackground();
			updateDisplay();
		    } else {
			fretCountField.setForeground(BAD_INPUT_COLOR);
		    }
		} catch (NumberFormatException nfe) {
		    fretCountField.setForeground(BAD_INPUT_COLOR);
		}
	    } else if (source == stringOpenFreqField) {
		try {
		    // limit string open freq to 20..10K
		    double value = Double.valueOf(stringOpenFreqField.getText()).doubleValue();
		    if ((20.0 <= value) && (value <= 10.0e3)) {
			stringOpenFreqField.setForeground(Color.black);
			guitarModel.stringOpenFreq = value;
			updateDisplay();
		    } else {
			stringOpenFreqField.setForeground(BAD_INPUT_COLOR);
		    }
		} catch (NumberFormatException nfe) {
		    stringOpenFreqField.setForeground(BAD_INPUT_COLOR);
		}
	    } else if (source == scaleLengthField) {
		try {
		    // limit scale length to 10..100
		    double value = Double.valueOf(scaleLengthField.getText()).doubleValue();
		    if ((10.0 <= value) && (value <= 100.0)) {
			scaleLengthField.setForeground(Color.black);
			guitarModel.scaleLength = value;
			guitarDisplay.updateBackground();
			updateDisplay();
		    } else {
			scaleLengthField.setForeground(BAD_INPUT_COLOR);
		    }
		} catch (NumberFormatException nfe) {
		    scaleLengthField.setForeground(BAD_INPUT_COLOR);
		}
	    } else {
		return false;
	    }
	    return true;
	}

	public boolean action(Event evt, Object what) {
	    Object source = evt.target;
	    if (source == addPickupButton) {
		addPickup();
	    } else {
		return false;
	    }
	    return true;
	}
    }
}

class GuitarModel {
    double stringOpenFreq = 110.0;
    double scaleLength = 25.5;
    int fretCount = 24;
    int playedFret = 0;
    Vector pickups = new Vector();

    public GuitarModel() {
    }
	
    Pickup getPickup(int i) {
	return (Pickup) pickups.elementAt(i);
    }
	
    Pickup addPickup() {
	Pickup newPickup = new Pickup(pickups.size() + 1);
	newPickup.position = bestLocationForANewPickup();
	pickups.addElement(newPickup);
	return newPickup;
    }
	
    double bestLocationForANewPickup() {
	int pickupCount = pickups.size();
	if (0 == pickupCount) {
	    // First pickup?  At the end of the neck.
	    return fretPosition(fretCount) - 1.0;
	} 
	// Where's the rightmost pickup?
	int rightMost = 0;
	double minX = fretPosition(fretCount) - 1.0;
	for (int i = 0; i < pickupCount; i++) {
	    Pickup p = getPickup(i);
	    double rightSide = p.position - 0.5 * p.width;
	    if (rightSide < minX) {
		minX = rightSide;
		rightMost = i;
	    }
	}
	return (1.5 < minX) ? (minX - 1.0) : 1.0;
    }
	    

    void removePickup(Pickup pickup) {
	int i = pickups.indexOf(pickup);
	if (0 <= i) {
	    pickups.removeElementAt(i);
	    // renumber
	    while (i < pickups.size()) {
		pickup = getPickup(i);
		pickup.number = ++i;
		pickup.updateControlPanel();
	    }
	}
    }

    /**
     * Return the number of the pickup at this location.
     * or -1 if none found
     */
    int pickupAt(double xFromBridge) {
	for (int i = pickups.size() - 1; 0 <= i; i--) {
	    if (getPickup(i).isHere(xFromBridge)) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Compute the response of all the pickups at this point.
     */
    double responseAt(double freq) {
	double radRelFreq =  freq * Math.PI / (scaleLength * stringOpenFreq);
	int pickupCount = pickups.size();
	double a = 0.0;
    	for (int i = 0; i < pickupCount; i++) {
	    a += getPickup(i).responseAt(radRelFreq);
	}
	return a;
    }
	
    /**
     * Returns the distance from the bridge to this fret.
     */
    double fretPosition(int fret) {
	return scaleLength * Math.pow(0.5, (double) fret / 12.0);
    }

    double fretFreq() {
	return stringOpenFreq * Math.pow(2.0, (((double) playedFret) / 12.0));
    }
}

class Pickup {
    double position = 1.0;
    double width = 1.0;
    int polarity = +1;
    double levelDB = 0.0;
    double level = 1.0;
    double height = 0.75;
    double depth = 0.5;
    int number = 0;
    Color color = null;
    PickupControlPanel controlPanel;
	
    /**
     * Make a new pickup.
     * The number is expected to start from 1
     * A random color is assigned.
     */
    public Pickup(int number) {
	this.number = number;
	// give it a random color
	color = new Color(128 + (int) (128.0 * Math.random()),
			  128 + (int) (128.0 * Math.random()),
			  128 + (int) (128.0 * Math.random()));
    }
	
    void setLevelDB(double newLevelDB) {
	levelDB = newLevelDB;
	level = Math.pow(10.0, levelDB / 20.0);
    }
	
    /**
     * radrelfreq the radian relative frequency:
     *   Fstring pi / (Lscale Fopen)
     */
    double responseAt(double radRelFreq) {
	double widthPart = 1.0;
	if (0.0 != width) {
	    double widthTemp = 0.5 * width * radRelFreq;
	    widthPart = Math.sin(widthTemp) / widthTemp;
	}
	return polarity * level * Math.sin(position * radRelFreq) * widthPart;
    }
	
    /**
     * When you make a change to the pickup call this to tell the 
     * control panel to reflect the change.
     */
    void updateControlPanel() {
	controlPanel.update();
    }
	
    /**
     * Is this pickup located here?
     * xFromBridge is the distance in inches from the bridge.
     * yBelow is the distance below the pickup (down is negative).
     */
    boolean isHere(double xFromBridge) {
	return ((position - 0.5 * width) <= xFromBridge) && 
	    (xFromBridge <= (position + 0.5 * width));
    }
}

/*
 * Displays the guitar, string, rulers, pickups.
 */
class GuitarDisplay extends Canvas {
    final static int MOUSE_ON_STRING = -1;
    final static int MOUSE_ON_NONE = -2;
    static long DOT_MARKERS = 010001000112511250L;
    static DecimalFormat df23 = new DecimalFormat("##.###");
    static int RULER_Y = 15;
    static int STRING_AMP = 8;
    static int STRING_Y = 50;
    static int NECK_TOP = STRING_Y + 8;
    static int PICKUP_TOP = NECK_TOP + 3;
    static Color RULER_COLOR = new Color(63, 63, 63);
    static Color BRIDGE_COLOR = new Color(31, 31, 31);
    static Color FINGERBOARD_COLOR = Color.black;
    static Color INLAY_COLOR = new Color(255, 255, 127);
    static Color NUT_COLOR = new Color(231, 231, 231);
    static Color NUT_OUTLINE_COLOR = new Color(63, 63, 63);
    static Color VIB_STRING_COLOR = new Color(220, 220, 255);
    static Color VIB_STRING_OUTLINE_COLOR = new Color(112, 112, 112);
    static String FRET_LEGEND = "Fret number";
    static String INCH_LEGEND = "Inches from bridge";
    static double fingerboardThickness	= 0.25;
    static double left = 0.15;
    static double right = 0.95;
    int polyX[] = new int[33];
    int polyY[] = new int[33];
    double mousePressedPosition;
    int mousePressedX;
    int mouseOn;		// for moving pickups
    int nutX;
    int bridgeX;
    double ppi; 		// pixels per inch
    Dimension prefSize = new Dimension(600, 100);
    Image offscreen;
    PickupPlot pickupPlot;
    GuitarModel guitarModel;

    public GuitarDisplay(PickupPlot pickupPlot, GuitarModel guitarModel) {
	this.pickupPlot = pickupPlot;
	this.guitarModel = guitarModel;
    }

    public Dimension preferredSize() {
	return prefSize;
    }

    public void reshape(int x, int y, int width, int height) {
	super.reshape(x, y, width, height);
	nutX = (int) Math.round(left * width);
	bridgeX = (int) Math.round(right * width);
	if ((null == offscreen) || 
	    (offscreen.getWidth(null) != width) || 
	    (offscreen.getHeight(null) != height)) {
	    offscreen = createImage(width, height);
	}
	updateBackground();
    }

    /*
     * The rulers and the guitar drawing don't change very much, so they
     * are placed on a background offscreen bitmap.  Call this to update 
     * them.
     */
    public void updateBackground() {
	ppi = (float) (bridgeX - nutX) / guitarModel.scaleLength;
	Graphics og = offscreen.getGraphics();
	super.paint(og);
	drawRuler(og);
	drawGuitar(og);
    }

    public void paint(Graphics g) {
	g.drawImage(offscreen, 0, 0, null);
	drawPickups(g);
	drawVibString(g);
    }

    /**
     * Draw the inches ruler above the neck.
     */
    void drawRuler(Graphics g) {
	g.setFont(pickupPlot.smallFont);
	FontMetrics fm = g.getFontMetrics();
	int tickLength = 3;
	int textUp = 3;
	int textHeight = fm.getHeight();
	String text;
	int textWidth;
	int xPos;
	g.setColor(RULER_COLOR);
	g.drawLine(nutX, RULER_Y, bridgeX, RULER_Y);
	// draw inch ruler
	g.drawString(INCH_LEGEND, nutX - fm.stringWidth(INCH_LEGEND) -4, RULER_Y);
	for (int d = 0; d <= guitarModel.scaleLength; d++) {
	    xPos = bridgeX - (int) Math.round(d * ppi);
	    g.drawLine(xPos, RULER_Y - tickLength, xPos, RULER_Y);
	    if (0 == d % 2) {
		text = "" + d;
		textWidth = fm.stringWidth(text);
		g.drawString(text, xPos - textWidth / 2, RULER_Y - tickLength);
	    }
	}
		
	// draw fret ruler
	g.drawString(FRET_LEGEND, nutX - fm.stringWidth(FRET_LEGEND) - 4, RULER_Y + textHeight);
	for (int f = 0; f <= guitarModel.fretCount; f++) {
	    xPos = fretX(f);
	    g.drawLine(xPos, RULER_Y, xPos, RULER_Y + tickLength);
	    // fret markers
	    if (0 != (1L & (DOT_MARKERS >>> f))) {
		text = "" + f;
		textWidth = fm.stringWidth(text);
		g.drawString(text, xPos - textWidth / 2, RULER_Y + textHeight);
	    }
	}
			
    }

    void drawGuitar(Graphics g) {
	int bodyDescent = (int) Math.round(0.375 * ppi);
		
	// draw neck & fingerboard
	int fingerboardThicknessP = (int) Math.round(fingerboardThickness * ppi);
	int neckRight = fretX(guitarModel.fretCount);
		
	g.setColor(FINGERBOARD_COLOR);
	g.fillRect(nutX, NECK_TOP, neckRight - nutX, fingerboardThicknessP);
		
	// draw frets
	int xPos;
	int xPosPrev = nutX;
	for (int f = 0; f <= guitarModel.fretCount; f++) {
	    xPos = fretX(f);
	    g.setColor(Color.white);
	    g.drawLine(xPos, NECK_TOP, xPos, NECK_TOP + fingerboardThicknessP);
	    // fret markers
	    if (0 != (1L & (DOT_MARKERS >>> f))) {
		g.setColor((0 == (f % 12)) ? INLAY_COLOR : Color.white);
		g.fillOval((int) Math.round((xPos + xPosPrev) / 2.0), 
			   NECK_TOP, 
			   fingerboardThicknessP, fingerboardThicknessP);
	    }
	    xPosPrev = xPos - fingerboardThicknessP;
	}
		
	// draw nut
	int nutHalfWidth = 5;
	polyX[0] = nutX;
	polyX[1] = nutX + nutHalfWidth;
	polyX[2] = nutX - nutHalfWidth;
	polyY[0] = STRING_Y;
	polyY[1] = NECK_TOP;
	polyY[2] = NECK_TOP;
	g.setColor(NUT_COLOR);
	g.fillPolygon(polyX, polyY, 3);
	g.setColor(NUT_OUTLINE_COLOR);
	g.drawPolygon(polyX, polyY, 3);
		
	// draw bridge
	int length = bridgeX - nutX;
	polyX[0] += length;
	polyX[1] += length;
	polyX[2] += length;
	g.setColor(BRIDGE_COLOR);
	g.fillPolygon(polyX, polyY, 3);
	int bridgeWidth = 10;
	g.fillRect(bridgeX - bridgeWidth / 2, NECK_TOP, bridgeWidth, bodyDescent);
    }

    void drawPickups(Graphics g) {
	int pickupCount = guitarModel.pickups.size();
	for (int i = 0; i < pickupCount; i++) {
	    drawPickup(g, guitarModel.getPickup(i));
	}
    }

    void drawPickup(Graphics g, Pickup pickup) {
		
	int xP = bridgeX - (int) Math.round(ppi * (pickup.position + 0.5 * pickup.width));
	int yP = PICKUP_TOP;
	int widthP = (int) Math.round(pickup.width * ppi);
	int heightP = (int) Math.round(pickup.height * ppi);
		
	g.setColor(pickup.color);
	g.fillRoundRect(xP, yP, widthP, heightP, 10, 10);
	g.setColor(Color.black);
	g.drawRoundRect(xP, yP, widthP, heightP, 10, 10);
	FontMetrics fm = g.getFontMetrics(getFont());
	String text = df23.format(pickup.position);
	g.drawString(text,
		     xP + (widthP - fm.stringWidth(text)) / 2, 
		     yP + heightP + fm.getHeight());
    }
	
    /**
     * n is the number of points computed.
     * the polygon is 2 * n + 1 ponts
     */
    void drawVibString(Graphics g) {
	int leftX = fretX(guitarModel.playedFret);
	int n = polyX.length / 2;
	int last = 2 * n;
	double incX = (bridgeX - leftX) / (double) n;
	double incY = Math.PI / n;
	for (int i = 0; i <= n; i++) {
	    double dist = STRING_AMP * Math.sin(i *incY);
	    polyX[i] = leftX + (int) Math.round(i * incX);
	    polyY[i] = (int) Math.round(STRING_Y - dist);
	    polyX[last - i] = polyX[i];
	    polyY[last - i] = (int) Math.round(STRING_Y + dist);
	}
	g.setColor(VIB_STRING_COLOR);
	g.fillPolygon(polyX, polyY, last + 1);
	g.setColor(VIB_STRING_OUTLINE_COLOR);
	g.drawPolygon(polyX, polyY, last + 1);
	g.drawLine(nutX, STRING_Y, bridgeX, STRING_Y);
    }

    public boolean mouseDown(Event evt, int x, int y) {
	int onPickup = -1;
	if (PICKUP_TOP <= y) {
	    double xInches = (bridgeX - x) / ppi;
	    onPickup = guitarModel.pickupAt(xInches);
	}
	if (0 <= onPickup) {
	    mouseOn = onPickup;
	    mousePressedX = x;
	    mousePressedPosition = guitarModel.getPickup(onPickup).position;
	} else if (onString(x, y)) {
	    mouseOn = MOUSE_ON_STRING;
	} else {
	    mouseOn = MOUSE_ON_NONE;
	    return super.mouseDown(evt, x, y);
	}
	return true;
    }

    /**
     * Is this mouse point in the string region.
     */
    boolean onString(int x, int y) {
	return ((STRING_Y - STRING_AMP) < y) && (y < (STRING_Y + STRING_AMP));
    }
    
    public boolean mouseDrag(Event evt, int x, int y) {
	if (0 <= mouseOn) {
	    Pickup p = guitarModel.getPickup(mouseOn);
	    p.position = mousePressedPosition - (x - mousePressedX) / ppi;
	    p.updateControlPanel();
	    pickupPlot.updateDisplay();
	} else if (MOUSE_ON_STRING == mouseOn) {
	    guitarModel.playedFret = xToFret(x);
	    pickupPlot.responsePlot.setLineCursor(guitarModel.fretFreq());
	    pickupPlot.responsePlot.repaint();
	    //pickupPlot.updateDisplay();****
	    repaint(0, STRING_Y - STRING_AMP, size().width, 2 * STRING_AMP);
	} else {
	    return super.mouseDrag(evt, x, y);
	} 
	return true;
    }

    int xToFret(int x) {
	double ratio = (double) (bridgeX - nutX) / (double) (bridgeX - x);
	return (int) Math.round((12.0 / Math.log(2.0)) * Math.log(ratio));
    }
	
    /**
     * return the pixel x value of this fret.
     */
    int fretX(int fret) {
	return bridgeX - (int) Math.round(ppi * guitarModel.fretPosition(fret));
    }
}

/**
 * The heading above the pickup control panels.
 */
class PickupHeading extends Panel {
    Label pickupLabel 	= new Label("Pickup", Label.CENTER);
    Label pickupLabel2	= new Label("number", Label.CENTER);
    Label positionLabel	= new Label("Position", Label.CENTER);
    Label positionLabel2 = new Label("(inches)", Label.CENTER);
    Label widthLabel = new Label("Width", Label.CENTER);
    Label widthLabel2 = new Label("(inches)", Label.CENTER);
    Label levelLabel = new Label("Level", Label.CENTER);
    Label levelLabel2 = new Label("(dB)", Label.CENTER);
    Label levelLabel3 = new Label("Level", Label.CENTER);
    Label levelLabel4 = new Label("control", Label.CENTER);
    Label polarityLabel	= new Label("Polarity", Label.CENTER);
    static Dimension preferredSize;
	
    public static int NUMBER_WIDTH = 48;
    public static int POSITION_WIDTH = 50;
    public static int WIDTH_WIDTH = 50;
    public static int LEVEL_VALUE_WIDTH	= 50;
    public static int LEVEL_CONTROL_WIDTH = 100;
    public static int POLARITY_WIDTH = 72;
    public static int REMOVE_WIDTH = 50;
    public static int HEIGHT = 28;
    public static int HEIGHT1 = 14;
    public static int ROW2 = 14;
	
    public PickupHeading() {
	setLayout(null);
	int x = 0;
	pickupLabel.reshape(x, 0, NUMBER_WIDTH, HEIGHT1);
	pickupLabel2.reshape(x, ROW2, NUMBER_WIDTH, HEIGHT1);
	x += NUMBER_WIDTH;
	positionLabel.reshape(x, 0, POSITION_WIDTH, HEIGHT1);
	positionLabel2.reshape(x, ROW2, POSITION_WIDTH, HEIGHT1);
	x += POSITION_WIDTH;
	widthLabel.reshape(x, 0, WIDTH_WIDTH, HEIGHT1);
	widthLabel2.reshape(x, ROW2, WIDTH_WIDTH, HEIGHT1);
	x += WIDTH_WIDTH;
	levelLabel.reshape(x, 0, LEVEL_VALUE_WIDTH, HEIGHT1);
	levelLabel2.reshape(x, ROW2, LEVEL_VALUE_WIDTH, HEIGHT1);
	x += LEVEL_VALUE_WIDTH;
	levelLabel3.reshape(x, 0, LEVEL_CONTROL_WIDTH, HEIGHT1);
	levelLabel4.reshape(x, ROW2, LEVEL_CONTROL_WIDTH, HEIGHT1);
	x += LEVEL_CONTROL_WIDTH;
	polarityLabel.reshape(x, 0, POLARITY_WIDTH, HEIGHT);
	x += POLARITY_WIDTH;
	x += REMOVE_WIDTH;
		
	if (null == preferredSize) {
	    preferredSize = new Dimension(x, HEIGHT + 2);
	}
		
	add(pickupLabel);
	add(pickupLabel2);
	add(positionLabel);
	add(positionLabel2);
	add(widthLabel);
	add(widthLabel2);
	add(levelLabel);
	add(levelLabel2);
	add(levelLabel3);
	add(levelLabel4);
	add(polarityLabel);
    }	
		
    public Dimension preferredSize() {
	return preferredSize;
    }
}

/**
 * Control panel for an individual pickup.
 * Provides control of position, width, level, polarity, remove command.
 */
class PickupControlPanel extends Panel {
    static DecimalFormat df23 = new DecimalFormat("#0.###");
    Dimension preferredSize = new Dimension(PickupHeading.preferredSize.width, 22);
	
    Label numberField = new Label("", Label.CENTER);
    TextField positionField = new TextField();
    TextField widthField = new TextField();
    TextField levelField = new TextField();
    Scrollbar levelControl = new Scrollbar(Scrollbar.HORIZONTAL, 0, 10, -40, 10);
    Choice polarityMenu	= new Choice();
    Button removeButton	= new Button("remove");
    Pickup pickup;
    PickupPlot pickupPlot;
	
    public PickupControlPanel(PickupPlot pickupPlot, Pickup pickup) {
	this.pickupPlot = pickupPlot;
	this.pickup = pickup;
		
	setLayout(null);
	setBackground(pickup.color);
		
	polarityMenu.addItem("positive");
	polarityMenu.addItem("negative");
		
	add(numberField);
	add(positionField);
	add(widthField);
	add(levelField);
	add(levelControl);
	add(polarityMenu);
	add(removeButton);
	validate();
    }
	
    public void layout () {
	int compCount = countComponents();
	int hMax = 0;
	for (int i = 0; i < compCount; i++) {
	    int h = getComponent(i).preferredSize().height;
	    if (hMax < h) {
		hMax = h;
	    }
	}
	int x = 0;
		
	numberField.reshape(x, 0, PickupHeading.NUMBER_WIDTH, hMax);
	x += PickupHeading.NUMBER_WIDTH;
	positionField.reshape(x, 0, PickupHeading.POSITION_WIDTH, hMax);
	x += PickupHeading.POSITION_WIDTH;
	widthField.reshape(x, 0, PickupHeading.WIDTH_WIDTH, hMax);
	x += PickupHeading.WIDTH_WIDTH;
	levelField.reshape(x, 0, PickupHeading.LEVEL_VALUE_WIDTH, hMax);
	x += PickupHeading.LEVEL_VALUE_WIDTH;
	
	// scrollbars are funny
	int sbHeight = levelControl.preferredSize().height;
	levelControl.reshape(x, (hMax - sbHeight)/2, PickupHeading.LEVEL_CONTROL_WIDTH, sbHeight);
	x += PickupHeading.LEVEL_CONTROL_WIDTH;

	// menubars too
	int pmHeight = polarityMenu.preferredSize().height;
	polarityMenu.reshape(x, (hMax - pmHeight)/2, PickupHeading.POLARITY_WIDTH, pmHeight);
	x += PickupHeading.POLARITY_WIDTH;

	removeButton.reshape(x, 0, PickupHeading.REMOVE_WIDTH, hMax);
	x += PickupHeading.REMOVE_WIDTH;
	//preferredSize.width = x;
	//preferredSize.height = hMax;
    }

    public Dimension preferredSize() {
	return preferredSize;
    }

    public boolean handleEvent(Event evt) {
	if ((Event.SCROLL_LINE_UP   == evt.id) ||
	    (Event.SCROLL_LINE_DOWN == evt.id) ||
	    (Event.SCROLL_PAGE_UP   == evt.id) ||
	    (Event.SCROLL_PAGE_DOWN == evt.id) ||
	    (Event.SCROLL_ABSOLUTE  == evt.id) ||
	    (Event.SCROLL_BEGIN     == evt.id) ||
	    (Event.SCROLL_END       == evt.id)) {
	    int val = levelControl.getValue();
	    pickup.setLevelDB(val);
	    levelField.setText("" + val);
	    pickupPlot.updateDisplay();
	    return true;
	}
	return super.handleEvent(evt);
    }

    public boolean action(Event evt, Object what) {
	Object source = evt.target;
	if (source == positionField) {
	    try {
		pickup.position = Double.valueOf(positionField.getText()).doubleValue();
		pickupPlot.updateDisplay();
	    } catch (NumberFormatException nfe) {
	    } 
	} else if (source == widthField) {
	    try {
		pickup.width = Double.valueOf(widthField.getText()).doubleValue();
		pickupPlot.updateDisplay();
	    } catch (NumberFormatException nfe) {
	    } 
	} else if (source == levelField) {
	    try {
		double val = Double.valueOf(levelField.getText()).doubleValue();
		levelControl.setValue((int) Math.round(val));
		pickup.setLevelDB(val);
		pickupPlot.updateDisplay();
	    } catch (NumberFormatException nfe) {
	    } 
	} else if (source == polarityMenu) {
	    pickup.polarity = (0 == polarityMenu.getSelectedIndex()) ? +1 : -1;
	    pickupPlot.updateDisplay();
	} else if (source == removeButton) {
	    pickupPlot.removePickup(pickup, this);
	} else {
	    return false;
	}
	return true;
    }

    /**
     * Reflect changes to the pickup
     */
    public void update() {
    	numberField.setText("" + pickup.number);
    	positionField.setText(df23.format(pickup.position));
    	widthField.setText(df23.format(pickup.width));
    	levelField.setText(df23.format(pickup.levelDB));
    }
}

class ResponsePlot extends Canvas {
    static Color BACKGROUND_COLOR = Color.white;
    static Color AXIS_COLOR = new Color(63, 63, 63);
    static Color PLOT_COLOR_FILL = new Color(220, 220, 160);
    static Color PLOT_COLOR_OUTLINE = new Color(31, 63, 63);
    static Color LINE_CURSOR_COLOR = new Color(255, 255, 0);
    static Color NOTE_BAR_COLOR = new Color(255, 128, 128);
    final static double oolog10 = 1.0 / Math.log(10.0);
    static DecimalFormat df1 = new DecimalFormat("#");
	
    Dimension prefSize = new Dimension(512 + 128, 200);
    double dbMax = 10.0;
    double dbMin = -40.0;
    double fMin = 20.0;
    double fMax = 20000.0;
    int plotLeft;
    int plotTop;
    int plotRight;
    int plotBottom;
    int prevY;
    double ppd; 
    double ppdb;
    int unityX;
    int unityY;
    int lineCursorX;
    double barOpenFreq;
    double barDecades;
    Image offscreen;
    Graphics offscreenG;
    double minorTicks[] = {2.0, 5.0};
    int majorTickLength = 5;
    int minorTickLength = 2;
	
    public ResponsePlot() {
    }
	
    public Dimension preferredSize() {
	return prefSize;
    }

    public void reshape(int x, int y, int width, int height) {
	super.reshape(x, y, width, height);
	resetLayout();
	if ((null == offscreen) || 
	    (offscreen.getWidth(null) != width) || 
	    (offscreen.getHeight(null) != height)) {
	    offscreen = createImage(width, height);
	    offscreenG = offscreen.getGraphics();
	    offscreenG.setColor(BACKGROUND_COLOR);
	    offscreenG.fillRect(0, 0, width, height);
	    drawXAxis(offscreenG);
	    drawYAxis(offscreenG);
	}
    }

    void setBar(double openFreq, int fretCount) {
	barOpenFreq = openFreq;
	barDecades = Math.log(2.0) * (((double) fretCount) / 12.0) * oolog10;
    }

    void resetLayout() {
	setBackground(BACKGROUND_COLOR);
	Dimension size = size();
	plotLeft = (int) Math.round(0.1 * size.width);
	plotRight = (int) Math.round(0.9 * size.width);
	plotTop = size.height / 32;
	plotBottom = size.height * 7 / 8;
	ppd = (plotRight - plotLeft) / (Math.log(fMax / fMin) * oolog10);
	unityX = plotLeft - (int) Math.round(Math.log(fMin) * oolog10 * ppd);
	ppdb = (plotTop - plotBottom) / (dbMax - dbMin);
	unityY = plotTop + (int) Math.round((dbMax / (dbMax - dbMin)) * (plotBottom - plotTop));
		
    }

    public void paint(Graphics g) {
	g.drawImage(offscreen, 0, 0, null);
	drawLineCursor(g);
    }

    void drawXAxis(Graphics g) {
	String str;
	g.setColor(AXIS_COLOR);
	g.drawLine(plotLeft, plotBottom + 1, plotRight, plotBottom + 1);
	FontMetrics fm = g.getFontMetrics(getFont());
	int majorTextY = plotBottom + majorTickLength;
	int min = (int) Math.floor(Math.log(fMin) * oolog10);
	int max = (int) Math.ceil(Math.log(fMax) * oolog10);
	int tickX;
		
	for (int i = min; i <= max; i++) {
	    tickX = unityX + (int) Math.round(i * ppd);
	    if (plotLeft <= tickX && tickX <= plotRight) {
		g.drawLine(tickX, plotBottom + 1, tickX, plotBottom + 1 + majorTickLength);
		str = engNotationDecade(i) + " Hz";
		g.drawString(str,
			     tickX - fm.stringWidth(str) / 2,
			     plotBottom + majorTickLength + fm.getHeight());
	    }
	    int minorTickCount = minorTicks.length;
	    for (int j = 0; j < minorTickCount; j++) {
		tickX = unityX + (int) Math.round((i + Math.log(minorTicks[j]) * oolog10) * ppd);
		if (plotLeft <= tickX && tickX <= plotRight) {
		    g.drawLine(tickX, plotBottom + 1, tickX, plotBottom + 1 + minorTickLength);
		    str = df1.format(minorTicks[j]);
		    g.drawString(str,
				 tickX - fm.stringWidth(str) / 2,
				 plotBottom + minorTickLength + fm.getHeight());
		}
	    }
	}
    }

    String engNotationDecade(int log) {
	String val = "??";
	switch (log % 3) {
	case 0: val = "1"; break;
	case 1: val = "10"; break;
	case 2: val = "100"; break;
	}					
	switch (log / 3) {
	case -5: return val + "f";
	case -4: return val + "p";
	case -3: return val + "n";
	case -2: return val + "u";
	case -1: return val + "m";
	case  0: return val;
	case  1: return val + "K";
	case  2: return val + "M";
	case  3: return val + "G";
	}
	return null;
    }

    void drawYAxis(Graphics g) {
	String str;
	FontMetrics fm = g.getFontMetrics();
	g.setColor(AXIS_COLOR);
	g.drawLine(plotLeft - 1, plotTop, plotLeft - 1, plotBottom);
	int dbMinInt = 10 * (int) Math.floor(dbMin * 0.1);
	int dbMaxInt = 10 * (int) Math.ceil(dbMax * 0.1);
	for (int i = dbMinInt; i <= dbMaxInt; i += 10) {
	    int dby = unityY + (int) Math.round(i * ppdb);
	    if (plotTop <= dby && dby <= plotBottom) {
		g.drawLine(plotLeft - 1, dby, plotLeft - 1 - majorTickLength, dby);
		str = (0 == i) ? "0 dB" : String.valueOf(i);
		g.drawString(str,
			     plotLeft - majorTickLength - fm.stringWidth(str) - 2, 
			     dby + fm.getHeight() / 2);
	    }
	    for (int j = 1; j < 10; j ++) {
		dby = unityY + (int) Math.round((i + j) * ppdb);
		if (plotTop <= dby && dby <= plotBottom) {
		    g.drawLine(plotLeft - 1, dby, plotLeft - 1 - minorTickLength, dby);
		}
	    }
	}
    }

    void drawNoteBar(Graphics g) {
	g.setColor(NOTE_BAR_COLOR);
	int x = unityX + (int) Math.round(Math.log(barOpenFreq) * oolog10 * ppd);
	int w = (int) Math.round(barDecades * ppd);
	g.fillRect(x, plotTop + 5, w, 6);
    }
	
    int getPlotPointCount() {
	return plotRight - plotLeft;
    }
	
    void clearPlot() {
	offscreenG.setColor(BACKGROUND_COLOR);
	offscreenG.fillRect(plotLeft, plotTop, plotRight - plotLeft, plotBottom - plotTop);
	drawNoteBar(offscreenG);
    }	

    /*
     * Plot each point.
     * 
     * We do the dB conversion here.
     * These go right to an offscreen array, so it's intended that these go in 
     * strict consecutive order.
     */
    void setPlotPoint(int i, double freq, double amp) {
	int x = plotLeft + i;
	if (amp < 0.0) {
	    amp = -amp;
	}
	double dbAmp = 20.0 * oolog10 * Math.log(amp);
	if ((Double.isNaN(dbAmp)) || (dbAmp < dbMin)) {
	    dbAmp = dbMin;
	}
	if (dbMax < dbAmp) {
	    dbAmp = dbMax;
	}
	int y = unityY + (int) Math.round(dbAmp * ppdb);
	offscreenG.setColor(PLOT_COLOR_FILL);
	offscreenG.drawLine(x, plotBottom, x, y);
	if (0 < i) {
	    offscreenG.setColor(PLOT_COLOR_OUTLINE);
	    offscreenG.drawLine(x - 1, prevY, x, y);
	}
	prevY = y;
    }

    void drawLineCursor(Graphics g) {
	g.setColor(LINE_CURSOR_COLOR);
	g.drawLine(lineCursorX, plotBottom, lineCursorX, plotTop);
    }

    void setLineCursor(double freq) {
	int prev = lineCursorX;
	lineCursorX = unityX + (int) Math.round(Math.log(freq) * oolog10 * ppd);
    }
}

