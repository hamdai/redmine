import java.awt.*; //awt 패키지 클래스 사용. 웹에서 동작하기 위한프로그램을 작성할때 사용
import java.applet.*;//applet 클래스 사용. 웹에서 동작하기 위한 프로그램을 작성할 때 사용

//계산기 기능을 하는 calculator 클래스 사용
public class Calculator extends Applet{
 Label label;
 float f = 0;
 char c = ' ';

 Panel button;
 Panel nPanel;
 Panel fPanel;
 Button nButton[] = new Button[10];
 Button fButton[] = new Button[6];

 //버튼 이벤트가 발생했을 때 실행
 public boolean action(Event e, Object obj){
  if ( e.target instanceof Button) {
   char c_event = ((Button) e.target).getLabel().charAt(0);
   //사칙연산
   switch (c_event){
    case '+':
    case '*':
    case '-':
    case '/':

     f = Float.valueOf(label.getText()).floatValue();
     c = c_event;
     label.setText("");
     break;

     //'='를 클릭해 연산 결과 출력
     case '=':
      float fNumber = 0;
     float result = 0;
     fNumber = Float.valueOf (label.getText()).floatValue();

     if ( c!=' '){
      switch (c){
      
       //덧셈 연산
       case '+':
        result = f + fNumber;
       break;

       //곱셈연산
       case '*':
        result = f * fNumber;
        break;

        //뺄셈연산
        case '-':
         result = f - fNumber;
        break;

        //나눗셈연산
        case '/':
         result = f / fNumber;
        break;
      }
      f = result;
      label.setText(String.valueOf(result));
     }
     break;
     default:

      //'0'으로 초기화
      if(label.getText() =="0")
       label.setText("");
       label.setText(label.getText() + c_event);
   }
   return true;
  }
  return false;
 }
 //애플릿 초기화
 public void init() {
  //borderlayout 설정
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
