// const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.

const functions = require('firebase-functions');

// The Firebase Admin SDK to access Cloud Firestore.
const admin = require('firebase-admin');

// Import Node ortools
var ortools = require('node_or_tools');
var util = require('util');

admin.initializeApp();

const db = admin.firestore();


// Take the text parameter passed to this HTTP endpoint and insert it into 
// Cloud Firestore under the path /messages/:documentId/original

function calculateDistance(lat1,long1,lat2,long2){
  var lat_1 = lat1/180*Math.PI;
  var lat_2 = lat2/180*Math.PI;
  var long_1 = long1/180*Math.PI;
  var long_2 = long2/180*Math.PI;

  var d = 3963*1.609*Math.acos(Math.sin(lat_1)*Math.sin(lat_2) + 
                               Math.cos(lat_1)*Math.cos(lat_2)*Math.cos(long2-long1));
  return d;
}

function createMatrix(locations,avg_speed){
    var speed = (avg_speed || 40)/60;
    var distance = new Array();
    var duration = new Array();
    for(var i=0;i<locations.length;i++){
      var distance_i = new Array();
      var duration_i = new Array();
      var loc_i = locations[i].split("#");
      for(var j=0;j<locations.length;j++){
        var loc_j = locations[j].split("#");
        distance_i[j] = calculateDistance(loc_i[2],loc_i[3],loc_j[2],loc_j[3]);
        duration_i[j] = distance_i[j]/speed;
        if(i===j){
        	distance_i[j] = 0;
        	duration_i[j] = 0;
        }
      }
      distance[i] = distance_i;
      duration[i] = duration_i;
    }
    return [distance,duration];
}


function setupVRP(locs,maxTime){
  var d_d = createMatrix(locs);
  var mTime = 24*60;
  if(maxTime===undefined){
  	mTime = 24*60;
  }
  else{
  	mTime = maxTime*10;
  }
  console.log("mTime "+mTime);
  var t_win = new Array();
  for(var i=0;i<locs.length;i++){
    t_win[i] = [0,mTime];
  }
  var hdemands = new Array();
  for(i=0;i<locs.length-1;i++){
  	var ldemands = new Array();
    for(var j=0;j<locs.length;j++){
      // console.log(i+" "+j);
      ldemands[j] = parseInt(locs[i].split("#")[1]);
    }
    hdemands[i] = ldemands;
  }
  ldemands = new Array();
  for(i=0;i<locs.length;i++){
    ldemands[i] = 0;
  }
  hdemands[hdemands.length] = ldemands;
 
  var paramVRP = {
    numNodes:locs.length,
    costs : d_d[0],
    durations : d_d[1],
    timeWindows : t_win,
    demands : hdemands
  };
  console.log(paramVRP);
  return paramVRP;
}

function setupSearch(locs,prevParams){
  var p = new Array();
  var d = new Array();
  var rl = new Array();
  var nv = 5;
  var th = 24*60;
  var ctl = 60000;
  var vc = 100;
  for(var i=0;i<locs.length-1;i++){
    p[i] = locs.length - 1;
  }  
  for(i=0;i<locs.length-1;i++){
    d[i] = i;
  }
  if(prevParams===undefined){
    nv = 5;
  }
  else{
    nv = prevParams.get("numVehicles")+5;
    th = prevParams.get("timeHorizon")*10;
    ctl = prevParams.get("computeTimeLimit") + 10000;
    vc = prevParams.get("vehicleCapacity") + 10;
  }
  for(i=0;i<nv;i++){
      rl[i] = [];
  }
  var vrpSearchOpts = {
      computeTimeLimit: ctl,
      numVehicles: nv,
      depotNode: locs.length - 1,
      timeHorizon: th,
      vehicleCapacity: vc,
      routeLocks: rl,
      pickups: p,
      deliveries : d
      };
  console.log(vrpSearchOpts);
  return vrpSearchOpts;
}

exports.solveVRP = functions.firestore
    .document('warehouse/{warehouseID}')
    .onUpdate(async (change,context) => {

    var docNew = change.after.data();
    var docOld = change.before.data();

    if(!("runVRP" in docNew)){
      console.log("runVRP is not set");
      return null;
    }
    if("runVRP" in docOld){
    	if(docOld.runVRP===docNew.runVRP){
    		console.log("runVRP is not updated");
    		return null;
    	}
    }

    console.log("Setting up VRP");
    let docRef = db.doc("VRP/"+docNew.warehouse);
    var documentSnapshot = await docRef.get();
	if(documentSnapshot.exists){
		console.log("Have solved it before");
		if(docRef.get("solveVRP")===1){
		  console.log("VRP already solved");
		  return null;
		}
		else{
			console.log("Have solved it before unsuccessfully");
			var stops = docNew.stops;
			stops[stops.length] = docNew.warehouse + "#"  + 
			                    docNew.warehouse_cap + "#" +
			                    docNew.warehouse_lat + "#" +
			                    docNew.warehouse_long;
			console.log("timeHorizon "+documentSnapshot.get("timeHorizon"));
			var setup = setupVRP(stops,documentSnapshot.get("timeHorizon"));
			var VRP = new ortools.VRP(setup);
			var vrpSearchOpts = setupSearch(stops,documentSnapshot);
			VRP.Solve(vrpSearchOpts, async function (err, solution) {
		    if (err) {
		      console.log("VRP did not find a solution");
		      return docRef.update({warehouse:docNew.warehouse,warehouse_cap:docNew.warehouse_cap,
		      						solveVRP:0,numVehicles:vrpSearchOpts.numVehicles,
		      						computeTimeLimit:vrpSearchOpts.computeTimeLimit,
		      						timeHorizon:vrpSearchOpts.timeHorizon,
		      						vehicleCapacity:vrpSearchOpts.vehicleCapacity});
		    }
		    console.log("Solved successfully!");
		    console.log(solution);
		    var index = 0;
		    var flag = 0;
		    var collectionRef = db.collection("users");
		    var writeVRP = await docRef.update({warehouse:docNew.warehouse,warehouse_cap:docNew.warehouse_cap,
		      						solveVRP:1,numVehicles:vrpSearchOpts.numVehicles,
		      						computeTimeLimit:vrpSearchOpts.computeTimeLimit,
		      						timeHorizon:vrpSearchOpts.timeHorizon,
		      						vehicleCapacity:vrpSearchOpts.vehicleCapacity});
		    console.log("Now allocating routes");
		    return collectionRef.listDocuments().then(documentRefs => {
		    	console.log("Retrieving All Documents");
          var t = db.getAll(...documentRefs);
          console.log("TEST 1");
		    	return t;
		    }).then(documentSnapshots => {
		    	console.log("Retrieved All documents");
		    	var writeStop = [];
		    	for (let documentSnapshot of documentSnapshots) {
			      if(documentSnapshot.get("assigned")=== -1 ){
			      	var assign_route = new Array();
			      	var k = 0;
			      	var always_true = 1;
			      	while(always_true===1){
			      		if(solution.routes[index][k]===undefined){
			      			break;
			      		}
			      		assign_route[k] = solution.routes[index][k];
			      	}
			      	if(assign_route===[]){
			      		flag = 1;
			      		break;
			      	}
			      	console.log(assign_route+" "+documentSnapshot.id);
			      	writeStop.push(collectionRef.doc(documentSnapshot.id)
			      								.update({warehouse:docNew.warehouse,assigned:1,
			      										stops:assign_route}));
			      	index++;
			      }
			    }
			    var dummy = 0;
			    console.log("Index "+index+ " length "+solution.routes.length);
			    while(index<solution.routes.length){
			    	console.log("Still some routes left");
			      	assign_route = new Array();
			      	k = 0;
			      	always_true = 1;
			      	while(always_true===1){
			      		if(solution.routes[index][k]===undefined){
			      			break;
			      		}
			      		assign_route[k] = solution.routes[index][k];
			      	}
			      	if(assign_route===[]){
			      		console.log("No routes left");
			      		break;
			      	}
			      	console.log(assign_route+" "+dummy);
			    	writeStop.push(collectionRef.doc("dummy"+dummy)
			    								.set({warehouse:docNew.warehouse,assigned:0,
			    									stops:assign_route}));
			      	index++;
			      	dummy++;
			    }
			    return Promise.all(writeStop);
		    }, reason => {
          console.error(reason); // Error!
        });
		    });
		} 
	}else{
		console.log("Have not solved before");
		stops = docNew.stops;
		stops[stops.length] = docNew.warehouse + "#"  + 
		                      docNew.warehouse_cap + "#" +
		                      docNew.warehouse_lat + "#" +
		                      docNew.warehouse_long;

		setup = setupVRP(stops);
		VRP = new ortools.VRP(setup);
		vrpSearchOpts = setupSearch(stops);
		VRP.Solve(vrpSearchOpts,async function (err, solution) {
			if (err) {
				console.log("VRP did not find a solution");
				return docRef.set({warehouse:docNew.warehouse,warehouse_cap:docNew.warehouse_cap,
									solveVRP:0,numVehicles:vrpSearchOpts.numVehicles,
									computeTimeLimit:vrpSearchOpts.computeTimeLimit,
									timeHorizon:vrpSearchOpts.timeHorizon,vehicleCapacity:vrpSearchOpts.vehicleCapacity});
			}
		  	console.log("Solved successfully!");
		  	console.log(solution);
		    var index = 0;
		    var flag = 0;
		    var collectionRef = db.collection("users");
		    var writeVRP = await docRef.set({warehouse:docNew.warehouse,warehouse_cap:docNew.warehouse_cap,
		      						solveVRP:1,numVehicles:vrpSearchOpts.numVehicles,
		      						computeTimeLimit:vrpSearchOpts.computeTimeLimit,
		      						timeHorizon:vrpSearchOpts.timeHorizon,
		      						vehicleCapacity:vrpSearchOpts.vehicleCapacity});
		    console.log("Now Allocating Routes");
		    return collectionRef.listDocuments().then(documentRefs => {
		    	console.log("Retrieving All Documents");
		    	return db.getAll(...documentRefs);
		    }).then(documentSnapshots => {
		    	console.log("Retrieved All documents");
		    	var writeStop = [];
		    	for (let documentSnapshot of documentSnapshots) {
			      if(documentSnapshot.get("assigned")=== -1 ){
			      	var assign_route = new Array();
			      	var k = 0;
			      	var always_true = 1;
			      	while(always_true===1){
			      		if(solution.routes[index][k]===undefined){
			      			break;
			      		}
			      		assign_route[k] = solution.routes[index][k];
			      	}
			      	if(assign_route===undefined){
			      		flag = 1;
			      		break;
			      	}
			      	console.log(assign_route+" "+documentSnapshot.id);
			      	writeStop.push(collectionRef.doc(documentSnapshot.id)
			      								.update({warehouse:docNew.warehouse,assigned:1,
			      									stops:assign_route}));
			      	index++;
			      }
			    }
			    var dummy = 0;
			    console.log("Index "+index+ " length "+solution.routes.length);
			    while(index<solution.routes.length){
			    	console.log("Still some routes left");
			    	assign_route = new Array();
			      	k = 0;
			      	always_true = 1;
			      	while(always_true===1){
			      		if(solution.routes[index][k]===undefined){
			      			break;
			      		}
			      		assign_route[k] = solution.routes[index][k];
			      	}
			      	if(assign_route===[]){
			      		flag = 1;
			      		console.log("No routes left");
			      		break;
			      	}
			      	console.log(assign_route+" "+dummy);
			    	writeStop.push(collectionRef.doc("dummy"+dummy)
			    								.set({warehouse:docNew.warehouse,assigned:0,
			    									stops:assign_route}));
			      	index++;
			      	dummy++;
			    }
			    return Promise.all(writeStop);
		    });
		});  
	}
});
