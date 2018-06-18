$(document).ready(function() {
	$.ajax({
		type : "GET",
		url : "rest/data",
		dataType : "text",
		success : function(data) {
			processData(data);
		}
	});
});

function onlyUnique(value, index, self) {
	return self.indexOf(value) === index;
}

function processData(allText) {
	var allTextLines = allText.split(/\r\n|\n/);
	console.log("Retrived " + (allTextLines.length - 1) + " attractions.");
	var municipalities = [];
	var types = [];
	var statutes = [];
	var accessibility = [];
	var transportAccess = [];
	var environment = [];
	var data = [];
	var filterIndexes = [ 0, 2, 4, 5, 7, 9 ];
	var columnCount = 16;
	data[0] = allTextLines[0].split('","');
	
	//remove trailing quotes
	data[0][0] = data[0][0].substring(1);
	var last = data[0][columnCount-1];
	data[0][columnCount -1 ] = last.substring(0, last.indexOf('"'));
	dataType = data[0];
	
	for (var i = 1; i < allTextLines.length - 1; i++) {
		data[i] = allTextLines[i].split('","');
		// remove trailing quotes
		data[i][0] = data[i][0].substring(1);
		var last = data[i][columnCount-1];
		data[i][columnCount -1] = last.substring(0, last.indexOf('"'));
		
		//console.log(data[i][columnCount -1]);
		
		municipalities[i - 1] = data[i][0];
		types[i - 1] = data[i][2];
		statutes[i - 1] = data[i][4];
		accessibility[i - 1] = data[i][5];
		transportAccess[i - 1] = data[i][7];
		environment[i - 1] = data[i][9];
	}

	var uniqueMunicipality = municipalities.filter(onlyUnique);
	uniqueMunicipality.sort();
	var uniqueTypes = types.filter(onlyUnique);
	uniqueTypes.sort();
	var uniqueStatutes = statutes.filter(onlyUnique);
	uniqueStatutes.sort();
	var uniqueAccessiblity = accessibility.filter(onlyUnique);
	uniqueAccessiblity.sort();
	var uniqueTransportAccess = transportAccess.filter(onlyUnique);
	uniqueTransportAccess.sort();
	var uniqueEnvironment = environment.filter(onlyUnique);
	uniqueEnvironment.sort();
	
	var selectorIds = ["municipality", "type", "statute","accessibility","transportAccess","environment"];
	var selectorsOptions = [uniqueMunicipality, uniqueTypes, uniqueStatutes, uniqueAccessiblity, uniqueTransportAccess, uniqueEnvironment];

	var selections = fillOptions(selectorIds, selectorsOptions);
	
	document.addEventListener("change", function() {
		removeMarkers();
		refreshData(data, filterIndexes, selections);
	});
	
	
	infoWindow = new google.maps.InfoWindow();
	
	attractionsToShow = filterAttractions(data, filterIndexes, selections);
	showAttractionsOnMap();
}

function fillOptions(selectorIds, options) {
	var selections = [];
	for(var i = 0; i < selectorIds.length; i++) {
		var selector = document.getElementById(selectorIds[i]);
		for (var j = 0; j < options[i].length; j++) {
			var option = document.createElement("option");
			option.text = options[i][j];
			selector.add(option);
		}
		selections.push(selector);
	}
	return selections;
}
function refreshData(data, filterIndexes, selections) {
	//console.log("Refreshing map :" + data[1]);
	attractionsToShow = filterAttractions(data, filterIndexes, selections);
	
	showAttractionsOnMap();
}

function filterAttractions(data, filterIndexes, selections) {
	var filteredAttracions = [];
	for (var i = 1; i < data.length; i++) {
		attractionData = data[i];
		var toShow = true;
		for (var j = 0; j < filterIndexes.length; j++) {
			var selector = selections[j];
			var selection = selector.value;
			if (selection === "All") {
				continue;
			}

			if (attractionData[filterIndexes[j]] !== selection) {
				toShow = false;
				break;
			}
		}

		if (toShow) {
			filteredAttracions.push(attractionData);
		}
	}
	return filteredAttracions;
}

var attractionDisplayData = [];
var markers = [];
function showAttractionsOnMap() {
	//console.log("showAttractionsOnMap() called with data: " + attractionsToShow);

	for (var i = 0; i < attractionsToShow.length; i++) {
		//console.log(map);
		var latidude = attractionsToShow[i][14];
		var longitude = attractionsToShow[i][15];
		//console.log("Showing on map at : " + latidude 
				//+ "; " + langitude);
		var latLng = new google.maps.LatLng(latidude, longitude);
		var marker = new google.maps.Marker({
			position : latLng,
			map : window.map
		});
		markers.push(marker);
		var name = attractionsToShow[i][1];
		var municipality = attractionsToShow[i][0];
		var imageSearchKeywords = name;
		attractionDisplayData[i] = '<div id="content" style="float:left; width:50%">' + 
		'<h2>'+name+'</h2>'+
		'<div>'+dataType[0]+': <b>'+attractionsToShow[i][0]+'</b></div>';
		for(var k = 2; k < dataType.length; k++) {
			attractionDisplayData[i] = attractionDisplayData[i] + 
			'<div>'+dataType[k]+': <b>'+attractionsToShow[i][k]+'</b></div>';
		}
		attractionDisplayData[i] = attractionDisplayData[i] + '</div>';
		//console.log(attractionDisplayData[i]);
		markers[i].addListener('click', (function(i, imageSearchKeywords){
			var index = i;
			var imageSearch = imageSearchKeywords;
			return function() {
				window.infoWindow.setContent(attractionDisplayData[index]);
				window.infoWindow.open(map, markers[index]);
				getImages(imageSearch);
			}
		})(i, imageSearchKeywords));
	}
}

function removeMarkers() {
	for(var i = 0; i < markers.length; i++) {
		markers[i].setMap(null);
	}
	markers = [];
	//console.log("Markers Removed!!");
}

function getImages(attractionSearchName) {
	$.ajax({
		url : 'https://www.googleapis.com/customsearch/v1',
		type : "GET",
		dataType: "jsonp",
		data: {
			q: attractionSearchName,
			num: 2,
			start: 1,
			imgSize: "medium",
			searchType: "image",
			key: "AIzaSyDTUGg6-uFuQMDz4cZDHaNgtARJa3PkCKo",
			cx: "003471061625072461810:hdmllyffxhk"
		},
		success: function(response) {
			//console.log(response);
			if(response.items && response.items.length>0) {
				var data = window.infoWindow.content;
				var items = response.items;
				//console.log(items);
				data = data + 
				'<div style="float:right; width:50%">';
				items.forEach(function(element){
					if(element.link) {
						var url = element.link;
						//console.log(url);
						data = data + '<img src="'+url+'" style="width:200px;height:150px;">';
					}
				}); 
				data = data + '</div>';
				window.infoWindow.setContent(data);
			} else {
				console.log("No images found!");
			}
		},
	})
}