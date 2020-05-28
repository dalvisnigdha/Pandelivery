// const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.

const functions = require('firebase-functions');

var ortools = require('node_or_tools');
var util = require('util');
// The Firebase Admin SDK to access Cloud Firestore.
const admin = require('firebase-admin');
admin.initializeApp();

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


