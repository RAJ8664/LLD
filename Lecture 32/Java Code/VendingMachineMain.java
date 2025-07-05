// Abstract State Interface
interface VendingState {
    VendingState insertCoin(VendingMachine machine, int coin);
    VendingState selectItem(VendingMachine machine);
    VendingState dispense(VendingMachine machine);
    VendingState returnCoin(VendingMachine machine);
    VendingState refill(VendingMachine machine, int quantity);
    String getStateName();
}

// Context Class - Vending Machine
class VendingMachine {
    private VendingState currentState;
    private int itemCount;
    private int itemPrice;
    private int insertedCoins;
    
    // State objects (we'll initialize these)
    private VendingState noCoinState;
    private VendingState hasCoinState;
    private VendingState dispenseState;
    private VendingState soldOutState;
    
    public VendingMachine(int itemCount, int itemPrice) {
        this.itemCount = itemCount;
        this.itemPrice = itemPrice;
        this.insertedCoins = 0; 
        
        // Create state objects
        noCoinState = new NoCoinState();
        hasCoinState = new HasCoinState();
        dispenseState = new DispenseState();
        soldOutState = new SoldOutState();
        
        // Set initial state
        if (itemCount > 0) {
            currentState = noCoinState;
        } else {
            currentState = soldOutState;
        }
    }
    
    // Delegate to current state and update state based on return value
    public void insertCoin(int coin) {
        currentState = currentState.insertCoin(this, coin);
    }
    
    public void selectItem() {
        currentState = currentState.selectItem(this);
    }
    
    public void dispense() {
        currentState = currentState.dispense(this);
    }
    
    public void returnCoin() {
        currentState = currentState.returnCoin(this);
    }
    
    public void refill(int quantity) {
        currentState = currentState.refill(this, quantity);
    }
        
    // Print the status of Vending Machine
    public void printStatus() {
        System.out.println("\n--- Vending Machine Status ---");
        System.out.println("Items remaining: " + itemCount);
        System.out.println("Inserted coin: Rs " + insertedCoins);
        System.out.println("Current state: " + currentState.getStateName() + "\n");
    }
    
    // Getters for states
    public VendingState getNoCoinState() { 
        return noCoinState;
    }
    public VendingState getHasCoinState() { 
        return hasCoinState;
    }
    public VendingState getDispenseState() { 
        return dispenseState; 
    }
    public VendingState getSoldOutState() { 
        return soldOutState;
    }
    
    // Data access methods
    public int getItemCount() { 
        return itemCount; 
    }
    public void decrementItemCount() { 
        itemCount--; 
    }
    public void incrementItemCount(int count) {
        itemCount += count;
    }
    public void incrementItemCount() {
        itemCount += 1;
    }
    public int getInsertedCoin() { 
        return insertedCoins;
    }
    public void setInsertedCoin(int coin) { 
        insertedCoins = coin;
    }
    public void addCoin(int coin) { 
        insertedCoins += coin;
    }
    public int getPrice() {
        return this.itemPrice;
    }
    public void setPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }
}

// Concrete State: No Coin Inserted
class NoCoinState implements VendingState {
    public VendingState insertCoin(VendingMachine machine, int coin) {
        machine.setInsertedCoin(coin); // Rs 10
        System.out.println("Coin inserted. Current balance: Rs " + coin);
        return machine.getHasCoinState(); // Transition to HasCoinState
    }
    
    public VendingState selectItem(VendingMachine machine) {
        System.out.println("Please insert coin first!");
        return machine.getNoCoinState(); // Stay in same state
    }
    
    public VendingState dispense(VendingMachine machine) {
        System.out.println("Please insert coin and select item first!");
        return machine.getNoCoinState(); // Stay in same state
    }
    
    public VendingState returnCoin(VendingMachine machine) {
        System.out.println("No coin to return!");
        return machine.getNoCoinState(); // Stay in same state
    }

    public VendingState refill(VendingMachine machine, int quantity) {
        System.out.println("Items refilling");
        machine.incrementItemCount(quantity);
        return machine.getNoCoinState(); // Stay in same state
    }
    
    public String getStateName() {
        return "NO_COIN";
    }
}

// Concrete State: Coin Inserted
class HasCoinState implements VendingState {
    public VendingState insertCoin(VendingMachine machine, int coin) {
        machine.addCoin(coin);
        System.out.println("Additional coin inserted. Current balance: Rs " + machine.getInsertedCoin());
        return machine.getHasCoinState(); // Stay in same state
    }
    
    public VendingState selectItem(VendingMachine machine) {
        if (machine.getInsertedCoin() >= machine.getPrice()) {
            System.out.println("Item selected. Dispensing...");
            
            int change = machine.getInsertedCoin() - machine.getPrice();
            if (change > 0) {
                System.out.println("Change returned: Rs " + change);
            }
            machine.setInsertedCoin(0);
            
            return machine.getDispenseState(); // Transition to DispenseState
        } 
        else {
            int needed = machine.getPrice() - machine.getInsertedCoin();
            System.out.println("Insufficient funds. Need Rs " + needed + " more.");
            return machine.getHasCoinState(); // Stay in same state
        }
    }
    
    public VendingState dispense(VendingMachine machine) {
        System.out.println("Please select an item first!");
        return machine.getHasCoinState(); // Stay in same state
    }
    
    public VendingState returnCoin(VendingMachine machine) {
        System.out.println("Coin returned: Rs " + machine.getInsertedCoin());
        machine.setInsertedCoin(0);
        return machine.getNoCoinState(); // Transition to NoCoinState
    }

    public VendingState refill(VendingMachine machine, int quantity) {
        System.out.println("Can't refil in this state");
        return machine.getHasCoinState(); // Stay in same state
    }
    
    public String getStateName() {
        return "HAS_COIN";
    }
}

// Concrete State: Item Sold
class DispenseState implements VendingState {
    public VendingState insertCoin(VendingMachine machine, int coin) {
        System.out.println("Please wait, already dispensing item. Coin returned: Rs " + coin);
        return machine.getDispenseState();  // Stay in same state
    }
    
    public VendingState selectItem(VendingMachine machine) {
        System.out.println("Already dispensing item. Please wait.");
        return machine.getDispenseState(); // Stay in same state
    }
    
    public VendingState dispense(VendingMachine machine) {
        System.out.println("Item dispensed!");
        machine.decrementItemCount();
        
        if (machine.getItemCount() > 0) {
            return machine.getNoCoinState(); // Transition to NoCoinState
        } 
        else {
            System.out.println("Machine is now sold out!");
            return machine.getSoldOutState(); // Transition to SoldOutState
        }
    }
    
    public VendingState returnCoin(VendingMachine machine) {
        System.out.println("Cannot return coin while dispensing item!");
        return machine.getDispenseState(); // Stay in same state
    }

    public VendingState refill(VendingMachine machine, int quantity) {
        System.out.println("Can't refil in this state");
        return machine.getDispenseState(); // Stay in same state
    }

    public String getStateName() {
        return "DISPENSING";
    }
}

// Concrete State: Sold Out
class SoldOutState implements VendingState {
    public VendingState insertCoin(VendingMachine machine, int coin) {
        System.out.println("Machine is sold out. Coin returned: Rs " + coin);
        return machine.getSoldOutState(); // Stay in same state
    }
    
    public VendingState selectItem(VendingMachine machine) {
        System.out.println("Machine is sold out!");
        return machine.getSoldOutState(); // Stay in same state
    }
    
    public VendingState dispense(VendingMachine machine) {
        System.out.println("Machine is sold out!");
        return machine.getSoldOutState(); // Stay in same state
    }
    
    public VendingState returnCoin(VendingMachine machine) {
        System.out.println("Machine is sold out. No coin inserted.");
        return machine.getSoldOutState(); // Stay in same state
    }

    public VendingState refill(VendingMachine machine, int quantity) {
        System.out.println("Items refilling");
        machine.incrementItemCount(quantity);
        return machine.getNoCoinState();
    }
    
    public String getStateName() {
        return "SOLD_OUT";
    }
}

// Main class for Vending Machine
public class VendingMachineMain {
    public static void main(String[] args) {
        System.out.println("=== Water Bottle VENDING MACHINE ===");
        
        int itemCount = 2;
        int itemPrice = 20;

        VendingMachine machine = new VendingMachine(itemCount, itemPrice);
        machine.printStatus();
        
        // Test scenarios - each operation potentially changes state
        System.out.println("1. Trying to select item without coin:");
        machine.selectItem();  // Should ask for coin, no state change
        machine.printStatus();
        
        System.out.println("2. Inserting coin:");
        machine.insertCoin(10);  // State changes to HAS_COIN
        machine.printStatus();
        
        System.out.println("3. Selecting item with insufficient funds:");
        machine.selectItem();  // Insufficient funds, stays in HAS_COIN
        machine.printStatus();
        
        System.out.println("4. Adding more coins:");
        machine.insertCoin(10);  // Add more money, stays in HAS_COIN
        machine.printStatus();
        
        System.out.println("5. Selecting item Now");
        machine.selectItem();  // State changes to SOLD
        machine.printStatus();
        
        System.out.println("6. Dispensing item:");
        machine.dispense(); // State changes to NO_COIN (items remaining)
        machine.printStatus();
        
        System.out.println("7. Buying last item:");
        machine.insertCoin(20);  // State changes to HAS_COIN
        machine.selectItem();  // State changes to SOLD
        machine.dispense(); // State changes to SOLD_OUT (no items left)
        machine.printStatus();
        
        System.out.println("8. Trying to use sold out machine:");
        machine.insertCoin(5);  // Coin returned, stays in SOLD_OUT

        System.out.println("9. Trying to use sold out machine:");
        machine.refill(2);
        machine.printStatus(); // State changes NO_COIN
    }
}
