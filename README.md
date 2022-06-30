# SF-DataReceiver
The Data Receiver is part of the Semantic Framework, which has been separated from the SF allowing:
-	The extension of the Data Receiver to support additional request type and request parameters of new data sources.
-	The development of a new Data Translator to include additional data sources for both Copernicus and Event data sources.

## Developing a new Data Receiver API
Τo develop a new Data Receiver API, it should be exposed on 'http://dr-api:8080/DataReceiver/webresources/{task}' and should provide the following tasks:
- Events /event-{eventType}, where eventType will be the specific type of event (i.e. /event-fire, etc)
- Earthquakes	/earthquake
- Products /product

The service needs to be dockerized and the specific docker image should be included on the data-compose file. 

### Expected input format
The API should be accessed using a POST request and should receive a JSONObject containing all the required parameters:
- **Events.** 
Month, day, username and password are optional fields. In case these fields do not contain any value, they are included in the JSON structure, with value “null”. The expected input format for events is the following:
```
{
  "password": "null",
  "pageNumber": "1",
  "resultsPerPage": "2",
  "cityLat": "47.251232",
  "month": "null",
  "cityLong": "-122.122934",
  "year": "2020",
  "source": "https://data.seattle.gov/resource/kzjm-xkqj.json?$limit=[limit]&$offset=[offset]&$where=datetime%20between%20%27[SelectedDate]T00:00:00%27%20and%20%27[EndDate]T23:59:59%27&latitude=[CityLat]&longitude=[CityLong]",
  "day": "null",
  "username": "null"
}
```
-  **Earthquakes.** 
Month, day, magnitude, username and password are optional fields. In case these fields do not contain any value, they are included in the JSON structure, with value “null”. The expected input format for earthquake events is the following:
```
{
  "password": "null",
  "pageNumber": "1",
  "resultsPerPage": "2",
  "cityLat": "37.97945",
  "month": "null",
  "cityLong": "23.71622",
  "year": "2020",
  "magnitude": "5.0",
  "source": "https://webservices.ingv.it/fdsnws/event/1/query?starttime=[SelectedDate]T00:00:00&endtime=[EndDate]T23:59:59&minmagnitude=[magnitude]&format=text&lat=[CityLat]&lon=[CityLong]&maxradiuskm=50&limit=[limit]&offset=[offset]",
  "day": "null",
  "username": "null"
}
```
-	 **Products.** 
The expected input format for sentinel products is the following:
```
{
  "password": "xxx",
  "address": "scihub.copernicus.eu/dhus",
  "eventLat": "47.251232",
  "source": "https://scihub.copernicus.eu/dhus/search?start=0&rows=100&q=(platformname:Sentinel-2%20AND%20producttype:S2MSI2A%20AND%20footprint:%22Intersects(POINT([eventLat]%20[eventLong]))%22%20AND%20beginposition:[[eventStartTime]T00:00:00.000Z%20TO%20[eventEndTime]T00:00:00.000Z])&format=json",
  "eventLong": "-122.122934",
  "eventDate": "2020-06-03T13:20:00.000",
  "username": "xxx"
}
```
The aim of the API is to communicate with the data sources and return the results in “text/plain” format following the expected data schema. 
### Expected data schema
The expected data schema and response of the Data Receiver API for each data source type is summarized below:

**Events (generic)**	
```
[ 
  {
    "latitude": "38.1076",
    "additionalField": "value",
    "timestamp": "2019-07-19T11:13:17.666000",
    "longitude": "23.5393"
  }
]
```
If for instance a new field named “density” has to be included, the schema is transformed as follows:
```
[ 
  {
    "density": "xxx",
    "latitude": "38.1076",
    "timestamp": "2019-07-19T11:13:17.666000",
    "longitude": "23.5393"
  }
]
```
**Earthquake events**	
```
[ 
  {
    "eventId": "22703251",
    "depth": "19.7",
    "latitude": "38.1076",
    "magnitude": "5.2",
    "timestamp": "2019-07-19T11:13:17.666000",
    "longitude": "23.5393"
  }
]
```
**Sentinel products**	
```
[
  {
    "date": "2019-07-30T15:29:18.175Z",
    "location": "MULTIPOLYGON (((38.069313 21.988863, 40.469334 22.408005, 40.154675 24.031868, 37.724159 23.615938, 38.069313 21.988863)))",
    "id": "f828a4fb-a93b-4cee-9185-e8dad26aa6eb",
    "productURL": "https://scihub.copernicus.eu/dhus/odata/v1/Products('f828a4fb-a93b-4cee-9185-e8dad26aa6eb')/"
  }
]
```
It is worth mentioning that for products we select a batch of sentinel products corresponding to 12-days before and 12-days after the event. Also:
-	in case a user searches for events within a specific year, we detect all events within the selected year (from the 1st until the last day), 
-	in case he searches for events within a specific month of a year, we detect all events within the selected month (from the 1st until the last day)
-	and in case he searches for events within a specific day of a month, we detect all events within the specific day


## Editing the existing Data Receiver API
In case the user is familiar with Java, there is always the opportunity to edit the Data Receiver API. The changes that might need to be made (i.e. for adding a new event data source) and the corresponding classes for each of them are:
- In case the event data source does not comply with the request type or parameters edit the DataReceiver class to:
    -	Change request method (i.e. from GET to POST) for a specific event type in eventReceiver method
    -	Add additional request parameters (i.e. request properties, parameters, headers, etc) for the specific event type in eventReceiver method
    -	Additional changes will need to be made on the MyResource class to call the new method in case the event type complies with the new use case

- In case the event data source response schema does not comply with the SF data schema implement a new method on the DataTranslator class:
  -	Implement a new translatedEvents method that parses the response of the data source and generates a JSON response that complies with the SF data schema that contains the mandatory fields (timestamp, latitude, longitude) and the required additional fields (which are mentioned on the configuration file). The additional field names of the JSON response should exactly comply with the ones mentioned on the configuration file.
  -	Additional changes will need to be made on the DataReceiver class (eventReceiver method) to call the new method of the DataTranslator in case the event type complies with this use case and return the results
The same changes are applicable for including a new Copernicus data source that does not comply with the expected data schema.
To successfully include the changes made on the Data Receiver API, building the docker image and calling it on docker-compose is required.
