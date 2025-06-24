**New Features!**

 - Order Status Highlights - In the bazaar orders screen, orders are highlighted by their status (outdated, matched, competitive). If they are outdated, you can see the market price in the tooltip.
 - Max Buy Order - Now sets the maximum buy order price to maximum amount of the item you can buy with your current purse.

**Other Changes**

 - Bookmarks now show the buy and sell prices of the item in the tooltip.
 - Added /bu discord command to open the Discord server.
 - Backend: ItemData is now OrderData, and price info is now in OrderPriceInfo object
 - Can now disable error notifications

**Bug Fixes**

 - **Null product id error from foraging update**
 - Bookmarks now only show when there is an item that can actually be bookmarked
 - RestrictSell causing issues when an item has no buy orders
 - Task null error caused by chained calls to tickExecuteLater()
 - Flip helper showing briefly in other screens
 - Now correctly identifies when an order is matched
 - 