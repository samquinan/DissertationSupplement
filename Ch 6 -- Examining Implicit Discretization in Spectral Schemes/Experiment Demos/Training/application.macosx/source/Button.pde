class Button{
	String text;
	float x,y;
	int textsize;
	boolean rollover, mouseDown, active, tInactive;
	color rest, highlight, pressed;
  color rest_bg, highlight_bg, pressed_bg;
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
	
	void setTextSize(int s){
		textsize = s;
	}

  void setActive(boolean b){
    active = b;
  }
  
  void timedHighlight(int ms){
    startTimer = millis();
    timerLength = ms;
  }
  
  void timedInactive(int ms){
    tInactive = true;
    startTimer = millis();
    timerLength = ms;
  }

	
	//void setColors(color r, color h, color p){
	//	rest      = r;
	//	highlight = h;
	//	pressed   = p;
	//}
		
	void draw(){
    
    int elapsed = millis() - startTimer;
    if (tInactive) tInactive = elapsed < timerLength; 
    
    int alpha = 75;
    if (!active | tInactive) fill(rest_bg, alpha);
    else if (mouseDown && rollover) fill(pressed_bg);
    else if (rollover) fill(highlight_bg);
    else if (elapsed < timerLength){
      float f = float(elapsed)/timerLength;
      alpha = (155+int(100*abs(f - 0.5)/0.5)) << 24;
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

	boolean intersected(float mx, float my){
		textSize(textsize);
		return (mx > x && mx < x+textWidth(text)+20 && my > y && my < y+textAscent()+textDescent()+12) ? true : false;
	}
	
	boolean interacting(int mx, int my){
		rollover = active && intersected(mx,my);
		return rollover;
	}
	
	boolean clicked(int mx, int my) {
		mouseDown = active && intersected(mx, my);
		return mouseDown;
	}
	
  boolean released(){//returns true if release did work
	  boolean tmp = mouseDown && rollover;
      mouseDown = false;
	  return tmp;
  }

}