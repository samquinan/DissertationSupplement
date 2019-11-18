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

public class RCM_Pilot extends PApplet {









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

String Q1 = "In the image above, how many distinct color categories do you see?\nPlease type your answer using the numeric keys.";
String Q2 = "Your task is to delineate the distinct color categories that you see. To do this, click on the image to create a boundary between distinct color categories. You can move the boundary by clicking and dragging the boundary. To remove a boundary double-click on it. You may make as few or as many boundaries as you need to partition the color categories that you believe are distinct.";

public void setup() {
  
  //size(512, 662);

  // create submit button
  submit = new Button(width-100, height-40, "Next");

  // create text input
  myFont = createFont("Lucida Grande", 16, true);
  input = new TextInput(this, myFont, 16);

  // create popup   
  popup = new TextPopup(this, 100, 100);

  // attempt to load scheme
  scheme = loadTable("run_scheme.csv", "header");
  int count = scheme.getRowCount();
  
  // finished flag
  finished = false;

  if (count > 0) {
    record_number = round(random(1)*(count-1));
    record = scheme.getRow(record_number);
    participant = record.getInt("Participant");
    uuid = record.getString("UUID");
    println(participant);
    println(uuid);
  } else {// display error popup
    popup.addText("Error: No trials left to run on this machine.");
    popup.setIsActive(true);
    finished = true;
  }

  // open file for appending records
  out_file = "test_records.csv";
  writer = initializeWriter(out_file);

  //load complex example
  StringList images = new StringList();
  images.append("sinusoidal.png");
  images.append("brain.png");
  images.append("elevation.png");

  option = 2; // SET COMPLEX EXAMPLE

  data = loadImage(images.get(option)); // heightmap (about 250x250px)
  complex = loadImage(images.get(option));
  data.loadPixels();
  complex.resize(width, 0);
  complex.loadPixels();
  factor = PApplet.parseFloat(width)/data.width;

  // initalize image for display
  img = createImage(width, width, RGB);
  img.loadPixels();

  //define possible color maps
  cmap_image = new StringList();
  cmap_image.append("jet_map.png");
  cmap_image.append("gray_map.png");
  cmap_image.append("default_map.png");
  cmap_image.append("kindlmann_map.png");


  // key up first block
  block = -1; // expected constant for initilaization of first block
  color_block = 0;
  data_mode_index = 0;
  data_mode = 0;
  question = 0;

  // initialize first block
  if (count > 0) next();

  // initialize delimeters
  seg_positions = new ArrayList<Delimeter>();
  current = null;
  t_last_click = millis();

  // set default drawing values
  stroke(10); // stroke color
  noFill();
  ellipseMode(RADIUS); // ellipse mode
  
}

public void next() {
  if(!submit.active) return;
  
  if (block == -1) nextBlock(); //initialize
  else if (++question > 2) nextDataSet();
}

public void nextBlock() {
  String current_cmap = null; // grab new color map
  switch (++block) {
  case 0:
    current_cmap = "block_0 cmap";
    break;
  case 1:
    current_cmap = "block_1 cmap";
    break;
  case 2:
    current_cmap = "block_2 cmap";
    break;
  case 3:
    current_cmap = "block_3 cmap";
    break;
  default:
    finish();
    break;
  }

  if (current_cmap != null && block < 4) {
    color_block = blockIndex(record.getString(current_cmap));

    // grab new color-map 
    map = loadImage(cmap_image.get(color_block));
    map.loadPixels();

    // update with new data set
    data_mode_index = -1;
    nextDataSet(false);
  }
}

public void nextDataSet() {
  nextDataSet(true);
}

public void nextDataSet(boolean write) {
  if (write) writeRecord();
  if (++data_mode_index > 2) nextBlock();
  else {
    int[][] permutations = {{1, 2, 3}, {1, 3, 2}, {2, 1, 3}, {2, 3, 1}, {3, 1, 2}, {3, 2, 1}};
    String current; // grab new color map
    switch (block) {
    default:
    case 0:
      current = "block_0 dPmt";
      break;
    case 1:
      current = "block_1 dPmt";
      break;
    case 2:
      current = "block_2 dPmt";
      break;
    case 3:
      current = "block_3 dPmt";
      break;
    }
    // get the data mode
    data_mode = permutations[record.getInt(current)-1][data_mode_index];

    // update display
    updateDisplay();

    // reset questions
    question = 1;
  }
}

public void finish() {
  scheme.removeRow(record_number);
  saveTable(scheme, "run_scheme.csv");

  popup.clearText();
  popup.addText("\nDO NOT CLOSE THIS WINDOW UNTIL YOU ARE TOLD IT IS OK TO DO SO.\n\nYour UID: "+ uuid +"\n\nYou will need this UID to complete your exit survey");
  popup.setIsActive(true);
  finished = true; 
}

public int blockIndex(String s) {
  int i;
  switch (s) {
  case "D":
    i=2;
    break;
  case "J":
    i=0;
    break;
  case "G":
    i=1;
    break;
  case "M":
    i=3;
    break;
  default:
    i=-1;
  }

  return i;
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

  if (question == 2) {
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
      if (option == 0) {
        ix = 0; 
        iy = 0; 
        iw = img.width; 
        ih = 1*img.height/8;
        copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
        ix = 0; 
        iy = 7*img.height/9; 
        iw = img.width; 
        ih = img.height-iy;
        copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
      } else if (option == 1) {
        ix = 0; 
        iy = 0; 
        iw = img.width/2; 
        ih = img.height/2;
        copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
      } else if (option == 2) {
        ix = img.width/6; 
        iy = 0; 
        iw = 4*img.width/6; 
        ih = img.height/3;
        copy(img, ix, iy, iw, ih, ix, iy, iw, ih);
      }
    }
  }

  //Display the question
  fill(255);
  textSize(13);
  if (question == 1) {
    text(Q1, 50, height - 130, width-100, 40);
    input.draw(50, height - 75);
  } else if (question == 2) {
    text(Q2, 50, height - 130, width-100, 90);
  }
  // button
  boolean disabled = finished;
  disabled = disabled | ((question == 1) && !(input.getText().length() > 0));
  disabled = disabled | ((question == 2) && (PApplet.parseInt(input.getText()) > 1) && (seg_positions.size() < 1) );
  submit.active = !disabled;
  submit.interacting(mouseX, mouseY);
  submit.draw();

  // pop up
  popup.draw();
}

public void mousePressed() {
  int t_click = millis();

  // check for submit click, then check for delimeter interaction
  if (!submit.clicked(mouseX, mouseY) && (mouseY < width) && (question == 2)) {
    // if arbitrarily close to existing, grab that
    for (Delimeter p : seg_positions) {
      if (p.interacting(mouseX, mouseY, img, data, factor)) { 
        current = p;
        continue;
      }
    }
    // if double click, delete
    if ((current != null) && ((t_click - t_last_click) < threshold)) {
      seg_positions.remove(current);
      current = null;
    } else if (current == null) { // else generate a new delimeter
      current = new Delimeter(mouseX, mouseY, data_mode, img, data, factor);
      current.dragging = true;
      seg_positions.add(current);
    } else current.dragging = true; // current != null and not a double click
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
    if (question == 1) input.keyPressed();
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
    if ((question == 1)&&(input.getText().length() < 6)) input.keyPressed();
    break;
  case 's':
    img.save("screen"+str(block*10+data_mode)+".png");
    break;
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

public void stop() {
  if (writer != null) {
    try {
      writer.close();
    } 
    catch (IOException e) {
      println("Error while closing the writer");
      e.printStackTrace();
    }
  }
}

public void writeRecord() {
  if (writer != null) {
    try {//participant, UUID, block, cmap, data, num_color_categories, delimeter_list
      StringBuilder new_record = new StringBuilder();
      new_record.append(str(participant));
      new_record.append(", ");
      new_record.append(uuid);
      new_record.append(", ");
      new_record.append(block);
      new_record.append(", ");
      switch(color_block) {
      case 0:
        new_record.append("jet");
        break;
      case 1:
        new_record.append("gray");
        break;
      case 2:
        new_record.append("default");
        break;
      case 3:
        new_record.append("kindlmann");//matplotlib
        break;
      }
      new_record.append(", ");
      switch(data_mode) {
      case 1:
        new_record.append("1D");
        break;
      case 2:
        new_record.append("2D");
        break;
      case 3:
        new_record.append("Complex");
        break;
      }
      new_record.append(", ");
      new_record.append(input.getText());
      new_record.append(", ");
      for (Delimeter p : seg_positions) {
        new_record.append(str(p.getNormalizedValue(img)));
        new_record.append(", ");
      }
      writer.write(new_record.toString());
      writer.newLine();
      writer.flush();
    } 
    catch (IOException e) {
      println("Error on Write");
      e.printStackTrace();
    }
  }
  // clear before next question
  input.setText("");
  seg_positions.clear();
}

// function to initialize BufferedWriter that will append to existing file.
public BufferedWriter initializeWriter(String file_name) {
  // check if file exists
  File f = new File(sketchPath(file_name));
  boolean init = !f.isFile();
  BufferedWriter out = null;
  try {
    out = new BufferedWriter(new FileWriter(f, true));
    if (init) {
      out.write("participant, UUID, block, cmap, data, num_color_categories, delimeter_list");
      out.newLine();
      out.flush();
    }
  }
  catch (IOException e) {
    println("Error: Bad File Write");
    e.printStackTrace();
  }

  return out;
}
class Button{
	String text;
	float x,y;
	int textsize;
	boolean rollover, mouseDown, active;
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
	
	//void setColors(color r, color h, color p){
	//	rest      = r;
	//	highlight = h;
	//	pressed   = p;
	//}
		
	public void draw(){
    
    int elapsed = millis() - startTimer;
    
    int alpha = 75;
    if (!active) fill(rest_bg, alpha);
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
    
    if (!active) fill(rest, alpha);
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
      return (abs(value - c_value) < epsilon);
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
  public void settings() {  size(768, 918); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "RCM_Pilot" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
