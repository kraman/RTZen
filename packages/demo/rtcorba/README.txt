Most of these examples are based on TAO's RTCORBA examples located at: 
$ACE_wrappers/TAO/tests/RTCORBA

Only the client side of this example is working and is meant to be run with
the TAO server of the same example. Eventually we'll have them interoperable
in both directions.

Please check the README files located with each example.

When you run the TAO server, include the following command-line argument to 
make sure the IOR is encoded with a literal IP address instead of the DNS 
name. Otherwise you will get an error. Be sure to change to your own IP
address.

-ORBEndpoint iiop://1.2@128.195.174.182

These examples assume the ior files are in the same directory as the client.
Please check the timesys.sh file to see what files are expected.