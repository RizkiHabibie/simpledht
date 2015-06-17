# Introduction #

Project Desgin
There are three things you need to implement: <br>
1) ID space partitioning/re-partitioning, <br>
2) Ring-based routing, and <br>
3) Node joins<br>
Content Provider:<br>
Predecessor_node_id<br>
Successor_node_id<br>
Have node_id (avoid using emulator number is a good approach)<br>
(The direction of message transfer is anti-clock wise direction of chord.<br>
The value-space on the ring increases in clock wise direction.<br>
Each node is responsible for objects (keys)  anti-clockwise direction. (keys less than current node_id but greater than previous node_id.<br>
Request forward happens in clockwise direction:<br>
	Successor(curr_node_id) = next_node_id ( hash > current_node_id)<br>
	We need successor node_id to check the current node’s hash space<br>
	And, we require a hash code comparator function.<br>
o	Given hash of key & succ_node_id check if key is between hash_of_succ_node_id<br>
Handle New Node Join:<br>
Two approaches:<br>
1)	First node 5554 stores all information & when a new node joins, 5554 places it at appropriate place and adjusts related node_id & their successor & previous<br>
2)	Node places itself recursively calling successor nodes.<br>
Node contacts 5554 first, <br>
if new_node  falls between 5554 & successor(5554), fixes itself<br>
else new_node contacts successor(5554) and process repeats until it places itself.<br>
<blockquote>(this requires, a way for node to know if it is placed in the system, a message to new node node may be required.) <br>
IMP: If finger tables are present even the node join takes log (No_of_node) steps <br>
Step 1 appears to have a central dependency, which can be avoided to some extent by following second approach. <br>
Another Problem Arises: <br>
How do nodes handle different requests? <br>
1)	Node join request <br>
2)	Message forward request (for the get(key) implementation <br>
3)	Message reply request (when a key is found, it directly reply’s to the source) <br>
Since there can only be a single server running on the machine for a given port, and only port is opened in the given input script, all the three different requests must be handled. <br>
One approach to do this is: upon each new connection receive a message (an Object) <br>
So, if the message has some information to identify the above three cases, the issue is solved. <br>
This implies upon a new Node connection, and message forward request, message reply request, node includes identification information and sends an object. <br>
One best idea is to pass messages as Objects, (Object Serialization)<br>
Where we read and write objects using Object Stream wrapper on top of input streams. <br></blockquote>

Since everyone should implement the same idea, it’s IMP to agree on one idea. Add inputs to this document where required, and feel free to change it.<br>
<blockquote><br> <br>
(MAKE USE OF Review -> (New Comment (or) Track Changes options to quickly identify new changes.)<br>
Lets’ agree upon an idea by tomorrow 12 Noon time<br>
<br> <br>
This is not actual project design document, but it will be based on the ideas discussed here.<br>
<br> <br> <br>
<br>
<hr><br>
<br>
<br>
Things to remember:  <br>
Project URI:		 <br>	content://edu.buffalo.cse.cse486_586.simpledht.provider <br>
Package of the project:		edu.buffalo.cse.cse486_586.simpledht <br>
Package of the Content Provider:	edu.buffalo.cse.cse486_586.simpledht.provider <br>
Project Name:			SimpleDHT  <br></blockquote>



<h1>Details</h1>

Add your content here.  Format your content with:<br>
<ul><li>Text in <b>bold</b> or <i>italic</i>
</li><li>Headings, paragraphs, and lists<br>
</li><li>Automatic links to other wiki pages