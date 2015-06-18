import java.awt.*; //awt ��Ű�� Ŭ���� ���. ������ �����ϱ� �������α׷��� �ۼ��Ҷ� ���
import java.applet.*;//applet Ŭ���� ���. ������ �����ϱ� ���� ���α׷��� �ۼ��� �� ���

//���� ����� �ϴ� calculator Ŭ���� ���
public class Calculator extends Applet{
 Label label;
 float f = 0;
 char c = ' ';

 Panel button;
 Panel nPanel;
 Panel fPanel;
 Button nButton[] = new Button[10];
 Button fButton[] = new Button[6];

 //��ư �̺�Ʈ�� �߻����� �� ����
 public boolean action(Event e, Object obj){
  if ( e.target instanceof Button) {
   char c_event = ((Button) e.target).getLabel().charAt(0);
   //��Ģ����
   switch (c_event){
    case '+':
    case '*':
    case '-':
    case '/':

     f = Float.valueOf(label.getText()).floatValue();
     c = c_event;
     label.setText("");
     break;

     //'='�� Ŭ���� ���� ��� ���
     case '=':
      float fNumber = 0;
     float result = 0;
     fNumber = Float.valueOf (label.getText()).floatValue();

     if ( c!=' '){
      switch (c){
      
       //���� ����
       case '+':
        result = f + fNumber;
       break;

       //��������
       case '*':
        result = f * fNumber;
        break;

        //��������
        case '-':
         result = f - fNumber;
        break;

        //����������
        case '/':
         result = f / fNumber;
        break;
      }
      f = result;
      label.setText(String.valueOf(result));
     }
     break;
     default:

      //'0'���� �ʱ�ȭ
      if(label.getText() =="0")
       label.setText("");
       label.setText(label.getText() + c_event);
   }
   return true;
  }
  return false;
 }
 //���ø� �ʱ�ȭ
 public void init() {
  //borderlayout ����
  setLayout(new BorderLayout());

  label = new Label("0", Label.RIGHT);
  add("North", label);

  button = new Panel();
  button.setLayout(new BorderLayout());
  nPanel = new Panel();
  nPanel.setLayout(new GridLayout(4, 3));

  for (int i = 7; i>0 ;i=i-3 )
   for(int j= 0 ; j<3; j++) {
   nButton[i+j] = new Button((new String()).valueOf(i+j));
   nPanel.add(nButton[i+j]);
  }

  nButton[0] = new Button((new String()).valueOf(0));
  nPanel.add(nButton[0]);

  fButton[4] = new Button(".");
  nPanel.add(fButton[4]);

  fButton [5] = new Button("=");
  nPanel.add(fButton[5]);

  button.add("Center", nPanel);

  fPanel = new Panel();
  fPanel.setLayout(new GridLayout(4,1));


  fButton[0] = new Button("+");
  fButton[1] = new Button("-");
  fButton[2] = new Button("*");
  fButton[3] = new Button("/");

  for (int i=0; i<4 ; i++ )
   fPanel.add(fButton[i]);
  button.add("East", fPanel);
  add("Center", button);
 }
}
