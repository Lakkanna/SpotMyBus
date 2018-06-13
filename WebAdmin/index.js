var express = require('express');
var bodyParser = require('body-parser');
var firebase = require('firebase');
var WebSocketServer = require('websocket').server;
var WebSocketRouter = require('websocket').router;
var http = require('http');

const application = express();
const httpServer = http.createServer(application);


var firebaseConfiguration = {
    apiKey: 'AIzaSyAZGvEEh4soCg76sShPt4zVlK7gN1Ete1E',
    authDomain: 'seproject-e0ae0.firebaseapp.com',
    databaseURL: 'https://seproject-e0ae0.firebaseio.com',
    storageBucket: ''
};

var studentPointsRouter = express.Router();
var driverPointsRouter = express.Router();
var resourcesRouter = express.Router();

function DatabaseHandler(firebaseConfiguration, httpServer) {
    this.firebaseApplication = firebase.initializeApp(firebaseConfiguration);
    this.firebaseDatabase = this.firebaseApplication.database();
    this.studentLocationTableReference = this.firebaseDatabase.ref('UserLatLngData/');
    this.driverLocationTableReference = this.firebaseDatabase.ref('BusPosition/');
    this.webSocketServer = new WebSocketServer({
        httpServer: httpServer,
        autoAcceptConnections: true // should be changed to false, to avoid CORS
    });
    this.studentRouter = new WebSocketRouter();
    this.driverRouter = new WebSocketRouter();
}

DatabaseHandler.prototype = {
    init: function() {
        thisObject = this;
        driverPointsRouter.get('/', function(request, response) {
            thisObject.driverLocationTableReference.once('value').then(function(dataSnapshot) {
                response.send(JSON.stringify(dataSnapshot));
            });
        });
        studentPointsRouter.get('/', function(request, response) {
            thisObject.studentLocationTableReference.once('value').then(function(dataSnapshot) {
                response.send(JSON.stringify(dataSnapshot));
            });
        });
        this.startEventLoop();
    },
    startEventLoop: function() {
        // add routers for better clarity
        thisObject = this;
        this.webSocketServer.on('connect', function(webSocketConnection) {
            studentResponseHandler = function(dataSnapshot) {
                webSocketConnection.send(JSON.stringify({
                    type: 'student',
                    data: dataSnapshot.val()
                }))
            };
            driverResponseHandler = function(dataSnapshot) {
                webSocketConnection.send(JSON.stringify({
                    type: 'driver',
                    data: dataSnapshot.val()
                }));
            };
            thisObject.studentLocationTableReference.on('child_added', studentResponseHandler);
            thisObject.studentLocationTableReference.on('child_changed', studentResponseHandler);
            thisObject.driverLocationTableReference.on('child_added', driverResponseHandler);
            thisObject.driverLocationTableReference.on('child_changed', driverResponseHandler);
        });
    }
};

application.use(function(request, response, next){
    console.log(request.method + " " + request.url);
    next();
});

application.use(bodyParser.json())

application.get("/", function(request, response) {
    response.sendFile(__dirname + '/public/index.html');
});

resourcesRouter.get("/:resource_type/:file", function(request, response) {
    console.log(request.params.file);
    response.sendFile(__dirname + '/public/resources/' + request.params.resource_type + '/' + request.params.file);
});

application.use('/resources', resourcesRouter);
application.use('/getInitialPoints/driver', driverPointsRouter);
application.use('/getInitialPoints/student', studentPointsRouter);

httpServer.listen(3000, function() {
    console.log("Server running at port 3000");
    console.log('Starting Firebase Connection');
    databaseHandler = new DatabaseHandler(firebaseConfiguration, httpServer);    
    console.log('Connection established');
    databaseHandler.init();
});
