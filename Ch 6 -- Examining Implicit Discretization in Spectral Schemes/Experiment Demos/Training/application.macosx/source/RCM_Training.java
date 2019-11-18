import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import org.gicentre.utils.gui.*; 
import blobDetection.*; 
import java.io.File; 
import java.io.BufferedWriter; 
import java.io.FileWriter; 
import java.io.IOException; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class RCM_Training extends PApplet {









PFont myFont;
TextInput input;
Button submit;

TextPopup popup;

Table scheme;
TableRow record;
int record_number;

String out_file;
BufferedWriter writer;

ArrayList<Delimeter> seg_positions;
Delimeter current; 

PImage img, map, complex, data;
int t_last_click;
int threshold = 300;

float factor; //complex data scale factor

int block;
int color_block;
int data_mode_index;
int data_mode;
int question;

int option; // different complex examples

int participant;
String uuid;
boolean finished;

StringList cmap_image;

String Q1 = "This study is examining how how individuals distinguish color categories in color maps commonly used for data visualization. A color-category is a continuous subset of the of the color map for which you consider colors within the subset more similar to one another than colors outside that subset. In the above image, for example, one color category might be the \u201cred\u201d or \u201creddish\u201d colors.\n\nIn the study, you will be provided images similar to the ones in this training session and asked to make judgements about color categories. You will then be asked to complete a survey asking about the reasoning behind your responses.";
String Q2 = "In this study you will be required to type answers using the numeric keys on the keyboard. Please type the number twelve using the numeric keys, then either hit \"Enter\" or click \"Next\".";
String Q3 = "You will also be asked to use a mouse to delineate the boundaries between color categories. Please click anywhere on the above image to create a boundary line.";
String Q4 = "Boundaries can be highlighted by mousing over them and moved by clicking and dragging. Please familiarize yourself with these interaction mechanisms by adding more boundary lines and dragging them around.";
String Q5 = "Boundary lines can be removed by double clicking them. Please remove at least one boundary now.";
String Q6 = "There is a portion of the image that is never covered by the boundary lines (in this case the top half of the image). This allows you to use the end of the boundary line to see the exact color where you are placing the boundary. Note that boundary lines can still be selected in the uncovered portions of the image, however. Try selecting and dragging a boundary from the top half of the image.";
String Q7 = "Here is a different data set. Practice placing, dragging and deleting boundary lines. When you are done, delete all the boundaries you placed, then click next.";
String Q8 = "Here is a real-world data set. With a complex data set like this, the subset of the image never covered by the boundary line may be arbitrary. Create and drag a boundary line over the image to discover what subset of this data is never contoured. Delete the contour when you are done.";
String Q9 = "\nYou should now be prepped for everything you will encounter in the study! Please inform the RA that you have completed the training.";
boolean q_passed, q_passed_alt;


public void setup() {
  
  //size(512, 662);

  // create submit button
  submit = new Button(width-100, height-40, "Next");

  // create text input
  myFont = createFont("Lucida Grande", 16, true);
  input = new TextInput(this, myFont, 16);

  // create popup   
  popup = new TextPopup(this, 100, 100);
  
  // finished flag
  finished = false;

  data = loadImage("brain.png"); // heightmap (about 250x250px)
  complex = loadImage("brain.png");
  data.loadPixels();
  complex.resize(width, 0);
  complex.loadPixels();
  factor = PApplet.parseFloat(width)/data.width;

  // initalize image for display
  img = createImage(width, width, RGB);
  img.loadPixels();

   //grab new color-map 
   map = loadImage("rainbow_map.png");
   map.loadPixels();

  // key up first block
  data_mode = 1;
  question = 6;
  updateDisplay();

  // initialize delimeters
  seg_positions = new ArrayList<Delimeter>();
  current = null;
  t_last_click = millis();

  // set default drawing values
  stroke(10); // stroke color
  noFill();
  ellipseMode(RADIUS); // ellipse mode
  
  //question 1 read time
  //submit.timedInactive(15000);
  
  q_passed = false;
}

public void next() {
  if(!submit.active) return;
  
  question++;
  q_passed = false;
  
  if (question == 4) submit.timedInactive(10000);
  else if (question == 7){
    nextDataSet();
    submit.timedInactive(10000);
  }
  else if (question == 8){
    nextDataSet();
    submit.timedInactive(5000);
  }
  else if (question == 9) finish();
}

public void nextDataSet() {
    // update data mode
    data_mode++;

    // update display
    updateDisplay();
    
    //clear delimeters
    seg_positions.clear();
}

public void finish() {
  popup.clearText();
  popup.addText(Q9);
  popup.setIsActive(true);
  finished = true; 
}

public void updateDisplay() {
  // remove popup clear when update complex example
  popup.clearText();
  popup.setIsActive(false);
  // build stimuli based on current color map and data mode -- note: only works for 1D and 2D case
  if (data_mode < 3) {
    for (int y = 0; y < img.height; y++) {
      for (int x = 0; x < img.width; x++) {
        // for each pixel location get value
        float v = (data_mode == 1) ? PApplet.parseFloat(x)/img.width : max(1-min(sqrt(sq(2*PApplet.parseFloat(x)/img.width - 1) + sq(2*PApplet.parseFloat(y)/img.width - 1)), 1), 0);
        // map color
        img.pixels[y*img.height + x] =  map.pixels[round(v*255)];
      }
    }
    img.updatePixels();
  } else { // complex example
    if (complex.width != img.width) {
      popup.addText("Error with Complex Example: source image is wrong size.");
      popup.setIsActive(true);
    } else {
      for (int y = 0; y < img.height; y++) {
        for (int x = 0; x < img.width; x++) {
          // for each pixel location get value using red channel (src is  grayscale, so could also use G or B)
          int v = (complex.pixels[y*img.height + x] >> 16) & 0xFF; 
          // map color
          img.pixels[y*img.height + x] =  map.pixels[v];
        }
      }
      img.updatePixels();
    }
  }
}

public void draw() {
  background(100);
  // Displays the image at its actual size at point (0,0)
  image(img, 0, 0);

  // Display any delimeters
  stroke(((color_block == 1) ? color(255, 0, 0) : color(10))); // stroke color
  noFill();
  for (Delimeter p : seg_positions) {
    strokeWeight((p.interacting(mouseX, mouseY, img, data, factor) && mouseY < width) ? 1.5f : 1); // bias stroke weight based on whetehr currently interacting
    p.draw(img, data, factor);
  }

  // hide half of delimeters -- MAY NOT WORK FOR COMPLEX CASE
  // define symmetry based on mode
  int ix, iy, iw, ih;
  if (data_mode == 1) {
    ix = 0; 
    iy = 0; 
    iw = img.width; 
    ih = img.height/2;
    copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
  } else if (data_mode == 2) { //(data_mode == 2)
    ix = 0; 
    iy = 0; 
    iw = img.width/2; 
    ih = img.height;
    copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
  } else { // NEED TO CHANGE / UPDATE DEPENDING ON COMPLEX EXAMPLE
    //if (false) {
    //  ix = 0; 
    //  iy = 0; 
    //  iw = img.width; 
    //  ih = 1*img.height/8;
    //  copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
    //  ix = 0; 
    //  iy = 7*img.height/9; 
    //  iw = img.width; 
    //  ih = img.height-iy;
    //  copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
    //} else if (true) {
    ix = 90; 
    iy = 237; 
    iw = img.width/3; 
    ih = img.height/3;
    copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
    //} else if (false) {
    //  ix = img.width/6; 
    //  iy = 0; 
    //  iw = 4*img.width/6; 
    //  ih = img.height/3;
    //  copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
    //}
  }

  //Display the question
  fill(255);
  textSize(13);
  switch(question){
    case(1):
      text(Q1, 50, height - 200, width-100, 170);
      break;
    case(2):
      text(Q2, 50, height - 200, width-100, 170);
      input.draw(50, height - 125);
      break;
    case(3):
      text(Q3, 50, height - 200, width-100, 170);
      break;
    case(4):
      text(Q4, 50, height - 200, width-100, 170);
      break;
    case(5):
      text(Q5, 50, height - 200, width-100, 170);
      break;
    case(6):
      text(Q6, 50, height - 200, width-100, 170);
      break;
    case(7):
      text(Q7, 50, height - 200, width-100, 170);
      break;
    case(8):
      text(Q8, 50, height - 200, width-100, 170);
      break;
    default:
      break;
  }
      
  // button
  boolean disabled = finished;
  disabled = disabled || ((question == 2) && (PApplet.parseInt(input.getText()) != 12));
  disabled = disabled || ((question == 3) && !(seg_positions.size() > 0));
  disabled = disabled || ((question == 4) && !q_passed && !(seg_positions.size() > 1));
  disabled = disabled || ((question == 5) && !q_passed);
  disabled = disabled || ((question == 6) && !q_passed);
  disabled = disabled || (((question == 7) || (question == 8)) && !((seg_positions.size() == 0) && q_passed));
  submit.active = !disabled;
  submit.interacting(mouseX, mouseY);
  submit.draw();

  // pop up
  popup.draw();
}

public void mousePressed() {
  int t_click = millis();
  
  // check for submit click, then check for delimeter interaction
  if (!submit.clicked(mouseX, mouseY) && (mouseY < width)) {
    if (question > 2){
      // if arbitrarily close to existing, grab that
      for (Delimeter p : seg_positions) {
        if (p.interacting(mouseX, mouseY, img, data, factor)) { 
          current = p;
          q_passed_alt = q_passed || ((question == 6) & (mouseY < (width/2)));
          continue;
        }
      }
      // if double click, delete
      if ((current != null) && ((t_click - t_last_click) < threshold)) {
        seg_positions.remove(current);
        current = null;
        if(question == 5) q_passed = true;
      } else if (current == null) { // else generate a new delimeter
        current = new Delimeter(mouseX, mouseY, data_mode, img, data, factor);
        current.dragging = true;
        seg_positions.add(current);
        if ((question == 7) || (question == 8)) q_passed = true;
      } else {
        current.dragging = true; // current != null and not a double click
        if(question == 4) q_passed = true;
        if(question == 6) q_passed = q_passed_alt;
      }
    }
  }

  //regardless update time of last click
  t_last_click = t_click;
}

public void keyPressed() {
  switch (key) {
  case ENTER:
  case RETURN:
    next();
    break;
  case BACKSPACE:
  case DELETE:
    if (question == 2) input.keyPressed();
    break;
  case '0':
  case '1':
  case '2':
  case '3':
  case '4':
  case '5':
  case '6':
  case '7':
  case '8':
  case '9':
    if ((question == 2)&&(input.getText().length() < 6)) input.keyPressed();
    break;
  case 'p':
    println((seg_positions.size() == 0));
    println(q_passed);
    println(!((seg_positions.size() == 0) && q_passed));
  default:
  }
}

public void mouseReleased() {
  if (submit.released()) next();
  else if (current != null) {
    current.dragging = false;
    current = null;
  }
}
class Button{
	String text;
	float x,y;
	int textsize;
	boolean rollover, mouseDown, active, tInactive;
	int rest, highlight, pressed;
  int rest_bg, highlight_bg, pressed_bg;
  int startTimer, timerLength;

	Button(float x0, float y0, String s){
		text = s;
		x = x0;
		y = y0;
		textsize = 14;
		rollover=false;
		mouseDown=false;
		active = true;
		
		rest = color(60);
    rest_bg = color(240);
		highlight = color(60);
    highlight_bg = color(230);
		pressed = color(240);
    pressed_bg = color(180);
    
    startTimer = 0;
    timerLength = 0;
    
	}
	
	public void setTextSize(int s){
		textsize = s;
	}

  public void setActive(boolean b){
    active = b;
  }
  
  public void timedHighlight(int ms){
    startTimer = millis();
    timerLength = ms;
  }
  
  public void timedInactive(int ms){
    tInactive = true;
    startTimer = millis();
    timerLength = ms;
  }

	
	//void setColors(color r, color h, color p){
	//	rest      = r;
	//	highlight = h;
	//	pressed   = p;
	//}
		
	public void draw(){
    
    int elapsed = millis() - startTimer;
    if (tInactive) tInactive = elapsed < timerLength; 
    
    int alpha = 75;
    if (!active | tInactive) fill(rest_bg, alpha);
    else if (mouseDown && rollover) fill(pressed_bg);
    else if (rollover) fill(highlight_bg);
    else if (elapsed < timerLength){
      float f = PApplet.parseFloat(elapsed)/timerLength;
      alpha = (155+PApplet.parseInt(100*abs(f - 0.5f)/0.5f)) << 24;
      fill(highlight_bg, alpha);
    }
    else fill(rest_bg);
    stroke(color(255), alpha);
    rect(x, y, textWidth(text)+20, textAscent()+textDescent()+10, 8);
    
    if (!active | tInactive) fill(rest, alpha);
    else if (mouseDown && rollover) fill(pressed);
		else if (rollover) fill(highlight);
		else fill(rest);
    noStroke();
	
		textAlign(LEFT,TOP);
		textSize(textsize);
		text(text, x+10, y+5);
	}

	public boolean intersected(float mx, float my){
		textSize(textsize);
		return (mx > x && mx < x+textWidth(text)+20 && my > y && my < y+textAscent()+textDescent()+12) ? true : false;
	}
	
	public boolean interacting(int mx, int my){
		rollover = active && intersected(mx,my);
		return rollover;
	}
	
	public boolean clicked(int mx, int my) {
		mouseDown = active && intersected(mx, my);
		return mouseDown;
	}
	
  public boolean released(){//returns true if release did work
	  boolean tmp = mouseDown && rollover;
      mouseDown = false;
	  return tmp;
  }

}
class Delimeter {
  int mode;
  float value;
  float epsilon = 3; //defines notion of arbitary closeness for selection 
  boolean dragging = false;
  BlobDetection detector;
  float factor;
  
  Delimeter(int mouse_x){
    mode = 1;
    value = mouse_x;
    detector = null;
  }
  
  //Delimeter(int mouse_x, int mouse_y, int m, PImage img){
  //  mode = m;
  //  if (mode == 2) value = f_mode2(mouse_x, mouse_y, img.width, img.height);
  //  else value = f_mode1(mouse_x);
  //}
  
  Delimeter(int mouse_x, int mouse_y, int m, PImage img, PImage data, float factor){
    mode = m;
    if (mode == 3){
      value = f_mode3(mouse_x, mouse_y, data, factor);
      detector = new BlobDetection(data.width, data.height);
      detector.setThreshold(value/255);
      detector.computeBlobs(data.pixels);
    }
    else if (mode == 2) value = f_mode2(mouse_x, mouse_y, img.width, img.height);
    else value = f_mode1(mouse_x);
  }
    
  //// interacting returns true if dragging or hovering (at arbitrarily close to value)
  //boolean interacting(int mouse_x, int mouse_y, PImage img){
  //  if (dragging){ // update
  //    if (mode == 2) value = f_mode2(mouse_x, mouse_y, img.width, img.height);
  //    else value = f_mode1(mouse_x);
  //    return true;
  //  }
  //  else {
  //    float c_value;
  //    if (mode == 2) c_value = f_mode2(mouse_x, mouse_y, img.width, img.height);
  //    else c_value = f_mode1(mouse_x);
  //    return (abs(value - c_value) < epsilon);
  //  }
  //}
  
  public boolean interacting(int mouse_x, int mouse_y, PImage img, PImage data, float factor){
    if (dragging){ // update
      if (mode == 3){
        value = f_mode3(mouse_x, mouse_y, data, factor);
        detector.setThreshold(value/255);
        detector.computeBlobs(data.pixels);
      }
      else if (mode == 2) value = f_mode2(mouse_x, mouse_y, img.width, img.height);
      else value = f_mode1(mouse_x);
      return true;
    }
    else {
      float c_value;
      if (mode == 3) c_value = f_mode3(mouse_x, mouse_y, data, factor);
      else if (mode == 2) c_value = f_mode2(mouse_x, mouse_y, img.width, img.height);
      else c_value = f_mode1(mouse_x);
      return (abs(value - c_value) < (epsilon + ((mode == 3) ? 20 : 0)));
    }
  }
  
  public float getNormalizedValue(PImage img){
    if (mode == 3) return value/255;
    else if (mode == 2) return value/(img.width/2);
    else return value/img.width;
  }
    
  public void draw(PImage img, PImage data, float factor){
    if (mode == 3) {
      Blob b;
      EdgeVertex eA,eB;
      for (int n=0 ; n<detector.getBlobNb(); n++) {
        b = detector.getBlob(n);
        if (b!=null) {
          for (int m=0;m<b.getEdgeNb();m++) {
            eA = b.getEdgeVertexA(m);
            eB = b.getEdgeVertexB(m);
            if (eA !=null && eB !=null)
              line(
              eA.x*data.width*factor, eA.y*data.height*factor, 
              eB.x*data.width*factor, eB.y*data.height*factor 
                );
          }
        }
      }
    }
    else if (mode == 2) ellipse(img.width/2.0f, img.height/2.0f, value, value);
    else line(value, 0, value, img.height-1);
  }
  
  private float f_mode1(float x){
    return x;
  }

  private float f_mode1(float x, float y, float w, float h){
    return x;
  }

  private float f_mode2(float x, float y, float w, float h){
    float half_height = h/2.0f;
    return min(sqrt(sq(w/2.0f-x)+sq(half_height-y)), half_height);
  }
  
  private float f_mode3(int x, int y, PImage data, float factor){
    float fx = x/factor;
    float fy = y/factor;

    //grab 4 nearest data values for interpolation
    int i0, j0, i1, j1;
    float ax, ay;
  
    float tmp = x/factor;
    i0 = floor(tmp);
    i1 = ceil(tmp);
    ax = tmp - i0;
        
    tmp = y/factor;//note: 0,0 is bottom left
    j0 = floor(tmp);
    j1 = ceil(tmp);
    ay = tmp - j0;
        
    float v00, v01, v10, v11;
    v00 = ((data.pixels[getIndex(constrain(i0,0,(data.width-1)),constrain(j0,0,(data.height-1)), data.width)] >> 16) & 0xFF);
    v01 = ((data.pixels[getIndex(constrain(i0,0,(data.width-1)),constrain(j1,0,(data.height-1)), data.width)] >> 16) & 0xFF);
    v10 = ((data.pixels[getIndex(constrain(i1,0,(data.width-1)),constrain(j0,0,(data.height-1)), data.width)] >> 16) & 0xFF);
    v11 = ((data.pixels[getIndex(constrain(i1,0,(data.width-1)),constrain(j1,0,(data.height-1)), data.width)] >> 16) & 0xFF);
  
    //bilinear interpolation
    float v0x, v1x;
    v0x = lerp(v00,v10,ax);//map(ax,0.0,1.0,v00,v10);
    v1x = lerp(v01,v11,ax);///map(ax,0.0,1.0,v01,v11);
  
    float v = lerp(v0x,v1x,ay);//map(ay,0.0,1.0,v0x,v1x);

    //float fx = x/factor;
    //float fy = y/factor;
        
    //int t_l = floor(fy)*data.height + floor(fx); // top left index
    //int b_l = (floor(fy)+1)*data.height + floor(fx); //  bottom left index
    
    //float i1, i2, v;
    //i1 = lerp(((data.pixels[t_l] >> 16) & 0xFF), ((data.pixels[min(data.width,t_l+1)] >> 16) & 0xFF), fx-floor(fx));
    //i2 = lerp(((data.pixels[b_l] >> 16) & 0xFF), ((data.pixels[min(data.width,b_l+1)] >> 16) & 0xFF), fx-floor(fx));
    
    //println(i1, i2);

    ////(complex.pixels[y*complex.height + x] >> 16) & 0xFF
    //v = lerp(i1,i2,fy-floor(fy));
    return v;
  }

  private int getIndex(int x, int y, int N)
  {
     int idx = (y*N)+x;
     return idx;
  }

  
}
  public void settings() {  size(768, 988); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "RCM_Training" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
