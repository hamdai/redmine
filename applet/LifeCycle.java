import java.applet.Applet;
import java.awt.Graphics;
public class LifeCycle extends Applet{
StringBuffer buffer;
public void init(){
buffer = new StringBuffer();
additem("���ø��� �ʱ�ȭ �Ǿ����ϴ�.");
}
public void start()
{additem("���ø��� �����ϰ��ֽ��ϴ�");}
public void stop()
{additem("���ø��� ������Ű���ֽ��ϴ�...");}
public void destroy()
{additem("�ε� �Ǿ��� ��� ���ҽ��� �������Դϴ�..");}
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