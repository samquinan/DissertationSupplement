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
  
  boolean interacting(int mouse_x, int mouse_y, PImage img, PImage data, float factor){
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
  
  float getNormalizedValue(PImage img){
    if (mode == 3) return value/255;
    else if (mode == 2) return value/(img.width/2);
    else return value/img.width;
  }
    
  void draw(PImage img, PImage data, float factor){
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
    else if (mode == 2) ellipse(img.width/2.0, img.height/2.0, value, value);
    else line(value, 0, value, img.height-1);
  }
  
  private float f_mode1(float x){
    return x;
  }

  private float f_mode1(float x, float y, float w, float h){
    return x;
  }

  private float f_mode2(float x, float y, float w, float h){
    float half_height = h/2.0;
    return min(sqrt(sq(w/2.0-x)+sq(half_height-y)), half_height);
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