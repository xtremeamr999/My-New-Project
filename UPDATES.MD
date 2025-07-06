**Changes**

  - Improved error handling and notification so it _shouldn't_ spam the chat anymore
  - Improved calculation for tolerance, so outdated order notifications are more accurate
  - Major code refactor to improve maintainability and clarity

**Bug Fixes**

 - Typo in the Order Filled Sound setting (Akinsoft)
 - Improved some product ID conversions, which fixes product id null errors
 - Crash from viewing an item with only one letter in the name in the bazaar
 - Bug with outdated order notifications not sending when they can't be sent immediately. Now they get queued.
 - Crash when making order with Farming for Dummies, or other items with "for" in the name
 - Aquamarine product id conversion fix (Akinsoft)
 - Order Status Highlight breaking from using wrong method to find slot (Akinsoft)
 - Errors from Restrict Sell feature due to variable components (Akinsoft)
 - Crash when claiming stash when not using Amecs Reborn
 - Product ID somehow getting lost between minecraft restarts, causing errors
 - Orders not being removed from the mod's internal list when you have no orders after