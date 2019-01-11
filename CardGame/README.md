# project-team-8
project-team-8 created by GitHub Classroom

Compiled using JDK 9.
Compile Instruction:
-> Download Source Code & Libraries.
---If using IDE---
1. Have to load all the .java files in Source Code & Libraries, with CardGameResources being a package, into whatever project format the IDE uses. For example, Eclipse requires a new project to be created then the package to be created and the base .java files all added to the project to be run.
2. In addition, "kryonet-2.21-all" must be imported as an external JAR library. SEGame is not required as the compiler will pull that info from the other source code files.
---If using Command Line---
1. Make sure that the java IDK is added to build path in order to run javac and java, in addition to access to java libraries
2. Make Source Code & Libraries the current working directory
3. Run java compiler for Test_ServerClient.java with the added class path of the current working directory, which gets resources from "kryonet-      2.21-all" and "SEGame".
    For Windows this is: javac -cp ".\*" Test_ServerClient.java 
 4. To run the program, run java for Test_ServerClient with the same added class path as before
    For Windows this is: java -cp ".\*" Test_ServerClient
 *As noted below, please terminate the program before attempting a different run or else editing of the source code will be required to change to port numbers.


Test_ServerClient allows for Server and Client to both be run at the same time, otherwise a seperate executable of Server and Client must be run. Thus, for now Test_ServerClient has the main class for the program to run and must be pulled to run the program even if not checking for specific test cases.
    
If the error message "Selected port already in use." is displayed, then the TCPport and UDPport varaibles of the ServerNetwork class need to be changed.
  -Incrementing the value often solves the issue, occurs if the server is not correctly closed. If using an IDE, make sure to terminate the program before running it again. If using command line, simply using CTRL-C to terminate the program works.

Output: Test_ServerClient provides a single execution, though still uses networking, to allow 2 players to play in the same execution instance.
