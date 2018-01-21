/*  Luqhasal
*   Computer Networks and Applications
*/

/* http_server.c - http 1.0 server  */

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <netdb.h>
#include <unistd.h>
#include <stdlib.h>  
#include <stdio.h>   
#include <string.h> 
#include <memory.h> 

#include "config.h"
#include "helpers.h"

/*------------------------------------------------------------------------
 * Program:   http server
 *
 * Purpose:   allocate a socket and then repeatedly execute the following:
 *              (1) wait for the next connection from a client
 *              (2) read http request, reply to http request
 *              (3) close the connection
 *              (4) go back to step (1)
 *
 * Syntax:    http_server [ port ]
 *
 *               port  - protocol port number to use
 *
 * Note:      The port argument is optional.  If no port is specified,
 *            the server uses the port specified in config.h
 *
 *------------------------------------------------------------------------
 */

int main(int argc, char *argv[])
{
  struct  addrinfo * serv_addr; /* structure to hold server's address  */
  struct  addrinfo hints;
  int     listen_socket, connection_socket;
  char *  port;            
  pid_t   pid;  /* id of child process to handle request */
  char    response_buffer[MAX_HTTP_RESP_SIZE];  
  int     status_code;
  char *  status_phrase;

  /* Check command-line argument for port and extract    */
  /* use port number if one is specified.  Otherwise, use value in config.h  */

  if (argc > 1) {                 /* if argument specified on command line   */
    port = argv[1];               /* use given port   */
  } else {
    port = DEFAULT_PORT;         /* otherwise use the port definied in the config file = 8080*/
  }

  /* 1) Set the values for the server address structure:  serv_addr */
  /* make sure you use the port number specified - this should NOT be set to DEFAULT_PORT */

  memset(&serv_addr,0,sizeof(serv_addr)); /* clear sockaddr structure */
  memset(&hints,0,sizeof(hints)); /* clear hints structure */

  hints.ai_family = AF_INET;          //IPv4
  hints.ai_socktype = SOCK_STREAM;    //connection-based protocol
  hints.ai_flags = AI_PASSIVE;        //accept connection and contained wildcard address 

  getaddrinfo(NULL,port,&hints,&serv_addr);   // getaddrinfo function provides protocol-independent translation from an ANSI host name to an address

  /* 2) Create a socket */
  listen_socket = socket(serv_addr->ai_family,serv_addr->ai_socktype,0);

  /* 3) Bind the socket to the address information set in serv_addr */
  if (bind(listen_socket,serv_addr->ai_addr,serv_addr->ai_addrlen) !=0) {
    perror("bind failed: ");
  }

  /* 4) Start listening for connections */
  listen(listen_socket,QLEN);

  /* Main server loop - accept and handle requests */

  while (true) {

    /* 5) Accept a connection */
    connection_socket = accept(listen_socket,NULL,NULL);

    /* Fork a child process to handle this request */

    if ((pid = fork()) == 0) {

      /*----------START OF CHILD CODE----------------*/
      /* we are now in the child process */
      
      /* child does not need access to listen_socket */
      if ( close(listen_socket) < 0) {
        fprintf(stderr, "child couldn't close listen socket");
        exit(EXIT_FAILURE);
      }

      struct http_request new_request; // defined in httpreq.h
      /* 6) call helper function to read the request         *
       * this will fill in the struct new_request for you *
       * see helper.h and httpreq.h                       */
      
      //implementing the validity of method
      int flag = 0;
      char* str2;
      if (Parse_HTTP_Request(connection_socket, &new_request) && (new_request.URI[0] == '/')) { 
        str2 = new_request.URI;
        if (strcmp(new_request.method, "DELETE") == 0 || strcmp(new_request.method, "PUT") == 0 || strcmp(new_request.method, "POST") == 0 ) {
            flag = 1;   //The requested method isn't implemented
          } else if (strcmp(new_request.method, "GET") == 0 || strcmp(new_request.method, "HEAD") == 0) {
            if (Is_Valid_Resource(new_request.URI)) {
              flag = 2;   //The requested resource is available
            } else {
            flag = 3;   //The requested resource does not exist
          }
        }
      } else {
        flag = 4;     //The client sent an invalid request
      }

      /*** 
       *   URI, METHOD and return value of  Parse_HTTP_Request()
       */
      

      /* 7) Decide which status_code and reason phrase to return to client */

      switch(flag) {
        case 1 :
          status_code = 501;
          status_phrase = "Not Implemented";
          break;
        case 2 :
          status_code = 200;
          char* str1 = "OK";
          //char* str3 = "\nContent-Length: 105";
          char* str4 = "\nContent-Type: text/html; charset=utf-8";
          char* str5 = "\nConnection: Close";
          status_phrase = (char *) malloc(1 + strlen(str1) + strlen(str4) + strlen(str5));
          strcpy(status_phrase, str1);  //OK
          //strcat(status_phrase, str2);  //Location:
          //strcat(status_phrase, str3);  //Content-Length: 105
          strcat(status_phrase, str4);  //Content-Type: text/html
          strcat(status_phrase, str5);  //Connection: Close
          break;
        case 3 :
          status_code = 404;
          status_phrase = "Not Found";
          break;
        case 4 :
          status_code = 400;
          status_phrase = "Bad Request";
          break;
      }

      // set the reply to send
      sprintf(response_buffer, "HTTP/1.0 %d %s\r\n",status_code,status_phrase);
      printf("Sending response line: %s\n", response_buffer);
      send(connection_socket,response_buffer,strlen(response_buffer),0);
      
      // send resource if requested, under what condition will the server send an 
      // entity body?
      if (new_request.method[0] == 'G' && Is_Valid_Resource(new_request.URI))
        Send_Resource(connection_socket, new_request.URI);
      else {
        // don't need to send resource.  end HTTP headers
        send(connection_socket, "\r\n\r\n", strlen("\r\n\r\n"), 0);
      }

      /* child's work is done, close remaining descriptors and exit */

      if ( close(connection_socket) < 0) {
        fprintf(stderr, "closing connected socket failed");
        exit(EXIT_FAILURE);
      }

      /* all done return to parent */
      exit(EXIT_SUCCESS);

    }  
    /*----------END OF CHILD CODE----------------*/

    /* back in parent process  */
    /* close parent's reference to connection socket, */
    /* then back to top of loop waiting for next request */
    if ( close(connection_socket) < 0) {
      fprintf(stderr, "closing connected socket failed");
      exit(EXIT_FAILURE);
    }

    /* if child exited, wait for resources to be released */
    waitpid(-1, NULL, WNOHANG);

  } // end while(true)
}
