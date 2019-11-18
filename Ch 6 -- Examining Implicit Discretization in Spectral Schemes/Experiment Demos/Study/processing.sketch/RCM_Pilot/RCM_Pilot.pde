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

String Q1 = "In the image above, how many distinct color categories do you see?\nPlease type your answer using the numeric keys.";
String Q2 = "Your task is to delineate the distinct color categories that you see. To do this, click on the image to create a boundary between distinct color categories. You can move the boundary by clicking and dragging the boundary. To remove a boundary double-click on it. You may make as few or as many boundaries as you need to partition the color categories that you believe are distinct.";

void setup() {
  size(768, 918);
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
  factor = float(width)/data.width;

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

void next() {
  if(!submit.active) return;
  
  if (block == -1) nextBlock(); //initialize
  else if (++question > 2) nextDataSet();
}

void nextBlock() {
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

void nextDataSet() {
  nextDataSet(true);
}

void nextDataSet(boolean write) {
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

void finish() {
  scheme.removeRow(record_number);
  saveTable(scheme, "run_scheme.csv");

  popup.clearText();
  popup.addText("\nDO NOT CLOSE THIS WINDOW UNTIL YOU ARE TOLD IT IS OK TO DO SO.\n\nYour UID: "+ uuid +"\n\nYou will need this UID to complete your exit survey");
  popup.setIsActive(true);
  finished = true; 
}

int blockIndex(String s) {
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

  if (question == 2) {
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
  disabled = disabled | ((question == 2) && (int(input.getText()) > 1) && (seg_positions.size() < 1) );
  submit.active = !disabled;
  submit.interacting(mouseX, mouseY);
  submit.draw();

  // pop up
  popup.draw();
}

void mousePressed() {
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

void keyPressed() {
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

void mouseReleased() {
  if (submit.released()) next();
  else if (current != null) {
    current.dragging = false;
    current = null;
  }
}

void stop() {
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

void writeRecord() {
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
BufferedWriter initializeWriter(String file_name) {
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