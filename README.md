# truckMapApp
Mr. Vaughn: 
    Please be patient and read my code. I can code more neatly if you give me more time. Please contact me (d77@live.de) if you need to ask any questions
    or if some feature does not work. As we devs say: "It works on my machine"
     
![it-works-on-my-machine](https://user-images.githubusercontent.com/42528802/44319702-75a2d000-a40b-11e8-9455-617c80963413.jpg)
    


  ✓ •	Use custom markers to represent  the truck stops in a map

  ✓ •	The initial view should show truck stops within a 100-mile radius from the current location

  ✓ •	The user should be able to zoom and pan the view using the common gestures for the platform. If the user zooms or pans the view, the number of truck stops should be adjusted to the new view  

  ✓ •	Represent the current location of the user with a different custom marker

  ✓ •	Provide a button with a custom button to zoom the view to the current location with a 100-mile radius 

  ✓ •	We provide a Restful API to retrieve points within a radius from a certain location 

  ✓ •	The truck stops do not need to be persisted in the phone

  ✓ •	Handle the scenario where the location information is not available

  ✓ •	Handle the scenario where the location services are disabled by the user and provide adequate actions to change this 

  ✓ •	Provide a way to switch between map view and satellite view. This setting should be persisted between different launches of the app

  ✓ •	When the user taps in a truck stop you should use a different marker for the truck stop so the user knows which one is selected and you should also display a floating view with:

    o	The name of the truck stop

    o	The distance between the truck stop and the current location

    o	The complete address: street, city, state 

    o	Additional information in other fields. If not available do not display it.

  ✓ •	Provide another button to switch between tracking mode and non-tracking mode. 

    o	In tracking mode, the map should position the current location at the center of the map so if the user is moving the user’s location will be centered. 

    o	If tracking mode is enabled, it should be temporarily suspended if the user is interacting with the screen (panning, zooming to a different area, tapping in truck stops). 

    o	After 5 seconds of inactivity, or 15 seconds of inactivity if the user is reading details of a truck stop, tracking mode should revert to the previous state before suspension. 

    o	If tracking mode is disabled the movement of the user does not affect the center of the map view which is driven by the user touches. 

    o	Tracking mode should be persisted within launches of the app 

  ✓ •	Implement a search functionality:

    o	The search will be by name, city, state, and/or zip

    o	Search for the name should match any part of the name string. The search on the other fields should be an exact match. 

    o	If more than one search field is specified the match should satisfy all conditions at the same time

    o	The results will be displayed in the map. All truck stops in the result set should be displayed. Tracking mode, if enabled, should be disabled for 30 seconds. 
