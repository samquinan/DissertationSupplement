import org.gicentre.utils.gui.*;
import blobDetection.*;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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

String Q1 = "This study is examining how how individuals distinguish color categories in color maps commonly used for data visualization. A color-category is a continuous subset of the of the color map for which you consider colors within the subset more similar to one another than colors outside that subset. In the above image, for example, one color category might be the “red” or “reddish” colors.\n\nIn the study, you will be provided images similar to the ones in this training session and asked to make judgements about color categories. You will then be asked to complete a survey asking about the reasoning behind your responses.";
String Q2 = "In this study you will be required to type answers using the numeric keys on the keyboard. Please type the number twelve using the numeric keys, then either hit \"Enter\" or click \"Next\".";
String Q3 = "You will also be asked to use a mouse to delineate the boundaries between color categories. Please click anywhere on the above image to create a boundary line.";
String Q4 = "Boundaries can be highlighted by mousing over them and moved by clicking and dragging. Please familiarize yourself with these interaction mechanisms by adding more boundary lines and dragging them around.";
String Q5 = "Boundary lines can be removed by double clicking them. Please remove at least one boundary now.";
String Q6 = "There is a portion of the image that is never covered by the boundary lines (in this case the top half of the image). This allows you to use the end of the boundary line to see the exact color where you are placing the boundary. Note that boundary lines can still be selected in the uncovered portions of the image, however. Try selecting and dragging a boundary from the top half of the image.";
String Q7 = "Here is a different data set. Practice placing, dragging and deleting boundary lines. When you are done, delete all the boundaries you placed, then click next.";
String Q8 = "Here is a real-world data set. With a complex data set like this, the subset of the image never covered by the boundary line may be arbitrary. Create and drag a boundary line over the image to discover what subset of this data is never contoured. Delete the contour when you are done.";
String Q9 = "\nYou should now be prepped for everything you will encounter in the study! Please inform the RA that you have completed the training.";
boolean q_passed, q_passed_alt;


void setup() {
  size(768, 988);
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
  factor = float(width)/data.width;

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

void next() {
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

void nextDataSet() {
    // update data mode
    data_mode++;

    // update display
    updateDisplay();
    
    //clear delimeters
    seg_positions.clear();
}

void finish() {
  popup.clearText();
  popup.addText(Q9);
  popup.setIsActive(true);
  finished = true; 
}

void updateDisplay() {
  // remove popup clear when update complex example
  popup.clearText();
  popup.setIsActive(false);
  // build stimuli based on current color map and data mode -- note: only works for 1D and 2D case
  if (data_mode < 3) {
    for (int y = 0; y < img.height; y++) {
      for (int x = 0; x < img.width; x++) {
        // for each pixel location get value
        float v = (data_mode == 1) ? float(x)/img.width : max(1-min(sqrt(sq(2*float(x)/img.width - 1) + sq(2*float(y)/img.width - 1)), 1), 0);
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

void draw() {
  background(100);
  // Displays the image at its actual size at point (0,0)
  image(img, 0, 0);

  // Display any delimeters
  stroke(((color_block == 1) ? color(255, 0, 0) : color(10))); // stroke color
  noFill();
  for (Delimeter p : seg_positions) {
    strokeWeight((p.interacting(mouseX, mouseY, img, data, factor) && mouseY < width) ? 1.5 : 1); // bias stroke weight based on whetehr currently interacting
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
  disabled = disabled || ((question == 2) && (int(input.getText()) != 12));
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

void mousePressed() {
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

void keyPressed() {
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

void mouseReleased() {
  if (submit.released()) next();
  else if (current != null) {
    current.dragging = false;
    current = null;
  }
}