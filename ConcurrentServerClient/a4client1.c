#include<stdio.h>
#include<sys/types.h>//socket
#include<sys/socket.h>//socket
#include<string.h>//memset
#include<stdlib.h>//sizeof
#include<netinet/in.h>//INADDR_ANY
#include<time.h>


#define PORT 3000
#define SERVER_IP "129.110.92.15"
#define MAXSZ 2048
int main(int argc, char ** argv)
{
 int sockfd;//to create socket

 struct sockaddr_in serverAddress;//client will connect on this

 int n;
 //char msg1[25];
 char msg2[MAXSZ];
 FILE *f = fopen("a4Client1Log.txt", "a");
 time_t rawtime;
 struct tm *timeInfo;
 

 //create socket
 if( (sockfd=socket(AF_INET,SOCK_STREAM,0)) < 0)
 {
 	perror("Problem in creating the socket");
	exit(2);
 }
 //initialize the socket addresses
 memset(&serverAddress,0,sizeof(serverAddress));
 serverAddress.sin_family=AF_INET;
 serverAddress.sin_addr.s_addr=inet_addr(argv[1]);
 serverAddress.sin_port = htons((int) strtol(argv[2], (char **)NULL, 10));
 //serverAddress.sin_port=htons((int) strtol(argv[1], (char **)NULL, 10));

 //client  connect to server on port
 if ( (connect(sockfd,(struct sockaddr *)&serverAddress,sizeof(serverAddress))) <0)
 {
 	perror("Problem in connecting to the server");
	exit(3);
 }
 
 //send to server and receive from server
 while(1)
 { 
  
  char msg1[]= "SELECT * from `classics`";
  send(sockfd, msg1, strlen(msg1), 0);
  printf("Message sent: %s", msg1);
  
  if( recv(sockfd, msg2, MAXSZ, 0) == 0)
  {
  	perror("The server terminated early");
	exit(4);
  }
  printf("%s\n", "Message recieved");
  
  time(&rawtime);
  timeInfo = localtime(&rawtime);
 
  fprintf(f, "Date and Time: %s", asctime(timeInfo) );
  fprintf(f, "PID: %u\nTID: %u\n", getpid(), pthread_self());
  fprintf(f, "String sent: %s\nRecieved:\n%s", msg1, msg2);
  fflush(f);

  printf( "Date and Time: %s", asctime(timeInfo) );
  printf("PID: %u TID %u\n", getpid(), pthread_self() );
  printf("%s", "String recieved from the server:\n");
  fputs(msg2, stdout);
  


  memset(msg1, 0, MAXSZ);
  memset(msg2, 0, MAXSZ);
  
  
  sleep(3);
 }
 fclose(f);
 return 0;
}
