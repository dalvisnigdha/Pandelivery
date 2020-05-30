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


exports.addMessage = functions.https.onRequest(async (req, res) => {
  // Grab the text parameter.
  const original = req.query.text;
  var SolverOpts = {
      numNodes : 3,
      costs : [[0, 10, 10],[10, 0, 10],[10, 10, 0]],
      durations : [[0, 2, 2],[2, 0, 2],[2, 2, 0]],
      timeWindows : [[0,9],[0,9],[0,9]],
      demands : [[0,0,0],[1,1,1],[1,1,1]]
    };
  var VRP = new ortools.VRP(SolverOpts);
  var vrpSearchOpts = {
  computeTimeLimit: 10000,
  numVehicles: 5,
  depotNode: 0,
  timeHorizon: 9 * 60 * 60,
  vehicleCapacity: 100,
  routeLocks: [[], [], [], [],[]],
  pickups: [0 , 0 ],
  deliveries : [1,2]
  };

  VRP.Solve(vrpSearchOpts, async function (err, solution) {
    if (err) return console.log(err);
    console.log(util.inspect(solution, {showHidden: false, depth: null}));
    const writeResult = await admin.firestore().collection('messages').add({original: original});
    // Send back a message that we've succesfully written the message
    res.json({result: `Message with ID: ${writeResult.id} added.`,cost: solution.cost});
  });
    
});


// Listens for new messages added to /messages/:documentId/original and creates an
// uppercase version of the message to /messages/:documentId/uppercase
exports.makeUppercase = functions.firestore.document('/messages/{documentId}')
    .onCreate((snap, context) => {
      // Grab the current value of what was written to Cloud Firestore.
      const original = snap.data().original;

      // Access the parameter `{documentId}` with `context.params`
      console.log('Uppercasing', context.params.documentId, original);
      
      const uppercase = original.toUpperCase();
      
      // You must return a Promise when performing asynchronous tasks inside a Functions such as
      // writing to Cloud Firestore.
      // Setting an 'uppercase' field in Cloud Firestore document returns a Promise.
      return snap.ref.set({uppercase}, {merge: true});
    });


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
    for(var i=0;i<stops.length;i++){
      var distance_i = new Array();
      var duration_i = new Array();
      var loc_i = stops[i].split("#");
      for(var j=0;j<stops.length;j++){
        var loc_j = stops[j].split("#");
        distance_i[j] = calculateDistance(loc_i[2],loc_i[3],loc_j[2],loc_j[3]);
        duration_i[j] = distance_i[j]/speed;
      }
      distance[i] = distance_i;
      duration[i] = duration_i;
    }
    return [distance,duration];
}


function setupVRP(locs,maxTime){
  var d_d = createMatrix(locs);
  var mTime = maxTime || 24*60;
  var t_win = new Array();
  for(var i=0;i<locs.length;i++){
    t_win[i] = [0,mTime];
  }
  var hdemands = new Array();
  for(i=0;i<locs.length-1;i++){
    for(var j=0;j<locs.length;j++){
      hdemands[i][j] = locs[i].split("#")[1];
    }
  }
  for(i=0;i<locs.length;i++){
    hdemands[hdemands.length][i] = 0;
  }

  var paramVRP = {
    numNodes:locs.length,
    costs : d_d[0],
    durations : d_d[1],
    timeWindows : t_win,
    demands : hdemands
  };

  return paramVRP;
}

function setupSearch(locs,prevParams){
  var p = new Array();
  var d = new Array();
  var rl = new Array();
  var nv = 5;
  for(var i=0;i<locs.length-1;i++){
    p[i] = locs.length - 1;
  }  
  for(i=0;i<loc.length-1;i++){
    d[i] = i;
  }
  if(prevParams===undefined){
    nv = 5;
  }
  else{
    nv = prevParams.numVehicles+5;
  }
  for(i=0;i<nv;i++){
      rl[i] = [];
  }
  var vrpSearchOpts = {
      computeTimeLimit: 60000,
      numVehicles: nv,
      depotNode: locs.length - 1,
      timeHorizon: 24*60,
      vehicleCapacity: 100,
      routeLocks: rl,
      pickups: p,
      deliveries : d
      };
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
    let docRef = db.doc("VRP"+docNew.warehouse);
    var documentSnapshot = await docRef.get();
	if(documentSnapshot.exists){
		if(docRef.get("solveVRP")){
		  console.log("VRP already solved");
		  return null;
		}
		else{
		  var stops = docNew.stops;
		  stops[stops.length] = docNew.warehouse + "#"  + 
		                        docNew.warehouse_cap + "#" +
		                        docNew.warehouse_lat + "#" +
		                        docNew.warehouse_long;
		  var setup = setupVRP(stops);
		  var VRP = new ortools.VRP(setup);
		  var vrpSearchOpts = setupSearch(stops,docNew);
		  VRP.Solve(vrpSearchOpts, async function (err, solution) {
		    if (err) {
		      vrpSearchOpts.warehouse = docNew.warehouse;
		      vrpSearchOpts.warehouse_cap = docNew.warehouse_cap;
		      vrpSearchOpts.solveVRP = 0;
		      console.log("VRP did not find a solution");
		      return docRef.update(vrpSearchOpts);
		    }
		    vrpSearchOpts.warehouse = docNew.warehouse;
		    vrpSearchOpts.warehouse_cap = docNew.warehouse_cap;
		    vrpSearchOpts.solveVRP = 1;
		    vrpSearchOpts.cost = solution.cost;
		    vrpSearchOpts.routes = solution.routes;
		    vrpSearchOpts.times = solution.times;

		    var index = 0;
		    var flag = 0;
		    var collectionRef = db.collection("users");
		    var writeVRP = await docRef.update(vrpSearchOpts);
		    return collectionRef.listDocuments().then(documentRefs => {
		    	return db.getAll(documentRefs);
		    }).then(async documentSnapshots => {
		    	var writeStop = [];
		    	for (let documentSnapshot of documentSnapshots) {
			      if(documentSnapshot.get("assigned")=== -1 ){
			      	if(solution.routes[index]!==[]){
			      		flag = 1;
			      		break;
			      	}
			      	writeStop.push(collectionRef.doc(documentSnapshot.id)
			      								.update({warehouse:docNew.warehouse,assigned:1,stops:solution.routes[index]}));
			      	index++;
			      }
			    }
			    while(flag===0){
			    	if(solution.routes[index]!==[]){
			      		break;
			      	}
			    	writeStop.push(collectionRef.doc(documentSnapshot.id)
			    								.update({warehouse:docNew.warehouse,assigned:0,stops:solution.routes[index]}));
			      	index++;
			    }
			    return Promise.all(writeStop);
		    });
		  });
		} 
	}else{
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
		      vrpSearchOpts.warehouse = docNew.warehouse;
		      vrpSearchOpts.warehouse_cap = docNew.warehouse_cap;
		      vrpSearchOpts.solveVRP = 0;
		      console.log("VRP did not find a solution");
		      return docRef.update(vrpSearchOpts);
		    }
		    vrpSearchOpts.warehouse = docNew.warehouse;
		    vrpSearchOpts.warehouse_cap = docNew.warehouse_cap;
		    vrpSearchOpts.solveVRP = 1;
		    vrpSearchOpts.cost = solution.cost;
		    vrpSearchOpts.routes = solution.routes;
		    vrpSearchOpts.times = solution.times;

		    var index = 0;
		    var flag = 0;
		    var collectionRef = db.collection("users");
		    var writeVRP = await docRef.update(vrpSearchOpts);
		    return collectionRef.listDocuments().then(documentRefs => {
		    	return db.getAll(documentRefs);
		    }).then(async documentSnapshots => {
		    	var writeStop = [];
		    	for (let documentSnapshot of documentSnapshots) {
			      if(documentSnapshot.get("assigned")=== -1 ){
			      	if(solution.routes[index]!==[]){
			      		flag = 1;
			      		break;
			      	}
			      	writeStop.push(collectionRef.doc(documentSnapshot.id)
			      								.update({warehouse:docNew.warehouse,assigned:1,stops:solution.routes[index]}));
			      	index++;
			      }
			    }
			    while(flag===0){
			    	if(solution.routes[index]!==[]){
			      		break;
			      	}
			    	writeStop.push(collectionRef.doc(documentSnapshot.id)
			    								.update({warehouse:docNew.warehouse,assigned:0,stops:solution.routes[index]}));
			      	index++;
			    }
			    return Promise.all(writeStop);
		    });
		});  
	}
});
