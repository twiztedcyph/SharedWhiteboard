# SharedWhiteboard

This is a project I did as part of my masters year in Computer Science.

Introduction
The following report details prominent features and underlying mechanisms of the shared whiteboard application.  The purpose of this application would be to support a remote meeting of people capable of sharing voice but not video.

General
This section contains general information about the program.
Starting the program
To start the program follow these steps.
1.	Extract/unzip the SharedWhiteboard.zip file.
2.	Open the extracted folder.
3.	Double click the SharedWhiteboard.jar file.
4.	Alternatively, run “java -jar SharedWhiteboard.jar” from the command console. 

Look and feel
Once started you will be presented with the simple but functional shared whiteboard window.

The buttons on the left allow you to connect to and disconnect from the network, use the auto testing robot, select text or line drawing and slowly replay the current content.  The central slider controls the drawing and text size.  To the right of the slider is some information on the number of connected peers and the current history size.  Finally, on the right is a colour wheel to allow the selection of colours for text and drawing.

Functionality
This section will give some high level details on how the program works.
Blocking queue system for draw and broadcast
During the planning stages it was decided that this program should be able to handle a large number of instructions originating both from the local user and the other peers.  In order to achieve this, the draw and broadcast classes each contain a synchronized blocking queue which are constantly polled.  Due to the blocking nature of the queues, CPU cycles are not wasted when the queue is empty.  As soon as there are instructions in the queue they are executed or sent in a first in, first out fashion.

Communication conventions
All internal i.e. draw instructions and external i.e. peer communications are done using integer arrays the only exception to this is where images are sent as byte arrays.  Integer arrays were chosen as they are small and therefore fast to send and cheap to store.  Each integer array is headed by the ID of the originating machine, followed by the type of instruction it carries.  The following elements make up the parameters for the instruction to be executed.  For example a typical draw instruction array would look like: 
int[] data = {ID, DRAW, startX, startY, endX, endY, myColour, line thickness};

Malformed data
Received data is handled based on the values in the integer array.  Firstly if a received packet originated at this machine, it is ignored.  If the instruction type is not recognised the data is ignored, a console message is produced and the program waits for the next instruction.  During testing the program proved resilient to accidental and intentionally malformed data.

Peers
On connecting, the program will send out a “is anyone out there” multicast.  Any other connected peers will reply with their ID, IP and current history size.  These replies are used to form a list peer Triples (like a Pair<> but with three) i.e. Triple<int key, String peer_IP, int history size>.  The program will select the peer with the longest history and send a history request to that peer.  The peer will reply with its full history which is stored and drawn.
On disconnecting or closing the program the peer list is reset and a multicast informing all other peers that it is leaving the network is sent out.  Both the remaining connected peer and the disconnected program continue normally.

History. 
In order to allow slow playback and newly connected peers to “catch up”, a history system was implemented.  At its core lies a queue of integer arrays.  All generated and received draw based instructions are added in order to the history.  A history of each action is maintained whenever the peer is connected.

Drawing
As mentioned earlier, the draw class constantly polls its synchronised blocking queue for drawing instructions.  There are four main instruction types: 
•	Draw
For this instruction, a line is simply drawn between two points with a selected colour and thickness.  Each time the mouse is dragged the start and end points are updated allowing for freehand lines.

•	Clear
To clear the screen, the program draws a white filled rectangle over the entire area of the drawable canvas.  If the program is connected, the local history is also cleared and all other peers are asked if they would like to clear their screens too.

•	Text
In text mode, each character is sent to the draw queue individually with a selected colour and thickness.  Correct spacing is achieved by getting the characters width and adding it to an offset.

•	Image
Draw image instructions are slightly different to the above in that not all of this information required to draw the image is contained in the instruction.  The instruction contains the location for the image, the image ID and the ID of the peer who added the image.  The application will use the image ID to request the image from the image cache.  If the image is not contained in the cache, the program will request it from the originating peer.  Once the image has been received, it is drawn at the correct coordinates.
