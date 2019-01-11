#include<stdio.h>
#include<sys/types.h>//socket
#include<sys/socket.h>//socket
#include<string.h>//memset
#include<stdlib.h>//sizeof
#include<netinet/in.h>//INADDR_ANY
#include<sqlite3.h>
#include<time.h>

//#define PORT 3000

#define MAXSZ 2048

char table[MAXSZ];
void* funcTask(void *arg);


int main(int argc, char ** argv)
{
 int sockfd;//to create socket
 int newsockfd;//to accept connection

 struct sockaddr_in serverAddress;//server receive on this address
 struct sockaddr_in clientAddress;//server sends to client on this address


 int clientAddressLength;
 int pid;
 pthread_t serverThread;



 //create socket
 sockfd=socket(AF_INET,SOCK_STREAM,0);
 int option = 1;
  if(  (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &option, sizeof(option)))< 0)
       perror("setsockopt(SO_REUSEADDR) failed");


 //initialize the socket addresses
 memset(&serverAddress,0,sizeof(serverAddress));
 serverAddress.sin_family=AF_INET;
 serverAddress.sin_addr.s_addr=htonl(INADDR_ANY);
 serverAddress.sin_port= htons((int) strtol(argv[1], (char **)NULL, 10));
 //serverAddress.sin_port=htons((int) strtol(argv[1], (char **)NULL, 10));


 //bind the socket with the server address and port
 if( (bind(sockfd,(struct sockaddr *)&serverAddress, sizeof(serverAddress))) <0)
 {
 	perror("bind failed");
	exit(0);
 }

 //listen for connection from client
 listen(sockfd,8);

 while(1)
 {
  //parent process waiting to accept a new connection
  printf("\n*****server waiting for new client connection:*****\n");
  clientAddressLength=sizeof(clientAddress);
  newsockfd=accept(sockfd,(struct sockaddr*)&clientAddress,&clientAddressLength);


  if(newsockfd < 0)
  {
  	perror("accept failed");
  }

  printf("%s\n", "Recieved request...");

  //thread is created for serving each new clients
  pthread_create(&serverThread, NULL, &funcTask, (void *)newsockfd);

 }

 return 0;
}

void* funcTask(void *arg)
{
  FILE *f = fopen("a4ServerLog.txt", "a");
  struct tm *timeInfo;
  time_t rawtime;
  char msg[MAXSZ];
  int n;
  int newsockfd = (int)arg;
  
  
  //pthread_detach(pthread_self());
  while(1)
  {
    n=recv(newsockfd,msg,MAXSZ,0);
    if(n<0)
    {
	perror("message read failed");
    }
    else if(n==0)
    {
     close(newsockfd);
     break;
    }

    time(&rawtime);
    timeInfo = localtime(&rawtime);
    fprintf(f, "Date and Time: %s", asctime(timeInfo));
    fprintf(f, "PID: %u\nTID: %u\n", getpid(), pthread_self());
    fprintf(f, "String from Client: %s\n", msg);
    fflush(f);

    printf("Date and Time: %s", asctime(timeInfo));
    printf("PID: %u TID: %u\n", getpid(), pthread_self());
    printf("String received from client: %s", msg);



    memset(table, 0, MAXSZ);
    displayTable(msg);
    send(newsockfd, table, strlen(table), 0);

    memset(msg, 0, MAXSZ);

   }
   close(newsockfd);
   fclose(f);
 }


static int callback1(void *data, int argc, char **argv, char **azColName){
   int i;
   strcat(table, "\n");
   //fprintf(stderr, "%s: ", (const char*)data);
   for(i=0; i<argc; i++){
       strcat(table, azColName[i]);
       strcat(table, " = ");
       strcat(table, argv[i] ? argv[i] : "NULL");
       strcat(table, "\n");
       //fprintf(stderr, "%s = %s\n", azColName[i], argv[i] ? argv[i] : "NULL");
   }
   printf("\n");
   return 0;
}

int displayTable(char *sql)
{
   sqlite3 *db;
   char *zErrMsg = 0;
   int rc;
   //char *sql;
   const char* data = "Callback function called";
   /* Open database */
   rc = sqlite3_open("emp.db", &db);
   if( rc ){
	fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
   	exit(0);
   }else{
        fprintf(stderr, "Opened database successfully\n");
   }
   /* Create SQL statement */
   //sql = "SELECT * from `classics`";
   /* Execute SQL statement */
   rc = sqlite3_exec(db, sql, callback1, (void*)data, &zErrMsg);
   if( rc != SQLITE_OK ){
        fprintf(stderr, "SQL error: %s\n", zErrMsg);
        sqlite3_free(zErrMsg);
   }else{
        fprintf(stdout, "Operation done successfully\n");
   }
   sqlite3_close(db);
   return 0;
}
