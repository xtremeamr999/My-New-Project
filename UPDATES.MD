**Changes**

  - Improved error handling and notification so it _shouldn't_ spam the chat anymore
  - Outdated order notifications now give the option to open the orders page in the bazaar, instead of the main bazaar page
  - Improved calculation for tolerance, so outdated order notifications are more accurate
  - Major code refactor to improve maintainability and clarity
  - Outdated orders now save between sessions, so you won't get repeat notifications after restarting the game

**Bug Fixes**

 - Typo in the Order Filled Sound setting (Akinsoft)
 - Improved some product ID conversions, which fix product id null errors (often with wood logs)
 - Crash from viewing an item with only one letter in the name in the bazaar
 - Bug with outdated order notifications not sending when they can't be sent immediately. Now they get queued.
 - Crash when making order with Farming for Dummies, or other items with "for" in the name
 - Aquamarine product id conversion fix (Akinsoft)
 - Order Status Highlight breaking from using the wrong method to find slot (Akinsoft)
 - Errors from Restrict Sell feature due to variable components (Akinsoft)
 - Crash when claiming stash when not using Amecs Reborn
 - Occasional crash when the replaceItem in FlipHelper is null
 - Product ID somehow getting lost between minecraft restarts, causing errors
 - Orders not being removed from the mod's internal list when you have no orders after
 - Skyblocker Bazaar Search Overlay getting enabled when using bookmark