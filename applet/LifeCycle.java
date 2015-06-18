import java.applet.Applet;
import java.awt.Graphics;
public class LifeCycle extends Applet{
StringBuffer buffer;
public void init(){
buffer = new StringBuffer();
additem("애플릿이 초기화 되었습니다.");
}
public void start()
{additem("에플릿을 시작하고있습니다");}
public void stop()
{additem("애플릿을 중지시키고있습니다...");}
public void destroy()
{additem("로딩 되었던 모든 리소스를 해재중입니다..");}
void additem(String Status){
buffer.append(Status);
System.out.println(Status);
repaint();
}
public void paint(Graphics g){
g.drawRect(0,0, getSize().width -1, getSize().height -1);
g.drawString(buffer.toString(),5, 15);
}
} 