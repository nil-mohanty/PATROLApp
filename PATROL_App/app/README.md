# PATROL App Android Application 

## CSCI 578 Team 4
## Team Members - Advait Naik, CHahita Verma, Nilamadhab Mohanty, Roshnee Matlani and Trinanjan Nandi


This is the UI of PATROL created using Android Studio

### Steps to Run the App
1.Download the Latest Version of Android Studio. Preferably Iguana.
2. Import the project. 
3. Resolve the dependency issues if any.
4. Add a device. Preferably a physical device Android 12+, or you can use the emulator(Exposure notification which uses BLE will not be available).
5. Currently the app is set to call the server hosted at vercel. If you want to change it to your local host, 
    Change host = "https://patrol-fawn.vercel.app" in com.example.patrol.service.HTTPService.java  to your local host or desired IP. 
6. Once the device/emulator is ready. Click the run button. 


### FAQs
1. What version of Java is preferred?
   Use Java 8 or above.
2. What if the server is not getting connected?
   If you are using Vercel URL, make sure your VPN is not turned on. 
   If you are using custom IP make sure that both device and server are in same "private" network not public network. 
3. In case of any issues while running the app whom should we reach out to?
    Please reach out at nmohanty@usc.edu or any other member of the team.
    



