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
admin.initializeApp();

// Take the text parameter passed to this HTTP endpoint and insert it into 
// Cloud Firestore under the path /messages/:documentId/original
exports.addMessage = functions.https.onRequest(async (req, res) => {
  // Grab the text parameter.
  const original = req.query.text;

  const spawn = require("child_process").spawn;
  const pythonProcess = spawn('python',["pythonFromNode.py", original]);

  var out = "";
  pythonProcess.stdout.on('data', async (data) => {
    // Do something with the data returned from python script
    data += "";
    out = data;
     // Push the new message into Cloud Firestore using the Firebase Admin SDK.
	console.log(out);

	const writeResult = await admin.firestore().collection('messages').add({out: out});
	  // Send back a message that we've succesfully written the message
  	res.json({result: `Message with ID: ${writeResult.id} added.`});
  });

  // const writeResult = await admin.firestore().collection('messages').add({original: original});
	 //  // Send back a message that we've succesfully written the message
  // 	res.json({result: `Message with ID: ${writeResult.id} added.`});
});


// Listens for new messages added to /messages/:documentId/original and creates an
// uppercase version of the message to /messages/:documentId/uppercase
exports.makeUppercase = functions.firestore.document('/messages/{documentId}')
    .onCreate((snap, context) => {
      // Grab the current value of what was written to Cloud Firestore.
      const original = snap.data().out;

      // Access the parameter `{documentId}` with `context.params`
      console.log('Uppercasing', context.params.documentId, original);
      
      const uppercase = original.toUpperCase();
      
      // You must return a Promise when performing asynchronous tasks inside a Functions such as
      // writing to Cloud Firestore.
      // Setting an 'uppercase' field in Cloud Firestore document returns a Promise.
      return snap.ref.set({uppercase}, {merge: true});
    });


