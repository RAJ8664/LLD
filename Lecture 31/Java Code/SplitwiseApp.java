import java.util.*;
import java.text.DecimalFormat;

// Forward declarations equivalent - not needed in Java due to automatic resolution

enum SplitType {
    EQUAL,
    EXACT,
    PERCENTAGE
}

class Split {
    public String userId;
    public double amount;
    
    public Split(String userId, double amount) {
        this.userId = userId;
        this.amount = amount;
    }
}

// Observer Pattern - Notification interface
interface Observer {
    void update(String message);
}

// Strategy Pattern - Split strategies
interface SplitStrategy {
    List<Split> calculateSplit(double totalAmount, List<String> userIds, List<Double> values);
}

class EqualSplit implements SplitStrategy {
    @Override
    public List<Split> calculateSplit(double totalAmount, List<String> userIds, List<Double> values) {
        List<Split> splits = new ArrayList<>();
        double amountPerUser = totalAmount / userIds.size();
        
        for (String userId : userIds) {
            splits.add(new Split(userId, amountPerUser));
        }
        return splits;
    }
}

class ExactSplit implements SplitStrategy {
    @Override
    public List<Split> calculateSplit(double totalAmount, List<String> userIds, List<Double> values) {
        List<Split> splits = new ArrayList<>();

        //validations
        
        for (int i = 0; i < userIds.size(); i++) {
            splits.add(new Split(userIds.get(i), values.get(i)));
        }
        return splits;
    }
}

class PercentageSplit implements SplitStrategy {
    @Override
    public List<Split> calculateSplit(double totalAmount, List<String> userIds, List<Double> values) {
        List<Split> splits = new ArrayList<>();

        //validations
        
        for (int i = 0; i < userIds.size(); i++) {
            double amount = (totalAmount * values.get(i)) / 100.0;
            splits.add(new Split(userIds.get(i), amount));
        }
        return splits;
    }
}

// Factory for split strategies
class SplitFactory {
    public static SplitStrategy getSplitStrategy(SplitType type) {
        switch (type) {
            case EQUAL:
                return new EqualSplit();
            case EXACT:
                return new ExactSplit();
            case PERCENTAGE:
                return new PercentageSplit();
            default:
                return new EqualSplit();
        }
    }
}

// User class --> Concrete Observer
class User implements Observer {
    public static int nextUserId = 0;
    public String userId;
    public String name;
    public String email;
    public Map<String, Double> balances; // userId -> amount (positive = they owe you, negative = you owe them)
    
    public User(String name, String email) {
        this.userId = "user" + (++nextUserId);
        this.name = name;
        this.email = email;
        this.balances = new HashMap<>();
    }
    
    @Override
    public void update(String message) {
        System.out.println("[NOTIFICATION to " + name + "]: " + message);
    }
    
    public void updateBalance(String otherUserId, double amount) {
        balances.put(otherUserId, balances.getOrDefault(otherUserId, 0.0) + amount);
        
        // Remove if balance becomes zero
        if (Math.abs(balances.get(otherUserId)) < 0.01) {
            balances.remove(otherUserId);
        }
    }
    
    public double getTotalOwed() {
        double total = 0;
        for (Map.Entry<String, Double> balance : balances.entrySet()) {
            if (balance.getValue() < 0) {
                total += Math.abs(balance.getValue());
            }
        }
        return total;
    }
    
    public double getTotalOwing() {
        double total = 0;
        for (Map.Entry<String, Double> balance : balances.entrySet()) {
            if (balance.getValue() > 0) {
                total += balance.getValue();
            }
        }
        return total;
    }
}

// Expense Model class
class Expense {
    public static int nextExpenseId = 0;
    public String expenseId;
    public String description;
    public double totalAmount;
    public String paidByUserId;
    public List<Split> splits;
    public String groupId;
    
    public Expense(String desc, double amount, String paidBy,
            List<Split> splits, String group) {
        this.expenseId = "expense" + (++nextExpenseId);
        this.description = desc;
        this.totalAmount = amount;
        this.paidByUserId = paidBy;
        this.splits = splits;
        this.groupId = group;
    }
    
    public Expense(String desc, double amount, String paidBy, List<Split> splits) {
        this(desc, amount, paidBy, splits, "");
    }
}

class DebtSimplifier {
    public static Map<String, Map<String, Double>> simplifyDebts(
        Map<String, Map<String, Double>> groupBalances) {
        
        // Calculate net amount for each person
        Map<String, Double> netAmounts = new HashMap<>();
        
        // Initialize all users with 0
        for (Map.Entry<String, Map<String, Double>> userBalance : groupBalances.entrySet()) {
            netAmounts.put(userBalance.getKey(), 0.0);
        }
        
        // Calculate net amounts
        // We only need to process each balance once (not twice)
        // If groupBalances[A][B] = 200, it means B owes A 200
        // So A should receive 200 (positive) and B should pay 200 (negative)
        for (Map.Entry<String, Map<String, Double>> userBalance : groupBalances.entrySet()) {
            String creditorId = userBalance.getKey();
            for (Map.Entry<String, Double> balance : userBalance.getValue().entrySet()) {
                String debtorId = balance.getKey();
                double amount = balance.getValue();
                
                // Only process positive amounts to avoid double counting
                if (amount > 0) {
                    netAmounts.put(creditorId, netAmounts.get(creditorId) + amount);  // creditor receives
                    netAmounts.put(debtorId, netAmounts.get(debtorId) - amount);    // debtor pays
                }
            }
        }
        
        // Divide users into creditors and debtors
        List<AbstractMap.SimpleEntry<String, Double>> creditors = new ArrayList<>(); // those who should receive money
        List<AbstractMap.SimpleEntry<String, Double>> debtors = new ArrayList<>();   // those who should pay money
        
        for (Map.Entry<String, Double> net : netAmounts.entrySet()) {
            if (net.getValue() > 0.01) { // creditor
                creditors.add(new AbstractMap.SimpleEntry<>(net.getKey(), net.getValue()));
            } else if (net.getValue() < -0.01) { // debtor
                debtors.add(new AbstractMap.SimpleEntry<>(net.getKey(), -net.getValue())); // store positive amount
            }
        }
        
        // Sort for better optimization (largest amounts first)
        creditors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        debtors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        // Create new simplified balance map
        Map<String, Map<String, Double>> simplifiedBalances = new HashMap<>();
        
        // Initialize empty maps for all users
        for (Map.Entry<String, Map<String, Double>> userBalance : groupBalances.entrySet()) {
            simplifiedBalances.put(userBalance.getKey(), new HashMap<>());
        }
        
        // Use greedy algorithm to minimize transactions
        int i = 0, j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            String creditorId = creditors.get(i).getKey();
            String debtorId = debtors.get(j).getKey();
            double creditorAmount = creditors.get(i).getValue();
            double debtorAmount = debtors.get(j).getValue();
            
            // Find the minimum amount to settle
            double settleAmount = Math.min(creditorAmount, debtorAmount);
            
            // Update simplified balances
            // debtorId owes creditorId the settleAmount
            simplifiedBalances.get(creditorId).put(debtorId, settleAmount);
            simplifiedBalances.get(debtorId).put(creditorId, -settleAmount);
            
            // Update remaining amounts
            creditors.get(i).setValue(creditors.get(i).getValue() - settleAmount);
            debtors.get(j).setValue(debtors.get(j).getValue() - settleAmount);
            
            // Move to next creditor or debtor if current one is settled
            if (creditors.get(i).getValue() < 0.01) {
                i++;
            }
            if (debtors.get(j).getValue() < 0.01) {
                j++;
            }
        }
        
        return simplifiedBalances;
    }
}

// Group class --> Concrete Observable
class Group {
    private User getUserByuserId(String userId) {
        User user = null;

        for(User member : members) {
            if(member.userId.equals(userId)) {
                user = member;
            }
        }
        return user;
    }
    
    public static int nextGroupId = 0;
    public String groupId;
    public String name;
    public List<User> members; //observers
    public Map<String, Expense> groupExpenses; // Group's own expense book
    public Map<String, Map<String, Double>> groupBalances; // memberId -> {otherMemberId -> balance}
    
    public Group(String name) {
        this.groupId = "group" + (++nextGroupId);
        this.name = name;
        this.members = new ArrayList<>();
        this.groupExpenses = new HashMap<>();
        this.groupBalances = new HashMap<>();
    }
    
    public void addMember(User user) {
        members.add(user);

        // Initialize balance map for new member
        groupBalances.put(user.userId, new HashMap<>());
        System.out.println(user.name + " added to group " + name);
    }
    
    public boolean removeMember(String userId) {    
        // Check if user can be removed or not
        if(!canUserLeaveGroup(userId)) {
            System.out.println("\nUser not allowed to leave group without clearing expenses");
            return false;
        }
        
        // Remove from observers
        members.removeIf(user -> user.userId.equals(userId));
        
        // Remove from group balances
        groupBalances.remove(userId);
        
        // Remove this user from other members' balance maps
        for (Map.Entry<String, Map<String, Double>> memberBalance : groupBalances.entrySet()) {
            memberBalance.getValue().remove(userId);
        }
        return true;
    }
    
    public void notifyMembers(String message) {
        for (Observer observer : members) {
            observer.update(message);
        }
    }

    public boolean isMember(String userId) {
        return groupBalances.containsKey(userId);
    }
    
    // Update balance within group
    public void updateGroupBalance(String fromUserId, String toUserId, double amount) {
        groupBalances.get(fromUserId).put(toUserId, 
            groupBalances.get(fromUserId).getOrDefault(toUserId, 0.0) + amount);
        groupBalances.get(toUserId).put(fromUserId, 
            groupBalances.get(toUserId).getOrDefault(fromUserId, 0.0) - amount);
        
        // Remove if balance becomes zero
        if (Math.abs(groupBalances.get(fromUserId).get(toUserId)) < 0.01) {
            groupBalances.get(fromUserId).remove(toUserId);
        }
        if (Math.abs(groupBalances.get(toUserId).get(fromUserId)) < 0.01) {
            groupBalances.get(toUserId).remove(fromUserId);
        }
    }
    
    // Check if user can leave group.
    public boolean canUserLeaveGroup(String userId) {
        if (!isMember(userId)) {
            throw new RuntimeException("user is not a part of this group");
        }
        
        // Check if user has any outstanding balance with other group members
        Map<String, Double> userBalanceSheet = groupBalances.get(userId);
        for (Map.Entry<String, Double> balance : userBalanceSheet.entrySet()) {
            if (Math.abs(balance.getValue()) > 0.01) {
                return false; // Has outstanding balance
            }
        }
        return true;
    }
    
    // Get user's balance within this group
    public Map<String, Double> getUserGroupBalances(String userId) {
        if (!isMember(userId)) {
            throw new RuntimeException("user is not a part of this group");
        }
        return groupBalances.get(userId);
    }
    
    // Add expense to this group
    public boolean addExpense(String description, double amount, String paidByUserId,
                   List<String> involvedUsers, SplitType splitType, 
                   List<Double> splitValues) {
        
        if (!isMember(paidByUserId)) {
            throw new RuntimeException("user is not a part of this group");
        }
        
        // Validate that all involved users are group members
        for (String userId : involvedUsers) {
            if (!isMember(userId)) {
                throw new RuntimeException("involvedUsers are not a part of this group");
            }
        }
        
        // Generate splits using strategy pattern
        List<Split> splits = SplitFactory.getSplitStrategy(splitType)
                                .calculateSplit(amount, involvedUsers, splitValues);
        
        // Create expense in group's own expense book
        Expense expense = new Expense(description, amount, paidByUserId, splits, groupId);
        groupExpenses.put(expense.expenseId, expense);
        
        // Update group balances
        for (Split split : splits) {
            if (!split.userId.equals(paidByUserId)) {
                // Person who paid gets positive balance, person who owes gets negative
                updateGroupBalance(paidByUserId, split.userId, split.amount);
            }
        }
        
        System.out.println("\n=========== Sending Notifications ====================");
        String paidByName = getUserByuserId(paidByUserId).name;
        notifyMembers("New expense added: " + description + " (Rs " + amount + ")");
        
        // Printing console message-------
        System.out.println("\n=========== Expense Message ====================");
        System.out.println("Expense added to " + name + ": " + description + " (Rs " + amount 
             + ") paid by " + paidByName +" and involved people are : ");
        if(!splitValues.isEmpty()) {
            for(int i=0; i<splitValues.size(); i++) {
                System.out.println(getUserByuserId(involvedUsers.get(i)).name + " : " + splitValues.get(i));
            }
        } 
        else {
            for(String user : involvedUsers) {
                System.out.print(getUserByuserId(user).name + ", ");
            }
            System.out.println("\nWill be Paid Equally");
        }    
        //-----------------------------------
        
        return true;
    }
    
    public boolean addExpense(String description, double amount, String paidByUserId,
                   List<String> involvedUsers, SplitType splitType) {
        return addExpense(description, amount, paidByUserId, involvedUsers, splitType, new ArrayList<>());
    }
    
    public boolean settlePayment(String fromUserId, String toUserId, double amount) {
        // Validate that both users are group members
        if (!isMember(fromUserId) || !isMember(toUserId)) {
            System.out.println("user is not a part of this group");
            return false;
        }
        
        // Update group balances
        updateGroupBalance(fromUserId, toUserId, amount);
        
        // Get user names for display
        String fromName = getUserByuserId(fromUserId).name;
        String toName = getUserByuserId(toUserId).name;
        
        // Notify group members
        notifyMembers("Settlement: " + fromName + " paid " + toName + " Rs " + amount);
        
        System.out.println("Settlement in " + name + ": " + fromName + " settled Rs " 
             + amount + " with " + toName);
        
        return true;
    }
    
    public void showGroupBalances() {
        System.out.println("\n=== Group Balances for " + name + " ===");
        DecimalFormat df = new DecimalFormat("#.##");
        
        for (Map.Entry<String, Map<String, Double>> pair : groupBalances.entrySet()) {
            String memberId = pair.getKey();
            String memberName = getUserByuserId(memberId).name;

            System.out.println(memberName + "'s balances in group:");
            
            Map<String, Double> userBalances = pair.getValue();
            if (userBalances.isEmpty()) {
                System.out.println("  No outstanding balances");
            } 
            else {
                for (Map.Entry<String, Double> userBalance : userBalances.entrySet()) {
                    String otherMemberUserId = userBalance.getKey();
                    String otherName = getUserByuserId(otherMemberUserId).name;
                    
                    double balance = userBalance.getValue();
                    if (balance > 0) {
                        System.out.println("  " + otherName + " owes: Rs " + df.format(balance));
                    } else {
                        System.out.println("  Owes " + otherName + ": Rs " + df.format(Math.abs(balance)));
                    }
                }
            }
        }
    }

    public void simplifyGroupDebts() {
        Map<String, Map<String, Double>> simplifiedBalances = DebtSimplifier.simplifyDebts(groupBalances);
        groupBalances = simplifiedBalances;
    
        System.out.println("\nDebts have been simplified for group: " + name);
    }
}
    
// Main ExpenseManager class (Singleton - Facade)
class Splitwise {
    private Map<String, User> users;
    private Map<String, Group> groups;
    private Map<String, Expense> expenses;

    private static Splitwise instance;
    
    private Splitwise() {
        users = new HashMap<>();
        groups = new HashMap<>();
        expenses = new HashMap<>();
    }
    
    public static Splitwise getInstance() {
        if(instance == null) {
            instance = new Splitwise();
        }
        return instance;
    }

    // User management
    public User createUser(String name, String email) {
        User user = new User(name, email);
        users.put(user.userId, user);
        System.out.println("User created: " + name + " (ID: " + user.userId + ")");
        return user;
    }
    
    public User getUser(String userId) {
        return users.get(userId);
    }
    
    // Group management
    public Group createGroup(String name) {
        Group group = new Group(name);
        groups.put(group.groupId, group);
        System.out.println("Group created: " + name + " (ID: " + group.groupId + ")");
        return group;
    }
    
    public Group getGroup(String groupId) {
        return groups.get(groupId);
    }
    
    public void addUserToGroup(String userId, String groupId) {
        User user = getUser(userId);
        Group group = getGroup(groupId);
        
        if (user != null && group != null) {
            group.addMember(user);
        }
    }
    
    // Try to remove user from group - just delegates to group
    public boolean removeUserFromGroup(String userId, String groupId) {
        Group group = getGroup(groupId);
        
        if (group == null) {
            System.out.println("Group not found!");
            return false;
        }
        
        User user = getUser(userId);
        if (user == null) {
            System.out.println("User not found!");
            return false;
        }

        boolean userRemoved = group.removeMember(userId);
        
        if(userRemoved) {
            System.out.println(user.name + " successfully left " + group.name);
        }
        return userRemoved;
    }
    
    // Expense management - delegate to group
    public void addExpenseToGroup(String groupId, String description, double amount, 
                          String paidByUserId, List<String> involvedUsers, 
                          SplitType splitType, List<Double> splitValues) {
        
        Group group = getGroup(groupId);
        if (group == null) {
            System.out.println("Group not found!");
            return;
        }
        
        group.addExpense(description, amount, paidByUserId, involvedUsers, splitType, splitValues);
    }
    
    public void addExpenseToGroup(String groupId, String description, double amount, 
                          String paidByUserId, List<String> involvedUsers, 
                          SplitType splitType) {
        addExpenseToGroup(groupId, description, amount, paidByUserId, involvedUsers, splitType, new ArrayList<>());
    }
    
    // Settlement - delegate to group
    public void settlePaymentInGroup(String groupId, String fromUserId, 
                              String toUserId, double amount) {
        
        Group group = getGroup(groupId);
        if (group == null) {
            System.out.println("Group not found!");
            return;
        }
        
        group.settlePayment(fromUserId, toUserId, amount);
    }
    
    // Settlement
    public void settleIndividualPayment(String fromUserId, String toUserId, double amount) {
        User fromUser = getUser(fromUserId);
        User toUser = getUser(toUserId);
        
        if (fromUser != null && toUser != null) {
            fromUser.updateBalance(toUserId, amount);
            toUser.updateBalance(fromUserId, -amount);
            
            System.out.println(fromUser.name + " settled Rs" + amount + " with " + toUser.name);
        }
    }
    
    public void addIndividualExpense(String description, double amount, String paidByUserId,
                             String toUserId, SplitType splitType,
                            List<Double> splitValues) {

        SplitStrategy strategy = SplitFactory.getSplitStrategy(splitType);
        List<Split> splits = strategy.calculateSplit(amount, Arrays.asList(paidByUserId, toUserId), splitValues);

        Expense expense = new Expense(description, amount, paidByUserId, splits);
        expenses.put(expense.expenseId, expense);
        
        User paidByUser = getUser(paidByUserId);
        User toUser = getUser(toUserId);

        paidByUser.updateBalance(toUserId, amount);
        toUser.updateBalance(paidByUserId, -amount);
        
        System.out.println("Individual expense added: " + description + " (Rs " + amount 
                + ") paid by " + paidByUser.name +" for " + toUser.name);
    }
    
    public void addIndividualExpense(String description, double amount, String paidByUserId,
                             String toUserId, SplitType splitType) {
        addIndividualExpense(description, amount, paidByUserId, toUserId, splitType, new ArrayList<>());
    }
    
    // Display Method
    public void showUserBalance(String userId) {
        User user = getUser(userId);
        if (user == null) return;
        
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println("\n=========== Balance for " + user.name +" ===================="); 
        System.out.println("Total you owe: Rs " + df.format(user.getTotalOwed()));
        System.out.println("Total others owe you: Rs " + df.format(user.getTotalOwing()));
        
        System.out.println("Detailed balances:");
        for (Map.Entry<String, Double> balance : user.balances.entrySet()) {
            User otherUser = getUser(balance.getKey());
            if (otherUser != null) {
                if (balance.getValue() > 0) {
                    System.out.println("  " + otherUser.name + " owes you: Rs" + balance.getValue());
                } else {
                    System.out.println("  You owe " + otherUser.name + ": Rs" + Math.abs(balance.getValue()));
                }
            }
        }
    }
    
    public void showGroupBalances(String groupId) {
        Group group = getGroup(groupId);
        if (group == null) return;
        
        group.showGroupBalances();
    }
    
    public void simplifyGroupDebts(String groupId) {
        Group group = getGroup(groupId);
        if (group == null) return;
                
        // Use group's balance data for debt simplification
        group.simplifyGroupDebts();
    }
}

public class SplitwiseApp {
    public static void main(String[] args) {
        
        Splitwise manager = Splitwise.getInstance();
        
        System.out.println("\n=========== Creating Users ====================");
        User user1 = manager.createUser("Aditya", "aditya@gmail.com");
        User user2 = manager.createUser("Rohit", "rohit@gmail.com");
        User user3 = manager.createUser("Manish", "manish@gmail.com");
        User user4 = manager.createUser("Saurav", "saurav@gmail.com");
        
        System.out.println("\n=========== Creating Group and Adding Members ====================");
        Group hostelGroup = manager.createGroup("Hostel Expenses");
        manager.addUserToGroup(user1.userId, hostelGroup.groupId);
        manager.addUserToGroup(user2.userId, hostelGroup.groupId);
        manager.addUserToGroup(user3.userId, hostelGroup.groupId);
        manager.addUserToGroup(user4.userId, hostelGroup.groupId);

        System.out.println("\n=========== Adding Expenses in group ====================");    
        List<String> groupMembers = Arrays.asList(user1.userId, user2.userId, user3.userId, user4.userId);
        manager.addExpenseToGroup(hostelGroup.groupId, "Lunch", 800.0, user1.userId, groupMembers, SplitType.EQUAL);
        
        List<String> dinnerMembers = Arrays.asList(user1.userId, user3.userId, user4.userId);
        List<Double> dinnerAmounts = Arrays.asList(200.0, 300.0, 200.0);
        manager.addExpenseToGroup(hostelGroup.groupId, "Dinner", 700.0, user3.userId, dinnerMembers, 
                                 SplitType.EXACT, dinnerAmounts);

        System.out.println("\n=========== printing Group-Specific Balances ===================="); 
        manager.showGroupBalances(hostelGroup.groupId);

        System.out.println("\n=========== Debt Simplification ===================="); 
        manager.simplifyGroupDebts(hostelGroup.groupId);

        System.out.println("\n=========== printing Group-Specific Balances ===================="); 
        manager.showGroupBalances(hostelGroup.groupId);

        System.out.println("\n=========== Adding Individual Expense ===================="); 
        manager.addIndividualExpense("Coffee", 40.0, user2.userId, user4.userId, SplitType.EQUAL);
        
        System.out.println("\n=========== printing User Balances ===================="); 
        manager.showUserBalance(user1.userId);
        manager.showUserBalance(user2.userId);
        manager.showUserBalance(user3.userId);
        manager.showUserBalance(user4.userId);

        System.out.println("\n==========Attempting to remove Rohit from group==========");
        manager.removeUserFromGroup(user2.userId, hostelGroup.groupId);

        System.out.println("\n======== Making Settlement to Clear Rohit's Debt =========="); 
        manager.settlePaymentInGroup(hostelGroup.groupId, user2.userId, user3.userId, 200.0);
        
        System.out.println("\n======== Attempting to Remove Rohit Again ==========");
        manager.removeUserFromGroup(user2.userId, hostelGroup.groupId);
        
        System.out.println("\n=========== Updated Group Balances ===================="); 
        manager.showGroupBalances(hostelGroup.groupId);
    }
}