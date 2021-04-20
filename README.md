# roma

###########
Preparation
###########

Install Eclipse IDE:
https://www.eclipse.org/downloads/
import project "roma" from git folder.

Intall sumo from:
https://sourceforge.net/projects/sumo/files/sumo/
version 0.32.0

Note: At this moment you cannot use SUMO 1.0.0 or above since the TraCi communicaton is no longer compatible with TraSMAPI.

###########
How to run
###########

First you need to compile, to do that you should run: "mvn compile" either from eclipse or the console.

To run the simulation you should do: "mvn exec:java -P sumo-sim" either from eclipse or the console.

############
How it works
############

Everything starts in main class:
com.iteso.roma.main.Roma

In there it starts the JADE gui and loads the RomaManagerAgent class into the main container. This is the main agent of the system.
After that uses TraSMAPI to start the simulation with SUMO. it loads some parameters:

params.add("-c=romaSimulations\\data\\romaBasic.sumo.cfg");
params.add("--device.emissions.probability=1.0");
params.add("--tripinfo-output=romaSimulations\\data\\trip_500.xml");

-c is the sumo config file
--tripinfo-output is the output file where all the information of the vehicles will be output for later analysis.

In romaSimulations\data\romaBasic.sumo.cfg there are two important files:

romaBasic.net.xml
Contains all the information related to your city grid and traffic light network.
(This file can be created with the SUMO tool provided: netedit)

rou_500.xml
Is the routes that the vehicles will take inside of the network. I have a couple of examples available.

############
Other utilities
############

In the package "com.iteso.roma.utils" you will find a lot of utilities to make your life easier.
You can check CreateVehiclesXML.java to check how to create loads of trafic based on functions
Or you can check VehiclesCreator.java to get by cluster values.

Also if you want to convert the xml output data to csv for analysis in tools like Excel or R you can use XMLReader.java


