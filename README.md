# Android Chat Client
This App will connect to the [ESP32 Chat Server](https://github.com/davidlehrian/ESP32-Chat-Server) running on the local network. It will use mDNS to locate it and then connect and open a socket through which it will send text typed at the prompt to and the ESP32 ChatServer and will receive text from the ESP32 ChatServer that is sent to it from other instances of the ChatClient. If you only have Android device you can use the Chat Client from [Chat Client and Server with Libevent](https://github.com/davidlehrian/ChatClientAndServerWithLibevent) to test. The ChatClient from [Chat Client and Server with Libevent](https://github.com/davidlehrian/ChatClientAndServerWithLibevent) does not incorporate mDNS so you will need to get the ip address of the ESP32 ChatServer from the terminal and input it on the command line when launching the Windows/Linux ChatClient. This Android ChatClient app cannot be used with the Windows/Linux ChatServer as the server does not incorporate mDNS.

This project was created mainly for me to learn how to communicate with an ESP32 via WiFi from an Android device and to locate it using mDNS and I thought that it may be useful to someone else out there. This app is fairly simple but demonstrates how to locate the device using mDNS and open a socket for bi-directional communication. 
## Building the project<br>
1. Check out the repository.
2. Open it with Android Studio.
3. Make sure you have the [ESP32 Chat Server](https://github.com/davidlehrian/ESP32-Chat-Server) already running.
4. Launch the app.
