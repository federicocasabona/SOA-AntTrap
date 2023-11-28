
#include<SoftwareSerial.h>
#define SERIAL_DEBUG_ENABLED 1
SoftwareSerial bt(10,11);
#if SERIAL_DEBUG_ENABLED
  #define DebugPrint(str)\
      {\
        Serial.println(str);\
      }
#else
  #define DebugPrint(str)
#endif

#define DebugPrintEstado(estado,evento)\
      {\
        String est = estado;\
        String evt = evento;\
        String str;\
        str = "-----------------------------------------------------";\
        DebugPrint(str);\
        str = "EST-> [" + est + "]: " + "EVT-> [" + evt + "].";\
        DebugPrint(str);\
        str = "-----------------------------------------------------";\
        DebugPrint(str);\
      }
//----------------------------------------------

// PINES
#define PIN_SENSOR_INFRA_ROJO 7
#define PIN_LED 3
#define PIN_RELAY 2
#define PIN_SENSOR_HUMEDAD A3

//CONSTANTES DE COMPARACION
#define HUMEDAD_MIN 512
#define ANTS_DETECTED   true
#define LOW_HUM    true
#define UMBRAL_DIFERENCIA_TIMEOUT 500
#define UMBRAL_DIFERENCIA_TIMEOUT_ROCIO 10000


// PARAMETROS
#define TOPE_INF_LED 175 
#define TOPE_SUP_LED 255 
#define CANT_AVISOS 5;
#define RALAY_ON 1
#define RALAY_OFF 0

int tirar=0;

#define MAX_STATES 7
#define MAX_EVENTS 10


 
typedef void (*transition)();

enum states         { ST_INIT, ST_WAITING_ANTS,  ST_WAITING_TIMER,  ST_CHECKING_HUM,  ST_SPRAYING_POISON,      ST_BLUETOOTH,   ST_ERROR  } current_state;
String states_s[] = { "ST_INIT", "ST_WAITING_ANTS",  "ST_WAITING_TIMER",  "ST_CHECKING_HUM", "ST_SPRAYING_POISON","ST_BLUETOOTH",  "ST_ERROR",  };

enum events         { EV_CONT,  EV_ANTS_DETECTED,     EV_ANTS_NO_DETECTED,   EV_TIMEOUT,       EV_HUM_OK,   EV_LOW_HUM , EV_BLUE,  EV_SPRAY,   EV_EXIT_BL, EV_UNKNOW } new_event;
String events_s[] = { "EV_CONT",  "EV_ANTS_DETECTED",  "EV_ANTS_NO_DETECTED",  "EV_TIMEOUT",  "EV_HUM_OK",  "EV_LOW_HUM", "EV_BLUE","EV_SPRAY", "EV_EXIT_BL", "EV_UNKNOW" };
void init_();
void error();
void none();
void hay_ants ();
void resetTimer();
void no_wait ();
void pre_spray();
void no_poison();
void spray_poison();
void sendAntSi();       
void sendAntNo(); 
void sendHumOk();  
void sendHumLow();
void wait_mensa();
void no_conect();
void exitBl();

bool presenciaDeHormigas;
bool timeout;
bool timerLiberado;
long lct;
long tiempoRociado;
bool blueEneable;
int cantAvisos;

transition state_table[MAX_STATES][MAX_EVENTS] =
{
      {init_      , none             , none                  , none        , none       ,  none         , error     , error     , none   , error  } , // state ST_INIT
      {none       , hay_ants         , resetTimer            , none        , none       ,  none         , wait_mensa, error     , exitBl , error  } , // state ST_WAITING_ANTS 
      {none       , none             , none                  , no_wait     , none       ,  none         , wait_mensa, error     , exitBl  , error  } , // state ST_WAITING_TIMER
      {none       , none             , none                  , none        , resetTimer ,  pre_spray    , no_conect , error     , exitBl  , error  } , // state ST_CHECKING_HUM
      {none       , none             , none                  , no_poison   , resetTimer ,  spray_poison , none      ,error      , exitBl  , error  } , // state ST_SPRAYING_POISON
      {none       , sendAntSi        , sendAntNo             , error       , sendHumOk  ,  sendHumLow   , none, spray_poison    , exitBl  , error  } , // state ST_BLUETOOTH
      {error      , error            , error                 , error       , error      ,  error        , error     , error      , error  , error  } , // state ST_ERROR
      
    //"EV_CONT"   , "EV_ANTS_DETECTED", "EV_ANTS_NO_DETECTED", "EV_TIMEOUT", "EV_HUM_OK",  "EV_LOW_HUM" , "EV_BLUE", "EV_SPRAY","EV_EXIT_BL","EV_UNKNOW", 
};
void do_init()
{
  Serial.begin(9600);
  
  pinMode( PIN_SENSOR_INFRA_ROJO, INPUT);
  pinMode( PIN_SENSOR_HUMEDAD, INPUT);
  pinMode( PIN_RELAY, OUTPUT);
  pinMode( PIN_LED, OUTPUT);
  
  current_state = ST_INIT;
 
  presenciaDeHormigas=false;
  cantAvisos=5;
  blueEneable=false;
  timeout = false;
  timerLiberado=false;
  lct=millis();
  bt.begin(9600);
  tiempoRociado=0;

}


bool leerSensorHumedad( )
{
  int val1=analogRead(PIN_SENSOR_HUMEDAD);
  return ( val1>=HUMEDAD_MIN );
}

bool leerSensorInfraRojo( )
{
  return ( digitalRead(PIN_SENSOR_INFRA_ROJO) == HIGH );;
}

void sendAntSi()
{
  bt.write('S');
  Serial.println('Sí Ants');
}
void sendAntNo()
{
  bt.write('N');
  Serial.println('NO Ants');
}

void sendHumLow()
{
  bt.write('L');
}
void sendHumOk()
{
  bt.write("Ok");
}
void wait_mensa()
{
  bt.write('A');
  current_state=ST_BLUETOOTH;
}
void exitBl()
{
  blueEneable=false;
  do_init();
}
void no_wait ()
{
  current_state = ST_WAITING_ANTS;
}
void hay_ants ()
{
  current_state = ST_CHECKING_HUM;
}
void resetTimer()
{
  analogWrite(PIN_LED, LOW);
  
  digitalWrite(PIN_RELAY,RALAY_OFF);
  
  presenciaDeHormigas=false;
  
  timeout = false;
  
  timerLiberado=false;
  blueEneable=false;
  
  lct=millis();
  
  current_state = ST_WAITING_TIMER; 
}
void no_poison()
{
  if(cantAvisos)
  {
    digitalWrite(PIN_RELAY,RALAY_OFF);
    analogWrite(PIN_LED,LOW);

    bt.write("No veneno");
    cantAvisos--;
  }
}
void pre_spray()
{
 current_state = ST_SPRAYING_POISON; 
}
void spray_poison()
{
  
  digitalWrite(PIN_RELAY,RALAY_ON);
  analogWrite(PIN_LED,random(TOPE_INF_LED,TOPE_SUP_LED));
  
}
void no_conect()
{
  if((tiempoRociado-millis())>UMBRAL_DIFERENCIA_TIMEOUT_ROCIO)
  {
    bt.write("No veneno");
  }
  else
  {
    bt.write("Rociando, espere");
  }
  
}

void setup( )
{
 do_init();

}
void get_new_event( )
{
  long ct = millis();
  long  diferencia = (ct - lct);
  timeout = (diferencia > UMBRAL_DIFERENCIA_TIMEOUT)? (true):(false);
 
  char op=bt.read();
  if( op=='I' || blueEneable ) 
  {
    
    
    switch(op)
    {
      case 'I':
      blueEneable=true;
      new_event=EV_BLUE;
      break;
      case 'R':
        new_event=EV_SPRAY;
      break;
      
      case 'C':
        if( leerSensorHumedad( ) == LOW_HUM)
          new_event=EV_LOW_HUM;
        else
          new_event=EV_HUM_OK;
      break;
      case 'H':
       if(leerSensorInfraRojo() == ANTS_DETECTED)
          new_event=EV_ANTS_DETECTED;
       else
          new_event=EV_ANTS_NO_DETECTED;
      break;
      case 'E':
       blueEneable=false;
       new_event=EV_EXIT_BL;
      break;
      default:
        new_event=EV_CONT;
      break;

    }
    return;
  }
  if ( timeout )
  {
    
  	if( timerLiberado && !presenciaDeHormigas && leerSensorInfraRojo() == ANTS_DETECTED  )
    {  
       presenciaDeHormigas=true;
       new_event = EV_ANTS_DETECTED; 
       return;
    }
    else if( timerLiberado && !presenciaDeHormigas )
    {
      new_event = EV_ANTS_NO_DETECTED;
      return;
    }
  	if( timerLiberado && leerSensorHumedad( ) == LOW_HUM  )
  	{
      if(tiempoRociado==0)
      {
        tiempoRociado=millis();
      }
      ct=millis();
      diferencia=ct-tiempoRociado;
      Serial.println(diferencia);
      if(diferencia>UMBRAL_DIFERENCIA_TIMEOUT_ROCIO)
      {  
        new_event=EV_TIMEOUT;
        return;
      }
    	new_event = EV_LOW_HUM; 
      return;
    }
    else if( timerLiberado )
    {
      new_event = EV_HUM_OK;
      return;
    }
  
    new_event = EV_TIMEOUT;
    timerLiberado = true;
    return;
  } 
  new_event = EV_CONT;
}

void maquina_estados_anti_ant( )
{
  get_new_event();

  if( (new_event >= 0) && (new_event < MAX_EVENTS) && (current_state >= 0) && (current_state < MAX_STATES) )
  {
    if( new_event != EV_CONT )
    {
      DebugPrintEstado(states_s[current_state], events_s[new_event]);
    }
    delay(10);
    state_table[current_state][new_event]();
   
   
  }
  else
  {
    DebugPrintEstado(states_s[ST_ERROR], events_s[EV_UNKNOW]);
  }
  
  // Consumo el evento...
  new_event = EV_CONT;
}

void loop()
{
 
  //delay(100);
 maquina_estados_anti_ant();
}
void init_()
{
  DebugPrintEstado(states_s[current_state], events_s[new_event]);
  current_state = ST_WAITING_ANTS;
}

void error()
{
  Serial.println("Error");
}


void none()
{
}



