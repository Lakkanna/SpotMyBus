var reverseGeoCodingKey = '949bcc61bf057d';
var initialLatitude = 12.9010418;
var initialLongitude = 77.5933167;
var googleMapsObject = {};

function getData(url) {
    return new Promise(function(resolve, reject) {
        xhr = new XMLHttpRequest();
        xhr.open("GET", url, true);
        xhr.onreadystatechange = function() {
            if(this.readyState === 4 && this.status == 200)
                resolve(xhr.responseText);
        };
        xhr.onerror = function() {
            reject(xhr.statusText);
        };
        xhr.send();
    });
}

function MarkableEntity(googleMapsObject, userID, initialLatitude, initialLongitude, label, color) {
    this.latitude = initialLatitude;
    this.longitude = initialLongitude;
    this.googleMapsObject = googleMapsObject;
    this.userID = userID;
    this.mapMarker = new google.maps.Marker({
        position: {
            lat: this.latitude,
            lng: this.longitude
        },
        map: googleMapsObject,
        icon: color,
        label: label
    });
    this.infoWindow = new google.maps.InfoWindow();
    this.content = document.createElement('div');
    var thisObject = this;
    console.log(thisObject);
    this.mapMarker.addListener('mouseover', function() {
        var geocoder = new google.maps.Geocoder;
        var latlng = {
            lat: thisObject.latitude,
            lng: thisObject.longitude
        };
        geocoder.geocode({'location': latlng}, function(results, status) {
            if (status === 'OK') {
                if (results[0]) {
                    thisObject.content.innerHTML = '';
                    var nameElement = document.createElement('p');
                    var addressElement = document.createElement('p');
                    nameElement.innerHTML = thisObject.userID;
                    addressElement.innerHTML = results[0].formatted_address;
                    thisObject.content.appendChild(nameElement);
                    thisObject.content.appendChild(addressElement);
                    thisObject.infoWindow.setContent(thisObject.content);
                    thisObject.infoWindow.open(thisObject.googleMapsObject, thisObject.mapMarker);
                } 
            } 
        });
        /* alternate reverse geocoder */
        /*getData("http://locationiq.org/v1/reverse.php?format=json&key=" + reverseGeoCodingKey + "&lat=" + thisObject.latitude + "&lon=" + thisObject.longitude)
            .then(function(responseData) {
                thisObject.content.innerHTML = '';
                var nameElement = document.createElement('p');
                var addressElement = document.createElement('p');
                nameElement.innerHTML = thisObject.userID;
                console.log(thisObject);
                addressElement.innerHTML = Object.values(JSON.parse(responseData)['address']).join(', ');
                thisObject.content.appendChild(nameElement);
                thisObject.content.appendChild(addressElement);
                thisObject.infoWindow.setContent(thisObject.content);
                
        });*/
    });
    this.mapMarker.addListener('mouseout', function() {
        thisObject.infoWindow.close();
    })
}

MarkableEntity.prototype = {
    getCoordinates: function() {
        return {
            'latitude': this.latitude,
            'longitude': this.longitude
        };
    },
    updateMarker: function() {
        console.log('Updating map marker');
        this.mapMarker.setPosition(new google.maps.LatLng(this.latitude, this.longitude));
    },
    updateMapPosition: function(latitude, longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.updateMarker();
    }
}

function Application(googleMapsObject) {
    this.googleMapsObject = googleMapsObject;
    this.webSocket = new WebSocket('ws://localhost:3000');
    this.busDrivers = {};
    this.students = {};
}

Application.prototype = {
    initialize: function() {
        thisObject = this;
        getData('http://localhost:3000/getInitialPoints/driver').then(function(responseData, thisObj = thisObject) {
            var jsonResponse = JSON.parse(responseData);
            for(var entityID in jsonResponse) {
                var value = jsonResponse[entityID];
                thisObj.busDrivers[value['userID']]
                    = new MarkableEntity(thisObj.googleMapsObject, value['userID'], value['latitude'], value['longitude'], 'Driver', 'http://maps.google.com/mapfiles/ms/icons/green-dot.png');
            }
            getData('http://localhost:3000/getInitialPoints/student').then(function(responseData, myObj = thisObj) {
                var jsonResponse = JSON.parse(responseData);
                for(var entityID in jsonResponse) {
                    var value = jsonResponse[entityID];
                    myObj.students[value['userID']] 
                        = new MarkableEntity(myObj.googleMapsObject, value['userID'], value['latitude'], value['longitude'], 'Student', 'http://maps.google.com/mapfiles/ms/icons/red-dot.png');
                }
                myObj.startEventLoop();  
            });
        });
    },
    startEventLoop: function() {
        thisObject = this;
        this.webSocket.addEventListener('message', function(event) {
            var responseData = JSON.parse(event.data);
            if(responseData.type == 'student') {
                var data = responseData.data;
                if(data['userID'] in thisObject.students) {
                    console.log(data['userID'] + ' has changed location');
                    thisObject.students[data['userID']].updateMapPosition(data['latitude'], data['longitude']);
                }
                else {
                    thisObject.students[data['userID']] = new MarkableEntity(thisObject.googleMapsObject, data['latitude'], data['longitude'], 'Student', 'http://maps.google.com/mapfiles/ms/icons/red-dot.png');
                }
            }
            else {
                var data = responseData.data;
                if(data['userID'] in thisObject.busDrivers) {
                    console.log(data['userID'] + ' has changed location');
                    thisObject.busDrivers[data['userID']].updateMapPosition(data['latitude'], data['longitude']);
                }
                else {
                    console.log('hello world');
                    thisObject.busDrivers[data['userID']] = new MarkableEntity(thisObject.googleMapsObject, data['latitude'], data['longitude'], 'Bus', 'http://maps.google.com/mapfiles/ms/icons/green-dot.png');
                }
            }
        });
    }
};

function start() {
    googleMapsObject = new google.maps.Map(document.getElementById('map'), {
        center: {
            lat: this.initialLatitude,
            lng: this.initialLongitude
        },
        zoom: 12
    });
    var application = new Application(googleMapsObject);
    application.initialize(); 
}
