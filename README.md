# SE Traider

Made for Space Engineers for easier travels

An app that makes it easy for you to choose a trading route. 


* SE Trader have two modes how to help you
  - **Better**: Will calculate every route and find for you the best one.
  - **Fast**:   Will calculate the shortest jump from every GPS, like player in game.

* Modern UI
* Using Clipboard and format like in the game

---

### UI
![ver](src/main/resources/art/se_trader_1.1.4.png)

---
### How to use

1. Add your GPS, Save it if you want
2. Calculate:
   - **By Next** - Finds the shortest distance from the GPS
   - **By Dist** - It will find all combinations. Calculates the distance and finds the smallest.
3. You can find your Route in **Documents\SE_Trader\route.txt** or click **View Route**


---

### Install
1. Download **exe** or **msi** file
2. Install it and you are ready to use.
3. GPS and Route File is located in **Documents\SE_Trader** folder

---

### Work to need to be done

   - Add Settings
   - Implement Log System
   - Make App Better

---

### Changelog:
<details>    
<summary>Show</summary>

    - v0.0
      - Working load and save of GPS
      - Distance to other GPS
      - Closest GPS
      - Load GPS on startup
    - v1.0
      - Working finding shortest Route
         - By Distance
         - By Jump
      - Added option to go back to home
    - v1.1
      - Design OverHaul
      - Fixed threats run after cloasing application
      - Added new Icon
      - Added GPS autosave after cloasing application
    - v1.1.1
      - Improve Buttons design
      - Added Reworked Table
    - v1.1.2
      - Improve Texts of X,Y,Z Table
      - Added Remove Btn to table
      - Added Clipboard to Enter And Copy Button
      - Improve output from Calculate Entire route now outputing every 2 sec
      - Fixed Icon on header
      - Locked Resizing aplication
    - v1.1.3
      - Removed remove button
      - Fixed remove button on the items
      - Added Copy all to the clipboard
      - Improve position of buttons
      - Fixed bug with not able to clean list
      - Added automatic show of route after calculating route
      - Added enter button next to textfield
      - Changed backHome checkbox
      - Changed Enter button to From Clipboard
    - v1.1.4
      - Massive optimization in calculating      
      - Increase RAM to 4GB
      - Added Stop button
      - Fixed bug with not responding on calculate by dist
      - Fixed Calculate by Dist was returning wrong route
      - Improve design of wrong input from files
      - Change output timer to 0,5sec
      - Fixed bug not reseting min distance
      
       


</details>
