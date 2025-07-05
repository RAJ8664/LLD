import java.util.*;
import java.lang.Math;

/////////////////////////////////////////////
// Product & Factory
/////////////////////////////////////////////

class Product {
    private int sku;
    private String name;
    private double price;

    public Product(int id, String nm, double pr) {
        sku   = id;
        name  = nm;
        price = pr;
    }

    // Getters & Setters
    public int getSku() {
        return this.sku;
    }

    public String getName() {
        return this.name;
    }

    public double getPrice() {
        return this.price;
    }
}

class ProductFactory {
    public static Product createProduct(int sku) {
        // In reality product comes from DB
        String name;
        double price;

        if (sku == 101) {
            name  = "Apple";
            price = 20;
        }
        else if (sku == 102) {
            name  = "Banana";
            price = 10;
        }
        else if (sku == 103) {
            name  = "Chocolate";
            price = 50;
        }
        else if (sku == 201) {
            name  = "T-Shirt";
            price = 500;
        }
        else if (sku == 202) {
            name  = "Jeans";
            price = 1000;
        }
        else {
            name  = "Item" + sku;
            price = 100;
        }
        return new Product(sku, name, price);
    }
}

/////////////////////////////////////////////
// InventoryStore (Interface) & DbInventoryStore
/////////////////////////////////////////////

interface InventoryStore {
    void addProduct(Product prod, int qty);
    void removeProduct(int sku, int qty);
    int checkStock(int sku);
    List<Product> listAvailableProducts();
}

class DbInventoryStore implements InventoryStore {
    private Map<Integer,Integer> stock;         // SKU -> quantity
    private Map<Integer,Product> products;      // SKU -> Product

    public DbInventoryStore() {
        stock    = new HashMap<>();
        products = new HashMap<>();
    }

    @Override
    public void addProduct(Product prod, int qty) {
        int sku = prod.getSku();
        if (!products.containsKey(sku)) {
            products.put(sku, prod);
        }
        // else drop the extra prod instance
        stock.put(sku, stock.getOrDefault(sku, 0) + qty);
    }

    @Override
    public void removeProduct(int sku, int qty) {
        if (!stock.containsKey(sku)) 
            return;

        int currentQuantity   = stock.get(sku);
        int remainingQuantity = currentQuantity - qty;
        if (remainingQuantity > 0) {
            stock.put(sku, remainingQuantity);
        } else {
            stock.remove(sku);
            products.remove(sku);
        }
    }

    @Override
    public int checkStock(int sku) {
        return stock.getOrDefault(sku, 0);
    }

    @Override
    public List<Product> listAvailableProducts() {
        List<Product> available = new ArrayList<>();
        for (Map.Entry<Integer,Integer> it : stock.entrySet()) {
            int sku = it.getKey();
            int qty = it.getValue();
            if (qty > 0 && products.containsKey(sku)) {
                available.add(products.get(sku));
            }
        }
        return available;
    }
}

/////////////////////////////////////////////
// InventoryManager 
/////////////////////////////////////////////

class InventoryManager {
    private InventoryStore store;

    public InventoryManager(InventoryStore store) {
        this.store = store;
    }

    public void addStock(int sku, int qty) {
        Product prod = ProductFactory.createProduct(sku);
        store.addProduct(prod, qty);
        System.out.println("[InventoryManager] Added SKU " + sku + " Qty " + qty);
    }

    public void removeStock(int sku, int qty) {
        store.removeProduct(sku, qty);
    }

    public int checkStock(int sku) {
        return store.checkStock(sku);
    }

    public List<Product> getAvailableProducts() {
        return store.listAvailableProducts();
    }
}

/////////////////////////////////////////////
// Replenishment Strategy (Strategy Pattern)
/////////////////////////////////////////////

interface ReplenishStrategy {
    void replenish(InventoryManager manager, Map<Integer,Integer> itemsToReplenish);
}

class ThresholdReplenishStrategy implements ReplenishStrategy {
    private int threshold;

    public ThresholdReplenishStrategy(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void replenish(InventoryManager manager, Map<Integer,Integer> itemsToReplenish) {
        System.out.println("[ThresholdReplenish] Checking threshold...");
        for (Map.Entry<Integer,Integer> it : itemsToReplenish.entrySet()) {
            int sku       = it.getKey();
            int qtyToAdd  = it.getValue();
            int current   = manager.checkStock(sku);
            if (current < threshold) {
                manager.addStock(sku, qtyToAdd);
                System.out.println("  -> SKU " + sku + " was " + current 
                                 + ", replenished by " + qtyToAdd);
            }
        }
    }
}

class WeeklyReplenishStrategy implements ReplenishStrategy {
    public WeeklyReplenishStrategy() {}

    @Override
    public void replenish(InventoryManager manager, Map<Integer,Integer> itemsToReplenish) {
        System.out.println("[WeeklyReplenish] Weekly replenishment triggered for inventory.");
    }
}

/////////////////////////////////////////////
// DarkStore (formerly Warehouse)
/////////////////////////////////////////////

class DarkStore {
    private String name;
    private double x, y;                       // location coordinates
    private InventoryManager inventoryManager;
    private ReplenishStrategy replenishStrategy;

    public DarkStore(String n, double x_coord, double y_coord) {
        name = n;
        x    = x_coord;
        y    = y_coord;

        // We could have made another factory called InventoryStoreFactory to get
        // DbInventoryStore by enum and hence make it loosely coupled.
        inventoryManager = new InventoryManager(new DbInventoryStore());
    }

    public double distanceTo(double ux, double uy) {
        return Math.sqrt((x - ux)*(x - ux) + (y - uy)*(y - uy));
    }

    public void runReplenishment(Map<Integer,Integer> itemsToReplenish) {
        if (replenishStrategy != null) {
            replenishStrategy.replenish(inventoryManager, itemsToReplenish);
        }
    }

    // Delegation Methods
    public List<Product> getAllProducts() {
        return inventoryManager.getAvailableProducts();
    }

    public int checkStock(int sku) {
        return inventoryManager.checkStock(sku);
    }

    public void removeStock(int sku, int qty) {
        inventoryManager.removeStock(sku, qty);
    }

    public void addStock(int sku, int qty) {
        inventoryManager.addStock(sku, qty);
    }

    // Getters & Setters
    public void setReplenishStrategy(ReplenishStrategy strategy) {
        this.replenishStrategy = strategy;
    }

    public String getName() {
        return this.name;
    }

    public double getXCoordinate() {
        return this.x;
    }

    public double getYCoordinate() {
        return this.y;
    }

    public InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }
}

/////////////////////////////////////////////
// DarkStoreManager (Singleton)
/////////////////////////////////////////////

class DarkStoreManager {
    private static DarkStoreManager instance;
    private List<DarkStore> darkStores;

    private DarkStoreManager() {
        darkStores = new ArrayList<>();
    }

    public static DarkStoreManager getInstance() {
        if (instance == null) {
            instance = new DarkStoreManager();
        }
        return instance;
    }

    public void registerDarkStore(DarkStore ds) {
        darkStores.add(ds);
    }

    public List<DarkStore> getNearbyDarkStores(double ux, double uy, double maxDistance) {
        List<Pair<Double,DarkStore>> distList = new ArrayList<>();
        for (DarkStore ds : darkStores) {
            double d = ds.distanceTo(ux, uy);
            if (d <= maxDistance) {
                distList.add(new Pair<>(d, ds));
            }
        }
        distList.sort(Comparator.comparing(Pair::getKey));
        List<DarkStore> result = new ArrayList<>();
        for (Pair<Double,DarkStore> p : distList) {
            result.add(p.getValue());
        }
        return result;
    }
}

// Simple helper Pair class (since JavaFX/Android Pair might not be available)
class Pair<K,V> {
    private K key; private V value;
    public Pair(K k, V v) { key = k; value = v; }
    public K getKey()   { return key; }
    public V getValue() { return value; }
}

/////////////////////////////////////////////
// User & Cart
/////////////////////////////////////////////

class Cart {
    public List<Pair<Product,Integer>> items = new ArrayList<>();

    public void addItem(int sku, int qty) {
        Product prod = ProductFactory.createProduct(sku);
        items.add(new Pair<>(prod, qty));
        System.out.println("[Cart] Added SKU " + sku + " (" + prod.getName() 
                         + ") x" + qty);
    }

    public double getTotal() {
        double sum = 0.0;
        for (Pair<Product,Integer> it : items) {
            sum += (it.getKey().getPrice() * it.getValue());
        }
        return sum;
    }

    public List<Pair<Product,Integer>> getItems() {
        return items;
    }
}

class User {
    public String name;
    public double x, y;
    private Cart cart;  // User owns a cart

    public User(String n, double x_coord, double y_coord) {
        name = n;
        x    = x_coord;
        y    = y_coord;
        cart = new Cart();
    }

    public Cart getCart() {
        return cart;
    }
}

/////////////////////////////////////////////
// DeliveryPartner
/////////////////////////////////////////////

class DeliveryPartner {
    public String name;
    public DeliveryPartner(String n) {
        name = n;
    }
}

/////////////////////////////////////////////
// Order & OrderManager (Singleton)
/////////////////////////////////////////////

class Order {
    private static int nextId = 1;
    public int orderId;
    public User user;
    public List<Pair<Product,Integer>> items = new ArrayList<>();
    public List<DeliveryPartner> partners = new ArrayList<>();
    public double totalAmount;

    public Order(User u) {
        orderId = nextId++;
        user    = u;
        totalAmount = 0.0;
    }
}

class OrderManager {
    private static OrderManager instance;
    private List<Order> orders;

    private OrderManager() {
        orders = new ArrayList<>();
    }

    public static OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }

    public void placeOrder(User user, Cart cart) {
        System.out.println("\n[OrderManager] Placing Order for: " + user.name);

        List<Pair<Product,Integer>> requestedItems = cart.getItems();

        // 1) Find nearby dark stores within 5 KM
        double maxDist = 5.0;
        List<DarkStore> nearbyDarkStores = 
            DarkStoreManager.getInstance().getNearbyDarkStores(user.x, user.y, maxDist);

        if (nearbyDarkStores.isEmpty()) {
            System.out.println("  No dark stores within 5 KM. Cannot fulfill order.");
            return;
        }

        // 2) Check if closest store has all items
        DarkStore firstStore = nearbyDarkStores.get(0);
        boolean allInFirst = true;
        for (Pair<Product,Integer> item : requestedItems) {
            int sku = item.getKey().getSku();
            int qty = item.getValue();
            if (firstStore.checkStock(sku) < qty) {
                allInFirst = false;
                break;
            }
        }

        Order order = new Order(user);

        // One delivery partner required...
        if (allInFirst) {
            System.out.println("  All items at: " + firstStore.getName());

            for (Pair<Product,Integer> item : requestedItems) {
                int sku = item.getKey().getSku();
                int qty = item.getValue();
                firstStore.removeStock(sku, qty);
                order.items.add(new Pair<>(item.getKey(), qty));
            }

            order.totalAmount = cart.getTotal();
            order.partners.add(new DeliveryPartner("Partner1"));
            System.out.println("  Assigned Delivery Partner: Partner1");
        }

        // Multiple delivery partners required
        else {
            System.out.println("  Splitting order across stores...");
            Map<Integer,Integer> allItems = new HashMap<>();
            for (Pair<Product,Integer> item : requestedItems) {
                allItems.put(item.getKey().getSku(), item.getValue());
            }

            int partnerId = 1;
            for (DarkStore store : nearbyDarkStores) {
                if (allItems.isEmpty()) break;
                System.out.println("   Checking: " + store.getName());
                List<Integer> toErase = new ArrayList<>();
                for (Map.Entry<Integer,Integer> entry : allItems.entrySet()) {
                    int sku = entry.getKey();
                    int qtyNeeded = entry.getValue();
                    int availableQty = store.checkStock(sku);
                    if (availableQty <= 0) continue;
                    int takenQty = Math.min(availableQty, qtyNeeded);
                    store.removeStock(sku, takenQty);
                    System.out.println("     " + store.getName() + " supplies SKU " + sku 
                                     + " x" + takenQty);
                    order.items.add(new Pair<>(ProductFactory.createProduct(sku), takenQty));
                    if (qtyNeeded > takenQty) {
                        allItems.put(sku, qtyNeeded - takenQty);
                    } else {
                        toErase.add(sku);
                    }
                }
                for (int sku : toErase) {
                    allItems.remove(sku);
                }
                if (!toErase.isEmpty()) {
                    String pname = "Partner" + partnerId++;
                    order.partners.add(new DeliveryPartner(pname));
                    System.out.println("     Assigned: " + pname + " for " + store.getName());
                }
            }
            if (!allItems.isEmpty()) {
                System.out.println("  Could not fulfill:");
                for (Map.Entry<Integer,Integer> entry : allItems.entrySet()) {
                    System.out.println("    SKU " + entry.getKey() 
                                     + " x" + entry.getValue());
                }
            }
            double sum = 0;
            for (Pair<Product,Integer> it : order.items) {
                sum += it.getKey().getPrice() * it.getValue();
            }
            order.totalAmount = sum;
        }

        // Printing Order Summary
        System.out.println("\n[OrderManager] Order #" + order.orderId + " Summary:");
        System.out.println("  User: " + user.name + "\n  Items:");
        for (Pair<Product,Integer> item : order.items) {
            System.out.println("    SKU " + item.getKey().getSku()
                             + " (" + item.getKey().getName() + ") x" + item.getValue()
                             + " @ ₹" + item.getKey().getPrice());
        }
        System.out.println("  Total: ₹" + order.totalAmount + "\n  Partners:");
        for (DeliveryPartner dp : order.partners) {
            System.out.println("    " + dp.name);
        }
        System.out.println();

        orders.add(order);
    }

    public List<Order> getAllOrders() {
        return orders;
    }
}

/////////////////////////////////////////////
// Zepto Initialization & Main
/////////////////////////////////////////////

class ZeptoHelper {
    public static void showAllItems(User user) {
        System.out.println("\n[Zepto] All Available products within 5 KM for " + user.name + ":");
        DarkStoreManager dsManager = DarkStoreManager.getInstance();
        List<DarkStore> nearbyStores = dsManager.getNearbyDarkStores(user.x, user.y, 5.0);
        Map<Integer,Double> skuToPrice = new HashMap<>();
        Map<Integer,String>  skuToName  = new HashMap<>();

        for (DarkStore ds : nearbyStores) {
            for (Product product : ds.getAllProducts()) {
                int sku = product.getSku();
                if (!skuToPrice.containsKey(sku)) {
                    skuToPrice.put(sku, product.getPrice());
                    skuToName.put(sku, product.getName());
                }
            }
        }

        for (Map.Entry<Integer,Double> entry : skuToPrice.entrySet()) {
            System.out.println("  SKU " + entry.getKey() + " - " 
                               + skuToName.get(entry.getKey()) 
                               + " @ ₹" + entry.getValue());
        }
    }

    public static void initialize() {
        DarkStoreManager dsManager = DarkStoreManager.getInstance();

        // DarkStore A.......
        DarkStore darkStoreA = new DarkStore("DarkStoreA", 0.0, 0.0);
        darkStoreA.setReplenishStrategy(new ThresholdReplenishStrategy(3));
        System.out.println("\nAdding stocks in DarkStoreA....");
        darkStoreA.addStock(101, 5);
        darkStoreA.addStock(102, 2);

        // DarkStore B.......
        DarkStore darkStoreB = new DarkStore("DarkStoreB", 4.0, 1.0);
        darkStoreB.setReplenishStrategy(new ThresholdReplenishStrategy(3));
        System.out.println("\nAdding stocks in DarkStoreB....");
        darkStoreB.addStock(101, 3);
        darkStoreB.addStock(103, 10);

        // DarkStore C.......
        DarkStore darkStoreC = new DarkStore("DarkStoreC", 2.0, 3.0);
        darkStoreC.setReplenishStrategy(new ThresholdReplenishStrategy(3));
        System.out.println("\nAdding stocks in DarkStoreC....");
        darkStoreC.addStock(102, 5);
        darkStoreC.addStock(201, 7);

        dsManager.registerDarkStore(darkStoreA);
        dsManager.registerDarkStore(darkStoreB);
        dsManager.registerDarkStore(darkStoreC);
    }
}

public class ZeptoClone {
    public static void main(String[] args) {
        // 1) Initialize.
        ZeptoHelper.initialize();

        // 2) A User comes on Platform
        User user = new User("Aditya", 1.0, 1.0);
        System.out.println("\nUser with name " + user.name + " comes on platform");

        // 3) Show all available items via Zepto
        ZeptoHelper.showAllItems(user);

        // 4) User adds items to cart
        System.out.println("\nAdding items to cart");
        Cart cart = user.getCart();
        cart.addItem(101, 4);
        cart.addItem(102, 3);
        cart.addItem(103, 2);

        // 5) Place Order
        OrderManager.getInstance().placeOrder(user, cart);

        System.out.println("\n=== Demo Complete ===");
    }
}
