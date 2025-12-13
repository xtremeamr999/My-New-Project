# Events and Handlers Documentation

This document provides comprehensive documentation for all events and event handlers in the Bazaar Utils mod. It is intended for developers who want to understand the event system or extend the mod with custom functionality.

## Table of Contents

1. [Overview](#overview)
2. [Event System](#event-system)
3. [Events](#events)
4. [Handlers](#handlers)
5. [Usage Examples](#usage-examples)
6. [Best Practices](#best-practices)

## Overview

Bazaar Utils uses the [Orbit Event System](https://github.com/MeteorDevelopment/orbit) for managing events. The event system allows different parts of the mod to communicate without tight coupling, making the code more maintainable and extensible.

### Key Concepts

- **Events**: Data classes that represent something that has happened in the game
- **Handlers**: Methods annotated with `@EventHandler` that listen for and respond to events
- **Event Bus**: The central hub that distributes events to registered handlers
- **Cancellable Events**: Events that implement `ICancellable` can be cancelled to prevent default behavior

## Event System

### Registering Handlers

To listen for events, create a class with methods annotated with `@EventHandler` and subscribe it to the event bus:

```java
public class MyEventListener {
    @RunOnInit
    public static void subscribe() {
        EVENT_BUS.subscribe(MyEventListener.class);
    }
    
    @EventHandler
    public void onEvent(SomeEvent event) {
        // Handle the event
    }
}
```

### Posting Events

Events are posted to the event bus using:

```java
EVENT_BUS.post(new SomeEvent(...));
```

All registered handlers for that event type will be called.

## Events

### Core Events

#### ScreenChangeEvent

**Package:** `com.github.mkram17.bazaarutils.events`

**Description:** Fired when the player's current screen changes.

**Fields:**
- `oldScreen` (Screen) - The screen that was previously displayed (may be null)
- `newScreen` (Screen) - The screen that is now being displayed (may be null)

**Cancellable:** No

**Usage:**
```java
@EventHandler
public void onScreenChange(ScreenChangeEvent event) {
    Screen old = event.getOldScreen();
    Screen newScreen = event.getNewScreen();
    // Handle screen transition
}
```

---

#### ChestLoadedEvent

**Package:** `com.github.mkram17.bazaarutils.events`

**Description:** Fired when a chest/container GUI is fully loaded with all items. The mod waits for all item slots to be populated (not in "Loading..." state) before firing this event.

**Fields:**
- `lowerChestInventory` (Inventory) - The inventory of the lower chest/container
- `itemStacks` (List<ItemStack>) - List of all non-empty item stacks
- `containerName` (String) - The display name of the container

**Cancellable:** No (implements ICancellable but not functional)

**Important Notes:**
- Uses a polling mechanism that checks every 40ms (up to 50 attempts / 2 seconds)
- Automatically registered via `@RunOnInit` annotation
- Handles Hypixel's asynchronous item loading

**Usage:**
```java
@EventHandler
public void onChestLoaded(ChestLoadedEvent event) {
    String containerName = event.getContainerName();
    if (containerName.contains("Bazaar")) {
        List<ItemStack> items = event.getItemStacks();
        processBazaarItems(items);
    }
}
```

---

#### SlotClickEvent

**Package:** `com.github.mkram17.bazaarutils.events`

**Description:** Fired when a slot is clicked in a handled screen (GUI with inventory).

**Fields:**
- `handledScreen` (HandledScreen<?>) - The screen where the click occurred
- `slot` (Slot) - The slot that was clicked
- `slotId` (int) - The numeric ID of the slot
- `clickedButton` (int) - The mouse button clicked (0 = left, 1 = right, 2 = middle)
- `clickType` (SlotActionType) - The type of click action
- `usePickblockInstead` (boolean) - If true, pickblock action will be used
- `cancelled` (boolean) - Whether the event is cancelled

**Cancellable:** Yes

**Usage:**
```java
@EventHandler
public void onSlotClick(SlotClickEvent event) {
    if (shouldPreventClick(event.slot)) {
        event.setCancelled(true);
        return;
    }
    // Process the click normally
}
```

---

#### ReplaceItemEvent

**Package:** `com.github.mkram17.bazaarutils.events`

**Description:** Fired when an item in an inventory is about to be replaced. Useful for modifying item displays in GUI menus.

**Fields:**
- `original` (ItemStack) - The original item stack before replacement
- `inventory` (SimpleInventory) - The inventory containing the item
- `slotId` (int) - The slot where the replacement occurs
- `replacement` (ItemStack) - The item that will replace the original (modifiable)

**Cancellable:** No (implements ICancellable but not functional)

**Usage:**
```java
@EventHandler
public void onItemReplace(ReplaceItemEvent event) {
    ItemStack original = event.getOriginal();
    ItemStack modified = addCustomLore(original);
    event.setReplacement(modified);
}
```

---

#### SignOpenEvent

**Package:** `com.github.mkram17.bazaarutils.events`

**Description:** Fired when a sign editing screen is opened.

**Fields:**
- `signEditScreen` (SignEditScreen) - The sign editing screen being opened

**Cancellable:** No

**Usage:**
```java
@EventHandler
public void onSignOpen(SignOpenEvent event) {
    // Example: Log when a sign editing screen is opened
    System.out.println("Sign editing screen opened: " + event.getSignEditScreen());
    // You can add additional logic here if needed
}
```

---

### Bazaar-Specific Events

#### BazaarChatEvent

**Package:** `com.github.mkram17.bazaarutils.events`

**Description:** Fired when a bazaar-related chat message is received and parsed. This is a generic record that contains the event type and order information.

**Type Parameter:**
- `T extends OrderInfoContainer` - The type of order information

**Fields:**
- `type` (BazaarEventTypes) - The type of bazaar event
- `order` (T) - The order information associated with the event

**Event Types:**
- `ORDER_CREATED` - A new buy or sell order was created
- `ORDER_CANCELLED` - An existing order was cancelled
- `ORDER_FILLED` - An order was completely filled
- `ORDER_CLAIMED` - Coins or items from a filled order were claimed
- `ORDER_FLIPPED` - An order's price was flipped/updated
- `INSTA_SELL` - Items were instantly sold to buy orders
- `INSTA_BUY` - Items were instantly bought from sell offers

**Cancellable:** No

**Usage:**
```java
@EventHandler
public void onBazaarChat(BazaarChatEvent<?> event) {
    switch (event.type()) {
        case ORDER_CREATED -> handleOrderCreated(event.order());
        case ORDER_FILLED -> playNotificationSound();
        case INSTA_SELL -> updateOrderLimit(event.order());
    }
}
```

---

#### OutbidOrderEvent

**Package:** `com.github.mkram17.bazaarutils.events`

**Description:** Fired when a bazaar order is outbid or becomes competitive again.

**Fields:**
- `order` (BazaarOrder) - The bazaar order that was affected
- `isOutbid` (boolean) - True if outbid, false if competitive again

**Cancellable:** No (implements ICancellable but not functional)

**Usage:**
```java
@EventHandler
public void onOutbid(OutbidOrderEvent event) {
    if (event.isOutbid()) {
        notifyPlayer("Your order for " + event.getOrder().getName() + " was outbid!");
    }
}
```

---

#### UserOrdersChangeEvent

**Package:** `com.github.mkram17.bazaarutils.events`

**Description:** Fired when the user's bazaar orders list changes.

**Fields:**
- `changeType` (ChangeTypes) - The type of change (ADD, REMOVE, UPDATE)
- `order` (BazaarOrder) - The bazaar order that was affected

**Change Types:**
- `ADD` - A new order was added
- `REMOVE` - An order was removed
- `UPDATE` - An order was updated

**Cancellable:** No (implements ICancellable but not functional)

**Usage:**
```java
@EventHandler
public void onOrderChange(UserOrdersChangeEvent event) {
    switch (event.getChangeType()) {
        case ADD -> handleNewOrder(event.getOrder());
        case REMOVE -> handleRemovedOrder(event.getOrder());
        case UPDATE -> handleUpdatedOrder(event.getOrder());
    }
}
```

---

#### BazaarDataUpdateEvent

**Package:** `com.github.mkram17.bazaarutils.events`

**Description:** Fired when bazaar data is updated from the Hypixel API.

**Fields:**
- `bazaarReply` (SkyBlockBazaarReply) - The complete bazaar data from the API

**Cancellable:** No (implements ICancellable but not functional)

**Usage:**
```java
@EventHandler
public void onBazaarDataUpdate(BazaarDataUpdateEvent event) {
    SkyBlockBazaarReply reply = event.getBazaarReply();
    updatePriceCache(reply);
}
```

---

## Handlers

### BUListener Interface

**Package:** `com.github.mkram17.bazaarutils.events.handlers`

**Description:** Interface for event listeners that need to subscribe to the event bus.

**Methods:**
- `subscribe()` - Subscribes this listener to the event bus
- `getEventListeners()` - Returns list of all registered listeners from config

**Implementation Example:**
```java
public class MyEventListener implements BUListener {
    @Override
    public void subscribe() {
        EVENT_BUS.subscribe(this);
    }
    
    @EventHandler
    public void onSomeEvent(SomeEvent event) {
        // Handle the event
    }
}
```

---

### BazaarChatEventHandler

**Package:** `com.github.mkram17.bazaarutils.events.handlers`

**Description:** Handles all `BazaarChatEvent` occurrences and performs various actions based on the event type.

**Key Responsibilities:**
- Track order creation and add orders to watch lists
- Play notification sounds when orders are filled
- Update the order limit tracker for instant transactions
- Mark orders as filled in the internal tracking system

**Handler Methods:**
- `onAnyOrder()` - Fires for any bazaar chat event, sends notifications
- `onOrderCreated()` - Handles order creation, updates limits and watch list
- `onInstaSell()` - Handles instant sell, updates order limits (considers tax)
- `onInstaBuy()` - Handles instant buy, updates order limits
- `onOrderFilled()` - Handles filled orders, plays sounds and updates status

**Constants:**
- `ORDER_FILLED_NOTIFICATIONS = 2` - Number of notification sounds to play

**Registration:** Automatically registered via `@RunOnInit` annotation

---

### ChatHandler

**Package:** `com.github.mkram17.bazaarutils.events.handlers`

**Description:** Parses and processes bazaar-related chat messages from the game, converting them into `BazaarChatEvent` instances.

**Supported Message Types:**
- Buy Order Setup / Sell Offer Setup
- Order Filled
- Order Claimed
- Order Cancelled
- Order Flipped
- Instant Sell
- Instant Buy

**Key Methods:**
- `registerBazaarChat()` - Registers the chat listener (called via `@RunOnInit`)
- `getMessageType()` - Determines the type of bazaar event from a message
- `parseOrderData()` - Parses order information from message components
- `handleOrderCreated()` - Processes order creation messages
- `handleFilled()` - Processes order filled messages
- `handleClaimed()` - Processes order claim messages (buy and sell)
- `handleInstaSell()` - Processes instant sell messages
- `handleInstaBuy()` - Processes instant buy messages
- `handleFlip()` - Processes order flip messages
- `handleCancelled()` - Processes order cancellation messages

**Configuration:**
- `createOrderFilledSoundOption()` - Creates YACL config option for notification sounds

**Registration:** Automatically registered via `@RunOnInit` annotation on `registerBazaarChat()`

**Important Notes:**
- Uses Fabric's `ClientReceiveMessageEvents.GAME` to intercept chat messages
- Parses message components (siblings) to extract order information
- Handles different message formats for buy orders vs sell orders
- Posts events to the event bus after successful parsing

---

## Usage Examples

### Example 1: Adding a Custom Order Notification

```java
public class CustomOrderNotifier {
    @RunOnInit
    public static void subscribe() {
        EVENT_BUS.subscribe(CustomOrderNotifier.class);
    }
    
    @EventHandler
    public void onOrderCreated(BazaarChatEvent<?> event) {
        if (event.type() == BazaarChatEvent.BazaarEventTypes.ORDER_CREATED) {
            OrderInfoContainer order = event.order();
            sendDiscordWebhook("New order: " + order.getName() + 
                " x" + order.getVolume() + 
                " @ " + order.getPricePerItem());
        }
    }
}
```

### Example 2: Preventing Accidental Clicks

```java
public class ClickProtection {
    @RunOnInit
    public static void subscribe() {
        EVENT_BUS.subscribe(ClickProtection.class);
    }
    
    @EventHandler
    public void onSlotClick(SlotClickEvent event) {
        ItemStack item = event.slot.getStack();
        if (isValuableItem(item) && !hasConfirmed()) {
            event.setCancelled(true);
            showConfirmationDialog();
        }
    }
}
```

### Example 3: Tracking Market Data

```java
public class MarketTracker {
    private Map<String, Double> priceHistory = new HashMap<>();
    
    @RunOnInit
    public static void subscribe() {
        EVENT_BUS.subscribe(MarketTracker.class);
    }
    
    @EventHandler
    public void onBazaarDataUpdate(BazaarDataUpdateEvent event) {
        SkyBlockBazaarReply reply = event.getBazaarReply();
        reply.getProducts().forEach((itemId, product) -> {
            double buyPrice = product.getQuickStatus().getBuyPrice();
            priceHistory.put(itemId, buyPrice);
        });
    }
}
```

### Example 4: Custom Item Display

```java
public class CustomItemDisplay {
    @RunOnInit
    public static void subscribe() {
        EVENT_BUS.subscribe(CustomItemDisplay.class);
    }
    
    @EventHandler
    public void onReplaceItem(ReplaceItemEvent event) {
        ItemStack original = event.getOriginal();
        ItemStack modified = original.copy();
        
        // Add custom lore
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("Custom Info: " + calculateProfit(original)));
        
        // Apply the lore to the modified item
        modified.setLore(lore);
        
        // Set the modified item as replacement
        event.setReplacement(modified);
}
```

## Best Practices

### 1. Always Register Handlers

Use the `@RunOnInit` annotation to ensure your handlers are registered during mod initialization:

```java
@RunOnInit
public static void subscribe() {
    EVENT_BUS.subscribe(YourHandler.class);
}
```

### 2. Handle Null Values

Many events can have null fields. Always check before using:

```java
@EventHandler
public void onScreenChange(ScreenChangeEvent event) {
    if (event.getNewScreen() == null) {
        return; // Screen is closing
    }
    // Process screen
}
```

### 3. Be Careful with Event Cancellation

Only cancel events when necessary, and document why:

```java
@EventHandler
public void onSlotClick(SlotClickEvent event) {
    // Cancel to prevent accidental item deletion
    if (isDangerousAction(event)) {
        event.setCancelled(true);
    }
}
```

### 4. Keep Handlers Lightweight

Event handlers should be fast. Move heavy processing to async tasks:

```java
@EventHandler
public void onBazaarDataUpdate(BazaarDataUpdateEvent event) {
    CompletableFuture.runAsync(() -> {
        processLargeDataset(event.getBazaarReply());
    });
}
```

### 5. Use Specific Event Types

Listen for the most specific event type you need:

```java
// Good - specific handling
@EventHandler
public void onOrderFilled(BazaarChatEvent<?> event) {
    if (event.type() == BazaarChatEvent.BazaarEventTypes.ORDER_FILLED) {
        // Handle only filled orders
    }
}
```

### 6. Document Your Handlers

Always document what your handler does and why:

```java
/**
 * Tracks profit from filled orders and updates statistics.
 * This helps users understand their trading performance.
 */
@EventHandler
public void onOrderFilled(BazaarChatEvent<?> event) {
    // Implementation
}
```

### 7. Error Handling

Always handle potential errors gracefully:

```java
@EventHandler
public void onChatEvent(BazaarChatEvent<?> event) {
    try {
        processOrder(event.order());
    } catch (Exception e) {
        Util.notifyError("Failed to process order", e);
    }
}
```

### 8. Testing

Test your event handlers with different scenarios:

- Normal cases
- Edge cases (null values, empty lists)
- Error conditions
- Concurrent events

---

## Additional Resources

- [Orbit Event System Documentation](https://github.com/MeteorDevelopment/orbit)
- [Fabric API Documentation](https://fabricmc.net/wiki/documentation:fabric_api)
- [Bazaar Utils Source Code](https://github.com/mkram17/Bazaar-Utils)

---

## Contributing

When adding new events or handlers:

1. Follow the existing patterns and conventions
2. Add comprehensive JavaDoc documentation
3. Update this documentation file
4. Add usage examples if the event/handler is complex
5. Test thoroughly before submitting a PR

For questions or issues, please visit the [Discord server](https://discord.gg/xDKjvm5hQd).
